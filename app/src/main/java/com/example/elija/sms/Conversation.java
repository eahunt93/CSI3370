package com.example.elija.sms;

/**
 * Created by elija on 9/22/2017.
 */

public class Conversation {
    String sentmessage;
    String recievedmessage;
    String messagefrom;
    public Conversation(String sentmessage, String recievedmessage, String messagefrom){
        this.messagefrom = messagefrom;
        this.recievedmessage =recievedmessage;
        this.sentmessage = sentmessage;
    }

    public String getMessagefrom() {
        return messagefrom;
    }

    public void setMessagefrom(String messagefrom) {
        this.messagefrom = messagefrom;
    }

    public String getRecievedmessage() {
        return recievedmessage;
    }

    public void setRecievedmessage(String recievedmessage) {
        this.recievedmessage = recievedmessage;
    }

    public String getSentmessage() {
        return sentmessage;
    }

    public void setSentmessage(String sentmessage) {
        this.sentmessage = sentmessage;
    }

    @Override
    public String toString() {
        return "Sent: " + sentmessage + "\n"+
                "recieved message" + recievedmessage;
    }
}
