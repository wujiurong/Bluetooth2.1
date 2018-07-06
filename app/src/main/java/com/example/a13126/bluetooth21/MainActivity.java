package com.example.a13126.bluetooth21;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity{

    private ArrayList<String> btNameList = new ArrayList<String>(); // 用于保存获取到的蓝牙名称
    private ArrayList<String> addressList = new ArrayList<String>();
    private MyAdapter myAdapter;
    private BluetoothAdapter bluetooth;
    private Intent mIntent;

    private Switch aSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        aSwitch = findViewById(R.id.switch1);
        initData();
        //mIntent = new Intent(MainActivity.this,BleClientService.class);

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    Intent enable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    enable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,3000);
                    startActivity(enable);
                }else {
                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    bluetoothAdapter.disable();
                    initData();
                    intiListView();
                }
            }
        });
    }


    private void initData() {
        // 假如列表中有数据，先清空
        btNameList.clear();
        addressList.clear();
    }
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                btNameList.add(device.getName() + "\n" + device.getAddress());
                addressList.add(device.getAddress());
                Log.d("搜索到的设备：",device.getName()+":"+device.getAddress());
                System.out.println(device.getName() + "\n" + device.getAddress());
                intiListView();

            }
        }
    };
    private void intiListView(){
        ListView listView = findViewById(R.id.listView);

        myAdapter = new MyAdapter();
        listView.setAdapter(myAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String btAddress = addressList.get(position);//传输当前的蓝牙地址
                Toast.makeText(MainActivity.this,btAddress,Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this,ClintBluetoothActivity.class);
                intent.putExtra("add",btAddress);
                startActivity(intent);
            }
        });
    }

    public void scanClick(View view){

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.startDiscovery();
        //查询已配对过的设备
        Set<BluetoothDevice> set = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bd : set) {
            Log.d("TAG","已配对设备"+bd.getName()+":"+bd.getAddress());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View inflate = getLayoutInflater().inflate(R.layout.listview_item_layout, null);
            TextView textView =  inflate.findViewById(R.id.idBt);// 查找item中的textView
            String btName = btNameList.get(position);
            textView.setText(btName);
            return inflate;
        }
        @Override
        public int getCount() {
            // 决定ListView的行数,这里设成容器内容数
            return btNameList.size();
        }
        @Override
        public Object getItem(int position) {
            return position;
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    public void getClick(View view){
        //设置本机蓝牙可被其他设备检查到
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }


}

