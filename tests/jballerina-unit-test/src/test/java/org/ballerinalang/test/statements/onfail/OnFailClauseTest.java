/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.statements.onfail;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains test methods related to the edge cases with on-fail clause.
 *
 * @since 2.0.0
 */

public class OnFailClauseTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/onfail/on-fail-clause.bal");
    }

    @Test(description = "Test on-fail clause edge cases")
    public void testOnFailClause() {
        BRunUtil.invoke(result, "testOnFailEdgeTestcases");
    }

    @Test(description = "Test on-fail clause negative cases - v1")
    public void testOnFailClauseNegativeCaseV1() {
        CompileResult negativeResult = BCompileUtil.compile(
                "test-src/statements/onfail/on-fail-clause-negative.bal");
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "undefined symbol 'i'", 22, 55);
        BAssertUtil.validateError(negativeResult, i++, "incompatible error definition type: " +
                "'SampleError' will not be matched to 'error<record {| string code; anydata...; |}>'", 49, 15);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'SampleError', " +
                "found 'error<record {| string code; anydata...; |}>'", 49, 15);
        BAssertUtil.validateError(negativeResult, i++, "incompatible error definition type: " +
                "'SampleError' will not be matched to 'SampleComplexError'", 56, 15);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'SampleError', " +
                "found 'SampleComplexError'", 56, 15);
        BAssertUtil.validateError(negativeResult, i++, "incompatible error definition type: " +
                "'SampleError' will not be matched to 'int'", 63, 15);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'SampleError', " +
                "found 'int'", 63, 15);
        BAssertUtil.validateError(negativeResult, i++, "invalid error variable; expecting an error " +
                "type but found 'int' in type definition", 63, 15);
        BAssertUtil.validateError(negativeResult, i++, "invalid error variable; expecting an error " +
                "type but found 'int' in type definition", 63, 15);
        BAssertUtil.validateError(negativeResult, i++, "incompatible error definition type: " +
                "'error<record {| string code; anydata...; |}>' will not be matched to " +
                "'error<record {| int code; anydata...; |}>'", 71, 15);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'error<record " +
                "{| string code; anydata...; |}>', found 'error<record {| int code; anydata...; |}>'", 71, 15);
        BAssertUtil.validateError(negativeResult, i++, "incompatible error definition type: 'error' " +
                "will not be matched to 'anydata'", 78, 15);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'error', " +
                "found 'anydata'", 78, 15);
        BAssertUtil.validateError(negativeResult, i++, "invalid error variable; expecting an error " +
                "type but found 'anydata' in type definition", 78, 15);
        BAssertUtil.validateError(negativeResult, i++, "invalid error variable; expecting an error " +
                "type but found 'anydata' in type definition", 78, 15);
        BAssertUtil.validateError(negativeResult, i++, "incompatible error definition type: 'error' will" +
                " not be matched to '[error]'", 85, 15);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'error', " +
                "found '[error]'", 85, 15);
        BAssertUtil.validateError(negativeResult, i++, "invalid binding pattern in 'on fail' clause: " +
                "only a capture binding pattern or an error binding pattern is allowed", 85, 15);
        BAssertUtil.validateError(negativeResult, i++, "invalid error variable; expecting an error " +
                "type but found '[error]' in type definition", 85, 15);
        BAssertUtil.validateError(negativeResult, i++, "invalid binding pattern in 'on fail' clause: " +
                "only a capture binding pattern or an error binding pattern is allowed", 92, 15);
        BAssertUtil.validateError(negativeResult, i++, "invalid list binding pattern: attempted " +
                "to infer a list type, but found 'error'", 92, 15);
        BAssertUtil.validateError(negativeResult, i++, "a wildcard binding pattern can be used only " +
                "with a value that belong to type 'any'", 99, 15);
        BAssertUtil.validateError(negativeResult, i++, "invalid binding pattern in 'on fail' clause: " +
                "only a capture binding pattern or an error binding pattern is allowed", 106, 15);
        BAssertUtil.validateError(negativeResult, i++, "invalid record binding pattern " +
                "with type 'error'", 106, 15);
        BAssertUtil.validateError(negativeResult, i++, "unknown error detail arg 'cause' passed to " +
                "closed error detail type 'SampleComplexErrorData'", 114, 66);
        BAssertUtil.validateError(negativeResult, i++, "invalid error variable; expecting an error " +
                "type but found '(SampleComplexError|SampleError)' in type definition", 117, 15);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @Test(description = "Test on-fail clause negative cases - v2")
    public void testOnFailClauseNegativeCaseV2() {
        CompileResult negativeResult = BCompileUtil.compile(
                "test-src/statements/onfail/on-fail-clause-negative-v2.bal");

        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "unreachable code", 21, 9);
        BAssertUtil.validateError(negativeResult, i++, "unreachable code", 23, 5);
        BAssertUtil.validateError(negativeResult, i++, "this function must return a result", 32, 1);
        BAssertUtil.validateError(negativeResult, i++, "this function must return a result", 48, 1);
        BAssertUtil.validateError(negativeResult, i++, "this function must return a result", 66, 1);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
