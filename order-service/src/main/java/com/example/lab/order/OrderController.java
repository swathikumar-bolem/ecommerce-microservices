package com.example.lab.order;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@Entity class CustomerOrder { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; public Long userId; public Long productId; public Integer quantity; public Integer amountCents; public String status; public CustomerOrder(){} }
interface OrderRepo extends JpaRepository<CustomerOrder,Long> {}
@RestController class OrderController {
 private final OrderRepo repo; private final RestClient http=RestClient.create();
 private final String users=System.getenv().getOrDefault("USER_URL","http://localhost:8081");
 private final String products=System.getenv().getOrDefault("PRODUCT_URL","http://localhost:8082");
 private final String inventory=System.getenv().getOrDefault("INVENTORY_URL","http://localhost:8083");
 private final String payments=System.getenv().getOrDefault("PAYMENT_URL","http://localhost:8085");
 OrderController(OrderRepo repo){this.repo=repo;}
 record CreateOrder(Long userId,Long productId,Integer quantity,Boolean simulatePaymentFailure){}
 record ProductInfo(Long id,String name,Integer priceCents){}
 record PaymentInfo(Long id,Long orderId,Integer amountCents,String status){}
 @PostMapping("/orders") CustomerOrder create(@RequestBody CreateOrder r){
  if(r.userId()==null||r.productId()==null||r.quantity()==null||r.quantity()<=0)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Missing or invalid fields");
  try {
   http.get().uri(users+"/users/"+r.userId()).retrieve().toBodilessEntity();
   ProductInfo product=http.get().uri(products+"/products/"+r.productId()).retrieve().body(ProductInfo.class);
   if(product==null||product.priceCents()==null)throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Product unavailable");
   int amount=Math.multiplyExact(product.priceCents(),r.quantity());
   http.post().uri(inventory+"/inventory/"+r.productId()+"/reserve?quantity="+r.quantity()).retrieve().toBodilessEntity();
   CustomerOrder order=new CustomerOrder();order.userId=r.userId();order.productId=r.productId();order.quantity=r.quantity();order.amountCents=amount;order.status="PENDING_PAYMENT";order=repo.save(order);
   try {
    PaymentInfo pay=http.post().uri(payments+"/payments").body(Map.of("orderId",order.id,"amountCents",amount,"simulateFailure",Boolean.TRUE.equals(r.simulatePaymentFailure()))).retrieve().body(PaymentInfo.class);
    order.status=pay!=null&&"SUCCESS".equals(pay.status())?"CONFIRMED":"PAYMENT_FAILED";
   } catch(Exception e){order.status="PAYMENT_UNKNOWN";}
   if("PAYMENT_FAILED".equals(order.status)){
    try{http.post().uri(inventory+"/inventory/"+r.productId()+"/release?quantity="+r.quantity()).retrieve().toBodilessEntity();}catch(Exception e){order.status="REQUIRES_RECONCILIATION";}
   }
   return repo.save(order);
  }catch(RestClientResponseException e){throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Dependent service returned "+e.getStatusCode());}
  catch(ArithmeticException e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Amount too large");}
 }
 @GetMapping("/orders") List<CustomerOrder> all(){return repo.findAll();}
}
