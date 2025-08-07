package com.nhathuy.randomlucky.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nhathuy.randomlucky.data.local.dao.LotteryDao
import com.nhathuy.randomlucky.data.local.entity.LotterySessionEntity

@Database(
    entities = [LotterySessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LotteryDatabase : RoomDatabase(){
    abstract fun lotteryDao(): LotteryDao

    companion object{
        @Volatile
        private var INSTANCE : LotteryDatabase?= null

        fun getDatabase(context: Context) : LotteryDatabase{
            return INSTANCE?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LotteryDatabase::class.java,
                    "lottery_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}