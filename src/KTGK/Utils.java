/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package KTGK;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.swing.JFrame;

/**
 *
 * @author phatv
 */
public class Utils {

    private final static int VIBRATION_LENGTH = 15;
    private final static int VIBRATION_VELOCITY = 20;

    public static void vibrate(JFrame frame) {
        try {
            final int originalX = frame.getLocationOnScreen().x;
            final int originalY = frame.getLocationOnScreen().y;
            for (int i = 0; i < VIBRATION_LENGTH; i++) {
                Thread.sleep(10);
                frame.setLocation(originalX, originalY + VIBRATION_VELOCITY);
                Thread.sleep(10);
                frame.setLocation(originalX, originalY - VIBRATION_VELOCITY);
                Thread.sleep(10);
                frame.setLocation(originalX + VIBRATION_VELOCITY, originalY);
                Thread.sleep(10);
                frame.setLocation(originalX, originalY);
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public static String hashMD5(String input) {
        try {
            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            // digest() method is called to calculate message digest
            // of an input digest() return array of byte
            byte[] messageDigest = md.digest(input.getBytes());
            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void wait(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }
}
