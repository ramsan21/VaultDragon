Nice—this is much clearer now. From your two screenshots:

* `endpoints/ms-csa-reports-service` **exists** and has an address `10.233.100.103:8080`.
* The Ingress `ms-csa-reports-ingress` points to **service: ms-csa-reports-service / port 8080** and NGINX shows the same backend `(10.233.100.103:8080)`.

So the earlier “no active Endpoint” error is resolved. If you’re still not getting traffic through the Ingress, it’s almost always one of these:

---

## 1) Service selects the wrong pod

Make sure the endpoint IP actually belongs to the **ms-csa-reports** pod and not some other pod.

```bash
# Show which pod has that IP
kubectl get pod -n t-26066c-s2bsecks-s2b-security -o wide | grep 10.233.100.103

# Compare pod labels vs Service selector
kubectl get svc ms-csa-reports-service -n t-26066c-s2bsecks-s2b-security -o yaml | yq '.spec.selector'
kubectl get pod <the-pod-name> -n t-26066c-s2bsecks-s2b-security --show-labels
```

> If labels don’t match the Service selector exactly, fix the Service selector or the pod labels so the **intended** app backs the service.

---

## 2) App protocol mismatch (HTTPS vs HTTP)

Your Ingress has **Kong** annotations (`konghq.com/*`) but you’re using **NGINX**. NGINX ignores those. If your app speaks **HTTPS** on its container port (Spring Boot with `server.ssl.enabled=true`, common on 8443/8080), NGINX needs to be told:

```yaml
metadata:
  annotations:
    nginx.ingress.kubernetes.io/backend-protocol: "HTTPS"
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/proxy-read-timeout: "3600"
    nginx.ingress.kubernetes.io/proxy-send-timeout: "3600"
```

> Remove the `konghq.com/*` annotations; replace with the NGINX equivalents above.

If your app is **HTTP** only, keep `backend-protocol: "HTTP"` (or omit it).

---

## 3) App not actually listening on the port the Service targets

Double-check the container is listening on 8080 (or whatever `targetPort` is):

```bash
kubectl exec -n t-26066c-s2bsecks-s2b-security <the-pod> -- sh -c "netstat -tlnp || ss -tln"
# or curl locally
kubectl exec -n t-26066c-s2bsecks-s2b-security <the-pod> -- curl -vk http://127.0.0.1:8080/actuator/health
```

If it’s listening on a different port (e.g., 8443), fix the Service `targetPort` and the Ingress backend accordingly.

---

## 4) Readiness not passing → flapping endpoints

If you have a readinessProbe that isn’t succeeding, the endpoints may vanish/reappear. Use a simple readiness (health/ready endpoint) that reflects app readiness.

```yaml
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 10
  periodSeconds: 5
  timeoutSeconds: 2
  failureThreshold: 3
```

---

## 5) Quick end-to-end test (bypassing Ingress)

If the two checks above pass, validate the Service itself:

```bash
# Port-forward the Service and hit it from your laptop
kubectl -n t-26066c-s2bsecks-s2b-security port-forward svc/ms-csa-reports-service 8080:8080
curl -vk http://127.0.0.1:8080/
```

* Works here but **fails via Ingress** → issue is Ingress config (host, TLS, annotations).
* Fails here → issue is Service/Pod port or app itself.

---

## 6) Ingress host/TLS sanity

Confirm you’re curling the exact host defined in the Ingress and that your TLS secret (if any) matches:

```bash
kubectl describe ingress -n t-26066c-s2bsecks-s2b-security ms-csa-reports-ingress
# Then test (replace with your host):
curl -vk https://s2bsec-csa-reports-sit3-stg.26066.app.standardchartered.com/ -H 'Host: s2bsec-csa-reports-sit3-stg.26066.app.standardchartered.com'
```

---

### Minimal working matrix

* **HTTP app on 8080**

  * Service `port: 8080`, `targetPort: 8080`
  * Ingress backend → service:8080
  * (No `backend-protocol` or set `"HTTP"`)

* **HTTPS app on 8443 (or 8080 TLS)**

  * Service `port: 8443`, `targetPort: 8443` (or 8080 if that’s where TLS lives)
  * Ingress backend → service:8443
  * Add `nginx.ingress.kubernetes.io/backend-protocol: "HTTPS"`

---

If you paste your **Service YAML** (spec.selector + ports), the **pod labels**, and whether your app is HTTP or HTTPS, I’ll give you the exact patch to apply.
