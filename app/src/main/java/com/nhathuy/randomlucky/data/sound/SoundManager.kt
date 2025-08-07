package com.nhathuy.randomlucky.data.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.nhathuy.randomlucky.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsManager: SettingManager
) {

    private var soundPool: SoundPool? = null
    private val soundIds = mutableMapOf<SoundEffect, Int>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isSoundEnabled = true
    private var isVibrationEnabled = true
    private var isInitialized = false

    // vibrator instance
    private val vibrator: Vibrator? by lazy {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        }else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    enum class SoundEffect {
        DRUM_ROLL,
        BALL_SPIN,
        BALL_DROP,
        WIN_FANFARE,
        TICK,
        DING
    }

    enum class VibrationType {
        SHORT,
        MEDIUM,
        LONG,
        SUCCESS,
        SPECIAL_WIN
    }

    init {
        initialize()
        observeSettings()
    }

    private fun initialize() {
        if (isInitialized) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool?.release() // Release previous instance if exists
        soundPool = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(audioAttributes)
            .build()

        loadSounds()
        isInitialized = true
    }

    private fun loadSounds() {
        soundPool?.let { pool ->
            soundIds.clear() // Clear previous sound IDs
            soundIds[SoundEffect.DRUM_ROLL] = pool.load(context, R.raw.drum_roll, 1)
            soundIds[SoundEffect.BALL_SPIN] = pool.load(context, R.raw.ball_spin, 1)
            soundIds[SoundEffect.BALL_DROP] = pool.load(context, R.raw.ball_drop, 1)
            soundIds[SoundEffect.WIN_FANFARE] = pool.load(context, R.raw.win_fanfare, 1)
            soundIds[SoundEffect.TICK] = pool.load(context, R.raw.tick, 1)
            soundIds[SoundEffect.DING] = pool.load(context, R.raw.ding, 1)
        }
    }

    private fun observeSettings() {
        settingsManager.isSoundEnabled
            .onEach { enabled ->
                isSoundEnabled = enabled
                if (!enabled) {
                    stop()
                }
            }.launchIn(scope)

        settingsManager.isVibrationEnabled
            .onEach {
                enabled ->
                isVibrationEnabled = enabled
                if(!enabled){
                    cancelVibration()
                }
            }.launchIn(scope)
    }

    fun play(effect: SoundEffect, loop: Boolean = false) {
        if (!isSoundEnabled) return

        // Reinitialize if needed
        if (!isInitialized || soundPool == null) {
            initialize()
        }

        soundIds[effect]?.let { soundId ->
            soundPool?.play(
                soundId, 1.0f, 1.0f, 1, if (loop) -1 else 0, 1.0f
            )
        }
    }

    // xử lý run
    fun vibrate(type: VibrationType){
        if(!isVibrationEnabled) return

        vibrator?.let{
            vib ->
            val vibrationEffect = when(type){
                VibrationType.SHORT -> VibrationEffect.createOneShot(100L, VibrationEffect.DEFAULT_AMPLITUDE)
                VibrationType.MEDIUM -> VibrationEffect.createOneShot(300L, VibrationEffect.DEFAULT_AMPLITUDE)
                VibrationType.LONG -> VibrationEffect.createOneShot(500L, VibrationEffect.DEFAULT_AMPLITUDE)
                VibrationType.SUCCESS -> {
                    VibrationEffect.createWaveform(
                        longArrayOf(0,150,100,150),
                        intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE),
                        -1
                    )
                }
                VibrationType.SPECIAL_WIN -> {
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 100, 50, 100, 50, 100, 100, 400),
                        intArrayOf(0, 255, 0, 255, 0, 255, 0, 255),
                        -1
                    )
                }
            }
            vib.vibrate(vibrationEffect)
        }
    }

    fun vibrateShort() = vibrate(VibrationType.SHORT)
    fun vibrateMedium() = vibrate(VibrationType.MEDIUM)
    fun vibrateLong() = vibrate(VibrationType.LONG)
    fun vibrateSuccess() = vibrate(VibrationType.SUCCESS)
    fun vibrateSpecialWin() = vibrate(VibrationType.SPECIAL_WIN)

    fun cancelVibration(){
        vibrator?.cancel()
    }


    fun stop() {
        soundPool?.autoPause()
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        soundIds.clear()
        isInitialized = false
    }

    fun reinitialize() {
        release()
        initialize()
        observeSettings()
    }
}