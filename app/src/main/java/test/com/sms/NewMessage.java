package test.com.sms;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class NewMessage extends AppCompatActivity implements View.OnClickListener{

    ImageView sendNewMsgButton;
    EditText newMsgText, newMsgTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sendNewMsgButton = (ImageView) findViewById(R.id.new_msg_send_btn);
        newMsgText = (EditText) findViewById(R.id.new_msg_text);
        newMsgTo = (EditText) findViewById(R.id.new_msg_to);

        sendNewMsgButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.new_msg_send_btn:
                String msgBody = newMsgText.getText().toString();
                String msgPhoneNumber = newMsgTo.getText().toString();

                newMsgText.setText("");
                newMsgTo.setText("");

                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(msgPhoneNumber, null, msgBody, null, null);
                    Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
