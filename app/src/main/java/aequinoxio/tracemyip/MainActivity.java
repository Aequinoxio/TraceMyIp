package aequinoxio.tracemyip;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import aequinoxio.tracemyip.DatabaseUtils.DataRow;

public class MainActivity extends AppCompatActivity {

    Context context;
    BroadcastReceiver broadcastReceiver;
    DetailDialog detailDialog;

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
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = this;

        IntentFilter intentFilter = new IntentFilter(Constants.NETWORK_AVAILABLE_INTENT);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                int objNum = bundle.getInt(Constants.INTENT_OBJECTS_LENGTH);
                String timestamp = bundle.getString(Constants.INTENT_TIMESTAMP);
                ArrayList<String> ifaceName;
                ArrayList<String> ifaceIP;
                ifaceName = bundle.getStringArrayList(Constants.NETWORK_INTERFACE_NAME_DETAIL_INTENT);
                ifaceIP = bundle.getStringArrayList(Constants.NETWORK_INTERFACE_IP_DETAIL_INTENT);
                for (int i = 0; i < ifaceName.size(); i++) {
                    // TODO: USARE SE SERVE aggiornare qualche componente ui

                }
                updateListView();
                RelativeLayout relativeLayout = findViewById(R.id.relativeLayout);
                relativeLayout.setVisibility(View.GONE);

                SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swiperefresh);
                swipeRefreshLayout.setRefreshing(false);


            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RelativeLayout relativeLayout = findViewById(R.id.relativeLayout);
                relativeLayout.setVisibility(View.VISIBLE);

                NetworkState networkState = NetworkState.getInstance(context);
                networkState.updateState();

                updateListView();
            }
        });

        swipeRefreshLayout.setRefreshing(true);

        RelativeLayout relativeLayout = findViewById(R.id.relativeLayout);
        relativeLayout.setVisibility(View.VISIBLE);

        NetworkState networkState = NetworkState.getInstance(this);
        networkState.updateState();

        if (!isMyServiceRunning(LogIp.class)) {
            Intent intent = new Intent(this, LogIp.class);
            startService(intent);
        }

        updateListView();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        SharedPreferences sharedPreferences = this.getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE);

        boolean checkMenu = sharedPreferences.getBoolean(Constants.PREFERENCES_PREF_KEY_EXTERNAL_IP, false);
        MenuItem menuItem = menu.findItem(R.id.mnuOnlyExternalIp);
        menuItem.setChecked(checkMenu);

        checkMenu = sharedPreferences.getBoolean(Constants.PREFERENCES_PREF_KEY_ONLY_IP, false);
        menuItem = menu.findItem(R.id.mnuIPOnly);
        menuItem.setChecked(checkMenu);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences sharedPreferences = this.getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE);
        Uri fileUri;
        Intent mResultIntent;
        switch (item.getItemId()) {
            case R.id.mnuAbout:
                String s = getString(R.string.app_name) + " - Versione " + BuildConfig.VERSION_NAME + "\n";
                s += "by " + getString(R.string.Autore) + "\n\n";
                s += getString(R.string.app_desc);
                s += "\n\n" + getString(R.string.credits);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.action_about)
                        .setMessage(s)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).show();

                break;
            case R.id.mnuOnlyExternalIp:
                if (item.isChecked()) {
                    sharedPreferences.edit().putBoolean(Constants.PREFERENCES_PREF_KEY_EXTERNAL_IP, false).apply();
                    item.setChecked(false);
                } else {
                    sharedPreferences.edit().putBoolean(Constants.PREFERENCES_PREF_KEY_EXTERNAL_IP, true).apply();
                    item.setChecked(true);
                }
                updateListView();
                break;
            case R.id.mnuIPOnly:
                if (item.isChecked()) {
                    sharedPreferences.edit().putBoolean(Constants.PREFERENCES_PREF_KEY_ONLY_IP, false).apply();
                    item.setChecked(false);
                } else {
                    sharedPreferences.edit().putBoolean(Constants.PREFERENCES_PREF_KEY_ONLY_IP, true).apply();
                    item.setChecked(true);
                }
                updateListView();
                break;

            case R.id.mnuExportView:
                String savePath = exportToFile();
                fileUri = Uri.fromFile(new File(savePath));
                mResultIntent = new Intent(Intent.ACTION_SEND);
                if (fileUri != null) {
                    // Put the Uri and MIME type in the result Intent
//                    mResultIntent.setDataAndType(
//                            fileUri,
//                            getContentResolver().getType(fileUri));

                    mResultIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                    mResultIntent.setType("*/*");
                    mResultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    startActivity(Intent.createChooser(mResultIntent, getResources().getText(R.string.send_to)));
                    // Set the result
//                    MainActivity.this.setResult(Activity.RESULT_OK,
//                            mResultIntent);
                }
//                else {
//                    mResultIntent.setDataAndType(null, "");
//                    MainActivity.this.setResult(RESULT_CANCELED,
//                            mResultIntent);
//                }


