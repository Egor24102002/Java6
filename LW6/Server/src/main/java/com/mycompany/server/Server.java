/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.server;

import java.io.IOException;
import static java.lang.Math.cos;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Dan
 */
class FunctionIntegral {

    public double f(double x) {
        double F = cos(x);
        return F;
    }
}

class MyThread extends Thread {

    DatagramSocket dSocket;
    InetAddress iAddress;
    private int strTop;
    private int strBottom;
    private float strStep;
    private int num;

    MyThread(String name, int strTOP, int strBOTTOM, float strSTEP, int N, DatagramSocket dSOCKET, InetAddress iADDRESS) {
        super(name);
        strTop = strTOP;
        strBottom = strBOTTOM;
        strStep = strSTEP;
        num = N;
        dSocket = dSOCKET;
        iAddress = iADDRESS;
    }

    public void run() {
        FunctionIntegral funk = new FunctionIntegral();
        double n, x1, x2;
        int j, a, b;
        double result = 0;
        if ((int) strBottom < (int) strTop) {
            a = (int) strBottom;
            b = (int) strTop;
        } else {
            a = (int) strTop;
            b = (int) strBottom;
        }

        n = (int) ((b - a) / (float) strStep);
        for (j = 0; j < n - 1; j++) {
            x1 = a + j * (float) strStep;
            x2 = a + (float) strStep * (j + 1);
            result += 0.5 * (x2 - x1) * (funk.f(x1) + funk.f(x2));
        }
        if ((n - 1) * (float) strStep < b) {
            float newstep = (float) (b - (n - 1) * (float) strStep);
            x1 = a + (n - 1) * (float) strStep;
            result += 0.5 * (b - x1) * (funk.f(x1) + funk.f(b));
        }
        if ((int) strBottom < (int) strTop) {
            result = result * (-1);
        }
//        пиздец
        String message = String.valueOf(result) + ' ' + String.valueOf(num);
        byte[] buff = message.getBytes();
        DatagramPacket dpacket = new DatagramPacket(buff, buff.length, iAddress, 4444);
        try {
            dSocket.send(dpacket);
        } catch (IOException ex) {
            Logger.getLogger(MyThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}

public class Server {

    public static void main(String[] args) throws SocketException, UnknownHostException, IOException {
        DatagramSocket socketGet = new DatagramSocket(3333);
        DatagramSocket socketPost = new DatagramSocket();
        InetAddress address = InetAddress.getByName("localhost");
        while (true) {
            byte[] buffer = new byte[256];
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            socketGet.receive(request);
            if (request.getLength() != 0) {
                String Message = new String(request.getData(), 0, request.getLength());
                String strTop = "",
                        strLower = "",
                        strStep = "",
                        strNum = "";

                int size = Message.length();

                int j = 0;
                while (Message.charAt(j) != ' ') {
                    strTop += Message.charAt(j);
                    j++;
                }
                j++;

                while (Message.charAt(j) != ' ') {
                    strLower += Message.charAt(j);
                    j++;
                }
                j++;

                while (Message.charAt(j) != ' ') {
                    strStep += Message.charAt(j);
                    j++;
                }
                j++;

                while (j != size) {
                    strNum += Message.charAt(j);
                    j++;
                }

                MyThread myThread = new MyThread("MyThread", Integer.parseInt(strTop), Integer.parseInt(strLower), Float.parseFloat(strStep), Integer.parseInt(strNum), socketPost, address);
                myThread.start();
            }
        }
    }
}
