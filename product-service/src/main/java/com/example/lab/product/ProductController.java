package com.example.lab.product;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.*;
import java.util.*;
@Entity class Product { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; public String name; public Integer priceCents; public Product(){} }
interface ProductRepo extends JpaRepository<Product,Long> {}
@RestController class ProductController { private final ProductRepo repo; ProductController(ProductRepo repo){this.repo=repo;}
@GetMapping("/products") List<Product> all(){return repo.findAll();}
@GetMapping("/products/{id}") Product one(@PathVariable Long id){return repo.findById(id).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,"Product missing"));}
@PostMapping("/products") Product create(@RequestBody Product p){p.id=null;return repo.save(p);} }
