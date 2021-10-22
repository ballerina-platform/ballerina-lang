/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.dataflow.analysis;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases related to data flow analysis.
 *
 * @since 0.985.0
 */
@Test
public class DataflowAnalysisTest {

    @Test(description = "Test uninitialized variables")
    public void testSemanticsOfUninitializedVariables() {
        CompileResult result = BCompileUtil.compile(
                "test-src/dataflow/analysis/dataflow-analysis-semantics-negative.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "undefined field 'f' in object 'Foo'", 308, 14);
        BAssertUtil.validateError(result, i++, "missing non-defaultable required record field 'extra'", 548, 12);
        Assert.assertEquals(result.getErrorCount(), i);
    }

    @Test(description = "Test uninitialized variables")
    public void testUninitializedVariables() {
        CompileResult result = BCompileUtil.compile("test-src/dataflow/analysis/dataflow-analysis-negative.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 57, 12);
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 76, 12);
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 91, 12);
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 108, 12);
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 125, 12);
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 231, 12);
        BAssertUtil.validateError(result, i++, "variable 's' is not initialized", 238, 21);
        BAssertUtil.validateError(result, i++, "variable 'a' is not initialized", 241, 13);
        BAssertUtil.validateError(result, i++, "variable 'a' is not initialized", 244, 9);
        BAssertUtil.validateError(result, i++, "variable 's' is not initialized", 249, 24);
        BAssertUtil.validateError(result, i++, "variable 'm' is not initialized", 261, 24);
        BAssertUtil.validateError(result, i++, "variable 'm' is not initialized", 262, 9);
        BAssertUtil.validateError(result, i++, "variable 'm' is not initialized", 284, 9);
        BAssertUtil.validateError(result, i++, "variable 's' is not initialized", 284, 11);
        BAssertUtil.validateError(result, i++, "variable 'm' is not initialized", 287, 9);
        BAssertUtil.validateError(result, i++, "variable 'm' is not initialized", 287, 9);
        BAssertUtil.validateError(result, i++, "variable 's' is not initialized", 287, 18);
        BAssertUtil.validateError(result, i++, "variable 's' is not initialized", 288, 12);
        BAssertUtil.validateError(result, i++, "variable 's' is not initialized", 288, 15);
        BAssertUtil.validateError(result, i++, "variable 'msg' is not initialized", 311, 20);
        BAssertUtil.validateError(result, i++, "uninitialized field 'd'", 325, 5);
        BAssertUtil.validateError(result, i++, "variable 'd' is not initialized", 350, 16);
        BAssertUtil.validateWarning(result, i++, "concurrent calls will not be made to this method since the method " +
                "is not an 'isolated' method", 393, 5);
        BAssertUtil.validateWarning(result, i++, "concurrent calls will not be made to this method since the method " +
                "is not an 'isolated' method", 399, 5);
        BAssertUtil.validateError(result, i++, "variable 'a' is not initialized", 408, 5);
        BAssertUtil.validateError(result, i++, "variable 'a' is not initialized", 429, 13);
        BAssertUtil.validateError(result, i++, "variable 'b' may not have been initialized", 429, 16);
        BAssertUtil.validateError(result, i++, "uninitialized field 'a'", 457, 5);
        BAssertUtil.validateError(result, i++, "uninitialized field 'c'", 459, 5);
        BAssertUtil.validateWarning(result, i++, "concurrent calls will not be made to this method since the method " +
                "is not an 'isolated' method", 478, 5);
        BAssertUtil.validateWarning(result, i++, "concurrent calls will not be made to this method since the method " +
                "is not an 'isolated' method", 493, 5);
        BAssertUtil.validateError(result, i++, "unreachable code", 521, 9);
        BAssertUtil.validateError(result, i++, "variable 'msg' is not initialized", 530, 12);
        BAssertUtil.validateError(result, i++, "unreachable code", 539, 9);
        BAssertUtil.validateError(result, i++, "uninitialized field 'a'", 552, 5);
        BAssertUtil.validateError(result, i++, "uninitialized field 'c'", 554, 5);
        BAssertUtil.validateError(result, i++, "uninitialized field 'b'", 606, 5);
        BAssertUtil.validateError(result, i++, "uninitialized field 'c'", 607, 5);
        BAssertUtil.validateError(result, i++, "uninitialized field 's'", 615, 22);
        BAssertUtil.validateError(result, i++, "variable 'k' may not have been initialized", 625, 12);
        BAssertUtil.validateError(result, i++, "variable 'k' may not have been initialized", 647, 12);
        BAssertUtil.validateError(result, i++, "variable 'k' may not have been initialized", 657, 12);
        BAssertUtil.validateError(result, i++, "variable 'k' may not have been initialized", 679, 12);
        BAssertUtil.validateError(result, i++, "variable 'k' may not have been initialized", 702, 12);
        BAssertUtil.validateError(result, i++, "unreachable code", 708, 9);
        BAssertUtil.validateError(result, i++, "unreachable code", 711, 13);
        BAssertUtil.validateError(result, i++, "variable 'a' may not have been initialized", 715, 13);
        BAssertUtil.validateError(result, i++, "unreachable code", 719, 9);
        BAssertUtil.validateError(result, i++, "unreachable code", 722, 13);
        BAssertUtil.validateError(result, i++, "variable 'b' may not have been initialized", 726, 13);
        BAssertUtil.validateError(result, i++, "variable 'a' may not have been initialized", 741, 13);
        BAssertUtil.validateError(result, i++, "variable 'b' is not initialized", 742, 16);
        BAssertUtil.validateError(result, i++, "variable 'a' is not initialized", 751, 13);
        BAssertUtil.validateError(result, i++, "variable 'i' is not initialized", 756, 28);
        BAssertUtil.validateError(result, i++, "variable 'i' is not initialized", 756, 41);
        Assert.assertEquals(result.getErrorCount(), i - 4);
        Assert.assertEquals(result.getWarnCount(), 4);
    }

    @Test(description = "Test uninitialized variables in error-constructor-expr")
    public void testUninitializedVariablesInErrorConstructorExpr() {
        CompileResult result = BCompileUtil.compile("test-src/dataflow/analysis/dataflow-analysis-error-constructor" +
                "-expr.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "variable 'message' is not initialized", 21, 24);
        BAssertUtil.validateError(result, i++, "variable 'message' is not initialized", 22, 32);
        BAssertUtil.validateError(result, i++, "variable 'x' is not initialized", 22, 45);
        BAssertUtil.validateError(result, i++, "variable 'message' is not initialized", 24, 49);
        BAssertUtil.validateError(result, i++, "variable 'x' is not initialized", 24, 62);
        Assert.assertEquals(result.getErrorCount(), i);
    }

    @Test(description = "Test uninitialized local complex variable")
    public void testUninitializedLocalTupleVar() {
        CompileResult result =
                BCompileUtil.compile("test-src/dataflow/analysis/uninitialized_local_complex_variables.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "variable declaration having binding pattern must be initialized",
                18, 46);
        BAssertUtil.validateError(result, i++, "variable declaration having binding pattern must be initialized",
                31, 62);
        BAssertUtil.validateError(result, i++, "variable declaration having binding pattern must be initialized",
                44, 83);
        Assert.assertEquals(result.getErrorCount(), i);
    }
}
