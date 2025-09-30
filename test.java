I see what’s happening in your screenshot. Your Ingress YAML is correctly referencing the TLS secret (secretName: ingress-tls-secret), but the browser still shows the default self-signed certificate from your ingress controller. That means the TLS secret isn’t being picked up correctly by the ingress. Let’s walk through the possible causes and fixes:

⸻

✅ Steps to Apply Your Certificate in Ingress
	1.	Verify the TLS secret exists in the same namespace
	•	Your Ingress is created in namespace t-26066-s2bsec-inc1 (from screenshot).
	•	Your TLS secret (ingress-tls-secret) must also be in the same namespace as the Ingress.

kubectl get secret ingress-tls-secret -n t-26066-s2bsec-inc1


	2.	Check the TLS secret format
	•	The secret must be of type kubernetes.io/tls.
	•	It should contain exactly two keys: tls.crt and tls.key.

kubectl describe secret ingress-tls-secret -n t-26066-s2bsec-inc1

If you see data: tls.crt and tls.key, it’s good. If not, recreate it:

kubectl create secret tls ingress-tls-secret \
  --cert=your-cert.crt \
  --key=your-cert.key \
  -n t-26066-s2bsec-inc1


	3.	Confirm your Ingress spec has TLS block
In your YAML I see the tls: section but double-check it matches:

tls:
  - hosts:
      - z2b-sec-cl-pr1-v1-stg.26066.app.standardchartered.com
    secretName: ingress-tls-secret

	•	Make sure the hostname under hosts: exactly matches the certificate CN/SAN.
	•	Even a small mismatch (like missing www.) will cause browsers to reject it.

	4.	Check your ingress controller
	•	If you’re using NGINX ingress, confirm that it is watching the right namespace (some setups require adding an annotation like kubernetes.io/ingress.class: nginx).
	•	Example:

metadata:
  annotations:
    kubernetes.io/ingress.class: nginx


	5.	Force reload
Sometimes ingress controller caches secrets. You can delete the ingress and recreate it, or restart the ingress controller pod:

kubectl delete ingress your-ingress -n t-26066-s2bsec-inc1
kubectl apply -f ingress.yaml

Or restart NGINX ingress pods:

kubectl rollout restart deployment ingress-nginx-controller -n ingress-nginx



⸻

⚠️ Common Mistakes That Cause Default Fake Cert
	•	Secret not in the same namespace as the Ingress.
	•	Secret not of type kubernetes.io/tls.
	•	Host in tls.hosts doesn’t match the domain in the browser.
	•	Ingress controller not annotated with the right class.

⸻

👉 Do you want me to rewrite your current ingress.yml into a corrected version with the proper tls block and annotations so you can apply directly?