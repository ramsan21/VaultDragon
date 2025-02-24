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

stages:
- stage: Deploy
  jobs:

  # Deploy to AKS (if deployStackName is aks)
  - ${{ if eq(parameters.deployStackName, 'aks') }}:
    - job: Deploy_AKS
      displayName: "Deploy to AKS"
      pool: sc-linux-devfactory  # Pool for AKS Deployment

      steps:
      - script: echo "Deploying to AKS..."
      - script: |
          echo "Setting AKS Kubernetes Parameters"
          echo "Cluster: 51366-s2bapi-dev-sg-b7cbg"
          echo "Resource Group: 51366-S2B-API-RG"
          echo "Namespace: s2b-security-dev"
      - script: |
          echo "Deploying using AKS Helm values"
          helm upgrade --install $(artifactId) ./$(artifactId) \
            --namespace s2b-security-dev \
            --values ./$(artifactId)/aks-dev-values.yaml

  # Deploy to SKE (if deployStackName is skecaasapp)
  - ${{ if eq(parameters.deployStackName, 'skecaasapp') }}:
    - job: Deploy_SKE
      displayName: "Deploy to SKE"
      pool: sc-linux  # Pool for SKE Deployment

      steps:
      - script: echo "Deploying to SKE..."
      - script: |
          echo "Setting SKE Kubernetes Parameters"
          echo "Server: https://api.skes006.50933.hk.app.standardchartered.com:6443"
          echo "Namespace: t-26066-s2bsec-s2b-security"
      - script: |
          echo "Deploying using SKE Helm values"
          helm upgrade --install $(artifactId) ./$(artifactId) \
            --namespace t-26066-s2bsec-s2b-security \
            --values ./$(artifactId)/stg-prod-values.yaml \
            --debug