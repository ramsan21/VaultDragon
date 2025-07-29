kubectl describe pod <pod-name> -n <namespace>


kubectl get pod <pod-name> -n <namespace> -o jsonpath="{.status.containerStatuses[*].lastState.terminated.reason}"

kubectl logs <pod-name> -n <namespace> --previous