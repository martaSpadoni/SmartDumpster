package it.unibo.seiot.progetto3.smartdumpstermobileapp;
/*Authors: Matteo Ragazzini, Marta Spadoni*/
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Objects;
import java.util.UUID;

import it.unibo.seiot.progetto3.smartdumpstermobileapp.netutils.Http;
import it.unibo.seiot.progetto3.smartdumpstermobileapp.netutils.HttpResponse;
import it.unibo.seiot.progetto3.smartdumpstermobileapp.utils.C;
import unibo.btlib.BluetoothChannel;
import unibo.btlib.BluetoothUtils;
import unibo.btlib.ConnectToBluetoothServerTask;
import unibo.btlib.ConnectionTask;
import unibo.btlib.RealBluetoothChannel;
import unibo.btlib.exceptions.BluetoothDeviceNotFound;

public class MainActivity extends AppCompatActivity {
    private NetworkInfo activeNetwork;
    private ConnectivityManager cm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter != null && !btAdapter.isEnabled()){
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), C.bluetooth.ENABLE_BT_REQUEST);
        }
         cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = Objects.requireNonNull(cm).getActiveNetworkInfo();
        if(activeNetwork == null){
            new AlertDialog.Builder(this).setMessage(R.string.request_wifi)
                    .setPositiveButton(android.R.string.ok, (DialogInterface d, int u) -> {
                        startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), C.ENABLE_WIFI_REQUEST);
                    }).show();
        }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new HomeFragment(), HomeFragment.class.getName());
    }


    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == C.ENABLE_WIFI_REQUEST ){
            activeNetwork = Objects.requireNonNull(cm).getActiveNetworkInfo();
            if(activeNetwork == null) {
                new AlertDialog.Builder(this).setMessage(R.string.request_wifi)
                        .setPositiveButton(android.R.string.ok, (d, u) -> {
                            startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), C.ENABLE_WIFI_REQUEST);
                        }).show();
            }
        }
        if (requestCode == C.bluetooth.ENABLE_BT_REQUEST && resultCode == RESULT_CANCELED) {
            new AlertDialog.Builder(this).setMessage(R.string.request_bt)
                    .setPositiveButton(android.R.string.ok, (d,u) -> {
                        startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), C.bluetooth.ENABLE_BT_REQUEST );
                    }).show();
        }

        if (requestCode == C.DEPOSIT_ACTIVITY && resultCode == RESULT_OK){
                new AlertDialog.Builder(this)
                        .setTitle(R.string.deposit_done)
                        .setPositiveButton(android.R.string.ok, (d,u)->d.dismiss())
                        .show();

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
