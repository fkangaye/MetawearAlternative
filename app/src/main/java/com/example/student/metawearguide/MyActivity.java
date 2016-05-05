package com.example.student.metawearguide;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.mbientlab.metawear.MetaWearBleService;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.module.Led;

import static com.mbientlab.metawear.MetaWearBoard.ConnectionStateHandler;

public class MyActivity extends AppCompatActivity implements ServiceConnection {


    private static final String TAG = "METAWEAR";
    private final String MAC_ADDRESS = "C1:92:39:BE:73:77";
    private Button connect;
    private Button led_on;
    private Button led_off;

    //Metawear objects
    private MetaWearBleService.LocalBinder serviceBinder;
    private MetaWearBoard metaWearBoard;
    private Led ledModule;


    public void retrieveBoard() {
        final BluetoothManager btManager = (BluetoothManager) getSystemService(Context
                .BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice = btManager.getAdapter().getRemoteDevice(MAC_ADDRESS);

        //Create a metaWear board object for the bluetooth device
        metaWearBoard = serviceBinder.getMetaWearBoard(remoteDevice);
        metaWearBoard.setConnectionStateHandler(stateHandler);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        getApplicationContext().bindService(new Intent(this, MetaWearBleService.class), this,
                Context.BIND_AUTO_CREATE);

        Log.i(TAG, "log test");
        connect = (Button) findViewById(R.id.connect);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Clicked connect");
                metaWearBoard.connect();
            }
        });



    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getApplicationContext().unbindService(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        serviceBinder = (MetaWearBleService.LocalBinder) service;
        retrieveBoard();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    private final ConnectionStateHandler stateHandler = new ConnectionStateHandler() {
        @Override
        public void connected() {
            //super.connected();
            Log.i(TAG, "Connected");
            try {
                ledModule = metaWearBoard.getModule(Led.class);
            } catch (UnsupportedModuleException e) {
                e.printStackTrace();
            }

            led_on = (Button) findViewById(R.id.led_on);
            led_on.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "Turn on LED");
                    ledModule.configureColorChannel(Led.ColorChannel.BLUE).setRiseTime((short) 0)
                            .setPulseDuration((short) 1000).setRepeatCount((byte) -1).setHighTime(
                            (short) 500).setHighIntensity((byte) 16).setLowIntensity((byte) 16)
                            .commit();
                    ledModule.play(true);
                }
            });

            led_off = (Button) findViewById(R.id.led_off);
            led_off.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "Turn off LED");
                    ledModule.stop(true);
                }
            });
        }

        @Override
        public void disconnected() {
            super.disconnected();
            Log.i(TAG, "Connection Lost");
        }

        @Override
        public void failure(int status, Throwable error) {
            super.failure(status, error);
            Log.i(TAG, "Error Connection", error);
        }
    };


}
