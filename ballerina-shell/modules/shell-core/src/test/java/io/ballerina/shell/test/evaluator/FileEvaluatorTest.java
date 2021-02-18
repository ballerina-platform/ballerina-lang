/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.test.evaluator;

import org.testng.annotations.Test;

/**
 * Test simple snippets.
 *
 * @since 2.0.0
 */
public class FileEvaluatorTest extends AbstractEvaluatorTest {
    private static final String BASIC_EVALUATOR_TESTCASE = "testcases/evaluator/files.basic.json";
    private static final String UNORDERED_EVALUATOR_TESTCASE = "testcases/evaluator/files.unordered.json";
    private static final String MU_USED_EVALUATOR_TESTCASE = "testcases/evaluator/files.mu.used.json";
    private static final String RECURSIVE_EVALUATOR_TESTCASE = "testcases/evaluator/files.mu.recursive.json";

    @Test
    public void testEvaluateFileBasic() throws Exception {
        testEvaluate(BASIC_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateFileUnordered() throws Exception {
        testEvaluate(UNORDERED_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateFileMutuallyUsed() throws Exception {
        testEvaluate(MU_USED_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateFileRecursive() throws Exception {
        testEvaluate(RECURSIVE_EVALUATOR_TESTCASE);
    }
}
