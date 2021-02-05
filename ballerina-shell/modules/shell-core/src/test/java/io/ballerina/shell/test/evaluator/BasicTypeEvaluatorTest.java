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
public class BasicTypeEvaluatorTest extends AbstractEvaluatorTest {
    private static final String STRING_EVALUATOR_TESTCASE = "testcases/evaluator/values.strings.json";
    private static final String TUPLES_EVALUATOR_TESTCASE = "testcases/evaluator/values.tuples.json";
    private static final String ARRAYS_EVALUATOR_TESTCASE = "testcases/evaluator/values.arrays.json";
    private static final String TABLE_EVALUATOR_TESTCASE = "testcases/evaluator/values.table.json";
    private static final String MAPS_EVALUATOR_TESTCASE = "testcases/evaluator/values.maps.json";

    @Test
    public void testEvaluateString() throws BallerinaShellException {
        testEvaluate(STRING_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateTuples() throws BallerinaShellException {
        testEvaluate(TUPLES_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateArrays() throws BallerinaShellException {
        testEvaluate(ARRAYS_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateTable() throws BallerinaShellException {
        testEvaluate(TABLE_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateMaps() throws BallerinaShellException {
        testEvaluate(MAPS_EVALUATOR_TESTCASE);
    }
}
