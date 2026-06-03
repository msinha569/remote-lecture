package com.example

import android.app.Application
import android.content.Context
import android.net.DhcpInfo
import android.net.wifi.WifiManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface

sealed interface ConnectionStatus {
    object Disconnected : ConnectionStatus
    object Connecting : ConnectionStatus
    data class Connected(val ip: String) : ConnectionStatus
}

data class RemoteUiState(
    val laptopIp: String = "",
    val connectionStatus: ConnectionStatus = ConnectionStatus.Disconnected,
    val isScanning: Boolean = false,
    val discoveredDevices: List<String> = emptyList(),
    val commandHistory: List<String> = emptyList(),
    val useVibration: Boolean = true,
    val port: Int = 5005
)

class RemoteViewModel(application: Application) : AndroidViewModel(application) {

    private val PREFS_NAME = "lecture_remote_prefs"
    private val KEY_LAPTOP_IP = "laptop_ip"
    private val KEY_USE_VIBRATION = "use_vibration"
    private val sharedPrefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(RemoteUiState())
    val uiState: StateFlow<RemoteUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null
    private var responseListenerSocket: DatagramSocket? = null

    init {
        val savedIp = sharedPrefs.getString(KEY_LAPTOP_IP, "") ?: ""
        val savedVibe = sharedPrefs.getBoolean(KEY_USE_VIBRATION, true)
        _uiState.update { 
            it.copy(
                laptopIp = savedIp,
                useVibration = savedVibe,
                connectionStatus = if (savedIp.isNotEmpty()) ConnectionStatus.Connected(savedIp) else ConnectionStatus.Disconnected
            )
        }
    }

    fun setLaptopIp(ip: String) {
        val cleanIp = ip.trim()
        sharedPrefs.edit().putString(KEY_LAPTOP_IP, cleanIp).apply()
        _uiState.update { 
            it.copy(
                laptopIp = cleanIp,
                connectionStatus = if (cleanIp.isNotEmpty()) ConnectionStatus.Connected(cleanIp) else ConnectionStatus.Disconnected
            )
        }
    }

