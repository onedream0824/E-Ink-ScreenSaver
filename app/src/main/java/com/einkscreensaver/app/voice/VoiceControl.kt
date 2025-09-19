package com.einkscreensaver.app.voice

import android.content.Context
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import com.einkscreensaver.app.data.model.ScreenSaverSettings
import kotlinx.coroutines.*
import java.util.*

class VoiceControl(private val context: Context) {
    
    private val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private val textToSpeech = TextToSpeech(context) { status ->
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.language = Locale.getDefault()
        }
    }
    
    private val voiceCommands = VoiceCommands()
    private val voiceSettings = VoiceSettings()
    private var isListening = false
    private var isSpeaking = false
    
    suspend fun initialize() {
        voiceCommands.initialize()
        voiceSettings.initialize()
    }
    
    fun startListening() {
        if (isListening) return
        
        val intent = RecognizerIntent.getRecognizerIntent().apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        }
        
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
            }
            
            override fun onBeginningOfSpeech() {
                // Speech started
            }
            
            override fun onRmsChanged(rmsdB: Float) {
                // Volume level changed
            }
            
            override fun onBufferReceived(buffer: ByteArray?) {
                // Audio buffer received
            }
            
            override fun onEndOfSpeech() {
                isListening = false
            }
            
            override fun onError(error: Int) {
                isListening = false
                handleRecognitionError(error)
            }
            
            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    processVoiceCommand(matches[0])
                }
            }
            
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    // Handle partial results
                }
            }
            
            override fun onEvent(eventType: Int, params: Bundle?) {
                // Handle events
            }
        })
        
        speechRecognizer.startListening(intent)
    }
    
    fun stopListening() {
        if (isListening) {
            speechRecognizer.stopListening()
            isListening = false
        }
    }
    
    fun speak(text: String) {
        if (isSpeaking) return
        
        isSpeaking = true
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        
        // Reset speaking flag after a delay
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            isSpeaking = false
        }
    }
    
    fun setLanguage(language: String) {
        val locale = Locale(language)
        textToSpeech.language = locale
    }
    
    fun setSpeechRate(rate: Float) {
        textToSpeech.setSpeechRate(rate)
    }
    
    fun setPitch(pitch: Float) {
        textToSpeech.setPitch(pitch)
    }
    
    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }
    
    fun isListening(): Boolean = isListening
    fun isSpeaking(): Boolean = isSpeaking
    
    private fun processVoiceCommand(command: String) {
        val processedCommand = command.lowercase().trim()
        
        when {
            processedCommand.contains("time") -> {
                val currentTime = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(java.util.Date())
                speak("The current time is $currentTime")
            }
            
            processedCommand.contains("date") -> {
                val currentDate = java.text.SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
                    .format(java.util.Date())
                speak("Today is $currentDate")
            }
            
            processedCommand.contains("battery") -> {
                speak("Battery level is 75 percent")
            }
            
            processedCommand.contains("weather") -> {
                speak("The weather is sunny with a temperature of 22 degrees")
            }
            
            processedCommand.contains("brightness") -> {
                when {
                    processedCommand.contains("increase") || processedCommand.contains("up") -> {
                        speak("Increasing brightness")
                        // Increase brightness
                    }
                    processedCommand.contains("decrease") || processedCommand.contains("down") -> {
                        speak("Decreasing brightness")
                        // Decrease brightness
                    }
                    else -> {
                        speak("Current brightness is 50 percent")
                    }
                }
            }
            
            processedCommand.contains("theme") -> {
                when {
                    processedCommand.contains("dark") -> {
                        speak("Switching to dark theme")
                        // Switch to dark theme
                    }
                    processedCommand.contains("light") -> {
                        speak("Switching to light theme")
                        // Switch to light theme
                    }
                    else -> {
                        speak("Current theme is classic")
                    }
                }
            }
            
            processedCommand.contains("help") -> {
                speak("Available commands: time, date, battery, weather, brightness, theme, help")
            }
            
            else -> {
                speak("I didn't understand that command. Say help for available commands.")
            }
        }
    }
    
    private fun handleRecognitionError(error: Int) {
        val errorMessage = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech input recognized"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error"
        }
        
        speak("Error: $errorMessage")
    }
    
    fun destroy() {
        speechRecognizer.destroy()
        textToSpeech.shutdown()
    }
}

class VoiceCommands {
    private val commands = mutableMapOf<String, VoiceCommand>()
    
    suspend fun initialize() {
        loadDefaultCommands()
    }
    
    fun addCommand(command: VoiceCommand) {
        commands[command.trigger] = command
    }
    
    fun removeCommand(trigger: String) {
        commands.remove(trigger)
    }
    
    fun getCommand(trigger: String): VoiceCommand? {
        return commands[trigger]
    }
    
    fun getAllCommands(): List<VoiceCommand> {
        return commands.values.toList()
    }
    
    fun findMatchingCommand(input: String): VoiceCommand? {
        val normalizedInput = input.lowercase().trim()
        
        return commands.values.find { command ->
            command.triggers.any { trigger ->
                normalizedInput.contains(trigger.lowercase())
            }
        }
    }
    
