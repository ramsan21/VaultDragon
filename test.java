Thanks for the clearer picture — I see exactly what you want now.

Right now your method:

@WebMethod(action = "signB64")
@WebResult(name = "return")
public @XmlElement(name = "string") String[] signB64(
    @WebParam(name = "sign_message") String sign_message,
    @WebParam(name = "alias") String alias
) { ... }

produces:

<return>
  <item>100</item>
  <item>1</item>
</return>

but you want:

<return>
  <ns1:string>100</ns1:string>
  <ns1:string>1</ns1:string>
</return>


⸻

Why it happens
	•	Returning a raw String[] makes JAX-WS/JAXB default to <item>....
	•	To force <ns1:string>..., you must control the JAXB mapping of the array.

⸻

✅ Solution: Wrap the array in a JAXB type

1. Create a response wrapper

package com.example.ws;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "signB64Response", propOrder = { "strings" })
@XmlRootElement(name = "signB64Response", namespace = "http://server.qapi.starsec.scb.com")
public class SignB64Response {

    @XmlElement(name = "string", namespace = "http://server.qapi.starsec.scb.com")
    private List<String> strings = new ArrayList<>();

    public List<String> getStrings() {
        return strings;
    }

    public void setStrings(List<String> strings) {
        this.strings = strings;
    }
}


⸻

2. Update your WebMethod to return this wrapper

@WebMethod(action = "signB64")
@WebResult(name = "return")
public SignB64Response signB64(
        @WebParam(name = "sign_message") String sign_message,
        @WebParam(name = "alias") String alias) {

    SignB64Response resp = new SignB64Response();
    resp.getStrings().add("100");
    resp.getStrings().add("1");
    return resp;
}


⸻

3. Output will now look like this

<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <ns1:signB64Response xmlns:ns1="http://server.qapi.starsec.scb.com">
      <return>
        <ns1:string>100</ns1:string>
        <ns1:string>1</ns1:string>
      </return>
    </ns1:signB64Response>
  </soap:Body>
</soap:Envelope>


⸻

🔑 Notes
	•	The @XmlElement(name="string", namespace="...") is the key to force <ns1:string> instead of <item>.
	•	The wrapper object is necessary because JAXB cannot apply the annotation directly to a raw String[] return type.
	•	If you want the prefix to always be ns1, you’ll also need a NamespacePrefixMapper (otherwise the runtime might choose ns2, ns3, etc.).

⸻

👉 Do you also want me to show you how to add a NamespacePrefixMapper so the prefix is always ns1 in the SOAP response?