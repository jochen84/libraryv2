package com.example.library.services;

import com.example.library.entities.AppUser;
import com.example.library.entities.Book;
import com.example.library.repositories.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final AppUserService appUserService;

    public List<Book> findAll(String title, String author, String genre, boolean sortByTitle, boolean sortByAuthor, boolean sortByGenre){
        log.info("Request to find all books");
        var books = bookRepository.findAll();
        if (title != null){
            books = books.stream()
                    .filter(book -> book.getTitle().toLowerCase().contains(title.toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (author != null){
            books = books.stream()
                    .filter(book -> book.getAuthor().toLowerCase().contains(author.toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (genre != null){
            books = books.stream()
                    .filter(book -> book.getGenre().contains(genre.toUpperCase()))
                    .collect(Collectors.toList());
        }
        if (sortByTitle){
            books.sort(Comparator.comparing(Book::getTitle));
        }
        if (sortByAuthor){
            books.sort(Comparator.comparing(Book::getAuthor));
        }
        if (sortByGenre){
            books.sort(Comparator.comparing(book -> book.getGenre().get(0)));
        }
        return books;
    }

    public Book findById(String id){
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find a book with that id"));
    }

    public Book save(Book book){
        return bookRepository.save(book);
    }

    public void update(String id, Book book){
        book.setId(id);
        bookRepository.save(book);
    }

    public void delete(String id){
        if(!bookRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find a book with that id");
        }
        bookRepository.deleteById(id);
    }


    public void borrowBook(String id){
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username == "anonymousUser"){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authorized to borrow books, login first");
        }
        var currentUser = appUserService.findByUsername(username);
        var book = findById(id);
        if (!book.isAvailable()){
            log.info("Someone tried to borrow a book not available");
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("%s is not available right now.", book.getTitle()));
        }
        log.info(String.format("%s borrowed %s",currentUser.getUsername(), book.getTitle()));
        book.setId(id);
        book.setAvailable(false);
        book.setBorrower(currentUser);
        currentUser.getBorrowedBooks().add("ISBN: " + book.getIsbn() + " | Title: " + book.getTitle());
        appUserService.update(currentUser.getId(), currentUser);
        update(id, book);
    }

    public void returnBook(String id){
        var book = findById(id);
        var currentBorrower = book.getBorrower();
        var isSuperman = checkAuthority("ADMIN") || checkAuthority("LIBRARIAN");
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        var currentUser = appUserService.findByUsername(username);
        if (book.isAvailable()){
            log.info(String.format("%s tried to return a book not borrowed", username));
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("%s is available right now, try borrowing it", book.getTitle()));
        }
        if (!isSuperman && currentUser.getUsername() != username && username == "anonymousUser"){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authorized to do this!");
        }
        if (!isSuperman && !currentUser.getUsername().equals(book.getBorrower().getUsername())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authorized to do this! You dont have that book.");
        }
        log.info(String.format("Someone returned %s", book.getTitle()));
        book.setId(id);
        book.setAvailable(true);
        book.setBorrower(null);
        currentBorrower.getBorrowedBooks().remove("ISBN: " + book.getIsbn() + " | Title: " + book.getTitle());
        appUserService.update(currentBorrower.getId(), currentBorrower);
        update(id, book);
    }

    private boolean checkAuthority(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().toUpperCase().equals("ROLE_" + role));
    }
}
