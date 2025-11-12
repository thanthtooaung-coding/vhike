package com.vinn.vhike.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "hike_registry")
data class Hike(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hikeName: String,
    val location: String,
    val hikeDate: Date,
    val parkingAvailable: Boolean,
    val hikeLength: Double,
    val difficultyLevel: String,
    val trailType: String,
    val description: String?,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val duration: String
)