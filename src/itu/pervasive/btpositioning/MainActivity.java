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
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private BluetoothAdapter mBluetoothAdapter;
	private int REQUEST_ENABLE_BT = 1;

	private TextView tv;
	private ScrollView sv;
	private List<String> devices;
	private boolean goRead = true;
	//	protected BtPositioning btPositioning;
	private BtPositioning btPositioningAll;
	private SearchingManager searchManager;
	private ProgressBar wholeProgress;
	private ProgressBar individualProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		devices = new ArrayList<String>();
		devices.add("08:37:3D:C7:2B:FD"); //galaxy_fame_758
		//find views
		tv = (TextView) findViewById(R.id.text);
		sv = (ScrollView) findViewById(R.id.scrollview);
		wholeProgress = (ProgressBar) findViewById(R.id.wholeprogressbar);
		individualProgress = (ProgressBar) findViewById(R.id.individualprogressbar);

		tv.setText("\n Searching for bt devices \n");
		//initialize BT adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		makeToast("starting");

		//manager for searching devices
		searchManager = new SearchingManager(); 
		if (mBluetoothAdapter == null) { // Device does not support Bluetooth
			appendScreen("Device does not support Bluetooth");
		}else if (!mBluetoothAdapter.isEnabled()) {//bluetooth is not enabled
			appendScreen("initializing BT");
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}else{
			//everything good, search for devices
			searchManager.execute(null, null, null); 
		}
	}


	/*
	 * start discovery of devices
	 * initialize intent for ACTION_FOUND
	 * register receiver for intent
	 * 
	 */
	private void discoverDevices() {
		//		Log.w("discoverDevices","register");
		mBluetoothAdapter.startDiscovery();
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter);
	}

	/*
	 * stop discovering devices
	 * unregister receiver for intent ACTION_FOUND
	 * cancelDiscovery on bluetoothAdapter
	 * 
	 */
	private void undiscoverDevices(){
		//		Log.w("broadcastReceiver","UNregister");
		try{
			unregisterReceiver(mReceiver);
		}catch(Exception e){}
		mBluetoothAdapter.cancelDiscovery();
	}

	/*
	 * 
	 * simple method to display something on screen with TOAST
	 */
	private void makeToast(String toPrint) {
		Toast.makeText(getApplicationContext(), toPrint, Toast.LENGTH_SHORT).show();
	}

	/*
	 * beep sound 
	 */
	private void beep() {
		try {
			Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
			r.play();
		} catch (Exception e) {}
	}

	/*
	 * 
	 * append text to TV in screen - to be deleted
	 */
	private void appendScreen(String text){
		tv.append(text);
		sv.fullScroll(View.FOCUS_DOWN);
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 * create menu options
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.add(0, 2, 0,"Repeat Search!");
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 * options in menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case 2:
			searchManager.execute(null, null, null); 
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 * destroy method, unregister everything if needed
	 * disable bluetooth adapter
	 * cancel asynctask searchManager
	 */
	public void onDestroy(){
		super.onDestroy();
		try{
			Log.w("destroy","destroooy");
			searchManager.cancel(true);
			unregisterReceiver(mReceiver);
			mBluetoothAdapter.cancelDiscovery();
			mBluetoothAdapter.disable();			
		}catch(Exception e){
			Log.w("onDestroy", e.toString());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 * Intent for enabling Bluetooth
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == REQUEST_ENABLE_BT){
			if(resultCode == RESULT_OK){
				// Register the BroadcastReceiver
				searchManager.execute(null, null, null);
			}else {
				appendScreen("result NOT_OK - request_enable_bt \n");
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
	}

	/*
	 *  Create a BroadcastReceiver for ACTION_FOUND
	 *  when device is found it is processed if it is in the list
	 *  of known devices - NOT ON PAIRED DEVICES
	 */
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override		
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if(devices.contains(device.getAddress()) ){
					//					Log.w("broadcastReceiver","received");
					short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short)0);
					btPositioningAll.addReading((double)rssi);
					//					Log.w("device",device.getName() + " rssi: "+ rssi + " -- dstnc: "+ String.format("%.6f",distance)+"\n");
					undiscoverDevices();
					if((btPositioningAll.readings.size() % 6) != 0){ //taking 6 readings on each side
						discoverDevices();
						goRead = true;
					}else{
						//						btPositioning.calculateDistances();//calculating distances & applying mean filter for every 8 readings
						//						appendScreen(btPositioning.getCalculatedDistances() + "\n*************\n");
						goRead = false;
					}

				}
			}
		}
	};

	/*
	 * Manager fo Bluetooth devices search 
	 * puts thread to sleep 3 seconds after reading 6 RSSIs
	 */
	private class SearchingManager extends AsyncTask<Void, Integer, Integer> {
		private long initialTime;
		private long endingTime;
		protected Integer doInBackground(Void... _void) {
			//			Log.w("ready", "searchingManagerStarted");
			Looper.prepare();
			initialTime = System.currentTimeMillis();
			try {
				btPositioningAll = new BtPositioning();
				for(int i=0; i< 8; i++){
					if(i != 0){
						makeToast("Turn 45 degrees to the right");
					}
					publishProgress(i+1);
					Log.w("run", ""+(i+1));
					Thread.sleep(300);
					//					btPositioning = new BtPositioning();
					discoverDevices();
					while(goRead){
						Thread.sleep(5000);
						if(goRead && btPositioningAll.readings.size() < 48){//if goRead is true it means the readings.size() is still less than 6 and possibly frozen 
							try{
								undiscoverDevices();								
							}catch(Exception e){}
							discoverDevices();
						}
						int wholeprogress = ((i*100)/8);
						int individualprogress = (int)((((double)btPositioningAll.readings.size()%6)/6)*100);
						publishProgress(-1, wholeprogress,individualprogress);
					}
					goRead = true;
				}
				//				btPositioningAll.calculateDistances();//calculating distances & applying mean filter for all the readings
				//				btPositioningAll.calculateOrientation();
				publishProgress(1000);
				endingTime = System.currentTimeMillis();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		protected void onProgressUpdate(Integer... progress){
			if(progress[0] < 0){
				wholeProgress.setProgress(progress[1]);
				individualProgress.setProgress(progress[2]);
				Log.w("progress", "ind:"+ progress[2] + " whole:"+ progress[1]);
			}else if(progress[0] == 1000){				
				wholeProgress.setProgress(100);
				beep();
				beep();
			}else {
				appendScreen("\n RUN -> #"+progress[0]+"\n");
				beep();
			}

		}


		protected void onPostExecute(final Integer result){

			btPositioningAll.calculateDistances();//calculating distances & applying mean filter for all the readings
			btPositioningAll.calculateOrientation();
			appendScreen(btPositioningAll.getCalculatedDistances() + "\n******DONE*******\n");
			appendScreen("time of run(mins): "+(endingTime-initialTime)/60000);//minutes
			Log.w("runningTime","time of run(mins): "+(endingTime-initialTime)/60000);//minutes

			//		device.setDistance(btPositioningAll.getCalculatedDistance());
			//		device.setIOrientation(btPositioningAll.getOrientation());
			Log.w("onPostExecute", "finished 8 readings");
			//		toast(btPositioningAll.getCalculatedDistances() + "\n "
			//				+ btPositioningAll.getOrientation() +" \n******DONE*******\n",true, false);

		}
	}
}


/*
 *we always start heading north
 * 
 * 				N
 * 				.
 * 			wn	.	ne
 * 				.
 *		W	.	.	.	E
 * 				.
 * 			sw	.	se
 * 				.
 * 				S
 * 
 * 
 * 
 */
