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

package org.wso2.ballerina.tooling.service.dockerizer.handler;

import io.fabric8.docker.api.model.ImageDelete;
import io.fabric8.docker.client.DockerClient;
import org.junit.Assert;
import org.junit.Test;
import org.testng.annotations.BeforeTest;
import org.wso2.ballerina.containers.docker.BallerinaDockerClient;
import org.wso2.ballerina.containers.docker.exception.DockerHandlerException;
import org.wso2.ballerina.containers.docker.impl.DefaultBallerinaDockerClient;
import org.wso2.ballerina.containers.docker.utils.Utils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Unit tests for dockerClient.
 */
public class DockerHandlerTest {

    private BallerinaDockerClient dockerClient;

    @BeforeTest
    public void setUp() {
        this.dockerClient = new DefaultBallerinaDockerClient();
    }

    @Test
    public void testSuccessfulCreateServiceImage() throws IOException, InterruptedException, DockerHandlerException {
        String serviceName = "TestService1";
        String imageName = serviceName.toLowerCase();
        Path ballerinaPackage = Paths.get(Utils.getResourceFile("ballerina/TestService.bal").getPath());

        String result = dockerClient.createServiceImage(serviceName, null, ballerinaPackage, null, null);

        deleteDockerImage(imageName);
        Assert.assertTrue("Docker image creation failed.", (result != null) && (result.equals(imageName)));
    }

    @Test
    public void testSuccessfulCreateFunctionImage() throws IOException, InterruptedException, DockerHandlerException {
        String serviceName = "TestFunction1";
        String imageName = serviceName.toLowerCase();
        Path ballerinaPackage = Paths.get(Utils.getResourceFile("ballerina/TestFunction.bal").getPath());

        String result = dockerClient.createMainImage(serviceName, null, ballerinaPackage, null, null);

        deleteDockerImage(imageName);
        Assert.assertTrue("Docker image creation failed.", (result != null) && (result.equals(imageName)));
    }

    @Test
    public void testSuccessfulDeleteImage() throws IOException, InterruptedException, DockerHandlerException {
        String serviceName = "TestFunction2";
        String imageName = serviceName.toLowerCase();
        Path ballerinaPackage = Paths.get(Utils.getResourceFile("ballerina/TestFunction.bal").getPath());

        String result = dockerClient.createMainImage(serviceName, null, ballerinaPackage, null, null);
        Assert.assertTrue("Docker image creation failed.", (result != null) && (result.equals(imageName)));
        boolean deleteResult = dockerClient.deleteImage(imageName, null, null, null);
        Assert.assertTrue("Docker image deletion failed.", deleteResult);

    }

    @Test
    public void testFailedDeleteImage() throws IOException, InterruptedException, DockerHandlerException {
        String nonExistentImageName = "nonexistentimage1";
        boolean result = dockerClient.deleteImage(nonExistentImageName, null, null, null);
        Assert.assertFalse("Docker image deletion.", result);

    }

    @Test
    public void testSuccessfulImageExists() throws IOException, InterruptedException, DockerHandlerException {
        String serviceName = "TestFunction3";
        String imageName = serviceName.toLowerCase();
        Path ballerinaPackage = Paths.get(Utils.getResourceFile("ballerina/TestFunction.bal").getPath());

        String result = dockerClient.createMainImage(serviceName, null, ballerinaPackage, null, null);
        Assert.assertTrue("Docker image creation failed.", (result != null) && (result.equals(imageName)));
        result = dockerClient.getImage(imageName, null);
        deleteDockerImage(imageName);
        Assert.assertTrue("Couldn't find existing image", (result != null) && (result.equals(imageName)));
    }

    @Test
    public void testFailImageExists() throws IOException, InterruptedException {
        String imageName = "nonexistentimage2";
        String result = dockerClient.getImage(imageName, null);
        Assert.assertFalse("Docker image find", result == null);
    }

//    @Test
//    public void testSuccesfulMainRun() throws IOException, InterruptedException, DockerHandlerException {
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

    private void deleteDockerImage(String imageName) {
        DockerClient client = new io.fabric8.docker.client.DefaultDockerClient();
        List<ImageDelete> imageDeleteList = client.image().withName(imageName + ":latest").delete().force().andPrune();
        for (ImageDelete imageDelete : imageDeleteList) {
            imageDelete.getDeleted();
            imageDelete.getUntagged();
        }
    }
}
