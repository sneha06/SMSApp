package test.com.sms;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "SMS Activity";
    private static final int REQUEST_CODE_RESOLUTION = 1;
    private static final  int REQUEST_CODE_OPENER = 2;
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 141;
    private GoogleApiClient mGoogleApiClient;
    private boolean fileOperation = false;
    public DriveFile file;

    List<String> smsAddress, smsBody, smsTime;
    ListView smsList;
    Cursor cursor;
    Toolbar toolbar;
    EditText searchBar;
    ImageView cancelSearch, searchBtn;
    SMSAdapter adapter;
    MenuItem item1,item2;

    String msgData = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        smsAddress = new ArrayList<String>();
        smsBody = new ArrayList<String>();
        smsTime = new ArrayList<String>();
        smsList = (ListView) findViewById(R.id.sms_list);

        searchBar = (EditText) findViewById(R.id.edit_search_bar);
        cancelSearch = (ImageView) findViewById(R.id.cancel_search_btn);
        searchBtn = (ImageView) findViewById(R.id.search_btn);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, NewMessage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        checkPermission();
    }


    public void checkPermission(){
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissionsNeeded = new ArrayList<String>();
            final List<String> permissionsList = new ArrayList<String>();
            if (!addPermission(permissionsList, Manifest.permission.READ_SMS))
                permissionsNeeded.add("Read sms");
            if (!addPermission(permissionsList, Manifest.permission.RECEIVE_SMS))
                permissionsNeeded.add("Receive sms");
            if (!addPermission(permissionsList, Manifest.permission.SEND_SMS))
                permissionsNeeded.add("Send sms");
            if (permissionsList.size() > 0) {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            } else {
                getSMS();
                smsList.setOnItemClickListener(this);
            }
        } else {
            getSMS();
            smsList.setOnItemClickListener(this);
        }
    }

    public void getSMS() {
        cursor = getContentResolver().query(Uri.parse("content://sms/"), null, null, null, null);

        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {
                //sms column Name: address, body, date
                String mAddress = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                String mBody = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                long mTime = cursor.getLong(cursor.getColumnIndexOrThrow("date"));

                Calendar calendar = Calendar.getInstance();
                Date currentDate = calendar.getTime();

                calendar.setTimeInMillis(mTime);
                DateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
                String finalDateString = formatter.format(calendar.getTime());
                String currentDateString = formatter.format(currentDate);

                msgData += "SMSAddress: " + mAddress + "\nSMSBody: " + mBody + "\nSMSTime: " + finalDateString + "\n\n";

                if (smsAddress.isEmpty() || !(smsAddress.contains(mAddress))) {
                    smsAddress.add(mAddress);
                    smsBody.add(mBody);

                    if (currentDateString.equals(finalDateString)){
                        formatter = new SimpleDateFormat("hh:mm a");
                        finalDateString = formatter.format(calendar.getTime());
                    }else{
                        formatter = new SimpleDateFormat("dd MMM");
                        finalDateString = formatter.format(calendar.getTime());
                    }

                    smsTime.add(finalDateString);
                }
            } while (cursor.moveToNext());
        } else {
            // empty box, no SMS
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder.setTitle("Oops!!!")
                    .setMessage("You don't have any SMS.")
                    .setPositiveButton(android.R.string.ok, null);

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        cursor.close();

        if (!smsAddress.isEmpty()) {
            adapter = new SMSAdapter(HomeActivity.this, smsAddress, smsBody, smsTime);
            smsList.setAdapter(adapter);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(HomeActivity.this, SMSDetailsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("smsAddress", smsAddress.get(i));
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home,menu);
        item1 = (MenuItem) menu.findItem(R.id.action_google_drive);
        item2 = (MenuItem) menu.findItem(R.id.action_open_google_drive);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                toolbar.setLogo(null);
                toolbar.setTitle("");
                item.setVisible(false);
                item1.setVisible(false);
                item2.setVisible(false);
                searchBar.setVisibility(View.VISIBLE);
                cancelSearch.setVisibility(View.VISIBLE);
                searchBtn.setVisibility(View.VISIBLE);
                searchBtn.setOnClickListener(this);
                cancelSearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        searchBar.setVisibility(View.GONE);
                        cancelSearch.setVisibility(View.GONE);
                        searchBtn.setVisibility(View.GONE);
                        toolbar.setLogo(R.mipmap.ic_launcher);
                        toolbar.setTitle("SMS");
                        item.setVisible(true);
                        item1.setVisible(true);
                        item2.setVisible(true);
                        if(getCurrentFocus()!=null) {
                            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                        }
                    }
                });
                break;
            case R.id.action_google_drive:
                connectToGoogleApi(true);
                break;
            case R.id.action_open_google_drive:
                connectToGoogleApi(false);
                break;
            default:
                break;
        }
        return true;
    }


    public void connectToGoogleApi(boolean value){
        if (mGoogleApiClient == null) {
            /**
             * Create the API client and bind it to an instance variable.
             * We use this instance as the callback for connection and connection failures.
             * Since no account name is passed, the user is prompted to choose.
             */
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        fileOperation = value;
        if (mGoogleApiClient.isConnected()){
            // create new contents resource
            Drive.DriveApi.newDriveContents(mGoogleApiClient)
                    .setResultCallback(driveContentsCallback);
        }
        else{
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.search_btn:
                String searchText = searchBar.getText().toString();
                Intent intent = new Intent(HomeActivity.this, SearchResultActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("searchText", searchText);
                startActivity(intent);
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            // disconnect Google API client connection
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
//        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
        // create new contents resource
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(driveContentsCallback);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Called whenever the API client fails to connect.
        Log.i(TAG, "GoogleApiClient connection failed: " + connectionResult.toString());

        if (!connectionResult.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, connectionResult.getErrorCode(), 0).show();
            return;
        }
        /**
         *  The failure has a resolution. Resolve it.
         *  Called typically when the app is not yet authorized, and an  authorization
         *  dialog is displayed to the user.
         */
        try {
            connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    /**
     * This is Result result handler of Drive contents.
     * this callback method call CreateFileOnGoogleDrive() method
     * and also call OpenFileFromGoogleDrive() method, send intent onActivityResult() method to handle result.
     */
    final ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {

                    if (result.getStatus().isSuccess()) {
                        if (fileOperation) {
                            CreateFileOnGoogleDrive(result);
                        } else {
                            OpenFileFromGoogleDrive();
                        }
                    }
                }
            };


    //Create a file in root folder using MetadataChangeSet object.
    public void CreateFileOnGoogleDrive(DriveApi.DriveContentsResult result){

        final DriveContents driveContents = result.getDriveContents();

        // Perform I/O off the UI thread.
        new Thread() {
            @Override
            public void run() {
                // write content to DriveContents
                OutputStream outputStream = driveContents.getOutputStream();
                Writer writer = new OutputStreamWriter(outputStream);
                try {
                    writer.write(msgData);
                    writer.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }

                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle("SMS Data")
                        .setMimeType("text/plain")
                        .setStarred(true).build();

                // create a file in root folder
                Drive.DriveApi.getRootFolder(mGoogleApiClient)
                        .createFile(mGoogleApiClient, changeSet, driveContents)
                        .setResultCallback(fileCallback);
            }
        }.start();
    }


    //Handle result of Created file
    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    if (result.getStatus().isSuccess()) {

                        Toast.makeText(getApplicationContext(), "File created in Google Drive", Toast.LENGTH_LONG).show();
//                        Toast.makeText(getApplicationContext(),""+result.getDriveFile().getDriveId(), Toast.LENGTH_LONG).show();
                    }
                }
            };

     //Open list of folder and file of the Google Drive
    public void OpenFileFromGoogleDrive(){

        IntentSender intentSender = Drive.DriveApi
                .newOpenFileActivityBuilder()
                .setMimeType(new String[] { "text/plain", "text/html" })
                .build(mGoogleApiClient);
        try {
            startIntentSenderForResult(intentSender, REQUEST_CODE_OPENER, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            Log.w(TAG, "Unable to send intent", e);
        }
    }

    //Handle Response of selected file
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_OPENER:
                if (resultCode == RESULT_OK) {
                    DriveId mFileId = (DriveId) data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);

//                    Log.e("file id", mFileId.getResourceId() + "");

                    String url = "https://drive.google.com/open?id="+ mFileId.getResourceId();
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (this.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.READ_SMS, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.RECEIVE_SMS, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.SEND_SMS, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_SMS
                if (perms.get(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    getSMS();
                    smsList.setOnItemClickListener(HomeActivity.this);
                } else {
                    // Permission Denied
                    Toast.makeText(this, R.string.message_allow_perm_to_continue, Toast.LENGTH_SHORT)
                            .show();
                    checkPermission();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
