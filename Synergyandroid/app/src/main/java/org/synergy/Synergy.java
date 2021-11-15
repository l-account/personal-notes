/*
 * synergy -- mouse and keyboard sharing utility
 * Copyright (C) 2010 Shaun Patterson
 * Copyright (C) 2010 The Synergy Project
 * Copyright (C) 2009 The Synergy+ Project
 * Copyright (C) 2002 Chris Schoeneman
 * 
 * This package is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * found in the file COPYING that should have accompanied this file.
 * 
 * This package is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.synergy;

import org.synergy.base.Event;
import org.synergy.base.EventQueue;
import org.synergy.base.EventType;
import org.synergy.base.Log;
import org.synergy.client.Client;
import org.synergy.common.screens.BasicScreen;
//import org.synergy.common.screens.PlatformIndependentScreen;
import org.synergy.injection.Injection;
import org.synergy.net.NetworkAddress;
import org.synergy.net.SocketFactoryInterface;
//import org.synergy.net.SynergyConnectTask;
import org.synergy.net.TCPSocketFactory;
import org.synergy.server.RunServer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Synergy extends Activity {
	
	private final static String PROP_clientName = "clientName";
	private final static String PROP_serverHost = "serverHost";
	private final static String PROP_deviceName = "deviceName";
	
	private Thread mainLoopThread = null;
	
	static {
		System.loadLibrary ("synergy-jni");

	}
	
	private class MainLoopThread extends Thread {
		
		public void run () {
			try {
		        Event event = new Event ();
		        event = EventQueue.getInstance ().getEvent (event, -1.0);
		        Log.note ("Event grabbed");
		        while (event.getType () != EventType.QUIT && mainLoopThread == Thread.currentThread()) {
		            EventQueue.getInstance ().dispatchEvent (event);
		            // TODO event.deleteData ();
		            event = EventQueue.getInstance ().getEvent (event, -1.0);
		            Log.note ("Event grabbed");
		        } 
				mainLoopThread = null;
			} catch (Exception e) {
				e.printStackTrace ();
			} finally {
				Injection.stop();
			}
		}
	}
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
       
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        String clientName = preferences.getString(PROP_clientName, null);
        if (clientName != null) {
        	((EditText) findViewById (R.id.clientNameEditText)).setText(clientName);
        }
    	String serverHost = preferences.getString(PROP_serverHost, null);
        if (serverHost != null) {
        	((EditText) findViewById (R.id.serverHostEditText)).setText(serverHost);
        }
		Log.setLogLevel (Log.Level.DEBUG2);


		final Button connectButton =  findViewById (R.id.connectButton);
		final Button asServer=findViewById(R.id.RunAsServer);

        //for client
		connectButton.setOnClickListener (new View.OnClickListener() {
			public void onClick (View arg) {
				asServer.setEnabled(false);
				Log.debug ("Client starting....");
				connect ();
			}
		});
		



		// for server
        asServer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				connectButton.setEnabled(false);
				runserver();
			}
		});



        try {
			Injection.setPermissionsForInputDevice();
		} catch (Exception e) {
        	e.printStackTrace();
			// TODO handle exception
		}
    }

    public boolean onTouchEvent(MotionEvent motionEvent){
    	float pos_x=motionEvent.getX();
    	float pos_y=motionEvent.getY();
    	try {
    		switch (motionEvent.getAction()){
				case MotionEvent.ACTION_UP:
					Log.debug("action up,x= "+pos_x+",y="+pos_y);
					break;
				case MotionEvent.ACTION_DOWN:
					Log.debug("action down,x= "+pos_x+",y="+pos_y);
					break;
				case MotionEvent.ACTION_MOVE:
					Log.debug("action move,x= "+pos_x+",y="+pos_y);
					break;
			}
			return true;
		}catch (Exception e){
    		return false;
		}
	}

    private void connect () {
    	
    	String clientName = ((EditText) findViewById (R.id.clientNameEditText)).getText().toString();
    	String ipAddress = ((EditText) findViewById (R.id.serverHostEditText)).getText().toString();
    	String portStr = ((EditText) findViewById(R.id.serverPortEditText)).getText().toString();
    	int port = Integer.parseInt(portStr);
    	String deviceName = ((EditText) findViewById(R.id.inputDeviceEditText)).getText().toString();
    	
    	SharedPreferences preferences = getPreferences(MODE_PRIVATE);
    	SharedPreferences.Editor preferencesEditor = preferences.edit();
    	preferencesEditor.putString(PROP_clientName, clientName);
    	preferencesEditor.putString(PROP_serverHost, ipAddress);
    	preferencesEditor.putString(PROP_deviceName, deviceName);
    	preferencesEditor.apply();
    	
        try {
        	SocketFactoryInterface socketFactory = new TCPSocketFactory();
       	   	NetworkAddress serverAddress = new NetworkAddress (ipAddress, port);

        	Injection.startInjection(deviceName);

        	BasicScreen basicScreen = new BasicScreen(false);
			DisplayMetrics displayMetrics=new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        	basicScreen.setShape(displayMetrics.widthPixels,displayMetrics.heightPixels);
			Log.debug("basicreen size:  width"+displayMetrics.widthPixels+" height "+displayMetrics.heightPixels);
        	
        	//PlatformIndependentScreen screen = new PlatformIndependentScreen(basicScreen);
            
            Log.debug ("Hostname: " + clientName);

			Client client = new Client (getApplicationContext(), clientName, serverAddress, socketFactory,
					null, basicScreen);
			new Thread(()->
				client.connect()
			).start();


			if (mainLoopThread == null) {
				mainLoopThread = new MainLoopThread();
				mainLoopThread.start();
			}
			
        } catch (Exception e) {
        	e.printStackTrace();
        	//((EditText) findViewById (R.id.outputEditText)).setText("Connection Failed.");
        }
    }

    private void runserver(){
		String serverName = ((EditText) findViewById (R.id.clientNameEditText)).getText().toString();
		String ipAddress = ((EditText) findViewById (R.id.serverHostEditText)).getText().toString();
		String portStr = ((EditText) findViewById(R.id.serverPortEditText)).getText().toString();
		int port = Integer.parseInt(portStr);
		String deviceName = ((EditText) findViewById(R.id.inputDeviceEditText)).getText().toString();

		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor preferencesEditor = preferences.edit();
		preferencesEditor.putString(PROP_clientName, serverName);
		preferencesEditor.putString(PROP_serverHost, ipAddress);
		preferencesEditor.putString(PROP_deviceName, deviceName);
		preferencesEditor.apply();
		try {
			SocketFactoryInterface socketFactory = new TCPSocketFactory();
			NetworkAddress serverAddress = new NetworkAddress (ipAddress, port);

			BasicScreen serverScreen = new BasicScreen(true);
			DisplayMetrics displayMetrics=new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
			serverScreen.setShape(displayMetrics.widthPixels,displayMetrics.heightPixels);

			//Toast.makeText(getApplicationContext(),"serverscreen size:  width"+displayMetrics.widthPixels+
			//		" height "+displayMetrics.heightPixels,Toast.LENGTH_LONG).show();
			Log.debug("server name: "+serverName+"，serverscreen size:  width--"+displayMetrics.widthPixels+
					"，height--"+displayMetrics.heightPixels);
			RunServer runServer=new RunServer(getApplicationContext(), serverName, serverAddress, socketFactory,
					null, serverScreen);

			new Thread(()->
					runServer.ServerInit()
			).start();

			if (mainLoopThread == null) {
				mainLoopThread = new MainLoopThread();
				mainLoopThread.start();
			}
		}catch (Exception e){
			e.printStackTrace();
			//Toast.makeText(getApplicationContext(),"runserver failed",Toast.LENGTH_LONG).show();
		}

	}
}
