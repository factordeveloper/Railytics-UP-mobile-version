package com.autonovations.railytics_up_mobile.data.repository

import com.autonovations.railytics_up_mobile.data.models.*

interface RailyticsRepository {
    suspend fun getStreams(activeOnly: Boolean = false): List<Stream>
    suspend fun getEvents(streamId: String? = null): List<RailwayEvent>
    suspend fun getFrames(streamId: String? = null, limit: Int = 12, skip: Int = 0): FrameResponse
    suspend fun startAnalysis(streamId: String): AnalysisSession?
    suspend fun stopAnalysis(streamId: String? = null): Boolean
    suspend fun getActiveSessions(): List<AnalysisSession>
}

data class FrameResponse(
    val frames: List<Frame>,
    val total: Int,
    val framesProcessed: Int,
    val framesDiscarded: Int
)
