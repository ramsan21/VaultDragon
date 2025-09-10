Yes — you can use your .pfx (PKCS#12) to fix this. Ingress needs a Kubernetes TLS secret of type kubernetes.io/tls, which contains:
	•	tls.crt → the full certificate chain in PEM (leaf + intermediates)
	•	tls.key → the private key in PEM (unencrypted)

Here’s the quickest, safe way to go from PFX → Ingress TLS.

⸻

1) Extract cert(s) and key from the PFX

You’ll need the PFX password. Run these on any machine with OpenSSL.

# 1) Extract the private key (still encrypted with the PFX password)
openssl pkcs12 -in yourcert.pfx -nocerts -nodes -out privkey_encrypted.pem

# 2) (Recommended) write an UNENCRYPTED key for Kubernetes (k8s can’t use encrypted keys)
# If privkey_encrypted.pem is already unencrypted (because of -nodes), skip this.
openssl rsa -in privkey_encrypted.pem -out privkey.pem

# 3) Extract the full chain (leaf + intermediates) from the PFX
# Many PFX files already contain the full chain. This outputs all certs in PEM.
openssl pkcs12 -in yourcert.pfx -clcerts -nokeys -out leaf.pem        # leaf only
openssl pkcs12 -in yourcert.pfx -nodes   -nokeys -out all-certs.pem   # leaf + CA(s)

# 4) Build fullchain.pem: leaf first, then intermediates/roots
# (If all-certs.pem has multiple certs, split if needed; order matters)
cat leaf.pem all-certs.pem > fullchain.pem

Gotchas
	•	Order must be leaf first → intermediates → (optionally) root.
	•	If all-certs.pem includes duplicates or the root at the top, reorder accordingly.
	•	To confirm SANs include your host, run:

openssl x509 -in leaf.pem -noout -ext subjectAltName

Ensure it contains: DNS:s2bsec-cnidexp.26066.app.standardchartered.com (or a wildcard that truly matches).

⸻

2) Create the Kubernetes TLS secret

Pick your namespace (replace <ns>):

kubectl -n <ns> create secret tls my-tls-secret \
  --cert=fullchain.pem \
  --key=privkey.pem

If you’re scripting/Helm, you can also render YAML:

kubectl -n <ns> create secret tls my-tls-secret \
  --cert=fullchain.pem --key=privkey.pem \
  --dry-run=client -o yaml | kubectl apply -f -


⸻

3) Reference it in your Ingress

apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: your-ingress
  namespace: <ns>
spec:
  ingressClassName: <your-class>   # if you use one (e.g., nginx)
  tls:
    - hosts:
        - s2bsec-cnidexp.26066.app.standardchartered.com
      secretName: my-tls-secret
  rules:
    - host: s2bsec-cnidexp.26066.app.standardchartered.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: <your-service>
                port:
                  number: 443

Apply the change. Your Ingress controller will start serving the real certificate instead of the “Kubernetes Ingress Controller Fake Certificate”.

⸻

4) Verify

# shows the served cert and SANs
openssl s_client -connect s2bsec-cnidexp.26066.app.standardchartered.com:443 \
  -servername s2bsec-cnidexp.26066.app.standardchartered.com -showcerts </dev/null \
  2>/dev/null | openssl x509 -noout -subject -ext subjectAltName

You should now see your organization’s cert, and the SANs should include your FQDN.

Then your Java client should connect without:
	•	PKIX path errors, and
	•	“No subject alternative DNS name …” errors.

⸻

If you’re using cert-manager (optional)

You can still import your PFX by creating the secret as above and referencing it in the Ingress. cert-manager isn’t required unless you want auto-renewal; in that case you would instead create a Certificate with dnsNames and let cert-manager populate the secret.

⸻

Troubleshooting checklist
	•	If Java still fails, ensure your client calls exactly the same hostname that’s in the cert SANs.
	•	If you see only the leaf in openssl s_client output, rebuild fullchain.pem (leaf + intermediates) and recreate the secret.
	•	Make sure the key is unencrypted (openssl rsa -in … -out privkey.pem) or k8s won’t be able to use it.

If you paste the output of subjectAltName from your leaf.pem and your Ingress snippet, I’ll spot-check that everything matches.