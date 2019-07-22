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
import android.widget.Toast;
import com.zeafan.mgzcode.R;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import date.Product;
import jxl.Sheet;
import jxl.Workbook;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SharedPreferences _sharedPreferences;
    private Preference ImpoertFile;
    ArrayList<cell_link> Links = new ArrayList<>();
    final int READ_EXTERNAL_STORAGE_PERMISSION_CODE = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setHasOptionsMenu(true);
            addPreferencesFromResource(R.xml.preferences);
            _sharedPreferences = getPreferenceManager().getSharedPreferences();
        }
        _sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        ImpoertFile = findPreference("import_file");
        ImpoertFile.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(getActivity().getApplication(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    //ask for permission
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION_CODE);

                } else {
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
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(context, R.string.no_sd_card, Toast.LENGTH_LONG).show();
            return;
        }
        ArrayList<String> items = new ArrayList<>();
        getFiles(new File(Environment.getExternalStorageDirectory().getAbsolutePath()).listFiles(), items);
        if (items.size() == 0) {
            Toast.makeText(context, R.string.on_file, Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
        builderSingle.setTitle(R.string.select_file);
        ArrayList<String> FileNames = FilterFilesPathes(items);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.select_dialog_singlechoice, FileNames);
        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final ArrayList<String> Files = items;
        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    InputStream in = new FileInputStream(Files.get(which));
                    Workbook wb = Workbook.getWorkbook(in);
                    int size = wb.getSheets().length;

                    if (size > 1) {
                        CreateDialog_SelectSheets(context, wb);
                    } else {
                        Sheet s = wb.getSheet(0);
                        ArrayList<cell> ColumesNames = GetColumesName(s);
                        if (ColumesNames.size() > 2) {
                            ShowDialogToSelectLinks(ColumesNames, s);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        builderSingle.show();
    }

    private void ShowDialogToSelectLinks(final ArrayList<cell> columnsNames, final Sheet s) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_links, null);
        Spinner Sp_name = v.findViewById(R.id.Sp_product_name);
        Spinner Sp_barCode = v.findViewById(R.id.Sp_product_barcode);
        Spinner Sp_Unit = v.findViewById(R.id.Sp_product_unit);
        Spinner Sp_note = v.findViewById(R.id.Sp_product_Note);
        Spinner Sp_Price = v.findViewById(R.id.Sp_price);
        columnsNames.add(0, new cell(-1, getString(R.string.select_column)));
        final ArrayList<String> copy_lst = new ArrayList<>();
        copy_lst.addAll(getList(columnsNames));
        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, copy_lst);
        Sp_name.setAdapter(adapter);
        Sp_barCode.setAdapter(adapter);
        Sp_note.setAdapter(adapter);
        Sp_Price.setAdapter(adapter);
        Sp_Unit.setAdapter(adapter);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //////////////
        Sp_Unit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position != 0) {
                    int index = getIndex(copy_lst.get(position), columnsNames);
                    Links = deleteIndexFromList(index, Links);
                    if (index != -1) {
                        cell_link unit = new cell_link(index, Product.KEY_UNIT, copy_lst.get(position));
                        Links.add(unit);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        ///////////////
        Sp_barCode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position != 0) {
                    int index = getIndex(copy_lst.get(position), columnsNames);
                    Links = deleteIndexFromList(index, Links);
                    if (index != -1) {
                        cell_link barcode = new cell_link(index, Product.KEY_BARCODE, copy_lst.get(position));
                        Links.add(barcode);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });
        ////////////
        Sp_note.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position != 0) {
                    int index = getIndex(copy_lst.get(position), columnsNames);
                    Links = deleteIndexFromList(index, Links);
                    if (index != -1) {
                        cell_link note = new cell_link(index, Product.KEY_Note, copy_lst.get(position));
                        Links.add(note);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });
        ///////////////////////////
        Sp_Price.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position != 0) {
                    int index = getIndex(copy_lst.get(position), columnsNames);
                    Links = deleteIndexFromList(index, Links);
                    if (index != -1) {
                        cell_link price = new cell_link(index, Product.KEY_PRICE, copy_lst.get(position));
                        Links.add(price);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });
        /////////////////////////
        Sp_name.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position != 0) {
                    int index = getIndex(copy_lst.get(position), columnsNames);
                    Links = deleteIndexFromList(index, Links);
                    if (index != -1) {
                        cell_link name = new cell_link(index, Product.KEY_NAME, copy_lst.get(position));
                        Links.add(name);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });
        builder.setView(v).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Links.size() > 2) {
                    ArrayList<Product> products = SaveProducts(Links, s);
                    ShowConfirmDialog(products);

                }
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setTitle(R.string.relate_operation).setMessage(R.string.info_relate)
                .show();

    }

    private void ShowConfirmDialog(final ArrayList<Product> products) {
        AlertDialog.Builder al = new AlertDialog.Builder(getActivity());
        al.setMessage(R.string.confirm_save);
        al.setTitle(R.string.load_data);
        al.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Product.DeleteAll2(getActivity(), products);
                if (Product.SaveInDatabase(getActivity(), products)) {
                    Toast.makeText(getActivity(), R.string.done, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), R.string.error_save, Toast.LENGTH_SHORT).show();
                }
            }
        });
        al.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        al.setNeutralButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Product.DeleteAll(getActivity());
                if (Product.SaveInDatabase(getActivity(), products)) {
                    Toast.makeText(getActivity(), R.string.done, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), R.string.error_save, Toast.LENGTH_SHORT).show();
                }
            }
        });
        al.show();
    }

    private ArrayList<cell_link> deleteIndexFromList(int index, ArrayList<cell_link> links) {
        ArrayList<cell_link> new_links = new ArrayList<>();
        for (cell_link c : links) {
            if (c.index != index) {
                new_links.add(c);
            }
        }
        return new_links;
    }

    private int getIndex(String s, ArrayList<cell> columnsNames) {
        for (cell c : columnsNames) {
            if (c.value.equals(s)) {
                return c.index;
            }
        }
        return -1;
    }

    private Collection<? extends String> getList(ArrayList<cell> columnsNames) {
        ArrayList<String> lst = new ArrayList<>();
        for (cell c : columnsNames) {
            lst.add(c.value);
        }
        return lst;
    }

    private ArrayList<Product> SaveProducts(ArrayList<cell_link> links, Sheet s) {
        int index_name = getIndex2(links, Product.KEY_NAME);
        int index_unit = getIndex2(links, Product.KEY_UNIT);
        int index_price = getIndex2(links, Product.KEY_PRICE);
        int index_note = getIndex2(links, Product.KEY_Note);
        int index_barcode = getIndex2(links, Product.KEY_BARCODE);
        ArrayList products = new ArrayList();
        for (int i = 1; i < s.getRows(); i++) {
            Product product = new Product();
            product.Name = index_name != -1 ? s.getCell(index_name, i).getContents() : "";
            product.Note = index_note != -1 ? s.getCell(index_note, i).getContents() : "";
            product.Barcode = index_barcode != -1 ? s.getCell(index_barcode, i).getContents() : "";
            try {
                product.Price = index_price != -1 ? Double.valueOf(s.getCell(index_price, i).getContents()) : 0.0;
            } catch (Exception ex) {
                product.Price = 0;
            }
            product.Unit = index_unit != -1 ? s.getCell(index_unit, i).getContents() : "";
            products.add(product);
        }
        return products;
    }

    private int getIndex2(ArrayList<cell_link> links, String keyName) {
        for (cell_link c : links) {
            if (c.key.equals(keyName)) {
                return c.index;
            }
        }
        return -1;
    }

    private void setAdapter(ArrayAdapter adapter, Spinner sp_name, Spinner sp_barCode, Spinner sp_note, Spinner sp_price, Spinner sp_unit) {

    }

    class cell {
        public cell(int index, String value) {
            this.index = index;
            this.value = value;
        }

        int index;
        String value;
    }

    class cell_link {
        public cell_link(int index, String key, String value) {
            this.index = index;
            this.key = key;
            this.value = value;
        }

        int index;
        String key;
        String value;
    }

    private ArrayList<cell> GetColumesName(Sheet s) {
        ArrayList<cell> columesNames = new ArrayList<>();
        for (int i = 0; i < s.getColumns(); i++) {
            columesNames.add(new cell(i, s.getCell(i, 0).getContents()));
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
                    ArrayList<cell> ColumesNames = GetColumesName(s);
                    if (ColumesNames.size() > 2) {
                        ShowDialogToSelectLinks(ColumesNames, s);
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
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION_CODE) {
            createListDialog(getActivity());
        }

    }

    private ArrayList<String> FilterFilesPathes(ArrayList<String> items2) {
        ArrayList<String> items = new ArrayList<>();
        for (String path : items2) {
            String[] folder = path.split("/");
            items.add(folder[folder.length - 1]);
        }
        return items;
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        try {
            switch (key) {
                case "password": {
                    GlobalClass.Password = sharedPreferences.getString(key, "");
                    break;
                }
                case "currency": {
                    GlobalClass.CurrenyName = sharedPreferences.getString(key, "");
                    break;
                }
            }
        } catch (Exception ignored) {
            GlobalClass.SendExceptionToFireBase(ignored);
        }

        // TODO

    }
}