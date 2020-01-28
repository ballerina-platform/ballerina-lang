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
package org.ballerinalang.testerina;

import com.google.gson.Gson;
import org.ballerinalang.testerina.core.TesterinaConstants;
import org.ballerinalang.testerina.core.TesterinaRegistry;
import org.ballerinalang.testerina.core.entity.Test;
import org.ballerinalang.testerina.core.entity.TestJsonData;
import org.ballerinalang.testerina.core.entity.TestMetaData;
import org.ballerinalang.testerina.core.entity.TestSuite;
import org.ballerinalang.testerina.util.TesterinaUtils;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main class to init the test suit.
 *
 * @since 1.2.0
 */
public class Executor {

    private static PrintStream outsStream = System.out;
    private static PrintStream errStream = System.err;

    public static void main(String[] args) {
        String testSuitCachePath = System.getenv("testerina.tesetsuite.path"); // get the path from env
        Path jsonCachePath = Paths.get(testSuitCachePath, TesterinaConstants.TESTERINA_TEST_SUITE);
        try {
            BufferedReader br = Files.newBufferedReader(jsonCachePath, StandardCharsets.UTF_8);

            //convert the json string back to object
            Gson gson = new Gson();
            TestJsonData response = gson.fromJson(br, TestJsonData.class);
            HashMap<TestMetaData, String> testMetaDataMap = initTestSuit(response);
            startTestSuit(Paths.get(response.getSourceRootPath()), testMetaDataMap);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            errStream.println(e);
        }
    }

    private static HashMap<TestMetaData, String> initTestSuit(TestJsonData testJsonData) {
        TestMetaData testMetaData = new TestMetaData();

        // set the PackageID
        testMetaData.setPackageID(testJsonData.getPackageID());

        // set the tests into the test suit and init the test suit
        List<Test> testNames = new ArrayList<>();
        Map<String, String> tmpTestMap = testJsonData.getTestFunctionNames();
        tmpTestMap.forEach((functionName, className) -> {
            if (!functionName.contains("$")) {
                Test test = new Test();
                test.setTestName(functionName);
                testNames.add(test);
            }
        });
        TestSuite testSuite = new TestSuite(testJsonData.getPackageName());
        testSuite.setTests(testNames);

        // create the test suit map
        Map<String, TestSuite> testSuiteMap = new HashMap<>();
        testSuiteMap.put(testJsonData.getPackageName(), testSuite);

        // set the test suit into the Testerina registry
        TesterinaRegistry testerinaRegistry = TesterinaRegistry.getInstance();
        testerinaRegistry.setTestSuites(testSuiteMap);

        // setting the function name maps with class names
        HashMap<String, String> callableFunctionNames = testJsonData.getCallableFunctionNames();
        HashMap<String, String> testFunctionNames = testJsonData.getTestFunctionNames();
        testMetaData.setCallableFunctionNames(callableFunctionNames);
        testMetaData.setTestFunctionNames(testFunctionNames);

        // set the init/start/stop function names for both normal and test functions
        testMetaData.setInitFunctionName(testJsonData.getInitFunctionName());
        testMetaData.setStartFunctionName(testJsonData.getStartFunctionName());
        testMetaData.setStopFunctionName(testJsonData.getStopFunctionName());
        testMetaData.setTestInitFunctionName(testJsonData.getTestInitFunctionName());
        testMetaData.setTestStartFunctionName(testJsonData.getTestStartFunctionName());
        testMetaData.setTestStopFunctionName(testJsonData.getTestStopFunctionName());

        // set rest required data
        testMetaData.setPackageName(testJsonData.getPackageName());
        testMetaData.setHasTestablePackages(Boolean.parseBoolean(testJsonData.isHasTestablePackages()));

        String testName = testJsonData.getPackageName();

        HashMap<TestMetaData, String> testMetaDataMap = new HashMap<>();
        testMetaDataMap.put(testMetaData, testName);

        return testMetaDataMap;
    }

    private static void startTestSuit(Path sourceRootPath, Map<TestMetaData, String> classLoaderMap) {
        TesterinaUtils.executeTests(sourceRootPath, classLoaderMap, outsStream, errStream);
    }
}
