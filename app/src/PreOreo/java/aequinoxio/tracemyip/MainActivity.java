package aequinoxio.tracemyip;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
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
    //final File DB_destination = new File(Constants.EXTERNAL_SD_SAVEPATH, Constants.DBNAME);

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
                    // TODO: USARE SE occorre aggiornare qualche componente ui

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

        if (!Utilities.getInstance().isMyServiceRunning(context,LogIp.class)) {
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
            case R.id.mnuDistroIp:
                if (item.isChecked()) {
                    sharedPreferences.edit().putBoolean(Constants.PREFERENCES_PREF_KEY_DISTRO_IP, false).apply();
                    item.setChecked(false);
                } else {
                    sharedPreferences.edit().putBoolean(Constants.PREFERENCES_PREF_KEY_DISTRO_IP, true).apply();
                    item.setChecked(true);
                }
                updateListView();

                break;

            case R.id.mnuExportView:
                // TODO: Gestire la sovrascrittura
                String savePath = exportToFile();
                fileUri = Uri.fromFile(new File(savePath));
                mResultIntent = new Intent(Intent.ACTION_SEND);
                if (fileUri != null) {

                    mResultIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                    mResultIntent.setType("*/*");
                    mResultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    startActivity(Intent.createChooser(mResultIntent, getResources().getText(R.string.send_to)));
                }

                break;

            case R.id.mnuExportDB:
                if (checkAndRequestPermission()) {
                  //  final File DB_destination = new File(Constants.EXTERNAL_SD_SAVEPATH, Constants.DBNAME);
                    if (Constants.DB_destination.exists()) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.conferma)
                                .setMessage(R.string.sovrascrivo_file)
                                .setNegativeButton(R.string.annulla, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        exportAllDB();
                                    }
                                })
                                .show();
                    } else {
                        exportAllDB();
                    }
                }
//                fileUri = Uri.fromFile(new File(DB_PATH));
//                mResultIntent = new Intent(Intent.ACTION_SEND);
//                if (fileUri != null) {
//
//                    mResultIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
//                    mResultIntent.setType("*/*");
//
//                    startActivity(Intent.createChooser(mResultIntent, getResources().getText(R.string.send_to)));
//                }
                break;

            case R.id.mnuResetPrefs:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.reset_preferences)
                        .setMessage(R.string.reset_preferences_conferma)
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

//

    private void exportAllDB() {
        // TODO: Se possibile spostarlo nelle constants. Verificare come recuperare il contesto
        String DB_PATH = context.getDatabasePath(Constants.DBNAME).getAbsolutePath();
        //File DOWNLOAD_FOLDER = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File DB_source = new File(DB_PATH);
        //File DB_destination = new File(Constants.EXTERNAL_SD_SAVEPATH, Constants.DBNAME);

        try {
            FileChannel source = new FileInputStream(DB_source).getChannel();
            FileChannel destination = new FileOutputStream(Constants.DB_destination).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();

            Uri fileUri;
            Intent mResultIntent;

            fileUri = Uri.fromFile(Constants.DB_destination);
            mResultIntent = new Intent(Intent.ACTION_SEND);
            if (fileUri != null) {
                // Put the Uri and MIME type in the result Intent

                mResultIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                mResultIntent.setType("*/*");
                mResultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                startActivity(Intent.createChooser(mResultIntent, getResources().getText(R.string.send_to)));
            }


            Toast.makeText(this, String.format(getString(R.string.export_DB_Confirm_toast),Constants.EXTERNAL_SD_SAVEPATH), Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, String.format(getString(R.string.export_DB_Error_toast),e.getMessage()), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.APP_WRITE_STORAGE_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    exportAllDB();
                } else {
                    Toast.makeText(this, R.string.export_DB_PermissionError_toast, Toast.LENGTH_LONG).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


    public boolean checkAndRequestPermission() {
        // Here, thisActivity is the current activity

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Constants.APP_WRITE_STORAGE_PERMISSION);

            return false;
//            // Permission is not granted
//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//
//            } else {
//
//                // No explanation needed; request the permission
//                ActivityCompat.requestPermissions(THIS,
//                        new String[]{Manifest.permission.READ_CONTACTS},
//                        Constants.APP_WRITE_STORAGE_PERMISSION);
//
//                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                // app-defined int constant. The callback method gets the
//                // result of the request.
//            }
//        } else {
//        }
        } else {
            // Permission has already been granted
            return true;
        }

    }

    private String exportToFile() {
        Date date = new Date();
        File sd = new File(Constants.EXTERNAL_SD_SAVEPATH);
        File fileSalvataggio;
        SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

        // Provo a creare la directory sulla sd
        boolean successCreaDir = true;
        if (!sd.exists()) {
            successCreaDir = sd.mkdir();
        }

        // Se non riesco a creare la directory metto tutto nella subdir dell'App
        if (!successCreaDir)
            fileSalvataggio = new File(context.getFilesDir(), Constants.FILENAME_GIORNALIERO);
        else
            fileSalvataggio = new File(sd, Constants.FILENAME_GIORNALIERO);

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

        boolean distroIpData = sharedPreferences.getBoolean(Constants.PREFERENCES_PREF_KEY_DISTRO_IP, true);
        menuItem = menu.findItem(R.id.mnuDistroIp);
        if (distroIpData) {
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
                SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE);
//                if(sharedPreferences.getBoolean(Constants.PREFERENCES_PREF_KEY_ONLY_IP, true)){
//                    Toast.makeText(getApplicationContext(),"Verranno mostrati solo i gioni",Toast.LENGTH_LONG).show();
//                    //return true; // Per ora tratto il caso di vista pe rsoli IP
//                }

                //Log.v("long clicked","pos: " + pos);
                detailDialog = new DetailDialog();

                DataRow dataRow = (DataRow) arg0.getItemAtPosition(pos);

                // Passo iparametri
                Bundle args = new Bundle();
                args.putString(Constants.DIALOG_PARAM_IP, dataRow.getIp());
                args.putString(Constants.DIALOG_PARAM_DATA, dataRow.getData());
                detailDialog.setArguments(args);

                detailDialog.show(getFragmentManager(), "DetailsDialog");

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

//    private boolean isMyServiceRunning(Class<?> serviceClass) {
//        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            if (serviceClass.getName().equals(service.service.getClassName())) {
//                return true;
//            }
//        }
//        return false;
//    }

    private void resetPreferences() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(Constants.PREFERENCES_PREF_KEY_EXTERNAL_IP, false).apply();
        sharedPreferences.edit().putBoolean(Constants.PREFERENCES_PREF_KEY_ONLY_IP, false).apply();
        sharedPreferences.edit().putBoolean(Constants.PREFERENCES_PREF_KEY_DISTRO_IP, false).apply();
    }

}
