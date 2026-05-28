package com.autonovations.railytics_up_mobile.data.models

import org.json.JSONArray
import org.json.JSONObject

data class YoutubeMetadata(
    val title: String,
    val uploader: String,
    val viewCount: Long,
    val isLive: Boolean
) {
    companion object {
        fun fromJson(json: JSONObject): YoutubeMetadata {
            return YoutubeMetadata(
                title = json.optString("title", ""),
                uploader = json.optString("uploader", ""),
                viewCount = json.optLong("view_count", 0L),
                isLive = json.optBoolean("is_live", false)
            )
        }
    }
}

data class Stream(
    val id: String,
    val name: String,
    val url: String,
    val description: String,
    val active: Boolean,
    val thumbnail: String?,
    val youtubeMetadata: YoutubeMetadata?,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        fun fromJson(json: JSONObject): Stream {
            val thumb = if (json.isNull("thumbnail")) null else json.optString("thumbnail")
            val ytMetaJson = json.optJSONObject("youtube_metadata")
            val ytMeta = if (ytMetaJson != null) YoutubeMetadata.fromJson(ytMetaJson) else null

            return Stream(
                id = json.optString("id", ""),
                name = json.optString("name", ""),
                url = json.optString("url", ""),
                description = json.optString("description", ""),
                active = json.optBoolean("active", false),
                thumbnail = thumb,
                youtubeMetadata = ytMeta,
                createdAt = json.optString("created_at", ""),
                updatedAt = json.optString("updated_at", "")
            )
        }
    }
}

data class Serial(
    val text: String,
    val cleanedText: String,
    val confidence: Double,
    val bboxRelative: List<Double>
) {
    companion object {
        fun fromJson(json: JSONObject): Serial {
            val bboxArr = json.optJSONArray("bbox_relative")
            val bbox = mutableListOf<Double>()
            if (bboxArr != null) {
                for (i in 0 until bboxArr.length()) {
                    bbox.add(bboxArr.optDouble(i, 0.0))
                }
            }
            return Serial(
                text = json.optString("text", ""),
                cleanedText = json.optString("cleaned_text", ""),
                confidence = json.optDouble("confidence", 0.0),
                bboxRelative = bbox
            )
        }
    }
}

data class Detection(
    val clazz: Int,
    val confidence: Double,
    val bbox: List<Int>,
    val className: String,
    val serials: List<Serial>,
    val serialCount: Int
) {
    companion object {
        fun fromJson(json: JSONObject): Detection {
            val bboxArr = json.optJSONArray("bbox")
            val bbox = mutableListOf<Int>()
            if (bboxArr != null) {
                for (i in 0 until bboxArr.length()) {
                    bbox.add(bboxArr.optInt(i, 0))
                }
            }

            val serialsArr = json.optJSONArray("serials")
            val serials = mutableListOf<Serial>()
            if (serialsArr != null) {
                for (i in 0 until serialsArr.length()) {
                    val sObj = serialsArr.optJSONObject(i)
                    if (sObj != null) {
                        serials.add(Serial.fromJson(sObj))
                    }
                }
            }

            return Detection(
                clazz = json.optInt("class", 0),
                confidence = json.optDouble("confidence", 0.0),
                bbox = bbox,
                className = json.optString("class_name", ""),
                serials = serials,
                serialCount = json.optInt("serial_count", 0)
            )
        }
    }
}

data class Frame(
    val filename: String,
    val filepath: String,
    val timestamp: String,
    val size: Int,
    val hasTrains: Boolean,
    val detectionCount: Int,
    val totalSerials: Int,
    val streamId: String,
    val detections: List<Detection>
) {
    companion object {
        fun fromJson(json: JSONObject): Frame {
            val detsArr = json.optJSONArray("detections")
            val dets = mutableListOf<Detection>()
            if (detsArr != null) {
                for (i in 0 until detsArr.length()) {
                    val dObj = detsArr.optJSONObject(i)
                    if (dObj != null) {
                        dets.add(Detection.fromJson(dObj))
                    }
                }
            }

            return Frame(
                filename = json.optString("filename", ""),
                filepath = json.optString("filepath", ""),
                timestamp = json.optString("timestamp", ""),
                size = json.optInt("size", 0),
                hasTrains = json.optBoolean("has_trains", false),
                detectionCount = json.optInt("detection_count", 0),
                totalSerials = json.optInt("total_serials", 0),
                streamId = json.optString("stream_id", ""),
                detections = dets
            )
        }
    }
}

data class RailwayEvent(
    val id: String,
    val streamId: String,
    val streamName: String,
    val startTime: String,
    val endTime: String?,
    val durationSeconds: Int?,
    val framesCount: Int,
    val unitsCount: Int,
    val locomotivesCount: Int,
    val wagonsCount: Int,
    val reportingMarks: List<String>,
    val reportingMarksCount: Int,
    val firstFrame: String,
    val lastFrame: String,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        fun fromJson(json: JSONObject): RailwayEvent {
            val marksArr = json.optJSONArray("reporting_marks")
            val marks = mutableListOf<String>()
            if (marksArr != null) {
                for (i in 0 until marksArr.length()) {
                    marks.add(marksArr.optString(i, ""))
                }
            }

            val end = if (json.isNull("end_time")) null else json.optString("end_time")
            val dur = if (json.isNull("duration_seconds")) null else json.optInt("duration_seconds")

            return RailwayEvent(
                id = json.optString("id", ""),
                streamId = json.optString("stream_id", ""),
                streamName = json.optString("stream_name", ""),
                startTime = json.optString("start_time", ""),
                endTime = end,
                durationSeconds = dur,
                framesCount = json.optInt("frames_count", 0),
                unitsCount = json.optInt("units_count", 0),
                locomotivesCount = json.optInt("locomotives_count", 0),
                wagonsCount = json.optInt("wagons_count", 0),
                reportingMarks = marks,
                reportingMarksCount = json.optInt("reporting_marks_count", 0),
                firstFrame = json.optString("first_frame", ""),
                lastFrame = json.optString("last_frame", ""),
                createdAt = json.optString("created_at", ""),
                updatedAt = json.optString("updated_at", "")
            )
        }
    }
}

data class AnalysisSession(
    val streamId: String,
    val streamName: String,
    var active: Boolean,
    val startTime: String,
    var durationMinutes: Int = 0,
    var framesProcessed: Int = 0,
    var trainsDetected: Int = 0,
    var framesDiscarded: Int = 0,
    var detectionRate: Double = 0.0,
    var runtimeSeconds: Int = 0
)

data class PerformanceMetric(
    val time: String,
    val processed: Int,
    val detected: Int,
    val discarded: Int
)

data class LiveAlert(
    val id: Long,
    val type: String, // warning, success, info
    val title: String,
    val desc: String,
    val time: Long
)
