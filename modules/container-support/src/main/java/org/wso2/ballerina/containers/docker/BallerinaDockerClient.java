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

package org.wso2.ballerina.containers.docker;

import org.wso2.ballerina.containers.docker.exception.DockerHandlerException;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Provide support for Docker image creation and Container manipulation for Ballerina.
 */
public interface BallerinaDockerClient {

    public String createServiceImage(String packageName, String dockerEnv, Path bPackagePath)
            throws DockerHandlerException, IOException, InterruptedException;

    public String createFunctionImage(String packageName, String dockerEnv, Path bPackagePath)
            throws DockerHandlerException, IOException, InterruptedException;

    public boolean deleteImage(String packageName, String dockerEnv);

    public String getImage(String packageName, String dockerEnv);

    public String runFunctionContainer(String dockerEnv, String serviceName)
            throws InterruptedException, IOException, DockerHandlerException;

    public String runServiceContainer(String packageName, String dockerEnv) throws DockerHandlerException;

    public void stopContainer(String packageName, String dockerEnv) throws DockerHandlerException;
}
