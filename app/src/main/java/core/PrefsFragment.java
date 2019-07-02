package core;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.zeafan.mgzcode.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import jxl.Sheet;
import jxl.Workbook;

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
    private void getFiles(File[] files, ArrayList<String> items) {
        try {
            for (File file : files) {
                if (file.isDirectory()) {
                    getFiles(file.listFiles(), items);
                } else {
                    if (file.getPath().endsWith(".xls")) {
                        items.add(file.getPath());
                    }
                }
            }
        } catch (Exception e) {
            GlobalClass.SendExceptionToFireBase(e);
        }
    }
    private void createListDialog(final Activity context) {
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            Toast.makeText(context, R.string.no_sd_card, Toast.LENGTH_LONG).show();
            return;
        }
        ArrayList<String> items = new ArrayList<>();
         getFiles(new File(Environment.getExternalStorageDirectory().getAbsolutePath()).listFiles(),items);
        if(items.size()==0)
        {
            Toast.makeText(context,R.string.on_file, Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
        builderSingle.setTitle(R.string.select_file);
      ArrayList <String> FileNames = FilterFilesPathes(items);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.select_dialog_singlechoice, FileNames);
        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
      final   ArrayList <String> Files = items;
        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    InputStream in = new FileInputStream(Files.get(which));
                    Workbook wb =Workbook.getWorkbook(in);
                    int size = wb.getSheets().length;

                    if(size>1){
                        CreateDialog_SelectSheets(context,wb);
                    }else {
                        Sheet s = wb.getSheet(0);
                        ArrayList<String> ColumesNames = GetColumesName(s);
                        if(ColumesNames.size()>2)
                        {
                            ShowDialogToSelectLinks(ColumesNames,s);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        builderSingle.show();
    }

    private void ShowDialogToSelectLinks(ArrayList<String> columesNames, Sheet s) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_links,null);
        Spinner Sp_name =v.findViewById(R.id.Sp_product_name);
        Spinner Sp_barCode =v.findViewById(R.id.Sp_product_barcode);
        Spinner Sp_Unit =v.findViewById(R.id.Sp_product_unit);
        Spinner Sp_note  =v.findViewById(R.id.Sp_product_Note);
        Spinner Sp_Price =v.findViewById(R.id.Sp_price);
        columesNames.add(0,getString(R.string.select_column));
        ArrayList<String> copy_lst = new ArrayList<>();
        copy_lst.addAll(columesNames);
        ArrayAdapter adapter = new ArrayAdapter(getActivity(),android.R.layout.simple_spinner_item,copy_lst);
        Sp_name.setAdapter(adapter);
        Sp_barCode.setAdapter(adapter);
        Sp_note.setAdapter(adapter);
        Sp_Price.setAdapter(adapter);
        Sp_Unit.setAdapter(adapter);
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        Sp_name.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        builder.setView(v).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setTitle(R.string.relate_operation).setMessage(R.string.info_relate)
                .show();

    }

    private void setAdapter(ArrayAdapter adapter, Spinner sp_name, Spinner sp_barCode, Spinner sp_note, Spinner sp_price, Spinner sp_unit) {

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
                    if(ColumesNames.size()>2)
                    {
                        ShowDialogToSelectLinks(ColumesNames,s);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        builderSingle.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==READ_EXTERNAL_STORAGE_PERMISSION_CODE)
        {
            createListDialog(getActivity());
        }

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