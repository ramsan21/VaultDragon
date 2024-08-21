1. Backup files with current date
/prd/starss/uaas/secrets/hc.cer
/prd/starss/uaas/secrets/hc-pkcs8.key

/prd/starss/uaas/conf_v2/uaas_server.properties
/prd/starss/uaasadm/conf_v2/uaas_server.properties

copy the files from 
/tmp/uaas/hc.cer 		to /prd/starss/uaas/secrets/hc.cer
/tmp/uaas/hc-pkcs8.key 	to /prd/starss/uaas/secrets/hc.cer

/tmp/uaas/uaas_server.properties to /prd/starss/uaas/conf_v2/uaas_server.properties
/tmp/uaasadm/uaas_server.properties to /prd/starss/uaasadm/conf_v2/uaas_server.properties

#!/bin/bash

# Get current date in YYYYMMDD format
current_date=$(date +%Y%m%d)

# Backup files
cp /prd/starss/uaas/secrets/hc.cer /prd/starss/uaas/secrets/hc.cer.$current_date.bak
cp /prd/starss/uaas/secrets/hc-pkcs8.key /prd/starss/uaas/secrets/hc-pkcs8.key.$current_date.bak
cp /prd/starss/uaas/conf_v2/uaas_server.properties /prd/starss/uaas/conf_v2/uaas_server.properties.$current_date.bak
cp /prd/starss/uaasadm/conf_v2/uaas_server.properties /prd/starss/uaasadm/conf_v2/uaas_server.properties.$current_date.bak

# Copy new files
cp /tmp/uaas/hc.cer /prd/starss/uaas/secrets/hc.cer
cp /tmp/uaas/hc-pkcs8.key /prd/starss/uaas/secrets/hc-pkcs8.key
cp /tmp/uaas/uaas_server.properties /prd/starss/uaas/conf_v2/uaas_server.properties
cp /tmp/uaasadm/uaas_server.properties /prd/starss/uaasadm/conf_v2/uaas_server.properties

echo "Backup and copy operation completed successfully."
