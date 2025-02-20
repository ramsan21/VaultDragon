xsi:schemaLocation="
    http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
    http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd
    http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx.xsd
    http://cxf.apache.org/jaxws http://cxf.apache.org/schemas/jaxws.xsd">

<jaxws:endpoint 
    id="CryptoServiceEndPoint" 
    implementor="#cryptoService"
    address="/services/CryptoService"
    xmlns:s="http://server.uaas.starsec.scb.com"
    serviceName="s:CryptoServiceService"
    endpointName="s:CryptoService">
</jaxws:endpoint>