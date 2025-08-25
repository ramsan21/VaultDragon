apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: myapps-ingress
  annotations:
    kubernetes.io/ingress.class: nginx
spec:
  rules:
  - host: tmx.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: tmx-service
            port:
              number: 80
  - host: csa.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: csa-service
            port:
              number: 80