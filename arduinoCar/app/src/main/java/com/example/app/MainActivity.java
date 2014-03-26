package com.example.app;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

@SuppressLint("NewApi")
@TargetApi(Build.VERSION_CODES.ECLAIR)
public class MainActivity extends Activity implements OnClickListener , OnSeekBarChangeListener {
    private BluetoothAdapter bluetoothAdapter = null;

    //  private BluetoothConnection connection = null;
    private BluetoothConnection.ConnectionFuture connectionFuture = null;

    private Button transmitButton , btn_adelante , btn_atras , btn_izquierda , btn_derecha , btn_stop ;
    private TextView displayedTextBox;
    private EditText transmitTextBox ;

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private void debug(String text) {
//    Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        Log.i("BlueDuino", text);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Indicate scanning in the title
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_main );
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ;

        // If the adapter is null, then Bluetooth is not supported
        if (bluetoothAdapter == null) {
            debug("Bluetooth is not available");
            finish();
            return;
        }

        displayedTextBox = (TextView) findViewById(R.id.recieved_text);

    /* importaciones */


        btn_adelante = (Button) findViewById( R.id.btn_adelante );
        btn_adelante.setOnClickListener( (OnClickListener) this );

        btn_atras = (Button) findViewById( R.id.btn_atras );
        btn_atras.setOnClickListener( (OnClickListener) this );

        btn_izquierda = (Button) findViewById( R.id.btn_izquiera );
        btn_izquierda.setOnClickListener( (OnClickListener) this );

        btn_derecha = (Button) findViewById( R.id.btn_derecha );
        btn_derecha.setOnClickListener( (OnClickListener) this );

        btn_stop = (Button) findViewById( R.id.btn_stop );
        btn_stop.setOnClickListener( (OnClickListener) this );



    /*-------------------------------*/

        displayedTextBox.setMovementMethod(new ScrollingMovementMethod());

        transmitTextBox = (EditText) findViewById( R.id.transmit_text );
        transmitButton = (Button) findViewById( R.id.button_transmit );
        transmitButton.setEnabled(false);
        transmitButton.setText("Conectando...");
        transmitButton.setOnClickListener( this );
    }

    public void onStart() {
        super.onStart();

        if ( !bluetoothAdapter.isEnabled() ) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        debug("result!");
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                onSelectDeviceActivityResult(resultCode, data);
                break;
            case REQUEST_ENABLE_BT:
                onEnableBluetoothActivityResult(resultCode, data);
                break;
        }
        // Indicate scanning in the title
    }

    private void onEnableBluetoothActivityResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            // do something interesting?
        } else {
            debug("Setting up bluetooth failed.");
            finish();
        }
    }

    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    private void onSelectDeviceActivityResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String address = data.getExtras()
                    .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
//      debug("GRD");
            debug("extras");

//      debug("Connecting to: " + address);

            Log.i("BlueDuino", "Creating connection");
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            connectionFuture = new BluetoothConnection.ConnectionFuture(device, readHandler);
            if (connectionFuture.failed()) {
                debug("Connection failed");
            } else {
                final BluetoothConnection.ConnectionFuture localConnection = connectionFuture;
                final Button localButton = transmitButton;
                Log.i("BlueDuino", "Starting AsyncTask");
                new AsyncTask<Integer , Integer , Boolean >() {
                    public Boolean doInBackground(Integer... params) {
                        localConnection.block();
                        Log.i("BlueDuino", "done blocking for connection");
                        return localConnection.failed();
                    }

                    public void onPostExecute(Boolean failed) {
                        if (!failed) {
                            localButton.setEnabled(true);
                            transmitButton.setText("Enviar");
                        }
                    }
                }.execute();
//        connection = connectionFuture.get();

//        debug("Established connection to: " + address);
                // try {
                //   connection.write("+RR-".getBytes());
                //   debug("Writing message");
                // } catch (IOException e) {
                //   debug("Write failed.");
                // }
            }
        }
    }

    private final Handler readHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothConnection.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
//                mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
//                debug(readMessage);
                    // Indicate scanning in the title
                    TextView displayedTextBox = (TextView) findViewById(R.id.recieved_text);
                    displayedTextBox.append(readMessage);

                    final int scrollAmount = displayedTextBox.getLayout().getLineTop(
                            displayedTextBox.getLineCount()) - displayedTextBox.getHeight();

                    // if there is no need to scroll, scrollAmount will be <=0
                    if (scrollAmount > 0) {
                        displayedTextBox.scrollTo(0, scrollAmount);
                    }
// else {
//                  displayedTextBox.scrollTo(0,0);
//                }
            }
        }
    };

    @Override
    public void onClick(View v) {
	/*  botones de centreo */
        if ( v.getId() == R.id.btn_adelante  ) {
            try {
                connectionFuture.get().write(("a").getBytes());
                displayedTextBox.setText("adelante");
            } catch (IOException e) {
                debug("Write failed.");
            }
        }


        if ( v.getId() == R.id.btn_atras  ) {
            try {
                connectionFuture.get().write( ("b").getBytes() );
                displayedTextBox.setText( "atras"  );
            } catch (IOException e) {
                debug("Write failed.");
            }
        }
        if ( v.getId() == R.id.btn_izquiera  ) {
            try {
                connectionFuture.get().write( ("c").getBytes() );
                displayedTextBox.setText( "izquierda"  );
            } catch (IOException e) {
                debug("Write failed.");
            }
        }
        if ( v.getId() == R.id.btn_derecha  ) {
            try {
                connectionFuture.get().write( ("d").getBytes() );
                displayedTextBox.setText( "derecha"  );
            } catch (IOException e) {
                debug("Write failed.");
            }
        }
        if ( v.getId() == R.id.btn_stop  ) {
            try {
                connectionFuture.get().write( ("e").getBytes() );
                displayedTextBox.setText( "stop"  );
            } catch (IOException e) {
                debug("Write failed.");
            }
        }
         if ( v.getId() == R.id.button_transmit  ) {
            synchronized ( transmitTextBox ) {
                Editable transmitText = transmitTextBox.getText();
                String text = transmitText.toString();
                transmitText.clear();
                try {
                    // Disable and block until this is ready
                    connectionFuture.get().write(text.getBytes());
                    displayedTextBox.setText( "CMD : " + text );
                    debug("Wrote message: " + text);
                } catch (IOException e) {
                    debug("Write failed.");
                }
            }
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //sdfsdf
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    // @Override
    // public boolean onOptionsItemSelected(MenuItem item) {
    //       switch (item.getItemId()) {
    //       case R.id.scan:
    //           // Launch the DeviceListActivity to see devices and do scan
    //           Intent serverIntent = new Intent(this, DeviceListActivity.class);
    //           startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    //           return true;
    //       case R.id.discoverable:
    //           // Ensure this device is discoverable by others
    //           ensureDiscoverable();
    //           return true;
    //       }
    //       return false;
    //   }
}
