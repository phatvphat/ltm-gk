package Models;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author phatv
 */
public class Friend {

    public int id;
    public int user_id, friend_id, status;
    public String username;

    public Friend() {
    }

    public Friend(int id, int user_id, int friend_id, int status, String username) {
        this.id = id;
        this.user_id = user_id;
        this.friend_id = friend_id;
        this.status = status;
        this.username = username;
    }

    @Override
    public String toString() {
        return username;
    }
}
