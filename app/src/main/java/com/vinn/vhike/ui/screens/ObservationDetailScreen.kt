package com.vinn.vhike.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.vinn.vhike.data.db.Observation
import com.vinn.vhike.ui.viewmodel.HikeViewModel
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservationDetailScreen(
    observationId: Long,
    onNavigateBack: () -> Unit,
    viewModel: HikeViewModel = hiltViewModel()
) {
    val observation by produceState<Observation?>(initialValue = null, observationId) {
        value = viewModel.getObservationDetails(observationId).firstOrNull()
    }

    val scrollState = rememberScrollState()
    val dateTimeFormat = SimpleDateFormat("MMMM dd, yyyy 'at' h:mm a", Locale.getDefault())

    val backgroundColor = Color(0xFFF5F5F5)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Observation Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Edit logic */ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { /* TODO: Delete logic */ }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            if (observation == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = observation!!.photoUrl ?: "https://placehold.co/600x400/e0e0e0/666666?text=Observation+Photo",
                        ),
                        contentDescription = observation!!.observationText,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .aspectRatio(16 / 9f)
                            .clip(RoundedCornerShape(16.dp))
                    )

                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        // Title
                        Text(
                            text = observation!!.observationText,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C5F5D)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Time",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray,
                                fontWeight = FontWeight.Normal
                            )
                            Text(
                                text = dateTimeFormat.format(observation!!.observationTime),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 1.dp,
                            color = Color.Gray.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Comments",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = observation!!.additionalComments?.takeIf { it.isNotBlank() } ?: "No comments provided.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black,
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Location",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C5F5D)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val obsLocation = if (observation!!.latitude != null && observation!!.longitude != null) {
                            LatLng(observation!!.latitude!!, observation!!.longitude!!)
                        } else {
                            null
                        }

                        if (obsLocation != null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Auto-Captured (Lat: ${String.format("%.4f", obsLocation.latitude)}, Lon: ${String.format("%.4f", obsLocation.longitude)})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    ) {
                                        val cameraState = rememberCameraPositionState {
                                            position = CameraPosition.fromLatLngZoom(obsLocation, 14f)
                                        }
                                        GoogleMap(
                                            modifier = Modifier.fillMaxSize(),
                                            cameraPositionState = cameraState,
                                            uiSettings = MapUiSettings(
                                                zoomControlsEnabled = false,
                                                scrollGesturesEnabled = false
                                            )
                                        ) {
                                            Marker(state = MarkerState(obsLocation))
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                "No location data captured for this observation.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}