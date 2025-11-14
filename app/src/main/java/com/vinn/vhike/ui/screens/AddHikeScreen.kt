package com.vinn.vhike.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import com.google.android.gms.maps.model.LatLng
import com.vinn.vhike.ui.theme.AppTeal
import com.vinn.vhike.ui.theme.LightGray
import com.vinn.vhike.ui.viewmodel.AddHikeFormState
import com.vinn.vhike.ui.viewmodel.HikeViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHikeScreen(
    navBackStackEntry: NavBackStackEntry,
    onNavigateBack: () -> Unit,
    onNavigateToMap: () -> Unit,
    onHikeSaved: (Long) -> Unit, // ADDED: Callback for navigation
    viewModel: HikeViewModel = hiltViewModel()
) {
    val uiState by viewModel.addHikeUiState.collectAsState()
    val scrollState = rememberScrollState()

    val context = LocalContext.current

    // NEW: Observe save state and navigate
    val newHikeId by viewModel.newHikeId.collectAsState()

    LaunchedEffect(newHikeId) {
        newHikeId?.let { id ->
            onHikeSaved(id)
            viewModel.onNavigationToConfirmationDone() // Reset the state
        }
    }

    LaunchedEffect(navBackStackEntry) {
        navBackStackEntry.savedStateHandle.getLiveData<LatLng>("pickedLocation")
            .observe(navBackStackEntry) { latLng ->
                if (latLng != null) {
                    viewModel.onLocationSelectedFromMap(latLng, context)
                    navBackStackEntry.savedStateHandle.remove<LatLng>("pickedLocation")
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add a New Hike") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                    viewModel.saveNewHike() // Just call save, LaunchedEffect handles navigation
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppTeal),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Save Hike",
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

            // --- Name of hike ---
            FormTextField(
                label = "Name of hike",
                placeholder = "e.g. Eagle Peak Trail",
                value = uiState.hikeName,
                onValueChange = { viewModel.onHikeNameChanged(it) }
            )

            // --- Location ---
            FormTextField(
                label = "Location",
                placeholder = "Search or select on map",
                value = uiState.location,
                onValueChange = { viewModel.onLocationChanged(it) },
                trailingIcon = {
                    IconButton(onClick = onNavigateToMap) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Select on Map")
                    }
                }
            )

            // --- Description ---
            FormTextField(
                label = "Description",
                placeholder = "Share some details about the hike...",
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChanged(it) },
                singleLine = false,
                modifier = Modifier.height(120.dp)
            )

            // --- Date of the hike ---
            DatePickerField(
                label = "Date of the hike",
                selectedDate = uiState.hikeDate,
                onDateSelected = { viewModel.onDateSelected(it) }
            )

            // --- Length (Full Width) ---
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Length",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, LightGray, RoundedCornerShape(12.dp))
                ) {
                    OutlinedTextField(
                        value = uiState.hikeLength?.toString() ?: "",
                        onValueChange = { viewModel.onLengthChanged(it) },
                        placeholder = { Text("e.g. 5.2") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )
                    // Unit Toggle (mi/km)
                    UnitToggle(
                        selectedUnit = uiState.lengthUnit,
                        onUnitChange = { viewModel.onLengthUnitChanged(it) }
                    )
                }
            }

            // --- Duration & Elevation ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TimePickerField(
                    label = "Duration",
                    value = uiState.duration,
                    onTimeSelected = { viewModel.onDurationChanged(it) },
                    modifier = Modifier.weight(1f)
                )
                // NEW: Elevation Field
                FormTextField(
                    label = "Elevation (ft)",
                    placeholder = "e.g. 2425",
                    value = uiState.elevation,
                    onValueChange = { viewModel.onElevationChanged(it) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            // --- Level of difficulty ---
            DropdownField(
                label = "Level of difficulty",
                options = listOf("Easy", "Moderate", "Difficult"),
                selectedValue = uiState.difficultyLevel,
                onValueSelected = { viewModel.onDifficultyChanged(it) }
            )

            // --- Parking available ---
            SettingSwitch(
                text = "Parking available",
                isChecked = uiState.parkingAvailable,
                onCheckedChange = { viewModel.onParkingChanged(it) }
            )

            // --- Trail Type ---
            RadioGroupField(
                label = "Trail Type",
                options = listOf("Loop", "Out & Back", "Multi-day"),
                selectedValue = uiState.trailType,
                onValueSelected = { viewModel.onTrailTypeChanged(it) }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- Reusable Composables for the Form ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormTextField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default, // MODIFIED
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = LightGray,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = AppTeal
            ),
            singleLine = singleLine,
            readOnly = readOnly,
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions // MODIFIED
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    selectedDate: Date?,
    onDateSelected: (Date) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    if (selectedDate != null) {
        calendar.time = selectedDate
    }

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    val dateText = selectedDate?.let { dateFormat.format(it) } ?: ""

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                val newCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                onDateSelected(newCalendar.time)
            }, year, month, day
        )
    }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = dateText,
            onValueChange = {},
            placeholder = { Text("Select a date", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { datePickerDialog.show() }, // Make the whole field clickable
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = LightGray,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = AppTeal
            ),
            readOnly = true,
            enabled = false, // Disable text field interaction
            trailingIcon = {
                Icon(Icons.Default.CalendarToday, "Select Date")
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    label: String,
    value: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val timeParts = value.split(":").map { it.trim() }
    val currentHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 0
    val currentMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, selectedHour: Int, selectedMinute: Int ->
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                onTimeSelected(formattedTime)
            },
            currentHour,
            currentMinute,
            true // true for 24-hour format, false for 12-hour format
        )
    }

    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = {},
            placeholder = { Text("HH:MM", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { timePickerDialog.show() },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = LightGray,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = AppTeal
            ),
            readOnly = true,
            enabled = false,
            trailingIcon = {
                Icon(Icons.Default.Timer, "Select Time")
            }
        )
    }
}

@Composable
fun UnitToggle(selectedUnit: String, onUnitChange: (String) -> Unit) {
    val units = listOf("mi", "km")
    Row(
        modifier = Modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(LightGray)
    ) {
        units.forEach { unit ->
            val isSelected = unit == selectedUnit
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) AppTeal else Color.Transparent)
                    .clickable { onUnitChange(unit) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = unit,
                    color = if (isSelected) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selectedValue: String,
    onValueSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = LightGray,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = AppTeal
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingSwitch(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = text, fontSize = 16.sp)
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AppTeal,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.Gray
            )
        )
    }
}

@Composable
fun RadioGroupField(
    label: String,
    options: List<String>,
    selectedValue: String,
    onValueSelected: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 2.dp,
                        color = if (option == selectedValue) AppTeal else LightGray,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onValueSelected(option) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = option == selectedValue,
                    onClick = { onValueSelected(option) },
                    colors = RadioButtonDefaults.colors(selectedColor = AppTeal)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = option, fontSize = 16.sp)
            }
        }
    }
}