package com.parse.starter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageHolder> {

    Context context;
    List<MessageModel> messagesList;
    // constructor
    public MessagesAdapter(Context context , List<MessageModel> messagesList)
    {
        this.context = context;
        this.messagesList = messagesList;
    }
    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_item , viewGroup , false);
        MessageHolder messageHolder = new MessageHolder(v);
        return messageHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder messageHolder, int i) {
        final MessageModel model = messagesList.get(i);

        messageHolder.messageBody.setText("Code: "+model.body);
        messageHolder.branchTxt.setText("Branch: "+model.branch);
        messageHolder.statusTxt.setText("Status: "+model.status);
        messageHolder.sender.setText(model.sender);
        messageHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // prompt before delete
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete Message ?");
                builder.setMessage("the message will be deleted permanently !");
                builder.setCancelable(false);
                builder.setPositiveButton("delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // start deleting process
                        final ProgressDialog progressDialog = new ProgressDialog(context);
                        progressDialog.setTitle("please wait");
                        progressDialog.setMessage("Deleting message...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        // query the message from parse server
                        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("SMS");
                        parseQuery.whereEqualTo("smsCode" , model.smsCode);
                        parseQuery.setLimit(1);
                        parseQuery.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> objects, ParseException e) {
                                if( e == null)
                                {

                                    for(ParseObject object : objects)
                                    {
                                        // delete object after finding it in the parse server
                                        object.deleteInBackground(new DeleteCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                progressDialog.cancel();

                                                if(e == null)
                                                {
                                                    // notify that sms has been deleted and refresh app
                                                    Toast.makeText(context, "message deleted successfully", Toast.LENGTH_SHORT).show();
                                                   /* Intent intent = new Intent(context , MainActivity.class);
                                                    ((MainActivity)context).finish();
                                                    ((MainActivity)context).startActivity(intent);*/
                                                   MainActivity.getMessages(context);

                                                }
                                                else {
                                                    Toast.makeText(context, "failed, message will be deleted once connection is restored !", Toast.LENGTH_LONG).show();
                                                }

                                            }
                                        });
                                    }
                                }
                            }
                        });

                    }
                });
                builder.setNegativeButton("keep", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // do nothing
                    }
                });
                builder.show();

            }
        });

    }



    @Override
    public int getItemCount() {
        return messagesList.size();
    }
    class MessageHolder extends RecyclerView.ViewHolder{

        // declare the views in the mesages row item
        TextView messageBody;
        TextView branchTxt;
        TextView statusTxt;
        TextView sender;
        Button deleteButton;


        public MessageHolder(@NonNull View itemView) {
            super(itemView);
            // init the views

            messageBody = (TextView) itemView.findViewById(R.id.messageTxt);
            branchTxt = (TextView) itemView.findViewById(R.id.branch_Txt_item);
            statusTxt = (TextView) itemView.findViewById(R.id.status_Txt_item);
            sender = (TextView) itemView.findViewById(R.id.sender);
            deleteButton = (Button)itemView.findViewById(R.id.delete_button);

        }
    }
}
