package com.nhathuy.randomlucky.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.nhathuy.randomlucky.data.local.dao.LotteryDao
import com.nhathuy.randomlucky.data.local.database.LotteryDatabase
import com.nhathuy.randomlucky.data.mapper.LotteryMapper
import com.nhathuy.randomlucky.data.repository.LotteryRepositoryImpl
import com.nhathuy.randomlucky.data.sound.SettingManager
import com.nhathuy.randomlucky.data.sound.SoundManager
import com.nhathuy.randomlucky.domain.model.LotteryPrize
import com.nhathuy.randomlucky.domain.repository.LotteryRepository
import com.nhathuy.randomlucky.domain.usecase.GenerateLotteryNumbersUseCase
import com.nhathuy.randomlucky.domain.usecase.RunLotterySessionUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LotteryDatabase {
        return Room.databaseBuilder(
            context, LotteryDatabase::class.java, "lottery_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideLotterySessionDao(database: LotteryDatabase) : LotteryDao {
        return database.lotteryDao()
    }

    @Provides
    @Singleton
    fun provideGson() : Gson = Gson()

    @Provides
    @Singleton
    fun provideLotteryMapper(gson: Gson) : LotteryMapper{
        return LotteryMapper(gson)
    }

    @Provides
    @Singleton
    fun provideLotteryPrizes() : List<LotteryPrize>{
        return listOf(
            LotteryPrize("eighth", "Giải Tám", "Giải Tám", 1, 2, 2000L),
            LotteryPrize("seventh", "Giải Bảy", "Giải Bảy", 1, 3, 3000L),
            LotteryPrize("sixth", "Giải Sáu", "Giải Sáu", 3, 4, 3000L),
            LotteryPrize("fifth", "Giải Năm", "Giải Năm", 1, 4, 4000L),
            LotteryPrize("fourth", "Giải Tư", "Giải Tư", 7, 5, 4000L),
            LotteryPrize("third", "Giải Ba", "Giải Ba", 2, 5, 5000L),
            LotteryPrize("second", "Giải Hai", "Giải Hai", 1, 5, 3000L),
            LotteryPrize("first", "Giải Nhất", "Giải Nhất", 1, 5, 3000L),
            LotteryPrize("special", "Giải Đặc Biệt", "GIẢI ĐẶC BIỆT", 1, 6, 2000L)
        )
    }

    @Provides
    @Singleton
    fun provideLotteryRepository(
        dao: LotteryDao,
        mapper: LotteryMapper,
        prizes: List<LotteryPrize>
    ): LotteryRepository {
        return LotteryRepositoryImpl(dao, mapper, prizes)
    }

    @Provides
    @Singleton
    fun provideSoundManager(
        @ApplicationContext context: Context,
        settingManager: SettingManager
    ): SoundManager {
        return SoundManager(context,settingManager)
    }

    @Provides
    @Singleton
    fun provideRunLotterySessionUseCase(
        repository: LotteryRepository,
        generateNumbers: GenerateLotteryNumbersUseCase
    ): RunLotterySessionUseCase {
        return RunLotterySessionUseCase(repository,generateNumbers)
    }

    @Singleton
    @Provides
    fun provideGenerateLotteryNumbersUseCase() : GenerateLotteryNumbersUseCase{
        return GenerateLotteryNumbersUseCase()
    }
}