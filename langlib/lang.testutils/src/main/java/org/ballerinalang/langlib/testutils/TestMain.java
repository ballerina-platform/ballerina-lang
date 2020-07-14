/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.langlib.testutils;


import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.values.XMLValue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import static java.lang.System.exit;

/**
 * Main class to init the test suite.
 *
 * @since 2.0.0
 */

public class TestMain {
    private static final PrintStream outStream = System.out;

    public static void main(String[] args) {
        for (int i = 1; i < args.length; i++) {
            String testSuitePath = args[0] + args[i];
            CTestSuite cTestSuite = new CTestSuite();
            extractTestGroupPaths(cTestSuite, testSuitePath, args[0]);
        }
        //TODO: remove after implementing proper build failure approach
        if (CTestSuite.failedTestCount > 0) {
            exit(1);
        }
    }

    public static void extractTestGroupPaths(CTestSuite cTestSuite, String testSuitePath, String absPath) {
        XMLValue xmlValue = readXml(testSuitePath);
        if (xmlValue != null) {
            InitializeTestSuite.extractPath(xmlValue, cTestSuite, absPath);
            testSuiteInitilizer(cTestSuite, absPath);
        } else {
            CTestSuite.failedTestCount++;
        }
    }

    public static void testSuiteInitilizer(CTestSuite cTestSuite, String absPath) {
        for (String groupPath : cTestSuite.getPaths()) {
            CTestGroup cTestGroup = new CTestGroup();
            XMLValue xmlValue = readXml(groupPath);
            if (xmlValue != null) {
                InitializeTestSuite.fillGroup(xmlValue, cTestGroup, absPath);
                cTestSuite.setTestGroup(cTestGroup);
            } else {
                CTestSuite.failedTestCount++;
            }
        }
        invokeTests(cTestSuite);
    }

    public static void invokeTests(CTestSuite cTestSuite) {
        CTestRunner cTestRunner = new CTestRunner();
        cTestRunner.invokeTestSuite(cTestSuite);
    }

    public static XMLValue readXml(String path) {
        XMLValue xmlvalue = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(path);
            xmlvalue = XMLFactory.parse(fileInputStream);
        } catch (FileNotFoundException e) {
            outStream.println(e.getMessage());
        }
        return xmlvalue;
    }
}
