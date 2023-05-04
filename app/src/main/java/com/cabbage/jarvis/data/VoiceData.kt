package com.cabbage.jarvis.data

data class VoiceData(var text: String? = "")
data class ChannelListBean(
    var avatarDrawable: Int,
    var characterName: String,
    var characterTime: String = "",
    var characterPath: String = "",
    var characterVitsPath: String = "",
    var fromExternal: Boolean = false
)
enum class VITSLoadStatus {
    STATE_SUCCESS,
    STATE_FAILED,
    STATE_DEFAULT
}
