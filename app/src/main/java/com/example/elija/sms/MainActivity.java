package com.example.elija.sms;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class MainActivity extends AppCompatActivity  {

    private ContactsAdapter adapter;
    public ArrayList<ContactObject> contactObject;
    public String title;
    public ListView listView;
    public String number;
    public String secretkey2;
    public String secretkey;
    public EditText mysecretkey;
    String FILENAME = "UsersSecretKey";
    FileOutputStream fos;
    String secret;
    private final BroadcastReciever myReciever = new BroadcastReciever();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getting permission to access our contactObjects
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_CONTACTS},1);

        contactObject = new ArrayList<ContactObject>();
        //gets contactObject names and numbers from our phones
        //Code cite: https://www.youtube.com/watch?v=g4_1UOFNLEY
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI,null,null,null,null);
        while(cursor.moveToNext()){
           String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            Cursor phoneCurser = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID+ "= ?", new String[]{id},null);
            while(phoneCurser.moveToNext()){
                String phoneNumber = phoneCurser.getString(phoneCurser.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                contactObject.add(new ContactObject(name,phoneNumber, ""));
            }
        }

        //alphabetizes the contactObject list
        Collections.sort(contactObject, new Comparator<ContactObject>() {
            @Override
            public int compare(ContactObject o1, ContactObject o2) {
                return o1.getContact().compareToIgnoreCase(o2.contact);
            }
        });
        //puts our contactObjects in the array adapter
        adapter = new ContactsAdapter(this, contactObject);
        listView = (ListView)findViewById(R.id.list);
        listView.setAdapter(adapter);

        mysecretkey = (EditText)findViewById(R.id.mysecretkey);
        //get secret key from internal storage
        try {
            FileInputStream fin = openFileInput(FILENAME);
            int c;
            String temp = "";
            while((c= fin.read())!= -1){
                temp = temp+ Character.toString((char)c);
                mysecretkey.setText(temp);
            }
           fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //opens up MessageActivity.java when you click on a contactObject
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                title = contactObject.get(position).getContact();
                number = contactObject.get(position).getNumber();
                secretkey2 = contactObject.get(position).getSecretkey();
                mysecretkey = (EditText)findViewById(R.id.mysecretkey);
                secretkey = mysecretkey.getText().toString();
                if(mysecretkey.length() == 16) {
                    String string = mysecretkey.getText().toString();
                    //store secret key to internal storage
                    try {
                        fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                        fos.write(string.getBytes());
                        Log.e("HEY", fos.toString());
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //get all the relevant variables from this class and give it to the message class.
                    Intent intent = new Intent(MainActivity.this, MessageActivity.class);
                    intent.putExtra("mykey", secretkey);
                    intent.putExtra("title", title);
                    intent.putExtra("number", number);
                    intent.putExtra("contactObject", contactObject);
                    startActivity(intent);
                }else{
                    Toast.makeText(getBaseContext(), "Please make a 16 character long secret key",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}//end of MainActivity
