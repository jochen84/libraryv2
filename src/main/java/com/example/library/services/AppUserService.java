package com.example.library.services;

import com.example.library.entities.AppUser;
import com.example.library.repositories.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Cacheable(value = "libraryCache")
    public List<AppUser> findAll(){
        log.info("Request to find all users.");
        return appUserRepository.findAll();
    }

    public AppUser findById(String id){
        log.info(String.format("Request to find user with id: %s", id));
        return appUserRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find a user with that id"));
    }

    public AppUser findByUsername(String username){
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find a user with that username"));
    }

    public AppUser save(AppUser appUser){
        appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        return appUserRepository.save(appUser);
    }

    public void update(String id, AppUser appUser){
        if (!appUserRepository.existsById(id)){
            log.error("Could not find any user with that id");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find any user with that id");
        }
        var isSuperman = checkAuthority("ADMIN") || checkAuthority("LIBRARIAN");
        var isCurrentUser = SecurityContextHolder.getContext().getAuthentication().getName().toLowerCase().equals(appUser.getUsername().toLowerCase());
        if (!isSuperman && !isCurrentUser){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authorized to do this! Can only update your own details. *Admin/Librarian can update all");
        }
        log.info(String.format("%s was updated", appUser.getUsername()));
        appUser.setId(id);
        appUserRepository.save(appUser);
    }

    public void delete(String id){
        if (!appUserRepository.existsById(id)){
            log.error("Could not find any user with that id");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find a user with that id");
        }
        log.info(String.format("User with id: % was deleted", id));
        appUserRepository.deleteById(id);
    }

    private boolean checkAuthority(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().toUpperCase().equals("ROLE_" + role));
    }
}
