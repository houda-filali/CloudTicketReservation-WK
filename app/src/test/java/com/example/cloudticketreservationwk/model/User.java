package com.cloudticketreservation.model;

public class User {
    // this class should handle all attributes related to users and contains no business logic.
    private String id;
    private String email;

    public User (String id, String email) {
        this.id = id;
        this.email = email;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

}
