package com.wangkai.audioplayerproject.manager;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;

/**
 * service侧业务层管理
 */
public class ServicePlayerManager {
    private final String TAG = ServicePlayerManager.class.getSimpleName();
    private MediaSessionCallback mediaSessionCallback;
    private ServicePlayerManagerCallback servicePlayerManagerCallback;
    private MediaPlayer player;
    /**
     * Callback是MediaSession的内部类，它是一个抽象类，专门用来通过MediaSession的使用者一些回调
     * MediaSession是受控端，使用者是service
     */
    private class MediaSessionCallback extends MediaSession.Callback{
        @Override
        public boolean onMediaButtonEvent(@NonNull Intent mediaButtonIntent) {
            int code = mediaButtonIntent.getIntExtra("AUDIO_KEY_CODE", -1);
            return super.onMediaButtonEvent(mediaButtonIntent);
        }

        @Override
        public void onPlay() {
            // 控制端点击播放，
            // 先调用MediaController.TransportControls的play方法，回调MediaSession.Callback的onPlay方法，
            // 最终调用MediaPlayer的play()方法
            handlePlayRequest();
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            handlePlayRequest();
        }

        @Override
        public void onPause() {
            super.onPause();
            handlePauseRequest();
        }


        @Override
        public void onSkipToNext() {
            handlePlayNextRequest();
        }

        @Override
        public void onSkipToPrevious() {
            handlePlayPreviousRequest();
        }

        @Override
        public void onStop() {
            handleStopRequest(null);
        }

        @Override
        public void onSeekTo(long pos) {
            player.seekTo((int) pos);
        }

        /**
         * 自定义事件
         * @param action
         * @param extras
         */
        @Override
        public void onCustomAction(@NonNull String action, @Nullable Bundle extras) {
            super.onCustomAction(action, extras);
            if (action.equals("SAVE_REC")){
                //audioHistory.saveRecord(player.getCurrentPosition());  //保存播放记录
            }
        }
    }

    /**
     * 构造方法初始化一个播放器和自己类中的回调接口
     * @param mediaPlayer
     * @param callback
     */
    public ServicePlayerManager(MediaPlayer mediaPlayer, ServicePlayerManagerCallback callback){
        this.player = mediaPlayer;
        this.servicePlayerManagerCallback = callback;
        this.mediaSessionCallback = new MediaSessionCallback();

    }

    /**
     * 当前MediaSession的回调接口
     * @return
     */
    public MediaSessionCallback getMediaSessionCallback() {
        return mediaSessionCallback;
    }

    /**
     * 当前播放器实例
     */
    public MediaPlayer getPlayer() {
        return player;
    }

    /**
     * 处理播放请求
     */
    public void handlePlayRequest(){
        // player.setDataSource(currentMedia); 这里需要设置播放资源
        player.start();
        // 调用ServicePlayerManager中的接口中的方法，告诉ServicePlayerManager的使用者播放开始了
        servicePlayerManagerCallback.onPlaybackStart();
    }

    public void handlePauseRequest(){}

    /**
     * 播放上一首音乐
     */
    private void handlePlayNextRequest(){

    }

    /**
     * 播放下一首音乐
     */
    private void handlePlayPreviousRequest(){

    }

    /**
     * 暂停播放，同时告诉ServicePlayerManager的使用者播放停止了
     */
    public void handlerPauseRequest(){
        if (player.isPlaying()){
            player.pause();
            servicePlayerManagerCallback.onPlaybackStop();
        }
    }

    public void handleStopRequest(String withError){
        player.stop();
        servicePlayerManagerCallback.onPlaybackStop();
        //updatePlaybackState(withError);
    }

    private long last = 0;
    public void handleMediaButtonRequest(int code){
        long current = System.currentTimeMillis();
        if (current - last < 500){
            return;
        }
        last = current;
        if (code == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE){
            if (player.isPlaying()){
                handlePauseRequest();
            }else {
                handlePlayRequest();
            }
        }else if (code == KeyEvent.KEYCODE_MEDIA_NEXT){
            handlePlayNextRequest();
        }else if (code == KeyEvent.KEYCODE_MEDIA_PREVIOUS){
            handlePlayPreviousRequest();
        }
    }

    /**
     * ServicePlayerManger中的接口回调，给ServicePlayerManager的使用者进行回调信息
     */
    public interface ServicePlayerManagerCallback{
        void onPlaybackStart();
        void onNotificationRequired();
        void onPlaybackStop();
        void onPlaybackStateUpdate(PlaybackState newState);
        void onPlayComplete();
        void onBufferUpdate(int percent);
    }
}
