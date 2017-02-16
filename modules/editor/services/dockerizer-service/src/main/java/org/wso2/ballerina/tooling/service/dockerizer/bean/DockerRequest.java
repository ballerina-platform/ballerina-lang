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
 * Dockerize Request bean from the UI.
 */
public class DockerRequest {

    @SerializedName("type")
    private String type;

    @SerializedName("packagePath")
    private String packagePath;

    @SerializedName("docker-env")
    private String dockerEnv;

    @SerializedName("image-name")
    private String imageName;

    @SerializedName("image-version")
    private String imageVersion;

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageVersion() {
        return imageVersion;
    }

    public void setImageVersion(String imageVersion) {
        this.imageVersion = imageVersion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDockerEnv() {
        return dockerEnv;
    }

    public void setDockerEnv(String dockerEnv) {
        this.dockerEnv = dockerEnv;
    }

    public String getPackagePath() {
        return packagePath;
    }

    public void setPackagePath(String packagePath) {
        this.packagePath = packagePath;
    }
}
