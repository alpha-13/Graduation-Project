package com.example.mohamedelsayed.icontrol;

import android.content.Context;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Mohamed Elsayed on 15/02/2017.
 */

/*
    This class will send all instructions from user to raspberry in message
     Instructions like:-
        1. Checking unauthorized users
        2. Control LEDs
        3. Control Fans/ Windows
        4. Setting alarm
        5. Control music

   This class used for getting some information from raspberry like: -
        1. Temperature
        2. Humidity
        3. Light now
        4. Get all music play list
*/

public class SendToServer extends AsyncTask<Void, Void, Void> {
    Socket socket;
    Context context;
    String Message;

    public static String ResponseMessage = null;
    public static String MusicFiles;

    public SendToServer(Context context, String Message){
        this.context = context;
        this.Message = Message;
    }

    @Override
    protected Void doInBackground(Void... params) {

        try {
            socket = new Socket(MainActivity.ServerIPAddress, 9000);

            // Convert string data to byte array for transfer it in stream of bytes
            byte[] array = Message.getBytes();
            OutputStream out = socket.getOutputStream();
            out.write(array, 0, array.length);

            Thread.sleep(50);

            // If we need to get some information or all music
            if(Message.contains("Get") || Message.contains("Music")){
                InputStream inputStream = socket.getInputStream();

                if (inputStream.available() > 0) {
                    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    while ((bytesRead = inputStream.read(buffer)) != 0) {
                        byteArrayOutputStream.write(buffer, 0, bytesRead);
                        if (inputStream.available() == 0) break;
                    }

                    // Convert byte array to string
                    final String Response = new String(byteArrayOutputStream.toByteArray());

                    if(Message.contains("Get"))
                        ResponseMessage = Response;
                    else if(Message.contains("Music"))
                        MusicFiles = Response.substring(1, Response.length() - 1);

                    MainActivity.SendState = true;
                }
            }
            out.close();

        } catch (UnknownHostException e) {
            MainActivity.SendState = false;
            e.printStackTrace();
        } catch (IOException e) {
            MainActivity.SendState = false;
            e.printStackTrace();
        } catch (Exception e) {
            MainActivity.SendState = false;
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

}
