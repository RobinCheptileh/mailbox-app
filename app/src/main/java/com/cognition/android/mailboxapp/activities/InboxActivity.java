package com.cognition.android.mailboxapp.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.cognition.android.mailboxapp.R;
import com.cognition.android.mailboxapp.models.Message;
import com.cognition.android.mailboxapp.utils.MessagesAdapter;
import com.cognition.android.mailboxapp.utils.Utils;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.EasyPermissions;

import static com.cognition.android.mailboxapp.activities.MainActivity.PREF_ACCOUNT_NAME;
import static com.cognition.android.mailboxapp.activities.MainActivity.REQUEST_AUTHORIZATION;
import static com.cognition.android.mailboxapp.activities.MainActivity.REQUEST_GOOGLE_PLAY_SERVICES;
import static com.cognition.android.mailboxapp.activities.MainActivity.SCOPES;

public class InboxActivity extends AppCompatActivity {

    CoordinatorLayout lytParent;
    Toolbar toolbar;
    SwipeRefreshLayout refreshMessages;
    RecyclerView listMessages;
    FloatingActionButton fabCompose;

    List<Message> messageList;
    MessagesAdapter messagesAdapter;

    GoogleAccountCredential mCredential;
    Gmail mService;
    SharedPreferences sharedPref;
    Utils mUtils;

    String pageToken = null;
    int itemCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        mService = null;
        sharedPref = InboxActivity.this.getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE);
        mUtils = new Utils(InboxActivity.this);

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
            startActivity(new Intent(InboxActivity.this, MainActivity.class));
            ActivityCompat.finishAffinity(InboxActivity.this);
        }

        messageList = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(InboxActivity.this, messageList);

        initViews();
        new GetEmailsTask().execute();
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
                    new GetEmailsTask().execute();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(InboxActivity.this);
                    builder.setMessage(R.string.app_requires_auth);
                    builder.setPositiveButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            ActivityCompat.finishAffinity(InboxActivity.this);
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

    private void initViews() {
        lytParent = findViewById(R.id.lytParent);
        toolbar = findViewById(R.id.toolbar);
        refreshMessages = findViewById(R.id.refreshMessages);
        listMessages = findViewById(R.id.listMessages);
        fabCompose = findViewById(R.id.fabCompose);

        toolbar.inflateMenu(R.menu.menu_main);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) toolbar.getMenu().findItem(R.id.action_search).getActionView();
        searchView.setQueryHint("Search");
        searchView.setSearchableInfo(searchManager != null ? searchManager.getSearchableInfo(getComponentName()) : null);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                messagesAdapter.getFilter().filter(query);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                messagesAdapter.getFilter().filter(newText);

                return true;
            }
        });

        refreshMessages.setColorSchemeResources(R.color.colorPrimary);
        refreshMessages.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                InboxActivity.this.pageToken = null;
                InboxActivity.this.itemCount = 0;
                InboxActivity.this.messageList.clear();
                new GetEmailsTask().execute();
            }
        });

        listMessages.setAdapter(messagesAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(InboxActivity.this);
        listMessages.setLayoutManager(mLayoutManager);
        listMessages.setItemAnimator(new DefaultItemAnimator());

        fabCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUtils.showSnackbar(lytParent, "Compose");
            }
        });
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
                InboxActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @SuppressLint("StaticFieldLeak")
    private class GetEmailsTask extends AsyncTask<Void, Void, List<Message>> {

        int messageCount;
        private Exception mLastError = null;

        GetEmailsTask() {
            messageCount = 0;
        }

        @Override
        protected List<Message> doInBackground(Void... voids) {
            List<Message> messageListReceived = null;

            try {
                InboxActivity.this.refreshMessages.setRefreshing(true);
                String user = "me";
                String query = "in:inbox";
                ListMessagesResponse messageResponse = mService.users().messages().list(user).setQ(query).setMaxResults(6L).setPageToken(InboxActivity.this.pageToken).execute();
                InboxActivity.this.pageToken = messageResponse.getNextPageToken();

                messageListReceived = new ArrayList<>();
                List<com.google.api.services.gmail.model.Message> receivedMessages = messageResponse.getMessages();
                for (com.google.api.services.gmail.model.Message message : receivedMessages) {
                    com.google.api.services.gmail.model.Message actualMessage = mService.users().messages().get(user, message.getId()).execute();

                    Map<String, String> headers = new HashMap<>();
                    for (MessagePartHeader messagePartHeader : actualMessage.getPayload().getHeaders())
                        headers.put(
                                messagePartHeader.getName(), messagePartHeader.getValue()
                        );

                    messageListReceived.add(new Message(
                            actualMessage.getLabelIds(),
                            actualMessage.getSnippet(),
                            actualMessage.getPayload().getMimeType(),
                            headers,
                            actualMessage.getPayload().getParts(),
                            actualMessage.getInternalDate(),
                            InboxActivity.this.mUtils.getRandomMaterialColor()
                    ));
                    messageCount++;
                }
            } catch (Exception e) {
                Log.w("MailBoxApp", e);
                mLastError = e;
                cancel(true);
            }

            return messageListReceived;
        }

        @Override
        protected void onPostExecute(List<Message> output) {
            if (output != null && output.size() != 0) {
                InboxActivity.this.messageList.addAll(output);
                InboxActivity.this.messagesAdapter.notifyItemRangeInserted(itemCount, messageCount);
                itemCount += messageCount;
                InboxActivity.this.refreshMessages.setRefreshing(false);
            } else {
                InboxActivity.this.refreshMessages.setRefreshing(false);
                InboxActivity.this.mUtils.showSnackbar(lytParent, "Fetch failed");
            }
        }

        @Override
        protected void onCancelled() {
            InboxActivity.this.refreshMessages.setRefreshing(false);
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
                    mUtils.showSnackbar(lytParent, "An error occurred, try again");
                }
            } else {
                mUtils.showSnackbar(lytParent, "An error occurred, try again");
            }
        }

    }
}
