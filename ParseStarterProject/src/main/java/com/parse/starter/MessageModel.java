package com.parse.starter;

public class MessageModel {
    String body;
    String status;
    String branch;
    String sender;
    String smsCode;
    public MessageModel(String body , String status , String branch , String sender , String smsCode)
    {
        this.body = body;
        this.status = status;
        this.branch = branch;
        this.sender  =sender;
        this.smsCode = smsCode;
    }
}
