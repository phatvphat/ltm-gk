/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UDP;

import DB.DBQuery;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.Instant;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author phatv
 */
public class UDPServer {

    public static void main(String[] args) {
        int PORT = 4321;

        DBQuery query = new DBQuery();
        Client client = new Client();
        // Loại bỏ các client không có update trong 3 phút
        new Thread(() -> {
            int time = 60 * 3;
            while (true) {
                JSONObject listClients = client.getAll();
//                System.out.println("asd: " + listClients.toString());
                if (client.length() > 0) {
                    try {
                        JSONObject listClients_temp = new JSONObject(listClients.toString());

//                    JSONArray keys = listClients.names();
//                    for (int i = 0; i < keys.length(); ++i) {
//                        String value = listClients.getString(keys.getString(i));
//                        JSONObject jsonTemp = new JSONObject(String.valueOf(value));
//                        long start = Long.parseLong(String.valueOf(jsonTemp.get("time")));
//                        long end = Instant.now().getEpochSecond(); // in seconds
//                        System.out.println("Start: " + start + ", end: " + end);
//                        if (end - start >= 60 * 3) {
//                            listClients_temp.remove(keys.getString(i));
//                        }
//                    }
                        Iterator<?> iterator = listClients.keys();
                        while (iterator.hasNext()) {
                            Object key = iterator.next();
                            Object value = listClients.get(key.toString());
                            JSONObject jsonTemp = new JSONObject(String.valueOf(value));
                            long start = Long.parseLong(String.valueOf(jsonTemp.get("time")));
                            long end = Instant.now().getEpochSecond(); // in seconds
//                        System.out.println("Start: " + start + ", end: " + end);
                            if (end - start >= time) {
                                listClients_temp.remove(key.toString());
                                System.out.println("Removed client id: " + key.toString());
                            }
                        }
//                        System.out.println("qwe: " + listClients_temp.toString());
                        client.putALl(listClients_temp.toString());
                    } catch (Exception e) {
                        System.out.println("Error filter client: " + e.toString());
//                        System.out.println("JSON debug: " + client.getAll().toString());
                    }
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();

        DatagramSocket socket;
        try {
            byte[] buffer = new byte[1024];
            socket = new DatagramSocket(PORT);
            DatagramPacket receivePacket;
            String stReceive;
            while (true) {
                receivePacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(receivePacket);
                stReceive = new String(receivePacket.getData(), 0, receivePacket.getLength());

                try {
                    System.out.println("----------\nReceived: " + stReceive);
                    JSONObject jsonObj = new JSONObject(stReceive);
                    String action = String.valueOf(jsonObj.get("action"));
                    String sender_id = String.valueOf(jsonObj.get("sender_id"));
//                    System.out.println(listClients.optString(sender_id));

                    InetAddress address = receivePacket.getAddress();
//                    InetAddress address = InetAddress.getByName(String.valueOf(jsonObj.get("address")));
                    int port = receivePacket.getPort();
//                    int port = Integer.parseInt(String.valueOf(jsonObj.get("port")));

                    if (action.equals("join")) {
                        JSONObject jsonTemp = new JSONObject();
                        jsonTemp.put("address", address.toString());
                        jsonTemp.put("port", port);
                        jsonTemp.put("time", Instant.now().getEpochSecond());
                        System.out.println("Json temp: " + jsonTemp.toString());
                        client.put(sender_id, jsonTemp.toString());
                    } else if (action.equals("chat")) {
                        System.out.println("From: " + address.toString() + ":" + port);
                        String receiver_id = String.valueOf(jsonObj.get("receiver_id"));
                        String message = jsonObj.getString("message");
                        System.out.println("message: " + message);
                        // Save chat to db
                        query.InsertMessage(Integer.parseInt(sender_id), Integer.parseInt(receiver_id), message);
                        if (client.getString(receiver_id) != null) {
                            JSONObject jsonTemp = new JSONObject(client.getString(receiver_id));
                            // send for receiver
                            System.out.println("To receiver: " + jsonTemp.get("address") + ":" + jsonTemp.get("port"));
                            DatagramPacket outsending1 = new DatagramPacket(stReceive.getBytes(), receivePacket.getLength(),
                                    InetAddress.getByName(String.valueOf(jsonTemp.get("address")).replace("/", "")), Integer.parseInt(String.valueOf(jsonTemp.get("port"))));
                            socket.send(outsending1);
                        }
                        // send again for sender
                        System.out.println("To sender: " + address + ":" + port);
                        DatagramPacket outsending2 = new DatagramPacket(stReceive.getBytes(), receivePacket.getLength(),
                                address, port);
                        socket.send(outsending2);

                        System.out.println("Str length: " + stReceive.length());
                        System.out.println("receivePacket length: " + receivePacket.getLength());
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e.toString());
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }
}
