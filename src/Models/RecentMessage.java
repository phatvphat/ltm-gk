/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Models;

/**
 *
 * @author phatv
 */
public class RecentMessage {

    public int id, sender_id, receiver_id, user_id;
    public String message, created_at, username;

    public RecentMessage() {
    }

    public RecentMessage(int id, int sender_id, int receiver_id, String message, String created_at, int user_id, String username) {
        this.id = id;
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
        this.message = message;
        this.created_at = created_at;
        this.user_id = user_id;
        this.username = username;
    }

    @Override
    public String toString() {
        int limit = 15;
        String shortMessage = message.length() >= limit ? message.substring(0, limit) + "..." : message;
        return (sender_id == user_id ? "Từ " : "Đến ") + username + ": " + shortMessage;
    }
}
