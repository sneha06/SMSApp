package test.com.sms;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SMSDetailsActivity extends AppCompatActivity implements View.OnClickListener{

    ListView smsDetailList;
    ImageView sendMsgButton;
    EditText textMsg;
    Cursor cursor;
    List<String> smsDetailBody, smsDetailTime, smsDetailType;

    String mSMSAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smsdetails);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSMSAddress = getIntent().getStringExtra("smsAddress");
        getSupportActionBar().setTitle(mSMSAddress);

        smsDetailList = (ListView) findViewById(R.id.sms_detail_list);
        sendMsgButton = (ImageView) findViewById(R.id.msg_send_btn);
        textMsg = (EditText) findViewById(R.id.text_msg);

        sendMsgButton.setOnClickListener(this);

        filterSMS();
    }

    private void filterSMS() {
        cursor = getContentResolver().query(Uri.parse("content://sms/"), null, null, null, null);

        smsDetailBody = new ArrayList<String>();
        smsDetailTime = new ArrayList<String>();
        smsDetailType = new ArrayList<String>();

        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {
                String mAddress = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                if (mAddress.equals(mSMSAddress)) {
                    smsDetailBody.add(cursor.getString(cursor.getColumnIndexOrThrow("body")));
                    smsDetailType.add(cursor.getString(cursor.getColumnIndexOrThrow("type")));

                    long milliSeconds = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
                    Calendar calendar = Calendar.getInstance();
                    Date currentDate = calendar.getTime();

                    calendar.setTimeInMillis(milliSeconds);
                    DateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
                    String finalDateString = formatter.format(calendar.getTime());
                    String currentDateString = formatter.format(currentDate);

                    if (currentDateString.equals(finalDateString)){
                        formatter = new SimpleDateFormat("hh:mm a");
                        finalDateString = formatter.format(calendar.getTime());
                    }else{
                        formatter = new SimpleDateFormat("dd MMM");
                        finalDateString = formatter.format(calendar.getTime());
                    }

                    smsDetailTime.add(finalDateString);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (!smsDetailBody.isEmpty()) {
            Collections.reverse(smsDetailBody);
            Collections.reverse(smsDetailTime);
            Collections.reverse(smsDetailType);
            SMSDetailItemAdapter adapter = new SMSDetailItemAdapter(SMSDetailsActivity.this, smsDetailBody, smsDetailTime, smsDetailType);
            smsDetailList.setAdapter(adapter);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.msg_send_btn:
                sendMessage();
                break;
            default:
                break;
        }
    }

    public void gotoHome(){
        Intent intent = new Intent(SMSDetailsActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                gotoHome();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        gotoHome();
    }

    private void sendMessage() {
        String msg = textMsg.getText().toString();
        textMsg.setText("");

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(mSMSAddress , null, msg, null, null);
            Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
