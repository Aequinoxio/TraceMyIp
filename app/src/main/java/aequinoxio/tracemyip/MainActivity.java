package aequinoxio.tracemyip;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import aequinoxio.tracemyip.DatabaseUtils.DataRow;

public class MainActivity extends AppCompatActivity {

    Context context;

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

        this.context=this;

        IntentFilter intentFilter = new IntentFilter(Constants.NETWORK_AVAILABLE_INTENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                int objNum = bundle.getInt(Constants.INTENT_OBJECTS_LENGTH);
                String timestamp = bundle.getString(Constants.INTENT_TIMESTAMP);
                ArrayList<String> ifaceName;
                ArrayList<String> ifaceIP;
                ifaceName = bundle.getStringArrayList(Constants.NETWORK_INTERFACE_NAME_DETAIL_INTENT);
                ifaceIP = bundle.getStringArrayList(Constants.NETWORK_INTERFACE_IP_DETAIL_INTENT);
                for(int i=0;i<ifaceName.size();i++){
                    // TODO: USARE SE SERVE aggiornare qualche componente ui

                }
                updateListView();
                RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
                relativeLayout.setVisibility(View.GONE);

                SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
                swipeRefreshLayout.setRefreshing(false);


            }
        }, intentFilter);

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
                relativeLayout.setVisibility(View.VISIBLE);

                NetworkState networkState = NetworkState.getInstance(context);
                networkState.updateState();

                updateListView();
            }
        });

        swipeRefreshLayout.setRefreshing(true);

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        relativeLayout.setVisibility(View.VISIBLE);

        NetworkState networkState = NetworkState.getInstance(this);
        networkState.updateState();

        if (!isMyServiceRunning(LogIp.class)){
            Intent intent= new Intent(this,LogIp.class);
            startService(intent);
        };

        updateListView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.mnuAbout:
                String s = getString(R.string.app_name) +" - Versione " + BuildConfig.VERSION_NAME +"\n";
                s+="by "+ getString(R.string.Autore)+"\n\n";
                s+=getString(R.string.app_desc);
                s+="\n\n"+getString(R.string.credits);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.action_about)
                        .setMessage(s)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).show();

                break;
            case R.id.mnuPrefs:
                SharedPreferences sharedPreferences = this.getSharedPreferences(Constants.PREFERENCES_NAME,MODE_PRIVATE);
                if(item.isChecked()){
                    sharedPreferences.edit().putBoolean(Constants.PREFERENCES_PREF_KEY_EXTERNAL_IP,false).apply();
                    item.setChecked(false);
                }else{
                    sharedPreferences.edit().putBoolean(Constants.PREFERENCES_PREF_KEY_EXTERNAL_IP,true).apply();
                    item.setChecked(true);
                }
                updateListView();
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);

        SharedPreferences sharedPreferences = (SharedPreferences)this.getSharedPreferences(Constants.PREFERENCES_NAME,MODE_PRIVATE);
        boolean showExternam = sharedPreferences.getBoolean(Constants.PREFERENCES_PREF_KEY_EXTERNAL_IP,true);
        MenuItem menuItem= menu.findItem(R.id.mnuPrefs);
        if (showExternam){
            menuItem.setChecked(true);
        }else{
            menuItem.setChecked(false);
        }

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
