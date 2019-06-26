package core;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class GlobalClass {
    public static final String DATE_FORMAT = "dd-MM-yyyy";
    public static final String BarcodeScannerApkFileName = Environment.getExternalStorageDirectory().getPath() + File.separator + "barcode_scanner.apk";


    public static String getVersionName(Context context) {
        String versionName = "N/A";

        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = null;
            info = manager.getPackageInfo(context.getPackageName(), 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {GlobalClass.SendExceptionToFireBase(e);
            e.printStackTrace();
            return versionName;
        }

        return versionName;
    }
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static Date StringToDate(String dateString, String format, Locale locale) {
        SimpleDateFormat df = new SimpleDateFormat(format, locale);
        Date convertedDate = new Date();

        try {
            convertedDate = df.parse(dateString);
        } catch (Exception e) {GlobalClass.SendExceptionToFireBase(e);
            e.printStackTrace();
        }

        return convertedDate;
    }
public static void SendExceptionToFireBase(Exception ex)
{
 //   Crashlytics.logException(ex);
  //  FirebaseCrash.report(ex);
}
    public static String DateToString(Context context, Date date) {
        SimpleDateFormat df;

        df = new SimpleDateFormat(GlobalClass.DATE_FORMAT, context.getResources().getConfiguration().locale);
        return df.format(date);
    }

    public static void installBarcodeScannerApp(Context context) {
        File DbFile = new File("mnt/sdcard/barcode_scanner_4.7.3.apk");

        try {
            int length;
            DbFile.createNewFile();
            InputStream inputStream = context.getAssets().open("barcode_scanner.apk");
            FileOutputStream fOutputStream = new FileOutputStream(DbFile);
            byte[] buffer = new byte[inputStream.available()];

            while ((length = inputStream.read(buffer)) > 0) {
                fOutputStream.write(buffer, 0, length);
            }
            fOutputStream.flush();
            fOutputStream.close();
            inputStream.close();
        } catch (Exception ignored) {
        }
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.setDataAndType(Uri.fromFile(new File(BarcodeScannerApkFileName)), "application/vnd.android.package-archive");
        installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(installIntent);
    }
}
