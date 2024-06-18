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
public class SimpleEvaluatorTest extends AbstractEvaluatorTest {
    private static final String BASIC_EVALUATOR_TESTCASE = "testcases/evaluator/values.basic.json";
    private static final String LITERALS_EVALUATOR_TESTCASE = "testcases/evaluator/values.literals.json";

    @Test
    public void testEvaluateBasic() {
        testEvaluate(BASIC_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateLiterals() {
        testEvaluate(LITERALS_EVALUATOR_TESTCASE);
    }
}
