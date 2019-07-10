package com.zeafan.mgzcode.login;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.zeafan.mgzcode.R;

import java.io.IOException;

import core.GlobalClass;
import date.Product;
import ru.katso.livebutton.LiveButton;
import setting.SettingsActivity;

public class MainActivity extends AppCompatActivity {
    SurfaceView surfaceView;
    CameraSource cameraSource;
    LiveButton BtnScan;
    LinearLayout linear;
    ImageView imClose;
    ImageView imSetting;
    TextView price,name,unit,note;
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private int ScanID = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        initViews();

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
        name =findViewById(R.id.name);
        price =findViewById(R.id.price);
        note =findViewById(R.id.note);
        unit =findViewById(R.id.unit);
        BtnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scan(ScanID);
            }
        });
        linear=findViewById(R.id.linear);
        linear.setVisibility(View.GONE);
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
            Product selectedProduct = Product.GetByBarcode(MainActivity.this,contents);
            if(selectedProduct!=null){
                setValues(selectedProduct);
            }
        }
    }

    private void setValues(Product selectedProduct) {
        linear.setVisibility(View.VISIBLE);
        name.setText(selectedProduct.Name);
        note.setVisibility(selectedProduct.Note.isEmpty()?View.GONE:View.VISIBLE);
        unit.setVisibility(selectedProduct.Unit.isEmpty()?View.GONE:View.VISIBLE);
        note.setText(getString(R.string.notes) +": "+ selectedProduct.Note);
        unit.setText(getString(R.string.unit) +": "+selectedProduct.Unit);
        price.setText(String.valueOf(selectedProduct.Price));
    }
}
