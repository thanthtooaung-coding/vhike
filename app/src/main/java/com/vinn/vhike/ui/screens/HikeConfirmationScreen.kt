package com.vinn.vhike.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.vinn.vhike.data.db.Hike
import com.vinn.vhike.ui.theme.AppTeal
import com.vinn.vhike.ui.theme.LightGray
import com.vinn.vhike.ui.viewmodel.HikeViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HikeConfirmationScreen(
    hikeId: Long,
    onNavigateBack: () -> Unit,
    onEditHike: (Long) -> Unit,
    viewModel: HikeViewModel = hiltViewModel()
) {
    val allHikes by viewModel.allHikes.collectAsState(initial = emptyList())
    val hike = allHikes.find { it.id == hikeId }

    val scrollState = rememberScrollState()

    if (hike == null) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Hike Confirmation") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = Color.Black
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Hike not found or loading...")
            }
        }
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Hike Confirmation") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        TextButton(onClick = onNavigateBack) {
                            Text(
                                "Done",
                                color = AppTeal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = Color.Black
                    )
                )
            },
            bottomBar = {
                Button(
                    onClick = { onEditHike(hikeId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppTeal),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Edit Hike",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        ) { paddingValues ->

            val cameraPositionState = rememberCameraPositionState()
            val uiSettings = remember {
                MapUiSettings(
                    zoomControlsEnabled = false,
                    zoomGesturesEnabled = false,
                    scrollGesturesEnabled = false,
                    rotationGesturesEnabled = false,
                    tiltGesturesEnabled = false
                )
            }
            val hikeLocation = LatLng(hike.latitude ?: 0.0, hike.longitude ?: 0.0)

            LaunchedEffect(hikeLocation) {
                if (hike.latitude != null && hike.longitude != null) {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(hikeLocation, 12f)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .background(Color.White)
            ) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(12.dp))
                ) {
                    if (hike.latitude != null && hike.longitude != null) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            uiSettings = uiSettings
                        ) {
                            Marker(
                                state = MarkerState(position = hikeLocation),
                                title = hike.hikeName
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No map data available", color = Color.Gray)
                        }
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = hike.location,
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                    Text(
                        text = hike.hikeName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                    Text(
                        text = dateFormat.format(hike.hikeDate),
                        color = Color.Gray,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            icon = Icons.Default.Straighten,
                            label = "Length",
                            value = "${hike.hikeLength} ${"km"}", // Assuming km, you can grab from hike
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            icon = Icons.Default.Timer,
                            label = "Duration",
                            value = hike.duration.ifBlank { "N/A" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            icon = Icons.Default.Landscape,
                            label = "Difficulty",
                            value = hike.difficultyLevel,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            icon = Icons.Default.Loop,
                            label = "Trail Type",
                            value = hike.trailType,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    InfoCard(
                        icon = Icons.Default.LocalParking,
                        text = "Parking ${if (hike.parkingAvailable) "Available" else "Not Available"}"
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Notes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = hike.description ?: "No notes added for this hike.",
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// Re-using the StatCard and InfoCard composables from the original file
@Composable
fun StatCard(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(LightGray)
            .padding(16.dp)
    ) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = AppTeal,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = label, color = Color.Gray, fontSize = 14.sp)
            Text(text = value, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun InfoCard(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LightGray)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = AppTeal,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
