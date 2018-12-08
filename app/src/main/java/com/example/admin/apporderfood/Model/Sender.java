package com.example.admin.apporderfood.Model;

public class Sender {
    public String to;
    public Notification notification;


    public Sender(String to, Notification notification) {
        this.to = to;
        this.notification = notification;
    }
}
