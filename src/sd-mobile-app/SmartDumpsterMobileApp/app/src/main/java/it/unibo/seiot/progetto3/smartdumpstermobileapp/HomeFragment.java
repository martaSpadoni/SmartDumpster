package it.unibo.seiot.progetto3.smartdumpstermobileapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.io.IOException;
import java.net.HttpURLConnection;

import it.unibo.seiot.progetto3.smartdumpstermobileapp.netutils.Http;
import it.unibo.seiot.progetto3.smartdumpstermobileapp.utils.C;
import unibo.btlib.exceptions.BluetoothDeviceNotFound;

public class HomeFragment extends Fragment {


    private View view;
    FragmentActivity activity;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.home_fragment, container, false);
        activity = getActivity();
        if(activity != null){
            view.findViewById(R.id.tokenBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getToken();
                }
            });
        }
        return view;
    }

    private void getToken(){

        Http.get(C.URL, response -> {
            if (response == null){
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.request_token)
                        .setMessage(R.string.sm_not_working)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                getToken();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            } else if(response.code() == HttpURLConnection.HTTP_ACCEPTED){
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.sm_not_available)
                        .setMessage(R.string.try_later)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }else if(response.code() == HttpURLConnection.HTTP_OK){
                activity.startActivityForResult(new Intent(activity, DepositActivity.class), C.DEPOSIT_ACTIVITY);
            }
        });
    }
}
