package core;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.ListAdapter;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MapingDialog extends AlertDialog.Builder {
    public MapingDialog(Context context, ArrayList<String> rows) {
        super(context);
    }

    @Override
    public AlertDialog.Builder setAdapter(ListAdapter adapter, DialogInterface.OnClickListener listener) {
        return super.setAdapter(adapter, listener);
    }
}
