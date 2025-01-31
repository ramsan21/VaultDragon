helm lint <chart-directory>

helm template <release-name> <chart-directory>

helm template my-release ./my-chart

helm install --dry-run --debug my-release ./my-chart

helm template <chart-directory> | kubectl apply --dry-run=client -f -

helm template <chart-directory> | yamllint -

helm plugin install https://github.com/helm-unittest/helm-unittest

helm unittest <chart-directory>
