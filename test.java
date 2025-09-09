Got it—your new screenshots pin it down:
	•	Chrome shows NET::ERR_CERT_AUTHORITY_INVALID for
s2bsec-cnidexp.26066.app.standardchartered.com → the cert chain isn’t trusted by your machine.
	•	Your Java stack trace fails with PKIX… Signature does not match → the server’s chain/signature doesn’t validate against what the JVM trusts.
	•	Your openssl s_client -connect 10.65.4.37:443 fails to connect (internal IP). Also, testing by IP won’t present the right cert (SNI), so even if it connected it could mislead.

What this likely means
	1.	The site is using an internal/corporate CA (or a TLS-intercepting proxy) that your laptop/JVM doesn’t trust; and/or
	2.	The server is serving an incomplete or wrong chain (missing intermediates / wrong bundle); and/or
	3.	You tested by IP without SNI; the right way is by hostname.

⸻

Quick, targeted steps

1) Verify the exact chain (use hostname + SNI)

# Use the full host shown in Chrome
HOST=s2bsec-cnidexp.26066.app.standardchartered.com
openssl s_client -connect $HOST:443 -servername $HOST -showcerts </dev/null 2>/dev/null | openssl x509 -noout -issuer -subject -dates -fingerprint -sha256

	•	If the Issuer is a corporate CA or a proxy (e.g., BlueCoat/Zscaler/etc.), you must trust that root/intermediate in the JVM (and possibly in the OS/Chrome too).
	•	If you see only the leaf or an unexpected intermediate, the server/balancer needs the full chain (leaf + intermediates) configured.

2) If it’s a corporate CA / TLS-inspection proxy

Export the corporate root CA (Base-64 .cer/.pem) and add it to the JVM truststore used by your app:

# check where your app’s JVM truststore is
$JAVA_HOME/bin/keytool -list -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit | head

# import (choose a unique alias)
sudo $JAVA_HOME/bin/keytool -importcert -noprompt \
  -alias corp-root-ca \
  -file corp-root.cer \
  -keystore "$JAVA_HOME/lib/security/cacerts" \
  -storepass changeit

If your app runs in a container/K8s, create a ConfigMap/Secret with an updated cacerts and run the JVM with:

-Djavax.net.ssl.trustStore=/opt/app/certs/cacerts
-Djavax.net.ssl.trustStorePassword=changeit

(Spring Boot alternative)

server.ssl.trust-store=classpath:certs/cacerts
server.ssl.trust-store-password=changeit

3) If it’s a server/balancer chain issue

Fix the served bundle (F5/NGINX/Apache):
	•	Use a fullchain (leaf + all intermediates) matching the private key.
	•	Validate on the server:

# key ↔ cert match
openssl x509 -noout -modulus -in server.crt | openssl md5
openssl rsa  -noout -modulus -in server.key | openssl md5
# must be identical

	•	Re-test:

openssl s_client -connect $HOST:443 -servername $HOST -showcerts </dev/null | openssl verify -CAfile <root+intermediates.pem>

4) Make sure you’re actually reaching the right host

Your IP test failed to connect (firewall/VPN). Test hostname from the same network the app uses. If the corporate proxy is in the path, either:
	•	add the proxy’s root CA to the JVM, or
	•	bypass the proxy for this host (NO_PROXY) if policy allows.

5) Turn on SSL debug once and confirm

Run the Java client with:

-Djavax.net.debug=ssl,handshake,certpath

You’ll see exactly which certificate in the path fails (e.g., “unable to find valid certification path to requested target” vs “signature check failed on ”).

⸻

What I’d do next (minimal)
	1.	Run the openssl s_client with hostname + -servername and paste the Issuer/Subject lines here.
	2.	If Issuer is corporate → import that root into the JVM used by your app (and into the pod image if in K8s).
	3.	If Issuer is a public CA but chain is incomplete → fix the LB/server to serve the full chain.

If you share the openssl s_client output (redact names if needed), I’ll point to the exact cert to import or the exact gap in the chain.