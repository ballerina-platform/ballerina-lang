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
# Knative annotation configuration.
#
#
#
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
public type FieldValue record{|
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
# Value for config map key.
#
# + name - name of the config.
# + key - key of the config.
public type ConfigMapKeyValue record {|
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
# Value from config map key.
#
# + configMapKeyRef - Reference for config map key.
public type ConfigMapKeyRef record {|
    ConfigMapKeyValue configMapKeyRef;

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
public const string IMAGE_PULL_POLICY_IF_NOT_PRESENT = "IfNotPresent";
public const string IMAGE_PULL_POLICY_ALWAYS = "Always";
public const string IMAGE_PULL_POLICY_NEVER = "Never";
# Image pull policy type field for kubernetes deployment and jobs.
public type ImagePullPolicy "IfNotPresent"|"Always"|"Never";
# Probing configuration.
#
# + port - Port to check for tcp connection.
# + initialDelayInSeconds - Initial delay for pobing in seconds.
# + periodSeconds - Interval between probes in seconds.
public type ProbeConfiguration record {|
    int port?;
    int initialDelayInSeconds?;
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
# Knative service configuration.
#
# + dockerHost - Docker host IP and docker PORT. ( e.g minikube IP and docker PORT).
# Default is to use DOCKER_HOST environment variable.
# If DOCKER_HOST is unavailable, use `"unix:///var/run/docker.sock"` for Unix or use `"npipe:////./pipe/docker_engine"` for Windows 10 or use `"localhost:2375"`.
# + dockerCertPath - Docker certificate path. Default is "DOCKER_CERT_PATH" environment variable.
# + registry - Docker registry url.
# + username - Username for docker registry.
# + password - Password for docker registry.
# + baseImage - Base image for docker image building. Default value is `"ballerina/ballerina-runtime:<BALLERINA_VERSION>"`.
# Use `"ballerina/ballerina-runtime:latest"` to use the latest stable ballerina runtime docker image.
# + image - Docker image name with tag. Default is `"<OUTPUT_FILE_NAME>:latest"`. If field `registry` is set then it will be prepended to the docker image name as `<registry>/<OUTPUT_FILE_NAME>:latest`.
# + buildImage - Docker image to be build or not. Default is `true`.
# + push - Enable pushing docker image to registry. Field `buildImage` must be set to `true` to be effective. Default value is `false`.
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
# + dependsOn - Services this deployment depends on.
# + imagePullSecrets - Image pull secrets.
# + containerConcurrency - concurent request handle by one container instance
# + timeoutSeconds - max time the instance is allowed for responding to a request
# + port - containerPort value for Knative service
public type ServiceConfiguration record{|
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
        string[] dependsOn?;
        string[] imagePullSecrets?;
        int containerConcurrency=100;
        int timeoutSeconds=60;
        int port=8080;

|};
public const annotation ServiceConfiguration Service on source service, source function, source listener;
# Knative Horizontal Pod Autoscaler configuration
#
# + minReplicas - Minimum number of replicas.
# + maxReplicas - Maximum number of replicas.
public type PodAutoscalerConfig record {|
    *Metadata;
    int minReplicas?;
    int maxReplicas?;

|};
# @knative:HPA annotation to configure horizontal pod autoscaler yaml.
public const annotation PodAutoscalerConfig HPA on source service, source function;
# Knative secret volume mount.
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
#Secret volume mount configurations for knative.
#
# + secrets - Array of [Secret](knative.html#Secret)
public type SecretMount record {|
    Secret[] secrets;

|};
# @knative:Secret annotation to configure secrets.
public const annotation SecretMount Secret on source service, source function;
# Knative Config Map volume mount.
#
# + mountPath - Mount path.
# + readOnly - Is mount read only. Default is `true`.
# + data - Paths to data files.+ port - port value for the containerPort
public type ConfigMap record {|
    *Metadata;
    string mountPath;
    boolean readOnly = true;
    string[] data;

|};
# Secret volume mount configurations for knative.
#
# + conf - path to ballerina configuration file
# + configMaps - Array of [ConfigMap](kubernetes.html#ConfigMap)
public type ConfigMapMount record {|
    string conf;
    ConfigMap[] configMaps?;

|};
# @knative:ConfigMap annotation to configure config maps.
public const annotation ConfigMapMount ConfigMap on source service, source function;