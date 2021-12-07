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
        BAssertUtil.validateWarning(result, i++, "unused variable 'e'", 238, 5);
        BAssertUtil.validateError(result, i++, "variable 's' is not initialized", 238, 21);
        BAssertUtil.validateError(result, i++, "variable 'a' is not initialized", 241, 13);
        BAssertUtil.validateError(result, i++, "variable 'a' is not initialized", 244, 9);
        BAssertUtil.validateError(result, i++, "variable 's' is not initialized", 249, 24);
        BAssertUtil.validateWarning(result, i++, "unused variable 'val'", 261, 13);
        BAssertUtil.validateError(result, i++, "variable 'm' is not initialized", 261, 24);
        BAssertUtil.validateError(result, i++, "variable 'm' is not initialized", 262, 9);
        BAssertUtil.validateWarning(result, i++, "unused variable 'str'", 271, 5);
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
        BAssertUtil.validateWarning(result, i++, "unused variable 'y'", 380, 9);
        BAssertUtil.validateWarning(result, i++, "concurrent calls will not be made to this method since the method " +
                "is not an 'isolated' method", 393, 5);
        BAssertUtil.validateWarning(result, i++, "unused variable 'a'", 394, 9);
        BAssertUtil.validateWarning(result, i++, "concurrent calls will not be made to this method since the method " +
                "is not an 'isolated' method", 399, 5);
        BAssertUtil.validateWarning(result, i++, "unused variable 'a'", 400, 9);
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
        BAssertUtil.validateWarning(result, i++, "unused variable 'theMap'", 574, 5);
        BAssertUtil.validateError(result, i++, "uninitialized field 'b'", 606, 5);
        BAssertUtil.validateError(result, i++, "uninitialized field 'c'", 607, 5);
        BAssertUtil.validateError(result, i++, "uninitialized field 's'", 615, 22);
        BAssertUtil.validateError(result, i++, "variable 'k' may not have been initialized", 625, 12);
        BAssertUtil.validateError(result, i++, "variable 'k' may not have been initialized", 647, 12);
        BAssertUtil.validateWarning(result, i++, "unused variable 'a'", 654, 9);
        BAssertUtil.validateWarning(result, i++, "unused variable 'b'", 654, 9);
        BAssertUtil.validateWarning(result, i++, "unused variable 'a'", 655, 9);
        BAssertUtil.validateWarning(result, i++, "unused variable 'b'", 655, 9);
        BAssertUtil.validateError(result, i++, "variable 'k' may not have been initialized", 657, 12);
        BAssertUtil.validateWarning(result, i++, "unused variable 'a'", 664, 9);
        BAssertUtil.validateWarning(result, i++, "unused variable 'b'", 664, 9);
        BAssertUtil.validateWarning(result, i++, "unused variable 'a'", 665, 9);
        BAssertUtil.validateWarning(result, i++, "unused variable 'b'", 665, 9);
        BAssertUtil.validateWarning(result, i++, "unused variable 'x'", 666, 9);
        BAssertUtil.validateWarning(result, i++, "unused variable 'a'", 675, 9);
        BAssertUtil.validateWarning(result, i++, "unused variable 'b'", 675, 9);
        BAssertUtil.validateWarning(result, i++, "unused variable 'a'", 676, 9);
        BAssertUtil.validateWarning(result, i++, "unused variable 'b'", 676, 9);
        BAssertUtil.validateWarning(result, i++, "unused variable 'x'", 677, 9);
        BAssertUtil.validateError(result, i++, "variable 'k' may not have been initialized", 679, 12);
        BAssertUtil.validateError(result, i++, "variable 'k' may not have been initialized", 702, 12);
        BAssertUtil.validateError(result, i++, "unreachable code", 708, 9);
        BAssertUtil.validateError(result, i++, "unreachable code", 711, 13);
        BAssertUtil.validateError(result, i++, "variable 'a' may not have been initialized", 715, 13);
        BAssertUtil.validateError(result, i++, "unreachable code", 719, 9);
        BAssertUtil.validateError(result, i++, "unreachable code", 722, 13);
        BAssertUtil.validateWarning(result, i++, "unused variable 'k'", 726, 5);
        BAssertUtil.validateError(result, i++, "variable 'b' may not have been initialized", 726, 13);
        BAssertUtil.validateWarning(result, i++, "unused variable 'j'", 741, 5);
        BAssertUtil.validateError(result, i++, "variable 'a' may not have been initialized", 741, 13);
        BAssertUtil.validateError(result, i++, "variable 'b' is not initialized", 742, 16);
        BAssertUtil.validateWarning(result, i++, "unused variable 'j'", 751, 5);
        BAssertUtil.validateError(result, i++, "variable 'a' is not initialized", 751, 13);
        BAssertUtil.validateError(result, i++, "variable 'i' is not initialized", 756, 28);
        BAssertUtil.validateError(result, i++, "variable 'i' is not initialized", 756, 41);
        BAssertUtil.validateError(result, i++, "variable 'i' is not initialized", 762, 7);
        BAssertUtil.validateError(result, i++, "variable 'i' is not initialized", 763, 15);
        BAssertUtil.validateError(result, i++, "variable 'n' is not initialized", 766, 5);
        BAssertUtil.validateError(result, i++, "variable 'i' is not initialized", 766, 7);
        BAssertUtil.validateError(result, i++, "variable 'n' is not initialized", 767, 13);
        BAssertUtil.validateError(result, i++, "variable 'i' is not initialized", 767, 15);
        BAssertUtil.validateError(result, i++, "variable 'f1' is not initialized", 776, 5);
        BAssertUtil.validateError(result, i++, "variable 'f2' is not initialized", 785, 5);
        BAssertUtil.validateError(result, i++, "variable 'i' is not initialized", 785, 8);
        BAssertUtil.validateError(result, i++, "variable 'j' is not initialized", 785, 11);
        BAssertUtil.validateError(result, i++, "variable 'k' is not initialized", 785, 17);
        BAssertUtil.validateError(result, i++, "variable 'f2' is not initialized", 791, 5);
        BAssertUtil.validateError(result, i++, "variable 'i' is not initialized", 793, 8);
        BAssertUtil.validateError(result, i++, "variable 'j' is not initialized", 793, 11);
        BAssertUtil.validateError(result, i++, "variable 'k' is not initialized", 793, 17);
        BAssertUtil.validateError(result, i++, "variable 'b' is not initialized", 796, 5);
        BAssertUtil.validateError(result, i++, "variable 'i' is not initialized", 796, 10);
        BAssertUtil.validateError(result, i++, "variable 'b' is not initialized", 797, 5);
        BAssertUtil.validateError(result, i++, "variable 'i' is not initialized", 827, 18);
        BAssertUtil.validateError(result, i++, "variable 'anydataArr' is not initialized", 827, 24);
        BAssertUtil.validateError(result, i++, "variable 'fn2' is not initialized", 829, 19);
        BAssertUtil.validateWarning(result, i++, "unused variable 'x'", 831, 9);
        BAssertUtil.validateWarning(result, i++, "unused variable 'y'", 831, 9);
        BAssertUtil.validateError(result, i++, "variable 'i' is not initialized", 831, 23);
        BAssertUtil.validateWarning(result, i++, "unused variable 'a1'", 842, 5);
        BAssertUtil.validateError(result, i++, "variable 'j' is not initialized", 842, 23);
        BAssertUtil.validateError(result, i++, "variable 'j' is not initialized", 843, 23);
        BAssertUtil.validateWarning(result, i++, "unused variable 'a2'", 846, 5);
        BAssertUtil.validateError(result, i++, "variable 't1' is not initialized", 854, 9);
        BAssertUtil.validateError(result, i++, "variable 'condition' is not initialized", 866, 8);
        BAssertUtil.validateError(result, i++, "variable 'condition' is not initialized", 870, 16);
        BAssertUtil.validateError(result, i++, "variable 'a' is not initialized", 870, 28);
        BAssertUtil.validateError(result, i++, "variable 'b' may not have been initialized", 870, 32);
        BAssertUtil.validateError(result, i++, "variable 'i' may not have been initialized", 886, 13);
        BAssertUtil.validateError(result, i++, "variable 'i' may not have been initialized", 897, 13);
        BAssertUtil.validateError(result, i++, "variable 'i' may not have been initialized", 908, 13);
        BAssertUtil.validateError(result, i++, "variable 'i' may not have been initialized", 919, 13);
        BAssertUtil.validateError(result, i++, "unreachable code", 929, 5);
        BAssertUtil.validateError(result, i++, "unreachable code", 936, 9);
        BAssertUtil.validateError(result, i++, "variable 'i' may not have been initialized", 939, 13);
        BAssertUtil.validateError(result, i++, "variable 'i' may not have been initialized", 950, 13);

        Assert.assertEquals(result.getErrorCount(), i - 32);
        Assert.assertEquals(result.getWarnCount(), 32);
    }

    @Test(description = "Test uninitialized variables in error-constructor-expr")
    public void testUninitializedVariablesInErrorConstructorExpr() {
        CompileResult result = BCompileUtil.compile("test-src/dataflow/analysis/dataflow-analysis-error-constructor" +
                "-expr.bal");
        int i = 0;
        BAssertUtil.validateWarning(result, i++, "unused variable 'err1'", 21, 5);
        BAssertUtil.validateError(result, i++, "variable 'message' is not initialized", 21, 24);
        BAssertUtil.validateWarning(result, i++, "unused variable 'err2'", 22, 5);
        BAssertUtil.validateError(result, i++, "variable 'message' is not initialized", 22, 32);
        BAssertUtil.validateError(result, i++, "variable 'x' is not initialized", 22, 45);
        BAssertUtil.validateWarning(result, i++, "unused variable 'err3'", 24, 5);
        BAssertUtil.validateError(result, i++, "variable 'message' is not initialized", 24, 49);
        BAssertUtil.validateError(result, i++, "variable 'x' is not initialized", 24, 62);
        Assert.assertEquals(result.getErrorCount(), i - 3);
        Assert.assertEquals(result.getWarnCount(), 3);
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
