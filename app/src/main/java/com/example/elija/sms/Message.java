package com.example.elija.sms;

import android.Manifest;
import android.content.ContentResolver;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.security.Key;
import java.util.ArrayList;
import java.util.Collections;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import static android.os.Build.VERSION_CODES.M;

public class Message extends AppCompatActivity {

    public String title;
    public String number;
    EditText secretkey2;
    ArrayList<Contact> contacts;
    public String secretkey;
    ArrayList<Conversation> convo;
    ConvoAdapter adapter;
    public ListView listView;
    private static Message inst;
    private static final int READ_SMS_PERMISSIONS_REQUEST = 1;
    MyDBhandler dBhandler;
    private final BroadcastReciever myReciever = new BroadcastReciever();

    public static Message instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }
    public void printDatabase(){
        String dbString = dBhandler.databaseToString();
        Log.e("SQL", dbString);
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(myReciever);
    }
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(myReciever, filter);
    }
    @RequiresApi(api = M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        listView = (ListView) findViewById(R.id.convo);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(myReciever, filter);
        dBhandler = new MyDBhandler(this,null,null,1);
        //allow SMS sending
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, 1);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
        //gets our variables and arraylist from the last page
        title = getIntent().getStringExtra("title").toString();
        number = getIntent().getStringExtra("number").toString();
        contacts = (ArrayList<Contact>) getIntent().getSerializableExtra("contact");
        //formats the phone number string so it can match the phone number coming in. probably shouldnt touch this
        if (number.contains("(") ||
                number.contains(")") ||
                number.contains(" ") ||
                number.contains("-")) {
            number = number.replace("(", "");
            number = number.replace(")", "");
            number = number.replace(" ", "");
            number = number.replace("-", "");
        }
        Log.e("Formatted number", number);
        //sets the title and gets the users secret key from the MainActivity
        secretkey = getIntent().getStringExtra("mykey").toString();
        setTitle(title);
        secretkey2 = (EditText)findViewById(R.id.secretkey);
        secretkey2.setText(dBhandler.getSecretKey(title));
        //sets the array adapter and puts it in the listview
        convo = new ArrayList<Conversation>();
        adapter = new ConvoAdapter(this, convo);
        listView.setAdapter(adapter);

        //checks to see if database is empty.
        //if so it adds all the contacts in
       if(dBhandler.checkIfTableIsEmpty() == false){
           for(int i = 0; i < contacts.size(); i++){
               dBhandler.addContact(contacts.get(i));
               printDatabase();
           }
       }
       printDatabase();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            getPermissionToReadSMS();
            Toast.makeText(this, "getting permission to read", Toast.LENGTH_SHORT).show();
        } else {
            refreshSmsInbox();
            Toast.makeText(this, "Refreshing smsinbox", Toast.LENGTH_SHORT).show();
        }

        //sets the secret key of the person we are contacting, only need to edit this when we start to store the messages in SQLite
        Button b = (Button) findViewById(R.id.secretkeybutton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Conversation> temp = new ArrayList<>();
                secretkey2 = (EditText) findViewById(R.id.secretkey);
                for(int i = 0; i < contacts.size(); i++){
                    if(title.equals(contacts.get(i).getContact())){
                        contacts.get(i).setSecretkey(secretkey2.getText().toString());
                        dBhandler.upDateRow(contacts.get(i));
                    }
                }
                for (int i = 0; i <= convo.size(); i++) {
                        try {
                            if (convo.get(i).getBody().contains("issa secret") && secretkey2.getText().length() ==16) {

                                String lol = convo.get(i).getBody().replace("issa secret", "");
                                // convert the encrypted String message body to a byte
                                // array
                                byte[] msg = hex2byte(lol.getBytes());
                                // decrypt the byte array
                                byte[] result = decryptSMS(secretkey2.getText().toString(), msg);
                                String idk = new String(result);
                                if (idk.contains("Sent: ")) {
                                    idk = idk.replace("Sent: ", "");
                                }
                                Log.e("Decrypted Body", idk);
                                Conversation c1 = new Conversation("", idk, convo.get(i).getAddress());
                                temp.add(c1);
                            }else{

                                temp.add(convo.get(i));
                            }
                        } catch (Exception e) {
                            // in the case of message corrupted or invalid key
                            // decryption cannot be carried out
                            Log.e("catch", "" + e );
                        }
                    }
                convo.clear();
                convo.addAll(temp);
                adapter.notifyDataSetChanged();
            }
        });
        //send message and add the message to the list
        Button send = (Button) findViewById(R.id.sendbutton);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText msg = (EditText) findViewById(R.id.message);
                String msgContentString = msg.getText().toString();
                // check for the validity of the user input
                // key length should be 16 characters as defined by AES-128-bit
                // encrypt the message
                byte[] encryptedMsg = encryptSMS(secretkey, msgContentString);
                // convert the byte array to hex format in order for
                // transmission
                String msgString = byte2hex(encryptedMsg);
                // send the message through SMS
                sendSMS(number, "issa secret"+msgString);

                convo.add(new Conversation(msgContentString, "", ""));
                adapter.notifyDataSetChanged();
                msg.setText("");
            }
        });
    }
    //uses the smsManager to sent the smsMessages, dont touch this
    public static void sendSMS(String recNumString, String encryptedMsg) {
        try {
            // get a SmsManager
            SmsManager smsManager = SmsManager.getDefault();
            // Message may exceed 160 characters
            // need to divide the message into multiples
            ArrayList<String> parts = smsManager.divideMessage(encryptedMsg);
            smsManager.sendMultipartTextMessage(recNumString, null, parts, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // utility function dont touch this
    public static String byte2hex(byte[] b) {
        String hs = "";
        String stmp = "";

        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            if (stmp.length() == 1)
                hs += ("0" + stmp);
            else
                hs += stmp;
        }
        return hs.toUpperCase();
    }
    // encryption function dont touch this
    public static byte[] encryptSMS(String secretKeyString, String msgContentString) {
        try {
            byte[] returnArray;
            // generate AES secret key from user input
            Key key = generateKey(secretKeyString);
            // specify the cipher algorithm using AES
            Cipher c = Cipher.getInstance("AES");
            // specify the encryption mode
            c.init(Cipher.ENCRYPT_MODE, key);
            // encrypt
            returnArray = c.doFinal(msgContentString.getBytes());
            return returnArray;
        } catch (Exception e) {
            e.printStackTrace();
            byte[] returnArray = null;
            return returnArray;
        }
    }
    //just generating a secret key. dont touch this
    private static Key generateKey(String secretKeyString) throws Exception {
        // generate secret key from string
        Key key = new SecretKeySpec(secretKeyString.getBytes(), "AES");
        return key;
    }
    //gets the in coming SMS message, decrypts it if it can, if not it displays the encrypted message.
    public void updateInbox(Conversation c) {
        Log.e("New Message coming in", c.toString());
        secretkey2 = (EditText) findViewById(R.id.secretkey);
        String secretkey = secretkey2.getText().toString();
        try {
            if(c.getBody().toString().contains("issa secret") && c.getAddress().equals(number) || c.getAddress().equals("+1"+ number)) {
                // convert the encrypted String message body to a byte
                // array
                String newbody = c.getBody().toString().replace("issa secret", "");
                byte[] msg = hex2byte(newbody.getBytes());
                // decrypt the byte array
                byte[] result = decryptSMS(secretkey, msg);
                String idk = new String(result);
                //just formatting the decrypted message
                if (idk.contains("Sent: ")) {
                    idk = idk.replace("Sent: ", "");
                }
                Conversation c1 = new Conversation("", idk, c.getAddress());
                convo.add(c1);
                adapter.notifyDataSetChanged();
                Log.e("HEEEELLLOOOO", c1.toString());
            }else if(!c.getBody().toString().contains("issa secret") && (c.getAddress().equals(number) || c.getAddress().equals("+1"+ number))) {
                convo.add(c);
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            // in the case of message corrupted or invalid key
            // decryption cannot be carried out
            convo.add(c);
            adapter.notifyDataSetChanged();
            Log.e("catch", "" + e);
        }
    }
    //permissions to read from sms messages. dont touch this
    @RequiresApi(api = M)
    public void getPermissionToReadSMS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_SMS)) {
                Toast.makeText(this, "Please allow permission!", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.READ_SMS},
                    READ_SMS_PERMISSIONS_REQUEST);
        }
    }
    //permissions to read sms stuff. dont touch
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == READ_SMS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Read SMS permission granted", Toast.LENGTH_SHORT).show();
                refreshSmsInbox();
            } else {
                Toast.makeText(this, "Read SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    //gets all recieved messages in SMS inbox. eventually we want to get all messages but its tricky.
    public void refreshSmsInbox() {
        //this is a cursor to go through and get all of your recieved messages
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"),
                null, null, null, null);
        //this gets the message itsself
        int indexBody = smsInboxCursor.getColumnIndex("body");
        //this gets the address the message came from
        int indexAddress = smsInboxCursor.getColumnIndex("address");

        int indexDate = smsInboxCursor.getColumnIndex("date");
        //right now its only getting messages sent to you
        if (indexBody < 0 || !smsInboxCursor.moveToFirst())
            return;
        adapter.clear();
        do {
            Conversation c = new Conversation("",smsInboxCursor.getString(indexBody),smsInboxCursor.getString(indexAddress));

            if (c.getAddress().equals(number)|| c.getAddress().equals("+1" + number)){
                adapter.add(c);
            }
        } while (smsInboxCursor.moveToNext());
        Collections.reverse(convo);
    }
    // utility function: convert hex array to byte array. dont touch
    public static byte[] hex2byte(byte[] b) {
        if ((b.length % 2) != 0)
            throw new IllegalArgumentException("hello");
        byte[] b2 = new byte[b.length / 2];
        for (int n = 0; n < b.length; n += 2) {
            String item = new String(b, n, 2);
            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }
        return b2;
    }
    // decryption function. dont touch
    public static byte[] decryptSMS(String secretKeyString, byte[] encryptedMsg) throws Exception {
        // generate AES secret key from the user input secret key
        Key key = generateKey(secretKeyString);
        // get the cipher algorithm for AES
        Cipher c = Cipher.getInstance("AES");
        // specify the decryption mode
        c.init(Cipher.DECRYPT_MODE, key);
        // decrypt the message
        byte[] decValue = c.doFinal(encryptedMsg);
        return decValue;
    }
}
