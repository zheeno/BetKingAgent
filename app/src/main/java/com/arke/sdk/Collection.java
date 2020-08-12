package com.arke.sdk;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.arke.sdk.util.printer.Printer;
import com.usdk.apiservice.aidl.printer.ASCScale;
import com.usdk.apiservice.aidl.printer.ASCSize;
import com.usdk.apiservice.aidl.printer.AlignMode;
import com.usdk.apiservice.aidl.printer.HZScale;
import com.usdk.apiservice.aidl.printer.HZSize;
import com.usdk.apiservice.aidl.printer.OnPrintListener;

import org.json.JSONException;
import org.json.JSONObject;
import com.alibaba.fastjson.JSON;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.ProgressDialog;

public class Collection extends AppCompatActivity {

    private Context context;

    private static final int FONT_SIZE_SMALL = 0;
    private static final int FONT_SIZE_NORMAL = 1;
    private static final int FONT_SIZE_LARGE = 2;
    private static final String divider = "-----------------------------------";
    private static final String single_divider = "***********************************";

    /**
     * Alert dialog.
     */
    private ProgressDialog progDialog;
    private String amount;
    private String revType;
    private String collectorName;
    private String collectorType;
    private String collectorRef;
    private String tranRef;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getRevType() {
        return revType;
    }

    public void setRevType(String revType) {
        this.revType = revType;
    }

    public String getCollectorName() {
        return collectorName;
    }

    public void setCollectorName(String collectorName) {
        this.collectorName = collectorName;
    }

    public String getCollectorType() {
        return collectorType;
    }

    public void setCollectorType(String collectorType) {
        this.collectorType = collectorType;
    }

    public String getCollectorRef() {
        return collectorRef;
    }

    public void setCollectorRef(String collectorRef) {
        this.collectorRef = collectorRef;
    }

    public String getTranRef() {
        return tranRef;
    }

    public void setTranRef(String tranRef) {
        this.tranRef = tranRef;
    }

    /**
     * Constructor.
     */
    Collection(Context context) {
        this.context = context;
        this.progDialog = new ProgressDialog(context);
    }


