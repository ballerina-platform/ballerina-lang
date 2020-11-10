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
package org.ballerinalang.test.runtime;

import com.google.gson.Gson;
import io.ballerina.projects.testsuite.TestSuite;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.runtime.launch.LaunchUtils;
import org.ballerinalang.test.runtime.entity.ModuleStatus;
import org.ballerinalang.test.runtime.util.TesterinaConstants;
import org.ballerinalang.test.runtime.util.TesterinaUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Main class to init the test suit.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        InputStream jsonCachePath = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(ProjectConstants.TEST_SUITE);
        Path jsonTmpSummaryPath = Paths.get(args[0], TesterinaConstants.STATUS_FILE);
        String[] configArgs = Arrays.copyOfRange(args, 1, args.length);
        LaunchUtils.initConfigurations(configArgs);

        TestSuite testSuite = deserialize(jsonCachePath.readAllBytes());
        startTestSuit(Paths.get(testSuite.getSourceRootPath()), testSuite, jsonTmpSummaryPath);
    }

    private static TestSuite deserialize(byte[] byteArray) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
             ObjectInput in = new ObjectInputStream(bis)) {
            return (TestSuite) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void startTestSuit(Path sourceRootPath, TestSuite testSuite, Path jsonTmpSummaryPath)
            throws IOException {
        int exitStatus = 0;
        try {
            TesterinaUtils.executeTests(sourceRootPath, testSuite);
        } catch (RuntimeException e) {
            exitStatus = 1;
        } finally {
            if (testSuite.isReportRequired()) {
                writeStatusToJsonFile(ModuleStatus.getInstance(), jsonTmpSummaryPath);
            }
            Runtime.getRuntime().exit(exitStatus);
        }
    }

    private static void writeStatusToJsonFile(ModuleStatus moduleStatus, Path tmpJsonPath) throws IOException {
        File jsonFile = new File(tmpJsonPath.toString());
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8)) {
            Gson gson = new Gson();
            String json = gson.toJson(moduleStatus);
            writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        }
    }
}
