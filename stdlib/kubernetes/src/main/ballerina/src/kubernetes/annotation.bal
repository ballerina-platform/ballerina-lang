// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

# Metadata for artifacts
#
# + name - Name of the resource. Default is `"<OUPUT_FILE_NAME>-<RESOURCE_TYPE>"`.
# + labels - Map of labels for the resource.
# + annotations - Map of annotations for resource.
public type Metadata record {|
    string name?;
    map<string> labels?;
    map<string> annotations?;
|};

# External file type for docker.
#
# + sourceFile - source path of the file (in your machine)
# + target - target path (inside container)
public type FileConfig record {|
    string sourceFile;
    string target;
|};

# Value for a field.
#
# + fieldPath - Path of the field
public type FieldValue record {|
    string fieldPath;
|};

# Value for a secret key.
#
# + name - Name of the secret.
# + key - Key of the secret.
public type SecretKeyValue record {|
    string name;
    string key;
|};

# Value for resource field.
#
# + containerName - Name of the container.
# + resource - Resource field
public type ResourceFieldValue record {|
    string containerName?;
    string 'resource;
|};

# Value for config map key.
#
# + name - name of the config.
# + key - key of the config.
public type ConfigMapKeyValue record {|
    string name;
    string key;
|};

# Value from field.
#
# + fieldRef - Reference for a field.
public type FieldRef record {|
    FieldValue fieldRef;
|};

# Value from secret key.
#
# + secretKeyRef - Reference for secret key.
public type SecretKeyRef record {|
    SecretKeyValue secretKeyRef;
|};

# Value from resource field.
#
# + resourceFieldRef - Reference for resource field.
public type ResourceFieldRef record {|
    ResourceFieldValue resourceFieldRef;
|};

# Value from config map key.
#
# + configMapKeyRef - Reference for config map key.
public type ConfigMapKeyRef record {|
    ConfigMapKeyValue configMapKeyRef;
|};

public const string IMAGE_PULL_POLICY_IF_NOT_PRESENT = "IfNotPresent";
public const string IMAGE_PULL_POLICY_ALWAYS = "Always";
public const string IMAGE_PULL_POLICY_NEVER = "Never";

# Image pull policy type field for kubernetes deployment and jobs.
public type ImagePullPolicy "IfNotPresent"|"Always"|"Never";

# Extend building of the docker image.
#
# + openshift - Openshift build config.
public type BuildExtension record {|
    OpenShiftBuildConfigConfiguration openshift?;
|};

# Probing configuration.
#
# + port - Port to check for tcp connection.
# + initialDelaySeconds - Initial delay for pobing in seconds.
# + periodSeconds - Interval between probes in seconds.
public type ProbeConfiguration record {|
    int port?;
    int initialDelaySeconds?;
    int periodSeconds?;
|};

# Type of operations between key and value of a toleration.
public type TolerationOperator "Exists"|"Equal";

# Types of toleration effects for pods.
public type TolerationEffect "NoSchedule"|"PreferNoSchedule"|"NoExecute";

# Pod toleration configuration.
#
# + key - Taint key of the toleration.
# + operator - Operator between the key and value. Default is `"Equal"`.
# + value - Taint value of the toleration.
# + effect - The taint effect
# + tolerationSeconds - Time period of toleration in seconds. Default is `0`.
public type PodTolerationConfiguration record {|
    string key;
    TolerationOperator operator = "Equal";
    string value;
    TolerationEffect effect?;
    int tolerationSeconds = 0;
|};

