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
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/**
 * Tests ballerina profiler.
 *
 * @since 2201.7.0
 */
public class ProfilerTest extends BaseTest {
    private static final String testFileLocation =
            Paths.get("src", "test", "resources", "profiler")
                    .toAbsolutePath()
                    .toString();

    String sourceRoot = testFileLocation + "/";
    String packageName = "singleBalFile";
    List<String> outputs = new ArrayList<>();

    @Test
    public void testProfilerExecution() throws BallerinaTestException {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bal", "run", "--profile");
            processBuilder.directory(new File(sourceRoot + packageName + "/"));
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                outputs.add(line);
                if (line.contains("Generating Output")) {
                    Thread.sleep(1000);
                    break;
                }
            }

            if (outputs.toString().contains("Generating Output")) {
                process.destroy();
            } else {
                throw new BallerinaTestException("Error testing the profiler output");
            }

        } catch (Exception e) {
            throw new BallerinaTestException("Error testing the profiler");
        }

    }
}
