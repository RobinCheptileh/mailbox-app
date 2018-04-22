package com.cognition.android.mailboxapp.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.cognition.android.mailboxapp.R;
import com.cognition.android.mailboxapp.utils.Utils;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import pub.devrel.easypermissions.EasyPermissions;

import static com.cognition.android.mailboxapp.activities.MainActivity.PREF_ACCOUNT_NAME;
import static com.cognition.android.mailboxapp.activities.MainActivity.REQUEST_AUTHORIZATION;
import static com.cognition.android.mailboxapp.activities.MainActivity.REQUEST_GOOGLE_PLAY_SERVICES;
import static com.cognition.android.mailboxapp.activities.MainActivity.SCOPES;
import static com.cognition.android.mailboxapp.activities.MainActivity.TAG;

public class ComposeActivity extends AppCompatActivity {

    CoordinatorLayout lytParent;
    Toolbar toolbar;
    AppCompatTextView txtFrom;
    AppCompatEditText txtTo, txtSubject, txtMessage;

    GoogleAccountCredential mCredential;
    Gmail mService;
    SharedPreferences sharedPref;
    Utils mUtils;

    boolean isSending = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        mService = null;
        sharedPref = ComposeActivity.this.getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE);
        mUtils = new Utils(ComposeActivity.this);

        String accountName = sharedPref.getString(PREF_ACCOUNT_NAME, null);
        if (accountName != null) {
            mCredential.setSelectedAccountName(accountName);

            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, mCredential)
                    .setApplicationName("MailBox App")
                    .build();

        } else {
            startActivity(new Intent(ComposeActivity.this, MainActivity.class));
            ActivityCompat.finishAffinity(ComposeActivity.this);
        }

        initViews();
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    triggerSendEmail();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ComposeActivity.this);
                    builder.setMessage(R.string.app_requires_auth);
                    builder.setPositiveButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            ActivityCompat.finishAffinity(ComposeActivity.this);
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Initialize views
     */
    private void initViews() {
        lytParent = findViewById(R.id.lytParent);
        toolbar = findViewById(R.id.toolbar);
        txtFrom = findViewById(R.id.txtFrom);
        txtTo = findViewById(R.id.txtTo);
        txtSubject = findViewById(R.id.txtSubject);
        txtMessage = findViewById(R.id.txtMessage);

        toolbar.setTitle(getString(R.string.compose));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.inflateMenu(R.menu.menu_compose);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_send:
                        triggerSendEmail();
                        return true;

                    default:
                        return false;
                }
            }
        });

        txtFrom.setText(mCredential.getSelectedAccountName());
    }

    /**
     * Trigger the SendEmailTask if all fields are okay
     */
    private void triggerSendEmail() {
        if (!txtTo.getText().toString().trim().isEmpty()
                && !txtSubject.getText().toString().trim().isEmpty()
                && !txtMessage.getText().toString().trim().isEmpty()) {
            if (mUtils.isValidEmail(txtTo.getText().toString().trim()))
                if (!isSending)
                    try {
                        new SendEmailTask(
                                createEmail(
                                        txtTo.getText().toString().trim(),
                                        mCredential.getSelectedAccountName(),
                                        txtSubject.getText().toString().trim(),
                                        txtMessage.getText().toString().trim()
                                )
                        ).execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.w(MainActivity.TAG, e);
                    }
                else
                    mUtils.showSnackbar(lytParent, getString(R.string.invalid_email));
        } else
            mUtils.showSnackbar(lytParent, getString(R.string.empty_fields));
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                ComposeActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * Create a MimeMessage using the parameters provided.
     *
     * @param to       Email address of the receiver.
     * @param from     Email address of the sender, the mailbox account.
     * @param subject  Subject of the email.
     * @param bodyText Body text of the email.
     * @return MimeMessage to be used to send email.
     * @throws MessagingException MessagingException
     */
    public static MimeMessage createEmail(String to, String from, String subject,
                                          String bodyText) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }

    /**
     * Create a Message from an email
     *
     * @param email Email to be set to raw of message
     * @return Message containing base64url encoded email.
     * @throws IOException        IOException
     * @throws MessagingException MessagingException
     */
    public static Message createMessageWithEmail(MimeMessage email)
            throws MessagingException, IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        email.writeTo(bytes);
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes.toByteArray());
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }


    /**
     * Send email in background
     */
    @SuppressLint("StaticFieldLeak")
    private class SendEmailTask extends AsyncTask<Void, Void, Message> {

        MimeMessage email;
        private Exception mLastError = null;

        SendEmailTask(MimeMessage email) {
            this.email = email;
        }

        @Override
        protected Message doInBackground(Void... voids) {
            Message sentMessage = null;
            isSending = true;

            try {
                mUtils.showSnackbar(lytParent, getString(R.string.sending));
                Message message = createMessageWithEmail(email);
                sentMessage = mService.users().messages().send("me", message).execute();
            } catch (Exception e) {
                Log.w(TAG, e);
                mLastError = e;
                cancel(true);
            }

            return sentMessage;
        }

        @Override
        protected void onPostExecute(Message output) {
            isSending = false;
            if (output != null) {
                mUtils.showSnackbar(lytParent, getString(R.string.message_sent));
                onBackPressed();
            } else
                mUtils.showSnackbar(lytParent, getString(R.string.an_error_occurred));
        }

        @Override
        protected void onCancelled() {
            isSending = false;
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                } else {
                    mUtils.showSnackbar(lytParent, getString(R.string.an_error_occurred));
                }
            } else {
                mUtils.showSnackbar(lytParent, getString(R.string.an_error_occurred));
            }
        }
    }
}
