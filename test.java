Got you—here’s a clean, working pattern for path-based routing on a single host. I’ll show two variants:
	•	A. Pass-through paths (your app serves /app1, /app2 as-is)
	•	B. Strip the prefix (your app serves /, we map /app1 → /)

Assumes you’re using the NGINX Ingress Controller and everything is in the same namespace.

⸻

A) Pass-through paths (no rewrite)

service.yaml

apiVersion: v1
kind: Service
metadata:
  name: app1-svc
spec:
  type: ClusterIP
  selector:
    app: app1
  ports:
    - name: http
      port: 80
      targetPort: 8080
---
apiVersion: v1
kind: Service
metadata:
  name: app2-svc
spec:
  type: ClusterIP
  selector:
    app: app2
  ports:
    - name: http
      port: 80
      targetPort: 8080

Your Deployments should expose containers on containerPort: 8080 and have matching labels app: app1 / app: app2.

ingress.yaml

apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: apps-ingress
  annotations:
    kubernetes.io/ingress.class: "nginx"   # or use spec.ingressClassName below
spec:
  ingressClassName: nginx                  # preferred if your controller is named "nginx"
  rules:
    - host: apps.example.com               # change to your DNS name
      http:
        paths:
          - path: /app1
            pathType: Prefix
            backend:
              service:
                name: app1-svc
                port:
                  number: 80
          - path: /app2
            pathType: Prefix
            backend:
              service:
                name: app2-svc
                port:
                  number: 80

Use this if the apps are actually mounted under /app1 and /app2 (static assets, links, health, etc. all expect the prefix).

⸻

B) Strip prefix (rewrite /app1 → /)

If your app expects to live at / (root), add a rewrite. With modern NGINX ingress you can use a regex path plus rewrite-target.

ingress-rewrite.yaml

apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: apps-ingress
  annotations:
    kubernetes.io/ingress.class: "nginx"
    nginx.ingress.kubernetes.io/use-regex: "true"
    nginx.ingress.kubernetes.io/rewrite-target: /$2
spec:
  ingressClassName: nginx
  rules:
    - host: apps.example.com
      http:
        paths:
          # /app1, /app1/, /app1/anything -> /, /, /anything for app1
          - path: /app1(/|$)(.*)
            pathType: Prefix
            backend:
              service:
                name: app1-svc
                port:
                  number: 80

          # /app2, /app2/, /app2/anything -> /, /, /anything for app2
          - path: /app2(/|$)(.*)
            pathType: Prefix
            backend:
              service:
                name: app2-svc
                port:
                  number: 80

The /(.*) capture becomes $2; we drop the leading segment so your app sees /… at root.

⸻

Common causes of 404 Not Found (and quick fixes)

Most 404s come from the ingress controller’s default backend, meaning the request didn’t match a rule or the service had no endpoints.
	1.	Ingress class mismatch
	•	Check your controller’s class (often nginx).
	•	Fix: Ensure either metadata.annotations["kubernetes.io/ingress.class"] = "nginx" or spec.ingressClassName: nginx matches the controller.
	•	Verify:

kubectl get ingressclass
kubectl describe ingress apps-ingress


	2.	Host mismatch
	•	Your browser/curl must send Host: apps.example.com.
	•	Fix: Use the correct DNS name or test with curl:

curl -I -H "Host: apps.example.com" http://<INGRESS-LB-IP>/app1


	3.	DNS not pointing to the Ingress LB
	•	Fix: Point apps.example.com A/AAAA record to your ingress LoadBalancer IP.
	•	Verify:

kubectl get svc -n ingress-nginx   # or your controller namespace
nslookup apps.example.com


	4.	No endpoints behind the Service
	•	If your Pods aren’t Ready or label selectors don’t match, the Service has zero endpoints.
	•	Fix: Align labels; ensure Pods Ready.

kubectl get pods -o wide
kubectl get endpoints app1-svc -o yaml
kubectl describe svc app1-svc


	5.	Wrong service port / targetPort
	•	The Service port: 80 must map to the Pod containerPort: 8080 (or whatever your app listens on).
	•	Fix: Ensure targetPort matches the container.
	6.	Path behavior not as expected
	•	If your app needs root but you used variant A (no rewrite), internal links may break or you’ll get 404s.
	•	Fix: Use variant B with rewrite-target, or configure your app’s basePath.
	7.	Controller isn’t running or wrong namespace
	•	Fix: Ensure the NGINX ingress controller is installed and healthy.

kubectl get pods -n ingress-nginx
kubectl logs -n ingress-nginx deploy/ingress-nginx-controller


	8.	Conflicting Ingress rules
	•	Another Ingress with same host and overlapping paths can shadow routes.
	•	Fix: Consolidate rules or adjust path specificity.
	9.	App-level 404
	•	Ingress forwarded correctly, but your app returned 404.
	•	Fix: Port-forward the Service and test directly:

kubectl port-forward svc/app1-svc 8081:80
curl -I http://localhost:8081/        # for rewrite case
curl -I http://localhost:8081/app1    # for pass-through case


	10.	Regex + rewrite misconfiguration
	•	If using regex paths, you must set use-regex: "true" and match the right capture groups.
	•	Fix: Double-check (.*) groups and $2 usage.

⸻

Minimal Deployment example (optional, for completeness)

If you need a quick Pod to test:

apiVersion: apps/v1
kind: Deployment
metadata:
  name: app1
spec:
  replicas: 2
  selector:
    matchLabels:
      app: app1
  template:
    metadata:
      labels:
        app: app1
    spec:
      containers:
        - name: app1
          image: hashicorp/http-echo:0.2.3
          args: ["-text=Hello from app1", "-listen=:8080"]
          ports:
            - containerPort: 8080
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: app2
spec:
  replicas: 2
  selector:
    matchLabels:
      app: app2
  template:
    metadata:
      labels:
        app: app2
    spec:
      containers:
        - name: app2
          image: hashicorp/http-echo:0.2.3
          args: ["-text=Hello from app2", "-listen=:8080"]
          ports:
            - containerPort: 8080


⸻

If you tell me which controller you’re on (NGINX, Traefik, HAProxy, AKS AGIC, GKE, etc.) and whether your apps expect a basePath or root, I can tailor the exact Ingress/annotations.