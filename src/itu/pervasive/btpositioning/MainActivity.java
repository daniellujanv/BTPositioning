package itu.pervasive.btpositioning;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity {

	private BluetoothAdapter mBluetoothAdapter;
	private int REQUEST_ENABLE_BT = 1;

	private TextView tv;
	private BtPositioning btp;
	private List<String> devices;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		devices = new ArrayList<String>();
		devices.add("54:26:96:D0:84:6E"); //daniel's macbook
		tv = (TextView) findViewById(R.id.text);
		tv.append("\n Searching for bt devices \n");

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			tv.append("initializeBt -> adapter null");
			// Device does not support Bluetooth
		}else{
			if (!mBluetoothAdapter.isEnabled()) {
				tv.append("initializeBt -> bt not enabled");
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}else{
//				while(true){
					discoverDevices();
//				}
			}
		}	
		tv.append("\n finished \n");
	}

	private void discoverDevices() {
		// TODO Auto-generated method stub
		Log.w("discoverDevices","register");
		mBluetoothAdapter.startDiscovery();
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.add(0, 2, 0,"Repeat");
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case 2:
	        	unregisterReceiver(mReceiver);
	    		mBluetoothAdapter.cancelDiscovery();
	    		discoverDevices();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == REQUEST_ENABLE_BT){
			if(resultCode == RESULT_OK){
				// Register the BroadcastReceiver
				discoverDevices();
			}else {
				tv.append("result NOT_OK - request_enable_bt \n");
			}
		}
	}
	
	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    private List<String> mArrayAdapter = new ArrayList<String>();
	    @Override		
	    public void onReceive(Context context, Intent intent) {
    		Log.w("broadcastReceiver","received");
	        String action = intent.getAction();
	        // When discovery finds a device
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	            // Get the BluetoothDevice object from the Intent
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	        	if(devices.contains(device.getAddress()) ){
		            short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short)0);
		            // Add the name and address to an array adapter to show in a ListView
		            double distance = BtPositioning.getDistance((double)rssi);
		            mArrayAdapter.add(device.getName() + " rssi: "+ rssi + " -- dstnc: "+ String.format("%.6f",distance));
		            tv.append(mArrayAdapter.get(mArrayAdapter.size()-1).toString()+"\n");	
		    		unregisterReceiver(mReceiver);
		    		Log.w("broadcastReceiver","UNregister");
		    		mBluetoothAdapter.cancelDiscovery();
		    		discoverDevices();
	        	}
//	        	else{
//	        		tv.append("stranger - "+device.getAddress()+"\n");
//	        	}
	        }
	    }
	};

	public void onDestroy(){
		super.onDestroy();
		Log.w("destroy","destroooy");
		unregisterReceiver(mReceiver);
	}
}
