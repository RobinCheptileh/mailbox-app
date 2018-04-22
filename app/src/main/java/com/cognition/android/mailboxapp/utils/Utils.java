package com.cognition.android.mailboxapp.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.text.format.DateFormat;
import android.view.View;

import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

public class Utils {

    private Context mContext;

    public Utils(Context context) {
        this.mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    public boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) this.mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Display messages on the snackbar
     *
     * @param message String
     */
    public void showSnackbar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Get a random material color
     *
     * @return int
     */
    public int getRandomMaterialColor() {
        int returnColor = Color.GRAY;
        int arrayId = mContext.getResources().getIdentifier("material_colors", "array", mContext.getPackageName());

        if (arrayId != 0) {
            TypedArray colors = mContext.getResources().obtainTypedArray(arrayId);

            Random random = new Random();
            int index = random.nextInt(colors.length());
            returnColor = colors.getColor(index, Color.GRAY);
            colors.recycle();
        }
        return returnColor;
    }

    /**
     * Convert timestamp to date string dd/MM/yyyy
     *
     * @param time long
     * @return String
     */
    public String timestampToDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        return DateFormat.format("dd/MM/yyyy", cal).toString();
    }

    /**
     * Validates email addresses
     *
     * @param email String
     * @return boolean
     */
    public boolean isValidEmail(String email) {
        final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
        return Pattern.compile(EMAIL_PATTERN).matcher(email).matches();
    }
}
