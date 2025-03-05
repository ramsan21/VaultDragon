Issue: Tomcat 10 Requires Jakarta EE Instead of Javax

Starting from Tomcat 10, the Java EE (javax.) namespace has been replaced with **Jakarta EE (jakarta.)**. If your Spring WAR application is using javax.servlet, it will not work in Tomcat 10 because Tomcat 10 requires Jakarta EE 9 or later.

🔧 Solution: Upgrade to Jakarta EE (Spring 5 or Spring 6)

1️⃣ Update web.xml (Replace javax.servlet with jakarta.servlet)

In src/main/webapp/WEB-INF/web.xml, change:

❌ Old (Java EE / Tomcat 9 and below)

<web-app xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
         http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
         version="3.0">

    <servlet>
        <servlet-name>dispatcher</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    </servlet>

    <servlet-mapping>
        <servlet-name>dispatcher</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>
</web-app>

✅ New (Jakarta EE / Tomcat 10+)

<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
         https://jakarta.ee/xml/ns/jakartaee/web-app_5_0.xsd"
         version="5.0">

    <servlet>
        <servlet-name>dispatcher</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    </servlet>

    <servlet-mapping>
        <servlet-name>dispatcher</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>
</web-app>

2️⃣ Update pom.xml Dependencies

❌ Old Dependencies (javax.servlet)

<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <version>4.0.1</version>
    <scope>provided</scope>
</dependency>

✅ New Dependencies (jakarta.servlet)

<dependency>
    <groupId>jakarta.servlet</groupId>
    <artifactId>jakarta.servlet-api</artifactId>
    <version>5.0.0</version>
    <scope>provided</scope>
</dependency>

For Spring Web MVC:

If you are using Spring 5, upgrade to Spring 6, which supports Jakarta EE:

<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webmvc</artifactId>
    <version>6.1.2</version>
</dependency>

3️⃣ Update applicationContext.xml (Spring Configuration)
	•	If you have references to javax.*, update them to jakarta.*.
	•	In spring-web.xml, make sure all servlet configurations are compatible with Jakarta EE.

4️⃣ Update Java Classes (Servlet Imports)

If you have servlet-related classes, update the imports.

❌ Old (javax.servlet - Tomcat 9)

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

✅ New (jakarta.servlet - Tomcat 10)

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;

5️⃣ Rebuild and Deploy the WAR

mvn clean package

Deploy target/your-app.war in Tomcat 10’s webapps/ folder and restart Tomcat.

💡 Alternative: Run Tomcat in Compatibility Mode

If you cannot migrate to Jakarta EE immediately, use Tomcat 9 instead, or run Tomcat 10 in compatibility mode.
	1.	Download the Tomcat 9 version from here.
	2.	If using Tomcat 10, use the migration tool:

java -jar tomcat-migration-tool.jar migrate -src /path/to/your/war -dest /path/to/migrated/war


	3.	Alternatively, use Tomcat 10 EE Compatibility Mode:

catalina.sh run --add-opens=java.base/java.lang=ALL-UNNAMED

✅ Summary

Issue	Solution
Tomcat 10 requires jakarta.*	Update web.xml to use jakarta.servlet
Spring Web MVC not working	Upgrade to Spring 6
Servlet classes using javax.servlet	Change imports to jakarta.servlet
Tomcat 10 WAR deployment fails	Use Tomcat 9 OR apply migration tool

Would you like me to guide you in testing your WAR file in Tomcat 10? 🚀