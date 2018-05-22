package com.wangkai.audioplayerproject.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.wangkai.audioplayerproject.IServiceCallback;
import com.wangkai.audioplayerproject.IServiceConstants;
import com.wangkai.audioplayerproject.service.AudioService;

import java.lang.ref.WeakReference;

/**
 * 绑定service与取消service的封装
 * 绑定service,bindService
 * 接着走到onServiceConnected方法，在该方法中通过Messenger给service发送消息
 * 最后service端会给client端发送一个消息，client端在handMessage中处理消息，并回调成功的方法
 * 用来绑定连接service,使用方式如下：
 * 1：实例化ConnectionManager
 * 2: 设置ConnectCallback: setConnectCallback()
 * 3: 在页面启动时调用connect()
 * 4: 在页面不可用时调用disConnect()
 *
 */

/**
 * 两个接口
 * IServiceCallback：这个接口是通知自己service连接成功了
 * ConnectionManagerConnectCallback：这个接口是通知ConnectionManager的使用者service连接成功了
 */
public class ConnectionManager implements IServiceCallback, IServiceConstants {
    private static final String TAG = ConnectionManager.class.getSimpleName();

    // 绑定service的时候呈现的三种状态
    private static final int STATE_CONNECTING = 0;  // 正在连接
    private static final int STATE_CONNECTED = 1;  // 已连接
    private static final int STATE_DISCONNECTED = 2;  // 未连接状态

    // 当前service的绑定状态，未连接
    private int mConnectState = STATE_DISCONNECTED;

    private MyServiceConnection mServiceConnection;
    private Context mContext;

    // 通知ConnectionManagerConnectCallback的使用者绑定成功或者失败的回调
    private ConnectionManagerConnectCallback mConnectCallback;
    // client侧的信使
    private Messenger mClientMessenger;

    // 该类封装了发送消息的过程
    private ServiceBinderWrapper mServiceBinderWrapper;

    private HandMessageFromServiceHandler mHandler = new HandMessageFromServiceHandler(this);

    public ConnectionManager(Context context) {
        mContext = context;
    }

    /**
     * 必须通过bindService来开启服务，才能与ServiceConnection产生关联
     * 调用bindService后的生命周期 onCreate-onBind-onServiceConnected
     * 注意：onBind方法必须返回了IBinder的实例，才会有onServiceConnected方法的调用
     */
    class MyServiceConnection implements ServiceConnection {

