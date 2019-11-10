package com.ds.uartManager;

import java.io.IOException;

public interface IDevice {
	boolean open();
	boolean isOpen();
	int read(byte[] buf)throws IOException;
	void close();
}
