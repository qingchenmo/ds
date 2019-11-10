package com.ds.common;

import java.util.List;

public class BeenObser {
	private Object o;
	private int key;
	private List<IDataObserver> obser;
	public BeenObser(Object o, int key, List<IDataObserver> obser) {
		super();
		this.o = o;
		this.key = key;
		this.obser = obser;
	}
	public BeenObser(Object o, int key) {
		super();
		this.o = o;
		this.key = key;
	}
	public Object getO() {
		return o;
	}
	public void setO(Object o) {
		this.o = o;
	}
	public int getKey() {
		return key;
	}
	public void setKey(int key) {
		this.key = key;
	}
	public List<IDataObserver> getObser() {
		return obser;
	}

	
}
