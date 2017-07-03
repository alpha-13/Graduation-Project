package com.example.mohamedelsayed.icontrol;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/*
    This class used for checking unauthorized users and get decision about any person if he/she ..
        authorized in the system or not
*/

public class Users extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener{

    ListView lstUnAuthorized, lstUsers;

    CircleImageView Img;
    TextView txtName, txtAccess;
    Button btnConfirm, btnUn;

    ArrayList<Person> Users;
    ArrayList<UnPerson> UnUsers;

    PersonAdapter MyAdapter;

    DataBase DB;

    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_users);

        lstUnAuthorized = (ListView)findViewById(R.id.lstUnAuthorized);
        lstUsers = (ListView)findViewById(R.id.lstUsers);

        Img = (CircleImageView)findViewById(R.id.Img);
        txtName = (TextView)findViewById(R.id.txtName);
        txtAccess = (TextView)findViewById(R.id.txtAccess);
        btnConfirm = (Button)findViewById(R.id.btnConfirm);
        btnUn = (Button)findViewById(R.id.btnUn);


        btnConfirm.setOnClickListener(this);
        btnUn.setOnClickListener(this);

        DB = new DataBase(this);

        alertDialog = new AlertDialog.Builder(this).create();

        // Get All authorized users names and ID
        // Get all unAuthorized users Images, AccessDate
        Users = DB.GetAllUsers();
        UnUsers = DB.GetUnAuthorizedUsers();

        // Add all unauthorized users
        if(UnUsers != null) {
            MyAdapter = new PersonAdapter(this, UnUsers);
            lstUnAuthorized.setAdapter(MyAdapter);
            lstUnAuthorized.setOnItemClickListener(this);
        }

        // Add all users names
        if(Users != null){
            ArrayList<String> Names = new ArrayList<>();
            for (Person P: Users)
                Names.add(P.Name);

            lstUsers.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Names));
            lstUsers.setOnItemClickListener(this);
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Change unauthorized user Image and setTag to use it later to get next Image
        if(parent.getId() == R.id.lstUnAuthorized) {
            Img.setImageBitmap(UnUsers.get(position).Image);
            txtAccess.setText(UnUsers.get(position).AccessTime);
            Img.setTag(position);
        }
        // Change User name and set tag
        else if(parent.getId() == R.id.lstUsers) {
            txtName.setText(Users.get(position).Name);
            txtName.setTag(position);
        }
    }

    @Override
    public void onClick(View v) {
        int ImgPosition = -1;
        if(Img.getTag() != null)
            ImgPosition = (Integer) Img.getTag();

        if(v.getId() == btnConfirm.getId()) {
            if(MainActivity.SendState) {
                if (txtName.getText() != "" && Img.getDrawable() != null) {
                    new SendToServer(this, "Accept::ID:" + UnUsers.get(ImgPosition).ID + ":ToID:" + Users.get((Integer) txtName.getTag()).ID).execute();
                    Toast.makeText(this, "Accepted ID: " + UnUsers.get(ImgPosition).ID + " Name: " + txtName.getText() + " ID: " + Users.get((Integer) txtName.getTag()).ID, Toast.LENGTH_SHORT).show();

                    // Get next Image
                    if (ImgPosition > -1)
                        NextImage(ImgPosition);
                }
            }
            else {
                // Toast.makeText(this, "Connection failed !", Toast.LENGTH_LONG).show();
                showAlertDialog("Receive only !", "You can only receive data and can't send any instruction to the system!\nDo you want to try connection?!", R.drawable.cloud_download, false);
            }
        }
        else if(v.getId() == btnUn.getId()){
            if (Img.getDrawable() != null) {
                if (MainActivity.SendState) {
                    new SendToServer(this, "NotAccepted::ID:" + UnUsers.get(ImgPosition).ID).execute();

                    Toast.makeText(this, "NotAccepted ID: " + UnUsers.get(ImgPosition).ID, Toast.LENGTH_SHORT).show();

                    // Get next Image
                    if (ImgPosition > -1)
                        NextImage(ImgPosition);
                }
                else {
                    //Toast.makeText(this, "Connection failed !", Toast.LENGTH_LONG).show();
                    showAlertDialog("Receive only !", "You can only receive any images and can't send any instruction!\nDo you want to try connection?!", R.drawable.cloud_download, false);
                }
            }
        }
    }

    private void showAlertDialog(String title, String Message, int iconID, boolean isCanelable){
        if(!alertDialog.isShowing()) {
            alertDialog.setTitle(title);
            alertDialog.setMessage(Message);
            alertDialog.setIcon(iconID);
            if (!isCanelable) {
                alertDialog.setButton(Dialog.BUTTON_POSITIVE,"Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (!MainActivity.ReceiveState)
                            startService(new Intent(getBaseContext(), ReceiveImg.class));

                        if (!MainActivity.SendState) {
                            new SendToServer(getApplicationContext(), "Get").execute();
                        }
                    }
                });
                alertDialog.setButton(Dialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Toast.makeText(getApplicationContext(), "You clicked on NO", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });
            } else {
                alertDialog.setButton(Dialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
            }
            alertDialog.show();
        }
    }


    // Get next image to show it for user and delete current unauthorized user from database and from listView adapter
    private void NextImage(int position){
        final int ID = UnUsers.get(position).ID;

        final Handler h3 = new Handler();
        h3.postDelayed(new Runnable() {
            public void run() {
                int c = DB.DeleteUnUser(ID);
               // Toast.makeText(getApplicationContext(), "Count: " + c, Toast.LENGTH_LONG).show();
            }
        }, 100);

        MyAdapter.Remove(ID);
        UnUsers = PersonAdapter.UnPersons;

        if(UnUsers.size() > 0) {
            if(position == UnUsers.size())
                position = 0;

            try{
                Img.setImageBitmap(UnUsers.get(position).Image);
                txtAccess.setText(UnUsers.get(position).AccessTime);
                Img.setTag(position);
            }
            catch (Exception e){

            }
        }
        else {
            Img.setImageDrawable(null);
            Img.setTag(null);
            txtName.setText(null);
            txtAccess.setText(null);
        }
    }
}
