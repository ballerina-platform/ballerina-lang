/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package io.ballerina.shell.test.unit;

import io.ballerina.shell.exceptions.PreprocessorException;
import io.ballerina.shell.preprocessor.Preprocessor;
import io.ballerina.shell.preprocessor.SeparatorPreprocessor;
import io.ballerina.shell.test.TestUtils;
import io.ballerina.shell.test.unit.base.TestCase;
import io.ballerina.shell.test.unit.base.TestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.List;

/**
 * Test preprocessor use cases.
 *
 * @since 2.0.0
 */
public class SeparatorPreprocessorTest {
    private static final String TESTCASES = "testcases/preprocessor.separator.json";

    @Test
    public void testProcess() {
        List<TestCase> testCases = TestUtils.loadTestCases(TESTCASES, TestCases.class);
        Preprocessor preprocessor = new SeparatorPreprocessor();
        for (TestCase testCase : testCases) {
            try {
                Collection<String> actual = preprocessor.process(testCase.getInput());
                Assert.assertEquals(actual, testCase.getExpected(), testCase.getName());
                Assert.assertTrue(testCase.isAccepted(), testCase.getName());
            } catch (PreprocessorException e) {
                Assert.assertFalse(testCase.isAccepted(), testCase.getName());
            }
        }
    }
}
