package com.dqd2022

object Config {
    const val Debug = BuildConfig.DEBUG_MODE
    const val AppName = BuildConfig.APP_NAME
    const val MMKV_ID = BuildConfig.MMKV_ID
    const val STUN = BuildConfig.STUN
    const val TURN = BuildConfig.TURN
    const val TURN_USER = BuildConfig.TURN_USER
    const val TURN_PASS = BuildConfig.TURN_PASS
    const val SIGNAL_SERVER = BuildConfig.SIGNAL_SERVER
    const val COMET_SERVER = BuildConfig.COMET_SERVER // 如果有多个 comet 实例，ip 应该通过服务发现获取
    const val IMBIZ = BuildConfig.IMBIZ_API
    const val VideoApi = BuildConfig.VIDEO_API
}