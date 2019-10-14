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
public class DataflowAnalysisTest {

    @Test(description = "Test uninitialized variables")
    public void testSemanticsOfUninitializedVariables() {
        CompileResult result = BCompileUtil.compile(
                "test-src/dataflow/analysis/dataflow-analysis-semantics-negative.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "undefined field 'f' in object 'Foo'", 312, 13);
        BAssertUtil.validateError(result, i++, "missing non-defaultable required record field 'extra'", 552, 12);
        Assert.assertEquals(result.getErrorCount(), i);
    }

    @Test(description = "Test uninitialized variables")
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
        BAssertUtil.validateError(result, i++, "variable 'x' is not initialized", 269, 9);
        BAssertUtil.validateError(result, i++, "variable 's' is not initialized", 269, 12);
        BAssertUtil.validateError(result, i++, "variable 'msg' is not initialized", 290, 20);
        BAssertUtil.validateError(result, i++, "uninitialized field 'd'", 304, 5);
        BAssertUtil.validateError(result, i++, "variable 'd' is not initialized", 329, 16);
        BAssertUtil.validateError(result, i++, "variable 'a' is not initialized", 385, 5);
        BAssertUtil.validateError(result, i++, "variable 'a' is not initialized", 404, 13);
        BAssertUtil.validateError(result, i++, "variable 'b' may not have been initialized", 404, 16);
        BAssertUtil.validateError(result, i++, "uninitialized field 'a'", 432, 5);
        BAssertUtil.validateError(result, i++, "uninitialized field 'c'", 434, 5);
        BAssertUtil.validateError(result, i++, "unreachable code", 493, 9);
        BAssertUtil.validateError(result, i++, "variable 'msg' is not initialized", 502, 12);
        BAssertUtil.validateError(result, i++, "unreachable code", 510, 9);
        BAssertUtil.validateError(result, i++, "uninitialized field 'a'", 523, 5);
        BAssertUtil.validateError(result, i++, "uninitialized field 'c'", 525, 5);
        BAssertUtil.validateError(result, i++, "uninitialized field 'b'", 577, 5);
        BAssertUtil.validateError(result, i++, "uninitialized field 'c'", 578, 5);
        BAssertUtil.validateError(result, i++, "uninitialized field 's'", 586, 14);
        BAssertUtil.validateError(result, i++, "variable 'k' may not have been initialized", 596, 12);
        BAssertUtil.validateError(result, i++, "variable 'k' may not have been initialized", 618, 12);
        BAssertUtil.validateError(result, i++, "variable 'k' may not have been initialized", 628, 12);
        BAssertUtil.validateError(result, i++, "variable 'k' may not have been initialized", 650, 12);
        BAssertUtil.validateError(result, i++, "variable 'k' may not have been initialized", 673, 12);
        Assert.assertEquals(result.getErrorCount(), i);
    }
}
