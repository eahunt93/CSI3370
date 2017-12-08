package com.example.elija.sms;

import android.Manifest;
import android.content.ContentResolver;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
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
import java.util.Comparator;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import static android.os.Build.VERSION_CODES.M;

public class MessageActivity extends AppCompatActivity {

    String title;
    String number;
    EditText secretkey2;
    ArrayList<ContactObject> contactObjects;
    String secretkey;
    ArrayList<ConversationObject> convo;
    ConvoAdapter adapter;
    ListView listView;
    private static MessageActivity inst;
    private static final int READ_SMS_PERMISSIONS_REQUEST = 1;
    MyDBhandler dBhandler;
    private final BroadcastReciever myReciever = new BroadcastReciever();
    //list of all sent messages
    ArrayList<ConversationObject> sent;
    //list of all recieved messages
    ArrayList<ConversationObject> recieved;


    public static MessageActivity instance() {
        return inst;
    }
    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }
    //prints SQLite database
    public void printDatabase(){
        String dbString = dBhandler.databaseToString();
        Log.e("SQL", dbString);
    }
    //unregistering the broadcast receiver in when the app is paused.
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(myReciever);
    }
    //registering the broadcast reciever when the app resumes
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
        //allow SMS sending and receiving
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, 1);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
        //gets our variables and arraylist from the last page
        title = getIntent().getStringExtra("title").toString();
        number = getIntent().getStringExtra("number").toString();
        contactObjects = (ArrayList<ContactObject>) getIntent().getSerializableExtra("contactObject");

        //formats the phone number string so it can match the phone number coming in. probably shouldnt touch this
        if (number.contains("(") ||
                number.contains(")") ||
                number.contains(" ") ||
                number.contains("-") ||
                number.contains("+1")) {
            number = number.replace("(", "");
            number = number.replace(")", "");
            number = number.replace(" ", "");
            number = number.replace("-", "");
            number = number.replace("+1", "");
        }
        Log.e("Formatted number", number);

        //sets the title and gets the users secret key from the MainActivity
        secretkey = getIntent().getStringExtra("mykey").toString();
        setTitle(title);
        secretkey2 = (EditText)findViewById(R.id.secretkey);
        secretkey2.setText(dBhandler.getSecretKey(title));
        //sets the array adapter and puts it in the listview
        convo = new ArrayList<>();
        sent = new ArrayList<>();
        recieved = new ArrayList<>();
        adapter = new ConvoAdapter(this, convo);
        listView.setAdapter(adapter);
        //check to see if we have permission to read from sms and if we do we create the list
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            getPermissionToReadSMS();
            Toast.makeText(this, "getting permission to read", Toast.LENGTH_SHORT).show();
        } else {
            refreshSmsInbox();
            refreshSmssent();
            createconvolist();
            Toast.makeText(this, "Refreshing smsinbox", Toast.LENGTH_SHORT).show();
        }

        //checks to see if database is empty.
        //if so it adds all the contactObjects in
       if(dBhandler.checkIfTableIsEmpty() == false){
           for(int i = 0; i < contactObjects.size(); i++){
               dBhandler.addContact(contactObjects.get(i));
           }
       }
       printDatabase();

        //sets new secret key for the contactObject
        Button b2 = (Button)findViewById(R.id.setsecretkey);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                secretkey2 = (EditText) findViewById(R.id.secretkey);
                for(int i = 0; i < contactObjects.size(); i++){
                    if(title.equals(contactObjects.get(i).getContact())){
                        contactObjects.get(i).setSecretkey(secretkey2.getText().toString());
                        dBhandler.upDateRow(contactObjects.get(i));
                    }
                }
                printDatabase();
            }
        });

        //decryts messages
        Button b = (Button) findViewById(R.id.decrypt);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<ConversationObject> temp = new ArrayList<>();
                //set secretkey
                secretkey2 = (EditText) findViewById(R.id.secretkey);
                //decrytps messages
                for (int i = 0; i <= convo.size(); i++) {
                        try {
                            if (convo.get(i).getBody().contains("issa secret") && secretkey2.getText().length() ==16) {

                                String EncryptedMessage = convo.get(i).getBody().replace("issa secret", "");
                                // convert the encrypted String message body to a byte
                                // array
                                byte[] msg = hex2byte(EncryptedMessage.getBytes());
                                // decrypt the byte array
                                byte[] result = decryptSMS(secretkey2.getText().toString(), msg);
                                String DecryptedMessage = new String(result);
                                if (DecryptedMessage.contains("Sent: ")) {
                                    DecryptedMessage = DecryptedMessage.replace("Sent: ", "");
                                }
                                Log.e("Decrypted Body", DecryptedMessage);
                                ConversationObject c1 = new ConversationObject("", DecryptedMessage, convo.get(i).getAddress(),0);
                                temp.add(c1);
                            }else if(convo.get(i).getSentmessage().contains("issa secret") && secretkey2.getText().length() == 16){
                                String EncrytpedMessage = convo.get(i).getSentmessage().replace("issa secret", "");
                                // convert the encrypted String message body to a byte
                                // array
                                byte[] msg = hex2byte(EncrytpedMessage.getBytes());
                                // decrypt the byte array
                                byte[] result = decryptSMS(secretkey, msg);
                                String DecrytpedMessage = new String(result);
                                if (DecrytpedMessage.contains("Sent: ")) {
                                    DecrytpedMessage = DecrytpedMessage.replace("Sent: ", "");
                                }
                                Log.e("Decrypted Body", DecrytpedMessage);
                                ConversationObject c1 = new ConversationObject(DecrytpedMessage, "", convo.get(i).getAddress(),0);
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
                listView.smoothScrollToPosition(convo.size()-1);
            }
        });

        //send message and adds the message to the list
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

                convo.add(new ConversationObject(msgContentString, "", "",0));
                adapter.notifyDataSetChanged();
                msg.setText("");
                listView.smoothScrollToPosition(convo.size() -1);
            }
        });
    }
    //uses the smsManager to send the smsMessages
    //Code cite: https://sites.google.com/site/mobilesecuritylabware/3-data-location-privacy/lab-activity/cryptography/cryptography-mobile-labs/encryption-decryption/2-lab-activity/lab-1-encryption-decryption-on-sms-android-studio
    public static void sendSMS(String recNumString, String encryptedMsg) {
        try {
            // get a SmsManager
            SmsManager smsManager = SmsManager.getDefault();
            // MessageActivity may exceed 160 characters
            // need to divide the message into multiples
            ArrayList<String> parts = smsManager.divideMessage(encryptedMsg);
            smsManager.sendMultipartTextMessage(recNumString, null, parts, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Convert byte to hex
    // code cite: https://sites.google.com/site/mobilesecuritylabware/3-data-location-privacy/lab-activity/cryptography/cryptography-mobile-labs/encryption-decryption/2-lab-activity/lab-1-encryption-decryption-on-sms-android-studio
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

    // encryption function
    //code cite: https://sites.google.com/site/mobilesecuritylabware/3-data-location-privacy/lab-activity/cryptography/cryptography-mobile-labs/encryption-decryption/2-lab-activity/lab-1-encryption-decryption-on-sms-android-studio
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
    //generating a secret key.
    //code cite: https://sites.google.com/site/mobilesecuritylabware/3-data-location-privacy/lab-activity/cryptography/cryptography-mobile-labs/encryption-decryption/2-lab-activity/lab-1-encryption-decryption-on-sms-android-studio
    private static Key generateKey(String secretKeyString) throws Exception {
        // generate secret key from string
        Key key = new SecretKeySpec(secretKeyString.getBytes(), "AES");
        return key;
    }

    // convert hex array to byte array
    //Code cite: https://sites.google.com/site/mobilesecuritylabware/3-data-location-privacy/lab-activity/cryptography/cryptography-mobile-labs/encryption-decryption/2-lab-activity/lab-1-encryption-decryption-on-sms-android-studio
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

    // decryption function.
    //Code cite: https://sites.google.com/site/mobilesecuritylabware/3-data-location-privacy/lab-activity/cryptography/cryptography-mobile-labs/encryption-decryption/2-lab-activity/lab-1-encryption-decryption-on-sms-android-studio
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
    //gets the in coming SMS message, decrypts it if it can, if not it displays the encrypted message.
    //Code cite: https://www.androidauthority.com/how-to-create-an-sms-app-721438/
    public void updateInbox(ConversationObject c) {
        Log.e("New MessageActivity coming in", c.toString());
        secretkey2 = (EditText) findViewById(R.id.secretkey);
        String secretkey = secretkey2.getText().toString();
        try {
            if(c.getBody().toString().contains("issa secret") && c.getAddress().equals(number) || c.getAddress().equals("+1"+ number)) {
                // convert the encrypted String message body to a byte
                // array
                String EncryptedMessage = c.getBody().toString().replace("issa secret", "");
                byte[] msg = hex2byte(EncryptedMessage.getBytes());
                // decrypt the byte array
                byte[] result = decryptSMS(secretkey, msg);
                String DecryptedMessage = new String(result);
                //just formatting the decrypted message
                if (DecryptedMessage.contains("Sent: ")) {
                    DecryptedMessage = DecryptedMessage.replace("Sent: ", "");
                }
                ConversationObject c1 = new ConversationObject("", DecryptedMessage, c.getAddress(),0);
                convo.add(c1);
                adapter.notifyDataSetChanged();
                Log.e("HEEEELLLOOOO", c1.toString());
            }else if(!c.getBody().toString().contains("issa secret") && (c.getAddress().equals(number) || c.getAddress().equals("+1"+ number))) {
                convo.add(c);
                adapter.notifyDataSetChanged();
            }
            listView.smoothScrollToPosition(convo.size()-1);
        } catch (Exception e) {
            // in the case of message corrupted or invalid key
            // decryption cannot be carried out
            convo.add(c);
            adapter.notifyDataSetChanged();
            Log.e("catch", "" + e);
        }
    }

    //gets all recieved messages in SMS inbox.
    //Code cite: https://www.androidauthority.com/how-to-create-an-sms-app-721438/
    public void refreshSmsInbox() {
        //this is a cursor to go through and get all of your recieved messages
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"),
                null, null, null, null);
        //this gets the message itsself
        int indexBody = smsInboxCursor.getColumnIndex("body");
        //this gets the address the message came from
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        //gets the date/time value so we can sort the messages in the correct order
        int indexDate = smsInboxCursor.getColumnIndex("date");
        //loop through and get received messages
        if (indexBody < 0 || !smsInboxCursor.moveToFirst())
            return;
        do {
            if (smsInboxCursor.getString(indexAddress).equals(number)||
                    smsInboxCursor.getString(indexAddress).equals("+1"+number)){
                ConversationObject c = new ConversationObject("",smsInboxCursor.getString(indexBody),smsInboxCursor.getString(indexAddress),smsInboxCursor.getLong(indexDate));
                recieved.add(c);
            }
        } while (smsInboxCursor.moveToNext());
    }

    //get sent messages
    //Code cite: https://www.androidauthority.com/how-to-create-an-sms-app-721438/
    public void refreshSmssent() {
        //this is a cursor to go through and get all of your recieved messages
        ContentResolver contentResolver = getContentResolver();
        Cursor smssentCursor = contentResolver.query(Uri.parse("content://sms/sent"),
                null, null, null, null);
        //this gets the message itsself
        int indexBody = smssentCursor.getColumnIndex("body");
        //this gets the address the message came from
        int indexAddress = smssentCursor.getColumnIndex("address");
        int indexDate = smssentCursor.getColumnIndex("date");
        //get messages the user sent
        if (indexBody < 0 || !smssentCursor.moveToFirst())
            return;
        do {
            if (smssentCursor.getString(indexAddress).equals(number)|| smssentCursor.getString(indexAddress).equals("+1"+number)){
                ConversationObject c = new ConversationObject(smssentCursor.getString(indexBody), "",smssentCursor.getString(indexAddress), smssentCursor.getLong(indexDate));
                sent.add(c);
            }
        } while (smssentCursor.moveToNext());
    }

    //combines the sent and recieved messages and sort them by their date and time
    public void createconvolist(){
        convo.clear();
        convo.addAll(sent);
        adapter.notifyDataSetChanged();
        convo.addAll(recieved);
        adapter.notifyDataSetChanged();
        Collections.reverse(convo);
        Collections.sort(convo, new Comparator<ConversationObject>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public int compare(ConversationObject o1, ConversationObject o2) {
                return Long.compare(o1.getTimeInMilliSeconds(), o2.getTimeInMilliSeconds());
            }
        });
    }

    //permissions to read from sms messages.
    //Code cite: https://www.androidauthority.com/how-to-create-an-sms-app-721438/
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
    //permissions to read sms stuff
    //code cite: https://www.androidauthority.com/how-to-create-an-sms-app-721438/
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
}//End of MessageActivity class
