package aequinoxio.tracemyip;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import aequinoxio.tracemyip.DatabaseUtils.DataRow;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter intentFilter = new IntentFilter(Constants.NETWORK_AVAILABLE_INTENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO: Update ui
               // EditText editText = (EditText)findViewById(R.id.edtLog);
                Bundle bundle = intent.getExtras();
                int objNum = bundle.getInt(Constants.INTENT_OBJECTS_LENGTH);
                String timestamp = bundle.getString(Constants.INTENT_TIMESTAMP);
               // editText.append(timestamp+"\n");
                ArrayList<String> ifaceName;
                ArrayList<String> ifaceIP;
                ifaceName = bundle.getStringArrayList(Constants.NETWORK_INTERFACE_NAME_DETAIL_INTENT);
                ifaceIP = bundle.getStringArrayList(Constants.NETWORK_INTERFACE_IP_DETAIL_INTENT);
                for(int i=0;i<ifaceName.size();i++){
                    // TODO: USARE SE SERVE aggiornare qualche componente ui
            //        editText.append(String.format("%s - %s%n",ifaceName.get(i),ifaceIP.get(i)));
                }
                updateListView();

            }
        }, intentFilter);

//        // debug
//        try {
//            FileOutputStream fos = openFileOutput("log.txt", MODE_APPEND | MODE_PRIVATE);
//            fos.write("test".getBytes());
//            fos.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        NetworkState networkState = NetworkState.getInstance(this);
        networkState.updateState();

        if (!isMyServiceRunning(LogIp.class)){
            Intent intent= new Intent(this,LogIp.class);
            startService(intent);
        };

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.mnuAbout:
                break;
            case R.id.mnuPrefs:
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    public void updateListView(){

        List<DataRow> lables;
        lables = NetworkState.getInstance(this).getData();
        // Creating adapter for spinner
        ArrayAdapter<DataRow> dataAdapter = new ArrayAdapter<DataRow>(this,
                android.R.layout.simple_list_item_1, lables);

        ListView lstView = (ListView)findViewById(R.id.lstIp);
        //attaching data adapter to spinner
        lstView.setAdapter(dataAdapter);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
