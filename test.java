You’re hitting a TLS chain validation failure:

PKIX path validation failed … CertPathValidatorException: signature check failed
… SignatureException: Signature does not match.

That means Java built a certificate path for the server but the issuer’s public key it found doesn’t match the certificate that signed the leaf. In practice this happens when:
	•	the intermediate CA (or root) in your truststore is wrong / missing / outdated, or
	•	the Ingress is serving only the leaf cert instead of the full chain, or
	•	you imported the wrong cert (e.g., a different org’s root) into your truststore.

Below is a tight checklist to fix it fast.

⸻

1) Verify what the server is actually serving

From any shell that can reach the Ingress FQDN:

# show what the Ingress presents (leaf + any intermediates)
openssl s_client -connect s2bsec-cnidexp.26066.app.standardchartered.com:443 \
  -servername s2bsec-cnidexp.26066.app.standardchartered.com -showcerts </dev/null

	•	Copy out the leaf and every intermediate you see into separate files (e.g., leaf.pem, intermediate.pem).
	•	Note each cert’s Subject and Issuer; they should chain: leaf -> intermediate -> root.

2) Inspect your truststore

Check what you imported:

keytool -list -v -keystore src/main/resources/truststore.jks

Look for:
	•	Correct chain present (usually intermediate(s) + root, not just the leaf).
	•	Validity dates (not expired).
	•	Subject/Issuer of each cert matches what openssl showed.

If you see a wrong CA (Issuer doesn’t match), delete it:

keytool -delete -alias wrong-alias -keystore src/main/resources/truststore.jks

Then (re)import the right ones:

# Import the intermediate first, then the root
keytool -importcert -alias scb-intermediate -file intermediate.pem \
  -keystore src/main/resources/truststore.jks
keytool -importcert -alias scb-root -file root.pem \
  -keystore src/main/resources/truststore.jks

Tip: You do not need to import the leaf into the truststore. Trust the CA(s), not the server cert.

3) (Server-side) ensure NGINX/Ingress serves the full chain

If you control the Ingress TLS secret, the tls.crt must contain leaf + intermediate(s) concatenated (aka “fullchain”):

# tls.crt = leaf.pem + intermediate.pem(s) concatenated in this order
cat leaf.pem intermediate.pem > fullchain.pem
kubectl create secret tls mytls --cert=fullchain.pem --key=privkey.pem -n <ns> --dry-run=client -o yaml | kubectl apply -f -

If certs are issued by cert-manager, ensure it’s configured to store the full chain (most issuers already do).

4) Keep your code but wire the truststore correctly

Your code is fine for a custom truststore. Two small safety tweaks:

KeyStore trustStore = KeyStore.getInstance("JKS"); // or "PKCS12" if that’s your file type
try (InputStream is = new FileInputStream(truststorePath)) {
  trustStore.load(is, truststorePassword.toCharArray());
}

TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
tmf.init(trustStore);

SSLContext sslContext = SSLContext.getInstance("TLS");
sslContext.init(null, tmf.getTrustManagers(), null);
HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

// Optional: also set system props so libraries that don’t use your factory still work
System.setProperty("javax.net.ssl.trustStore", truststorePath);
System.setProperty("javax.net.ssl.trustStorePassword", truststorePassword);

// Keep hostname verification ON (default). Do not disable for production.

Important: If your truststore.jks is actually a PKCS#12 file, use KeyStore.getInstance("PKCS12"). Using the wrong type won’t cause this error during validation, but it’s a common pitfall.

5) Re-test
	•	After fixing the truststore and/or Ingress full chain, rerun your Java client.
	•	If you still fail, compare again the Issuer of the leaf from openssl with the Subject of the CA you imported. They must match exactly.

⸻

Why you saw “Signature does not match”

Java verified the leaf’s signature using the CA public key it found in your truststore. Because that CA wasn’t the actual issuer of the leaf (wrong/missing intermediate, wrong root, or stale corporate CA), the signature check failed, yielding exactly the stack you posted.

⸻

Quick-but-temporary sanity check (not for prod)

To prove it’s chain-related (and not Basic Auth or your XML payload), you can momentarily trust the system CAs + your corporate CA bundle:

# import your corporate proxy/Root CA chain too, if your traffic traverses a TLS-inspecting proxy
keytool -importcert -alias corp-root -file corp_root.pem -keystore truststore.jks

If that makes it work, replace with the correct issuer chain for the Ingress and remove any broad corp roots you don’t need.

⸻

If you want, paste the Subject/Issuer lines from openssl s_client and the keytool -list -v output (trim serials), and I’ll tell you exactly which link in the chain is missing or mismatched.