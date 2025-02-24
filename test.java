trigger:
  - develop
  - release/*
  - feature/*
  - main

parameters:
  - name: releaseId
    displayName: "Release WorkItem ID"
    type: string
    default: "000000"

  - name: deployStackName
    displayName: "Place to deploy"
    type: string
    values:
      - aks
      - skecaasapp
    default: aks

variables:
  - group: 26066-NonProd
  - name: artifactId
    value: "ms-tmx"

  # **Dynamic Repository Name based on deployStackName**
  - name: repositoryName
    ${{ if eq(parameters.deployStackName, 'aks') }}:
      value: "dj-core/governed-templates"
    ${{ if eq(parameters.deployStackName, 'skecaasapp') }}:
      value: "dj-core/ske-governed-templates"

  # **Dynamic Agent Pool based on deployStackName**
  - name: agentPool
    ${{ if eq(parameters.deployStackName, 'aks') }}:
      value: "sc-linux-devfactory"
    ${{ if eq(parameters.deployStackName, 'skecaasapp') }}:
      value: "sc-linux"

resources:
  repositories:
    - repository: governed-templates
      name: $(repositoryName)  # Uses dynamically set repository name
      ref: main
      type: git

stages:
# **1st Stage: Deploy to AKS Dev**
- stage: Deploy_AKS_Dev
  displayName: "Deploy to AKS Dev"
  jobs:
  - job: Deploy_AKS_Dev
    displayName: "Deploying to AKS Dev"
    pool: $(agentPool)  # Uses dynamically set pool
    steps:
    - script: |
        echo "Deploying to AKS Dev..."
        helm upgrade --install $(artifactId) ./$(artifactId) \
          --namespace s2b-security-dev \
          --values ./$(artifactId)/aks-dev-values.yaml

# **2nd Stage: Deploy to AKS UAT**
- stage: Deploy_AKS_UAT
  displayName: "Deploy to AKS UAT"
  dependsOn: Deploy_AKS_Dev
  jobs:
  - job: Deploy_AKS_UAT
    displayName: "Deploying to AKS UAT"
    pool: $(agentPool)  # Uses dynamically set pool
    steps:
    - script: |
        echo "Deploying to AKS UAT..."
        helm upgrade --install $(artifactId) ./$(artifactId) \
          --namespace s2b-security-uat \
          --values ./$(artifactId)/aks-uat-values.yaml

# **3rd Stage: Deploy to SKE HK (Runs if Either AKS Dev or AKS UAT is Successful)**
- stage: Deploy_SKE_HK
  displayName: "Deploy to SKE HK"
  dependsOn:
    - Deploy_AKS_Dev
    - Deploy_AKS_UAT
  condition: or(succeeded('Deploy_AKS_Dev'), succeeded('Deploy_AKS_UAT'))  # Runs if either AKS stage succeeds
  jobs:
  - job: Deploy_SKE_HK
    displayName: "Deploying to SKE HK"
    pool: $(agentPool)  # Uses dynamically set pool
    steps:
    - script: |
        echo "Deploying to SKE HK..."
        helm upgrade --install $(artifactId) ./$(artifactId) \
          --namespace t-26066-s2bsec-s2b-security \
          --values ./$(artifactId)/stg-prod-values.yaml

# **4th Stage: Deploy to SKE SG (Only Runs After SKE HK Completes Successfully)**
- stage: Deploy_SKE_SG
  displayName: "Deploy to SKE SG"
  dependsOn: Deploy_SKE_HK  # Runs only after SKE HK
  jobs:
  - job: Deploy_SKE_SG
    displayName: "Deploying to SKE SG"
    pool: $(agentPool)  # Uses dynamically set pool
    steps:
    - script: |
        echo "Deploying to SKE SG..."
        helm upgrade --install $(artifactId) ./$(artifactId) \
          --namespace t-26066-s2bsec-s2b-security \
          --values ./$(artifactId)/stg-prod-values.yaml