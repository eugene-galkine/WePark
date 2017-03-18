package com.eg.upark.net;

import android.util.Log;

import com.eg.upark.MainActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by Eugene Galkine on 8/3/2016.
 */

public class Client
{
    public static final Client instance = new Client();

    private DataOutputStream OutToServer;
    private BufferedReader InFromServer;
    private Socket ClientSocket;
    private String UserName;
    private clientListenThread listenThread;
    private clientConnectThread connectThread;

    private Client()
    {

    }

    public void connect (String androidID)
    {
        UserName = androidID;
        connect();
    }

    private void connect()
    {
        if (ClientSocket != null)
        {
            for (StackTraceElement ste : Thread.currentThread().getStackTrace())
            {
                System.out.println(ste);
            }
            return;
        }

        connectThread = new clientConnectThread();
        connectThread.start();
    }

    public void close()
    {
        System.out.println("disconnecting from server");

        //Don't try to close connection if we never even connected to begin with
        if (ClientSocket == null)
            return;

        try
        {
            //InFromServer.close();
            OutToServer.close();
            ClientSocket.close();
            ClientSocket = null;
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        System.out.println("disconnected from server");
    }

    public void resume()
    {
        connect();
    }

    private void sendMessage (String s)
    {
        try
        {
            OutToServer.writeBytes(s + '\n');
        } catch (Exception e) {e.printStackTrace();}
    }

    /* functions for actions */

    public void parkCar(double laditude, double longitude)
    {
        sendMessage("p" + laditude + "," + longitude);
    }

    public void findCar()
    {
        sendMessage("f");
    }

    public void isCarParked ()
    {
        sendMessage("i");
    }

    public void clearParking()
    {
        sendMessage("c");
    }

    public void getAvailableParking(double laditude, double longitude)
    {
        sendMessage("g" + laditude + "," + longitude);
    }

    private class clientListenThread extends Thread
    {
        @Override
        public void run()
        {
            try
            {
                String in;
                while ((in = InFromServer.readLine()) != null)
                {
                    if (!ClientSocket.isConnected())
                        break;

                    //remove null characters
                    in = in.replace(((char)0) + "", "");

                    Log.e("WePark", "received from server: " + in);
                    System.out.println("got a new message!!!!!!!!!!!!!!!!!!!!!");
                    MainActivity.getInstance().newMessage(in);
                    if (in.startsWith("a"))//CONNECTION_ACCEPTED
                    {
                        MainActivity.getInstance().ConnectionEstablished();
                    } else if (in.startsWith("d"))//CONNECTION_DENIED
                    {
                        MainActivity.getInstance().ConnectionDenied();
                        close();
                    } else if (in.startsWith("f"))//FIND_CAR
                    {
                        MainActivity.getInstance().SetParkLocation(in.substring(1));
                    } else if (in.startsWith("i"))//IS_PARKED
                    {
                        MainActivity.getInstance().SetIsParked(in.substring(1).startsWith("1"));
                    } else if (in.startsWith("n"))//NEW_PARKING
                    {
                        MainActivity.getInstance().NewParkingFound(in.substring(1));
                    }
                }
            } catch (Exception e)
            {
                //e.printStackTrace();
                System.out.println("Client Input thread had exception");
            }
        }
    }

    private class clientConnectThread extends Thread
    {
        @Override
        public void run()
        {
            try
            {
                InetAddress serverAddr = InetAddress.getByName("192.168.1.192");
                ClientSocket = new Socket();//serverAddr, 6005);
                ClientSocket.connect(new InetSocketAddress(serverAddr, 6005), 8000);

                OutToServer = new DataOutputStream(ClientSocket.getOutputStream());
                InFromServer = new BufferedReader(new InputStreamReader(ClientSocket.getInputStream()));
                sendMessage("r"+UserName);
                listenThread = new clientListenThread();
                listenThread.start();
            } catch (IOException e)
            {
                if (e.getClass().getCanonicalName().equals(SocketTimeoutException.class.getCanonicalName()))
                    MainActivity.getInstance().ConnectionError("Connection Timed Out");
                else if (e.getClass().getCanonicalName().equals(android.system.ErrnoException.class.getCanonicalName()))
                    MainActivity.getInstance().ConnectionError("Network is Unreachable");
                else
                    e.printStackTrace();
            }
        }
    }
}
