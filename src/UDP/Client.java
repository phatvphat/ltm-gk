/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UDP;

import org.json.JSONObject;

/**
 *
 * @author phatv
 */
public class Client {

    JSONObject listClients;

    public Client() {
        listClients = new JSONObject();
        System.out.println("init clients: " + listClients.toString());
    }

    public JSONObject getAll() {
        return listClients;
    }

    public Object get(String key) {
        if (listClients.isNull(key)) {
            return null;
        } else {
            return listClients.get(key);
        }
    }

    public String getString(String key) {
        if (listClients.isNull(key)) {
            return null;
        } else {
            return listClients.getString(key);
        }
    }

    public void putALl(String data) {
//        System.out.println("Clear all.");
        listClients.clear();
//        System.out.println("After clear all: " + listClients.toString());
        listClients = new JSONObject(data);
//        System.out.println("After set all: " + listClients.toString());
    }

    // Insert or Update
    public void put(String key, String data) {
        listClients.put(key, data);
    }

    public void put(String key, int data) {
        listClients.put(key, data);
    }

    public void clear() {
        listClients.clear();
    }

    public void remove(String key) {
        if (!listClients.isNull(key)) {
            listClients.remove(key);
        }
    }

    public int length() {
        return listClients.length();
    }
}
