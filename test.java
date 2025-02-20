<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-core</artifactId>
    <version>3.2.7</version>
</dependency>
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-rt-frontend-jaxws</artifactId>
    <version>3.2.7</version>
</dependency>
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-rt-transports-http</artifactId>
    <version>3.2.7</version>
</dependency>
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-rt-databinding-jaxb</artifactId>
    <version>3.2.7</version>
</dependency>
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-rt-wsdl</artifactId>
    <version>3.2.7</version>
</dependency>
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-rt-bindings-soap</artifactId>
    <version>3.2.7</version>
</dependency>
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-rt-ws-policy</artifactId>
    <version>3.2.7</version>
</dependency>

<!-- JAXB for Java 8 -->
<dependency>
    <groupId>org.glassfish.jaxb</groupId>
    <artifactId>jaxb-runtime</artifactId>
    <version>2.3.3</version>
</dependency>

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:util="http://www.springframework.org/schema/util"
       xmlns:jee="http://www.springframework.org/schema/jee"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:cxf="http://cxf.apache.org/core"
       xmlns:jaxws="http://cxf.apache.org/jaxws"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd
           http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx.xsd
           http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd
           http://www.springframework.org/schema/jee http://www.springframework.org/schema/jee/spring-jee.xsd
           http://cxf.apache.org/core http://cxf.apache.org/schemas/core.xsd
           http://cxf.apache.org/jaxws http://cxf.apache.org/schemas/jaxws.xsd">

    <bean id="gapiApplicationContext" class="com.scb.starsec.gapiv2.utils.ApplicationContextProvider"/>

    <bean id="envPropertiesBean" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
        <property name="staticMethod" value="com.scb.starsec.gapiv2.utils.ApplicationContextProvider.setEnvProperties"/>
        <property name="arguments">
            <map>
                <entry key="gapi.config.file" value="/prd/stars/cc-gapi/conf/gapi_server.properties"/>
            </map>
        </property>
    </bean>

    <!-- Define the CryptoService bean -->
    <bean id="cryptoService" class="com.scb.starsec.gapiv2.service.impl.CryptoImpl"/>

    <!-- Define Apache CXF JAX-WS endpoint -->
    <jaxws:endpoint id="CryptoServiceEndPoint"
                    implementor="#cryptoService"
                    address="/services/CryptoService"
                    xmlns:s="http://server.uaas.starsec.scb.com"
                    serviceName="s:CryptoServiceService"
                    endpointName="s:CryptoService"/>
    
</beans>

