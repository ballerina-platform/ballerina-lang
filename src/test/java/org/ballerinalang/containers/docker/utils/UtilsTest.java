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

package org.ballerinalang.containers.docker.utils;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;

/**
 * Test Docker Utils.
 */
public class UtilsTest {

    @Test
    public void testGetResourceSuccessful() throws FileNotFoundException {
        File testNgFile = Utils.getResourceFile("testng.xml");
        Assert.assertNotNull(testNgFile);
    }

    @Test(expectedExceptions = {FileNotFoundException.class})
    public void testGetResourceFailed() throws FileNotFoundException {
        Utils.getResourceFile("nonExistingResource.rc");
    }

    @Test
    public void testGenerateContainerName() {
        String containerName = Utils.generateContainerName();
        Assert.assertNotNull(containerName);
        Assert.assertTrue(containerName.split("_").length == 2);
        Assert.assertFalse(containerName.startsWith("_"));
    }

    @Test
    public void testGenerateContainerPort() {
        int containerPort = Utils.generateContainerPort();
        Assert.assertNotEquals(containerPort, 0);

        ServerSocket testSocket = null;
        try {
            testSocket = new ServerSocket(containerPort);
            Assert.assertFalse(testSocket.isClosed());
        } catch (IOException e) {
            // test failure
        } finally {
            if (testSocket != null) {
                try {
                    testSocket.close();
                } catch (IOException e) {
                    // test failure
                }
            }
        }
    }
}
