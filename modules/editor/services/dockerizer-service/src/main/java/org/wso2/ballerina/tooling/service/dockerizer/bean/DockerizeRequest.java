/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.wso2.ballerina.tooling.service.dockerizer.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Dockerize Request bean from the UI
 */
public class DockerizeRequest {

    @SerializedName("service-name")
    private String serviceName;


    @SerializedName("config")
    private String ballerinaConfig;

    @SerializedName("image-name")
    private String imageName;

    @SerializedName("docker-env")
    private String dockerEnv;

    public String getDockerEnv() {
        return dockerEnv;
    }

    public void setDockerEnv(String dockerEnv) {
        this.dockerEnv = dockerEnv;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getBallerinaConfig() {
        return ballerinaConfig;
    }

    public void setBallerinaConfig(String ballerinaConfig) {
        this.ballerinaConfig = ballerinaConfig;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
