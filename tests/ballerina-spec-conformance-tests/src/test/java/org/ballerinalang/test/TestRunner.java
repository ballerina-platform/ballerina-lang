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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Test runner for run spec conformance tests through compiler phases.
 *
 * @since 2.0.0
 */
public class TestRunner {

    private final Path ballerinaLangDir = Paths.get("").toAbsolutePath().getParent().getParent();
    private final Path testDir = ballerinaLangDir.resolve("tests").resolve("ballerina-spec-conformance-tests");
    private final Path testSourcesDirectory = Paths.get("src/test/resources").toAbsolutePath().normalize();
    CompileResult compileResult;

    @Test(dataProvider = "spec-conformance-test-file-provider")
    public void test(String type, String path, List<String> outputValues, List<Integer> errLines, String fileName,
                                                                                                       int absLineNum) {
        try {
            compileResult = BCompileUtil.compile(path);
            Files.delete(testSourcesDirectory.resolve((new File(path)).toPath()));
        } catch (Exception e) {
            Assert.fail("failed to run spec conformance test: \"" + fileName + "\"", e);
        }

        if (!type.equals(TestRunnerUtils.ERROR)) {
            BRunUtil.ExitDetails exitDetails = BRunUtil.run(compileResult);
            if (exitDetails.exitCode != 0) {
                Matcher matcher = Pattern.compile(":(\\d+)\\)").matcher(exitDetails.errorOutput);
                if (matcher.find()) {
                    TestRunnerUtils.validatePanic(Integer.parseInt(matcher.group(1)) + absLineNum,
                                                  errLines.get(0) + absLineNum, fileName);
                    return;
                }
            }
            String consoleOutput = exitDetails.consoleOutput;
            String[] result = consoleOutput.split("\r\n|\r|\n");
            TestRunnerUtils.validateOutput(fileName, outputValues, result);
        } else {
            TestRunnerUtils.validateError(compileResult, errLines, fileName, absLineNum);
        }
    }

    public HashSet<String> skipList() {
        // to skip balt file
        HashSet<String> hashSet = new HashSet<>();
        return hashSet;
    }


    public HashSet<String> runSelectedTests() {
        final String[] setValues = new String[] {Labels.BOOLEAN_TYPE_DESCRIPTOR};
        return new HashSet<>(Arrays.asList(setValues));
    }

    @DataProvider(name = "spec-conformance-test-file-provider")
    public Iterator<Object[]> dataProvider() {
        HashSet<String> skippedTests = skipList();
        Set<String> labels = runSelectedTests();
        List<Object[]> testCases = new ArrayList<>();
        try {
            Files.walk(testDir.resolve("src").resolve("test").resolve("resources").resolve("ballerina-spec-tests")
                    .resolve("conformance"))
                    .filter(path -> {
                        File file = path.toFile();
                        return file.isFile() && file.getName().endsWith(".balt")
                                && !skippedTests.contains(file.getName());
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
