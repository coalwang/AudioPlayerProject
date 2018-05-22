package com.wangkai.audioplayerproject.manager;

import android.content.Context;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * client侧音乐框架、服务的业务逻辑管理
 */
public class ClientPlayBackManager {

    private final String TAG = ClientPlayBackManager.class.getSimpleName();  // 不需要生命为static类型
    private static String SESSION_EVENT_PLAY_COMPLETE = "session_event_play_complete";
    private Context context;
    private MediaController mediaController;
    private ConnectionManager connectionManager;
    private boolean isConnected = false;
    private int lastState = 0;
    private VPlayer player;
//        private VopenApp.NetworkChangeListener mNetworkListener;
//        private VPlayer player;
//        private AlertDialog mNetTipDialog;
    private static ClientPlayBackManager sInstance;
    private static ClientPlayBackManager getsInstance(){
        if (sInstance == null){
            synchronized (ClientPlayBackManager.class){
                if (sInstance == null){
                    sInstance = new ClientPlayBackManager();
                }
            }
        }
        return sInstance;
    }

    private List<ClientPlayBackManagerCallback> callbackList = new ArrayList<>();

    /**
     * 绑定ConnectionManager中的接口回调
     * @param context
     */
    private ConnectionManager.ConnectionManagerConnectCallback connectCallback = new ConnectionManager.ConnectionManagerConnectCallback()   {
        /**
         * 与服务绑定成功回调的方法
         */
        @Override
        public void connectSu() {
            isConnected = true;
            // 绑定成功后初始化控制端的MediaController，TransportControls
            // 参数1：context
            // 参数2：token：这个token是连接service成功后，在service生命周期serviceConnection类中获取的
            mediaController = new MediaController(context, connectionManager.getMediaSessionToken());
            mediaController.registerCallback(mediaControllerCallback);  // 注册接口回调，实质是传递一个引用过去，该引用调用接口中的方法
            player = new VPlayer(mediaController);
            updatePlaybackState(connectionManager.getPlayState());
        }

        @Override
        public void connectFailed() {
            Log.d(TAG, "onConnectFailed");
            isConnected = false;
        }
    };

    public void init(Context context){
        this.context = context;
        // client侧音乐框架、服务的业务逻辑管理使用ConnectionManager中的接口回调来通知自己链接服务成功或者失败
        connectionManager = new ConnectionManager(context);
        connectionManager.setConnectCallback(connectCallback);
        //            mNetworkListener = new VopenApp.NetworkChangeListener() {
//                @Override
//                public void onNetworkChange() {
//                    IMediaBean audioBean = AudioDataManager.getInstance().getCurrentAudio();
//                    if (audioBean!=null && AudioDataManager.getInstance().isAudioCached(audioBean.getPid(), audioBean.getPNumber())
//                            || NetUtils.isWIFI(context) || AppSettings.is2g3gPlayAllowed(VopenApp.mContext)) {
//                        return;
//                    }
//                }
//            };
//            VopenApp.getAppInstance().addNetworkChangeListener(mNetworkListener);
    }

    /**
     * 连接audioService
     * 连接audioService已经封装在connectionManager中了，这里直接调用该类中的方法
     */
    public void connect(){
        connectionManager.connect();
    }

    /**
     * 断开和audioService之间的绑定
     * ConnectionManager已经封装了断开连接操作，这里直接调用该类中的方法
     */
    public void disConnect(){
        if (isConnected){
            connectionManager.disConnect();
            isConnected = false; // 状态置为false
        }
        if (mediaController != null){
            mediaController.unregisterCallback(mediaControllerCallback);
        }
        //            VopenApp.getAppInstance().removeNetworkChangeListener(mNetworkListener);
    }

    public boolean isConnected(){
        return isConnected;
    }

    /**
     * 释放
     * @param callback
     */
    public void unRegisterCallback(ClientPlayBackManagerCallback callback){
        callbackList.remove(callback);
    }

    /**
     * 绑定
     * @param callback
     */
    public void registerCallback(ClientPlayBackManagerCallback callback){
        callbackList.add(callback);
    }

    /**
     * 播放状态改变，接口回调
     */
    private MediaController.Callback mediaControllerCallback = new MediaController.Callback() {
        /**
         * 播放事件传递
         * @param event
         * @param extras
         */
        @Override
        public void onSessionEvent(@NonNull String event, @Nullable Bundle extras) {
            super.onSessionEvent(event, extras);
            if (event.equals(SESSION_EVENT_PLAY_COMPLETE)){
                for (ClientPlayBackManagerCallback callback:callbackList){
                    if (callback != null){
                        callback.onAudioStop();
                    }
                }
            }
        }

        /**
         * 播放状态改变回调
         * @param state 传回来的是当前播放状态
         */
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackState state) {
            super.onPlaybackStateChanged(state);
            updatePlaybackState(state);  // 根据状态告诉ClientPlayback的使用者，服务绑定了或者解绑了
        }

        @Override
        public void onMetadataChanged(@Nullable MediaMetadata metadata) {
            super.onMetadataChanged(metadata);
        }
    };



    public interface ClientPlayBackManagerCallback{

        void onAudioStart();

        void onAudioStop();

    }

    /**
     *
     * @param state
     */
    private void updatePlaybackState(PlaybackState state){
        if (state == null){
            return;
        }
        lastState = state.getState();
        switch (lastState){
            case PlaybackState.STATE_PLAYING:
                // 遍历回调接口list，依次通知使用者服务开启了
                for (ClientPlayBackManagerCallback callback:callbackList){
                    if (callback != null){
                        callback.onAudioStart();
                    }
                }
                break;
            case PlaybackState.STATE_STOPPED:
                for (ClientPlayBackManagerCallback callback:callbackList){
                    if (callback != null){
                        callback.onAudioStop();  // 依次通知ClientPlayBackManager的使用者，服务断开
                    }
                }
                break;
                default:
                    break;
        }
    }

    public boolean isPlaying(){
        return lastState == PlaybackState.STATE_PLAYING;
    }
    }
