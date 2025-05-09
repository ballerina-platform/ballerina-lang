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

package io.ballerina.shell.test.evaluator;

import org.testng.annotations.Test;

/**
 * Test simple snippets.
 *
 * @since 2.0.0
 */
public class RegressionEvaluatorTest extends AbstractEvaluatorTest {
    private static final String SAME_IMPORT_EVALUATOR_TESTCASE = "testcases/evaluator/regression.same.import.json";
    private static final String IMPORT_USED_FN_TESTCASE = "testcases/evaluator/regression.import.used.fn.json";
    private static final String PANIC_SAVE_STATE_TESTCASE = "testcases/evaluator/regression.panic.save.state.json";
    private static final String QUALIFIERS_TESTCASE = "testcases/evaluator/regression.qualifiers.json";
    private static final String IMPORT_CYCLIC_TYPE_TESTCASE = "testcases/evaluator/regression.cyclic.type.json";

    @Test
    public void testEvaluateSameImport() {
        // Import can be again and again imported with same prefix.
        testEvaluate(SAME_IMPORT_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateImportUsedFn() {
        // Functions using imports are correctly processed.
        testEvaluate(IMPORT_USED_FN_TESTCASE);
    }

    @Test
    public void testEvaluateQualifiers() {
        // Test for qualifiers use.
        testEvaluate(QUALIFIERS_TESTCASE);
    }

    @Test
    public void testEvaluatePanicSaveState() {
        // Check if panic correctly preserves state.
        testEvaluate(PANIC_SAVE_STATE_TESTCASE);
    }

    @Test
    public void testEvaluateCyclicType() {
        // Cyclic types use.
        testEvaluate(IMPORT_CYCLIC_TYPE_TESTCASE);
    }
}
