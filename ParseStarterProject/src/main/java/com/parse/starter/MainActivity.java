/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity implements MessageListener {

  // recycler view components
  static RecyclerView recyclerView;
  static MessagesAdapter adapter;
  static List<MessageModel> messagesList;
  DatabaseHelper databaseHelper;
  static ImageView errorImage;

  static boolean active = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);



    errorImage = (ImageView)findViewById(R.id.error_image);
    errorImage.setVisibility(View.INVISIBLE);
//Register sms listener
    MessageReceiver.bindListener(this);

    // init the recycler
    recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    messagesList = new ArrayList<>();
    adapter = new MessagesAdapter(MainActivity.this , messagesList);
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));


    // fetch the data from the database
    final ProgressDialog progressDialog = new ProgressDialog(this);
    progressDialog.setTitle("Loading");
    progressDialog.setMessage("Getting messages...");
    progressDialog.setCancelable(false);
    progressDialog.show();
    ParseQuery<ParseObject> smsQuery = ParseQuery.getQuery("SMS");
    smsQuery.whereNotEqualTo("sender" , null);
    smsQuery.findInBackground(new FindCallback<ParseObject>() {
      @Override
      public void done(List<ParseObject> objects, ParseException e) {
        progressDialog.cancel();
        if( e == null)
        {
          if(objects.size()>0)
          {
            errorImage.setVisibility(View.INVISIBLE);
            messagesList.clear();
            // message list not empty --> retrieve messages
            for(ParseObject object : objects)
            {
              MessageModel model = new MessageModel(object.getString("code"), object.getString("status") , object.getString("branch") , object.getString("sender") , object.getString("smsCode") );
              messagesList.add(model);
            }
            Collections.reverse(messagesList);
            adapter.notifyDataSetChanged();
          }else {
            Toast.makeText(MainActivity.this, "No messages yet", Toast.LENGTH_SHORT).show();
            errorImage.setVisibility(View.VISIBLE);
          }
        }
        else {
          Toast.makeText(MainActivity.this, "network error"+e.getMessage(), Toast.LENGTH_LONG).show();
        }
      }
    });
    /*databaseHelper = new DatabaseHelper(this);
    Cursor data = databaseHelper.getAllData();
    while (data.moveToNext())
    {

      messagesList.add(data.getString(1));
    }
    Collections.reverse(messagesList);
    adapter.notifyDataSetChanged();*/
    ParseAnalytics.trackAppOpenedInBackground(getIntent());
  }

  @Override
  public void messageReceived(final String message , final String sender) {
    Toast.makeText(this, "New Message Received: " + message, Toast.LENGTH_SHORT).show();
       /* if(message.contains("parking") || message.contains("Parking") || message.contains("Calling") || message.contains("calling"))
        {
            messagesList.add(message);
            Collections.reverse(messagesList);
            adapter.notifyDataSetChanged();

            // add to database
            databaseHelper.addData(message);

        }*/
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
            errorImage.setVisibility(View.INVISIBLE);

            // get data
            for(final ParseObject object : objects)
            {
              String status = object.getString("status");
              String branch = object.getString("branch");
              String code = object.getString("code");
              String smsCode = object.getString("smsCode");
              MessageModel model = new MessageModel(code , status , branch , sender , smsCode);
              Toast.makeText(MainActivity.this, ""+objects.size(), Toast.LENGTH_SHORT).show();
              // get the message if the code exists

              messagesList.add(0 ,model);
                //Collections.reverse(messagesList);
              adapter.notifyDataSetChanged();

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

  static void getMessages(final Context context)
  {
    // fetch the data from the database
    final ProgressDialog progressDialog = new ProgressDialog(context);
    progressDialog.setTitle("Loading");
    progressDialog.setMessage("Getting messages...");
    progressDialog.setCancelable(false);
    progressDialog.show();
    ParseQuery<ParseObject> smsQuery = ParseQuery.getQuery("SMS");
    smsQuery.whereNotEqualTo("sender" , null);
    smsQuery.findInBackground(new FindCallback<ParseObject>() {
      @Override
      public void done(List<ParseObject> objects, ParseException e) {
        progressDialog.cancel();
        if( e == null)
        {
          if(objects.size()==0)
          {
            recyclerView.setVisibility(View.INVISIBLE);
            errorImage.setVisibility(View.VISIBLE);
          }
          else if(objects.size()>0)
          {
            recyclerView.setVisibility(View.VISIBLE);

            errorImage.setVisibility(View.INVISIBLE);
            messagesList.clear();
            // message list not empty --> retrieve messages
            for(ParseObject object : objects)
            {
              MessageModel model = new MessageModel(object.getString("code"), object.getString("status") , object.getString("branch") , object.getString("sender") , object.getString("smsCode") );
              messagesList.add(model);
            }
            Collections.reverse(messagesList);
            adapter.notifyDataSetChanged();
          }else {
            Toast.makeText(context, "No messages yet", Toast.LENGTH_SHORT).show();
            errorImage.setVisibility(View.VISIBLE);
          }
        }
        else {
          Toast.makeText(context, "network error"+e.getMessage(), Toast.LENGTH_LONG).show();
        }
      }
    });

  }

  @Override
  protected void onPostResume() {
    getMessages(this);
    super.onPostResume();
  }

  @Override
  protected void onDestroy() {
    Intent intent = new Intent(this , SmsService.class);
    stopService(intent);
    super.onDestroy();
  }

  @Override
  protected void onStart() {
    super.onStart();
    active = true;
  }

  @Override
  protected void onStop() {
    super.onStop();
    active = false;
  }


}
