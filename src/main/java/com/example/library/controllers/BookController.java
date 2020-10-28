package com.example.library.controllers;

import com.example.library.entities.AppUser;
import com.example.library.entities.Book;
import com.example.library.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping
    public ResponseEntity<List<Book>> findAllBooks(@RequestParam(required = false) String title, @RequestParam(required = false) String author, @RequestParam(required = false) String genre, @RequestParam(required = false) boolean sortByTitle,@RequestParam(required = false) boolean sortByAuthor,@RequestParam(required = false) boolean sortByGenre){
        return ResponseEntity.ok(bookService.findAll(title, author, genre, sortByTitle, sortByAuthor, sortByGenre));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> findBookById(@PathVariable String id){
        return ResponseEntity.ok(bookService.findById(id));
    }

    @Secured({"ROLE_ADMIN", "ROLE_LIBRARIAN"})
    @PostMapping
    public ResponseEntity<Book> save(@RequestBody Book book){
        return ResponseEntity.ok(bookService.save(book));
    }

    @Secured({"ROLE_ADMIN", "ROLE_LIBRARIAN"})
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bookUpdate(@PathVariable String id,@Validated @RequestBody Book book){
        bookService.update(id, book);
    }

    @Secured({"ROLE_ADMIN", "ROLE_LIBRARIAN"})
    @DeleteMapping("/{id}")
    public void bookDelete(@PathVariable String id){
        bookService.delete(id);
    }

    @Secured({"ROLE_ADMIN", "ROLE_LIBRARIAN", "ROLE_USER"})
    @PutMapping("/borrow/{id}")
    public void borrowBook(@PathVariable String id){
        bookService.borrowBook(id);
    }

    @Secured({"ROLE_ADMIN", "ROLE_LIBRARIAN", "ROLE_USER"})
    @PutMapping("/return/{id}")
    public void returnBook(@PathVariable String id){
        bookService.returnBook(id);
    }

}
