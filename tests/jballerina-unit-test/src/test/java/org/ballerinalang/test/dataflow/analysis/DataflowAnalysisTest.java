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

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases related to data flow analysis.
 *
 * @since 0.985.0
 */
@Test
public class DataflowAnalysisTest {

    @Test(description = "Test uninitialized variables", enabled = false)
    public void testSemanticsOfUninitializedVariables() {
        CompileResult result = BCompileUtil.compile(
                "test-src/dataflow/analysis/dataflow-analysis-semantics-negative.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "undefined field 'f' in object 'Foo'", 308, 13);
        BAssertUtil.validateError(result, i++, "missing non-defaultable required record field 'extra'", 548, 12);
        Assert.assertEquals(result.getErrorCount(), i);
    }

    @Test(description = "Test uninitialized variables", enabled = false)
    public void testUninitializedVariables() {
        CompileResult result = BCompileUtil.compile("test-src/dataflow/analysis/dataflow-analysis-negative.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 53, 12);
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 70, 12);
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 83, 12);
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 98, 12);
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 113, 12);
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 209, 12);
        BAssertUtil.validateError(result, i++, "variable 's' is not initialized", 216, 21);
        BAssertUtil.validateError(result, i++, "variable 'a' is not initialized", 219, 13);
        BAssertUtil.validateError(result, i++, "variable 'a' is not initialized", 222, 9);
        BAssertUtil.validateError(result, i++, "variable 's' is not initialized", 226, 24);
        BAssertUtil.validateError(result, i++, "variable 'm' is not initialized", 238, 24);
        BAssertUtil.validateError(result, i++, "variable 'm' is not initialized", 239, 9);
        BAssertUtil.validateError(result, i++, "variable 'm' is not initialized", 261, 9);
        BAssertUtil.validateError(result, i++, "variable 's' is not initialized", 261, 11);
        BAssertUtil.validateError(result, i++, "variable 'm' is not initialized", 264, 9);
        BAssertUtil.validateError(result, i++, "variable 'm' is not initialized", 264, 9);
        BAssertUtil.validateError(result, i++, "variable 's' is not initialized", 264, 18);
        BAssertUtil.validateError(result, i++, "variable 's' is not initialized", 265, 12);
        BAssertUtil.validateError(result, i++, "variable 's' is not initialized", 265, 15);
        BAssertUtil.validateError(result, i++, "variable 'msg' is not initialized", 286, 20);
        BAssertUtil.validateError(result, i++, "uninitialized field 'd'", 300, 5);
        BAssertUtil.validateError(result, i++, "variable 'd' is not initialized", 325, 16);
        BAssertUtil.validateError(result, i++, "variable 'a' is not initialized", 381, 5);
        BAssertUtil.validateError(result, i++, "variable 'a' is not initialized", 400, 13);
        BAssertUtil.validateError(result, i++, "variable 'b' may not have been initialized", 400, 16);
        BAssertUtil.validateError(result, i++, "uninitialized field 'a'", 428, 5);
        BAssertUtil.validateError(result, i++, "uninitialized field 'c'", 430, 5);
        BAssertUtil.validateError(result, i++, "unreachable code", 489, 9);
        BAssertUtil.validateError(result, i++, "variable 'msg' is not initialized", 498, 12);
        BAssertUtil.validateError(result, i++, "unreachable code", 506, 9);
        BAssertUtil.validateError(result, i++, "uninitialized field 'a'", 519, 5);
        BAssertUtil.validateError(result, i++, "uninitialized field 'c'", 521, 5);
        BAssertUtil.validateError(result, i++, "uninitialized field 'b'", 573, 5);
        BAssertUtil.validateError(result, i++, "uninitialized field 'c'", 574, 5);
        BAssertUtil.validateError(result, i++, "uninitialized field 's'", 582, 14);
        BAssertUtil.validateError(result, i++, "variable 'k' may not have been initialized", 592, 12);
        BAssertUtil.validateError(result, i++, "variable 'k' may not have been initialized", 614, 12);
        BAssertUtil.validateError(result, i++, "variable 'k' may not have been initialized", 624, 12);
        BAssertUtil.validateError(result, i++, "variable 'k' may not have been initialized", 646, 12);
        BAssertUtil.validateError(result, i++, "variable 'k' may not have been initialized", 669, 12);
        BAssertUtil.validateError(result, i++, "variable 'a' may not have been initialized", 682, 13);
        BAssertUtil.validateError(result, i++, "variable 'b' may not have been initialized", 693, 13);
        BAssertUtil.validateError(result, i++, "variable 'a' may not have been initialized", 708, 13);
        BAssertUtil.validateError(result, i++, "variable 'b' is not initialized", 709, 16);
        BAssertUtil.validateError(result, i++, "variable 'a' is not initialized", 718, 13);
        BAssertUtil.validateError(result, i++, "variable 'i' is not initialized", 723, 28);
        BAssertUtil.validateError(result, i++, "variable 'i' is not initialized", 723, 41);
        Assert.assertEquals(result.getErrorCount(), i);
    }
}
