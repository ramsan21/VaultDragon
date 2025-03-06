Let's troubleshoot the issue with Tomcat 10 shutdown events not being triggered. There are several potential causes and solutions:

## Common Causes for Shutdown Events Not Triggering

1. **Improper Servlet Context Path**:
   - Your ServletContextListener might not be registered properly

2. **Jakarta vs Javax Import Confusion**:
   - Make sure you're using `jakarta.*` packages instead of `javax.*`

3. **Incorrect Shutdown Method**:
   - You might be using a non-graceful shutdown method

4. **Web.xml Configuration Issue**:
   - Your listener might not be properly registered in web.xml

5. **Context Configuration Location**:
   - Spring context might not be loading properly

## Troubleshooting Steps

### 1. Check Your Shutdown Method

Ensure you're shutting down Tomcat properly:
```bash
# Use this command for graceful shutdown (recommended)
$CATALINA_HOME/bin/shutdown.sh

# Don't use these to test shutdown hooks
# kill -9 <tomcat-pid>  # This will force-kill and skip shutdown hooks
# Ctrl+C in the terminal  # Might not trigger all shutdown hooks
```

### 2. Double-Check Your web.xml

Ensure your listener is registered properly and uses the correct package:

```xml
<listener>
    <listener-class>com.example.shutdown.CustomShutdownListener</listener-class>
</listener>
```

### 3. Verify Imports in Your Listener Class

Make sure you're using Jakarta EE imports:

```java
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
// NOT javax.servlet.*
```

### 4. Add Debug Logging

Add more explicit debug logging to your shutdown methods:

```java
@Override
public void contextDestroyed(ServletContextEvent sce) {
    System.out.println("APPLICATION SHUTDOWN: Context destroyed event received");
    logger.info("APPLICATION SHUTDOWN: Context destroyed event received");
    
    // Rest of your code...
}
```

### 5. Check Tomcat logs

Look for any errors in Tomcat's logs:
```
$CATALINA_HOME/logs/catalina.out
```

### 6. Try Different Shutdown Hooks

If the ServletContextListener isn't working, try implementing a Spring-based shutdown hook:

```java
@Component
public class ApplicationShutdownHook implements DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationShutdownHook.class);
    
    @Override
    public void destroy() throws Exception {
        System.out.println("APPLICATION SHUTDOWN: Spring DisposableBean hook triggered");
        logger.info("APPLICATION SHUTDOWN: Spring DisposableBean hook triggered");
        // Your shutdown logic
    }
}
```

### 7. Runtime Shutdown Hook (Last Resort)

Add a JVM shutdown hook:

```java
@PostConstruct
public void registerShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        System.out.println("APPLICATION SHUTDOWN: JVM shutdown hook triggered");
        logger.info("APPLICATION SHUTDOWN: JVM shutdown hook triggered");
        // Your shutdown logic
    }));
}
```

Would you like me to provide a specific fix for any of these areas based on your setup? Or would you like to share more details about your specific implementation so I can provide more targeted help?
