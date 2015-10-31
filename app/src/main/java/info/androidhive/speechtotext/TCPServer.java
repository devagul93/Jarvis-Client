package info.androidhive.speechtotext;//TCPServer.java

import android.content.Context;
import android.os.Handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

class TCPServer
{
    public static TCPServer mInstance;
    public static MainActivity activity;
    public static  TCPServer getInstance(Context context){
        if(mInstance ==null){
            mInstance = new TCPServer();
        }
        activity = (MainActivity) context;
        return mInstance;
    }

    public static String mess;

    public static void createServer(String message){

        String fromclient = null;
        String toclient =  null;

        ServerSocket Server = null;
        try {
            Server = new ServerSocket(5000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println ("TCPServer Waiting for client on port 5000");

        while(true)
        {
            Socket connected = null;
            try {
                connected = Server.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println( " THE CLIENT"+" "+
                    connected.getInetAddress() +":"+connected.getPort()+" IS CONNECTED ");

            BufferedReader inFromUser =
                    new BufferedReader(new InputStreamReader(System.in));

            BufferedReader inFromClient =
                    null;
            try {
                inFromClient = new BufferedReader(new InputStreamReader(connected.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            PrintWriter outToClient =
                    null;
            try {
                outToClient = new PrintWriter(
                        connected.getOutputStream(),true);
            } catch (IOException e) {
                e.printStackTrace();
            }

                System.out.println("SEND(Type Q or q to Quit):");
                try {
//                    toclient = inFromUser.readLine();
                    toclient = mess;

                } catch (Exception e) {
                    e.printStackTrace();
                }

            if (outToClient != null) {
                outToClient.println(toclient);
            }

            try {
                if (inFromClient != null) {
                    fromclient = inFromClient.readLine();
                    System.out.println(fromclient);
                    MainActivity.message = fromclient;
                    Handler mainHandler = new Handler(activity.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            activity.speakOut();
                        } // This is your code
                    };
                    mainHandler.post(myRunnable);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}