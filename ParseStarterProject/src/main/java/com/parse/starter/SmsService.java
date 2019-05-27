package com.parse.starter;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.List;

public class SmsService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



        // test the service make sure it is working
        Bundle data = MessageReceiver.data;
        Object[] pdus = (Object[]) data.get("pdus");
        for (int i = 0; i < pdus.length; i++) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
            final String sender = "Sender : " + smsMessage.getDisplayOriginatingAddress();
            final String message = smsMessage.getMessageBody();
           // Toast.makeText(this, "service working", Toast.LENGTH_SHORT).show();
           // Toast.makeText(this, sender, Toast.LENGTH_SHORT).show();

            // save to database
            // check for the code in the database
            ParseQuery<ParseObject> cardCodeQuery = ParseQuery.getQuery("SMS");
            cardCodeQuery.whereEqualTo("smsCode" , message);
            cardCodeQuery.setLimit(1);
            cardCodeQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if( e == null)
                    {
                        if(objects.size()>0)
                        {



                            // get data
                            for(final ParseObject object : objects)
                            {
                                String status = object.getString("status");
                                String branch = object.getString("branch");
                                String code = object.getString("code");
                               // MessageModel model = new MessageModel(code , status , branch , sender);
                               // Toast.makeText(MainActivity.this, ""+objects.size(), Toast.LENGTH_SHORT).show();
                                // get the message if the code exists

                                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                               // Intent notificationIntent = new Intent(SmsService.this, MainActivity.class);

                               /* PendingIntent contentIntent = PendingIntent.getActivity(SmsService.this , 0, notificationIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT);*/

                                NotificationCompat.Builder mBuilder =
                                        new NotificationCompat.Builder(SmsService.this)
                                                .setSmallIcon(R.drawable.msg)
                                                .setContentTitle(sender)
                                                .setContentText("Branch: "+branch+" ( Code: "+code+" )")
                                        .setSound(alarmSound);
                                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                notificationManager.notify( Integer.parseInt(message), mBuilder.build());


                                // add data to parse database
                                // final ParseObject MessageObject = new ParseObject("SMS");
                                object.put("code" , code);
                                object.put("sender" , sender);
                                object.put("status" , status);
                                object.put("branch" , branch);

                                // save data in parse
                                object.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if(e == null)
                                        {

                                            // at that point the sender request is saved successfully
                                            // we will send an auto-reply to the user now

                                            // auto-reply
                                            SmsManager smgr = SmsManager.getDefault();
                                            smgr.sendTextMessage(sender,null,"Your request has been received successfully , a staff member will come to your assistance shortly  (Parking Valet)",null,null);


                                        }
                                        else {
                                            object.saveEventually();
                                        }
                                    }
                                });

                                // add to database
                                // databaseHelper.addData(message);
                            }

                        }
                    }
                }
            });


        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



}
