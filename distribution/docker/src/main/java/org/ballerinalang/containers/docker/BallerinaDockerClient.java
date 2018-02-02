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
     * @param packageName   The name used to identify the package.
     * @param dockerEnv     The docker host URL.
     * @param bPackagePaths The paths to packages to be packed to the image.
     * @param imageName     The docker image name to use.
     * @param imageVersion  The docker image version to use.
     * @return The {@link String} name of the docker image created. Null if the image building process failed.
     * @throws BallerinaDockerClientException If the input parameters are invalid.
     * @throws IOException                    If specified files cannot be accessed.
     * @throws InterruptedException           If the docker image build process was interrupted.
     *
     * @deprecated use {@link #createMainImage(String, String, Path, String, String)} instead.
     */
    @Deprecated
    String createServiceImage(String packageName, String dockerEnv, List<Path> bPackagePaths,
                              String imageName, String imageVersion)
            throws BallerinaDockerClientException, IOException, InterruptedException;

    /**
     * Create a Ballerina Service Docker image from a Ballerina configuration.
     *
     * @param serviceName     The name of the Service being packaged.
     * @param dockerEnv       The docker host URL.
     * @param ballerinaConfig The content of the Ballerina Service to be packaged.
     * @param imageName       The docker image name to use.
     * @param imageVersion    The docker image version to use.
     * @return The {@link String} name of the docker image created. Null if the image building process failed.
     * @throws InterruptedException           If the docker image build process was interrupted.
     * @throws BallerinaDockerClientException If the input parameters are invalid.
     * @throws IOException                    If specified files cannot be accessed.
     */
    String createServiceImage(String serviceName, String dockerEnv, String ballerinaConfig,
                              String imageName, String imageVersion)
            throws InterruptedException, BallerinaDockerClientException, IOException;

    /**
     * Create a Ballerina Main Docker image from a Ballerina Main Package.
     *
     * @param packageName   The name used to identify the package.
     * @param dockerEnv     The docker host URL.
     * @param bPackagePaths The paths to packages to be packed to the image.
     * @param imageName     The docker image name to use.
     * @param imageVersion  The docker image version to use.
     * @return The {@link String} name of the docker image created. Null if the image building process failed.
     * @throws BallerinaDockerClientException If the input parameters are invalid.
     * @throws IOException                    If specified files cannot be accessed.
     * @throws InterruptedException           If the docker image build process was interrupted.
     *
     * @deprecated use {@link #createMainImage(String, String, Path, String, String)} instead.
     */
    @Deprecated
    String createMainImage(String packageName, String dockerEnv, List<Path> bPackagePaths,
                           String imageName, String imageVersion)
            throws BallerinaDockerClientException, IOException, InterruptedException;

    /**
     * Create a Ballerina Main Docker image from a Ballerina BAL / BALX File.
     *
     * @param packageName  The name used to identify the package.
     * @param dockerEnv    The docker host URL.
     * @param bPackagePath The paths to package to be packed to the image.
     * @param imageName    The docker image name to use.
     * @param imageVersion The docker image version to use.
     * @return The {@link String} name of the docker image created. Null if the image building process failed.
     * @throws BallerinaDockerClientException If the input parameters are invalid.
     * @throws IOException                    If specified files cannot be accessed.
     * @throws InterruptedException           If the docker image build process was interrupted.
     */
    String createMainImage(String packageName, String dockerEnv, Path bPackagePath,
                           String imageName, String imageVersion)
            throws BallerinaDockerClientException, IOException, InterruptedException;

    /**
     * Create a Ballerina Main Docker image from a Ballerina configuration.
     *
     * @param mainPackageName The name used to identify the main function to be packed.
     * @param dockerEnv       The docker host URL.
     * @param ballerinaConfig The content of the Ballerina Service to be packaged.
     * @param imageName       The docker image name to use.
     * @param imageVersion    The docker image version to use.
     * @return The {@link String} name of the docker image created. Null if the image building process failed.
     * @throws InterruptedException           If the docker image build process was interrupted.
     * @throws BallerinaDockerClientException If the input parameters are invalid.
     * @throws IOException                    If specified files cannot be accessed.
     */
    String createMainImage(String mainPackageName, String dockerEnv, String ballerinaConfig,
                           String imageName, String imageVersion)
            throws InterruptedException, BallerinaDockerClientException, IOException;

    /**
     * Delete the Docker image of a created Ballerina package.
     *
     * @param packageName  The name used to identify the Ballerina program which was packed.
     * @param dockerEnv    The docker host URL.
     * @param imageName    The docker image name to use.
     * @param imageVersion The docker image version to use.
     * @return True if the image was successfully deleted, false otherwise.
     * @throws BallerinaDockerClientException If the input parameters are invalid.
     */
    boolean deleteImage(String packageName, String dockerEnv, String imageName, String imageVersion)
            throws BallerinaDockerClientException;

    /**
     * Retrieve Docker image name of a created Ballerina package.
     *
     * @param packageName The name used to identify the Ballerina program which was packed.
     * @param dockerEnv   The docker host URL.
     * @return The {@link String} name of the docker image
     */
    String getImage(String packageName, String dockerEnv);


    /**
     * Retrieve Docker image build error.
     *
     * @return The {@link String} docker image build error thrown
     */
    String getBuildError();

//    public String runMainContainer(String dockerEnv, String serviceName)
//            throws InterruptedException, IOException, BallerinaDockerClientException;
//
//    public String runServiceContainer(String packageName, String dockerEnv) throws BallerinaDockerClientException;
//
//    public void stopContainer(String packageName, String dockerEnv) throws BallerinaDockerClientException;
}
