package core;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.content.ContextCompat;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.zeafan.mgzcode.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SharedPreferences _sharedPreferences;
    private Preference ImpoertFile;
    final int READ_EXTERNAL_STORAGE_PERMISSION_CODE=100;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setHasOptionsMenu(true);
            addPreferencesFromResource(R.xml.preferences);
            _sharedPreferences = getPreferenceManager().getSharedPreferences();
        }
        _sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        ImpoertFile=findPreference("import_file");
        ImpoertFile.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M&& ContextCompat.checkSelfPermission(getActivity().getApplication(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                    //ask for permission
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION_CODE);

                }else {
                    createListDialog(getActivity());
                }
                return false;
            }
        });
    }
    private ArrayList<String> getFiles(File[] files) {
        ArrayList<String> items = new ArrayList<>();
        try {

            for (File file : files) {
                if (file.isDirectory()) {
                    getFiles(file.listFiles());
                } else {
                    if (file.getPath().endsWith(".xls")||file.getPath().endsWith(".xlsx")) {
                        items.add(file.getPath());
                    }
                }
            }
        } catch (Exception e) {
            GlobalClass.SendExceptionToFireBase(e);
        }
        return items;
    }
    private void createListDialog(final Activity context) {
            final ArrayList<String> items2 = getFiles(new File(Environment.getExternalStorageDirectory().getAbsolutePath()).listFiles());
        if(items2.size()==0)
        {
            Toast.makeText(context,R.string.on_file, Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
        builderSingle.setTitle(R.string.select_file);
        ArrayList<String> items=FilterFilesPathes(items2);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.select_dialog_singlechoice, items);
        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    InputStream in = new FileInputStream(items2.get(which));
                    Workbook wb =Workbook.getWorkbook(in);
                    int size = wb.getSheets().length;

                    if(size>1){
                        CreateDialog_SelectSheets(context,wb);
                    }else {
                        Sheet s = wb.getSheet(0);
                        ArrayList<String> ColumesNames = GetColumesName(s);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        builderSingle.show();
    }

    private ArrayList<String> GetColumesName(Sheet s) {
        ArrayList<String> columesNames=new ArrayList<>();

        for (int i=0;i<=s.getColumns();i++)
        {
            columesNames.add(s.getCell(0,i).getContents());
        }
        return columesNames;
    }

    private void CreateDialog_SelectSheets(final Activity context, final Workbook wb) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
        builderSingle.setTitle(R.string.select_sheet);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.select_dialog_singlechoice, wb.getSheetNames());
        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                        Sheet s = wb.getSheet(which);
                        ArrayList<String> ColumesNames = GetColumesName(s);
                    Toast.makeText(context, ColumesNames.get(0), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        builderSingle.show();
    }

    private ArrayList<String> FilterFilesPathes(ArrayList<String> items2) {
        ArrayList<String> items=new ArrayList<>();
        for(String path:items2)
        {
            String[]folder=path.split("/");
            items.add(folder[folder.length-1]);
        }
        return items;
    }
    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        try {
            switch (key) {
                case "scan_method": {
                  break;
                }
            }
        } catch (Exception ignored) {
            GlobalClass.SendExceptionToFireBase(ignored);
        }

        // TODO

    }
}