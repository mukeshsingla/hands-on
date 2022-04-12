# Links
	http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/#/login
		Token: eyJhbGciOiJSUzI1NiIsImtpZCI6IkdKVEFjckxtY3F1RFhRU19IS0t2M2h4UTZVU05TRUoyMGJRQnZ4V3U0WncifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJhZG1pbi11c2VyLXRva2VuLW5wd3d0Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQubmFtZSI6ImFkbWluLXVzZXIiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC51aWQiOiIwYmY1YWIyYS05NWJhLTQ0MGItODcxNC1lZTk4NzM0ZGE0NGQiLCJzdWIiOiJzeXN0ZW06c2VydmljZWFjY291bnQ6a3ViZS1zeXN0ZW06YWRtaW4tdXNlciJ9.aLwsNtegBA2AInJ3OYHjYlyzCIPaPjZYGxB8H12kmzPxLjV35n7DWJWGy_SMADQSKJZzC3kS0P2q6nBzSPtidfP56iLsY6m26dhrNadADyxjjKJ1WUOU0yhBQ2KbSusgyjiyN7WJs9HNRoKm78FE--yOYTEJqmevGDtlO1Mni4P__Ujzj8wS-lhffDlK06BSVuuGjdJO9PjAgIrm805IuT4NlglbnWbEDFEJIwru-XE7y14eA1kjsPiUiR2YDxRxI9LepfAVrH6NifxlMaIw7ZK04xQb20c-n72sY58lCWRYVHHFeiurzPlJ-OhzIde7YlgDrjHlDpQrqs_z-Opmyg


# Kubernetes hands-on

kubectl config view --raw >~/.kube/config

## Setting microk8s for user
https://microk8s.io/docs/
sudo usermod -a -G microk8s msingla
sudo chown -f -R msingla ~/.kube
su - msingla
alias kubectl='microk8s kubectl'
microk8s status --wait-ready
microk8s start --wait-ready

https://thenewstack.io/deploy-a-single-node-kubernetes-instance-in-seconds-with-microk8s/

### Default Kubernetes exposed API via REST
https://192.168.1.152:16443

### Access Kubernetes Dashboard
https://192.168.1.152:32414/

### Get password for admin to access API via REST
kubectl config view

### Exposing dashboard externally
kubectl -n kube-system edit service kubernetes-dashboard
	spec:
	  clusterIP: 10.152.183.91
	  ports:
	  - port: 443		# add nodePort: 32414
		protocol: TCP
		targetPort: 8443
	  selector:
		k8s-app: kubernetes-dashboard
	  sessionAffinity: None
	  type: ClusterIP	# type: NodePort instead of ClusterIP

### Get token for logging in dashboard
kubectl -n kube-system describe $(kubectl -n kube-system get secret -n kube-system -o name | grep namespace) | grep token

### helm
microk8s.enable helm helm3

## kubectl auto completion
kubectl completion -h
source <(kubectl completion bash)
kubectl completion bash > ~/.kube/completion.bash.inc
vi .bash_profile
	### Add following at the EOF
	# .bash_profile

	# Get the aliases and functinos
	if [ -f ~/.bashrc ]; then
			. ~/.bashrc
	fi

	source .kube/completion.bash.inc
	# User specific environment and startup programs

## Using kubectl
kubectl create deployment my-dep --image=busybox
kubectl create -f <yamlfilename>
kubectl delete -f <yamlfilename>
kubectl delete pod <podName>
kubectl get <all|pod|deployment> -o yaml > <nameForYamlFile>
kubectl get <all|pod|deployment> --show-labels
kubectl get <all|pod|deployment> --selector app=<label>
kubectl get pods --namespace=dev-env
kubectl explain
kubectl api-resources
kubectl api-versions

kubectl describe pod busybox-multi --namespace=dev-env

## kubectx
git clone https://github.com/ahmetb/kubectx

kubectl label pods cmd-nginx-66b6dc78d-8tt5f app-	# Removes label from pod but creates new

## Searching based on labels
kubectl get all --selector app=cmd-nginx

kubectl edit deployments cmd-nginx
kubectl scale --replicas=3 deployment cmd-nginx

# After updating image details of the deployment
kubectl rollout history deployment

kubectl rollout undo deployment <deploymentName> --to-revision=<revisionNumber>

kubectl exec -it busybox-multi -c busy --namespace=dev-env -- /bin/sh


# Troubleshooting
## pending Pods
kubectl describe pod <podName>

## swap is enabled on the node
swapoff -ased -i '/ swap / s/^/#/' /etc/fstab


# References
	https://kubernetes.io/docs/reference/kubectl/cheatsheet/

	https://www.devdiaries.net/blog/Single-Node-Kubernetes-Cluster-Part-3/


~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# HELM

## Install HELM
curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3
chmod 700 get_helm.sh
./get_helm.sh

helm repo add equinor-charts https://equinor.github.io/helm-charts/charts/
helm repo update
helm --install neo4j-community equinor-charts/neo4j-community --set acceptLicenseAgreement=yes --set neo4jPassword=neo4j101

kubectl rollout status --namespace default StatefulSet/neo4j-community-neo4j-community --watch 

kubectl logs -l "app=neo4j-community"

