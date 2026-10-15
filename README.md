# E-commerce microservices Kubernetes practice lab

Five **separately deployable** Spring Boot 3 / Java 17 services, each with its own PostgreSQL database (five databases in one Postgres instance for lab cost). This is an educational demo, not production-ready payment/order processing.

## Components
- user-service: 8081 (create/list/get users)
- product-service: 8082 (create/list/get products)
- inventory-service: 8083 (set/get/reserve/release inventory)
- order-service: 8084 (calls user, product, inventory, payment)
- payment-service: 8085 (simulated payment, never processes real payments)

## Start
```bash
docker compose up -d --build
docker compose ps
```
Wait for the services to become available, then:
```bash
curl -X POST http://localhost:8081/users -H 'Content-Type: application/json' -d '{"name":"Alice","email":"alice@example.com"}'
curl -X POST http://localhost:8082/products -H 'Content-Type: application/json' -d '{"name":"Keyboard","priceCents":2500}'
curl -X PUT http://localhost:8083/inventory/1 -H 'Content-Type: application/json' -d '{"quantity":20}'
curl -X POST http://localhost:8084/orders -H 'Content-Type: application/json' -d '{"userId":1,"productId":1,"quantity":2,"simulatePaymentFailure":false}'
curl http://localhost:8084/orders
curl http://localhost:8085/payments
```

## Build and push images
Set `DOCKERHUB_USER` to your Docker Hub username, then:
```bash
export DOCKERHUB_USER=YOUR_DOCKERHUB_USERNAME
docker compose build
docker login
for s in user product inventory order payment; do docker push "$DOCKERHUB_USER/$s-service:1.0.0"; done
```

## GitHub
```bash
git init
git add .
git commit -m 'Initial five-service ecommerce lab'
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/ecommerce-microservices-lab.git
git push -u origin main
```

## Kubernetes next steps
Use each pushed image in a separate Deployment and ClusterIP Service. Set the DB_URL/DB_USER/DB_PASSWORD environment variables from ConfigMap/Secret and configure order-service with USER_URL, PRODUCT_URL, INVENTORY_URL, PAYMENT_URL using Kubernetes service DNS names. For persistence use a PostgreSQL StatefulSet and PVC (or managed RDS). Add readiness/liveness probes on `/actuator/health/readiness` and `/actuator/health/liveness`.

## Limitations to understand in interviews
- No real payment gateway, authentication, event bus, distributed tracing, or API gateway.
- Order calls are synchronous; failure after reserving inventory may require reconciliation. A production design would use idempotency, transactional outbox, retries, and a saga/compensation workflow.
- Inventory reservation here is synchronized only within a single JVM; **it is not safe under multiple inventory replicas**. Use database-level atomic conditional updates/locking before HPA on inventory.
- `ddl-auto=update` is only for this lab; production should use Flyway/Liquibase migrations.
- Sample passwords are for local demo only. Never expose this demo directly to the internet or use real customer data.

## Web frontend
The optional Nginx-based frontend runs on host port 80. Open `http://YOUR_EC2_PUBLIC_IP/` after `docker compose up -d --build`. It calls backend services via same-origin `/api/*` reverse proxy routes. This is a demo application with no login/authentication; do not expose it to the public internet except in a tightly restricted lab security group.
