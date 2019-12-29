/*
 * Copyright 2009 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android_serialport_api;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SerialPort {

    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;
    private String filePath;
    private int baudrate;
    private int flags;

    public SerialPort(String device, int baudrate, int flags) {
        this.filePath = device;
        this.baudrate = baudrate;
        this.flags = flags;
    }

    public boolean open() {
        if (mFd != null && mFileInputStream != null && mFileOutputStream != null) return true;
        try {
            mFd = open(new File(filePath).getAbsolutePath(), baudrate, flags);
            if (mFd == null) {
                return false;
            }
            mFileInputStream = new FileInputStream(mFd);
            mFileOutputStream = new FileOutputStream(mFd);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    public boolean reset() {
        if (mFd == null) return open();
        try {
            if (mFileInputStream != null) mFileInputStream.close();
            if (mFileOutputStream != null) mFileOutputStream.close();
            mFileInputStream = new FileInputStream(mFd);
            mFileOutputStream = new FileOutputStream(mFd);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    public boolean write(byte[] bytes) {
        if (mFileOutputStream == null) return false;
        try {
            mFileOutputStream.write(bytes);
            mFileOutputStream.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public int read(byte[] bytes) {
        if (mFileInputStream == null) return -1;
        try {
            return mFileInputStream.read(bytes);
        } catch (IOException e) {
            return -1;
        }
    }

    public void release() {
        try {
            mFd = null;
            if (mFileOutputStream != null) mFileOutputStream.close();
            if (mFileInputStream != null) mFileInputStream.close();
//            close();
        } catch (Throwable e) {
            Log.e("SerialPort", "release exception msg == " + e.getMessage());
        }
    }

    private native static FileDescriptor open(String path, int baudrate, int flags);

    public native void close();

    static {
        System.loadLibrary("SerialPort");
    }
}
