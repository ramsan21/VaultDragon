Good — your screenshot shows logs from your NGINX Ingress Controller pod (t-26066-s2bsecks-inc1-ingress-nginx-controller-7f96ff576-zpq5s), and I can explain what’s happening:

⸻

🔍 Key Log Lines and Meaning

Service "t-26066-s2bsecks-s2b-security/ms-csa-reports-service" does not have any active Endpoint.

➡️ Meaning:
The Kubernetes Service exists, but it has no active backend pods (i.e., no pods are matching the selector labels defined in the Service).
NGINX can’t route traffic because there’s nowhere to send it.

⸻


ignoring ingress ms-csa-reports-ingress in t-26066-s2bsecks-s2b-security: no object matching key "t-26066-s2bsecks-s2b-security/ms-csa-reports-service" in local store

➡️ Meaning:
Your Ingress resource refers to a Service that doesn’t currently exist or isn’t visible to the Ingress controller.
Either:
	•	The service name is misspelled in the ingress YAML.
	•	The ingress and service are in different namespaces.
	•	Or the service hasn’t been created yet / got deleted.

⸻


successfully validated configuration, accepting ingress ...
Backend successfully reloaded

➡️ This means the Ingress controller has successfully reloaded its configuration, but it will still skip any routes pointing to invalid or inactive services.

⸻

🚨 Root Cause Summary

Your ingress is defined, but the backend service has no endpoints.
In short:
	•	The pods backing ms-csa-reports-service are not running, crashed, or not matching the selector in the service manifest.

⸻

✅ How to Fix It
	1.	Check service details:

kubectl get svc -n t-26066-s2bsecks-s2b-security

Confirm that ms-csa-reports-service exists.

	2.	Check pod labels:

kubectl get pods -n t-26066-s2bsecks-s2b-security --show-labels

Make sure the pod labels match the selector in the Service definition. Example:

selector:
  app: ms-csa-reports


	3.	Check endpoints:

kubectl get endpoints -n t-26066-s2bsecks-s2b-security ms-csa-reports-service -o yaml

If subsets: [], it means no pods match → no endpoints.

	4.	Check ingress:

kubectl describe ingress -n t-26066-s2bsecks-s2b-security ms-csa-reports-ingress

Ensure service.name and service.port.number align with your service.

⸻

🧩 Example YAML Alignment Check

# Deployment
metadata:
  name: ms-csa-reports
  labels:
    app: ms-csa-reports

# Service
metadata:
  name: ms-csa-reports-service
spec:
  selector:
    app: ms-csa-reports
  ports:
    - port: 8080
      targetPort: 8080

# Ingress
spec:
  rules:
    - host: reports.example.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: ms-csa-reports-service
                port:
                  number: 8080

If any of these app or service.name values differ, your ingress will not have endpoints.

⸻

Would you like me to help you write a kubectl diagnostic command set (a ready-to-copy script that verifies namespace, service, pods, and ingress automatically)?