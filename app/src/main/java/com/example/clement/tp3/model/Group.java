package com.example.clement.tp3.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Clement on 18/06/15.
 */
public class Group {

    private String name;
    private ArrayList<String> users;

    public Group(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addUser(String email) {
        users.add(email);
    }

    public List<String> getUsers() {
        return this.users;
    }

    public void setUsers(List<String> users) {
        this.users = new ArrayList<String>(users);
    }

}
