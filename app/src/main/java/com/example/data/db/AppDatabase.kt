package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.Trip
import com.example.data.BoardingPass
import com.example.data.MilesTransaction
import com.example.data.WalletTransaction
import com.example.data.Notification
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY departureDate ASC")
    fun getAllTrips(): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getTripById(id: String): Trip?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: Trip)

    @Update
    suspend fun updateTrip(trip: Trip)

    @Query("DELETE FROM trips WHERE id = :id")
    suspend fun deleteTripById(id: String)
}

@Dao
interface BoardingPassDao {
    @Query("SELECT * FROM boarding_passes")
    fun getAllBoardingPasses(): Flow<List<BoardingPass>>

    @Query("SELECT * FROM boarding_passes WHERE tripId = :tripId")
    suspend fun getBoardingPassByTripId(tripId: String): BoardingPass?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoardingPass(boardingPass: BoardingPass)
}

@Dao
interface MilesDao {
    @Query("SELECT * FROM miles_transactions ORDER BY id DESC")
    fun getAllTransactions(): Flow<List<MilesTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: MilesTransaction)
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet_transactions ORDER BY id DESC")
    fun getAllTransactions(): Flow<List<WalletTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransaction)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<Notification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)

    @Query("UPDATE notifications SET read = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)
}

@Database(
    entities = [
        Trip::class,
        BoardingPass::class,
        MilesTransaction::class,
        WalletTransaction::class,
        Notification::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun boardingPassDao(): BoardingPassDao
    abstract fun milesDao(): MilesDao
    abstract fun walletDao(): WalletDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "turkish_airlines_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
