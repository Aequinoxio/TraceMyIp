package aequinoxio.tracemyip.DatabaseUtils;

/**
 * Created by utente on 29/03/2016.
 */

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataAdapter {
    private static final String TAG = "DataAdapter";

    private SQLiteDatabase sqLiteDatabase;
    private DatabaseHandler databaseHandler;

    public DataAdapter(Context context) {
        Context mContext = context;
        databaseHandler = new DatabaseHandler(mContext);
    }

    public DataAdapter createDatabase() throws SQLException {
        try {
            databaseHandler.createDataBase();
        } catch (IOException mIOException) {
            Log.e(TAG, mIOException.toString() + "  UnableToCreateDatabase");
            throw new Error("UnableToCreateDatabase");
        }
        return this;
    }

    public DataAdapter open() throws SQLException {
        try {
            //databaseHandler.openDataBase();
            //databaseHandler.close();
            sqLiteDatabase = databaseHandler.getReadableDatabase();
        } catch (SQLException mSQLException) {
            Log.e(TAG, "open >>" + mSQLException.toString());
            throw mSQLException;
        }
        return this;
    }

    public void close() {
        databaseHandler.close();
    }

    public Cursor getCursor(String sql) {
        try {
            Cursor mCur = sqLiteDatabase.rawQuery(sql, null);
            if (mCur != null) {
                //mCur.moveToNext();
                mCur.moveToFirst();
            }
            return mCur;
        } catch (SQLException mSQLException) {
            Log.e(TAG, "getTestData >>" + mSQLException.toString());
            throw mSQLException;
        }
    }

    /**
     * Getting all labels
     * returns list of labels
     */
    public List<DataRow> getValues(String selectQuery, String columnName) {
        List<DataRow> dataRows = new ArrayList<>();
        String columns[] = columnName.split(",");

        //SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                DataRow dr = new DataRow();
                for (String col : columns) {
                    switch (col.trim()) {
                        case "ip":
                            dr.ip = cursor.getString(cursor.getColumnIndex("ip"));
                            break;
                        case "data_semplice":
                            dr.timestamp = cursor.getString(cursor.getColumnIndex("data_semplice"));
                            break;
                        case "timestamp":
                            dr.timestamp = cursor.getString(cursor.getColumnIndex("timestamp"));
                            break;
                        case "interface":
                            dr.networkInterface = cursor.getString(cursor.getColumnIndex("interface"));
                            break;
                        case "counter":
                            dr.count=Integer.valueOf(cursor.getString(cursor.getColumnIndex("counter")));
                            break;
                    }
                }
                dataRows.add(dr);
            } while (cursor.moveToNext());
        }

        cursor.close();

        // returning lables
        return dataRows;
    }

    /**
     * Ritorna una lista delle stringhe per una select con singola colonna
     * @param selectQuery
     * @param whereValues
     * @return
     */
    public List<String> getValues(String selectQuery, String[] whereValues) {
        List<String> dataRows = new ArrayList<>();

        //SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, whereValues);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                dataRows.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        cursor.close();

        // returning lables
        return dataRows;

    }


    /**
     * Inserisce i valori nella tabella
     */
    public void insertValues(String updateQuery, String value1, String value2) {
        //Cursor cursor = sqLiteDatabase.rawQuery(updateQuery, null);
        SQLiteStatement stmt = sqLiteDatabase.compileStatement(updateQuery);
        stmt.bindString(1, value1);
        stmt.bindString(2, value2);
        long temp = stmt.executeInsert();
        //sqLiteDatabase.execSQL(updateQuery);
    }

    public void insertQuery(String insertQuery) {
        //Cursor cursor = sqLiteDatabase.rawQuery(updateQuery, null);
        sqLiteDatabase.execSQL(insertQuery);
    }

}