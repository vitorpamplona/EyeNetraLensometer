/**
 * Copyright (c) 2024 Vitor Pamplona
 *
 * This program is offered under a commercial and under the AGPL license.
 * For commercial licensing, contact me at vitor@vitorpamplona.com.
 * For AGPL licensing, see below.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * This application has not been clinically tested, approved by or registered in any health agency.
 * Even though this repository grants licenses to use to any person that follow it's license,
 * any clinical or commercial use must additionally follow the laws and regulations of the
 * pertinent jurisdictions. Having a license to use the source code does not imply on having
 * regulatory approvals to use or market any part of this code.
 */
package com.vitorpamplona.netrometer.printer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class BluetoothService {
    private static final String TAG = "BluetoothService";
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_CONNECTION_LOST = 5;
    public static final int MESSAGE_UNABLE_CONNECT = 6;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    private BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    private Handler mHandler;
    private BluetoothService.ConnectThread mConnectThread;
    private BluetoothService.ConnectedStream mConnectedStream;
    private int mState = 0;

    public BluetoothService(Context context, Handler handler) {
        this.mHandler = handler;
    }

    public synchronized boolean isAvailable() {
        return this.mAdapter != null;
    }

    public synchronized boolean isBTopen() {
        return this.mAdapter.isEnabled();
    }

    public synchronized BluetoothDevice getDevByMac(String mac) {
        return this.mAdapter.getRemoteDevice(mac);
    }

    public synchronized BluetoothDevice getDevByName(String name) {
        BluetoothDevice tem_dev = null;
        Set pairedDevices = this.getPairedDev();
        if(pairedDevices.size() > 0) {
            Iterator var5 = pairedDevices.iterator();

            while(var5.hasNext()) {
                BluetoothDevice device = (BluetoothDevice)var5.next();
                if(device.getName().indexOf(name) != -1) {
                    tem_dev = device;
                    break;
                }
            }
        }

        return tem_dev;
    }

    public synchronized void sendMessage(String message, String charset) {
        if(message.length() > 0) {
            byte[] send;
            try {
                send = message.getBytes(charset);
            } catch (UnsupportedEncodingException var5) {
                send = message.getBytes();
            }

            this.write(send);
            byte[] tail = new byte[]{(byte)10, (byte)13, (byte)0};
            this.write(tail);
        }

    }

    public synchronized Set<BluetoothDevice> getPairedDev() {
        Set dev = null;
        dev = this.mAdapter.getBondedDevices();
        return dev;
    }

    private synchronized void setState(int state) {
        if (this.mState != state) {
            this.mState = state;
            this.mHandler.obtainMessage(MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
        }
    }

    public synchronized int getState() {
        return this.mState;
    }

    public synchronized void connect(BluetoothDevice device) {
        Log.d("BluetoothService", "connect to: " + device);
        if(this.mState == STATE_CONNECTING && this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if(this.mConnectedStream != null) {
            this.mConnectedStream.close();
            this.mConnectedStream = null;
        }

        this.mConnectThread = new BluetoothService.ConnectThread(device);
        this.mConnectThread.start();
        this.setState(STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d("BluetoothService", "connected");
        if(this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if(this.mConnectedStream != null) {
            this.mConnectedStream.close();
            this.mConnectedStream = null;
        }

        this.mConnectedStream = new BluetoothService.ConnectedStream(socket);
        Message msg = this.mHandler.obtainMessage(MESSAGE_DEVICE_NAME );
        this.mHandler.sendMessage(msg);
        this.setState(STATE_CONNECTED);
    }

    public synchronized void stop() {
        Log.d("BluetoothService", "stop");
        this.setState(STATE_NONE);
        if(this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if(this.mConnectedStream != null) {
            this.mConnectedStream.close();
            this.mConnectedStream = null;
        }

    }

    public void write(byte[] out) {
        BluetoothService.ConnectedStream r;
        synchronized(this) {
            if(this.mState != STATE_CONNECTED) {
                return;
            }

            r = this.mConnectedStream;
        }

        r.write(out);
    }

    private void connectionFailed() {
        this.setState(STATE_NONE);
        Message msg = this.mHandler.obtainMessage(MESSAGE_UNABLE_CONNECT);
        this.mHandler.sendMessage(msg);
    }

    private void connectionLost() {
        Message msg = this.mHandler.obtainMessage(MESSAGE_CONNECTION_LOST);
        this.mHandler.sendMessage(msg);
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            this.mmDevice = device;
            this.mmSocket = null;

            try {
                this.mmSocket = device.createRfcommSocketToServiceRecord(BluetoothService.MY_UUID);
            } catch (IOException var5) {
                Log.e("BluetoothService", "create() failed", var5);
            }
        }

        public void run() {
            Log.i("BluetoothService", "BEGIN mConnectThread");
            this.setName("ConnectThread");

            try {
                this.mmSocket.connect();
            } catch (IOException var5) {
                Log.e("BluetoothService", "Connecting failed", var5);
                BluetoothService.this.connectionFailed();

                try {
                    this.mmSocket.close();
                } catch (IOException var3) {
                    Log.e("BluetoothService", "unable to close() socket during connection failure", var3);
                }

                return;
            }

            BluetoothService e = BluetoothService.this;
            synchronized(BluetoothService.this) {
                BluetoothService.this.mConnectThread = null;
            }

            BluetoothService.this.connected(this.mmSocket, this.mmDevice);
            Log.i("BluetoothService", "END mConnectThread");
        }

        public void cancel() {
            try {
                this.mmSocket.close();
            } catch (IOException var2) {
                Log.e("BluetoothService", "close() of connect socket failed", var2);
            }
        }
    }

    private class ConnectedStream {
        private BluetoothSocket mmSocket;
        private OutputStream mmOutStream;

        public ConnectedStream(BluetoothSocket socket) {
            Log.d("BluetoothService", "create ConnectedThread");
            this.mmSocket = socket;

            try {
                this.mmOutStream = socket.getOutputStream();
            } catch (IOException var6) {
                Log.e("BluetoothService", "temp sockets not created", var6);
            }
        }

        public void write(byte[] buffer) {
            try {
                this.mmOutStream.write(buffer);
                BluetoothService.this.mHandler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException var3) {
                Log.e("BluetoothService", "Exception during write", var3);
            }

        }

        public void close() {
            try {
                this.mmSocket.close();
            } catch (IOException var2) {
                Log.e("BluetoothService", "close() of connect socket failed", var2);
            }

        }
    }
}
