package test.com.sms;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Arpit on 17/08/16.
 */
public class SMSDetailItemAdapter extends ArrayAdapter<String> {

    protected Context mContext;
    protected List<String> mSmsBody, mSmsTime, mSmsType;

    public SMSDetailItemAdapter(Context context, List<String> smsBody, List<String> smsTime, List<String> smsType){
        super(context, R.layout.sms_detail_list_item, smsBody);
        mContext = context;
        mSmsBody = smsBody;
        mSmsTime = smsTime;
        mSmsType = smsType;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.sms_detail_list_item, null);
            holder = new ViewHolder();
            holder.mSmsBodyTextView = (TextView) convertView.findViewById(R.id.smsBodyText);
            holder.mSmsTimeTextView = (TextView) convertView.findViewById(R.id.smsTimeText);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        if (mSmsType.get(position).equals("2")) {
            holder.mSmsBodyTextView.setGravity(Gravity.END);
            holder.mSmsTimeTextView.setGravity(Gravity.END);
        }else{
            holder.mSmsBodyTextView.setGravity(Gravity.START);
            holder.mSmsTimeTextView.setGravity(Gravity.START);
        }
        holder.mSmsBodyTextView.setText(mSmsBody.get(position));
        holder.mSmsTimeTextView.setText(mSmsTime.get(position));

        return convertView;
    }

    private static class ViewHolder{
        TextView mSmsBodyTextView;
        TextView mSmsTimeTextView;
    }
}