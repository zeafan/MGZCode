package com.zeafan.mgzcode.login;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.zeafan.mgzcode.R;
import core.GlobalClass;
import date.Product;
import ru.katso.livebutton.LiveButton;
import setting.SettingsActivity;

public class MainActivity extends AppCompatActivity {
    LiveButton BtnScan;
    LinearLayout linear;
    ImageButton IbPrice;
    ImageButton IbName;
    ImageButton IbUnit;
    ImageButton IbNote;
    boolean check =true;
    TextView price,name,unit,note;
    Product selectedProduct;
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private int ScanID = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        initViews();
        action();
        GlobalClass.UpdateSetting(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.close_app:
                finish();
                break;
            case R.id.Setting:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.cart:

                break;
            case R.id.edit:
                if(check) {
                    check = false;
                    IbUnit.setVisibility(View.VISIBLE);
                    IbPrice.setVisibility(View.VISIBLE);
                    IbNote.setVisibility(View.VISIBLE);
                    IbName.setVisibility(View.VISIBLE);
                }else {
                    check = true;
                    IbUnit.setVisibility(View.GONE);
                    IbPrice.setVisibility(View.GONE);
                    IbNote.setVisibility(View.GONE);
                    IbName.setVisibility(View.GONE);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
 }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void initViews() {
        BtnScan = findViewById(R.id.Scan);
        BtnScan = findViewById(R.id.Scan);
        name = findViewById(R.id.name);
        price = findViewById(R.id.price);
        note = findViewById(R.id.note);
        unit = findViewById(R.id.unit);
        BtnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scan(ScanID);
            }
        });
        linear = findViewById(R.id.linear);
        linear.setVisibility(View.GONE);
        IbPrice = findViewById(R.id.Ib_price);
        IbName = findViewById(R.id.Ib_name);
        IbUnit = findViewById(R.id.Ib_unit);
        IbNote = findViewById(R.id.Ib_note);
    }
    public void scan(int ResultKey) {
        try {
            Intent intent = new Intent(ACTION_SCAN);
            startActivityForResult(intent, ResultKey);
        } catch (ActivityNotFoundException anfe) {
            try {
                GlobalClass.installBarcodeScannerApp(MainActivity.this);
            } catch (Exception e) {
                Toast.makeText(this, R.string.problem, Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == ScanID && resultCode == MainActivity.RESULT_OK) {
            String contents = intent.getStringExtra("SCAN_RESULT");
            selectedProduct = Product.GetByBarcode(MainActivity.this,contents);
            if(selectedProduct!=null){
                setValues(selectedProduct);
            }else {
                Toast.makeText(this, R.string.no_product, Toast.LENGTH_SHORT).show();
            }
        }
    }
    public enum EditType{
        price,
        name,
        note,
        unit
    }
void action(){
        IbName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = name.getText().toString();
                if(GlobalClass.Password.isEmpty())
                {
                    ShowEditDialog(value,EditType.name);
                }else {
                    ShowConfirmPassword(value, EditType.name);
                }
            }
        });
    IbNote.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String value = note.getText().toString();
            if(GlobalClass.Password.isEmpty())
            {
                ShowEditDialog(value,EditType.note);
            }else {
                ShowConfirmPassword(value, EditType.note);
            }
        }
    });
    IbUnit.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String value = unit.getText().toString();
            if(GlobalClass.Password.isEmpty())
            { ShowEditDialog(value,EditType.unit);
            }else {
                ShowConfirmPassword(value, EditType.unit);
            }
        }
    });
    IbPrice.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String value = price.getText().toString();
            if(GlobalClass.Password.isEmpty())
            {
                ShowEditDialog(value,EditType.price);
            }else {
                ShowConfirmPassword(value, EditType.price);
            }
        }
    });
}
    private void ShowEditDialog(final String value, final EditType type) {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setText(value);
        if(type==EditType.price){
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        alert.setTitle(R.string.update);
        alert.setView(input);
        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!input.getText().toString().isEmpty()){
                    if(Product.UpdateValue(MainActivity.this,input.getText().toString(),type,selectedProduct.Barcode)){
                        setNewValue(input.getText().toString(),type);
                    }
                }
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    private void setNewValue(String value, EditType type) {
        switch (type)
        {
            case name:
                name.setText(value);
                break;
            case note:
                note.setText(value);
                break;
            case unit:
                unit.setText(value);
                break;
            case price:
                price.setText(value);
                break;
        }
    }


    private void ShowConfirmPassword(final String value, final EditType name) {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alert.setView(input);
        alert.setTitle(R.string.password);
        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = input.getText().toString();
                if(password.equals(GlobalClass.Password)){
                    ShowEditDialog(value,name);
                }else {
                    Toast.makeText(MainActivity.this, R.string.error_password, Toast.LENGTH_SHORT).show();
                }
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    private void setValues(Product selectedProduct) {
        linear.setVisibility(View.VISIBLE);
        name.setText(selectedProduct.Name);
        note.setVisibility(selectedProduct.Note.isEmpty()?View.GONE:View.VISIBLE);
        unit.setVisibility(selectedProduct.Unit.isEmpty()?View.GONE:View.VISIBLE);
        note.setText(selectedProduct.Note);
        unit.setText(selectedProduct.Unit);
        price.setText(String.valueOf(selectedProduct.Price)+" "+GlobalClass.CurrenyName);
    }
}
