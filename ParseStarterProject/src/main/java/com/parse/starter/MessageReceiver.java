package com.parse.starter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.telephony.SmsMessage;

public class MessageReceiver extends WakefulBroadcastReceiver {

    private static MessageListener mListener;
    static Bundle data;

    @Override
    public void onReceive(Context context, Intent intent) {
         data = intent.getExtras();
        Object[] pdus = (Object[]) data.get("pdus");
        for(int i=0; i<pdus.length; i++){
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
            String sender = "Sender : " + smsMessage.getDisplayOriginatingAddress();
            String message =smsMessage.getMessageBody();
            //mListener.messageReceived(message , sender);

            // start a service

        if(MainActivity.active)
        {
            mListener.messageReceived(message , sender);
        }
        else {
            intent = new Intent(context , SmsService.class) ;
            startWakefulService(context , intent);
        }

        }



            // check if app is open or closed , if open call the listener , if closed call the service

        }


    public static void bindListener(MessageListener listener){
        mListener = listener;
    }
}