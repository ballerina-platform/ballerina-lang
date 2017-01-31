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
import io.fabric8.docker.client.DefaultDockerClient;
import io.fabric8.docker.client.DockerClient;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Unit tests for DockerHandler.
 */
public class DockerHandlerTest {

    private DockerClient client = new DefaultDockerClient();

    @Test
    public void testSuccessfulCreateServiceImage() throws IOException, InterruptedException {
//        String imageName = "testimage0001";
        String serviceName = "TestService";
        String imageName = serviceName.toLowerCase();
        String ballerinaConfig = new String(Files.readAllBytes(Paths.get(Thread.currentThread().getContextClassLoader().
                getResource("ballerina/TestService.bal").getPath())));

        boolean result = DockerHandler.createServiceImage(null, serviceName, ballerinaConfig);

        deleteDockerImage(imageName + ":latest");
        Assert.assertTrue("Docker image creation failed.", result);
    }

    @Test
    public void testSuccessfulCreateFunctionImage() throws IOException, InterruptedException {
//        String imageName = "testimage0002";
        String serviceName = "TestFunction";
        String imageName = serviceName.toLowerCase();
        String ballerinaConfig = new String(Files.readAllBytes(Paths.get(Thread.currentThread().getContextClassLoader().
                getResource("ballerina/TestFunction.bal").getPath())));

        boolean result = DockerHandler.createFunctionImage(null, serviceName, ballerinaConfig);

        deleteDockerImage(imageName + ":latest");
        Assert.assertTrue("Docker image creation failed.", result);
    }

    @Test
    public void testSuccessfulDeleteImage() throws IOException, InterruptedException {
//        String imageName = "testimage0003";
        String serviceName = "TestFunction";
        String imageName = serviceName.toLowerCase();
        String ballerinaConfig = new String(Files.readAllBytes(Paths.get(Thread.currentThread().getContextClassLoader().
                getResource("ballerina/TestFunction.bal").getPath())));

        boolean result = DockerHandler.createFunctionImage(null, serviceName, ballerinaConfig);
        Assert.assertTrue("Docker image creation failed.", result);
        result = DockerHandler.deleteImage(null, imageName);
        Assert.assertTrue("Docker image deletion failed.", result);

    }

    @Test
    public void testFailedDeleteImage() throws IOException, InterruptedException {
        String nonExistentImageName = "nonexistentimage:latest";
        boolean result = DockerHandler.deleteImage(null, nonExistentImageName);
        Assert.assertFalse("Docker image deletion.", result);

    }

    private void deleteDockerImage(String imageName) {
        List<ImageDelete> imageDeleteList = client.image().withName(imageName).delete().force().andPrune();
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
