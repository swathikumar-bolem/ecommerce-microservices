package com.example.lab.inventory;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Entity class Stock { @Id public Long productId; public Integer quantity; public Stock(){} }
interface StockRepo extends JpaRepository<Stock,Long> {}
@RestController class InventoryController { private final StockRepo repo; InventoryController(StockRepo repo){this.repo=repo;}
@GetMapping("/inventory/{id}") Stock one(@PathVariable Long id){return repo.findById(id).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,"No stock record"));}
@PutMapping("/inventory/{id}") Stock set(@PathVariable Long id,@RequestBody Stock s){s.productId=id;return repo.save(s);}
@PostMapping("/inventory/{id}/reserve") @Transactional synchronized Stock reserve(@PathVariable Long id,@RequestParam Integer quantity){if(quantity==null||quantity<=0)throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,"Quantity must be positive"); Stock s=one(id);if(s.quantity<quantity)throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT,"Insufficient stock");s.quantity-=quantity;return repo.save(s);}
@PostMapping("/inventory/{id}/release") @Transactional synchronized Stock release(@PathVariable Long id,@RequestParam Integer quantity){if(quantity==null||quantity<=0)throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,"Quantity must be positive");Stock s=one(id);s.quantity+=quantity;return repo.save(s);} }
