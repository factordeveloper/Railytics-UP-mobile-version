package com.autonovations.railytics_up_mobile.data.repository

import android.content.Context
import com.autonovations.railytics_up_mobile.data.models.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MockRailyticsRepository(private val context: Context) : RailyticsRepository {

    private val streams = mutableListOf<Stream>()
    private val events = mutableListOf<RailwayEvent>()
    private val frames = mutableListOf<Frame>()
    private val activeSessions = mutableListOf<AnalysisSession>()

    private var framesProcessedBase = 4520
    private var framesDiscardedBase = 3195

    init {
        loadData()
    }

    private fun loadData() {
        try {
            // Load streams
            val streamsStr = context.assets.open("streams.json").bufferedReader().use { it.readText() }
            val streamsArr = JSONArray(streamsStr)
            for (i in 0 until streamsArr.length()) {
                val obj = streamsArr.optJSONObject(i)
                if (obj != null) {
                    streams.add(Stream.fromJson(obj))
                }
            }

            // Load events
            val eventsStr = context.assets.open("events.json").bufferedReader().use { it.readText() }
            val eventsArr = JSONArray(eventsStr)
            for (i in 0 until eventsArr.length()) {
                val obj = eventsArr.optJSONObject(i)
                if (obj != null) {
                    events.add(RailwayEvent.fromJson(obj))
                }
            }
            events.sortByDescending { it.startTime }

            // Load frames
            val framesStr = context.assets.open("frames.json").bufferedReader().use { it.readText() }
            val framesObj = JSONObject(framesStr)
            framesProcessedBase = framesObj.optInt("frames_processed", 4520)
            framesDiscardedBase = framesObj.optInt("frames_discarded", 3195)
            val framesArr = framesObj.optJSONArray("frames")
            if (framesArr != null) {
                for (i in 0 until framesArr.length()) {
                    val obj = framesArr.optJSONObject(i)
                    if (obj != null) {
                        frames.add(Frame.fromJson(obj))
                    }
                }
            }

            // Auto-start sessions for streams that are active by default in the streams.json
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val nowStr = sdf.format(Date())

            streams.filter { it.active }.forEach { s ->
                val proc = (200..700).random()
                val det = (proc.toDouble() * (Math.random() * 0.15 + 0.05)).toInt()
                val disc = proc - det
                val rate = if (proc > 0) String.format(Locale.US, "%.1f", (det.toDouble() / proc * 100)).toDouble() else 0.0

                activeSessions.add(
                    AnalysisSession(
                        streamId = s.id,
                        streamName = s.name,
                        active = true,
                        startTime = nowStr,
                        framesProcessed = proc,
                        trainsDetected = det,
                        framesDiscarded = disc,
                        detectionRate = rate,
                        runtimeSeconds = (60..300).random()
                    )
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun getStreams(activeOnly: Boolean): List<Stream> {
        return if (activeOnly) streams.filter { it.active } else streams
    }

    override suspend fun getEvents(streamId: String?): List<RailwayEvent> {
        return if (streamId.isNullOrEmpty() || streamId == "ALL") {
            events
        } else {
            events.filter { it.streamName == streamId || it.streamId == streamId }
        }
    }

    override suspend fun getFrames(streamId: String?, limit: Int, skip: Int): FrameResponse {
        val filteredFrames = if (streamId.isNullOrEmpty()) {
            frames
        } else {
            frames.filter { it.streamId == streamId }
        }
        val total = filteredFrames.size
        val endIdx = minOf(skip + limit, total)
        val paginated = if (skip < total) filteredFrames.subList(skip, endIdx) else emptyList()

        // Calculate simulated live dynamic counts
        val sessionProcessed = activeSessions.sumOf { it.framesProcessed }
        val sessionDiscarded = activeSessions.sumOf { it.framesDiscarded }

        return FrameResponse(
            frames = paginated,
            total = total,
            framesProcessed = framesProcessedBase + sessionProcessed,
            framesDiscarded = framesDiscardedBase + sessionDiscarded
        )
    }

    override suspend fun startAnalysis(streamId: String): AnalysisSession? {
        val stream = streams.find { it.id == streamId } ?: return null
        val existing = activeSessions.find { it.streamId == streamId }
        if (existing != null) {
            existing.active = true
            return existing
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val nowStr = sdf.format(Date())

        val newSession = AnalysisSession(
            streamId = streamId,
            streamName = stream.name,
            active = true,
            startTime = nowStr,
            framesProcessed = 0,
            trainsDetected = 0,
            framesDiscarded = 0,
            detectionRate = 0.0,
            runtimeSeconds = 0
        )
        activeSessions.add(newSession)
        return newSession
    }

    override suspend fun stopAnalysis(streamId: String?): Boolean {
        if (streamId == null) {
            activeSessions.clear()
            return true
        }
        val session = activeSessions.find { it.streamId == streamId }
        if (session != null) {
            activeSessions.remove(session)
            return true
        }
        return false
    }

    override suspend fun getActiveSessions(): List<AnalysisSession> {
        return activeSessions
    }
}
