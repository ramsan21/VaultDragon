To create the admin user token and mount it to /opt/secrets, follow these steps:

1. Create a Kubernetes Secret for the Admin User Token

Run the following command to create a secret named bot-token:

kubectl create secret generic bot-token \
    --from-literal=adminuser.token='your-admin-user-token-value' \
    --from-literal=edmi.token='your-edmi-token-value'

Alternatively, if you have the token values in files:

kubectl create secret generic bot-token \
    --from-file=adminuser.token=./adminuser.token \
    --from-file=edmi.token=./edmi.token

Check if the secret was created:

kubectl get secrets bot-token -o yaml

2. Update Your Deployment or Pod YAML

Modify your Deployment or Pod manifest to mount the secret.

apiVersion: v1
kind: Pod
metadata:
  name: my-pod
spec:
  containers:
    - name: my-container
      image: my-image
      volumeMounts:
        - name: bot-token
          mountPath: /opt/secrets
          readOnly: true
  volumes:
    - name: bot-token
      secret:
        secretName: bot-token
        items:
          - key: adminuser.token
            path: adminuser.token
          - key: edmi.token
            path: edmi.token

3. Apply the YAML to Kubernetes

Save the YAML file as pod.yaml and apply it:

kubectl apply -f pod.yaml

4. Verify the Secret Mount

Once the pod is running, check if the secret is mounted correctly:

kubectl exec -it my-pod -- ls /opt/secrets

You should see:

adminuser.token
edmi.token

To view the contents:

kubectl exec -it my-pod -- cat /opt/secrets/adminuser.token

Summary of Steps
	1.	Create the secret using kubectl create secret generic bot-token.
	2.	Update the pod or deployment YAML to mount the secret at /opt/secrets.
	3.	Apply the YAML and verify the secret is mounted.

Let me know if you need further troubleshooting!