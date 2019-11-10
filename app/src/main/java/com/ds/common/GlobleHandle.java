package com.ds.common;

import android.os.Handler;
import android.os.Message;

import java.util.List;

public class GlobleHandle extends Handler{
    @Override
    public void handleMessage(Message msg) {
    	super.handleMessage(msg);
    	if (msg.obj instanceof BeenObser) {
    		BeenObser beenObser = (BeenObser) msg.obj;
			List<IDataObserver> obsers = beenObser.getObser();
			if (obsers == null) {
				return;
			} else {
				for (IDataObserver o : obsers) {
					o.update(beenObser.getKey(), beenObser.getO());
				}
			}
    	}
    }
}
