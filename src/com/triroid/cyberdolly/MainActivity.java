package com.triroid.cyberdolly;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements Runnable, OnClickListener {
	private static final String TAG = "BluetoothController";

	private BluetoothAdapter mAdapter;
	private BluetoothDevice mDevice;
	private final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private final String DEVICE_NAME = "HC-06";
	private BluetoothSocket mSocket;
	private Thread mThread;
	private boolean isRunning;
	private Button connectButton, forwardButton, leftButton, rightButton,
			backButton, stopButton, rightTurnButton, leftTurnButton,
			rightFowardButton, leftFowardButton;
	private Context mContext;
	private String operation = "stop";
	private TextView x_axis, y_axis, z_axis;
    private final Handler mHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this.getApplicationContext();

		connectButton = (Button) findViewById(R.id.Connect);
		connectButton.setOnClickListener(this);
		forwardButton = (Button) findViewById(R.id.Foward);
		forwardButton.setOnClickListener(this);
		leftButton = (Button) findViewById(R.id.Left);
		leftButton.setOnClickListener(this);
		rightButton = (Button) findViewById(R.id.Right);
		rightButton.setOnClickListener(this);
		backButton = (Button) findViewById(R.id.Back);
		backButton.setOnClickListener(this);
		stopButton = (Button) findViewById(R.id.Stop);
		stopButton.setOnClickListener(this);
		leftTurnButton = (Button) findViewById(R.id.LeftTurn);
		leftTurnButton.setOnClickListener(this);
		rightTurnButton = (Button) findViewById(R.id.RightTurn);
		rightTurnButton.setOnClickListener(this);
		leftFowardButton = (Button) findViewById(R.id.LeftFoward);
		leftFowardButton.setOnClickListener(this);
		rightFowardButton = (Button) findViewById(R.id.RightFoward);
		rightFowardButton.setOnClickListener(this);
		
		x_axis = (TextView) findViewById(R.id.x_axis);
		y_axis = (TextView) findViewById(R.id.y_axis);
		z_axis = (TextView) findViewById(R.id.z_axis);		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		isRunning = false;
		try {
			mSocket.close();
		} catch (Exception e) {
		}
	}
	
	Double pi=3.141516;
	String bufferString = "";
	
	@Override
	public void run() {
		InputStream mmInStream = null;
		OutputStream mmOutputStream = null;
		try {
			// 取得したデバイス名を使ってBluetoothでSocket接続
			mSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
			mSocket.connect();
			mmInStream = mSocket.getInputStream();
			mmOutputStream = mSocket.getOutputStream();

			// InputStreamのバッファを格納
			byte[] buffer = new byte[1024];
			// 取得したバッファのサイズを格納
			int bytes;
			
			float[] axis = {0,0,0};

			while (isRunning) {
				// InputStreamの読み込み　
				bytes = mmInStream.read(buffer);

				// String型に変換
				String readMsg = new String(buffer, 0, bytes);
				bufferString = bufferString + readMsg;

				String[] axisString = bufferString.split("\r\n");
				if(axisString.length > 1) {					
					final String[] xyzString = axisString[0].split("\t");
					if(xyzString[0].equals("ypr")) {
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								x_axis.setText(xyzString[1]);
								y_axis.setText(xyzString[2]);
								z_axis.setText(xyzString[3]);															
							}
						});
						
//						axis[0] = Float.parseFloat(xyzString[1]);
//						axis[1] = Float.parseFloat(xyzString[2]);
//						axis[2] = Float.parseFloat(xyzString[3]);					
					}					
					bufferString = axisString[1];
				}

				// Stopの場合
				if (operation.equals("stop")) {
					String commandId = "0,0,0,0#";
					mmOutputStream.write(commandId.getBytes());
					operation="";
				}
				// Forwardの場合
				else if (operation.equals("forward")) {
					float degree = 45 - axis[0];
					String commandId = deg2Str(degree);
					mmOutputStream.write(commandId.getBytes());
					operation="";
				} else if (operation.equals("left")) {
					float degree = -45 - axis[0];
					String commandId = deg2Str(degree);
					mmOutputStream.write(commandId.getBytes());
					operation="";
				} else if (operation.equals("right")) {
					float degree = 135 - axis[0];
					String commandId = deg2Str(degree);
					mmOutputStream.write(commandId.getBytes());
					operation="";
				} else if (operation.equals("back")) {
					float degree = -135 - axis[0];
					String commandId = deg2Str(degree);
					mmOutputStream.write(commandId.getBytes());
					operation="";
				} else if (operation.equals("leftfoward")) {
					float degree = -22 - axis[0];
					String commandId = deg2Str(degree);
					mmOutputStream.write(commandId.getBytes());
					operation="";
				} else if (operation.equals("rightfoward")) {
					float degree = 112 - axis[0];
					String commandId = deg2Str(degree);
					mmOutputStream.write(commandId.getBytes());
					operation="";
				} else if (operation.equals("leftturn")) {
					String commandId = "-100,-100,-100,-100#";
					mmOutputStream.write(commandId.getBytes());
					operation="";
				} else if (operation.equals("rightturn")) {
					String commandId = "100,100,100,100#";
					mmOutputStream.write(commandId.getBytes());
					operation="";
				}

				// null以外なら表示
				
				if (readMsg.trim() != null && !readMsg.trim().equals("")) {
					Log.i(TAG, "value=" + readMsg.trim());
				} else {
					Log.i(TAG, "value=nodata");
				}
			}
			
			
		} catch (Exception e) {
			Log.e(TAG, "error:" + e);
			try {
				mSocket.close();
			} catch (Exception ee) {
			}
			isRunning = false;
		}
	}
	
	private final int MAX_SPEED = 100;

	private String deg2Str(float degree) {
		double rad = degree / 180 * pi;
		int m1 = (int) ( Math.sin(rad) * MAX_SPEED );
		int m2 = (int) ( Math.cos(rad) * -1 * MAX_SPEED );
		int m3 = (int) ( Math.sin(rad) * -1 * MAX_SPEED );
		int m4 = (int) ( Math.cos(rad) * MAX_SPEED );
		return String.format("%d,%d,%d,%d#", m1, m2, m3, m4);
	}

	@Override
	public void onClick(View v) {
		if (v.equals(connectButton)) {
			mAdapter = BluetoothAdapter.getDefaultAdapter();
			Set<BluetoothDevice> devices = mAdapter.getBondedDevices();
			for (BluetoothDevice device : devices) {
				Log.i(TAG, "DEVICE:" + device.getName());
				if (device.getName().equals(DEVICE_NAME)) {
					mDevice = device;
					Toast.makeText(mContext, "デバイス名:" + device.getName(),
							Toast.LENGTH_LONG).show();
				}
			}

			// Threadを起動し、Bluetooth接続
			mThread = new Thread(this);
			isRunning = true;
			mThread.start();
		} else if (v.equals(forwardButton)) {
			operation = "forward";
		} else if (v.equals(leftButton)) {
			operation = "left";
		} else if (v.equals(rightButton)) {
			operation = "right";
		} else if (v.equals(backButton)) {
			operation = "back";
		} else if (v.equals(stopButton)) {
			operation = "stop";
		} else if (v.equals(leftTurnButton)) {
			operation = "leftturn";
		} else if (v.equals(rightTurnButton)) {
			operation = "rightturn";
		} else if (v.equals(leftFowardButton)) {
			operation = "leftfoward";
		} else if (v.equals(rightFowardButton)) {
			operation = "rightfoward";
		}
	}
	
    private final Handler handler = new Handler() {
        @Override
		public void handleMessage(Message msg) {
        }
    };
}
