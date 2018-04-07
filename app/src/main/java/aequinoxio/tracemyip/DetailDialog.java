package aequinoxio.tracemyip;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import aequinoxio.tracemyip.DatabaseUtils.DataRow;

/**
 * Created by utente on 17/03/2018.
 */

public class DetailDialog extends DialogFragment {
    Activity mActivity;
    ListView mListViewIP;
    ListView mListViewData;
    String paramData ;
    String paramIP ;


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mActivity = getActivity();

        Bundle mArgs = getArguments();
        paramData = mArgs.getString(Constants.DIALOG_PARAM_DATA).trim();
        paramIP = mArgs.getString(Constants.DIALOG_PARAM_IP).trim();

        View v = getActivity().getLayoutInflater().inflate(R.layout.detail_dialog, null);
        mListViewData = v.findViewById(R.id.lstDaysForIp);
        mListViewIP = v.findViewById(R.id.lstIPInDay);

        // TODO: DEBUG
        updateValues(paramIP, paramData, false);

        TextView textView = v.findViewById(R.id.txtDialogDetailData);
        textView.setText(paramData);
        textView = v.findViewById(R.id.txtDialogDetailIp);
        textView.setText(paramIP);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());//, R.style.Theme_AppCompat_NoActionBar);

        // Get the layout inflater
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        // builder.setView(inflater.inflate(R.layout.detail_dialog, null));
        builder.setView(v);

        builder.setMessage("")
                .setTitle("Dettagli")
                .setPositiveButton("Chiudi", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
//                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // User cancelled the dialog
//                    }
//                });


        Dialog temp = builder.create();
        temp.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Create the AlertDialog object and return it
        return temp;
    }

    public void updateValues(String IP, String Data, boolean onlyExternal) {
        List<String> lables;
        if (IP != null) {
            lables = NetworkState.getInstance(getActivity()).getDataDayForIp(IP);
            // Creating adapter for spinner
            ArrayAdapter<String> dataAdapterIP = new ArrayAdapter<>(mActivity, android.R.layout.simple_list_item_1, lables);
            //attaching data adapter to spinner
            mListViewData.setAdapter(dataAdapterIP);
        }
        List<String> lablesData;
        if (Data != null) {
            lablesData = NetworkState.getInstance(getActivity()).getDataIpInDay(Data, onlyExternal);
            // Creating adapter for spinner
            ArrayAdapter<String> dataAdapterData = new ArrayAdapter<>(mActivity, android.R.layout.simple_list_item_1, lablesData);
            //attaching data adapter to spinner
            mListViewIP.setAdapter(dataAdapterData);
        }
    }

    public void updateListView(boolean checked){
        updateValues(paramIP, paramData, checked);
    }
// Questo metodo deve stare nella classe che lancia il dialog.
//    public void ckExternalIp(View view) {
//        // Is the view now checked?
//        boolean checked = ((CheckBox) view).isChecked();
//
//        // Check which checkbox was clicked
//        switch (view.getId()) {
//            case R.id.ckbDialogExIp:
//                if (checked) {
//
//                }
//                // Put some meat on the sandwich
//                else
//                    // Remove the meat
//                    break;
//                // TODO: Veggie sandwich
//        }
//
//    }
}
