package com.vinn.vhike.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search // NEW
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext // NEW
import com.google.android.gms.maps.CameraUpdateFactory // NEW
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place // NEW
import com.google.android.libraries.places.widget.Autocomplete // NEW
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode // NEW
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.vinn.vhike.ui.theme.AppTeal
import kotlinx.coroutines.launch // NEW

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    onLocationSelected: (LatLng) -> Unit,
    onNavigateBack: () -> Unit
) {
    val defaultLocation = LatLng(16.8053, 96.1561)
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 7f)
    }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
    val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
        .build(context)

    val autocompleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let {
                val place = Autocomplete.getPlaceFromIntent(it)
                place.latLng?.let { latLng ->
                    selectedLocation = latLng
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                        )
                    }
                }
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {

        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Location") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                // --- NEW: Search Action ---
                actions = {
                    IconButton(onClick = { autocompleteLauncher.launch(intent) }) {
                        Icon(Icons.Default.Search, "Search Location")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        floatingActionButton = {
            if (selectedLocation != null) {
                FloatingActionButton(
                    onClick = { onLocationSelected(selectedLocation!!) },
                    containerColor = AppTeal
                ) {
                    Icon(Icons.Default.Check, "Confirm Location", tint = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    selectedLocation = latLng
                }
            ) {
                if (selectedLocation != null) {
                    Marker(
                        state = MarkerState(position = selectedLocation!!),
                        title = "Selected Location"
                    )
                }
            }
        }
    }
}