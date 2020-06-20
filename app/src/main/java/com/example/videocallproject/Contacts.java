package com.example.videocallproject;

public class Contacts
{
    String Name, Status, image, uid;


    public Contacts()
    {

    }

    public Contacts(String name, String status, String image, String uid) {
        Name = name;
        Status = status;
        this.image = image;
        this.uid = uid;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
