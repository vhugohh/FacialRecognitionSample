package recognition.robot;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import recognition.sample.R;

import java.util.ArrayList;
import java.util.List;

public class BT_Main extends Activity {
    private static final int ENABLE_BT_REQ = 10000;
    

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> pairedDevicesAdapter;
    private ListView pairedDevices;
    private ProgressBar progress;
    private ListView availableDevices;
    private ArrayAdapter<String> availableDevicesAdapter;
    private List<BluetoothDevice> paired;
    private List<BluetoothDevice> available;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.bluetooth_main);

	pairedDevices = (ListView) findViewById(R.id.pairedDevices);
	pairedDevices.setOnItemClickListener(new OnItemClickListener() {
	    @Override
	    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		final BluetoothDevice device = ((BluetoothDevice) paired.toArray()[arg2]);
		Intent intent = new Intent(BT_Main.this, BT_Controls.class);
		intent.putExtra("device", device);
		startActivity(intent);
	    }
	});
	pairedDevicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
	pairedDevices.setAdapter(pairedDevicesAdapter);

	availableDevices = (ListView) findViewById(R.id.availableDevices);
	availableDevices.setOnItemClickListener(new OnItemClickListener() {
	    @Override
	    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		final BluetoothDevice device = ((BluetoothDevice) available.toArray()[arg2]);
		Intent intent = new Intent(BT_Main.this, BT_Controls.class);
		intent.putExtra("device", device);
		startActivity(intent);

	    }
	});

	availableDevicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
	availableDevices.setAdapter(availableDevicesAdapter);

	progress = (ProgressBar) findViewById(R.id.progress);
	bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	if (bluetoothAdapter == null) {
	    Toast.makeText(this, "WTF! No bluetooth device at all", Toast.LENGTH_SHORT).show();
	    return;
	}

	if (!bluetoothAdapter.isEnabled()) {
	    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	    startActivityForResult(enableBtIntent, ENABLE_BT_REQ);
	} else {
	    discoverDevices();
	}
    }

    

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	if (requestCode == ENABLE_BT_REQ) {
	    if (resultCode == RESULT_OK) {
		discoverDevices();
	    }
	}
    }

    private void discoverDevices() {
	// Register the BroadcastReceiver
	IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
	filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
	filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
	registerReceiver(mReceiver, filter);

	paired = new ArrayList<BluetoothDevice>(bluetoothAdapter.getBondedDevices());
	for (BluetoothDevice device : paired) {
	    pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
	}

	((BaseAdapter) pairedDevices.getAdapter()).notifyDataSetChanged();

	bluetoothAdapter.startDiscovery();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuItem add = menu.add("Scan");
	add.setOnMenuItemClickListener(new OnMenuItemClickListener() {

	    @Override
	    public boolean onMenuItemClick(MenuItem item) {
		bluetoothAdapter.startDiscovery();
		return false;
	    }
	});
	return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
	if (mReceiver != null) {
	    unregisterReceiver(mReceiver);
	}

	super.onDestroy();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	public void onReceive(Context context, Intent intent) {
	    String action = intent.getAction();
	    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
		BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

		if (available == null) {
		    available = new ArrayList<BluetoothDevice>();
		}

		if (!available.contains(device)) {
		    availableDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
		    availableDevicesAdapter.notifyDataSetChanged();
		}

		available.add(device);
	    } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
		progress.setVisibility(View.VISIBLE);
	    } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
		progress.setVisibility(View.INVISIBLE);
	    }
	}
    };

}