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
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.ballerina.containers.Constants;
import org.wso2.ballerina.containers.docker.impl.DefaultBallerinaDockerClient;
import org.wso2.ballerina.containers.docker.exception.DockerHandlerException;
import org.wso2.ballerina.containers.docker.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Unit tests for DefaultBallerinaDockerClient.
 */
public class DockerHandlerTest {

    @Test
    public void testSuccessfulCreateServiceImage() throws IOException, InterruptedException {
        String serviceName = "TestService1";
        String imageName = serviceName.toLowerCase();
        String ballerinaConfig = new String(Files.readAllBytes(Paths.get(Utils.getResourceFile("ballerina/TestService.bal").getPath())));

        boolean result = DefaultBallerinaDockerClient.createServiceImage(serviceName, null, ballerinaConfig);

        deleteDockerImage(imageName);
        Assert.assertTrue("Docker image creation failed.", result);
    }

    @Test
    public void testSuccessfulCreateFunctionImage() throws IOException, InterruptedException {
        String serviceName = "TestFunction1";
        String imageName = serviceName.toLowerCase();
        String ballerinaConfig = new String(Files.readAllBytes(Paths.get(Utils.getResourceFile("ballerina/TestFunction.bal").getPath())));

        boolean result = DefaultBallerinaDockerClient.createFunctionImage(serviceName, null, ballerinaConfig);

        deleteDockerImage(imageName);
        Assert.assertTrue("Docker image creation failed.", result);
    }

    @Test
    public void testSuccessfulDeleteImage() throws IOException, InterruptedException {
        String serviceName = "TestFunction2";
        String imageName = serviceName.toLowerCase();
        String ballerinaConfig = new String(Files.readAllBytes(Paths.get(Utils.getResourceFile("ballerina/TestFunction.bal").getPath())));

        boolean result = DefaultBallerinaDockerClient.createFunctionImage(serviceName, null, ballerinaConfig);
        Assert.assertTrue("Docker image creation failed.", result);
        result = DefaultBallerinaDockerClient.deleteImage(imageName, null);
        Assert.assertTrue("Docker image deletion failed.", result);

    }

    @Test
    public void testFailedDeleteImage() throws IOException, InterruptedException {
        String nonExistentImageName = "nonexistentimage1";
        boolean result = DefaultBallerinaDockerClient.deleteImage(nonExistentImageName, null);
        Assert.assertFalse("Docker image deletion.", result);

    }

    @Test
    public void testSuccessfulImageExists() throws IOException, InterruptedException {
        String serviceName = "TestFunction3";
        String imageName = serviceName.toLowerCase();
        String ballerinaConfig = new String(Files.readAllBytes(Paths.get(Utils.getResourceFile("ballerina/TestFunction.bal").getPath())));

        boolean result = DefaultBallerinaDockerClient.createFunctionImage(serviceName, null, ballerinaConfig);
        Assert.assertTrue("Docker image creation failed.", result);
        result = DefaultBallerinaDockerClient.getImage(imageName, null);
        deleteDockerImage(imageName);
        Assert.assertTrue("Couldn't find existing image", result);
    }

    @Test
    public void testFailImageExists() throws IOException, InterruptedException {
        String imageName = "nonexistentimage2";
        boolean result = DefaultBallerinaDockerClient.getImage(imageName, null);
        Assert.assertFalse("Docker image find", result);
    }

    @Test
    public void testSuccesfulFunctionRun() throws IOException, InterruptedException, DockerHandlerException {
        String serviceName = "TestFunction4";
        String imageName = serviceName.toLowerCase();
        String ballerinaConfig = new String(Files.readAllBytes(Paths.get(Utils.getResourceFile("ballerina/TestFunction.bal").getPath())));

        boolean result = DefaultBallerinaDockerClient.createFunctionImage(serviceName, null, ballerinaConfig);
        Assert.assertTrue("Docker image creation failed.", result);
        String output = DefaultBallerinaDockerClient.runFunctionContainer(null, serviceName, Constants.TYPE_BALLERINA_FUNCTION);
        deleteDockerImage(imageName);
        Assert.assertTrue("Running Ballerina function in Docker failed.", "Hello, World!".equals(output));
    }

    private void deleteDockerImage(String imageName) {
        DockerClient client = new io.fabric8.docker.client.DefaultDockerClient();
        List<ImageDelete> imageDeleteList = client.image().withName(imageName + ":latest").delete().force().andPrune();
        for (ImageDelete imageDelete : imageDeleteList) {
            if (StringUtils.isNotEmpty(imageDelete.getDeleted())) {
                System.out.println("Deleted:" + imageDelete.getDeleted());
            }
            if (StringUtils.isNotEmpty(imageDelete.getUntagged())) {
                System.out.println("Untagged:" + imageDelete.getUntagged());
            }
        }
    }
}
