# CI/CD Pipeline Setup Guide

This document provides instructions for setting up a continuous integration and deployment pipeline using GitHub Actions to build Docker images and push them to Alibaba Cloud Container Registry (ACR).

## Overview

The CI/CD pipeline consists of the following stages:
1. **Test Stage**: Runs automated tests on code changes
2. **Build Stage**: Builds a Docker image from the application
3. **Push Stage**: Pushes the Docker image to ACR
4. **Deploy Stage**: Deploys the image to a Kubernetes cluster

## Prerequisites

- GitHub account with admin access to the repository
- Alibaba Cloud account with ACR service enabled
- Kubernetes cluster (ACK or any other cluster)
- Basic understanding of Docker, Kubernetes, and GitHub Actions

## Step-by-Step Setup

### 1. Configure Alibaba Cloud ACR

1. Log in to the [Alibaba Cloud Console](https://home.console.aliyun.com/)
2. Navigate to Container Registry (ACR)
3. Create a new instance or use an existing one
4. Create a namespace (e.g., `billing-project`)
5. Create a repository named `billing-service` within the namespace
6. Obtain your registry URL (format: `registry.<region>.aliyuncs.com`)

### 2. Set Up Access Credentials

1. In ACR console, go to User Management
2. Create an AccessKey pair or use an existing one
3. Note down the username and password for ACR login

### 3. Configure GitHub Secrets

In your GitHub repository:

1. Go to Settings > Secrets and variables > Actions
2. Click "New repository secret" and add the following secrets:

```
ACR_REGISTRY_URL: registry.<region>.aliyuncs.com
ACR_NAMESPACE: <your-namespace>
ACR_USERNAME: <your-acr-username>
ACR_PASSWORD: <your-acr-password>
KUBECONFIG_DATA: <base64-encoded-kubeconfig>
```

### 4. Prepare Kubernetes Configuration

If deploying to a Kubernetes cluster:

1. Get your kubeconfig file (usually located at `~/.kube/config`)
2. Encode it using base64:
   ```bash
   cat ~/.kube/config | base64
   ```
3. Add the encoded output as the `KUBECONFIG_DATA` secret

### 5. Create GitHub Actions Workflow

Create the file `.github/workflows/cicd.yml` in your repository with the following content:

```yaml
name: Build and Deploy to ACR

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

env:
  REGISTRY: ${{ secrets.ACR_REGISTRY_URL }}
  IMAGE_NAME: ${{ secrets.ACR_NAMESPACE }}/billing-service

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'

    - name: Cache Gradle packages
      uses: actions/cache@v4
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-

    - name: Run tests
      run: ./gradlew test

    - name: Upload test reports
      uses: actions/upload-artifact@v4
      if: always
      with:
        name: test-reports
        path: build/reports/

  build-and-push:
    needs: test
    runs-on: ubuntu-latest
    if: github.event_name == 'push' && (github.ref == 'refs/heads/main' || github.ref == 'refs/heads/develop')
    
    permissions:
      contents: read
      packages: write
      
    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'

    - name: Cache Gradle packages
      uses: actions/cache@v4
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-

    - name: Build with Gradle
      run: ./gradlew clean build -x test

    - name: Set up Docker Buildx
      uses: docker/setup-buildx-action@v3

    - name: Login to ACR
      uses: docker/login-action@v3
      with:
        registry: ${{ env.REGISTRY }}
        username: ${{ secrets.ACR_USERNAME }}
        password: ${{ secrets.ACR_PASSWORD }}

    - name: Extract metadata (tags, labels) for Docker
      id: meta
      uses: docker/metadata-action@v5
      with:
        images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
        tags: |
          type=sha,prefix={{date 'YYYYMMDD'}}-,format=long
          type=ref,event=branch
          type=ref=event=tag
          type=raw,value=latest,enable={{is_default_branch}}

    - name: Build and push Docker image
      uses: docker/build-push-action@v5
      with:
        context: .
        platforms: linux/amd64
        push: true
        tags: ${{ steps.meta.outputs.tags }}
        labels: ${{ steps.meta.outputs.labels }}
        cache-from: type=gha
        cache-to: type=gha,mode=max

    - name: Image digest
      run: echo ${{ steps.build-and-push.outputs.digest }}

  deploy-to-kubernetes:
    needs: build-and-push
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Login to ACR
      uses: docker/login-action@v3
      with:
        registry: ${{ env.REGISTRY }}
        username: ${{ secrets.ACR_USERNAME }}
        password: ${{ secrets.ACR_PASSWORD }}

    - name: Set up kubectl
      uses: azure/setup-kubectl@v4
      with:
        version: 'latest'

    - name: Set up Kubeconfig
      run: |
        mkdir -p ~/.kube
        echo "${{ secrets.KUBECONFIG_DATA }}" | base64 -d > ~/.kube/config
        chmod 600 ~/.kube/config

    - name: Deploy to Kubernetes
      run: |
        # Update deployment with new image tag
        kubectl set image deployment/billing-deployment billing-app=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.sha }} -n default
        kubectl rollout status deployment/billing-deployment -n default
        
    - name: Verify deployment
      run: |
        kubectl get pods -n default
        kubectl describe deployment billing-deployment -n default
```

### 6. Kubernetes Deployment Manifest

Ensure your Kubernetes cluster has the necessary deployment configuration:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: billing-deployment
  namespace: default
  labels:
    app: billing-app
spec:
  replicas: 2
  selector:
    matchLabels:
      app: billing-app
  template:
    metadata:
      labels:
        app: billing-app
    spec:
      containers:
      - name: billing-app
        image: registry.example.com/namespace/billing-service:latest  # Will be updated by pipeline
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "docker"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: billing-service
  namespace: default
spec:
  selector:
    app: billing-app
  ports:
    - protocol: TCP
      port: 80
      targetPort: 8080
  type: ClusterIP
```

## Pipeline Behavior

- **Pull Requests**: Only runs the test stage
- **Push to main**: Runs all stages (test, build, push, deploy)
- **Push to develop**: Runs test, build, and push stages (no deployment)

## Troubleshooting

### Common Issues

1. **Authentication Failures**: Verify that all GitHub secrets are correctly set
2. **Registry Connection**: Confirm that the ACR registry URL is correct
3. **Kubernetes Access**: Ensure the kubeconfig file has appropriate permissions

### Verifying Setup

1. Make a small change to the main branch to trigger the workflow
2. Monitor the Actions tab in GitHub for job progress
3. Verify the image appears in your ACR repository
4. Check that deployments are updated in your Kubernetes cluster

## Security Considerations

- Store all sensitive information in GitHub secrets, never in code
- Regularly rotate access keys
- Use minimal required permissions for service accounts
- Enable vulnerability scanning for container images

## Maintenance

- Monitor workflow runs regularly
- Update GitHub Actions versions periodically
- Review and update security configurations
- Clean up old images in ACR to save costs