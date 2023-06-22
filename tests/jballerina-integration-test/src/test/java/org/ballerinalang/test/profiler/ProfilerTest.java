/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.profiler;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


/**
 * Tests ballerina profiler.
 *
 * @since 2201.7.0
 */
public class ProfilerTest extends BaseTest {
    private static final String testFileLocation = Paths.get("src/test/resources/identifier")
            .toAbsolutePath().toString();
    private BMainInstance bMainInstance;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        bMainInstance = new BMainInstance(balServer);
    }

    @Test
    public void testProfilerExecution() throws BallerinaTestException {
        String sourceRoot = testFileLocation + "/";
        String packageName = "testProject";
        Map<String, String> envProperties = new HashMap<>();
        bMainInstance.addJavaAgents(envProperties);
        bMainInstance.runMain("run",
                new String[]{"--profile", packageName},
                envProperties,
                null,
                null,
                sourceRoot);
    }

//    @Test
//    public void testProfilerOutput() throws BallerinaTestException {
//        Path expectedOutputFilePath = Paths.get(testFileLocation, "singleBalFile", "target", "bin", "ProfilerOutput.html");
//        File file = new File(expectedOutputFilePath.toUri());
//        if (!file.exists()) {
//            throw new BallerinaTestException("Failure to read from the file: " + expectedOutputFilePath);
//        }
//    }
}
