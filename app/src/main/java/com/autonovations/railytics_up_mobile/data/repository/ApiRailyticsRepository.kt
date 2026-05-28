package com.autonovations.railytics_up_mobile.data.repository

import com.autonovations.railytics_up_mobile.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class ApiRailyticsRepository(private val baseUrlProvider: () -> String) : RailyticsRepository {

    private fun getCleanUrl(path: String): String {
        var base = baseUrlProvider().trim()
        if (base.endsWith("/")) {
            base = base.substring(0, base.length - 1)
        }
        // If user didn't write http:// or https://, prepend http://
        if (!base.startsWith("http://") && !base.startsWith("https://")) {
            base = "http://$base"
        }
        return "$base$path"
    }

    override suspend fun getStreams(activeOnly: Boolean): List<Stream> = withContext(Dispatchers.IO) {
        try {
            val urlPath = if (activeOnly) "/streams?active_only=true" else "/streams"
            val url = URL(getCleanUrl(urlPath))
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                val obj = JSONObject(text)
                val arr = obj.optJSONArray("streams") ?: JSONArray()
                val list = mutableListOf<Stream>()
                for (i in 0 until arr.length()) {
                    val sObj = arr.optJSONObject(i)
                    if (sObj != null) {
                        list.add(Stream.fromJson(sObj))
                    }
                }
                list
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getEvents(streamId: String?): List<RailwayEvent> = withContext(Dispatchers.IO) {
        try {
            val query = if (!streamId.isNullOrEmpty() && streamId != "ALL") "?stream_id=$streamId" else ""
            val url = URL(getCleanUrl("/railway-events$query"))
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                val obj = JSONObject(text)
                val arr = obj.optJSONArray("events") ?: JSONArray()
                val list = mutableListOf<RailwayEvent>()
                for (i in 0 until arr.length()) {
                    val eObj = arr.optJSONObject(i)
                    if (eObj != null) {
                        list.add(RailwayEvent.fromJson(eObj))
                    }
                }
                list.sortByDescending { it.startTime }
                list
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getFrames(streamId: String?, limit: Int, skip: Int): FrameResponse = withContext(Dispatchers.IO) {
        try {
            var urlPath = "/frames?limit=$limit&skip=$skip"
            if (!streamId.isNullOrEmpty()) {
                urlPath += "&stream_id=$streamId"
            }
            val url = URL(getCleanUrl(urlPath))
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                val obj = JSONObject(text)
                val arr = obj.optJSONArray("frames") ?: JSONArray()
                val list = mutableListOf<Frame>()
                for (i in 0 until arr.length()) {
                    val fObj = arr.optJSONObject(i)
                    if (fObj != null) {
                        list.add(Frame.fromJson(fObj))
                    }
                }
                FrameResponse(
                    frames = list,
                    total = obj.optInt("total", 0),
                    framesProcessed = obj.optInt("frames_processed", 0),
                    framesDiscarded = obj.optInt("frames_discarded", 0)
                )
            } else {
                FrameResponse(emptyList(), 0, 0, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            FrameResponse(emptyList(), 0, 0, 0)
        }
    }

    override suspend fun startAnalysis(streamId: String): AnalysisSession? = withContext(Dispatchers.IO) {
        try {
            val url = URL(getCleanUrl("/analysis/start"))
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            val body = JSONObject().put("stream_id", streamId)
            OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                val obj = JSONObject(text)
                val sObj = obj.optJSONObject("session")
                if (sObj != null) {
                    val rate = sObj.optDouble("detection_rate", 0.0)
                    AnalysisSession(
                        streamId = sObj.optString("stream_id", streamId),
                        streamName = sObj.optString("stream_name", ""),
                        active = sObj.optBoolean("active", true),
                        startTime = sObj.optString("start_time", ""),
                        framesProcessed = sObj.optInt("frames_processed", 0),
                        trainsDetected = sObj.optInt("trains_detected", 0),
                        framesDiscarded = sObj.optInt("frames_discarded", 0),
                        detectionRate = rate,
                        runtimeSeconds = sObj.optInt("runtime_seconds", 0)
                    )
                } else null
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun stopAnalysis(streamId: String?): Boolean = withContext(Dispatchers.IO) {
        try {
            val path = if (streamId != null) "/analysis/stop/$streamId" else "/analysis/stop"
            val url = URL(getCleanUrl(path))
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            conn.responseCode == HttpURLConnection.HTTP_OK
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun getActiveSessions(): List<AnalysisSession> = withContext(Dispatchers.IO) {
        try {
            val url = URL(getCleanUrl("/analysis/sessions"))
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                val obj = JSONObject(text)
                val arr = obj.optJSONArray("active_sessions") ?: JSONArray()
                val list = mutableListOf<AnalysisSession>()
                for (i in 0 until arr.length()) {
                    val sObj = arr.optJSONObject(i)
                    if (sObj != null) {
                        val rate = sObj.optDouble("detection_rate", 0.0)
                        list.add(
                            AnalysisSession(
                                streamId = sObj.optString("stream_id", ""),
                                streamName = sObj.optString("stream_name", ""),
                                active = sObj.optBoolean("active", true),
                                startTime = sObj.optString("start_time", ""),
                                framesProcessed = sObj.optInt("frames_processed", 0),
                                trainsDetected = sObj.optInt("trains_detected", 0),
                                framesDiscarded = sObj.optInt("frames_discarded", 0),
                                detectionRate = rate,
                                runtimeSeconds = sObj.optInt("runtime_seconds", 0)
                            )
                        )
                    }
                }
                list
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun addStream(stream: Stream): Boolean {
        return true
    }

    override suspend fun updateStream(stream: Stream): Boolean {
        return true
    }
}
