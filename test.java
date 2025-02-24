deployEnvironments:
  - name: aks_dev
    environment: dev
    displayName: DF_CUI
    condition: eq('${{ parameters.deployStackName }}', 'aks')  # Runs only if deployStackName is "aks"
    pool: sc-linux-devfactory
    applicationSecretConfigs:
      - secretName: dev_factory_uaas_dev_tmx_db_ssap
        secretValue: $(dev_factory_uaas_dev_tmx_db_ssap)
    notifyUsers:
      - channels.security@sc.com
    aks_helm_params:
      tenanId: 4e84ad11-063a-42d0-b0a1-595b22d0db06
      subscriptionName: catalyst-sg-dev
      clustername: 51366-s2bapi-dev-sg-b7cbg
      resourcegroup: 51366-S2B-API-RG
      appId: 719f7b7f-63b6-4eb3-9de2-58bd51c916dc
      SPN_Credentials: $(51366-S2B-API-SPN)
      helm_chart_path: "./$(artifactId)/"
      helm_chart_values: "./$(artifactId)/aks-dev-values.yaml"
      releaseName: "$(artifactId)"
      Namespace: "s2b-security-dev"

  - name: aks_uat
    environment: dev
    displayName: DF_UAT
    condition: eq('${{ parameters.deployStackName }}', 'aks')  # Runs only if deployStackName is "aks"
    pool: sc-linux-devfactory
    applicationSecretConfigs:
      - secretName: dev_factory_uaas_dev_tmx_db_ssap
        secretValue: $(dev_factory_uaas_dev_tmx_db_ssap)
    notifyUsers:
      - channels.security@sc.com
    aks_helm_params:
      tenanId: 4e84ad11-063a-42d0-b0a1-595b22d0db06
      subscriptionName: catalyst-sg-dev
      clustername: 51366-s2bapi-dev-sg-b7cbg
      resourcegroup: 51366-S2B-API-RG
      appId: 719f7b7f-63b6-4eb3-9de2-58bd51c916dc
      SPN_Credentials: $(51366-S2B-API-SPN)
      helm_chart_path: "./$(artifactId)/"
      helm_chart_values: "./$(artifactId)/aks-uat-values.yaml"
      releaseName: "$(artifactId)"
      Namespace: "s2b-security-uat"

  - name: ske_hk
    environment: dev
    displayName: HK-SKE
    condition: eq('${{ parameters.deployStackName }}', 'skecaasapp')  # Runs only if deployStackName is "skecaasapp"
    notifyUsers:
      - channels.security@sc.com
    devApproval: false
    dependsOn:
      - CI
    pool: sc-linux
    applicationSecretConfigs:
      - secretName: tmxusr_hashicorp_uri_stg
        secretPath: '/scb/26066/global/app/kv/data/api_token_tmxusrhashicorpuristg'
    secretsFileSelector: "**/*.{yaml,yml}"
    k8s_params:
      server: https://api.skes006.50933.hk.app.standardchartered.com:6443
      token: $(ske_k8s_params_token)
    helm_params:
      namespace: t-26066-s2bsec-s2b-security
      release: $(artifactId)
      chart: "./$(artifactId)"
      values_file: "./$(artifactId)/stg-prod-values.yaml"
      set_files: []
      extra_args: "--debug"

  - name: dev_sg
    environment: dev
    displayName: SG-SKE
    condition: eq('${{ parameters.deployStackName }}', 'skecaasapp')  # Runs only if deployStackName is "skecaasapp"
    notifyUsers:
      - channels.security@sc.com
    devApproval: false
    pool: sc-linux
    applicationSecretConfigs:
      - secretName: tmxusr_hashicorp_uri_stg
        secretPath: '/scb/26066/global/app/kv/data/api_token_tmxusrhashicorpuristg'
    secretsFileSelector: "**/*.{yaml,yml}"
    k8s_params:
      server: https://api.skes006.50933.sg.app.standardchartered.com:6443
      token: $(sg_ske_k8s_params_token)
    helm_params:
      namespace: t-26066-s2bsec-s2b-security
      release: $(artifactId)
      chart: "./$(artifactId)"
      values_file: "./$(artifactId)/stg-prod-values.yaml"
      set_files: []
      extra_args: "--debug"
