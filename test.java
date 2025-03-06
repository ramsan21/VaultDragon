<context-param>
    <param-name>shutdownTimeoutSeconds</param-name>
    <param-value>10</param-value>
</context-param>

   
@Override
public void contextDestroyed(ServletContextEvent sce) {
    System.out.println("************ APPLICATION SHUTDOWN: Context destroyed event received ************");
    // Rest of your code...
}


<!-- Put this BEFORE the Spring listener -->
<listener>
    <listener-class>com.example.shutdown.CustomShutdownListener</listener-class>
</listener>

<!-- Spring Context Loader Listener -->
<listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
</listener>


@Component
public class JvmShutdownHook implements InitializingBean {
    @Override
    public void afterPropertiesSet() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("************ JVM SHUTDOWN HOOK TRIGGERED ************");
            // Your shutdown logic here
        }));
    }
}
