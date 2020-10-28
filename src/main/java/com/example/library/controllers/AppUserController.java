package com.example.library.controllers;

import com.example.library.entities.AppUser;
import com.example.library.services.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class AppUserController {

    @Autowired
    private AppUserService appUserService;

    @Secured({"ROLE_ADMIN", "ROLE_LIBRARIAN"})
    @GetMapping
    public ResponseEntity<List<AppUser>> findAllUsers(){
        return ResponseEntity.ok(appUserService.findAll());
    }

    @Secured({"ROLE_ADMIN", "ROLE_LIBRARIAN"})
    @GetMapping("/{id}")
    public ResponseEntity<AppUser> findUserById(@PathVariable String id){
        return ResponseEntity.ok(appUserService.findById(id));
    }

    @Secured({"ROLE_ADMIN", "ROLE_LIBRARIAN"})
    @PostMapping
    public ResponseEntity<AppUser> save(@Validated @RequestBody AppUser appUser){
        return ResponseEntity.ok(appUserService.save(appUser));
    }

    @Secured({"ROLE_ADMIN", "ROLE_LIBRARIAN", "ROLE_USER"})
    @PutMapping("/{id}")
    public void userUpdate(@PathVariable String id,@Validated @RequestBody AppUser appUser){
        appUserService.update(id, appUser);
    }

    @Secured({"ROLE_ADMIN"})
    @DeleteMapping("/{id}")
    public void userDelete(@PathVariable String id){
        appUserService.delete(id);
    }
}
