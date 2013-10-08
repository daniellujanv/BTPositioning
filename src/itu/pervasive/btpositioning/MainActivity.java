package itu.pervasive.btpositioning;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
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
	private boolean goRead = true;
	protected BtPositioning btPositioning;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		devices = new ArrayList<String>();
		//		devices.add("54:26:96:D0:84:6E"); //daniel's macbook
		devices.add("08:37:3D:C7:2B:FD"); //galaxy_fame_758
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
				new searchingManager().execute(null, null, null);
			}
		}	
	}

	private void discoverDevices() {
		// TODO Auto-generated method stub
		Log.w("discoverDevices","register");
		mBluetoothAdapter.startDiscovery();
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
	}

	private void undiscoverDevices(){
		unregisterReceiver(mReceiver);
		Log.w("broadcastReceiver","UNregister");
		mBluetoothAdapter.cancelDiscovery();
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
					double distance = btPositioning.addReading((double)rssi);
					mArrayAdapter.add(device.getName() + " rssi: "+ rssi + " -- dstnc: "+ String.format("%.6f",distance));
					tv.append(device.getName() + " rssi: "+ rssi + " -- dstnc: "+ String.format("%.6f",distance)+"\n");
					// Add the name and address to an array adapter to show in a ListView
					undiscoverDevices();
					if(btPositioning.readings.size() <= 5){
						discoverDevices();
					}else{
						btPositioning.calculateDistances();
						tv.append(btPositioning.getCalculatedDistances() + "\n*******EOR******\n");
						goRead = false;
					}

				}
			}
		}
	};

	private class searchingManager extends AsyncTask<Void, Void, Void> {
		protected Void doInBackground(Void... _void) {
			Looper.prepare();
			for(int i=0; i< 4; i++){
				btPositioning = new BtPositioning();
				discoverDevices();
				while(goRead){
					try {
						Thread.sleep(3000);
						if(goRead && btPositioning.readings.size() <= 5){//if goRead is true it means the readings.size() is still less than 6 and possibly frozen 
							undiscoverDevices();
							discoverDevices();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				goRead = true;
			}
//			undiscoverDevices();
			return null;
		}
	}


	public void onDestroy(){
		super.onDestroy();
		Log.w("destroy","destroooy");
		unregisterReceiver(mReceiver);
	}
}
