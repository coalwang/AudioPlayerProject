package com.wangkai.audioplayerproject.app;

public class AppApplication {

    //##############################################################################
    /**
     * 检查网络状态相关设置
     */

//    private NetworkInfo.State cacheState;
//    private int netType;
//
//    private void listenToNetworkStatusChange() {
//        mNetworkStatusReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                //NetEasePushMessageService_V1 推送服务会发送网络切换广播
//                String processName = getCurProcessName(context);
//                if (TextUtils.isEmpty(processName) || !processName.equals(getPackageName())) {
//                    return;
//                }
//
//                // 这里一次变化会有多次通知，只有当网络通了才进行通知
//                String action = intent.getAction();
//                PalLog.i("NetworkStatusChange", action);
//                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
//                    Bundle b = intent.getExtras();
//                    if (b == null) {
//                        PalLog.d("NetworkStatusChange", "bundle == null");
//                        return;
//                    }
//                    NetworkInfo netInfo = (NetworkInfo)
//                            b.get(ConnectivityManager.EXTRA_NETWORK_INFO);
//                    NetworkInfo.State state = netInfo.getState();
//                    int netInfoType = netInfo.getType();
//                    if (cacheState == null) {
//                        cacheState = state;
//                        netType = netInfoType;// 移动网络 (2G/3G/4G 间切换)  or  wifi
//                    } else if (cacheState == state && netType == netInfoType) {
//                        PalLog.i("NetworkStatusChange", "state : " + state.name() + " -- " + netInfo.getTypeName() + " : " + netInfo.getType());
//                        PalLog.i("NetworkStatusChange", " 相同 状态广播  ");
//                        return;
//                    }
//                    NetworkInfo activeNetInfo = getCurNetworkInfo();
//                    if (activeNetInfo != null) {
//                        int activeNetType = activeNetInfo.getType();
//                        PalLog.i("NetworkStatusChange", activeNetInfo.getTypeName() + " : " + activeNetInfo.getType());
//                        if (activeNetType != netInfoType) {  // 类型不同  认为是中间状态 不处理
//                            PalLog.i("NetworkStatusChange", "类型不同 判断处于中间状态  ：  不处理 ");
//                        } else {
//                            cacheState = state;
//                            netType = netInfoType;
//                            if (activeNetInfo.isAvailable()) {
//                                for (NetworkChangeListener l : mNetworkListeners) {
//                                    l.onNetworkChange();
//                                }
//                            }
//                            PalLog.i("NetworkStatusChange", "执行回调操作state : " + state.name() + " -- " + netInfo.getTypeName() + " : " + netInfo.getType());
//                        }
//                    } else {
//                        cacheState = state;
//                        netType = netInfoType;
//                        PalLog.i("NetworkStatusChange", "activeNetInfo == null ");
//                    }
//                }
//
//            }
//        };
//        IntentFilter filter = new IntentFilter(
//                ConnectivityManager.CONNECTIVITY_ACTION);
//        registerReceiver(mNetworkStatusReceiver, filter);
//    }
//
//    public void addNetworkChangeListener(NetworkChangeListener l) {
//        mNetworkListeners.add(l);
//    }
//
//    public void removeNetworkChangeListener(NetworkChangeListener l) {
//        mNetworkListeners.remove(l);
//    }
//
//    // 获取当前的网络信息：2g/3g/4g/wifi
//    private NetworkInfo getCurNetworkInfo() {
//        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        return cm.getActiveNetworkInfo();
//    }
//
//    public boolean isWifiNetwork() {
//        NetworkInfo info = getCurNetworkInfo();
//        return (info != null && info.getType() == ConnectivityManager.TYPE_WIFI);
//    }
//
//    public boolean isMobileNetwork() {
//        NetworkInfo info = getCurNetworkInfo();
//        return (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE);
//    }
//
//    public boolean hasNetworkConnection() {
//        NetworkInfo info = getCurNetworkInfo();
//        return (info != null && info.isConnectedOrConnecting());
//    }
}
