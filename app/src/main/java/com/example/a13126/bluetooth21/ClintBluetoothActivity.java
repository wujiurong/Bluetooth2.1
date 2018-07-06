package com.example.a13126.bluetooth21;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.UUID;

public class ClintBluetoothActivity extends AppCompatActivity {

    private static final int CONN_SUCCESS = 0x01;
    private static final int CONN_FAIL = 0x02;
    private static final int RECEIVER_INFO = 0x03;
    private static final int SET_EDITTEXT_NULL = 0x04;

    private Button button_send;
    private TextView textView_content;
    private EditText editText;
    private Intent intent;

    BluetoothAdapter bluetooth = null;
    BluetoothDevice device = null;
    BluetoothSocket socket = null;

    public static String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    PrintStream out;
    BufferedReader in;

    String address = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clinet_bluetooth);
        button_send = findViewById(R.id.button_send);
        textView_content = findViewById(R.id.textView_content);
        editText =findViewById(R.id.editText);

        intent = getIntent() ;
        address = intent.getStringExtra("add");
        System.out.println(address);
        init();
    }
    //初始化Socket连接
    private void init() {
        textView_content.setText("正在建立连接。。...。\n");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //得到本地蓝牙设备的默认适配器
                    bluetooth = BluetoothAdapter.getDefaultAdapter();
                    //通过本地蓝牙设备得到远程蓝牙设备
                    device = bluetooth.getRemoteDevice(address);
                    //根据UUID创建并返回一个BluetoothSocket
                    socket = device.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
                    if (socket!=null){
                        socket.connect();
                        out = new PrintStream(socket.getOutputStream());
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    }
                    handler.sendEmptyMessage(CONN_SUCCESS);
                } catch (IOException e) {
                    e.printStackTrace();
                    Message msg = handler.obtainMessage(CONN_FAIL,e.getLocalizedMessage());
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    private boolean isReceiver = true;
    /**
     * 接收线程
     */
    class ReceiverInfoThread implements Runnable{

        @Override
        public void run() {
            String info = null;
            while (isReceiver){
                try {
                    info = in.readLine();
                    Message msg = handler.obtainMessage(RECEIVER_INFO,info);
                    handler.sendMessage(msg);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case RECEIVER_INFO:
                    setInfo(msg.obj.toString()+"\n");
                    break;
                case SET_EDITTEXT_NULL:
                    editText.setText("");
                    break;
                case CONN_SUCCESS:
                    setInfo("连接成功！\n");
                    button_send.setEnabled(true);
                    new Thread(new ReceiverInfoThread()).start();
                    break;
                case CONN_FAIL:
                    setInfo("连接失败！\n");
                    setInfo(msg.obj.toString()+"\n");
                default:
                    break;
            }
        }
    };

    /**
     * 发送过程
     * @param view
     */
    public void sendClick(View view){
        final String content = editText.getText().toString();
        if (TextUtils.isEmpty(content)){
            Toast.makeText(this,"不能发送空消息",Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                out.println(content);
                out.flush();
                setInfo(content);
                handler.sendEmptyMessage(SET_EDITTEXT_NULL);
            }
        }).start();
    }

    private void setInfo(String info){
        StringBuffer sb = new StringBuffer();
        sb.append(textView_content.getText());
        sb.append(info);
        textView_content.setText(sb);
    }

    @Override
    protected void onDestroy() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}


