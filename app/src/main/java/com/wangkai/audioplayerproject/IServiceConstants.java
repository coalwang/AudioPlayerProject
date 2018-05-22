package com.wangkai.audioplayerproject;

public interface IServiceConstants {

    // 传递session使用的key
    String DATA_MEDIA_SESSION_TOKEN = "data_media_session_token";

    // audioService action
    String AUDIO_ACTION = "audio_service_action";

    int SERVICE_MSG_CONNECT_SU = 1;  // service侧连接成功
    int SERVICE_MSG_ON_CONNECT_FAIL = 2;  // service侧连接失败

    int CLIENT_MSG_CONNECT_SU = 3;
    int CLIENT_MSG_DISCONNECT = 4;

}