# Kubernetes deployment configuration.
#
# + dockerHost - Docker host IP and docker PORT. (e.g minikube IP and docker PORT).
# Default is to use DOCKER_HOST environment variable.
# If DOCKER_HOST is unavailable, use `"unix:///var/run/docker.sock"` for Unix or use `"npipe:////./pipe/docker_engine"` for Windows 10 or use `"localhost:2375"`.
# + dockerCertPath - Docker certificate path. Default is "DOCKER_CERT_PATH" environment variable.
# + registry - Docker registry url.
# + username - Username for docker registry.
# + password - Password for docker registry.
# + baseImage - Base image to create the docker image. Default value is `"openjdk:8-jre-alpine"`.
# + image - Docker image name with tag. Default is `"<OUTPUT_FILE_NAME>:latest"`. If field `registry` is set then it will be prepended to the docker image name as `<registry>/<OUTPUT_FILE_NAME>:latest`.
# + buildImage - Docker image to be build or not. Default is `true`.
# + push - Enable pushing docker image to registry. Field `buildImage` must be set to `true` to be effective. Default value is `false`.
# + cmd - Value for CMD for the generated Dockerfile. Default is `CMD java -jar ${APP} [--b7a.config.file=${CONFIG_FILE}] [--debug]`.
# + copyFiles - Array of [External files](kubernetes#FileConfig) for docker image.
# + singleYAML - Generate a single yaml file with all kubernetes artifacts (services, deployment, ingress and etc). Default is `true`.
# + namespace - Kubernetes namespace to be used on all artifacts.
# + replicas - Number of replicas. Default is `1`.
# + livenessProbe - Enable/Disable liveness probe and configure it. Default is `false`.
# + readinessProbe - Enable/Disable readiness probe and configure it. Default is `false`.
# + imagePullPolicy - Image pull policy. Default is `"IfNotPresent"`.
# + env - Environment variable map for containers.
# + podAnnotations - Map of annotations for pods.
# + podTolerations - Toleration for pods.
# + buildExtension - Docker image build extensions.
# + dependsOn - Services this deployment depends on.
# + imagePullSecrets - Image pull secrets.
public type DeploymentConfiguration record {|
    *Metadata;
    string dockerHost?;
    string dockerCertPath?;
    string registry?;
    string username?;
    string password?;
    string baseImage?;
    string image?;
    boolean buildImage = true;
    boolean push = false;
    string cmd?;
    FileConfig[] copyFiles?;
    boolean singleYAML = true;
    string namespace?;
    int replicas = 1;
    boolean|ProbeConfiguration livenessProbe = false;
    boolean|ProbeConfiguration readinessProbe = false;
    ImagePullPolicy imagePullPolicy = IMAGE_PULL_POLICY_IF_NOT_PRESENT;
    map<string|FieldRef|SecretKeyRef|ResourceFieldRef|ConfigMapKeyRef> env?;
    map<string> podAnnotations?;
    PodTolerationConfiguration[] podTolerations?;
    BuildExtension|string buildExtension?;
    string[] dependsOn?;
    string[] imagePullSecrets?;
|};

# @kubernetes:Deployment annotation to configure deplyoment yaml.
public const annotation DeploymentConfiguration Deployment on source service, source function, source listener;

public const string SESSION_AFFINITY_NONE = "None";
public const string SESSION_AFFINITY_CLIENT_IP = "ClientIP";

# Session affinity field for kubernetes services.
public type SessionAffinity "None"|"ClientIP";

public const string SERVICE_TYPE_NORD_PORT = "NodePort";
public const string SERVICE_TYPE_CLUSTER_IP = "ClusterIP";
public const string SERVICE_TYPE_LOAD_BALANCER = "LoadBalancer";

# Service type field for kubernetes services.
public type ServiceType "NodePort"|"ClusterIP"|"LoadBalancer";

# Kubernetes service configuration.
#
# + portName - Name for the port. Default value is the protocol of the listener.
# + port - Service port. Default is the Ballerina service port.
# + targetPort - Port of the pods. Default is the Ballerina service port.
# + sessionAffinity - Session affinity for pods. Default is `"None"`.
# + serviceType - Service type of the service. Default is `"ClusterIP"`.
public type ServiceConfiguration record {|
    *Metadata;
    string portName?;
    int port?;
    int targetPort?;
    SessionAffinity sessionAffinity = SESSION_AFFINITY_NONE;
    ServiceType serviceType = SERVICE_TYPE_CLUSTER_IP;
|};

