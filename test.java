That new error is a classic hostname verification failure:

javax.net.ssl.SSLHandshakeException
Caused by: java.security.cert.CertificateException: No subject alternative DNS name
matching s2bsec-cnidxep.26066.app.standardchartered.com found.

Java verified the TLS chain, but then rejected the connection because the certificate’s SANs don’t contain the exact hostname you’re calling.

Likely causes
	1.	Typo/mismatch in the host you’re calling
Your earlier screenshots used s2bsec-cnidexp... (…dexp) but the error shows s2bsec-**cnidxep**... (…dxep). If that’s a typo in your URL, fix the spelling and try again.
	2.	Certificate doesn’t cover the host
	•	The cert might be for another FQDN (or a wildcard that doesn’t match 2-level subdomains).
	•	Example: *.app.standardchartered.com won’t match s2bsec-cnidxep.26066.app.standardchartered.com. You’d need either the exact FQDN in SANs or a wildcard like *.26066.app.standardchartered.com.

⸻

What to do

A) Quick checks

Run this to see what the server’s cert actually covers:

openssl s_client -connect s2bsec-cnidxep.26066.app.standardchartered.com:443 \
  -servername s2bsec-cnidxep.26066.app.standardchartered.com -showcerts </dev/null \
  2>/dev/null | openssl x509 -noout -subject -ext subjectAltName

	•	Confirm the Subject Alternative Name list contains your exact host (or a valid wildcard).

If you just had a typo, fix the URL and you’re done.

B) If the cert is wrong (server-side fix — recommended)

Update the Ingress to use a certificate whose SANs include the real host:

Ingress snippet

spec:
  tls:
    - hosts:
        - s2bsec-cnidxep.26066.app.standardchartered.com   # exact FQDN
      secretName: my-tls-secret
  rules:
    - host: s2bsec-cnidxep.26066.app.standardchartered.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: <your-service>
                port:
                  number: 443

If you use cert-manager, define a Certificate with the proper dnsNames:

apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: cnidxep-cert
  namespace: <ns>
spec:
  secretName: my-tls-secret
  dnsNames:
    - s2bsec-cnidxep.26066.app.standardchartered.com
  issuerRef:
    name: <your-issuer>
    kind: ClusterIssuer

If you need a wildcard, make sure it matches the level you use, e.g. *.26066.app.standardchartered.com.

C) Temporary DEV-ONLY workaround (not for prod)

If you must proceed before the cert is fixed, you can bypass hostname verification in Java (unsafe—use only in controlled testing):

HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

A slightly safer variant is to allow only the one expected host:

String expected = "s2bsec-cnidxep.26066.app.standardchartered.com";
HttpsURLConnection.setDefaultHostnameVerifier((host, sess) -> expected.equalsIgnoreCase(host));

But the right fix is to correct either the URL hostname or the certificate’s SANs.

⸻

If you paste the subjectAltName output and the exact URL you intend to call, I’ll tell you whether it’s a typo or you need a new cert for that hostname.