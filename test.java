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
# **1st Stage: Deploy to AKS Dev**
- stage: Deploy_AKS_Dev
  displayName: "Deploy to AKS Dev"
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

# **2nd Stage: Deploy to AKS QA**
- stage: Deploy_AKS_QA
  displayName: "Deploy to AKS QA"
  dependsOn: Deploy_AKS_Dev  # Runs after AKS Dev
  jobs:
  - job: Deploy_AKS_QA
    displayName: "Deploying to AKS QA"
    pool: sc-linux-devfactory
    steps:
    - script: |
        echo "Deploying to AKS QA..."
        helm upgrade --install $(artifactId) ./$(artifactId) \
          --namespace s2b-security-qa \
          --values ./$(artifactId)/aks-qa-values.yaml

# **3rd Stage: Deploy to SKE HK (Runs if Either AKS Dev OR AKS QA is Successful)**
- stage: Deploy_SKE_HK
  displayName: "Deploy to SKE HK"
  dependsOn:
    - Deploy_AKS_Dev
    - Deploy_AKS_QA
  condition: or(succeeded('Deploy_AKS_Dev'), succeeded('Deploy_AKS_QA'))  # Condition to run if any AKS stage succeeds
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

# **4th Stage: Deploy to SKE Prod (Only Runs After SKE HK Completes Successfully)**
- stage: Deploy_SKE_Prod
  displayName: "Deploy to SKE Prod"
  dependsOn: Deploy_SKE_HK  # Ensures it runs after SKE HK
  jobs:
  - job: Deploy_SKE_Prod
    displayName: "Deploying to SKE Prod"
    pool: sc-linux
    steps:
    - script: |
        echo "Deploying to SKE Prod..."
        helm upgrade --install $(artifactId) ./$(artifactId) \
          --namespace t-26066-s2bsec-s2b-prod \
          --values ./$(artifactId)/ske-prod-values.yaml