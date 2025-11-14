package com.vinn.vhike.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

@Dao
interface HikeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHike(hike: Hike): Long

    @Update
    suspend fun updateHike(hike: Hike)

    @Delete
    suspend fun deleteHike(hike: Hike)

    @Query("DELETE FROM hike_registry")
    suspend fun deleteAllHikes()

    @Query("SELECT * FROM hike_registry ORDER BY hikeDate DESC")
    fun getAllHikes(): Flow<List<Hike>>

    @Query("SELECT * FROM hike_registry WHERE id = :hikeId")
    fun getHikeById(hikeId: Long): Flow<Hike?>

    @Query(
        "SELECT * FROM hike_registry WHERE " +
                "(:name IS NULL OR hikeName LIKE '%' || :name || '%') AND " +
                "(:location IS NULL OR location LIKE '%' || :location || '%') AND " +
                "(:date IS NULL OR hikeDate = :date) AND " +
                "(:lengthMin IS NULL OR hikeLength >= :lengthMin) AND " +
                "(:lengthMax IS NULL OR hikeLength <= :lengthMax)"
    )
    fun searchHikes(
        name: String?,
        location: String?,
        date: Date?,
        lengthMin: Double?,
        lengthMax: Double?
    ): Flow<List<Hike>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservation(observation: Observation)

    @Update
    suspend fun updateObservation(observation: Observation)

    @Delete
    suspend fun deleteObservation(observation: Observation)

    @Query("SELECT * FROM observation_log WHERE hikeId = :hikeId ORDER BY observationTime DESC")
    fun getObservationsForHike(hikeId: Long): Flow<List<Observation>>

    @Query("SELECT * FROM observation_log WHERE id = :observationId")
    fun getObservationById(observationId: Long): Flow<Observation?>
}

@Database(entities = [Hike::class, Observation::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class HikeDatabase : RoomDatabase() {

    abstract fun hikeDao(): HikeDao

    companion object {
        @Volatile
        private var INSTANCE: HikeDatabase? = null

        fun getDatabase(context: Context): HikeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HikeDatabase::class.java,
                    "m_hike_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}