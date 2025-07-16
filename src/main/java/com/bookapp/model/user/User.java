// src/main/java/com/bookapp/model/user/User.java

package com.bookapp.model.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor; // Remove this if you add the constructor manually

@Data
@NoArgsConstructor
// @AllArgsConstructor // You can remove this line if you add the constructor manually below
public class User {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("email")
    private String email;
    @JsonProperty("password")
    private String password;

    // Manual Constructor for creating a user with ID, email, and password
    // This explicitly defines the constructor that was implicitly expected from @AllArgsConstructor
    public User(Integer id, String email, String password) {
        this.id = id;
        this.email = email;
        this.password = password;
    }

    // You still keep @NoArgsConstructor or manually add:
    // public User() { }
}