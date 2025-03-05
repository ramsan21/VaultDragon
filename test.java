The error “No Spring WebApplicationInitializer types detected on classpath” suggests that your project is missing a proper web initializer, which is required when deploying a Spring web application.

Here are possible solutions:

1. Ensure Spring Web Dependency is Included

Check if your pom.xml has the required Spring dependencies:

<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-web</artifactId>
    <version>6.1.11</version>
</dependency>

<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webmvc</artifactId>
    <version>6.1.11</version>
</dependency>

If they are missing, add them and do a Maven update (mvn clean install).

2. Check web.xml Configuration

Your web.xml file inside WEB-INF should have:

<web-app xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
         http://java.sun.com/xml/ns/javaee/web-app_3_1.xsd"
         version="3.1">
  
    <display-name>HazelcastTest</display-name>

    <servlet>
        <servlet-name>dispatcher</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <load-on-startup>1</load-on-startup>
    </servlet>

    <servlet-mapping>
        <servlet-name>dispatcher</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>

    <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>/WEB-INF/applicationContext.xml</param-value>
    </context-param>

    <listener>
        <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
    </listener>

</web-app>

Make sure:
	•	DispatcherServlet is properly configured.
	•	ContextLoaderListener is present for loading applicationContext.xml.

3. Ensure applicationContext.xml is Correct

Your applicationContext.xml is already defining the Hazelcast client, but also ensure it’s properly picked up by Spring.

Try adding the following inside applicationContext.xml:

<context:component-scan base-package="com.example"/>
<bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
    <property name="prefix" value="/views/"/>
    <property name="suffix" value=".jsp"/>
</bean>

This ensures all annotated Spring components (@Component, @Service, @Configuration) are detected.

4. Use Java-Based Configuration Instead of XML (If Needed)

If you’re using Spring 6.x, consider removing web.xml and using WebApplicationInitializer:

public class AppInitializer implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext servletContext) {
        AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
        ctx.register(AppConfig.class);
        ctx.setServletContext(servletContext);

        ServletRegistration.Dynamic servlet = servletContext.addServlet("dispatcher", new DispatcherServlet(ctx));
        servlet.setLoadOnStartup(1);
        servlet.addMapping("/");
    }
}

5. Ensure Logging is Properly Configured

If logs from HazelcastConfig are not appearing, check if you have a log4j2.xml or logback.xml file in src/main/resources.

For log4j2.xml:

<Configuration status="WARN">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n" />
        </Console>
    </Appenders>
    <Loggers>
        <Root level="info">
            <AppenderRef ref="Console"/>
        </Root>
    </Loggers>
</Configuration>

Final Steps
	•	Clean and rebuild: mvn clean install
	•	Run the application again and check the logs.

Let me know if the issue persists!