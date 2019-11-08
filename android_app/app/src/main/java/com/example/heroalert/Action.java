package com.example.heroalert;

public class Action {

    private String actionTitle;
    private String address;

    public Action(String actionTitle, String address) {
        this.actionTitle = actionTitle;
        this.address = address;
    }

    public String getActionTitle() {
        return this.actionTitle;
    }

    public String getAddress() {
        return this.address;
    }

}
