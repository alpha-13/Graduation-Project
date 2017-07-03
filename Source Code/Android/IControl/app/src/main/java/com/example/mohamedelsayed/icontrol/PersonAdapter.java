package com.example.mohamedelsayed.icontrol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Mohamed Elsayed on 13/02/2017.
 */

/*
    This Class used as ListView Items which list all unauthorized users from the system
    Each item in this list contains unauthorized person Image and the date of access
    We need also unauthorized ID which used for deleting him/her from android database and from Raspberry
      folder or move his/her Image
 */

public class PersonAdapter extends BaseAdapter {

    private Context context;
    public static ArrayList<UnPerson> UnPersons;

    public PersonAdapter(Context context, ArrayList<UnPerson> UnPersons){
        this.context = context;
        this.UnPersons = UnPersons;
    }

    // Get count of all unauthorized users
    @Override
    public int getCount() {
        return UnPersons.size();
    }


    // get unauthorized information: (ID, Image, AccessDate)
    @Override
    public Object getItem(int position) {
        return UnPersons.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    // Get view which will used in listView this view Inflate with (custom_row) xml file
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = null;

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.custom_row, parent, false);
        }
        else {
            row = convertView;
        }

        // get the TextView and ImageView which will show unauthorized person information

        TextView txtView = (TextView)row.findViewById(R.id.txtView);
        CircleImageView Img = (CircleImageView) row.findViewById(R.id.Img);

        // set Image and AccessDate to item

        txtView.setText(UnPersons.get(position).AccessTime);
        Img.setImageBitmap(UnPersons.get(position).Image);

        return row;
    }

    // Remove Person from UnPerson ArrayList and update the view
    // Removed person from this list will be remove to from the database after calling this method
    public void Remove(int ID){
        ArrayList<Integer> Positions = new ArrayList<>();

        // If the Image of the same person is sent more than one time we need to remove all, so we use this loop
        for (Integer i=0; i< UnPersons.size(); i++) {
            if(UnPersons.get(i).ID == ID)
                Positions.add(i);
        }

        // Remove All matched persons
        for(Integer j = Positions.size()-1; j > -1; j--) {
            int index = Positions.get(j);
            UnPersons.remove(index);
        }

        notifyDataSetChanged();
    }
}
