kubectl get pod ms-tmx-7948669875-qgfcq -n t-26066-s2bsec-s2b-security \
  -o jsonpath="{.status.containerStatuses[0].lastState.terminated.reason}"