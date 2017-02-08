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

package org.wso2.ballerina.containers;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.ballerina.containers.docker.BallerinaDockerClient;
import org.wso2.ballerina.containers.docker.exception.BallerinaDockerClientException;
import org.wso2.ballerina.containers.docker.impl.DefaultBallerinaDockerClient;
import org.wso2.ballerina.containers.docker.utils.TestUtils;
import org.wso2.ballerina.containers.docker.utils.Utils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests to cover scenarios related to package based Docker image manipulation.
 */
public class DefaultBallerinaDockerClientPackageBasedTest {

    private BallerinaDockerClient dockerClient;
    private List<String> createdImages = new ArrayList<>();

    @BeforeMethod
    public void setUp() {
        this.dockerClient = new DefaultBallerinaDockerClient();
    }

    @Test
    public void testSuccessfulCreateServiceImage()
            throws IOException, InterruptedException, BallerinaDockerClientException {

        String serviceName = "TestService1";
        String imageName = serviceName.toLowerCase();
        Path ballerinaPackage = Paths.get(Utils.getResourceFile("ballerina/TestService.bal").getPath());

        String result = dockerClient.createServiceImage(serviceName, null, ballerinaPackage, null, null);
        createdImages.add(imageName);

        Assert.assertTrue((result != null) && (result.equals(imageName)), "Docker image creation failed.");
    }

    @Test
    public void testSuccessfulCreateFunctionImage()
            throws IOException, InterruptedException, BallerinaDockerClientException {

        String serviceName = "TestFunction1";
        String imageName = serviceName.toLowerCase();
        Path ballerinaPackage = Paths.get(Utils.getResourceFile("ballerina/TestFunction.bal").getPath());

        String result = dockerClient.createMainImage(serviceName, null, ballerinaPackage, null, null);
        createdImages.add(imageName);

        Assert.assertTrue((result != null) && (result.equals(imageName)), "Docker image creation failed.");
    }

    @AfterMethod
    public void tearDown() {
        for (String imageName : createdImages) {
            TestUtils.deleteDockerImage(imageName);
        }

        createdImages = new ArrayList<>();
    }
}
