package com.example.lab.payment;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.*;
import java.util.*;
@Entity class Payment { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; public Long orderId; public Integer amountCents; public String status; public Payment(){} }
interface PaymentRepo extends JpaRepository<Payment,Long> {}
@RestController class PaymentController { private final PaymentRepo repo; PaymentController(PaymentRepo repo){this.repo=repo;}
record PaymentRequest(Long orderId,Integer amountCents,Boolean simulateFailure){}
@PostMapping("/payments") Payment pay(@RequestBody PaymentRequest req){if(req.amountCents()==null||req.amountCents()<=0)throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,"Invalid amount");Payment p=new Payment();p.orderId=req.orderId();p.amountCents=req.amountCents();p.status=Boolean.TRUE.equals(req.simulateFailure())?"FAILED":"SUCCESS";return repo.save(p);}
@GetMapping("/payments") List<Payment> all(){return repo.findAll();} }