# @kubernetes:Service annotation to configure service yaml.
public const annotation ServiceConfiguration Service on source listener, source service;

# Kubernetes ingress configuration.
#
# + hostname - Host name of the ingress. Default is `"<BALLERINA_SERVICE_NAME>.com"` or `"<BALLERINA_SERVICE_LISTENER_NAME>.com"`.
# + path - Resource path. Default is `"/"`.
# + targetPath - Target path for url rewrite.
# + ingressClass - Ingress class. Default is `"nginx"`.
# + enableTLS - Enable/Disable ingress TLS. Default is `false`.
public type IngressConfiguration record {|
    *Metadata;
    string hostname;
    string path = "/";
    string targetPath?;
    string ingressClass = "nginx";
    boolean enableTLS = false;
|};

# @kubernetes:Ingress annotation to configure ingress yaml.
public const annotation IngressConfiguration Ingress on source service, source listener;

# Kubernetes Horizontal Pod Autoscaler configuration
#
# + minReplicas - Minimum number of replicas.
# + maxReplicas - Maximum number of replicas.
# + cpuPercentage - CPU percentage to start scaling. Default is `50`.
public type PodAutoscalerConfig record {|
    *Metadata;
    int minReplicas?;
    int maxReplicas?;
    int cpuPercentage = 50;
|};

# @kubernetes:HPA annotation to configure horizontal pod autoscaler yaml.
public const annotation PodAutoscalerConfig HPA on source service, source function;

# Kubernetes secret volume mount.
#
# + mountPath - Mount path.
# + readOnly - Is mount read only. Default is `true`.
# + data - Paths to data files as an array.
public type Secret record {|
    *Metadata;
    string mountPath;
    boolean readOnly = true;
    string[] data;
|};

#Secret volume mount configurations for kubernetes.
#
# + secrets - Array of [Secret](kubernetes.html#Secret)
public type SecretMount record {|
    Secret[] secrets;
|};

# @kubernetes:Secret annotation to configure secrets.
public const annotation SecretMount Secret on source service, source function;

# Kubernetes Config Map volume mount.
#
# + mountPath - Mount path.
# + readOnly - Is mount read only. Default is `true`.
# + data - Paths to data files.
public type ConfigMap record {|
    *Metadata;
    string mountPath;
    boolean readOnly = true;
    string[] data;
|};

# Secret volume mount configurations for kubernetes.
#
# + conf - path to ballerina configuration file
# + configMaps - Array of [ConfigMap](kubernetes.html#ConfigMap)
public type ConfigMapMount record {|
    string conf;
    ConfigMap[] configMaps?;
|};

# @kubernetes:ConfigMap annotation to configure config maps.
public const annotation ConfigMapMount ConfigMap on source service, source function;

# Kubernetes Persistent Volume Claim.
#
# + mountPath - Mount Path.
# + accessMode - Access mode. Default is `"ReadWriteOnce"`.
# + volumeClaimSize - Size of the volume claim.
# + readOnly - Is mount read only.
public type PersistentVolumeClaimConfig record {|
    *Metadata;
    string mountPath;
    string accessMode = "ReadWriteOnce";
    string volumeClaimSize;
    boolean readOnly;
|};

# Persistent Volume Claims configurations for kubernetes.
#
# + volumeClaims - Array of [PersistentVolumeClaimConfig](kubernetes.html#PersistentVolumeClaimConfig)
public type PersistentVolumeClaims record {|
    PersistentVolumeClaimConfig[] volumeClaims;
|};

# @kubernetes:PersistentVolumeClaim annotation to configure Persistent Volume Claims.
public const annotation PersistentVolumeClaims PersistentVolumeClaim on source service, source function;

# Scopes for kubernetes resource quotas
public type ResourceQuotaScope "Terminating"|"NotTerminating"|"BestEffort"|"NotBestEffort";

# Kubernetes Resource Quota
#
# + hard - Quotas for the resources
# + scopes - Scopes of the quota
public type ResourceQuotaConfig record {|
    *Metadata;
    map<string> hard;
    ResourceQuotaScope?[] scopes = [];
|};

