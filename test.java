apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: myapps-ingress
  annotations:
    kubernetes.io/ingress.class: nginx
spec:
  rules:
  - host: myapps.example.com
    http:
      paths:
      - path: /tmx
        pathType: Prefix
        backend:
          service:
            name: tmx-service
            port:
              number: 80
      - path: /csa
        pathType: Prefix
        backend:
          service:
            name: csa-service
            port:
              number: 80