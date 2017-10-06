/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.statements.returnstmt;

import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for return statement negative cases.
 */
public class ReturnStmtNegativeTest {

    CompileResult resultNegative;

    @Test(description = "Test semantic errors of Return stmt")
    public void testReturnStmtNegativeCases() {
        resultNegative = BTestUtils.compile("test-src/statements/returnstmt/return-stmt-negative.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 19);
        //testReturnInResource
        BTestUtils.validateError(resultNegative, 0, "return value is not expected", 7, 9);
        //testNotEnoughArgsToReturn1value
        BTestUtils.validateError(resultNegative, 1, "multi value return is expected", 12, 5);
        //TODO fix issue #3481
        // testNotEnoughArgsToReturn2values
        BTestUtils.validateError(resultNegative, 2, "assignment count mismatch: 2 != 1", 16, 12);
        //testNotEnoughArgsToReturn3values
        BTestUtils.validateError(resultNegative, 3, "not enough return values", 20, 5);
        //testTooManyArgsToReturn1value
        BTestUtils.validateError(resultNegative, 4, "single value return is expected", 24, 5);
        //testTooManyArgsToReturn2values
        BTestUtils.validateError(resultNegative, 5, "multi-valued 'split2()' in single-value context", 28, 12);
        //testInputTypeMismatch1values
        BTestUtils.validateError(resultNegative, 6, "incompatible types: expected 'int', found 'string'", 32, 12);
        //testInputTypeMismatch2values
        BTestUtils.validateError(resultNegative, 7, "incompatible types: expected 'boolean', found 'int'", 36, 25);
        //testMultiValueInSingleContext
        BTestUtils.validateError(resultNegative, 8, "multi-valued 'split4()' in single-value context", 212, 12);
        //testMissingReturnStatementWithinIf
        BTestUtils.validateError(resultNegative, 9, "this function must return a result", 51, 1);
        //testMissingReturnStatementWithinIfElse
        BTestUtils.validateError(resultNegative, 10, "this function must return a result", 59, 1);
        //testMissingReturnInNestedIf1
        BTestUtils.validateError(resultNegative, 11, "this function must return a result", 69, 1);
        //testMissingReturnInNestedIf2
        BTestUtils.validateError(resultNegative, 12, "this function must return a result", 82, 1);
        //testMissingReturnInNestedIf3
        BTestUtils.validateError(resultNegative, 13, "this function must return a result", 97, 1);
        //testUnreachableReturnStmt1
        BTestUtils.validateError(resultNegative, 14, "unreachable code", 136, 5);
        //testUnreachableReturnStmt2
        BTestUtils.validateError(resultNegative, 15, "unreachable code", 150, 9);
        //testUnreachableReturnStmt3
        BTestUtils.validateError(resultNegative, 16, "unreachable code", 168, 13);
        //testUnreachableReturnStmt4
        BTestUtils.validateError(resultNegative, 17, "unreachable code", 188, 9);
        //testUnreachableReturnStmt5
        BTestUtils.validateError(resultNegative, 18, "unreachable code", 208, 5);
    }

    @Test(description = "Test semantic errors of Return stmt in fork join", enabled = false)
    public void testReturnStmtWithForkJoinNegativeCases() {
        resultNegative = BTestUtils.compile("test-src/statements/returnstmt/return-stmt-forkjoin-negative.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 2);
        //testMissingReturnForkJoin1
        BTestUtils.validateError(resultNegative, 0, "this function must return a result", 0, 0);
        //testMissingReturnForkJoin1
        BTestUtils.validateError(resultNegative, 1, "this function must return a result", 0, 0);
    }

//    @Test(description = "Test missing return",
//          expectedExceptions = {SemanticException.class},
//          expectedExceptionsMessageRegExp = "missing-return-forkjoin-1.bal:1: missing return statement")
//    public void testMissingReturnForkJoin1() {
//        BTestUtils.getProgramFile("lang/statements/returnstmt/missing-return-forkjoin-1.bal");
//    }
//
//    @Test(description = "Test missing return",
//          expectedExceptions = {SemanticException.class},
//          expectedExceptionsMessageRegExp = "missing-return-forkjoin-2.bal:1: missing return statement")
//    public void testMissingReturnForkJoin2() {
//        BTestUtils.getProgramFile("lang/statements/returnstmt/missing-return-forkjoin-2.bal");
//    }
}