# Resource Quota configuration for kubernetes.
#
# + resourceQuotas - Array of [ResourceQuotaConfig](kubernetes.html#ResourceQuotaConfig)
public type ResourceQuotas record {|
    ResourceQuotaConfig[] resourceQuotas;
|};

# @kubernetes:ResourcesQuotas annotation to configure Resource Quotas.
public const annotation ResourceQuotas ResourceQuota on source service, source function;

public const string RESTART_POLICY_ON_FAILURE = "OnFailure";
public const string RESTART_POLICY_ALWAYS = "Always";
public const string RESTART_POLICY_NEVER = "Never";

# Restart policy type field for kubernetes jobs.
public type RestartPolicy "OnFailure"|"Always"|"Never";

# Kubernetes job configuration.
#
# + dockerHost - Docker host IP and docker PORT. (e.g minikube IP and docker PORT).
# Default is to use DOCKER_HOST environment variable.
# If DOCKER_HOST is unavailable, use `"unix:///var/run/docker.sock"` for Unix or use `"npipe:////./pipe/docker_engine"` for Windows 10 or use `"localhost:2375"`.
# + dockerCertPath - Docker certificate path. Default is "DOCKER_CERT_PATH" environment variable.
# + registry - Docker registry url.
# + username - Username for docker registry.
# + password - Password for docker registry.
# + baseImage - Base image to create the docker image. Default value is `"openjdk:8-jre-alpine"`.
# + image - Docker image name with tag. Default is `"<OUTPUT_FILE_NAME>:latest"`. If field `registry` is set then it will be prepended to the docker image name as `<registry>/<OUTPUT_FILE_NAME>:latest`.
# + buildImage - Docker image to be build or not. Default is `true`.
# + push - Enable pushing docker image to registry. Field `buildImage` must be set to `true`. Default value is `false`.
# + cmd - Value for CMD for the generated Dockerfile. Default is `CMD java -jar ${APP} [--b7a.config.file=${CONFIG_FILE}] [--debug]`.
# + copyFiles - Array of [External files](kubernetes#FileConfig) for docker image.
# + singleYAML - Generate a single yaml file with all kubernetes artifacts (ingress, configmaps, secrets and etc). Default is `true`.
# + namespace - Kubernetes namespace to be used on all artifacts.
# + imagePullPolicy - Image pull policy. Default is `"IfNotPresent"`.
# + env - Environment varialbes for container.
# + restartPolicy - Restart policy. Default is `"Never"`.
# + backoffLimit - Backoff limit.
# + activeDeadlineSeconds - Active deadline seconds. Default is `20`.
# + schedule - Schedule for cron jobs.
# + imagePullSecrets - Image pull secrets.
public type JobConfig record {|
    *Metadata;
    string dockerHost?;
    string dockerCertPath?;
    string registry?;
    string username?;
    string password?;
    string baseImage?;
    string image?;
    boolean buildImage = true;
    boolean push = false;
    string cmd?;
    FileConfig[] copyFiles?;
    boolean singleYAML = true;
    string namespace?;
    ImagePullPolicy imagePullPolicy = IMAGE_PULL_POLICY_IF_NOT_PRESENT;
    map<string|FieldRef|SecretKeyRef|ResourceFieldRef|ConfigMapKeyRef> env?;
    RestartPolicy restartPolicy = RESTART_POLICY_NEVER;
    string backoffLimit?;
    int activeDeadlineSeconds = 20;
    string schedule?;
    string[] imagePullSecrets?;
|};

# @kubernetes:Job annotation to configure kubernetes jobs.
public const annotation JobConfig Job on source function;


# Build Config configuration for OpenShift.
#
# + forcePullDockerImage - Set force pull images when building docker image.
# + buildDockerWithNoCache - Build docker image with no cache enabled.
public type OpenShiftBuildConfigConfiguration record {|
    boolean forcePullDockerImage = false;
    boolean buildDockerWithNoCache = false;
|};
