/*
*   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.testerina.test;

import org.ballerinalang.test.launcher.BTestRunner;
import org.ballerinalang.test.launcher.util.TesterinaUtils;
import org.ballerinalang.testerina.core.TesterinaRegistry;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Test cases for ballerina.test package.
 */
public class ServiceTest {

    String sourceRoot = "src/test/resources";

    @BeforeClass
    public void createDir() throws IOException {
        Path filePath = Paths.get(sourceRoot + "/.ballerina");
        Files.deleteIfExists(filePath);
        Files.createDirectory(filePath);
    }

    @BeforeClass
    public void setup() {
        System.setProperty("java.util.logging.manager", "org.ballerinalang.logging.BLogManager");
        System.setProperty("java.util.logging.config.file", "logging.properties");
    }

    @Test(enabled = false)
    public void testBefore() {
        TesterinaRegistry.getInstance().setOrgName("$anon");
        BTestRunner bTestRunner = new BTestRunner();
        bTestRunner.runTest("src/test/resources", new Path[]{Paths.get("servicemocktest"), Paths.get
                ("servicemocktest2")}, new ArrayList<>());
        Assert.assertEquals(bTestRunner.getTesterinaReport().getTestSummary("servicemocktest", "passed"), 1);
    }

    @AfterMethod
    private void cleanup() {
        TesterinaRegistry.getInstance().setTestSuites(new HashMap<>());
        TesterinaRegistry.getInstance().getInitializedPackages().clear();
        TesterinaRegistry.getInstance().setOrgName(null);
    }

    @AfterClass
    public void cleanDirectory() throws IOException {
        TesterinaUtils.cleanUpDir(Paths.get(sourceRoot + "/.ballerina"));
    }

}
