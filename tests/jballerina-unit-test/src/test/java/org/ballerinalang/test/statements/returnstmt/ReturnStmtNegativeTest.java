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

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for return statement negative cases.
 */
public class ReturnStmtNegativeTest {

    @Test(description = "Test not enough arguments to return")
    public void testNotEnoughArgsToReturn1() {
        CompileResult result = BCompileUtil.compile("test-src/statements/returnstmt/not-enough-args-to-return-1.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "incompatible types: expected '[int,int]', found '()'", 2, 5);
    }

    @Test(description = "Test not enough arguments to return")
    public void testNotEnoughArgsToReturn2() {
        CompileResult result = BCompileUtil.compile("test-src/statements/returnstmt/not-enough-args-to-return-2.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "incompatible types: expected '[string,string]', found 'string'", 2, 12);
    }

    @Test(description = "Test not enough arguments to return", groups = { "disableOnOldParser" })
    public void testNotEnoughArgsToReturn3() {
        CompileResult result = BCompileUtil.compile("test-src/statements/returnstmt/not-enough-args-to-return-3.bal");
        Assert.assertEquals(result.getErrorCount(), 3);
        BAssertUtil.validateError(result, 0, "incompatible types: expected '[string,string,int]', found 'string'", 2,
                12);
        BAssertUtil.validateError(result, 1, "invalid token '\"sameera\"'", 2, 31);
        BAssertUtil.validateError(result, 2, "invalid token ','", 2, 31);
    }

    @Test(description = "Test too many arguments to return", groups = { "disableOnOldParser" })
    public void testTooManyArgsToReturn1() {
        CompileResult result = BCompileUtil.compile("test-src/statements/returnstmt/too-many-args-to-return-1.bal");
        Assert.assertEquals(result.getErrorCount(), 2);
        BAssertUtil.validateError(result, 0, "invalid token '\"sameera\"'", 2, 31);
        BAssertUtil.validateError(result, 1, "invalid token ','", 2, 31);
    }

    @Test(description = "Test too many arguments to return")
    public void testTooManyArgsToReturn2() {
        CompileResult result = BCompileUtil.compile("test-src/statements/returnstmt/too-many-args-to-return-2.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "incompatible types: expected 'string', found '[string,string]'", 2, 12);
    }

    @Test(description = "Test type mismatch")
    public void testInputTypeMismatch1() {
        CompileResult result = BCompileUtil.compile("test-src/statements/returnstmt/return-type-mismatch-1.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result,
                0, "incompatible types: expected '[string,int,string]', found '[string,string,string]'",
                2, 12);
    }

    @Test(description = "Test type mismatch")
    public void testInputTypeMismatch2() {
        CompileResult result = BCompileUtil.compile("test-src/statements/returnstmt/return-type-mismatch-2.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "incompatible types: expected 'boolean', found 'int'", 2, 26);
    }

    @Test(description = "Test missing return")
    public void testMissingReturnStatement1() {
        CompileResult result = BCompileUtil.compile("test-src/statements/returnstmt/missing-return-stmt-1.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "this function must return a result", 1, 1);
    }

    @Test(description = "Test missing return")
    public void testMissingReturnStatement2() {
        CompileResult result = BCompileUtil.compile("test-src/statements/returnstmt/missing-return-stmt-2.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "this function must return a result", 1, 1);
    }

    @Test(description = "Test missing return")
    public void testMissingReturnInNestedIf1() {
        CompileResult result = BCompileUtil.compile("test-src/statements/returnstmt/missing-return-nested-if-1.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "this function must return a result", 1, 1);
    }

    @Test(description = "Test missing return")
    public void testMissingReturnInNestedIf2() {
        CompileResult result = BCompileUtil.compile("test-src/statements/returnstmt/missing-return-nested-if-2.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "this function must return a result", 1, 1);
    }

    @Test(description = "Test missing return")
    public void testMissingReturnInNestedIf3() {
        CompileResult result = BCompileUtil.compile("test-src/statements/returnstmt/missing-return-nested-if-3.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "this function must return a result", 1, 1);
    }

    @Test(description = "Test missing return")
    public void testMissingReturnForkJoin1() {
        CompileResult result = BCompileUtil.compile("test-src/statements/returnstmt/missing-return-forkjoin-1.bal");
        Assert.assertEquals(result.getErrorCount(), 2);
        BAssertUtil.validateError(result, 0, "this function must return a result", 1, 1);
        BAssertUtil.validateError(result, 1, "this function must return a result", 4, 32);
    }

    @Test
    public void testMissingReturnAfterLoops() {
        CompileResult result = BCompileUtil.compile("test-src/statements/returnstmt" +
                                                            "/missing_return_after_loops_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 2);
        BAssertUtil.validateError(result, 0, "this function must return a result", 17, 1);
        BAssertUtil.validateError(result, 1, "this function must return a result", 23, 1);
    }

    @Test(description = "Test missing return")
    public void testMissingReturnForkJoin2() {
        CompileResult result = BCompileUtil.compile("test-src/statements/returnstmt/missing-return-forkjoin-2.bal");
        Assert.assertEquals(result.getErrorCount(), 3);
        BAssertUtil.validateError(result, 0, "this function must return a result", 1, 1);
        BAssertUtil.validateError(result, 1, "this function must return a result", 4, 32);
        BAssertUtil.validateError(result, 2, "this function must return a result", 8, 32);
    }

    @Test(description = "Test unreachable return statement")
    public void testUnreachableReturnStmt1() {
        CompileResult result = BCompileUtil.compile("test-src/statements/returnstmt/unreachable-stmt-1.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "unreachable code", 21, 5);
    }

    @Test(description = "Test unreachable return statement")
    public void testUnreachableReturnStmt2() {
        CompileResult result = BCompileUtil.compile("test-src/statements/returnstmt/unreachable-stmt-2.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "unreachable code", 12, 9);
    }

    @Test(description = "Test unreachable return statement")
    public void testUnreachableReturnStmt3() {
        CompileResult result = BCompileUtil.compile("test-src/statements/returnstmt/unreachable-stmt-3.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "unreachable code", 15, 13);
    }

    @Test(description = "Test unreachable return statement")
    public void testUnreachableReturnStmt4() {
        CompileResult result = BCompileUtil.compile("test-src/statements/returnstmt/unreachable-stmt-4.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "unreachable code", 16, 9);
    }

    @Test(description = "Test unreachable return statement")
    public void testUnreachableReturnStmt5() {
        CompileResult result = BCompileUtil.compile("test-src/statements/returnstmt/unreachable-stmt-5.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "unreachable code", 17, 5);
    }

    @Test(description = "Test type mismatch")
    public void testMultiValueInSingleContext() {
        CompileResult result = BCompileUtil.compile("test-src/statements/returnstmt/multi-value-in-single-context.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "incompatible types: expected 'string', found '[string,int]'", 2, 13);
    }

    @Test(description = "Test return statement in resource with mismatching types", enabled = false)
    public void testReturnInResourceWithMismatchingTypes() {
        CompileResult result = BCompileUtil.compile("test-src/statements/returnstmt/return-in-resource-with-" +
                "mismatching-types.bal");
        Assert.assertEquals(result.getErrorCount(), 3);
        BAssertUtil.validateError(result, 0, "incompatible types: expected 'error?', found 'int'", 22, 16);
        BAssertUtil.validateError(result, 1, "incompatible types: expected '()', found 'string'", 26, 16);
        BAssertUtil.validateError(result, 2, "incompatible types: expected '()', found 'string'", 30, 16);
    }
}
