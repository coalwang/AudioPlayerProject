package com.wangkai.audioplayerproject.service;

import android.app.Service;
import android.content.Intent;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;

public class AudioBaseService extends Service implements IServiceConstants{

    private static final String TAG = AudioBaseService.class.getSimpleName();
    private ServiceHandler handler = new ServiceHandler();
    private Messenger serviceMessenger;
    private MediaSession.Token token;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (AUDIO_ACTION.equals(intent.getAction())){
            return serviceMessenger.getBinder();
        }
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        serviceMessenger = new Messenger(handler);
    }

    protected void setSessionToken(MediaSession.Token token){
        this.token = token;
    }

    protected PlaybackState getNowPlayState() {
        return null;
    }

    /**
     * 处理service端连接客户端的操作
     */
    private class ServiceImpl{
//        private void connect(Bundle data, )
//    }

//        private class ServiceImpl{
//
//            public void connect(final Bundle data,final ServiceCallbacks serviceCallbacks){
//                handler.postOrRun(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            serviceCallbacks.onConnect(token,data);
//                        } catch (RemoteException e) {
//                            e.printStackTrace();
//                            Log.d(TAG,"error: " + e.toString());
//                        }
//                    }
//                });
//
//            }
//
//            public void disConnect(ServiceCallbacks serviceCallbacks){
//
//            }
//
//        }

    /**
     * 处理client侧发来的消息
     */
    private class ServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();

        }
    }

//        private final class ServiceHandler extends Handler{
//            private final ServiceImpl serviceImpl = new ServiceImpl();
//            @Override
//            public void handleMessage(Message msg) {
//                Bundle clientData = msg.getData();
//                switch (msg.what){
//                    case CLIENT_MSG_CONNECT:
//                        Log.d(TAG,"get client msg: CLIENT_MSG_CONNECT");
//                        serviceImpl.connect(clientData, new ServiceCallbacks(msg.replyTo));
//                        break;
//                    case CLIENT_MSG_DISCONNECT:
//                        Log.d(TAG,"get client msg: CLIENT_MSG_CONNECT_DISCONNECT");
//                        serviceImpl.disConnect(new ServiceCallbacks(msg.replyTo));
//                        break;
//                    default:
//                        Log.d(TAG,"default client msg: " + msg.what);
//                }
//            }
//
//            public void postOrRun(Runnable r) {
//                if (Thread.currentThread() == getLooper().getThread()) {
//                    r.run();
//                } else {
//                    post(r);
//                }
//            }
//        }
//

    private class ServiceCallback{
        private Messenger clientMessenger;
        public ServiceCallback(Messenger clientMessenger){
            this.clientMessenger = clientMessenger;
        }

        public void onConnect(MediaSession.Token session, Bundle extras){
            if (extras == null){
                extras = new Bundle();
            }
            extras.putParcelable(DATA_MEDIA_SESSION_TOKEN, session);
            extras.putParcelable("init_state", getNowPlayState());
            sendRequest(SERVICE_MSG_CONNECT_SU, extras);
        }

        public void onConnectFailed(){
            sendRequest(SERVICE_MSG_ON_CONNECT_FAIL, null);
        }

        private void sendRequest(int what, Bundle data){
            if (data == null)data = new Bundle();
            Message message = Message.obtain();
            message.what = what;
            message.setData(data);
            try {
                clientMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}


