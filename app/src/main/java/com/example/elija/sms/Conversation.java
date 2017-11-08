package com.example.elija.sms;

/**
 * Created by elija on 9/22/2017.
 */

public class Conversation {
    String sentmessage;
    String body;
    String Address;
    long TimeInMilliSeconds;
    public Conversation(String sentmessage, String MessageBody, String Address, long TimeInMilliSeconds){
        this.sentmessage = sentmessage;
        this.body = MessageBody;
        this.Address = Address;
        this.TimeInMilliSeconds = TimeInMilliSeconds;

    }
    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSentmessage() {
        return sentmessage;
    }

    public void setSentmessage(String sentmessage) {
        this.sentmessage = sentmessage;
    }

    public long getTimeInMilliSeconds() {
        return TimeInMilliSeconds;
    }

    public void setTimeInMilliSeconds(long timeInMilliSeconds) {
        TimeInMilliSeconds = timeInMilliSeconds;
    }


    @Override
    public String toString() {
        return  Address + "\n " + body  ;
    }

}


