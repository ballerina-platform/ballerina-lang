/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.statements;

import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.exceptions.SemanticException;
import org.testng.annotations.Test;

/**
 * Test class for return statement negative cases.
 */
public class ReturnStmtNegativeTest {

    @Test(description = "Test return statement in resource",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "return-in-resource.bal:5: return statement " +
                    "cannot be used in a resource definition")
    public void testReturnInResource() {
        BTestUtils.getProgramFile("lang/statements/returnstmt/return-in-resource.bal");
    }

    public static void main(String[] args) {
        ReturnStmtNegativeTest test = new ReturnStmtNegativeTest();
        test.testReturnInResource();
    }

    @Test(description = "Test not enough arguments to return",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "not-enough-args-to-return-1.bal:2: not enough arguments to return")
    public void testNotEnoughArgsToReturn1() {
        BTestUtils.getProgramFile("lang/statements/returnstmt/not-enough-args-to-return-1.bal");
    }

    @Test(description = "Test not enough arguments to return",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "not-enough-args-to-return-2.bal:2: not enough arguments to return")
    public void testNotEnoughArgsToReturn2() {
        BTestUtils.getProgramFile("lang/statements/returnstmt/not-enough-args-to-return-2.bal");
    }

    @Test(description = "Test not enough arguments to return",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "not-enough-args-to-return-3.bal:2: not enough arguments to return")
    public void testNotEnoughArgsToReturn3() {
        BTestUtils.getProgramFile("lang/statements/returnstmt/not-enough-args-to-return-3.bal");
    }

    @Test(description = "Test too many arguments to return",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "too-many-args-to-return-1.bal:2: too many arguments to return")
    public void testTooManyArgsToReturn1() {
        BTestUtils.getProgramFile("lang/statements/returnstmt/too-many-args-to-return-1.bal");
    }

    @Test(description = "Test too many arguments to return",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "too-many-args-to-return-2.bal:2: too many arguments to return")
    public void testTooManyArgsToReturn2() {
        BTestUtils.getProgramFile("lang/statements/returnstmt/too-many-args-to-return-2.bal");
    }

    @Test(description = "Test type mismatch",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "return-type-mismatch-1.bal:2: incompatible types in return statement." +
            " expected 'int', found 'string'")
    public void testInputTypeMismatch1() {
        BTestUtils.getProgramFile("lang/statements/returnstmt/return-type-mismatch-1.bal");
    }

    @Test(description = "Test type mismatch",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "return-type-mismatch-2.bal:2: incompatible types in return statement." +
            " expected 'boolean', found 'int'")
    public void testInputTypeMismatch2() {
        BTestUtils.getProgramFile("lang/statements/returnstmt/return-type-mismatch-2.bal");
    }

    @Test(description = "Test missing return",
          expectedExceptions = {SemanticException.class},
          expectedExceptionsMessageRegExp = "missing-return-stmt-1.bal:1: missing return statement")
    public void testMissingReturnStatement1() {
        BTestUtils.getProgramFile("lang/statements/returnstmt/missing-return-stmt-1.bal");
    }

    @Test(description = "Test missing return",
          expectedExceptions = {SemanticException.class},
          expectedExceptionsMessageRegExp = "missing-return-stmt-2.bal:1: missing return statement")
    public void testMissingReturnStatement2() {
        BTestUtils.getProgramFile("lang/statements/returnstmt/missing-return-stmt-2.bal");
    }

    @Test(description = "Test missing return",
          expectedExceptions = {SemanticException.class},
          expectedExceptionsMessageRegExp = "missing-return-nested-if-1.bal:1: missing return statement")
    public void testMissingReturnInNestedIf1() {
        BTestUtils.getProgramFile("lang/statements/returnstmt/missing-return-nested-if-1.bal");
    }

    @Test(description = "Test missing return",
          expectedExceptions = {SemanticException.class},
          expectedExceptionsMessageRegExp = "missing-return-nested-if-2.bal:1: missing return statement")
    public void testMissingReturnInNestedIf2() {
        BTestUtils.getProgramFile("lang/statements/returnstmt/missing-return-nested-if-2.bal");
    }

    @Test(description = "Test missing return",
          expectedExceptions = {SemanticException.class},
          expectedExceptionsMessageRegExp = "missing-return-nested-if-3.bal:1: missing return statement")
    public void testMissingReturnInNestedIf3() {
        BTestUtils.getProgramFile("lang/statements/returnstmt/missing-return-nested-if-3.bal");
    }

    @Test(description = "Test missing return",
          expectedExceptions = {SemanticException.class},
          expectedExceptionsMessageRegExp = "missing-return-forkjoin-1.bal:1: missing return statement")
    public void testMissingReturnForkJoin1() {
        BTestUtils.getProgramFile("lang/statements/returnstmt/missing-return-forkjoin-1.bal");
    }

    @Test(description = "Test missing return",
          expectedExceptions = {SemanticException.class},
          expectedExceptionsMessageRegExp = "missing-return-forkjoin-2.bal:1: missing return statement")
    public void testMissingReturnForkJoin2() {
        BTestUtils.getProgramFile("lang/statements/returnstmt/missing-return-forkjoin-2.bal");
    }

    @Test(description = "Test unreachable return statement",
          expectedExceptions = {SemanticException.class},
          expectedExceptionsMessageRegExp = "unreachable-stmt-1.bal:21: unreachable statement")
    public void testUnreachableReturnStmt1() {
        BTestUtils.getProgramFile("lang/statements/returnstmt/unreachable-stmt-1.bal");
    }

    @Test(description = "Test unreachable return statement",
          expectedExceptions = {SemanticException.class},
          expectedExceptionsMessageRegExp = "unreachable-stmt-2.bal:12: unreachable statement")
    public void testUnreachableReturnStmt2() {
        BTestUtils.getProgramFile("lang/statements/returnstmt/unreachable-stmt-2.bal");
    }

    @Test(description = "Test unreachable return statement",
          expectedExceptions = {SemanticException.class},
          expectedExceptionsMessageRegExp = "unreachable-stmt-3.bal:15: unreachable statement")
    public void testUnreachableReturnStmt3() {
        BTestUtils.getProgramFile("lang/statements/returnstmt/unreachable-stmt-3.bal");
    }

    @Test(description = "Test unreachable return statement",
          expectedExceptions = {SemanticException.class},
          expectedExceptionsMessageRegExp = "unreachable-stmt-4.bal:16: unreachable statement")
    public void testUnreachableReturnStmt4() {
        BTestUtils.getProgramFile("lang/statements/returnstmt/unreachable-stmt-4.bal");
    }

    @Test(description = "Test unreachable return statement",
          expectedExceptions = {SemanticException.class},
          expectedExceptionsMessageRegExp = "unreachable-stmt-5.bal:17: unreachable statement")
    public void testUnreachableReturnStmt5() {
        BTestUtils.getProgramFile("lang/statements/returnstmt/unreachable-stmt-5.bal");
    }

//    @Test(description = "Test type mismatch",
//            expectedExceptions = {SemanticException.class},
//            expectedExceptionsMessageRegExp = "multi-value-in-single-context.bal:2: multiple-value " +
//                    "split() in single-value context")
//    public void testMultiValueInSingleContext() {
//        ParserUtils.parseBalFile("model/statements/returnstmt/multi-value-in-single-context.bal",
//                globalSymScope);
//    }
}
