package com.example.library.entities;

import lombok.Data;
import org.hibernate.validator.constraints.ISBN;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.List;

@Data
public class Book implements Serializable {

    private static final long serialVersionUID = 1578783440421245204L;

    @Id
    private String id;
    //@ISBN //Under testning utkommenterad
    private String isbn;
    @NotEmpty()
    @Size(min = 1, max = 255, message = "Book title can be max 255 letters long")
    private String title;
    @NotEmpty(message = "Book plot is not allowed to be empty")
    @Size(min = 3, max = 255)
    private String plot;
    @NotEmpty(message = "Books author is not allowed to be empty")
    @Size(min = 3, max = 50)
    private String author;
    private List<String> genre;
    private boolean isAvailable;
    private AppUser borrower;
}
