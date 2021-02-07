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

import io.ballerina.shell.exceptions.BallerinaShellException;
import org.testng.annotations.Test;

/**
 * Test simple snippets.
 *
 * @since 2.0.0
 */
public class PatternEvaluatorTest extends AbstractEvaluatorTest {
    private static final String TUPLE_EVALUATOR_TESTCASE = "testcases/evaluator/pattern.tuple.json";
    private static final String RECORD_EVALUATOR_TESTCASE = "testcases/evaluator/pattern.record.json";
    private static final String ARRAY_EVALUATOR_TESTCASE = "testcases/evaluator/pattern.array.json";

    @Test
    public void testEvaluateTuple() throws BallerinaShellException {
        // TODO: (#27824) Module level list binding patterns are unsupported
        testEvaluate(TUPLE_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateRecord() throws BallerinaShellException {
        // TODO: (#27824) Module level mapping binding patterns are unsupported
        testEvaluate(RECORD_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateArray() throws BallerinaShellException {
        // TODO: (#28434) Fix issue of float[3] type converting to float[]
        // TODO: (#27824) Module level array binding patterns are unsupported
        // testEvaluate(ARRAY_EVALUATOR_TESTCASE);
    }
}