    private fun loadDefaultCommands() {
        addCommand(VoiceCommand(
            trigger = "time",
            triggers = listOf("time", "what time", "current time"),
            action = { "The current time is ${getCurrentTime()}" },
            description = "Get current time"
        ))
        
        addCommand(VoiceCommand(
            trigger = "date",
            triggers = listOf("date", "what date", "current date", "today"),
            action = { "Today is ${getCurrentDate()}" },
            description = "Get current date"
        ))
        
        addCommand(VoiceCommand(
            trigger = "battery",
            triggers = listOf("battery", "battery level", "power"),
            action = { "Battery level is ${getBatteryLevel()} percent" },
            description = "Get battery level"
        ))
        
        addCommand(VoiceCommand(
            trigger = "weather",
            triggers = listOf("weather", "temperature", "forecast"),
            action = { "The weather is ${getWeatherInfo()}" },
            description = "Get weather information"
        ))
        
        addCommand(VoiceCommand(
            trigger = "brightness up",
            triggers = listOf("brightness up", "increase brightness", "brighter"),
            action = { 
                increaseBrightness()
                "Brightness increased"
            },
            description = "Increase brightness"
        ))
        
        addCommand(VoiceCommand(
            trigger = "brightness down",
            triggers = listOf("brightness down", "decrease brightness", "dimmer"),
            action = { 
                decreaseBrightness()
                "Brightness decreased"
            },
            description = "Decrease brightness"
        ))
        
        addCommand(VoiceCommand(
            trigger = "theme dark",
            triggers = listOf("dark theme", "switch to dark", "dark mode"),
            action = { 
                switchToDarkTheme()
                "Switched to dark theme"
            },
            description = "Switch to dark theme"
        ))
        
        addCommand(VoiceCommand(
            trigger = "theme light",
            triggers = listOf("light theme", "switch to light", "light mode"),
            action = { 
                switchToLightTheme()
                "Switched to light theme"
            },
            description = "Switch to light theme"
        ))
        
        addCommand(VoiceCommand(
            trigger = "help",
            triggers = listOf("help", "commands", "what can you do"),
            action = { 
                "Available commands: time, date, battery, weather, brightness, theme, help"
            },
            description = "Show available commands"
        ))
    }
    
    private fun getCurrentTime(): String {
        return java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
            .format(java.util.Date())
    }
    
    private fun getCurrentDate(): String {
        return java.text.SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
            .format(java.util.Date())
    }
    
    private fun getBatteryLevel(): Int {
        // Get actual battery level
        return 75
    }
    
    private fun getWeatherInfo(): String {
        // Get actual weather info
        return "sunny with a temperature of 22 degrees"
    }
    
    private fun increaseBrightness() {
        // Increase brightness
    }
    
    private fun decreaseBrightness() {
        // Decrease brightness
    }
    
    private fun switchToDarkTheme() {
        // Switch to dark theme
    }
    
    private fun switchToLightTheme() {
        // Switch to light theme
    }
}

class VoiceSettings {
    private var settings = VoiceControlSettings()
    
    suspend fun initialize() {
        loadSettings()
    }
    
    fun getSettings(): VoiceControlSettings {
        return settings
    }
    
    fun updateSettings(newSettings: VoiceControlSettings) {
        settings = newSettings
        saveSettings()
    }
    
    fun isEnabled(): Boolean {
        return settings.enabled
    }
    
    fun setEnabled(enabled: Boolean) {
        settings = settings.copy(enabled = enabled)
        saveSettings()
    }
    
    fun getLanguage(): String {
        return settings.language
    }
    
    fun setLanguage(language: String) {
        settings = settings.copy(language = language)
        saveSettings()
    }
    
    fun getSpeechRate(): Float {
        return settings.speechRate
    }
    
    fun setSpeechRate(rate: Float) {
        settings = settings.copy(speechRate = rate)
        saveSettings()
    }
    
    fun getPitch(): Float {
        return settings.pitch
    }
    
    fun setPitch(pitch: Float) {
        settings = settings.copy(pitch = pitch)
        saveSettings()
    }
    
    fun isContinuousListening(): Boolean {
        return settings.continuousListening
    }
    
    fun setContinuousListening(enabled: Boolean) {
        settings = settings.copy(continuousListening = enabled)
        saveSettings()
    }
    
    fun getWakeWord(): String {
        return settings.wakeWord
    }
    
    fun setWakeWord(wakeWord: String) {
        settings = settings.copy(wakeWord = wakeWord)
        saveSettings()
    }
    
    private suspend fun loadSettings() {
        // Load from preferences
    }
    
    private fun saveSettings() {
        // Save to preferences
    }
}

data class VoiceCommand(
    val trigger: String,
    val triggers: List<String>,
    val action: () -> String,
    val description: String
)

data class VoiceControlSettings(
    val enabled: Boolean = true,
    val language: String = "en-US",
    val speechRate: Float = 1.0f,
    val pitch: Float = 1.0f,
    val continuousListening: Boolean = false,
    val wakeWord: String = "hey screensaver",
    val autoResponse: Boolean = true,
    val soundEffects: Boolean = true
)