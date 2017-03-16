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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests to cover scenarios related to generic Docker image manipulation.
 */
public class DefaultBallerinaDockerClientGenericTest {

    private BallerinaDockerClient dockerClient;
    private List<String> createdImages = new ArrayList<>();

    @BeforeMethod
    public void setUp() {
        this.dockerClient = new DefaultBallerinaDockerClient();
        System.setProperty("ballerina.version", "latest");
    }

    @Test
    public void testSuccessfulDeleteImage() throws IOException, InterruptedException, BallerinaDockerClientException {
        String serviceName = "TestFunction2";
        String imageName = serviceName.toLowerCase();
        String ballerinaConfig = TestUtils.getTestFunctionAsString();

        String result = dockerClient.createMainImage(serviceName, null, ballerinaConfig, null, null);
        Assert.assertTrue(
                (result != null) && (result.equals(imageName + ":" + Constants.IMAGE_VERSION_LATEST)),
                "Docker image creation failed.");

        boolean deleteResult = dockerClient.deleteImage(imageName, null, null, null);
        Assert.assertTrue(deleteResult, "Docker image deletion failed.");
    }

    @Test
    public void testSuccessfulImageExists() throws IOException, InterruptedException, BallerinaDockerClientException {
        String serviceName = "TestFunction3";
        String imageName = serviceName.toLowerCase();
        String ballerinaConfig = TestUtils.getTestFunctionAsString();

        String result = dockerClient.createMainImage(serviceName, null, ballerinaConfig, null, null);
        Assert.assertTrue(
                (result != null) && (result.equals(imageName + ":" + Constants.IMAGE_VERSION_LATEST)),
                "Docker image creation failed.");
        createdImages.add(imageName);

        result = dockerClient.getImage(imageName, null);
        Assert.assertTrue(
                (result != null) && (result.equals(imageName + ":" + Constants.IMAGE_VERSION_LATEST)),
                "Couldn't find existing image");
    }

    @Test
    public void testFailedDeleteImage() throws IOException, InterruptedException, BallerinaDockerClientException {
        String nonExistentImageName = "nonexistentimage1";
        boolean result = dockerClient.deleteImage(nonExistentImageName, null, null, null);
        Assert.assertFalse(result, "Docker image deletion.");
    }

    @Test
    public void testFailImageExists() throws IOException, InterruptedException {
        String imageName = "nonexistentimage2";
        String result = dockerClient.getImage(imageName, null);
        Assert.assertTrue(result == null, "Docker image find");
    }

    @Test(expectedExceptions = {BallerinaDockerClientException.class})
    public void testFailedImageCreationNullPackageName()
            throws IOException, InterruptedException, BallerinaDockerClientException {
        List<Path> packagePaths = TestUtils.getTestServiceAsPathList();

        dockerClient.createServiceImage(null, null, packagePaths, null, null);
    }

    @Test(expectedExceptions = {BallerinaDockerClientException.class})
    public void testFailedImageCreationNullPackagePaths()
            throws IOException, InterruptedException, BallerinaDockerClientException {
        String serviceName = "TestService1";
        dockerClient.createServiceImage(serviceName, null, new ArrayList<>(), null, null);
    }

    @Test(expectedExceptions = {BallerinaDockerClientException.class})
    public void testFailedImageCreationNonExistentPackagePaths()
            throws IOException, InterruptedException, BallerinaDockerClientException {
        String serviceName = "TestService1";
        List<Path> packagePaths = new ArrayList<>();
        packagePaths.add(Paths.get("/non/existent/path/package.bsz"));
        dockerClient.createServiceImage(serviceName, null, packagePaths, null, null);
    }

    @Test(expectedExceptions = {BallerinaDockerClientException.class})
    public void testFailedImageCreationNullVersionWithImageName()
            throws IOException, InterruptedException, BallerinaDockerClientException {
        String serviceName = "TestService1";
        List<Path> packagePaths = TestUtils.getTestServiceAsPathList();
        dockerClient.createServiceImage(serviceName, null, packagePaths, "customImage", null);
    }

    @Test
    public void testSuccessfulImageCreateWithCustomImageName()
            throws IOException, InterruptedException, BallerinaDockerClientException {
        String serviceName = "TestService1";
        String imageName = "customimagename";
        String imageVersion = "0.0.1";
        List<Path> packagePaths = TestUtils.getTestServiceAsPathList();

        String result = dockerClient.createServiceImage(serviceName, null, packagePaths, imageName, imageVersion);
        createdImages.add(imageName + ":" + imageVersion);

        Assert.assertTrue((result != null) && (result.equals(imageName
                + ":" + imageVersion)), "Docker image creation failed.");
    }

    //    @Test
//    public void testSuccesfulMainRun() throws IOException, InterruptedException, BallerinaDockerClientException {
//        String serviceName = "TestFunction4";
//        String imageName = serviceName.toLowerCase();
//        Path ballerinaPackage = Paths.get(Utils.getResourceFile("ballerina/TestFunction.bal").getPath());
//
//        String result = dockerClient.createMainImage(serviceName, null, ballerinaPackage);
//        Assert.assertTrue("Docker image creation failed.", (result != null) && (result.equals(imageName)));
//        String output = dockerClient.runMainContainer(null, serviceName);
//        deleteDockerImage(imageName);
//        Assert.assertTrue("Running Ballerina function in Docker failed.", "Hello, World!".equals(output));
//    }

    @AfterMethod
    public void tearDown() {
        for (String imageName : createdImages) {
            TestUtils.deleteDockerImage(imageName);
        }

        createdImages = new ArrayList<>();
    }
}
