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
    public static final String KEY_LATIN_NAME = "LatinName";
    public static final String KEY_BARCODE = "Barcode";
    public static final String KEY_UNITY = "Unit";
    public static final String KEY_PRICE1 = "Price";
    public static final String KEY_BONUS = "Bonus";
    public static final String KEY_DISCOUNTS = "Discounts";
    public static String DATABASE_TABLE = "DistDeviceMt000";
    public String Name;
    public String LatinName;
    public String Barcode;
    public String Unit;
    public double Price;
    public double Bonus;
    public double Discount;

    public Product() {
    }

    public Product( String name, String latinName, String barcode, String unit, double price, double bonus, double discounts) {
        Name = name;
        LatinName = latinName;
        Barcode = barcode;
        Unit = unit;
        Price = price;
        Bonus = bonus;
        Discount = discounts;
    }

    public static boolean SaveInDatabase(Context context, ArrayList<Product> products) {
        DBAdapter adapter = new DBAdapter(context);
        try {
            adapter.Open();
            DBAdapter.database.beginTransaction();
            String sql = "INSERT INTO " + DATABASE_TABLE + " (" +
                    KEY_NAME + "," +
                    KEY_LATIN_NAME + "," +
                    KEY_BARCODE + "," +
                    KEY_UNITY + "," +
                    KEY_PRICE1 + "," +
                    KEY_BONUS + "," +
                    KEY_DISCOUNTS +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?)";


            SQLiteStatement stmt = DBAdapter.database.compileStatement(sql);
            for (int i = 0; i < products.size(); i++) {
                stmt.bindString(1, products.get(i).Name);
                stmt.bindString(2, products.get(i).LatinName);
                stmt.bindString(3, products.get(i).Barcode);
                stmt.bindString(4, products.get(i).Unit);
                stmt.bindDouble(5, products.get(i).Price);
                stmt.bindDouble(6, products.get(i).Bonus);
                stmt.bindDouble(7, products.get(i).Discount);
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
                product.LatinName = cursor.getString(cursor.getColumnIndex(KEY_LATIN_NAME));
                product.Barcode = cursor.getString(cursor.getColumnIndex(KEY_BARCODE));
                product.Unit = cursor.getString(cursor.getColumnIndex(KEY_UNITY));
                product.Price = cursor.getDouble(cursor.getColumnIndex(KEY_PRICE1));
                product.Bonus = cursor.getDouble(cursor.getColumnIndex(KEY_BONUS));
                product.Discount = cursor.getDouble(cursor.getColumnIndex(KEY_DISCOUNTS));
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
            values.put(KEY_LATIN_NAME, product.LatinName);
            values.put(KEY_BARCODE, product.Barcode);
            values.put(KEY_UNITY, product.Unit);
            values.put(KEY_PRICE1, product.Price);
            values.put(KEY_BONUS, product.Bonus);
            values.put(KEY_DISCOUNTS, product.Discount);
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