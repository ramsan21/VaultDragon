I looked closely at your screenshot. The key error message is:

Caused by: sun.security.validator.ValidatorException:
PKIX path validation failed: java.security.cert.CertPathValidatorException:
signature check failed
...
Caused by: java.security.SignatureException: Signature does not match.

What it means
	•	PKIX path validation failed → Java failed to validate the SSL/TLS certificate chain of the server you are connecting to.
	•	Signature does not match → The certificate presented by the server does not match the trusted CA chain in your JVM truststore. This can happen if:
	•	The certificate is self-signed or signed by an unknown CA.
	•	The certificate was re-issued but the server is still serving an old or mismatched chain.
	•	The JVM’s default truststore (cacerts) does not contain the intermediate/root CA.

⸻

Common causes
	1.	Server misconfiguration
	•	Wrong certificate chain is being served (missing intermediate cert).
	•	Expired certificate.
	•	Using a different private key/certificate mismatch.
	2.	Client-side issue
	•	The JDK truststore doesn’t have the CA used by the server.
	•	A proxy/firewall is intercepting TLS and replacing the cert.
	•	Old JDK version missing newer root certificates.

⸻

Fixes you can try

1. Verify the server’s certificate

Run:

openssl s_client -connect <hostname>:443 -showcerts

Check if the certificate chain is complete and valid.

2. Import the missing CA into your JVM truststore

If your server uses a custom CA:

keytool -import -trustcacerts -alias myserver -file server.crt -keystore $JAVA_HOME/lib/security/cacerts

(default password = changeit)

3. Check for expired or mismatched cert
	•	Ensure the cert and private key pair match.
	•	Renew/replace if expired.

4. Update JVM CA store

If you are using an older JDK, update it or update the cacerts file with the latest trusted CAs.

5. Debug with JVM flags

You can enable more SSL debugging:

-Djavax.net.debug=ssl,handshake,certpath

This will show exactly which cert in the chain is failing.

⸻

👉 Question for you:
Are you connecting to a public endpoint (like an API) or an internal service with a custom/self-signed certificate?

That will determine if you should import the CA or fix the server certificate chain.