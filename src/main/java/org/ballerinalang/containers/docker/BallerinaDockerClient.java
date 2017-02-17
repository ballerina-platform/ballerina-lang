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

package org.ballerinalang.containers.docker;

import org.ballerinalang.containers.docker.exception.BallerinaDockerClientException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Provide support for Docker image creation and Container manipulation for Ballerina.
 */
public interface BallerinaDockerClient {

    /**
     * Create a Ballerina Service Docker image from a Ballerina Service Package.
     *
     * @param packageName
     * @param dockerEnv
     * @param bPackagePaths
     * @param imageName
     * @param imageVersion
     * @return
     * @throws BallerinaDockerClientException
     * @throws IOException
     * @throws InterruptedException
     */
    public String createServiceImage(String packageName, String dockerEnv, List<Path> bPackagePaths,
                                     String imageName, String imageVersion)
            throws BallerinaDockerClientException, IOException, InterruptedException;

    /**
     * Create a Ballerina Service Docker image from a Ballerina configuration.
     *
     * @param serviceName
     * @param dockerEnv
     * @param ballerinaConfig
     * @param imageName
     * @param imageVersion
     * @return
     * @throws InterruptedException
     * @throws BallerinaDockerClientException
     * @throws IOException
     */
    public String createServiceImage(String serviceName, String dockerEnv, String ballerinaConfig,
                                     String imageName, String imageVersion)
            throws InterruptedException, BallerinaDockerClientException, IOException;

    /**
     * Create a Ballerina Main Docker image from a Ballerina Main Package.
     *
     * @param packageName
     * @param dockerEnv
     * @param bPackagePaths
     * @param imageName
     * @param imageVersion
     * @return
     * @throws BallerinaDockerClientException
     * @throws IOException
     * @throws InterruptedException
     */
    public String createMainImage(String packageName, String dockerEnv, List<Path> bPackagePaths,
                                  String imageName, String imageVersion)
            throws BallerinaDockerClientException, IOException, InterruptedException;

    /**
     * Create a Ballerina Main Docker image from a Ballerina configuration.
     *
     * @param mainPackageName
     * @param dockerEnv
     * @param ballerinaConfig
     * @param imageName
     * @param imageVersion
     * @return
     * @throws InterruptedException
     * @throws BallerinaDockerClientException
     * @throws IOException
     */
    public String createMainImage(String mainPackageName, String dockerEnv, String ballerinaConfig,
                                  String imageName, String imageVersion)
            throws InterruptedException, BallerinaDockerClientException, IOException;

    /**
     * Delete the Docker image of a created Ballerina package.
     *
     * @param packageName
     * @param dockerEnv
     * @param imageName
     * @param version
     * @return
     * @throws BallerinaDockerClientException
     */
    public boolean deleteImage(String packageName, String dockerEnv, String imageName, String version)
            throws BallerinaDockerClientException;

    /**
     * Retrieve Docker image name of a created Ballerina package.
     *
     * @param packageName
     * @param dockerEnv
     * @return
     */
    public String getImage(String packageName, String dockerEnv);

//    public String runMainContainer(String dockerEnv, String serviceName)
//            throws InterruptedException, IOException, BallerinaDockerClientException;
//
//    public String runServiceContainer(String packageName, String dockerEnv) throws BallerinaDockerClientException;
//
//    public void stopContainer(String packageName, String dockerEnv) throws BallerinaDockerClientException;
}
