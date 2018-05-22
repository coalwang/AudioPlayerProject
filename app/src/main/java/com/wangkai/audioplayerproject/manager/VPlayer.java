package com.wangkai.audioplayerproject.manager;

import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Bundle;

/**
 * 该类主要对MediaController.TransportControls的一些方法进行了封装
 * 通过该类主要可以完成一些播放动作或者播放状态
 */
public class VPlayer {
    private final String TAG = VPlayer.class.getSimpleName();
    private MediaController mediaController;
    private MediaController.TransportControls controls;


    /**
     * 构造方法，通过传入一个MediaController来获取后续的TransportControls
     * @param mediaController
     */
    public VPlayer(MediaController mediaController){
        if (mediaController == null){
            throw new NullPointerException("the param in VPlayer constructor is null");
        }
        this.mediaController = mediaController;
        controls = mediaController.getTransportControls();
    }

    /**
     *
     * @param mid
     */
    public void play(String mid){
        controls.playFromMediaId(mid, null);
    }

    public void start(){
        controls.play();
    }

    public void pause(){
        controls.pause();
    }

    public void stop(){
        controls.stop();
    }

    public void seekTo(long position){
        controls.seekTo(position);
    }


    public void skipToNext() {
        controls.skipToNext();
    }

    public void skipToPrevious() {
        controls.skipToPrevious();
    }

    public void setSpeed(float speed){
        Bundle bundle = new Bundle();
        bundle.putFloat("SPEED", speed);
        //            AudioDataManager.getInstance().setSpeed(speed);
        controls.sendCustomAction("SET_SPEED", bundle);
    }

    public void saveRec(){
        controls.sendCustomAction("SAVE_REC", null);
    }

    public boolean isPlaying(){
        return mediaController.getPlaybackState().getState() == PlaybackState.STATE_PLAYING;
    }

    /**
     * 是否可以进行暂停
     * @return
     */
    public boolean canPause(){
        int state = mediaController.getPlaybackState().getState();
        // 正在缓冲或者正在播放都可以进行暂停
        return state == PlaybackState.STATE_BUFFERING || state == PlaybackState.STATE_PLAYING;
    }

    public boolean canPlay(){
        int state = mediaController.getPlaybackState().getState();
        return state == PlaybackState.STATE_PAUSED || state == PlaybackState.STATE_STOPPED;
    }

    public boolean isPaused() {
        return mediaController.getPlaybackState().getState() == PlaybackState.STATE_PAUSED;
    }

    public boolean isBuffering() {
        int state = mediaController.getPlaybackState().getState();
        return state == PlaybackState.STATE_BUFFERING;
    }
}
