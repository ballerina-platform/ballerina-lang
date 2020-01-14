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
# + registry - Docker registry URL.
# + name - Name of the Docker image. Default value is the file name of the executable .jar file.
# + tag - Docker image tag. Default value is `"latest"`.
# + username - Username for the Docker registry.
# + password - Password for the Docker registry.
# + baseImage - Base image to create the Docker image. Default value is `"openjdk:8-jre-alpine"`.
# + buildImage - Enable building the Docker image. Default value is `true`.
# + push - Enable pushing the Docker image to registry. The value of the `buildImage` field must be set to `true` to be effective. Default value is `false`.
# + cmd - CMD value for the generated Docker file. Default is `CMD java -jar ${APP} [--b7a.config.file=${CONFIG_FILE}] [--debug]`.
# + enableDebug - Enable Ballerina debugging. Default is `false`.
# + debugPort - Ballerina remote debugging port. Default is `5005`.
# + dockerAPIVersion - Docker API version.
# + dockerHost - Docker host IP and Docker port (e.g,. Minikube IP and Docker port).
# Default is to use the `DOCKER_HOST` environment variable.
# If `DOCKER_HOST` is unavailable, use `"unix:///var/run/docker.sock"` for Unix, use `"npipe:////./pipe/docker_engine"` for Windows 10, or use `"localhost:2375"`.
# + dockerCertPath - Docker certificate path. Default is to use the `"DOCKER_CERT_PATH"` environment variable.
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

# @docker:Config annotation to configure Docker artifact generation.
public const annotation DockerConfiguration Config on source service, source listener;

# External file type for Docker.
#
# + sourceFile - Source path of the file (in your machine).
# + target - Target path (inside the container).
# + isBallerinaConf - Flag to specify the Ballerina config file. When true, the config is passed as a command argument to the Dockerfile CMD.
public type FileConfig record {|
    string sourceFile;
    string target;
    boolean isBallerinaConf = false;
|};

# External file configurations for Docker.
#
# + files - Array of [FileConfig](docker.html#FileConfig)
public type FileConfigs record {|
    FileConfig[] files;
|};

# @docker:CopyFile annotation to copy external files to the Docker image.
public const annotation FileConfigs CopyFiles on source service, source listener;

# Expose ports for Docker.
public type ExposeConfig record {| |};

# @docker:Expose annotation to expose Ballerina ports.
public const annotation ExposeConfig Expose on source listener;
