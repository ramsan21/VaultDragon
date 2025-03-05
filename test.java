Here’s a Spring WAR application with:
	•	A REST endpoint
	•	Beans initialized using applicationContext.xml
	•	Hazelcast Client initialized using ClientConfig
	•	Packaged as a WAR file for deployment in Tomcat

1. Project Structure

spring-war-hazelcast/
│── src/main/java/com/example/
│   ├── config/
│   │   ├── HazelcastConfig.java
│   ├── controller/
│   │   ├── HelloController.java
│── src/main/webapp/WEB-INF/
│   ├── web.xml
│   ├── applicationContext.xml
│── pom.xml

2. pom.xml (Maven Dependencies)

<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>spring-war-hazelcast</artifactId>
    <version>1.0</version>
    <packaging>war</packaging>

    <dependencies>
        <!-- Spring Web MVC -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-webmvc</artifactId>
            <version>5.3.30</version>
        </dependency>

        <!-- Hazelcast Client -->
        <dependency>
            <groupId>com.hazelcast</groupId>
            <artifactId>hazelcast-client</artifactId>
            <version>5.3.6</version>
        </dependency>

        <!-- Servlet API -->
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
            <version>4.0.1</version>
            <scope>provided</scope>
        </dependency>

        <!-- Jackson for JSON -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.15.3</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.3.2</version>
                <configuration>
                    <failOnMissingWebXml>false</failOnMissingWebXml>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>

3. applicationContext.xml (Bean Configuration)

<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans.xsd">

    <!-- Register Hazelcast Client as a bean -->
    <bean id="hazelcastClientConfig" class="com.example.config.HazelcastConfig" factory-method="getHazelcastClientInstance"/>

</beans>

4. HazelcastConfig.java (Hazelcast Client Configuration)

package com.example.config;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastConfig {

    private static HazelcastInstance hazelcastInstance;

    public static HazelcastInstance getHazelcastClientInstance() {
        if (hazelcastInstance == null) {
            ClientConfig clientConfig = new ClientConfig();
            clientConfig.getNetworkConfig().addAddress("127.0.0.1:5701"); // Adjust IP/port if needed
            hazelcastInstance = HazelcastClient.newHazelcastClient(clientConfig);
        }
        return hazelcastInstance;
    }
}

5. HelloController.java (REST Controller)

package com.example.controller;

import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ConcurrentMap;

@RestController
@RequestMapping("/api")
public class HelloController {

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @GetMapping("/hello")
    public String sayHello() {
        ConcurrentMap<String, String> map = hazelcastInstance.getMap("myDistributedMap");
        map.put("message", "Hello from Hazelcast!");
        return map.get("message");
    }
}

6. web.xml (Deployment Descriptor)

<web-app xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
         http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
         version="3.0">

    <display-name>Spring WAR Hazelcast Example</display-name>

    <servlet>
        <servlet-name>dispatcher</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>/WEB-INF/applicationContext.xml</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>

    <servlet-mapping>
        <servlet-name>dispatcher</servlet-name>
        <url-pattern>/api/*</url-pattern>
    </servlet-mapping>
</web-app>

7. Build and Deploy

Step 1: Package the WAR

Run the following Maven command:

mvn clean package

This generates a WAR file in target/spring-war-hazelcast-1.0.war.

Step 2: Deploy to Tomcat
	1.	Copy the generated WAR file to Tomcat’s webapps/ folder.
	2.	Start Tomcat:

./catalina.sh run


	3.	Test the Endpoint:
Open a browser or use curl to test:

http://localhost:8080/spring-war-hazelcast/api/hello

Expected Response:

"Hello from Hazelcast!"

Summary

✅ Spring application packaged as WAR
✅ Beans initialized using applicationContext.xml
✅ Hazelcast Client initialized via ClientConfig
✅ REST endpoint (/api/hello) interacts with Hazelcast
✅ Deployable in Tomcat

Would you like me to create a GitHub repository for you with this project?