        /**
         * service连接成功时回调的方法
         *
         * @param name
         * @param service
         */
        @Override
        public void onServiceConnected(ComponentName name, final IBinder service) {
            Log.e(TAG, "onServiceConnected");
            postOrRun(new Runnable() {
                @Override
                public void run() {
                    //开启服务后已经执行到onServiceConnected方法，将连接状态更新为正在连接
                    mConnectState = STATE_CONNECTING;
                    mServiceBinderWrapper = new ServiceBinderWrapper(service);  // 拿到了service端的Messenger
                    mClientMessenger = new Messenger(mHandler);  // Messenger实例化的方式，通过处理消息的handler来实例化Messenger
                    mHandler.setCallbackMessenger(mClientMessenger);  // 把client的messenger传递给了handler
                    try {
                        mServiceBinderWrapper.connect(mClientMessenger);  // 向service发送链接请求，将client端的messenger传递过去
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        /**
         * 取消service连接调用的方法
         *
         * @param name
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            postOrRun(new Runnable() {
                @Override
                public void run() {
                    mClientMessenger = null;
                    mServiceBinderWrapper = null;
                    mHandler.setCallbackMessenger(null);
                    mConnectState = STATE_DISCONNECTED;
                    // 告诉ConnectionManager连接断开了
                    if (mConnectCallback != null){
                        mConnectCallback.connectFailed();
                    }
                }
            });
        }

        private void postOrRun(Runnable runnable){
            // Thread.currentThread()：获取当前运行的线程
            // mHandler.getLooper().getThread()：获取handler所运行的线程
            if (Thread.currentThread() == mHandler.getLooper().getThread()){
                runnable.run();
            }else {
                mHandler.post(runnable);
            }
        }

    }
    //############################################################################
    // 客户端client最后一次握手的接口回调，确认连接成功或者连接失败调用的方法
    // 最后一次握手成功，此时表示连接可用，可以设置session等params
    @Override
    public void onServiceConnectedSu(Messenger messengerCallback, Message serviceMessage) {
        if (mConnectState != STATE_CONNECTING)return;
        Bundle bundle = serviceMessage.getData();
        mMediaSessionToken = bundle.getParcelable(DATA_MEDIA_SESSION_TOKEN);
        state = bundle.getParcelable("init_state");
        mConnectState = STATE_CONNECTED;
        if (mConnectCallback != null){
            mConnectCallback.connectSu();
        }
    }

    /**
     * 最后一次握手失败，即service绑定失败
     * @param messengerCallback
     */
    @Override
    public void onServiceConnectedFailed(Messenger messengerCallback) {
        mConnectState = STATE_DISCONNECTED;

        // 通知客户端，连接失败
        if (mConnectCallback != null){
            mConnectCallback.connectFailed();
        }

        forceCloseConnection();
    }

    /**
     * 设置一个拿到ConnectionManagerCallback的引用的方法
     * @param connectCallback
     */
    public void setConnectCallback(ConnectionManagerConnectCallback connectCallback){
        mConnectCallback = connectCallback;
    }

    /**
     * 绑定service
     */
    public void connect(){
        Log.d(TAG,"connect()");
        if (mConnectState != STATE_DISCONNECTED){
            return;
        }
        mConnectState = STATE_CONNECTING;
        ServiceConnection serviceConnection = new MyServiceConnection();
        Intent intent = new Intent(AUDIO_ACTION);
        intent.setClass(mContext, AudioService.class);
        mContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 页面不可用的时候调用
     */
    public void disConnect(){
        if (mClientMessenger != null){
            // 取消连接，给service端发送一个消息
            try {
                mServiceBinderWrapper.disConnect(mClientMessenger);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            forceCloseConnection();
        }
    }

    private MediaSession.Token mMediaSessionToken;
    public MediaSession.Token getMediaSessionToken(){
        return mMediaSessionToken;
    }
    private PlaybackState state;

    /**
     * 获取播放状态
     * @return
     */
    public PlaybackState getPlayState() {
        return state;
    }

    /**
     * 强制断开连接重置相关变量
     */
    private void forceCloseConnection(){
        if (mServiceConnection != null){
            mContext.unbindService(mServiceConnection);
        }
        mConnectState = STATE_DISCONNECTED;
        mServiceConnection = null;
        mClientMessenger = null;
        mServiceBinderWrapper = null;
        mMediaSessionToken = null;
    }


    public interface ConnectionManagerConnectCallback {

        /**
         * 绑定成功
         */
        void connectSu();

        /**
         * 绑定失败
         */
        void connectFailed();
    }

    /**
     * 这里的handler是用来处理service发来的message
     */
    private static class HandMessageFromServiceHandler extends Handler {
        private IServiceCallback mServiceCallback;

        private WeakReference<Messenger> mClientMessengerReference;

        /**
         * 通过构造方法拿到IServiceCallback的引用，ConnectionManager就是一个IServiceCallback
         * @param callback
         */
        public HandMessageFromServiceHandler(IServiceCallback callback) {
            mServiceCallback = callback;
        }

        /**
         * 处理service端发来的message
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mClientMessengerReference == null)return;
            switch (msg.what){
                case CLIENT_MSG_CONNECT_SU:
                    // 如果连接成功，则告诉ConnectionManager连接成功了，通过接口回调的方式告诉ConnectionManager
                    mServiceCallback.onServiceConnectedSu(mClientMessengerReference.get(), msg);
                    break;
                case SERVICE_MSG_ON_CONNECT_FAIL:
                    // 连接失败，告诉ConnectionManager连接失败了，也是通过接口回调的方法通知ConnectionManager
                    mServiceCallback.onServiceConnectedFailed(mClientMessengerReference.get());
                    break;
                    default:
                        Log.e("TAG", "handler default");
            }
        }
        void setCallbackMessenger(Messenger callbackMessenger){
            mClientMessengerReference = new WeakReference<>(callbackMessenger);
        }
    }

    /**
     * 通过service端的Messenger给service回发消息，其实就是client端持有service端的Messenger的引用
     */
    private static class ServiceBinderWrapper{
        private Messenger mServiceMessenger;
        public ServiceBinderWrapper(IBinder binder){
            // 在实例化的时候拿到service端的messenger
            mServiceMessenger = new Messenger(binder);
        }

        /**
         * 向service端发送消息，请求连接，通过service端的Messenger的引用
        @param clientMessenger
         */
        void connect(Messenger clientMessenger)throws RemoteException{
            Bundle bundle = new Bundle();
            sendRequest(CLIENT_MSG_CONNECT_SU, bundle, clientMessenger);
        }

        /**
         * 向service端发送消息，请求断开连接
         * @param clientMessenger
         * @throws RemoteException
         */
        void disConnect(Messenger clientMessenger)throws RemoteException{
            sendRequest(CLIENT_MSG_DISCONNECT, null, clientMessenger);
        }

        /**
         * 该方法封装了使用Messenger发送message的过程
         * @param what
         * @param bundle
         * @param clientMessenger client侧的Messenger，将他传递给service
         * @throws RemoteException
         */
        private void sendRequest(int what, Bundle bundle, Messenger clientMessenger)throws RemoteException{
            Message message = Message.obtain();
            message.what = what;
            message.setData(bundle);
            message.replyTo = clientMessenger;
            mServiceMessenger.send(message);
        }
    }

}

