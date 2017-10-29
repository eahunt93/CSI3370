package com.example.elija.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.telephony.SmsMessage;

/**
 * Created by elija on 9/23/2017.
 */

public class BroadcastReciever extends BroadcastReceiver {

    public static final String SMS_BUNDLE = "pdus";
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();

        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String smsMessageStr = "";
            String smsBody = "";
            Conversation c = null;
            for (int i = 0; i < sms.length; ++i) {
                String format = intentExtras.getString("format");
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i], format);

                smsBody = smsMessage.getMessageBody().toString();
                String address = smsMessage.getOriginatingAddress();
                 c = new Conversation("",smsBody, address);

                smsMessageStr +=  address + "\n";
                smsMessageStr += smsBody + "\n";
            }
            Message inst = Message.instance();
            inst.getIntent().putExtra("messagebody", smsBody);
            inst.updateInbox(c);
        }
    }

}
