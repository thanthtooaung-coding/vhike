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

    // --- NEW: Observation Form State ---
    private val _addObservationUiState = MutableStateFlow(AddObservationFormState())
    val addObservationUiState: StateFlow<AddObservationFormState> = _addObservationUiState.asStateFlow()

    // --- Hike Form Functions ---
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
        // ... (validation logic) ...
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
            val newId = hikeRepository.addNewHike(newHike)
            _addHikeUiState.value = AddHikeFormState()
            _newHikeId.value = newId
        }
    }

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

    fun getObservationDetails(observationId: Long): Flow<Observation?> {
        return hikeRepository.getObservationDetails(observationId)
    }

    fun onObservationTextChanged(text: String) {
        _addObservationUiState.value = _addObservationUiState.value.copy(observationText = text, errorMessage = null)
    }

    fun onObservationTimeChanged(date: Date) {
        _addObservationUiState.value = _addObservationUiState.value.copy(observationTime = date)
    }

    fun onObservationCommentsChanged(comments: String) {
        _addObservationUiState.value = _addObservationUiState.value.copy(additionalComments = comments)
    }

    fun onObservationPhotoAdded(url: String) {
        _addObservationUiState.value = _addObservationUiState.value.copy(photoUrl = url)
    }

    fun onObservationLocationSet(latLng: LatLng) {
        _addObservationUiState.value = _addObservationUiState.value.copy(
            latitude = latLng.latitude,
            longitude = latLng.longitude
        )
    }

    fun resetObservationForm() {
        _addObservationUiState.value = AddObservationFormState()
    }

    fun saveNewObservation(hikeId: Long) {
        val currentState = _addObservationUiState.value
        if (currentState.observationText.isBlank()) {
            _addObservationUiState.value = currentState.copy(errorMessage = "Observation text is required.")
            return
        }

        viewModelScope.launch {
            val newObservation = Observation(
                hikeId = hikeId,
                observationText = currentState.observationText,
                observationTime = currentState.observationTime ?: Date(),
                additionalComments = currentState.additionalComments,
                photoUrl = currentState.photoUrl,
                latitude = currentState.latitude,
                longitude = currentState.longitude
            )
            hikeRepository.addObservation(newObservation)

            _addObservationUiState.value = AddObservationFormState()
        }
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
    val elevation: String = "",
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

data class AddObservationFormState(
    val observationText: String = "",
    val observationTime: Date? = Date(),
    val additionalComments: String = "",
    val photoUrl: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val errorMessage: String? = null
)