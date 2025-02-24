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
  - job: DeployApp
    displayName: "Deploy Application"
    pool: sc-linux  # Common pool

    steps:
    - script: echo "Deployment Stack Selected: ${{ parameters.deployStackName }}"

    - ${{ if eq(parameters.deployStackName, 'aks') }}:
      - script: |
          echo "Deploying to AKS..."
          echo "Setting AKS Kubernetes Parameters"
          echo "Cluster: 51366-s2bapi-dev-sg-b7cbg"
          echo "Resource Group: 51366-S2B-API-RG"
          echo "Namespace: s2b-security-dev"
      - script: |
          echo "Deploying using AKS Helm values"
          helm upgrade --install $(artifactId) ./$(artifactId) \
            --namespace s2b-security-dev \
            --values ./$(artifactId)/aks-dev-values.yaml

    - ${{ if eq(parameters.deployStackName, 'skecaasapp') }}:
      - script: |
          echo "Deploying to SKE..."
          echo "Setting SKE Kubernetes Parameters"
          echo "Server: https://api.skes006.50933.hk.app.standardchartered.com:6443"
          echo "Namespace: t-26066-s2bsec-s2b-security"
      - script: |
          echo "Deploying using SKE Helm values"
          helm upgrade --install $(artifactId) ./$(artifactId) \
            --namespace t-26066-s2bsec-s2b-security \
            --values ./$(artifactId)/stg-prod-values.yaml \
            --debug