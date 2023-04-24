package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

public class User implements Serializable {
    private String name;
    private String password;
    private boolean logged;

    public User() {
    }

    public User(String name, String password) {
        this.name = name;
        this.password = password;
        this.logged = false;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public boolean isLogged() {
        return logged;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
