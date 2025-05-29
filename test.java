Here are **Top 10 Tough Java Spring Boot Interview Questions** with **scenario-based answers**, covering real-world complexities in microservices, dependency injection, transactions, security, etc. These are often asked in senior or architect-level roles:

---

### **1. Scenario: Circular Dependency Issue**

**Q:** You have two Spring Beans `A` and `B`, and both depend on each other. How do you resolve this circular dependency in Spring Boot?

**A:**
Spring cannot resolve circular dependencies with constructor injection. You can resolve it by using **setter injection** or `@Lazy`:

```java
@Component
public class A {
    private B b;
    public A(@Lazy B b) {
        this.b = b;
    }
}
```

Or switch to setter:

```java
@Component
public class B {
    private A a;
    @Autowired
    public void setA(A a) {
        this.a = a;
    }
}
```

---

### **2. Scenario: Service Layer Transaction Rollback**

**Q:** In your service layer, you're calling two repository methods. One fails, but the first persists data. How do you ensure both actions are rolled back?

**A:**
Wrap both repository calls in a `@Transactional` method:

```java
@Service
public class UserService {
    @Transactional
    public void createUserAndLog(User user, Log log) {
        userRepository.save(user);
        logRepository.save(log); // Fails → entire transaction rolls back
    }
}
```

Ensure exception isn't caught silently or marked as `@Transactional(noRollbackFor=...)`.

---

### **3. Scenario: Application Fails to Start Due to Port Conflict**

**Q:** Your Spring Boot app fails at startup with "port already in use". What steps do you take to fix it?

**A:**

* Change port in `application.properties`:

  ```properties
  server.port=8081
  ```
* Or free the port: `lsof -i :8080` → `kill <PID>`
* Alternatively, run with dynamic port: `--server.port=0`

---

### **4. Scenario: Inter-Service Call Fails in Production**

**Q:** Your microservice fails when calling another service in production. How do you handle this in Spring Boot?

**A:**
Use **Resilience4j** or **Spring Cloud Circuit Breaker** with fallback:

```java
@CircuitBreaker(name = "orderService", fallbackMethod = "fallback")
public Order getOrderDetails(String id) {
    return restTemplate.getForObject("http://order-service/orders/" + id, Order.class);
}

public Order fallback(String id, Throwable t) {
    return new Order("default", "Fallback order");
}
```

Also implement proper timeouts and retries.

---

### **5. Scenario: Bean Not Being Injected**

**Q:** You're getting `NullPointerException` when accessing a bean. It’s annotated with `@Component`. What could be wrong?

**A:**

* Bean is not in a scanned package. Solution: move it or define `@ComponentScan`.
* Manually instantiated via `new`, bypassing Spring.
* Circular dependency in constructor injection.

---

### **6. Scenario: Security - Restrict Access Based on Role**

**Q:** How do you secure an endpoint in Spring Boot to be accessed **only by admins**?

**A:**
Enable method security:

```java
@EnableGlobalMethodSecurity(prePostEnabled = true)
```

Then restrict:

```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin-data")
public String getAdminData() {
    return "Sensitive Info";
}
```

---

### **7. Scenario: Slow Boot Time**

**Q:** Your Spring Boot application takes long to start. How do you optimize?

**A:**

* Use `spring.main.lazy-initialization=true`
* Profile what’s slow: use `actuator` and `StartupStep`
* Remove unused `@ComponentScan`
* Avoid large static resource folders on classpath

---

### **8. Scenario: Environment-Specific Properties**

**Q:** You have different DBs for dev and prod. How do you configure environment-specific settings?

**A:**
Use Spring Profiles:

```properties
# application-dev.properties
spring.datasource.url=jdbc:h2:mem:testdb

# application-prod.properties
spring.datasource.url=jdbc:mysql://prod-db:3306/mydb
```

Activate profile:

```bash
-Dspring.profiles.active=prod
```

Or set in `application.properties`.

---

### **9. Scenario: Scheduled Task Executes Multiple Times**

**Q:** You notice your scheduled task runs multiple times concurrently. How do you prevent this?

**A:**

* Make the method **synchronized** or use a **lock** mechanism.
* Use `@Scheduled` with fixed delay and async disabled:

```java
@Scheduled(fixedDelay = 10000)
@SchedulerLock(name = "taskName", lockAtMostFor = "9s")
public void runTask() { ... }
```

Use ShedLock for distributed locking.

---

### **10. Scenario: Custom Exception Handling**

**Q:** You want to return a custom JSON error message for all `UserNotFoundException`. How do you do that?

**A:**
Use `@ControllerAdvice`:

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", ex.getMessage()));
    }
}
```

---

If you'd like this in a downloadable format (PDF/Word), let me know — I can generate that for you.
