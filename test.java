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
    displayName: place to deploy
    type: string
    values:
      - "aks"
      - "ske"  # Changed from "skecaasapp" to "ske" for clarity
    default: "aks"

resources:
  repositories:
    - repository: governed-templates
      name: dj-core/governed-templates
      ref: main
      type: git

variables:
  - group: 26066-NonProd
  - name: artifactId
    value: "ms-tmx"

extends:
  template: governed-template/build-and-deploy.yml@governed-template
  parameters:
    releaseId: ${{ parameters.releaseId }}
    ITAM: "26066"
    adoManagedVault: true
    featureRelease: true
    runDevStage: true
    buildStackName: maven
    buildStackParams:
      pool: "sc-linux"
      goals: "clean install"
      jdkVersion: "17"
      packageVersion: $(Version)
      mavenPomFile: "pom.xml"
      skipJacocoCoverage: true
      publishJUnitResults: true
      testResultsFiles: "**/surefire-reports/TEST-*.xml"
      binaryPath: "./target/classes"
      sonarSources: "./src/main"
      sonarExclusions: "**/db/model/*, **/model/**/*, **/repository/*, **/vo/*, com/sc/dcd"
      imageListFilePath: "images.yml"
      dockerBuild: true
      dockerRepository: "cib-ss"
      dockerFilePaths:
        - path: "$(Build.Repository.Name)/ci/Dockerfile"
          imageName: "$(artifactId)"
          imageTag: "$(Version)"

    dockerArguments: "--no-cache --pull --build-arg BUILD_ARTIFACT=target/$(artifactId)-$(Version).jar"
    dockerBuildContext: "$(Build.Repository.Name)"
    deployStackName: "helm"
    deploymentFolderName: "ci/$(artifactId)"
    targetPathArtifactory: "generic-release/cib-dcda/ado/helm/$(Build.Repository.Name)/"
    archiveType: "tar"
    featureRelease: true
    featureBranchScan: true
    skipEarlyFeedback: true
    calculateImageDigest: true
    postInputFileList:
      - "ci/$(artifactId)/values.yaml"
    postVariableList:
      - name: __applicationImageDigest__
        value: "$(DockerImageDigest_ms-tmx)"
    deployStackName: $(deployStackName)
    deployStackParams:
      run_mode: helm

deployEnvironments:
  # AKS Environments (run when deployStackName is 'aks')
  - name: aks_dev
    condition: eq('${{ parameters.deployStackName }}', 'aks')
    environment: dev
    displayName: DF_CUI
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
    condition: eq('${{ parameters.deployStackName }}', 'aks')
    environment: dev
    displayName: DF_UAT
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

  # SKE Environments (run when deployStackName is 'ske')
  - name: ske_hk
    condition: eq('${{ parameters.deployStackName }}', 'ske')
    environment: dev
    displayName: HK-SKE
    notifyUsers:
      - channels.security@sc.com
    devApproval: false
    dependsOn:  # Ensure SKE runs after AKS if both are triggered
      - aks_dev
      - aks_uat
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
    condition: eq('${{ parameters.deployStackName }}', 'ske')
    environment: dev
    displayName: SG-SKE
    notifyUsers:
      - channels.security@sc.com
    devApproval: false
    dependsOn:  # Ensure SKE runs after AKS if both are triggered
      - aks_dev
      - aks_uat
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
    
