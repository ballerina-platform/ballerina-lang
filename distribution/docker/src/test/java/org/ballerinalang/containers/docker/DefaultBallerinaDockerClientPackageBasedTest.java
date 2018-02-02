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

import org.ballerinalang.containers.Constants;
import org.ballerinalang.containers.docker.exception.BallerinaDockerClientException;
import org.ballerinalang.containers.docker.impl.DefaultBallerinaDockerClient;
import org.ballerinalang.containers.docker.utils.TestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
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
        List<Path> packagePaths = TestUtils.getTestServiceAsPathList();

        String result = dockerClient.createServiceImage(serviceName, null, packagePaths, null, null);
        createdImages.add(imageName);

        Assert.assertTrue((result != null) && (result.equals(imageName
                + ":" + Constants.IMAGE_VERSION_LATEST)), "Docker image creation failed.");
    }

    @Test
    public void testSuccessfulCreateFunctionImage()
            throws IOException, InterruptedException, BallerinaDockerClientException {

        String serviceName = "TestFunction1";
        String imageName = serviceName.toLowerCase();
        List<Path> packagePaths = TestUtils.getTestFunctionAsPathList();

        String result = dockerClient.createMainImage(serviceName, null, packagePaths, null, null);
        createdImages.add(imageName);

        Assert.assertTrue((result != null) && (result.equals(imageName
                + ":" + Constants.IMAGE_VERSION_LATEST)), "Docker image creation failed.");
    }

    @Test(expectedExceptions = {BallerinaDockerClientException.class})
    public void testFailedImageCreationWrongArchiveForMain()
            throws IOException, InterruptedException, BallerinaDockerClientException {
        String serviceName = "TestService1";
        List<Path> packagePaths = TestUtils.getTestServiceAsPathList();
        dockerClient.createMainImage(serviceName, null, packagePaths, null, null);
    }

    @Test(expectedExceptions = {BallerinaDockerClientException.class})
    public void testFailedImageCreationWrongArchiveForService()
            throws IOException, InterruptedException, BallerinaDockerClientException {
        String serviceName = "TestFunction1";
        List<Path> packagePaths = TestUtils.getTestFunctionAsPathList();
        dockerClient.createServiceImage(serviceName, null, packagePaths, null, null);
    }

    @AfterMethod
    public void tearDown() {
        for (String imageName : createdImages) {
            TestUtils.deleteDockerImage(imageName);
        }

        createdImages = new ArrayList<>();
    }
}
