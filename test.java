trigger:
  - develop
  - release/*
  - feature/*
  - main

parameters:
  - name: releaseId
    displayName: Release WorkItem ID
    type: string
    default: "000000"

  - name: deployStackName
    displayName: Place to deploy
    type: string
    values:
      - "aks"
      - "skecaasapp"
    default: "aks"

variables:
  - group: 26066-NonProd
  - name: artifactId
    value: "ms-tmx"

resources:
  repositories:
    - repository: governed-templates
      name: dj-core/governed-templates
      ref: main
      type: git

stages:
# **1st Stage: Deploy to AKS Dev**
- stage: Deploy_AKS_Dev
  displayName: "Deploy to AKS Dev"
  condition: eq('${{ parameters.deployStackName }}', 'aks')
  jobs:
  - job: Deploy_AKS_Dev
    displayName: "Deploying to AKS Dev"
    pool: sc-linux-devfactory
    steps:
    - script: |
        echo "Deploying to AKS Dev..."
        helm upgrade --install $(artifactId) ./$(artifactId) \
          --namespace s2b-security-dev \
          --values ./$(artifactId)/aks-dev-values.yaml

    # **AKS Helm Parameters**
    - script: |
        echo "Setting Helm Parameters for AKS Dev..."
        echo "Tenant ID: 4e84ad11-063a-42d0-b0a1-595b22d0db06"
        echo "Subscription Name: catalyst-sg-dev"
        echo "Cluster: 51366-s2bapi-dev-sg-b7cbg"
        echo "Resource Group: 51366-S2B-API-RG"
        echo "App ID: 719f7b7f-63b6-4eb3-9de2-58bd51c916dc"
        echo "SPN Credentials: $(51366-S2B-API-SPN)"

# **2nd Stage: Deploy to AKS UAT**
- stage: Deploy_AKS_UAT
  displayName: "Deploy to AKS UAT"
  dependsOn: Deploy_AKS_Dev
  condition: eq('${{ parameters.deployStackName }}', 'aks')
  jobs:
  - job: Deploy_AKS_UAT
    displayName: "Deploying to AKS UAT"
    pool: sc-linux-devfactory
    steps:
    - script: |
        echo "Deploying to AKS UAT..."
        helm upgrade --install $(artifactId) ./$(artifactId) \
          --namespace s2b-security-uat \
          --values ./$(artifactId)/aks-uat-values.yaml

    # **AKS Helm Parameters**
    - script: |
        echo "Setting Helm Parameters for AKS UAT..."
        echo "Tenant ID: 4e84ad11-063a-42d0-b0a1-595b22d0db06"
        echo "Subscription Name: catalyst-sg-dev"
        echo "Cluster: 51366-s2bapi-dev-sg-b7cbg"
        echo "Resource Group: 51366-S2B-API-RG"
        echo "App ID: 719f7b7f-63b6-4eb3-9de2-58bd51c916dc"
        echo "SPN Credentials: $(51366-S2B-API-SPN)"

# **3rd Stage: Deploy to SKE HK (Runs only after AKS is successfully deployed)**
- stage: Deploy_SKE_HK
  displayName: "Deploy to SKE HK"
  dependsOn:
    - Deploy_AKS_Dev
    - Deploy_AKS_UAT
  condition: and(succeeded('Deploy_AKS_Dev'), succeeded('Deploy_AKS_UAT'))
  jobs:
  - job: Deploy_SKE_HK
    displayName: "Deploying to SKE HK"
    pool: sc-linux
    steps:
    - script: |
        echo "Deploying to SKE HK..."
        helm upgrade --install $(artifactId) ./$(artifactId) \
          --namespace t-26066-s2bsec-s2b-security \
          --values ./$(artifactId)/stg-prod-values.yaml

    # **K8s Parameters**
    - script: |
        echo "Setting Kubernetes Parameters for SKE HK..."
        echo "Server: https://api.skes006.50933.hk.app.standardchartered.com:6443"
        echo "Token: $(ske_k8s_params_token)"

    # **Helm Parameters**
    - script: |
        echo "Setting Helm Parameters for SKE HK..."
        echo "Namespace: t-26066-s2bsec-s2b-security"
        echo "Chart Path: ./$(artifactId)"

# **4th Stage: Deploy to SKE SG (Only Runs After SKE HK Completes Successfully)**
- stage: Deploy_SKE_SG
  displayName: "Deploy to SKE SG"
  dependsOn: Deploy_SKE_HK
  jobs:
  - job: Deploy_SKE_SG
    displayName: "Deploying to SKE SG"
    pool: sc-linux
    steps:
    - script: |
        echo "Deploying to SKE SG..."
        helm upgrade --install $(artifactId) ./$(artifactId) \
          --namespace t-26066-s2bsec-s2b-security \
          --values ./$(artifactId)/stg-prod-values.yaml

    # **K8s Parameters**
    - script: |
        echo "Setting Kubernetes Parameters for SKE SG..."
        echo "Server: https://api.skes006.50933.sg.app.standardchartered.com:6443"
        echo "Token: $(sg_ske_k8s_params_token)"

    # **Helm Parameters**
    - script: |
        echo "Setting Helm Parameters for SKE SG..."
        echo "Namespace: t-26066-s2bsec-s2b-security"
        echo "Chart Path: ./$(artifactId)"
