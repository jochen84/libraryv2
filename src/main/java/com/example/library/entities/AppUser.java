package com.example.library.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.List;

@Data
public class AppUser implements Serializable {

    private static final long serialVersionUID = -2332408320138313003L;

    @Id
    private String id;
    @Email(message = "Must be a valid email")
    private String email;
    @NotBlank
    @Size(min = 3, max = 10, message = "Username must be between 3 and 10 characters")
    private String username;
    @NotBlank
    @Size(min = 3, max = 10, message = "Username must be between 3 and 10 characters")
    private String password;
    private List<String> acl;
    private List<String> borrowedBooks;
}
