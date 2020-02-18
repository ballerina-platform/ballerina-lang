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
package org.ballerinalang.test.launcher;

import com.google.gson.Gson;
import org.ballerinalang.test.launcher.entity.TestJsonData;
import org.ballerinalang.testerina.core.TesterinaConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

/**
 * Main class to init the test suit.
 */
public class Launch {

    private static PrintStream outsStream = System.out;
    private static PrintStream errStream = System.err;

    public static void main(String[] args) {

        Path jsonCachePath = Paths.get(args[0], TesterinaConstants.TESTERINA_TEST_SUITE);
        try {
            BufferedReader br = Files.newBufferedReader(jsonCachePath, StandardCharsets.UTF_8);

            //convert the json string back to object
            Gson gson = new Gson();
            TestJsonData response = gson.fromJson(br, TestJsonData.class);
            startTestSuit(Paths.get(response.getSourceRootPath()), response);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            errStream.println(e);
        }
    }

    private static void startTestSuit(Path sourceRootPath, TestJsonData testJsonData) {
        executeTests(sourceRootPath, testJsonData, outsStream, errStream);
    }

    /**
     * Execute tests in build.
     *
     * @param sourceRootPath source root path
     * @param testJsonData testMetaData
     * @param outStream      error stream for logging.
     * @param errStream      info stream for logging.
     */
    public static void executeTests(Path sourceRootPath, TestJsonData testJsonData,
                                    PrintStream outStream, PrintStream errStream) {
        BTestRunner testRunner = new BTestRunner(outStream, errStream);
        // Run the tests
        testRunner.runTest(testJsonData);
        if (testRunner.getTesterinaReport().isFailure()) {
            cleanUpDir(sourceRootPath.resolve(TesterinaConstants.TESTERINA_TEMP_DIR));
            Runtime.getRuntime().exit(1);
        }
        cleanUpDir(sourceRootPath.resolve(TesterinaConstants.TESTERINA_TEMP_DIR));
    }

    /**
     * Cleans up any remaining testerina metadata.
     *
     * @param path The path of the Directory/File to be deleted
     */
    public static void cleanUpDir(Path path) {
        try {
            if (Files.exists(path)) {
                Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            }
        } catch (IOException e) {
            errStream.println("Error occurred while deleting the dir : " + path.toString() + " with error : "
                                      + e.getMessage());
        }
    }
}
