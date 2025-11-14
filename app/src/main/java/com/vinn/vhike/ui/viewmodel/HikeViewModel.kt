package com.vinn.vhike.ui.viewmodel

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.vinn.vhike.data.db.Hike
import com.vinn.vhike.data.db.Observation
import com.vinn.vhike.data.repository.HikeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HikeViewModel @Inject constructor(
    private val hikeRepository: HikeRepository
) : ViewModel() {

    val allHikes: Flow<List<Hike>> = hikeRepository.allHikes

    private val _addHikeUiState = MutableStateFlow(AddHikeFormState())
    val addHikeUiState: StateFlow<AddHikeFormState> = _addHikeUiState.asStateFlow()

    private val _newHikeId = MutableStateFlow<Long?>(null)
    val newHikeId: StateFlow<Long?> = _newHikeId.asStateFlow()

    private val _searchFilterState = MutableStateFlow(SearchFilters())
    val searchFilterState: StateFlow<SearchFilters> = _searchFilterState.asStateFlow()

    private val _searchResultState = MutableStateFlow<List<Hike>>(emptyList())
    val searchResultState: StateFlow<List<Hike>> = _searchResultState.asStateFlow()

    fun onHikeNameChanged(name: String) {
        _addHikeUiState.value = _addHikeUiState.value.copy(hikeName = name, errorMessage = null)
    }
    fun onLocationChanged(location: String) {
        _addHikeUiState.value = _addHikeUiState.value.copy(location = location, errorMessage = null)
    }
    fun onDescriptionChanged(description: String) {
        _addHikeUiState.value = _addHikeUiState.value.copy(description = description)
    }
    fun onDateSelected(date: Date) {
        _addHikeUiState.value = _addHikeUiState.value.copy(hikeDate = date, errorMessage = null)
    }
    fun onLengthChanged(length: String) {
        val lengthAsDouble = length.toDoubleOrNull()
        _addHikeUiState.value = _addHikeUiState.value.copy(hikeLength = lengthAsDouble, errorMessage = null)
    }
    fun onLengthUnitChanged(unit: String) {
        _addHikeUiState.value = _addHikeUiState.value.copy(lengthUnit = unit)
    }
    fun onDurationChanged(duration: String) {
        _addHikeUiState.value = _addHikeUiState.value.copy(duration = duration)
    }
    // NEW: Handle elevation
    fun onElevationChanged(elevation: String) {
        _addHikeUiState.value = _addHikeUiState.value.copy(elevation = elevation)
    }
    fun onDifficultyChanged(difficulty: String) {
        _addHikeUiState.value = _addHikeUiState.value.copy(difficultyLevel = difficulty)
    }
    fun onParkingChanged(available: Boolean) {
        _addHikeUiState.value = _addHikeUiState.value.copy(parkingAvailable = available)
    }
    fun onTrailTypeChanged(trailType: String) {
        _addHikeUiState.value = _addHikeUiState.value.copy(trailType = trailType)
    }

    fun onLocationSelectedFromMap(latLng: LatLng, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val geocoder = Geocoder(context, Locale.getDefault())
            var locationName = "${latLng.latitude}, ${latLng.longitude}"
            try {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    locationName = addresses[0].getAddressLine(0) ?: locationName
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            withContext(Dispatchers.Main) {
                _addHikeUiState.value = _addHikeUiState.value.copy(
                    location = locationName,
                    latitude = latLng.latitude,
                    longitude = latLng.longitude,
                    errorMessage = null
                )
            }
        }
    }

    fun saveNewHike() {
        val currentState = _addHikeUiState.value
        if (currentState.hikeName.isBlank() || currentState.location.isBlank()) {
            _addHikeUiState.value = currentState.copy(errorMessage = "Name and Location are required.")
            return
        }
        if (currentState.hikeDate == null) {
            _addHikeUiState.value = currentState.copy(errorMessage = "Please select a date.")
            return
        }
        if (currentState.hikeLength == null || currentState.hikeLength <= 0.0) {
            _addHikeUiState.value = currentState.copy(errorMessage = "Please enter a valid length.")
            return
        }
        // NEW: Parse elevation
        val elevationAsDouble = currentState.elevation.toDoubleOrNull()

        viewModelScope.launch {
            val newHike = Hike(
                hikeName = currentState.hikeName,
                location = currentState.location,
                hikeDate = currentState.hikeDate,
                parkingAvailable = currentState.parkingAvailable,
                hikeLength = currentState.hikeLength,
                difficultyLevel = currentState.difficultyLevel,
                trailType = currentState.trailType,
                description = currentState.description,
                latitude = currentState.latitude,
                longitude = currentState.longitude,
                duration = currentState.duration,
                elevation = elevationAsDouble
            )
            // MODIFIED: Get new ID from repository
            val newId = hikeRepository.addNewHike(newHike)

            // Reset form
            _addHikeUiState.value = AddHikeFormState()

            // NEW: Signal navigation
            _newHikeId.value = newId
        }
    }

    // NEW: Reset navigation signal
    fun onNavigationToConfirmationDone() {
        _newHikeId.value = null
    }

    // --- Search Functions ---
    fun onSearchNameChanged(name: String) {
        _searchFilterState.value = _searchFilterState.value.copy(name = name)
    }
    fun onSearchLocationChanged(location: String) {
        _searchFilterState.value = _searchFilterState.value.copy(location = location)
    }
    fun onSearchDateSelected(date: Date?) {
        _searchFilterState.value = _searchFilterState.value.copy(selectedDate = date)
    }
    fun onSearchLengthRangeChanged(range: ClosedRange<Double>) {
        _searchFilterState.value = _searchFilterState.value.copy(lengthRange = range)
    }

    fun executeSearch() {
        val filters = _searchFilterState.value
        viewModelScope.launch {
            hikeRepository.performSearch(
                name = filters.name,
                location = filters.location,
                date = filters.selectedDate,
                lengthMin = filters.lengthRange?.start,
                lengthMax = filters.lengthRange?.endInclusive
            ).collect { results ->
                _searchResultState.value = results
            }
        }
    }

    fun resetSearch() {
        _searchFilterState.value = SearchFilters()
        _searchResultState.value = emptyList()
    }

    // --- Observation Functions ---
    fun getObservationsForHike(hikeId: Long): Flow<List<Observation>> {
        return hikeRepository.getObservations(hikeId)
    }
}

data class AddHikeFormState(
    val hikeName: String = "",
    val location: String = "",
    val description: String = "",
    val hikeDate: Date? = null,
    val hikeLength: Double? = null,
    val lengthUnit: String = "km",
    val duration: String = "",
    val elevation: String = "", // NEW
    val difficultyLevel: String = "Easy",
    val parkingAvailable: Boolean = false,
    val trailType: String = "Loop",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val errorMessage: String? = null
)

data class SearchFilters(
    val name: String? = null,
    val location: String? = null,
    val selectedDate: Date? = null,
    val lengthRange: ClosedRange<Double>? = null
)