package com.example.utils

import android.content.Context
import android.util.Log
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.Constants

object AgoraManager {
    private var rtcEngine: RtcEngine? = null

    // Agora App ID & Secure Admin Temp Tokens
    val AGORA_APP_ID = "58f04a205e6740b28d9692a08775ad66"
    private val LOCKED_TEMP_TOKENS = listOf(
        "007eJxTYPgt5LxXf7vzB52P92dPOr5B/VKC85ukguuWCeHOkkY39ikrMJhapBmYJBoZmKaamZsYJBlZpFiaWRolGliYm5smppiZbag5ltUQyMgw/7w2MyMDKwMjAxMDiM/AAAC7Vx4g",
        "007eJxTYLC9qRswm/fL3uzbWZXT3d7V/NdvyvJwT/gufeex+c7tGukKDKYWaQYmiUYGpqlm5iYGSUYWKZZmlkaJBhbm5qaJKWZmzFXHshoCGRnCPixmZWSAQBCfn8GtKDMvNSWjNElBtyS1uISBAQDmhiOv"
    )

    fun getPrimaryToken(): String {
        return LOCKED_TEMP_TOKENS.first()
    }

    fun init(context: Context) {
        if (rtcEngine != null) return

        val config = RtcEngineConfig()
        config.mContext = context
        config.mAppId = AGORA_APP_ID
        config.mEventHandler = object : IRtcEngineEventHandler() {
            override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                Log.d("AgoraManager", "Joined channel: $channel")
            }
        }
        try {
            rtcEngine = RtcEngine.create(config)
            rtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
            rtcEngine?.enableVideo()
        } catch (e: Exception) {
            Log.e("AgoraManager", "Failed to create RtcEngine", e)
        }
    }

    fun joinChannel(channelName: String, token: String? = getPrimaryToken(), uid: Int = 0) {
        val activeToken = if (token.isNullOrBlank()) getPrimaryToken() else token
        rtcEngine?.joinChannel(activeToken, channelName, null, uid)
    }

    fun leaveChannel() {
        rtcEngine?.leaveChannel()
    }

    fun destroy() {
        RtcEngine.destroy()
        rtcEngine = null
    }

    fun getEngine(): RtcEngine? = rtcEngine
}
