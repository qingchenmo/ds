package com.ds;

import android.content.Context;
import android.os.Message;

import com.ds.common.BeenObser;
import com.ds.common.GlobleHandle;
import com.ds.common.IDataObserver;
import com.ds.uartManager.DeviceManager;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by gongkan on 2019/8/3.
 */

public class GlobalContext {

    private static  GlobalContext instance = null;

    private Context mContext;

    private GlobleHandle globalHandler = new GlobleHandle();

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    public GlobleHandle getGlobalHandler() {
        return globalHandler;
    }

    private HashMap<Integer, ArrayList<IDataObserver>> obserMap = new HashMap<Integer, ArrayList<IDataObserver>>();

    private DeviceManager deviceManager;

    private GlobalContext(){
        deviceManager = new DeviceManager();
    }
    public static GlobalContext getInstance(){
        if (instance == null){
            instance = new GlobalContext();
        }
        return instance;
    }

    public void notifyDataChanged(int key, Object object) {
        Message message = Message.obtain();
        message.what = 100;
        message.obj = new BeenObser(object, key,obserMap.get(key));
        globalHandler.sendMessage(message);
    }

    public void registObserver(int key, IDataObserver obser) {
        ArrayList<IDataObserver> obsers = obserMap.get(key);
        if (obsers == null) {
            obsers = new ArrayList<IDataObserver>();
            obsers.add(obser);
            obserMap.put(key, obsers);
        }
        if (obsers.contains(obser)) {
            return;
        } else {
            obsers.add(obser);
        }
    }

    public void unRegestObser(int key, IDataObserver obser) {
        ArrayList<IDataObserver> obsers = obserMap.get(key);
        if (obsers == null) {
            return;
        } else {
            if (obsers.contains(obser)) {
                obsers.remove(obser);
            }
        }
    }
    public DeviceManager getDeviceManager() {
        return deviceManager;
    }
}