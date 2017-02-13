package test.com.sms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Arpit on 17/08/16.
 */
public class SMSAdapter extends ArrayAdapter<String> {

    protected Context mContext;
    protected List<String> mAddress, mSmsBody, mSmsTime;

    public SMSAdapter(Context context, List<String> address, List<String> smsBody, List<String> smsTime){
        super(context, R.layout.sms_list_item, address);
        mContext = context;
        mAddress = address;
        mSmsBody = smsBody;
        mSmsTime = smsTime;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.sms_list_item, null);
            holder = new ViewHolder();
            holder.mAddressTextView = (TextView) convertView.findViewById(R.id.addressText);
            holder.mSmsBodyTextView = (TextView) convertView.findViewById(R.id.smsBodyText);
            holder.mSmsTimeTextView = (TextView) convertView.findViewById(R.id.smsTimeText);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mAddressTextView.setText(mAddress.get(position));
        holder.mSmsBodyTextView.setText(mSmsBody.get(position));
        holder.mSmsTimeTextView.setText(mSmsTime.get(position));

        return convertView;
    }

    private static class ViewHolder{
        TextView mAddressTextView;
        TextView mSmsBodyTextView;
        TextView mSmsTimeTextView;
    }
}
