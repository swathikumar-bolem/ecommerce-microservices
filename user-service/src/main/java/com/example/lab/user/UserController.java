package com.example.lab.user;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.*;
import java.util.*;
@Entity class AppUser { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; public String name; public String email; public AppUser(){} }
interface UserRepo extends JpaRepository<AppUser,Long> {}
@RestController class UserController { private final UserRepo repo; UserController(UserRepo repo){this.repo=repo;}
@GetMapping("/users") List<AppUser> all(){return repo.findAll();}
@GetMapping("/users/{id}") AppUser one(@PathVariable Long id){return repo.findById(id).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,"User missing"));}
@PostMapping("/users") AppUser create(@RequestBody AppUser u){u.id=null;return repo.save(u);} }
