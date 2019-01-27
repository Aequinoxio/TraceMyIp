package aequinoxio.tracemyip;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Activity per mostrare i dettagli.
 */
public class DetailActivity extends AppCompatActivity {
    private Activity mActivity;
    private ListView mListViewIP;
    private ListView mListViewData;
    private String paramData ;
    private String paramIP ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Mostro il pulsante back nell'actionbar
        try {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException npe){
            npe.printStackTrace();
        }

        mActivity=this;

        Bundle mArgs = getIntent().getExtras();
        paramData = mArgs.getString(Constants.DIALOG_PARAM_DATA);
        if (paramData!=null){
            paramData=paramData.trim();
        }
        paramIP = mArgs.getString(Constants.DIALOG_PARAM_IP);
        if (paramIP!=null){
            paramIP=paramIP.trim();
        }

        mListViewData = findViewById(R.id.lstDaysForIp);
        mListViewIP = findViewById(R.id.lstIPInDay);

        // TODO: DEBUG
        updateValues(paramIP, paramData, false);

        TextView textView = findViewById(R.id.txtDialogDetailData);
        textView.setText(paramData);
        textView = findViewById(R.id.txtDialogDetailIp);
        textView.setText(paramIP);

    }

    public void ckExternalIp(View v){
        // Is the view now checked?
        boolean checked = ((CheckBox) v).isChecked();

        // Check which checkbox was clicked
        switch (v.getId()) {
            case R.id.ckbExIp:
                updateListView(checked);

                break;

        }

    }

    public void updateValues(String IP, String Data, boolean onlyExternal) {
        List<String> lables;
        if (IP != null) {
            lables = NetworkState.getInstance(this).getDataDayForIp(IP); // TODO: probabile BUG passare la MAIN Activity
            // Creating adapter for spinner
            ArrayAdapter<String> dataAdapterIP = new ArrayAdapter<>(mActivity, android.R.layout.simple_list_item_1, lables);
            //attaching data adapter to spinner
            mListViewData.setAdapter(dataAdapterIP);
        }
        List<String> lablesData;
        if (Data != null) {
            lablesData = NetworkState.getInstance(this).getDataIpInDay(Data, onlyExternal); // TODO: probabile BUG passare la MAIN Activity
            // Creating adapter for spinner
            ArrayAdapter<String> dataAdapterData = new ArrayAdapter<>(mActivity, android.R.layout.simple_list_item_1, lablesData);
            //attaching data adapter to spinner
            mListViewIP.setAdapter(dataAdapterData);
        }
    }

    public void updateListView(boolean checked){
        updateValues(paramIP, paramData, checked);
    }

}
