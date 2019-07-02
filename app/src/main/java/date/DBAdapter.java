package date;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class DBAdapter {
    public static final String DATABASE_NAME = "MGZData.db";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_PRODUCT_TABLE_SCRIPT = "CREATE TABLE " + Product.DATABASE_TABLE + " (" +
            Product.KEY_NAME + " TEXT, " +
            Product.KEY_BARCODE + " TEXT, " +
            Product.KEY_UNIT + " TEXT, " +
            Product.KEY_PRICE + " REAL, " +
            Product.KEY_Note + " Text );";
    public static SQLiteDatabase database;
    private static DatabaseHelper databaseHelper;
    final Context context;

    public DBAdapter(Context ctx) {
        context = ctx;

        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper(context);
        }
    }

    //---opens the database---
    public DBAdapter Open() throws SQLException {
        database = databaseHelper.getWritableDatabase();
        return this;
    }

    public void ExecuteSQL(String sql) {
        //Open();
        database = databaseHelper.getWritableDatabase();
        database.execSQL(sql);
        database.close();
        //Close();
    }

    //---closes the database---
    public void Close() {
        databaseHelper.close();
    }

    private class DatabaseHelper extends SQLiteOpenHelper {
        private DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(CREATE_PRODUCT_TABLE_SCRIPT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        }
    }
}
