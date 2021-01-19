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
public class OperationsEvaluatorTest extends AbstractEvaluatorTest {
    private static final String SHIFT_OPERATION_TESTCASE = "testcases/evaluator/operations.shift.json";
    private static final String BITWISE_OPERATION_TESTCASE = "testcases/evaluator/operations.bitwise.json";
    private static final String COMPOUND_OPERATION_TESTCASE = "testcases/evaluator/operations.comp.json";
    private static final String EQUALITY_OPERATION_TESTCASE = "testcases/evaluator/operations.equality.json";
    private static final String LENGTH_OPERATION_TESTCASE = "testcases/evaluator/operations.length.json";
    private static final String CAST_OPERATION_TESTCASE = "testcases/evaluator/operations.cast.json";
    private static final String OPTIONAL_OPERATION_TESTCASE = "testcases/evaluator/operations.optional.json";
    private static final String CLONE_OPERATION_TESTCASE = "testcases/evaluator/operations.clone.json";
    private static final String IMMUTABLE_OPERATION_TESTCASE = "testcases/evaluator/operations.immutable.json";
    private static final String CONV_OPERATION_TESTCASE = "testcases/evaluator/operations.conv.json";

    @Test
    public void testEvaluateShift() throws BallerinaShellException {
        testEvaluate(SHIFT_OPERATION_TESTCASE);
    }

    @Test
    public void testEvaluateBitwise() throws BallerinaShellException {
        testEvaluate(BITWISE_OPERATION_TESTCASE);
    }

    @Test
    public void testEvaluateCompound() throws BallerinaShellException {
        testEvaluate(COMPOUND_OPERATION_TESTCASE);
    }

    @Test
    public void testEvaluateEquality() throws BallerinaShellException {
        testEvaluate(EQUALITY_OPERATION_TESTCASE);
    }

    @Test
    public void testEvaluateLength() throws BallerinaShellException {
        testEvaluate(LENGTH_OPERATION_TESTCASE);
    }

    @Test
    public void testEvaluateCast() throws BallerinaShellException {
        testEvaluate(CAST_OPERATION_TESTCASE);
    }

    @Test
    public void testEvaluateOptional() throws BallerinaShellException {
        testEvaluate(OPTIONAL_OPERATION_TESTCASE);
    }

    @Test
    public void testEvaluateClone() throws BallerinaShellException {
        testEvaluate(CLONE_OPERATION_TESTCASE);
    }

    @Test
    public void testEvaluateImmutable() throws BallerinaShellException {
        testEvaluate(IMMUTABLE_OPERATION_TESTCASE);
    }

    @Test
    public void testEvaluateConvertTypes() throws BallerinaShellException {
        testEvaluate(CONV_OPERATION_TESTCASE);
    }
}
