package xavier.btkom;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.bluetooth.BluetoothDevice.BOND_BONDING;
import static android.bluetooth.BluetoothDevice.BOND_NONE;
import static android.widget.Toast.LENGTH_SHORT;


public class MainActivity extends AppCompatActivity {

    private BTKOM btkom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    protected void addLog(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView text = (TextView)findViewById(R.id.logs);
                text.setText(text.getText() + "\n" + str);
            }
        });
    }

    protected void end(final String str) {
        Log.i("log", "end: "+str);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), str, Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.connect).setClickable(true);
    }

    public void connect(View view) throws ExecutionException, InterruptedException {
        findViewById(R.id.connect).setClickable(false);
        TextView text = (TextView) findViewById(R.id.input);
        final String input = text.getText().toString();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<BTKOM> future = executor.submit(new Callable<BTKOM>(){

            int isPaired = -1;
            BluetoothDevice device;

            public BTKOM call() {

                if(!BluetoothAdapter.checkBluetoothAddress(input)){ end("Error : Invalid address"); return null; }

                BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
                if (bluetooth != null) {
                    if (!bluetooth.isEnabled()) {
                        boolean enable = bluetooth.enable();
                        if (!enable) {
                            end("Error : Cannot activate Bluetooth");
                            return null;
                        }
                    }

                    if(!BluetoothAdapter.checkBluetoothAddress(input)){
                        end("Error : Invalid bluetooth address");
                        return null;
                    }
                    device = bluetooth.getRemoteDevice(input);
                    Set<BluetoothDevice> devices = bluetooth.getBondedDevices();
                    // On regarde si l'appareil est déjà appairé
                    for (BluetoothDevice dev : devices) {
                        if (input.equals(dev.getAddress())) isPaired = 1;
                    }

                    IntentFilter intFilter = new IntentFilter();
                    intFilter.addAction(BluetoothDevice.ACTION_FOUND);
                    intFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                    intFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                    intFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                    registerReceiver(mReceiver, intFilter);

                    // Si on ne le trouve pas, on active la recherche bluetooth
                    if (isPaired != 1) {
                        if (bluetooth.isDiscovering()) bluetooth.cancelDiscovery();
                        if (!bluetooth.startDiscovery()) {
                            end("Error : Cannot start bluetooth discovery");
                            return null;
                        }
                    }else{
                        return connect();
                    }

                } else {
                    end("Error : Your device hasn't Bluetooth");
                }
                return null;
            }

            private boolean paired(Set<BluetoothDevice> devices){
                for (BluetoothDevice dev : devices) {
                    if (input.equals(dev.getAddress())) {
                        if (!dev.createBond()) {
                            end("Error : Cannot pair device");
                            return false;
                        }
                        while (isPaired == -1) {}
                        if (isPaired == 0) {
                            end("Error : Couldn't find device " + input);
                            return false;
                        }
                        device = dev;
                    }
                }
                return isPaired == 1;
            }

            private BTKOM connect(){
                BTKOM btkom = null;
                // Si on arrive ici, alors on est forcément apairé. Il faut alors connecter les appareils
                Log.e("ok", "connecting");
                try {
                    BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
                    try {
                        btkom = new BTKOM(socket);
                        addLog("Successfully connected to " + input);
                        end("Connected");
                    }catch(IllegalArgumentException e){
                        addLog(e.getMessage());
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }
                return btkom;
            }

            private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
                private Set<BluetoothDevice> devices;
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        Log.e("ok","adding");
                        devices.add((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
                    }else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                        devices = new HashSet<BluetoothDevice>();
                    }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                        Log.e("ok", "ending");
                        if(paired(devices)){
                            connect();
                        }else{
                            end("Failed to find device");
                        }
                    }else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                        if (intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1) == BOND_BONDED && intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1) == BOND_BONDING) {
                            isPaired = 1;
                        } else if (intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1) == BOND_NONE && intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1) == BOND_BONDING) {
                            isPaired = 0;
                        }
                    }
                }
            };
        });

        BTKOM bluetooth = future.get();
        if(bluetooth != null){
            Log.i("log","Socket well established !");
        }
        else {
            Log.i("log", "Error creating socket");
        }

    }
}
