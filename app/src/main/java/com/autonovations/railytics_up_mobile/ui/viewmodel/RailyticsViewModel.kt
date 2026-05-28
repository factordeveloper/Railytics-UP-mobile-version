package com.autonovations.railytics_up_mobile.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.autonovations.railytics_up_mobile.data.models.*
import com.autonovations.railytics_up_mobile.data.repository.ApiRailyticsRepository
import com.autonovations.railytics_up_mobile.data.repository.MockRailyticsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class RailyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val mockRepo = MockRailyticsRepository(application)
    private val apiRepo = ApiRailyticsRepository { apiUrl.value }

    // Settings
    private val _isOfflineMode = MutableStateFlow(true)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _apiUrl = MutableStateFlow("http://10.0.2.2:8080") // Default local dev or emulator host loopback
    val apiUrl: StateFlow<String> = _apiUrl.asStateFlow()

    // Active Repository Getter
    val activeRepo get() = if (_isOfflineMode.value) mockRepo else apiRepo

    // States
    private val _streams = MutableStateFlow<List<Stream>>(emptyList())
    val streams: StateFlow<List<Stream>> = _streams.asStateFlow()

    private val _events = MutableStateFlow<List<RailwayEvent>>(emptyList())
    val events: StateFlow<List<RailwayEvent>> = _events.asStateFlow()

    private val _frames = MutableStateFlow<List<Frame>>(emptyList())
    val frames: StateFlow<List<Frame>> = _frames.asStateFlow()

    private val _framesTotal = MutableStateFlow(0)
    val framesTotal: StateFlow<Int> = _framesTotal.asStateFlow()

    private val _framesProcessed = MutableStateFlow(4520)
    val framesProcessed: StateFlow<Int> = _framesProcessed.asStateFlow()

    private val _framesDiscarded = MutableStateFlow(3195)
    val framesDiscarded: StateFlow<Int> = _framesDiscarded.asStateFlow()

    private val _activeSessions = MutableStateFlow<List<AnalysisSession>>(emptyList())
    val activeSessions: StateFlow<List<AnalysisSession>> = _activeSessions.asStateFlow()

    private val _performanceMetrics = MutableStateFlow<List<PerformanceMetric>>(emptyList())
    val performanceMetrics: StateFlow<List<PerformanceMetric>> = _performanceMetrics.asStateFlow()

    private val _liveAlerts = MutableStateFlow<List<LiveAlert>>(emptyList())
    val liveAlerts: StateFlow<List<LiveAlert>> = _liveAlerts.asStateFlow()

    private val _hourlyActivity = MutableStateFlow<List<Int>>(List(24) { 0 })
    val hourlyActivity: StateFlow<List<Int>> = _hourlyActivity.asStateFlow()

    // Navigation and filters
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedCalDate = MutableStateFlow(Date())
    val selectedCalDate: StateFlow<Date> = _selectedCalDate.asStateFlow()

    private val _calLocationFilter = MutableStateFlow("ALL")
    val calLocationFilter: StateFlow<String> = _calLocationFilter.asStateFlow()

    private var simulationJob: Job? = null

    init {
        // Populate hourly activity with some mock starting stats
        val mockHourly = MutableList(24) { (2..12).random() }
        _hourlyActivity.value = mockHourly

        loadInitialData()
        startSimulation()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _streams.value = activeRepo.getStreams()
            _events.value = activeRepo.getEvents()
            val framesResp = activeRepo.getFrames(limit = 24, skip = 0)
            _frames.value = framesResp.frames
            _framesTotal.value = framesResp.total
            _framesProcessed.value = framesResp.framesProcessed
            _framesDiscarded.value = framesResp.framesDiscarded
            _activeSessions.value = activeRepo.getActiveSessions()

            // Initialize performance metrics
            val timeSdf = SimpleDateFormat("HH:mm:ss", Locale.US)
            val initialMetrics = mutableListOf<PerformanceMetric>()
            val nowTime = System.currentTimeMillis()
            var proc = _framesProcessed.value
            var disc = _framesDiscarded.value
            val det = proc - disc

            for (i in 15 downTo 0) {
                val t = timeSdf.format(Date(nowTime - i * 3000))
                initialMetrics.add(
                    PerformanceMetric(
                        time = t,
                        processed = proc - (i * 2),
                        detected = det - (i / 2),
                        discarded = disc - (i * 2 - i / 2)
                    )
                )
            }
            _performanceMetrics.value = initialMetrics
        }
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun toggleOfflineMode(offline: Boolean) {
        _isOfflineMode.value = offline
        // Reset simulation
        startSimulation()
        loadInitialData()
    }

    fun updateApiUrl(url: String) {
        _apiUrl.value = url
        if (!_isOfflineMode.value) {
            loadInitialData()
        }
    }

    fun setSelectedCalDate(date: Date) {
        _selectedCalDate.value = date
    }

    fun setCalLocationFilter(filter: String) {
        _calLocationFilter.value = filter
    }

    // SIMULATION LOOP
    private fun startSimulation() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            val timeSdf = SimpleDateFormat("HH:mm:ss", Locale.US)
            val dateSdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            dateSdf.timeZone = TimeZone.getTimeZone("UTC")

            while (true) {
                delay(1000)
                if (_isOfflineMode.value) {
                    val currentSessions = _activeSessions.value.toMutableList()
                    var anyChanges = false

                    if (currentSessions.isNotEmpty()) {
                        anyChanges = true
                        currentSessions.forEach { session ->
                            session.runtimeSeconds += 1
                            val newFrames = (1..3).random()
                            session.framesProcessed += newFrames

                            // 20% chance of train detection on tick
                            val trainDetected = Math.random() < 0.15
                            if (trainDetected) {
                                session.trainsDetected += 1

                                // Add live alert
                                val alertId = System.currentTimeMillis() + (0..999).random()
                                val alert = LiveAlert(
                                    id = alertId,
                                    type = "success",
                                    title = "🚂 Train Detected",
                                    desc = "BNSF/UP Train identified at ${session.streamName}",
                                    time = System.currentTimeMillis()
                                )
                                _liveAlerts.value = (listOf(alert) + _liveAlerts.value).take(6)

                                // Update heatmap
                                val calendar = Calendar.getInstance()
                                val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                                val updatedHourly = _hourlyActivity.value.toMutableList()
                                updatedHourly[currentHour] += 1
                                _hourlyActivity.value = updatedHourly

                                // Add a real-time event
                                if (Math.random() < 0.4) {
                                    val eventId = "evt-${UUID.randomUUID().toString().take(6)}"
                                    val nowStr = dateSdf.format(Date())
                                    val endStr = dateSdf.format(Date(System.currentTimeMillis() + (120..300).random() * 1000))
                                    val newEvent = RailwayEvent(
                                        id = eventId,
                                        streamId = session.streamId,
                                        streamName = session.streamName,
                                        startTime = nowStr,
                                        endTime = endStr,
                                        durationSeconds = (120..300).random(),
                                        framesCount = (30..100).random(),
                                        unitsCount = (10..50).random(),
                                        locomotivesCount = (2..5).random(),
                                        wagonsCount = (8..45).random(),
                                        reportingMarks = listOf("UP " + (1000..9999).random(), "BNSF " + (1000..9999).random()),
                                        reportingMarksCount = 2,
                                        firstFrame = "frame_${session.streamId.takeLast(4)}_001.jpg",
                                        lastFrame = "frame_${session.streamId.takeLast(4)}_100.jpg",
                                        createdAt = nowStr,
                                        updatedAt = nowStr
                                    )
                                    _events.value = (listOf(newEvent) + _events.value).take(60)
                                }
                            } else {
                                session.framesDiscarded += newFrames
                            }

                            session.detectionRate = if (session.framesProcessed > 0) {
                                String.format(Locale.US, "%.1f", (session.trainsDetected.toDouble() / session.framesProcessed * 100)).toDouble()
                            } else 0.0
                        }
                        _activeSessions.value = currentSessions
                    }

                    // Update dynamic counts
                    val totalProcessed = _framesProcessed.value + currentSessions.sumOf { if (it.runtimeSeconds > 0) (1..3).random() else 0 }
                    val totalDiscarded = _framesDiscarded.value + currentSessions.sumOf { if (it.runtimeSeconds > 0) (1..2).random() else 0 }
                    _framesProcessed.value = totalProcessed
                    _framesDiscarded.value = totalDiscarded

                    // Performance Metric line chart update
                    val t = timeSdf.format(Date())
                    val activeProc = currentSessions.sumOf { it.framesProcessed }
                    val activeDet = currentSessions.sumOf { it.trainsDetected }
                    val activeDisc = currentSessions.sumOf { it.framesDiscarded }

                    val metric = PerformanceMetric(
                        time = t,
                        processed = totalProcessed + activeProc,
                        detected = (totalProcessed - totalDiscarded) + activeDet,
                        discarded = totalDiscarded + activeDisc
                    )
                    _performanceMetrics.value = (_performanceMetrics.value + metric).takeLast(20)

                    // Add occasional random warning info alerts
                    if (Math.random() < 0.05) {
                        val alert = LiveAlert(
                            id = System.currentTimeMillis(),
                            type = "warning",
                            title = "📡 Latency Warning",
                            desc = "High latency detected on video stream",
                            time = System.currentTimeMillis()
                        )
                        _liveAlerts.value = (listOf(alert) + _liveAlerts.value).take(6)
                    }

                } else {
                    // Polling API mode
                    try {
                        val sessions = apiRepo.getActiveSessions()
                        _activeSessions.value = sessions

                        val freshEvents = apiRepo.getEvents()
                        if (freshEvents.isNotEmpty()) {
                            _events.value = freshEvents
                        }

                        val framesResp = apiRepo.getFrames(limit = 24, skip = 0)
                        _frames.value = framesResp.frames
                        _framesTotal.value = framesResp.total
                        _framesProcessed.value = framesResp.framesProcessed
                        _framesDiscarded.value = framesResp.framesDiscarded

                        // Performance Metric line chart update in API Mode
                        val t = timeSdf.format(Date())
                        val metric = PerformanceMetric(
                            time = t,
                            processed = framesResp.framesProcessed,
                            detected = framesResp.framesProcessed - framesResp.framesDiscarded,
                            discarded = framesResp.framesDiscarded
                        )
                        _performanceMetrics.value = (_performanceMetrics.value + metric).takeLast(20)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    // ACTIONS
    fun startStream(streamId: String) {
        viewModelScope.launch {
            val session = activeRepo.startAnalysis(streamId)
            if (session != null) {
                // If offline mode, the repo already holds it, we refresh our state
                _activeSessions.value = activeRepo.getActiveSessions()
            }
        }
    }

    fun stopStream(streamId: String) {
        viewModelScope.launch {
            activeRepo.stopAnalysis(streamId)
            _activeSessions.value = activeRepo.getActiveSessions()
        }
    }

    fun startAllAnalysis() {
        viewModelScope.launch {
            _streams.value.forEach { s ->
                activeRepo.startAnalysis(s.id)
            }
            _activeSessions.value = activeRepo.getActiveSessions()
        }
    }

    fun stopAllAnalysis() {
        viewModelScope.launch {
            activeRepo.stopAnalysis(null)
            _activeSessions.value = emptyList()
        }
    }

    fun loadFramesPage(streamId: String?, limit: Int, skip: Int) {
        viewModelScope.launch {
            val res = activeRepo.getFrames(streamId, limit, skip)
            _frames.value = res.frames
            _framesTotal.value = res.total
        }
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
    }
}