    public void printSlip() {
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        // Show dialog
        showDialog(R.string.waiting_for_printing, false);
        try {
            // Get status
            Printer.getInstance().getStatus();
            Printer.getInstance().setPrnGray(5);

            // Add logo
            Printer.getInstance().addImage(AlignMode.CENTER, readAssetsFile("ogun_logo_large.bmp"));
            setFontSpec(FONT_SIZE_NORMAL);
            Printer.getInstance().addText(AlignMode.CENTER, "Abeokuta South Local Government");

            setFontSpec(FONT_SIZE_NORMAL);
            Printer.getInstance().addText(AlignMode.CENTER, divider);
            setFontSpec(FONT_SIZE_LARGE);
            Printer.getInstance().addText(AlignMode.CENTER, "NGN "+formatAmount(Double.parseDouble(this.amount), false));

            setFontSpec(FONT_SIZE_NORMAL);
            Printer.getInstance().addText(AlignMode.CENTER, divider);
            Printer.getInstance().addText(AlignMode.LEFT, formatAlignedJustified("Revenue Type:", this.revType));
            Printer.getInstance().addText(AlignMode.LEFT, formatAlignedJustified("Transaction Ref.:", this.tranRef));
            Printer.getInstance().addText(AlignMode.LEFT, formatAlignedJustified("Time:", currentDate+" "+currentTime));

            Printer.getInstance().addText(AlignMode.CENTER, divider);
            Printer.getInstance().addText(AlignMode.LEFT, formatAlignedJustified("Collector:", this.collectorName));
            Printer.getInstance().addText(AlignMode.LEFT, formatAlignedJustified("Collector Type:", this.collectorType));
            Printer.getInstance().addText(AlignMode.LEFT, formatAlignedJustified("Collector Ref.:", this.collectorRef));

            Printer.getInstance().addText(AlignMode.CENTER, divider);
            setFontSpec(FONT_SIZE_LARGE);
            Printer.getInstance().addText(AlignMode.CENTER, "SUCCESSFUL");
            setFontSpec(FONT_SIZE_NORMAL);
            Printer.getInstance().addText(AlignMode.CENTER, divider);
            setFontSpec(FONT_SIZE_SMALL);
            Printer.getInstance().addText(AlignMode.CENTER, "Abeokuta South Local Government");
            Printer.getInstance().addText(AlignMode.CENTER, "Revenue Collection");
            Printer.getInstance().addText(AlignMode.CENTER, "For more information");
            Printer.getInstance().addText(AlignMode.CENTER, "Mail: support@lg.ogunstate.com.ng");

            // Add whitespace
            Printer.getInstance().feedLine(6);
            // Start printing
            Printer.getInstance().start(new OnPrintListener.Stub() {

                @Override
                public void onFinish() throws RemoteException {
                    Log.d("Print", "----- onFinish -----");

                            hideDialog();
//                    Toast.makeText(context.getApplicationContext(), R.string.succeed, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(int error) throws RemoteException {
                    Log.d("Print", "----- onError ----");

                            hideDialog();
//                    Toast.makeText(context.getApplicationContext(), Printer.getErrorId(error), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public String textToNewLine(String str){
        String strArray[] = str.split("");
        String newStr = ""; int count = 0;
        for(int i=0; i < strArray.length; i++){
            newStr += strArray[i];
            if(count > 30){
                newStr += "\n";
                count = 0;
            }
            count = count+1;
        }
        return newStr;
    }

    private String formatAmount(double totalAuthAmount, boolean curSym) {
        String Currency = "NGN";
        String Separator = ",";
        Boolean Spacing = false;
        Boolean Delimiter = false;
        Boolean Decimals = true;
        String currencyFormat = "";
        if (Spacing) {
            if (Delimiter) {
                currencyFormat = ". ";
            } else {
                currencyFormat = " ";
            }
        } else if (Delimiter) {
            currencyFormat = ".";
        } else {
            currencyFormat = "";
        }
        if(curSym){
            currencyFormat = Currency+currencyFormat;
        }

        String tformatted = NumberFormat.getCurrencyInstance().format(totalAuthAmount / 1.0D).replace(NumberFormat.getCurrencyInstance().getCurrency().getSymbol(), currencyFormat);
        return tformatted;
    }


    /**
     * Hide dialog.
     */
    private void hideDialog() {
        progDialog.cancel();
    }

    private void showDialog(int resId, boolean cancelable) {
        progDialog.setMessage(context.getString(resId));
        progDialog.show();
        progDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(cancelable);
        if (progDialog.getWindow() != null) {
            // // TODO: 2017/9/8 屏蔽的Home的方法在 4.0 以后已经不再支持，在 C10  Android 7.0 上会报错。
            /** 为了更好地实现应用屏蔽HOME键的功能而又不引起异常，现已在P990及W280PV2的版本上新增了一个接口，
             该接口可以让应用的window屏蔽HOME键及APP_SWITCH键（就是调出近期应用的键），调用方法如下：

             @Override
             public void onAttachedToWindow() {
             super.onAttachedToWindow();
             Window win = getWindow();
             try {
             Class<?> cls = win.getClass();
             final Class<?>[] PARAM_TYPES = new Class[] {int.class};
             Method method = cls.getMethod("addCustomFlags", PARAM_TYPES);
             method.setAccessible(true);
             method.invoke(win, new Object[] {0x00000001});
             } catch (Exception e) {
             // handle the error here.
             }
             }

             该方法在2015.03.17号的烧片版本才有效。以后的应用屏蔽HOME键，请尽量使用此方法，不要再使用TYPE_KEYGUARD_DIALOG方式。
             **/
//            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        }
    }


    /**
     * Set font spec.
     */
    private void setFontSpec(int fontSpec) throws RemoteException {
        switch (fontSpec) {
            case FONT_SIZE_SMALL:
                Printer.getInstance().setHzSize(HZSize.DOT16x16);
                Printer.getInstance().setHzScale(HZScale.SC1x1);
                Printer.getInstance().setAscSize(ASCSize.DOT16x8);
                Printer.getInstance().setAscScale(ASCScale.SC1x1);
                break;

            case FONT_SIZE_NORMAL:
                Printer.getInstance().setHzSize(HZSize.DOT24x24);
                Printer.getInstance().setHzScale(HZScale.SC1x1);
                Printer.getInstance().setAscSize(ASCSize.DOT24x12);
                Printer.getInstance().setAscScale(ASCScale.SC1x1);
                break;

            case FONT_SIZE_LARGE:
                Printer.getInstance().setHzSize(HZSize.DOT24x24);
                Printer.getInstance().setHzScale(HZScale.SC1x2);
                Printer.getInstance().setAscSize(ASCSize.DOT24x12);
                Printer.getInstance().setAscScale(ASCScale.SC1x2);
                break;
        }
    }

    private byte[] readAssetsFile(String fileName) throws RemoteException {

        InputStream input = null;
        try {
            input = this.context.getAssets().open(fileName);
            byte[] buffer = new byte[input.available()];
            int size = input.read(buffer);
            if (size == -1) {
                throw new RemoteException(this.context.getString(R.string.read_fail));
            }
            return buffer;
        } catch (IOException e) {
            throw new RemoteException(e.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private static byte[] readAssetsFileStorage(String fileName) throws RemoteException {
        FileInputStream input = null;
        Object var5;
        try {
            File f = new File(fileName);
            if (f == null || !f.exists()) {
                Object var18 = null;
                return (byte[])var18;
            }

            input = new FileInputStream(f);
            byte[] buffer = new byte[input.available()];
            int size = input.read(buffer);
            if (size != -1) {
                byte[] var19 = buffer;
                return var19;
            }

            var5 = null;
        } catch (IOException var16) {
            throw new RemoteException(var16.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException var15) {

                }
            }

        }

        return (byte[])var5;
    }


    private static String formatAlignedJustified(String left, String right) {
        if (left != null && right != null) {
            int leftlen = left.length();
            int rightlen = right.length();
            int space = 32 - (leftlen + rightlen);
            String sp = "";

            for(int i = 0; i < space; ++i) {
                sp = sp + " ";
            }

            return left + sp + right;
        } else {
            return "";
        }
    }

}
