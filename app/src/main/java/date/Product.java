package date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import org.json.JSONArray;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

@SuppressWarnings("serial")
public class Product implements Serializable {
    // Table Columns Names
    public static final String KEY_NAME = "Name";
    public static final String KEY_BARCODE = "Barcode";
    public static final String KEY_UNIT = "Unit";
    public static final String KEY_PRICE = "Price";
    public static final String KEY_Note = "Note";
    public static String DATABASE_TABLE = "DistDeviceMt000";
    public String Name;
    public String Barcode;
    public String Unit;
    public double Price;
    public  String Note;

    public Product() {
    }

    public Product( String name, String barcode, String unit, double price, String note) {
        Name = name;
        Barcode = barcode;
        Unit = unit;
        Price = price;
        Note = note;
    }

    public static boolean SaveInDatabase(Context context, ArrayList<Product> products) {
        DBAdapter adapter = new DBAdapter(context);
        try {
            adapter.Open();
            DBAdapter.database.beginTransaction();
            String sql = "INSERT INTO " + DATABASE_TABLE + " (" +
                    KEY_NAME + "," +
                    KEY_BARCODE + "," +
                    KEY_UNIT + "," +
                    KEY_PRICE + "," +
                    KEY_Note +
                    ") VALUES ( ?, ?, ?, ?, ?)";


            SQLiteStatement stmt = DBAdapter.database.compileStatement(sql);
            for (int i = 0; i < products.size(); i++) {
                stmt.bindString(1, products.get(i).Name);
                stmt.bindString(2, products.get(i).Barcode);
                stmt.bindString(3, products.get(i).Unit);
                stmt.bindDouble(4, products.get(i).Price);
                stmt.bindString(5, products.get(i).Note);
                stmt.execute();
                stmt.clearBindings();
            }
            DBAdapter.database.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            DBAdapter.database.endTransaction();
            adapter.Close();
        }
    }

    public static boolean DeleteAll(Context context) {
        boolean result;
        DBAdapter adapter = new DBAdapter(context);
        try {
            adapter.Open();
            DBAdapter.database.beginTransaction();
            result = (DBAdapter.database.delete(DATABASE_TABLE, null, null) > 0);
            DBAdapter.database.setTransactionSuccessful();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            DBAdapter.database.endTransaction();
            adapter.Close();
        }
    }

    public static boolean Delete(Context context, String barCode) {
        boolean result = false;
        DBAdapter adapter = new DBAdapter(context);
        try {
            adapter.Open();
            result = (DBAdapter.database.delete(DATABASE_TABLE, KEY_BARCODE + "=" + barCode, null) > 0);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            adapter.Close();
        }
    }

    public static Product GetByBarcode(Context context, String barcode) {
        Product product = null;
        DBAdapter adapter = new DBAdapter(context);
        try {
            String selectQuery = "SELECT * FROM " + DATABASE_TABLE + " WHERE " + KEY_BARCODE + "='" + barcode + "'";
            adapter.Open();
            Cursor cursor = DBAdapter.database.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                product = new Product();
                product.Name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                product.Barcode = cursor.getString(cursor.getColumnIndex(KEY_BARCODE));
                product.Unit = cursor.getString(cursor.getColumnIndex(KEY_UNIT));
                product.Price = cursor.getDouble(cursor.getColumnIndex(KEY_PRICE));
                product.Note = cursor.getString(cursor.getColumnIndex(KEY_Note));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            adapter.Close();
        }

        return product;
    }

    public static boolean Update(Context context, Product product) {
        boolean result;
        DBAdapter adapter = new DBAdapter(context);

        try {
            adapter.Open();
            ContentValues values = new ContentValues();
            values.put(KEY_NAME, product.Name);
            values.put(KEY_BARCODE, product.Barcode);
            values.put(KEY_UNIT, product.Unit);
            values.put(KEY_PRICE, product.Price);
            values.put(KEY_Note, product.Note);
            result = (DBAdapter.database.update(DATABASE_TABLE, values, KEY_BARCODE + "=" + product.Barcode, null) > 0);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            adapter.Close();
        }
    }
}