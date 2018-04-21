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

import org.ballerinalang.testerina.core.BTestRunner;
import org.ballerinalang.testerina.core.TesterinaConstants;
import org.ballerinalang.testerina.core.TesterinaRegistry;
import org.ballerinalang.testerina.util.Utils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Test cases for ballerina.test package.
 */
public class ServiceSkeletonTest {

    String sourceRoot = "src/test/resources/";

    @BeforeClass
    public void setup() {
        System.setProperty("java.util.logging.manager", "org.ballerinalang.logging.BLogManager");
        System.setProperty("java.util.logging.config.file", "logging.properties");
        System.setProperty(TesterinaConstants.BALLERINA_SOURCE_ROOT, System.getProperty("user.dir")
                                                                     + "/src/test/resources");
    }

    @Test
    public void testBefore() {
        cleanup();
        BTestRunner bTestRunner = new BTestRunner();
        bTestRunner.runTest(sourceRoot + "service.skeleton", new Path[]{Paths.get("service-skeleton-test.bal")}, new
                ArrayList<>());
        Assert.assertEquals(bTestRunner.getTesterinaReport().getTestSummary(".", "passed"), 1);
        Utils.cleanUpDir(Paths.get(System.getProperty(TesterinaConstants.BALLERINA_SOURCE_ROOT), TesterinaConstants
            .TESTERINA_TEMP_DIR));
    }

    private void cleanup() {
        TesterinaRegistry.getInstance().setProgramFiles(new ArrayList<>());
        TesterinaRegistry.getInstance().setTestSuites(new HashMap<>());
    }
}
