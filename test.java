Thanks for sharing the screenshot — now it’s clear.

👉 Your method currently returns a plain String[]. JAXB/JAX-WS marshals this as <item>...</item>. But you want each element to be <ns1:string>...</ns1:string>.

That means you need to tell JAXB:
	•	Don’t use anonymous <item> elements.
	•	Use a named element with namespace + type.

⸻

✅ How to fix

1. Wrap the array in a response class

Instead of returning String[] directly, create a wrapper with JAXB annotations:

@XmlRootElement(name = "signB64Response", namespace = "http://server.qapi.starsec.scb.com")
@XmlAccessorType(XmlAccessType.FIELD)
public class SignB64Response {

    @XmlElement(name = "string", namespace = "http://server.qapi.starsec.scb.com")
    private List<String> returnValues = new ArrayList<>();

    public List<String> getReturnValues() {
        return returnValues;
    }

    public void setReturnValues(List<String> returnValues) {
        this.returnValues = returnValues;
    }
}


⸻

2. Change your WebMethod to return this wrapper

@WebMethod(action = "signB64")
@WebResult(name = "return")
public SignB64Response signB64(
        @WebParam(name = "sign_message") String sign_message,
        @WebParam(name = "alias") String alias) {

    SignB64Response response = new SignB64Response();
    response.getReturnValues().add("100");
    response.getReturnValues().add("1");
    return response;
}


⸻

3. Output result

Now the SOAP response will look like:

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

🔑 Why this works
	•	Directly returning String[] makes JAXB default to <item>...</item>.
	•	A wrapper class with @XmlElement(name="string", namespace=...) forces elements to be <ns1:string>.
	•	You can also control the prefix (ns1) using a NamespacePrefixMapper if needed (Spring or JAX-WS runtime).

⸻

👉 Do you want me to also show you the NamespacePrefixMapper implementation, so the prefix is always ns1 (instead of possibly ns2, ns3, etc.)?