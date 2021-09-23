/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.test;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.ballerinalang.test.TestRunnerUtils.RESOURCE_DIR;
import static org.ballerinalang.test.TestRunnerUtils.TEMP_DIR;
import static org.ballerinalang.test.TestRunnerUtils.deleteDirectory;
import static org.ballerinalang.test.TestRunnerUtils.getAllLabels;
import static org.ballerinalang.test.TestRunnerUtils.isSkippedTest;
import static org.ballerinalang.test.TestRunnerUtils.setDetails;
import static org.ballerinalang.test.TestRunnerUtils.setDetailsOfTest;
import static org.ballerinalang.test.TestRunnerUtils.validateFormatOfTest;
import static org.ballerinalang.test.TestRunnerUtils.validateOutputOfTest;

/**
 * Test runner for run spec conformance tests through compiler phases.
 *
 * @since 2.0.0
 */
public class TestRunner {

    private final Path ballerinaLangDir = Paths.get("").toAbsolutePath().getParent().getParent();
    private final Path testDir = ballerinaLangDir.resolve("tests").resolve("ballerina-spec-conformance-tests");

    @BeforeClass
    public void cleanDir(ITestContext context) {
        context.setAttribute("ReportGenerator", new ReportGenerator());
        TestRunnerUtils.deleteFilesWithinDirectory(new File(ReportGenerator.REPORT_DIR.toString()));
    }

    @Test(dataProvider = "spec-conformance-tests-file-provider")
    public void test(String kind, String path, List<String> outputValues, List<Integer> lineNumbers, String fileName,
                     int absLineNum, boolean isSkippedTest, String diagnostics, ITestContext context,
                     boolean isKnownIssue) {
        setDetailsOfTest(context, kind, fileName, absLineNum, diagnostics);
        isSkippedTest(isSkippedTest);
        validateFormatOfTest(diagnostics);
        validateOutputOfTest(path, kind, outputValues, isKnownIssue, lineNumbers, fileName, absLineNum, context);
    }

    @AfterMethod
    public void resultsOfTests(ITestContext context, ITestResult result) {
        ReportGenerator reportGenerator = (ReportGenerator) context.getAttribute("ReportGenerator");
        setDetails(context, result, reportGenerator);
    }

    @AfterClass
    public void generateReports(ITestContext context) throws IOException {
        deleteDirectory(testDir + "/" + RESOURCE_DIR + TEMP_DIR);
        ReportGenerator reportGenerator = (ReportGenerator) context.getAttribute("ReportGenerator");
        reportGenerator.generateReport();
    }

    public static HashSet<String> runSelectedTests(HashMap<String, HashSet<String>> definedLabels) {
        final String[] labels = new String[] {};
        HashSet<String> hashSet = new HashSet<>();

        for (String label : labels) {
            if (definedLabels.containsKey(label)) {
                getAllLabels(hashSet, definedLabels.get(label), definedLabels, label);
            } else {
                Assert.fail(String.format("Label %s is not defined", label));
            }
        }
        return hashSet;
    }

    @DataProvider(name = "spec-conformance-tests-file-provider")
    public Iterator<Object[]> dataProvider() {
        HashMap<String, HashSet<String>> definedLabels = TestRunnerUtils.readLabels(testDir.toString());
        Set<String> labels = runSelectedTests(definedLabels);
        List<Object[]> testCases = new ArrayList<>();
        try {
            Files.walk(testDir.resolve("src").resolve("test").resolve("resources").resolve("ballerina-spec-tests")
                    .resolve("conformance"))
                    .filter(path -> {
                        File file = path.toFile();
                        return file.isFile() && file.getName().endsWith(".balt");
                    })
                    .map(path -> new Object[]{path.toFile().getName(), path.toString()})
                    .forEach(object -> {
                        try {
                            TestRunnerUtils.readTestFile((String) object[0], (String) object[1], testCases, labels);
                        } catch (IOException e) {
                            Assert.fail("failed to read spec conformance test: \"" + object[0] + "\"", e);
                        }
                    });
            return testCases.iterator();
        } catch (IOException e) {
            Assert.fail("Can't resolve spec conformance tests", e);
            return testCases.iterator();
        }
    }
}
