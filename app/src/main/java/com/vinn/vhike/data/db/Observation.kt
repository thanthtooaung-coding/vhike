package com.vinn.vhike.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "observation_log",
    foreignKeys = [
        ForeignKey(
            entity = Hike::class,
            parentColumns = ["id"],
            childColumns = ["hikeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["hikeId"])]
)
data class Observation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hikeId: Long,
    val observationText: String,
    val observationTime: Date,
    val additionalComments: String?,
    val photoUrl: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)