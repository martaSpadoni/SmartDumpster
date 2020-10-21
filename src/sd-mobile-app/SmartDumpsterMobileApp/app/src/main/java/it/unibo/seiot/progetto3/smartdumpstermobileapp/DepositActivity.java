package it.unibo.seiot.progetto3.smartdumpstermobileapp;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.net.HttpURLConnection;
import java.util.UUID;

import it.unibo.seiot.progetto3.smartdumpstermobileapp.netutils.Http;
import it.unibo.seiot.progetto3.smartdumpstermobileapp.netutils.HttpResponse;
import it.unibo.seiot.progetto3.smartdumpstermobileapp.utils.C;
import unibo.btlib.BluetoothChannel;
import unibo.btlib.BluetoothUtils;
import unibo.btlib.CommChannel;
import unibo.btlib.ConnectToBluetoothServerTask;
import unibo.btlib.ConnectionTask;
import unibo.btlib.RealBluetoothChannel;
import unibo.btlib.exceptions.BluetoothDeviceNotFound;

public class DepositActivity extends AppCompatActivity {

    private BluetoothChannel btChannel;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deposit_activity);
        setAllBtnEnable(false);
        findViewById(R.id.timeBtn).setEnabled(false);
        setAllBtnListener();
        try {
            connectToBTServer();
        } catch (BluetoothDeviceNotFound bluetoothDeviceNotFound) {
            bluetoothDeviceNotFound.printStackTrace();
        }
    }
    private void setAllBtnEnable(boolean enable) {
        findViewById(R.id.Abtn).setEnabled(enable);
        findViewById(R.id.Bbtn).setEnabled(enable);
        findViewById(R.id.Cbtn).setEnabled(enable);
        findViewById(R.id.timeBtn).setEnabled(!enable);
    }


    private void setAllBtnListener(){
        findViewById(R.id.Abtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btChannel.sendMessage("0");
                setAllBtnEnable(false);
            }
        });
        findViewById(R.id.Bbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btChannel.sendMessage("1");
                setAllBtnEnable(false);
            }
        });
        findViewById(R.id.Cbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btChannel.sendMessage("2");
                setAllBtnEnable(false);
            }
        });
        findViewById(R.id.timeBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btChannel.sendMessage("TIME");
            }
        });
    }


    private void notifyServer() {

        Http.post(C.URL, "END".getBytes(), new Http.Listener() {
            @Override
            public void onHttpResponseAvailable(HttpResponse response) {
            }
        });
        this.setResult(RESULT_OK);
        this.finish();

    }
    private void connectToBTServer() throws BluetoothDeviceNotFound {
        final BluetoothDevice serverDevice = BluetoothUtils.getPairedDeviceByName(C.bluetooth.BT_DEVICE_ACTING_AS_SERVER_NAME);

        final UUID uuid = BluetoothUtils.getEmbeddedDeviceDefaultUuid();

        new ConnectToBluetoothServerTask(serverDevice, uuid, new ConnectionTask.EventListener() {
            @Override
            public void onConnectionActive(final BluetoothChannel channel) {

                ((TextView) findViewById(R.id.statusLabel)).setText(R.string.sm_connected);

                setAllBtnEnable(true);

                btChannel = channel;
                btChannel.registerListener(new RealBluetoothChannel.Listener() {
                    @Override
                    public void onMessageReceived(String receivedMessage) {
                        btChannel.close();
                        findViewById(R.id.timeBtn).setEnabled(false);
                        notifyServer();

                    }

                    @Override
                    public void onMessageSent(String sentMessage) {
                    }
                });
            }

            @Override
            public void onConnectionCanceled() {
                ((TextView) findViewById(R.id.statusLabel)).setText(String.format("Status : unable to connect, device %s not found!",
                        C.bluetooth.BT_DEVICE_ACTING_AS_SERVER_NAME));
            }
        }).execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        btChannel.close();
    }

}
