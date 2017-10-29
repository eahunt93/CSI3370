package com.example.elija.sms;

import java.io.Serializable;

/**
 * Created by elijah on 9/16/2017.
 */

public class Contact implements Serializable{
    String contact;
    String number;
    String secretkey;


    public Contact(String contact, String number, String secretkey){
        this.contact = contact;
        this.number = number;
        this.secretkey = secretkey;

    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setSecretkey(String secretkey) {
        this.secretkey = secretkey;
    }

    public String getSecretkey() {
        return secretkey;
    }

    @Override
    public String toString() {
        return "Name: " + contact + "\n"
                + "Number: "+ number + "\n"
                + "Secret Key: " + secretkey;
    }
}

