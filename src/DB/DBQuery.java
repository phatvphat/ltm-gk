/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author phatv
 */
public class DBQuery {

    DBAccess acc = new DBAccess();

    public ResultSet GetUser(int userid) {
        return acc.Query("SELECT * FROM user WHERE id = '" + userid + "'");
    }

    public ResultSet GetUser(String username) {
        return acc.Query("SELECT * FROM user WHERE username = '" + username + "'");
    }

    public ResultSet Login(String username, String password) {
        return acc.Query("SELECT * FROM user WHERE username = '" + username + "' AND password = '" + password + "'");
    }

    public int Register(String username, String password) {
        ResultSet rs = GetUser(username);
        try {
            if (rs.next()) {
                int kq = acc.Update("INSERT INTO user(username, password) VALUES('" + username + "','" + password + "')");
                if (kq != 0) {
                    return 1;
                }
                return -1;
            }
            return 0;
        } catch (SQLException ex) {
            return -1;
        }
    }

    public boolean AddFriend(int user_id, int friend_id) {
        if (user_id != friend_id) {
            ResultSet rs = GetUser(friend_id);
            try {
                if (rs.next()) {
                    int kq = acc.Update("INSERT INTO friends_list(user_id, friend_id) VALUES('" + user_id + "', '" + friend_id + "')");
                    if (kq != 0) {
                        return true;
                    }
                }
                return false;
            } catch (SQLException ex) {
                return false;
            }
        }
        return false;
    }

    public boolean AddFriend(int user_id, String friend_username) {
        ResultSet rs = GetUser(friend_username);
        try {
            if (rs.next()) {
                if (user_id != rs.getInt("id")) {
                    int kq = acc.Update("INSERT INTO friends_list(user_id, friend_id) VALUES('" + user_id + "', '" + rs.getInt("id") + "')");
                    if (kq != 0) {
                        return true;
                    }
                }
                return false;
            }
            return false;
        } catch (SQLException ex) {
            return false;
        }
    }

    public ResultSet ListFriends(int user_id) {
        return acc.Query("SELECT FL.id, user_id, friend_id, username, status FROM friends_list FL LEFT JOIN user U ON FL.friend_id = U.id WHERE user_id = '" + user_id + "' AND status = 1 UNION ALL SELECT FL.id, user_id, friend_id, username, status FROM friends_list FL LEFT JOIN user U ON FL.user_id = U.id WHERE friend_id = '" + user_id + "' AND status = 1");
    }

    public ResultSet FriendRequest(int user_id) {
        return acc.Query("SELECT FL.id, user_id, friend_id, username, status FROM friends_list FL LEFT JOIN user U ON FL.user_id = U.id WHERE friend_id = '" + user_id + "' AND status = 0");
    }

    public int AcceptRequest(int id) {
        return acc.Update("UPDATE friends_list SET status = 1 WHERE id = '" + id + "'");
    }

    public int DenyRequest(int id) {
        return acc.Update("DELETE FROM friends_list WHERE id = '" + id + "'");
    }

    public int DeleteFriend(int id) {
        return DenyRequest(id);
    }

    public ResultSet ListRecentMessages(int user_id) {
        return acc.Query("SELECT * FROM (SELECT M.*, U.id AS user_id, U.username FROM message M LEFT JOIN user U ON M.receiver_id = U.id WHERE sender_id = '" + user_id + "' UNION ALL SELECT M.*, U.id AS user_id, U.username FROM message M LEFT JOIN user U ON M.sender_id = U.id WHERE receiver_id = '" + user_id + "' ORDER BY id DESC) AS t GROUP BY sender_id, receiver_id ORDER BY id DESC");
    }

    public ResultSet GetMessages(int sender_id, int receiver_id) {
        return acc.Query("SELECT * FROM message WHERE sender_id = '" + sender_id + "' AND receiver_id = '" + receiver_id + "' UNION ALL SELECT * FROM message WHERE sender_id = '" + receiver_id + "' AND receiver_id = '" + sender_id + "' ORDER BY id ASC");
    }

    public int InsertMessage(int sender_id, int receiver_id, String message) {
        java.util.Date dt = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = sdf.format(dt);

        return acc.Update("INSERT INTO message(sender_id, receiver_id, message, created_at) VALUES('" + sender_id + "', '" + receiver_id + "', '" + message + "', '" + currentTime + "')");
    }
}
