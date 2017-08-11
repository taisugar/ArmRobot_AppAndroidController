package com.tdt.js_v2;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener{
    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 1;

    private RelativeLayout layout_joystick1;
    private RelativeLayout layout_joystick2;
    private TextView textView1, textView2, textView3, textView4, textView5;
    private Button btnConnection, btnChat, btnInfo, btnX, btnY, btnA, btnB;
    private boolean autobtnA_Press = false;
    private boolean autobtnB_Press = false;
    private boolean autobtnX_Press = false;
    private boolean autobtnY_Press = false;
    private final long REPEAT_DELAY = 50;
    private Handler repeatUpdateHandler = new Handler();

    private JoyStickClass js1;
    private JoyStickClass js2;

    private BluetoothAdapter mBluetoothAdapter;
    private String textRecieve;
    private Dialog dialogConnection;
    private Button dialogBtnScan;
    private ListView lvNewDevice;
    private ArrayList<BluetoothDevice> mBTListDevice;
    private DeviceListAdapter deviceListAdapter;

    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothConnectionService mBluetoothConnection;
    private BluetoothDevice mBTDevice;

    private Dialog dialogChat;
    private EditText edtSendMessage;
    private Button dialogbtnSendMessage;
    private ListView dialoglvMessages;
    private ArrayList<String> listMessages;
    private MessageListAdapter messageListAdapter;
    private String messages;
    private String servo;

    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        if (keyCode == keyEvent.KEYCODE_BACK) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("BẠN CÓ MUỐN THOÁT KHÔNG?");
            alertDialogBuilder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(mBluetoothAdapter.isEnabled()){
                        Log.d(TAG, "EnableDisableOnBT: disabling BT");

                        mBluetoothAdapter.disable();

                        IntentFilter BTIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                        registerReceiver(mBroadcastReceiver1, BTIntentFilter);
                    }
                    finish();
                    System.exit(0);
                    int pid = android.os.Process.myPid();
                    android.os.Process.killProcess(pid);
                }
            });

            alertDialogBuilder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialogBuilder.show();
        }
        return super.onKeyDown(keyCode, keyEvent);
    }

    /**
     * Broadcast Receiver for changes made to bluetooth states such as:
     * 1) Discoverability mode on/off or expire.
     */
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                final int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (mode){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        Toast.makeText(context, "Bluetooth đã tắt",Toast.LENGTH_LONG).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        Toast.makeText(context, "Bluetooth đã mở",Toast.LENGTH_LONG).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                    //thiet bi bat che do tim kiem
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enable.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enable. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disable. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        break;
                }
            }
        }
    };

    /**
     * Broadcast Receiver for listing devices that are not yet paired
     * -Executed by btnDiscover() method.
     */
    private BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if(action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mBTListDevice.isEmpty()) {
                    mBTListDevice.add(device);
                }
                else {
                    for (int i = 0; i < mBTListDevice.size(); i++) {
                        if (!device.getAddress().equals(mBTListDevice.get(i).getAddress()))
                            mBTListDevice.add(device);
                    }
                }
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                deviceListAdapter.notifyDataSetChanged();
            }
        }
    };

    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 case:
                //case1: bonded already
                if(device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOND BONDED");
                    mBTDevice = device;
                    dialogConnection.dismiss();
                    startConnection();
                }
                //case2: creating a bone
                if(device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND BONDING");
                }
                //case3: breaking a bond
                if(device.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND NONE");
                }
            }
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            textRecieve = intent.getStringExtra("theMessage");
            messages = mBTDevice.getName() + ": " + textRecieve;

            listMessages.add(messages);
            lvNewDevice.setAdapter(messageListAdapter);
            messageListAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//Full screen on app
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        dialogChat = new Dialog(MainActivity.this);// khởi tạo dialogChat
        dialogConnection = new Dialog(MainActivity.this);// khởi tạo dialogConnection

        mBTListDevice = new ArrayList<>();
        deviceListAdapter = new DeviceListAdapter(dialogConnection.getContext(), R.layout.device_adapter_view, mBTListDevice);

        listMessages = new ArrayList<>();
        messageListAdapter = new MessageListAdapter(dialogChat.getContext(), R.layout.messages_adapter_view, listMessages);
        messages = new String();

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver3, filter);

        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textView3);
        textView4 = (TextView) findViewById(R.id.textView4);
        textView5 = (TextView) findViewById(R.id.textView5);

        btnConnection = (Button) findViewById(R.id.btnConnect);
        btnChat = (Button) findViewById(R.id.btnChat);
        btnInfo = (Button) findViewById(R.id.btnInfo);

        btnConnection.setOnClickListener(this);
        btnChat.setOnClickListener(this);
        btnInfo.setOnClickListener(this);

        class RepetitiveUpdater implements Runnable {
            @Override
            public void run() {
                if (autobtnA_Press) {
                    pressA();
                    repeatUpdateHandler.postDelayed(new RepetitiveUpdater(), REPEAT_DELAY);
                } else if (autobtnB_Press) {
                    pressB();
                    repeatUpdateHandler.postDelayed(new RepetitiveUpdater(), REPEAT_DELAY);
                } else if (autobtnX_Press) {
                    pressX();
                    repeatUpdateHandler.postDelayed(new RepetitiveUpdater(), REPEAT_DELAY);
                } else if (autobtnY_Press) {
                    pressY();
                    repeatUpdateHandler.postDelayed(new RepetitiveUpdater(), REPEAT_DELAY);
                }
            }

        }

        btnX = (Button) findViewById(R.id.btnX);
        btnY = (Button) findViewById(R.id.btnY);
        btnA = (Button) findViewById(R.id.btnA);
        btnB = (Button) findViewById(R.id.btnB);

        btnX.setOnClickListener(this);
        btnY.setOnClickListener(this);
        btnA.setOnClickListener(this);
        btnB.setOnClickListener(this);

        btnX.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                autobtnX_Press = true;
                repeatUpdateHandler.post(new RepetitiveUpdater());
                return false;
            }
        });
        btnX.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP && autobtnX_Press) {
                    autobtnX_Press = false;
                }
                return false;
            }
        });

        btnY.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                autobtnY_Press = true;
                repeatUpdateHandler.post(new RepetitiveUpdater());
                return false;
            }
        });
        btnY.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP && autobtnY_Press) {
                    autobtnY_Press = false;
                }
                return false;
            }
        });

        btnA.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                autobtnA_Press = true;
                repeatUpdateHandler.post(new RepetitiveUpdater());
                return false;
            }
        });
        btnA.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP && autobtnA_Press) {
                    autobtnA_Press = false;
                }
                return false;
            }
        });

        btnB.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                autobtnB_Press = true;
                repeatUpdateHandler.post(new RepetitiveUpdater());
                return false;
            }
        });
        btnB.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP && autobtnB_Press) {
                    autobtnB_Press = false;
                }
                return false;
            }
        });

        layout_joystick1 = (RelativeLayout) findViewById(R.id.layout_joystick1);

        js1 = new JoyStickClass(getApplicationContext()
                , layout_joystick1, R.drawable.image_button);
        js1.setStickSize(50, 50);
        js1.setLayoutSize(200, 200);
        js1.setLayoutAlpha(200);
        js1.setStickAlpha(100);
        js1.setOffset(25);
        js1.setMinimumDistance(40);

        layout_joystick1.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                js1.drawStick(arg1);
                if (arg1.getAction() == MotionEvent.ACTION_DOWN
                        || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                    int direction = js1.get4Direction();
                    if (direction == JoyStickClass.STICK_UP) {
                        servo = "4";
                        sendBytes(servo);
                        servo = "";
                        textView5.setText("Direction : Up");
                    } else if (direction == JoyStickClass.STICK_RIGHT) {
                        servo = "2";
                        sendBytes(servo);
                        servo = "";
                        textView5.setText("Direction: Right");
                    } else if (direction == JoyStickClass.STICK_DOWN) {
                        servo = "3";
                        sendBytes(servo);
                        servo = "";
                        textView5.setText("Direction: Down");
                    } else if (direction == JoyStickClass.STICK_LEFT) {
                        servo = "1";
                        sendBytes(servo);
                        servo = "";
                        textView5.setText("Direction: Left");
                    } else if (direction == JoyStickClass.STICK_NONE) {
                        textView5.setText("Direction : Center");
                    }
                } else if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    textView5.setText("Direction:");
                }
                return true;
            }
        });

        layout_joystick2 = (RelativeLayout) findViewById(R.id.layout_joystick2);

        js2 = new JoyStickClass(getApplicationContext()
                , layout_joystick2, R.drawable.image_button);
        js2.setStickSize(50, 50);
        js2.setLayoutSize(200, 200);
        js2.setLayoutAlpha(150);
        js2.setStickAlpha(100);
        js2.setOffset(25);
        js2.setMinimumDistance(40);

        layout_joystick2.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                js2.drawStick(arg1);
                if (arg1.getAction() == MotionEvent.ACTION_DOWN
                        || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                    int direction = js2.get4Direction();
                    if (direction == JoyStickClass.STICK_UP) {
                    } else if (direction == JoyStickClass.STICK_RIGHT) {
                    } else if (direction == JoyStickClass.STICK_DOWN) {
                    } else if (direction == JoyStickClass.STICK_LEFT) {
                    } else if (direction == JoyStickClass.STICK_NONE) {
                    }
                } else if (arg1.getAction() == MotionEvent.ACTION_UP) {
                }
                return true;
            }
        });
    }

    private void sendBytes(String servo) {
        if(mBluetoothAdapter.isEnabled()) {
            byte[] bytes = servo.getBytes();
            mBluetoothConnection.write(bytes);
        }
    }

    private void pressA() {
        servo = "5";
        sendBytes(servo);
        servo = "";
        textView5.setText("Direction: Down");
    }
    private void pressB() {
        servo = "8";
        sendBytes(servo);
        servo = "";
        textView5.setText("Direction: Open");
    }
    private void pressX() {
        servo = "7";
        sendBytes(servo);
        servo = "";
        textView5.setText("Direction: Close");
    }
    private void pressY() {
        servo = "6";
        sendBytes(servo);
        servo = "";
        textView5.setText("Direction: Up");
    }

    //create method for starting connection
    //***remember the connection will fail and app will crash if you haven't paired first
    public void startConnection(){
        startBTConnection(mBTDevice,MY_UUID_INSECURE);
    }

    /**
     * starting chat service method
     */
    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");
        mBluetoothConnection.startClient(device, uuid);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            btnDiscover_EnableDisable();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter.isEnabled()) {
            callDialogConnection();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBluetoothAdapter.cancelDiscovery();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: Duoc goi");
        super.onDestroy();
        if(mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "EnableDisableOnBT: disabling BT");
            mBluetoothAdapter.disable();
            IntentFilter BTIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntentFilter);
        }
        finish();
        System.exit(0);
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
        mBluetoothAdapter.cancelDiscovery();
        unregisterReceiver(mReceiver);
        unregisterReceiver(mBroadcastReceiver3);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver1);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnConnect:
                if (!mBluetoothAdapter.isEnabled()) {
                    onStart();
                }
                if (mBluetoothAdapter.isEnabled()) {
                    callDialogConnection();
                }
                break;
            case R.id.btnInfo:
                turnOffBluetooth();
                break;
            case R.id.btnScan:
                btnDiscover();
                break;
            case R.id.btnChat:
                if(mBluetoothAdapter.isEnabled()) {
                    callDialogChat();
                }
                break;
            case R.id.btnSendMessage:
                sendMessageChat();
                break;
            case R.id.btnA:
                pressA();
                break;
            case R.id.btnB:
                pressB();
                break;
            case R.id.btnX:
                pressX();
                break;
            case R.id.btnY:
                pressY();
                break;
        }
    }

    private void turnOffBluetooth() {
        if(mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "EnableDisableOnBT: disabling BT");

            // Build an AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            // Set a title for alert dialog
            builder.setTitle("Đóng Bluetooth.");

            // Ask the final question
            builder.setMessage("Bạn muốn tắt Bluetooth?");

            // Set the alert dialog yes button click listener
            builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mBluetoothAdapter.disable();
                    IntentFilter BTIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                    registerReceiver(mBroadcastReceiver1, BTIntentFilter);

                }
            });

            // Set the alert dialog no button click listener
            builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    private void sendMessageChat() {
        byte[] bytes = edtSendMessage.getText().toString().getBytes(Charset.defaultCharset());
        mBluetoothConnection.write(bytes);

        String me = "Me: " + edtSendMessage.getText().toString();
        listMessages.add(me);
        dialoglvMessages.setAdapter(messageListAdapter);
        edtSendMessage.setText("");
        messageListAdapter.notifyDataSetChanged();
    }

    private void callDialogChat() {

        dialogChat.setContentView(R.layout.dialog_listmessage_view);// xét layout cho dialogChat
        dialogChat.setTitle("Command AT:");// xét tiêu đề cho dialogChat

        dialogbtnSendMessage = (Button) dialogChat.findViewById(R.id.btnSendMessage);
        edtSendMessage = (EditText) dialogChat.findViewById(R.id.edtSendMessage);
        dialoglvMessages = (ListView) dialogChat.findViewById(R.id.lvMessages);
        // khai báo control để bắt sự kiện

        messageListAdapter.notifyDataSetChanged();
        dialogbtnSendMessage.setOnClickListener(this);
        // bắt sự kiện cho nút btnSendMessage
        dialogChat.show();
        // hiển thị dialog
    }

    private void callDialogConnection() {

        dialogConnection.setContentView(R.layout.dialog_listdevice_view);// xét layout cho dialogConnection
        dialogConnection.setCancelable(false);// không thể nhấn ra ngoài dialogConnection và không nhấn được nút Back.
        dialogConnection.setTitle("Connection");// xét tiêu đề cho dialogConnection

        dialogBtnScan = (Button) dialogConnection.findViewById(R.id.btnScan);
        lvNewDevice = (ListView) dialogConnection.findViewById(R.id.lvNewDevice);
        // khai báo control trong dialogConnection để bắt sự kiện

        dialogBtnScan.setOnClickListener(this);// bắt sự kiện cho nút dialogBtnScan
        lvNewDevice.setOnItemClickListener(this);// bắt sự kiện cho nút lvNewDevice

        lvNewDevice.setAdapter(deviceListAdapter);
        //deviceListAdapter.notifyDataSetChanged();

        dialogConnection.show();// hiển thị dialog
    }

    public void btnDiscover_EnableDisable() {
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Log.d(TAG,"EnableDisableOnBT: Khong On Roi!!! May khong co Bluetooth.");
            finish();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "btnDiscover_EnableDisable: Making device discoverable for 300 seconds.");

            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivityForResult(discoverableIntent, REQUEST_ENABLE_BT);

            IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            registerReceiver(mBroadcastReceiver1, intentFilter);
        }
        if(mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "EnableDisableOnBT: disabling BT");
            mBluetoothAdapter.disable();

            IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            registerReceiver(mBroadcastReceiver1, intentFilter);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void btnDiscover() {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

        if(mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");

            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver2, discoverDevicesIntent);
        }

        if(!mBluetoothAdapter.isDiscovering()) {
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver2, discoverDevicesIntent);
        }
    }

    /**
     *
     * **/
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if(permissionCheck != 0) {
                this.requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }
        }else {
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You clicked on a device.");
        String deviceName = mBTListDevice.get(position).getName();
        String deviceAddress = mBTListDevice.get(position).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        //create the bond.
        //NOTE: requires API 17+?. I think this is JellyBean
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            Log.d(TAG, "Try to pair with: " + deviceName);

            mBTDevice = mBTListDevice.get(position);
            mBTDevice.createBond();
            mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
            if(mBTDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                startConnection();
                dialogConnection.dismiss();
            }
        }
    }
}
