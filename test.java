Thanks for the clearer screenshot. I can now confirm the exact chain of exceptions:

Caused by: sun.security.validator.ValidatorException:
  PKIX path validation failed: java.security.cert.CertPathValidatorException: signature check failed

Caused by: java.security.cert.CertPathValidatorException: signature check failed

Caused by: java.security.SignatureException: Signature does not match.

Key points
	•	The server certificate signature validation failed.
	•	This isn’t just a missing CA — it specifically says “Signature does not match”, which means:
	1.	The certificate is corrupted or incorrectly generated.
	2.	The server is presenting a certificate that does not correctly chain to the trusted CA.
	3.	The private key and certificate pair might not match (common after a re-issue if the wrong key is deployed).
	4.	Or, if you’re behind a proxy/SSL interceptor, it might be replacing the cert with its own, which Java cannot validate.

⸻

Steps to diagnose & fix

1. Test the server’s SSL certificate

Run this from your client machine:

openssl s_client -connect <hostname>:443 -showcerts

	•	Check the chain shown.
	•	Verify issuer and subject.
	•	See if intermediates are missing.

2. Validate certificate chain manually

openssl verify -CAfile <ca-bundle.crt> server.crt

If it fails, the chain is broken.

3. Check for mismatch in private key & certificate

On the server:

openssl x509 -noout -modulus -in server.crt | openssl md5
openssl rsa -noout -modulus -in server.key | openssl md5

The hashes must match.

4. Update / import the CA

If it’s a custom CA:

keytool -import -trustcacerts -alias myca -file myca.crt -keystore $JAVA_HOME/lib/security/cacerts

(password = changeit)

5. Check Java version

Older JDKs sometimes reject newer signature algorithms (like SHA256 with RSA).
Since your trace shows java.base/sun.security..., you’re using Java 11+ — still, ensure it’s updated with the latest security patches.

6. Enable SSL debugging

Run your app with:

-Djavax.net.debug=ssl,handshake,certpath

This will show exactly which certificate signature fails and why.

⸻

⚡ Next step for you:
Can you confirm whether the server you’re calling is internal (self-signed/custom CA) or a public HTTPS API? That will decide whether you should fix the truststore on your client, or the certificate chain on the server.