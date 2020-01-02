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

# Docker annotation configuration.
#
# + registry - Docker registry url.
# + name - Name of the docker image. Default value is the file name of the executable .jar file.
# + tag - Docker image tag. Default value is `"latest"`.
# + username - Username for docker registry.
# + password - Password for docker registry.
# + baseImage - Base image to create the docker image. Default value is `"openjdk:8-jre-alpine"`.
# + buildImage - Enable building docker image. Default value is `true`.
# + push - Enable pushing docker image to registry. Field `buildImage` must be set to `true` to be effective. Default value is `false`.
# + cmd - Value for CMD for the generated Dockerfile. Default is `CMD java -jar ${APP} [--b7a.config.file=${CONFIG_FILE}] [--debug]`.
# + enableDebug - Enable ballerina debug. Default is `false`.
# + debugPort - Ballerina remote debug port. Default is `5005`.
# + dockerAPIVersion - Docker API version.
# + dockerHost - Docker host IP and docker PORT. (e.g minikube IP and docker PORT).
# Default is to use DOCKER_HOST environment variable.
# If DOCKER_HOST is unavailable, use `"unix:///var/run/docker.sock"` for Unix or use `"npipe:////./pipe/docker_engine"` for Windows 10 or use `"localhost:2375"`.
# + dockerCertPath - Docker certificate path. Default is to use `"DOCKER_CERT_PATH"` environment variable.
public type DockerConfiguration record {|
    string registry?;
    string name?;
    string tag?;
    string username?;
    string password?;
    string baseImage?;
    boolean buildImage = true;
    boolean push = false;
    string cmd?;
    boolean enableDebug = false;
    int debugPort = 5005;
    string dockerAPIVersion?;
    string dockerHost?;
    string dockerCertPath?;
|};

# @docker:Config annotation to configure docker artifact generation.
public const annotation DockerConfiguration Config on source service, source listener;

# External file type for docker.
#
# + sourceFile - Source path of the file (in your machine).
# + target - Target path (inside container).
# + isBallerinaConf - Flag to specify ballerina config file. When true, the config is passed as a command argument to the Dockerfile CMD.
public type FileConfig record {|
    string sourceFile;
    string target;
    boolean isBallerinaConf = false;
|};

# External File configurations for docker.
#
# + files - Array of [FileConfig](docker.html#FileConfig)
public type FileConfigs record {|
    FileConfig[] files;
|};

# @docker:CopyFile annotation to copy external files to docker image.
public const annotation FileConfigs CopyFiles on source service, source listener;

# Expose ports for docker.
public type ExposeConfig record {| |};

# @docker:Expose annotation to expose ballerina ports.
public const annotation ExposeConfig Expose on source listener;
