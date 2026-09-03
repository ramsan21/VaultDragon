[{
  "application": "26066-uaas-ms",
  "sourceType": "VM",
  "environment": "SIT1",
  "host": "10.1.2.3",
  "hosts": ["10.1.2.4", "10.1.2.5"],
  "logPath": "/var/log/app/app.log",
  "username": "svcuser",
  "credentialRef": "vm-sit1"
},{
"application": "26066-uaas-ms",
"sourceType": "SKE",
"environment": "PROD",
"k8sContext": "prod-cluster",
"k8sNamespace": "uaas",
"k8sPodSelector": "app=uaas",
"k8sLoginCommand": "kubectl login prod-cluster",
"formatType": "JSON"
}]