    fun setUseVibration(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_USE_VIBRATION, enabled).apply()
        _uiState.update { it.copy(useVibration = enabled) }
    }

    fun sendCommand(command: String) {
        val uiVal = _uiState.value
        val ip = uiVal.laptopIp
        if (ip.isEmpty()) {
            addLog("Error: No laptop IP set")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val socket = DatagramSocket()
                val byteData = command.toByteArray()
                val address = InetAddress.getByName(ip)
                val packet = DatagramPacket(byteData, byteData.size, address, uiVal.port)
                socket.send(packet)
                socket.close()
                addLog("Sent: $command")
            } catch (e: Exception) {
                addLog("Fail to send $command: ${e.message}")
            }
        }
    }

    fun testConnection(ip: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(connectionStatus = ConnectionStatus.Connecting) }
            try {
                // Send discovery ping directly to test connection
                val socket = DatagramSocket()
                socket.soTimeout = 1500
                val byteData = "DISCOVER".toByteArray()
                val address = InetAddress.getByName(ip)
                val packet = DatagramPacket(byteData, byteData.size, address, _uiState.value.port)
                socket.send(packet)

                // Wait for response back (the laptop python script sends ACK_REMOTE)
                val responseBuf = ByteArray(512)
                val responsePacket = DatagramPacket(responseBuf, responseBuf.size)
                socket.receive(responsePacket)
                val response = String(responsePacket.data, 0, responsePacket.length).trim()
                socket.close()

                if (response == "ACK_REMOTE") {
                    setLaptopIp(ip)
                    addLog("Test succeeded: Laptop auto-discovered!")
                } else {
                    addLog("Test received invalid response: $response")
                    _uiState.update { it.copy(connectionStatus = ConnectionStatus.Disconnected) }
                }
            } catch (e: Exception) {
                addLog("Test failed directly: ${e.message}")
                _uiState.update { it.copy(connectionStatus = ConnectionStatus.Disconnected) }
            }
        }
    }

    fun startAutoDiscovery() {
        if (_uiState.value.isScanning) return

        _uiState.update { it.copy(isScanning = true, discoveredDevices = emptyList()) }
        addLog("Starting network auto-discovery...")

        scanJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val socket = DatagramSocket()
                socket.broadcast = true
                socket.soTimeout = 3000
                responseListenerSocket = socket

                // Launch listener coroutine
                val listenerJob = launch {
                    val buffer = ByteArray(1024)
                    val recPacket = DatagramPacket(buffer, buffer.size)
                    while (socket.isBound && !socket.isClosed) {
                        try {
                            socket.receive(recPacket)
                            val foundIp = recPacket.address.hostAddress ?: continue
                            val message = String(recPacket.data, 0, recPacket.length).trim()
                            if (message == "ACK_REMOTE") {
                                Log.i("LectureRemote", "Discovered server at $foundIp")
                                _uiState.update { current ->
                                    if (!current.discoveredDevices.contains(foundIp)) {
                                        current.copy(discoveredDevices = current.discoveredDevices + foundIp)
                                    } else current
                                }
                            }
                        } catch (e: Exception) {
                            break
                        }
                    }
                }

                val byteData = "DISCOVER".toByteArray()
                
                // 1. Send typical local broadcasts
                try {
                    val broadcastAddr = InetAddress.getByName("255.255.255.255")
                    socket.send(DatagramPacket(byteData, byteData.size, broadcastAddr, _uiState.value.port))
                } catch (e: Exception) {
                    Log.e("LectureRemote", "Broadcast fail: ${e.message}")
                }

                // 2. Compute subnet broadcast or direct scan
                val localIp = getLocalIpAddress()
                if (localIp != null) {
                    val parts = localIp.split(".")
                    if (parts.size == 4) {
                        val subnet = "${parts[0]}.${parts[1]}.${parts[2]}"
                        
                        // Send subnet-level broadcast
                        try {
                            val subnetBroadcast = InetAddress.getByName("$subnet.255")
                            socket.send(DatagramPacket(byteData, byteData.size, subnetBroadcast, _uiState.value.port))
                        } catch (e: Exception) {
                            Log.e("LectureRemote", "Subnet broadcast fail: ${e.message}")
                        }

                        // Superfast unicast sweep in IO Dispatcher
                        addLog("Sweeping subnet $subnet.1 - 254...")
                        for (i in 1..254) {
                            if (i == parts[3].toIntOrNull()) continue
                            launch {
                                try {
                                    val unicastSocket = DatagramSocket()
                                    val sweepAddress = InetAddress.getByName("$subnet.$i")
                                    val sweepPacket = DatagramPacket(byteData, byteData.size, sweepAddress, _uiState.value.port)
                                    unicastSocket.send(sweepPacket)
                                    unicastSocket.close()
                                } catch (e: Exception) {
                                    // Ignore failures during broad sweep
                                }
                            }
                        }
                    }
                }

                // Wait 4 seconds for scan completion
                delay(4000)
                listenerJob.cancel()
                socket.close()

            } catch (e: Exception) {
                addLog("Discovery issue: ${e.message}")
            } finally {
                _uiState.update { it.copy(isScanning = false) }
                addLog("Discovery completed. Found ${_uiState.value.discoveredDevices.size} laptop(s).")
            }
        }
    }

    fun stopAutoDiscovery() {
        scanJob?.cancel()
        responseListenerSocket?.close()
        _uiState.update { it.copy(isScanning = false) }
        addLog("Discovery stopped.")
    }

    private fun addLog(msg: String) {
        val timeStamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        _uiState.update { 
            val updatedHistory = listOf("[$timeStamp] $msg") + it.commandHistory.take(29)
            it.copy(commandHistory = updatedHistory)
        }
    }

    fun clearLog() {
        _uiState.update { it.copy(commandHistory = emptyList()) }
    }

    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val inetAddress = addresses.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is java.net.Inet4Address) {
                        return inetAddress.hostAddress
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("LectureRemote", "Error getting IP", ex)
        }
        return null
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoDiscovery()
    }
}
