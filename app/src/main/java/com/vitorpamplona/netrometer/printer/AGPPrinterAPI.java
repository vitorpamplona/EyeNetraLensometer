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

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.vitorpamplona.netrometer.model.db.objects.DebugExam;

import java.util.List;

public class AGPPrinterAPI {

    public static final String GBK = "GBK";
    BluetoothService mService = null;
    BluetoothDevice con_dev = null;

    private DebugExam nextToPrint2;

    private Context context;
    private TryConnecting connectingListener;
    boolean connectable = false;

    public static interface TryConnecting {
        public void isConnectable();
        public void cannotConnect();
    }

    public AGPPrinterAPI(Context context, TryConnecting connectingListener) {
        this.context = context;
        this.connectingListener = connectingListener;
        this.mService = new BluetoothService(context, mHandlerConnectable);

        if (isBluetoothAvailable()) {
            con_dev = mService.getDevByName("BlueTooth Printer");
        }

        if (con_dev != null) {
            mService.connect(con_dev);
        }
    }

    public void destroy() {
        if (mService != null)
            mService.stop();
        mService = null;
        nextToPrint2 = null;
    }

    public boolean isBluetoothAvailable() {
        return mService != null && mService.isAvailable() && mService.isBTopen();
    }

    public boolean isPrinterAvailable() {
        return isBluetoothAvailable() && con_dev != null && connectable;
    }

    public boolean print(DebugExam results) {
        if (!isPrinterAvailable()) return false;

        Log.d("AGPPrinter Connecting to Print", "Printing Results");

        nextToPrint2 = results;

        mService = new BluetoothService(AGPPrinterAPI.this.context, mHandler);
        if (isBluetoothAvailable()) {
            con_dev = mService.getDevByName("BlueTooth Printer");
        }

        if (con_dev != null) {
            mService.connect(con_dev);
        }

        return true;
    }

    public void printDebugExam() {
        if (nextToPrint2 == null) return;

        List<String> lines = new ResultsFormatter(context).getFormattedResults(nextToPrint2);

        for (String line : lines) {
            mService.sendMessage(line, GBK);
        }
    }

    private final Handler mHandlerConnectable = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //Log.d("AGPPrinter Checking IF Connectable ", msg.toString());
            switch (msg.what) {
                case BluetoothService.MESSAGE_DEVICE_NAME:
                    Log.d("AGPPrinter Checking IF Connectable", "Device Name");
                    break;
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Log.d("AGPPrinter Checking IF Connectable", "State Change Msg: Connectable");
                            connectable = true;
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (mService != null)
                                mService.stop();
                            if (connectingListener != null)
                                connectingListener.isConnectable();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Log.d("AGPPrinter Checking IF Connectable", "State Change Msg: Connecting");
                            break;
                        case BluetoothService.STATE_LISTEN:
                            Log.d("AGPPrinter Checking IF Connectable", "State Change Msg: Listening");
                            break;
                        case BluetoothService.STATE_NONE:
                            Log.d("AGPPrinter Checking IF Connectable", "State Change Msg: None");
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_CONNECTION_LOST:
                    Log.d("AGPPrinter Checking IF Connectable", "Connection Lost");
                    break;
                case BluetoothService.MESSAGE_UNABLE_CONNECT:
                    Log.d("AGPPrinter Checking IF Connectable", "Unable to connect");
                    connectable = false;
                    if (mService != null) mService.stop();
                    mService = null;
                    if (connectingListener != null)
                        connectingListener.cannotConnect();
                    break;
            }
        }

    };

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_DEVICE_NAME:
                    Log.d("AGPPrinter Printing", "Msg Device Name");
                    break;
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Log.d("AGPPrinter Printing", "Connected, Printing.");
                            printDebugExam();
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Log.d("AGPPrinter Printing", "Connected, Killong.");
                            if (mService != null) mService.stop();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Log.d("AGPPrinter Printing", "Connecting.");
                            break;
                        case BluetoothService.STATE_LISTEN:
                            Log.d("AGPPrinter Printing", "Listening. ");
                            break;
                        case BluetoothService.STATE_NONE:
                            Log.d("AGPPrinter Printing", "None.");
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_CONNECTION_LOST:
                    Log.d("AGPPrinter Printing", "Connection Lost.");
                    if (mService != null) mService.stop();
                    break;
                case BluetoothService.MESSAGE_UNABLE_CONNECT:
                    Log.d("AGPPrinter Printing", "Unable to Connectc.");
                    if (mService != null) mService.stop();
                    break;
            }
        }

    };

}
