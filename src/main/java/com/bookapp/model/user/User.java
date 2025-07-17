// src/main/java/com/bookapp/model/user/User.java

package com.bookapp.model.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor; // Remove this if you add the constructor manually

@Data
@NoArgsConstructor
@AllArgsConstructor // You can remove this line if you add the constructor manually below
public class User {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("email")
    private String email;
    @JsonProperty("password")
    private String password;
}