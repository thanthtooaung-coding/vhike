package com.vinn.vhike.data.repository

import com.vinn.vhike.data.db.Hike
import com.vinn.vhike.data.db.HikeDao
import com.vinn.vhike.data.db.Observation
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HikeRepository @Inject constructor(private val hikeDao: HikeDao) {

    val allHikes: Flow<List<Hike>> = hikeDao.getAllHikes()

    fun getHikeDetails(hikeId: Long): Flow<Hike?> = hikeDao.getHikeById(hikeId)

    suspend fun addNewHike(hike: Hike): Long {
        return hikeDao.insertHike(hike)
    }

    suspend fun updateHikeDetails(hike: Hike) {
        hikeDao.updateHike(hike)
    }

    suspend fun removeHike(hike: Hike) {
        hikeDao.deleteHike(hike)
    }

    suspend fun clearAllHikes() {
        hikeDao.deleteAllHikes()
    }

    fun performSearch(
        name: String?,
        location: String?,
        date: Date?,
        lengthMin: Double?,
        lengthMax: Double?
    ): Flow<List<Hike>> {
        return hikeDao.searchHikes(
            name = if (name.isNullOrBlank()) null else name,
            location = if (location.isNullOrBlank()) null else location,
            date = date,
            lengthMin = lengthMin,
            lengthMax = lengthMax
        )
    }

    // --- Observation Operations ---
    fun getObservations(hikeId: Long): Flow<List<Observation>> {
        return hikeDao.getObservationsForHike(hikeId)
    }

    fun getObservationDetails(observationId: Long): Flow<Observation?> {
        return hikeDao.getObservationById(observationId)
    }

    suspend fun addObservation(observation: Observation) {
        hikeDao.insertObservation(observation)
    }

    suspend fun updateObservation(observation: Observation) {
        hikeDao.updateObservation(observation)
    }

    suspend fun removeObservation(observation: Observation) {
        hikeDao.deleteObservation(observation)
    }
}