package com.wangkai.audioplayerproject;

import android.os.Message;
import android.os.Messenger;

/**
 * 客户端client最后一个握手的接口回调，确认连接成功
 */
public interface IServiceCallback {  // 接口中的方法默认是public

    /**
     * service连接成功时回调的方法
     * @param messengerCallback
     * @param serviceMessage
     */
    void onServiceConnectedSu(Messenger messengerCallback, Message serviceMessage);

    void onServiceConnectedFailed(Messenger messengerCallback);
}