//                new AlertDialog.Builder(MainActivity.this)
//                        .setTitle(R.string.action_about)
//                        .setMessage("Salvato in: " + savePath)
//                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.cancel();
//                            }
//                        }).show();


//                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//                i.addCategory(Intent.CATEGORY_DEFAULT);
//                startActivityForResult(Intent.createChooser(i, "Choose directory"), Constants.DIRECTORY_CHOOSER_INTENT_ID);
                break;

            case R.id.mnuExportDB:
                String DB_PATH = context.getDatabasePath(Constants.DBNAME).getAbsolutePath() ;
                fileUri = Uri.fromFile(new File(DB_PATH));
                mResultIntent = new Intent(Intent.ACTION_SEND);
                if (fileUri != null) {

                    mResultIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                    mResultIntent.setType("*/*");

                    startActivity(Intent.createChooser(mResultIntent, getResources().getText(R.string.send_to)));
                }
                break;

            case R.id.mnuResetPrefs:
                new AlertDialog.Builder(this)
                        .setTitle("Reset delle preferenze")
                        .setMessage("Vuoi veramente ripristinare le preferenze ai valori didefault?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                resetPreferences();
                                updateListView();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
                break;

        }
        return true;
    }


    private String exportToFile() {
        Date date = new Date();
        File sd = new File(Environment.getExternalStorageDirectory() + "/TraceMyIp");
        File fileSalvataggio;
        SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

        // TODO: Costante estensione cablata
        //String filenameGiornaliero = ft.format(date) + ".txt";
        String filenameGiornaliero = "export.csv";

        // Provo a creare la directory sulla sd
        boolean successCreaDir = true;
        if (!sd.exists()) {
            successCreaDir = sd.mkdir();
        }

        // Se non riesco a creare la directory metto tutto nella subdir dell'App
        if (!successCreaDir)
            fileSalvataggio = new File(context.getFilesDir(), filenameGiornaliero);
        else
            fileSalvataggio = new File(sd, filenameGiornaliero);

        OutputStream outputStream;
        try {
            outputStream = getContentResolver().openOutputStream(Uri.fromFile(fileSalvataggio));
            List<DataRow> datas = NetworkState.getInstance(this).getData();
            for (DataRow dataRow : datas) {
                try {
                    outputStream.write(dataRow.toExportString().getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileSalvataggio.getAbsolutePath();

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.DIRECTORY_CHOOSER_INTENT_ID:
                if (data != null) {
                    Uri filePath = Uri.withAppendedPath(data.getData(), "test.csv");
                    Log.v("Test", "Result URI " + filePath.toString());
                    //filePath.buildUpon().path("test.csv").build();
                    try {
                        OutputStream outputStream = getContentResolver().openOutputStream(filePath);
                        List<DataRow> datas = NetworkState.getInstance(this).getData();
                        for (DataRow dataRow : datas) {
                            outputStream.write(dataRow.toExportString().getBytes());
                        }
                        outputStream.close();

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);

        SharedPreferences sharedPreferences = this.getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE);
        boolean showExternam = sharedPreferences.getBoolean(Constants.PREFERENCES_PREF_KEY_EXTERNAL_IP, true);
        MenuItem menuItem = menu.findItem(R.id.mnuOnlyExternalIp);
        if (showExternam) {
            menuItem.setChecked(true);
        } else {
            menuItem.setChecked(false);
        }

        return true;
    }


    public void updateListView() {

        List<DataRow> lables;
        lables = NetworkState.getInstance(this).getData();
        // Creating adapter for spinner
        ArrayAdapter<DataRow> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, lables);

        ListView lstView = findViewById(R.id.lstIp);
        //attaching data adapter to spinner
        lstView.setAdapter(dataAdapter);
        lstView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                // TODO Auto-generated method stub

                //Log.v("long clicked","pos: " + pos);
                 detailDialog = new DetailDialog();

                DataRow dataRow = (DataRow) arg0.getItemAtPosition(pos);

                // Passo iparametri
                Bundle args = new Bundle();
                args.putString(Constants.DIALOG_PARAM_IP, dataRow.getIp());
                args.putString(Constants.DIALOG_PARAM_DATA,dataRow.getData());
                detailDialog.setArguments(args);

                detailDialog.show(getFragmentManager(),"DetailsDialog");

//                DetailDialog2 d2 = new DetailDialog2(getApplicationContext());
//                d2.show();
                return true;
            }
        });
    }
    public void ckExternalIp(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch (view.getId()) {
            case R.id.ckbDialogExIp:
                detailDialog.updateListView(checked);

                break;

        }

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

    private void resetPreferences() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(Constants.PREFERENCES_PREF_KEY_EXTERNAL_IP, false).apply();
        sharedPreferences.edit().putBoolean(Constants.PREFERENCES_PREF_KEY_ONLY_IP, false).apply();
    }

}
