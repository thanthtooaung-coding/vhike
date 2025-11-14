package com.vinn.vhike.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.vinn.vhike.ui.theme.AppTeal
import com.vinn.vhike.ui.theme.LightGray
import com.vinn.vhike.ui.viewmodel.HikeViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddObservationScreen(
    hikeId: Long,
    onNavigateBack: () -> Unit,
    viewModel: HikeViewModel = hiltViewModel()
) {
    val uiState by viewModel.addObservationUiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        }
    )

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(hasLocationPermission, uiState.latitude) {
        if (hasLocationPermission && uiState.latitude == null) {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.onObservationLocationSet(LatLng(location.latitude, location.longitude))
                    }
                }
            }
        }
    }

    val calendar = Calendar.getInstance()
    uiState.observationTime?.let { calendar.time = it }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd, h:mm a", Locale.getDefault())

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            viewModel.onObservationTimeChanged(calendar.time)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            viewModel.onObservationTimeChanged(calendar.time)
            timePickerDialog.show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log New Observation") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetObservationForm()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                onClick = {
                    viewModel.saveNewObservation(hikeId)
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppTeal),
                shape = RoundedCornerShape(16.dp),
                enabled = uiState.observationText.isNotBlank()
            ) {
                Text(
                    text = "Save Observation",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
                .background(Color.White)
        ) {
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            FormTextField(
                label = "Observation*",
                placeholder = "e.g., Deer Sighting",
                value = uiState.observationText,
                onValueChange = { viewModel.onObservationTextChanged(it) }
            )

            FormTextField(
                label = "Time of the observation*",
                placeholder = "Select date and time",
                value = uiState.observationTime?.let { dateFormat.format(it) } ?: "",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.CalendarMonth, "Select Date") },
                modifier = Modifier.clickable { datePickerDialog.show() }
            )

            FormTextField(
                label = "Additional comments",
                placeholder = "Add more details here...",
                value = uiState.additionalComments,
                onValueChange = { viewModel.onObservationCommentsChanged(it) },
                singleLine = false,
                modifier = Modifier.height(120.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Location",
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LightGray)
                    .border(1.dp, Color.Gray, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                val defaultLocation = LatLng(40.7128, -74.0060)
                val observationLocation = if (uiState.latitude != null && uiState.longitude != null) {
                    LatLng(uiState.latitude!!, uiState.longitude!!)
                } else {
                    null
                }

                val cameraState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(
                        observationLocation ?: defaultLocation, 10f
                    )
                }

                LaunchedEffect(observationLocation) {
                    if (observationLocation != null) {
                        cameraState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(observationLocation, 15f),
                            durationMs = 1000
                        )
                    }
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraState,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        scrollGesturesEnabled = true
                    ),
                    onMapClick = { newLatLng ->
                        viewModel.onObservationLocationSet(newLatLng)
                    }
                ) {
                    if (observationLocation != null) {
                        Marker(state = MarkerState(observationLocation), title = "Observation Location")
                    }
                }

                val locationText = if (observationLocation != null) {
                    "Location: ${String.format("%.4f", observationLocation.latitude)}, ${String.format("%.4f", observationLocation.longitude)}"
                } else if (hasLocationPermission) {
                    "Getting location... (Tap map to set)"
                } else {
                    "Location permission denied. (Tap map to set)"
                }

                Text(
                    text = locationText,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(8.dp),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { /* TODO: Launch camera intent */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppTeal),
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Add Photo")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Photo/Video")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}