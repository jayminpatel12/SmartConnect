package com.jaymin.smartconnect.ui.screens.scanner

import android.Manifest
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SignalCellularAlt1Bar
import androidx.compose.material.icons.filled.SignalCellularAlt2Bar
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.jaymin.smartconnect.core.domain.model.BluetoothDeviceInfo
import com.jaymin.smartconnect.core.domain.model.DeviceType
import com.jaymin.smartconnect.core.domain.model.SignalStrength

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(viewModel: ScannerViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    val permissionState = rememberMultiplePermissionsState(permissions)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bluetooth Scanner") },
                actions = {
                    if (uiState.isScanning) {
                        val transition = rememberInfiniteTransition(label = "pulse")
                        val alpha by transition.animateFloat(
                            initialValue = 1f, targetValue = 0.3f,
                            animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
                            label = "pulse"
                        )
                        Icon(
                            Icons.Default.BluetoothSearching,
                            contentDescription = "Scanning",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 16.dp).alpha(alpha)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (!permissionState.allPermissionsGranted) {
                        permissionState.launchMultiplePermissionRequest()
                    } else if (uiState.isScanning) {
                        viewModel.stopScan()
                    } else {
                        viewModel.startScan()
                    }
                },
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.Bluetooth,
                    contentDescription = if (uiState.isScanning) "Stop" else "Scan"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
        ) {
            if (!permissionState.allPermissionsGranted) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Bluetooth permissions required", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                            Text("Grant Permissions")
                        }
                    }
                }
            }

            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(error, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
                }
            }

            if (uiState.isScanning && uiState.scannedDevices.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Scanning for nearby devices...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (uiState.scannedDevices.isNotEmpty()) {
                Text(
                    "Found ${uiState.scannedDevices.size} device(s)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.scannedDevices, key = { it.address }) { device ->
                    DeviceCard(
                        device = device,
                        isConnecting = uiState.connectingTo == device.address,
                        onConnect = { viewModel.connectTo(device) }
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceCard(
    device: BluetoothDeviceInfo,
    isConnecting: Boolean,
    onConnect: () -> Unit
) {
    Card(
        onClick = onConnect,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = device.type.icon(),
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(device.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(device.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                if (device.isPaired) {
                    Text("Paired", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }

            if (isConnecting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Icon(
                    imageVector = device.signalStrength.icon(),
                    contentDescription = "Signal: ${device.signalStrength}",
                    tint = device.signalStrength.color()
                )
            }
        }
    }
}

fun DeviceType.icon(): ImageVector = when (this) {
    DeviceType.PHONE -> Icons.Default.PhoneAndroid
    DeviceType.COMPUTER -> Icons.Default.Computer
    DeviceType.AUDIO -> Icons.Default.Headphones
    DeviceType.PERIPHERAL -> Icons.Default.Keyboard
    DeviceType.WEARABLE -> Icons.Default.Watch
    DeviceType.UNKNOWN -> Icons.Default.DeviceUnknown
}

fun SignalStrength.icon(): ImageVector = when (this) {
    SignalStrength.EXCELLENT -> Icons.Default.SignalCellular4Bar
    SignalStrength.GOOD -> Icons.Default.SignalCellularAlt
    SignalStrength.FAIR -> Icons.Default.SignalCellularAlt2Bar
    SignalStrength.WEAK -> Icons.Default.SignalCellularAlt1Bar
}

@Composable
fun SignalStrength.color() = when (this) {
    SignalStrength.EXCELLENT -> MaterialTheme.colorScheme.primary
    SignalStrength.GOOD -> MaterialTheme.colorScheme.secondary
    SignalStrength.FAIR -> MaterialTheme.colorScheme.tertiary
    SignalStrength.WEAK -> MaterialTheme.colorScheme.error
}
