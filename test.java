Yes, you can log all the lifecycle events when a valid restart is received in Tomcat by implementing various listeners in your Spring application. Here's how you can do it:

1. **Create a comprehensive ApplicationListener**

   You can create a single listener that captures multiple application context events:

   ```java
   public class ApplicationLifecycleLogger implements ApplicationListener<ApplicationContextEvent> {
       private static final Logger logger = LoggerFactory.getLogger(ApplicationLifecycleLogger.class);
       
       @Override
       public void onApplicationEvent(ApplicationContextEvent event) {
           if (event instanceof ContextRefreshedEvent) {
               logger.info("Context Refreshed Event received: {}", event);
           } else if (event instanceof ContextStartedEvent) {
               logger.info("Context Started Event received: {}", event);
           } else if (event instanceof ContextStoppedEvent) {
               logger.info("Context Stopped Event received: {}", event);
           } else if (event instanceof ContextClosedEvent) {
               logger.info("Context Closed Event received: {}", event);
           } else {
               logger.info("Application Context Event received: {}", event);
           }
       }
   }
   ```

2. **Register the listener in applicationContext.xml**

   ```xml
   <bean id="applicationLifecycleLogger" class="com.example.ApplicationLifecycleLogger"/>
   ```

3. **Log bean lifecycle events**

   Create a BeanPostProcessor to log individual bean lifecycle events:

   ```java
   public class BeanLifecycleLogger implements BeanPostProcessor, DisposableBean, InitializingBean {
       private static final Logger logger = LoggerFactory.getLogger(BeanLifecycleLogger.class);
       
       @Override
       public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
           logger.info("Bean '{}' before initialization", beanName);
           return bean;
       }
       
       @Override
       public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
           logger.info("Bean '{}' after initialization", beanName);
           return bean;
       }
       
       @Override
       public void destroy() {
           logger.info("DisposableBean's destroy method called");
       }
       
       @Override
       public void afterPropertiesSet() {
           logger.info("InitializingBean's afterPropertiesSet method called");
       }
   }
   ```

4. **Register in applicationContext.xml**

   ```xml
   <bean id="beanLifecycleLogger" class="com.example.BeanLifecycleLogger"/>
   ```

5. **ServletContextListener for Tomcat lifecycle**

   Implement a ServletContextListener to capture Tomcat-specific events:

   ```java
   public class TomcatLifecycleLogger implements ServletContextListener {
       private static final Logger logger = LoggerFactory.getLogger(TomcatLifecycleLogger.class);
       
       @Override
       public void contextInitialized(ServletContextEvent sce) {
           logger.info("Servlet context initialized: {}", sce.getServletContext().getServerInfo());
       }
       
       @Override
       public void contextDestroyed(ServletContextEvent sce) {
           logger.info("Servlet context being destroyed: {}", sce.getServletContext().getServerInfo());
       }
   }
   ```

6. **Register in web.xml**

   ```xml
   <listener>
       <listener-class>com.example.TomcatLifecycleLogger</listener-class>
   </listener>
   ```

7. **Spring Boot Actuator (if applicable)**

   If you're using Spring Boot, you can enable the Actuator's shutdown endpoint to get additional logging:

   ```properties
   management.endpoints.web.exposure.include=shutdown
   management.endpoint.shutdown.enabled=true
   ```

When Tomcat receives a valid restart command, these listeners will log the various lifecycle events as they occur, providing you with a comprehensive log of the shutdown and startup sequence.
