package com.vinn.vhike.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vinn.vhike.data.db.Hike
import com.vinn.vhike.ui.theme.AppTeal
import com.vinn.vhike.ui.theme.LightGray
import com.vinn.vhike.ui.viewmodel.HikeViewModel
import com.vinn.vhike.ui.viewmodel.SearchFilters
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchHikeScreen(
    onNavigateBack: () -> Unit,
    onHikeClick: (Long) -> Unit,
    viewModel: HikeViewModel = hiltViewModel()
) {
    val filterState by viewModel.searchFilterState.collectAsState()
    val searchResults by viewModel.searchResultState.collectAsState()

    // --- Date Picker Setup ---
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year: Int, month: Int, dayOfMonth: Int ->
            calendar.set(year, month, dayOfMonth)
            viewModel.onSearchDateSelected(calendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.setOnCancelListener {
        viewModel.onSearchDateSelected(null)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filter Hikes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.resetSearch() }) {
                        Text("Reset", color = AppTeal)
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
                onClick = { viewModel.executeSearch() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppTeal),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Apply Filters",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.White)
        ) {
            // --- Filter Controls ---
            Column(modifier = Modifier.padding(16.dp)) {
                SearchTextField(
                    value = filterState.name ?: "",
                    onValueChange = { viewModel.onSearchNameChanged(it) },
                    placeholder = "Hike Name",
                    icon = Icons.Default.Search
                )
                Spacer(modifier = Modifier.height(12.dp))
                SearchTextField(
                    value = filterState.location ?: "",
                    onValueChange = { viewModel.onSearchLocationChanged(it) },
                    placeholder = "Location",
                    icon = Icons.Default.LocationOn
                )
                Spacer(modifier = Modifier.height(12.dp))
                SearchTextField(
                    value = filterState.selectedDate?.let { dateFormat.format(it) } ?: "",
                    onValueChange = {},
                    placeholder = "Select Date",
                    icon = Icons.Default.CalendarMonth,
                    modifier = Modifier.clickable { datePickerDialog.show() },
                    readOnly = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- Hike Length Slider ---
                Text(
                    text = "Hike Length",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                // This is a complex control. A simple placeholder for now.
                val sliderRange = (filterState.lengthRange?.start?.toFloat() ?: 0f)..(filterState.lengthRange?.endInclusive?.toFloat() ?: 10f)
                val textRange = (filterState.lengthRange?.start ?: 2.0)..(filterState.lengthRange?.endInclusive ?: 10.0)

                Text(
                    text = "${"%.1f".format(textRange.start)}km - ${"%.1f".format(textRange.endInclusive)}km", // Dynamic text
                    color = AppTeal,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.End)
                )
                RangeSlider(
                    value = sliderRange,
                    onValueChange = { range ->
                        // Update VM, converting Float range to Double range
                        viewModel.onSearchLengthRangeChanged(range.start.toDouble()..range.endInclusive.toDouble())
                    },
                    valueRange = 0f..50f, // Max range
                    steps = 49,
                    colors = SliderDefaults.colors(
                        thumbColor = AppTeal,
                        activeTrackColor = AppTeal,
                        inactiveTrackColor = LightGray
                    )
                )
            }

            // --- Search Results ---
            Text(
                text = "Showing ${searchResults.size} Results",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(searchResults) { hike ->
                    HikeResultItem(hike = hike, onClick = { onHikeClick(hike.id) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.Gray) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = Color.Gray) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            containerColor = LightGray,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = AppTeal
        ),
        singleLine = true,
        readOnly = readOnly
    )
}

@Composable
fun HikeResultItem(hike: Hike, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(LightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Landscape, contentDescription = null, tint = AppTeal)
            }
            Spacer(modifier = Modifier.width(16.dp))
            // Text
            Column {
                Text(
                    text = hike.hikeName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${hike.location} • ${hike.hikeLength} km",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
        Icon(
            Icons.Default.ArrowForwardIos,
            contentDescription = "View details",
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
    }
}