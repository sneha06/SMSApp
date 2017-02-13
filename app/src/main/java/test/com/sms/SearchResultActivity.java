package test.com.sms;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SearchResultActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    Cursor cursor;
    List<String> smsAddress, smsBody, smsTime;
    ListView smsList;

    String mSearchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSearchText = getIntent().getStringExtra("searchText");
        getSupportActionBar().setTitle("Search Result of '"+ mSearchText + "'");

        smsAddress = new ArrayList<String>();
        smsBody = new ArrayList<String>();
        smsTime = new ArrayList<String>();
        smsList = (ListView) findViewById(R.id.search_sms_list);

        smsList.setOnItemClickListener(this);

        searchSMS();
    }

    private void searchSMS() {
        cursor = getContentResolver().query(Uri.parse("content://sms/"), null, null, null, null);

        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {
                if ((cursor.getString(cursor.getColumnIndexOrThrow("address")).toLowerCase()).contains(mSearchText.toLowerCase()) ||
                        (cursor.getString(cursor.getColumnIndexOrThrow("body")).toLowerCase()).contains(mSearchText.toLowerCase()) ) {

                    smsAddress.add(cursor.getString(cursor.getColumnIndexOrThrow("address")));
                    smsBody.add(cursor.getString(cursor.getColumnIndexOrThrow("body")));

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

                    smsTime.add(finalDateString);
                }
            } while (cursor.moveToNext());
        }else {
            // empty box, no SMS
            AlertDialog.Builder builder = new AlertDialog.Builder(SearchResultActivity.this);
            builder.setTitle("Try Again")
                    .setMessage("No Result found of '"+ mSearchText + "'")
                    .setPositiveButton(android.R.string.ok, null);

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        cursor.close();

        if (!smsAddress.isEmpty()) {
            SMSAdapter adapter = new SMSAdapter(SearchResultActivity.this, smsAddress, smsBody, smsTime);
            smsList.setAdapter(adapter);
        }

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(SearchResultActivity.this, SMSDetailsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("smsAddress", smsAddress.get(i));
        startActivity(intent);
        finish();
    }

    public void gotoHome(){
        Intent intent = new Intent(SearchResultActivity.this, HomeActivity.class);
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
}
