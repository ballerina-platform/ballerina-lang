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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases related to data flow analysis.
 *
 * @since 0.985.0
 */
public class DataflowAnalysisTest {

    @Test(description = "Test uninitialized variables")
    public void testUninitializedVariables() {
        CompileResult result = BCompileUtil.compile("test-src/dataflow/analysis/dataflow-analysis-negative.bal");
        Assert.assertEquals(result.getErrorCount(), 53);
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
        BAssertUtil.validateError(result, i++, "variable 'm' is not initialized", 262, 9);
        BAssertUtil.validateError(result, i++, "variable 's' is not initialized", 262, 11);
        BAssertUtil.validateError(result, i++, "variable 'm' is not initialized", 265, 9);
        BAssertUtil.validateError(result, i++, "variable 's' is not initialized", 265, 18);
        BAssertUtil.validateError(result, i++, "variable 's' is not initialized", 266, 18);
        BAssertUtil.validateError(result, i++, "variable 's' is not initialized", 266, 21);
        BAssertUtil.validateError(result, i++, "variable 'x' is not initialized", 270, 9);
        BAssertUtil.validateError(result, i++, "variable 's' is not initialized", 270, 12);
        BAssertUtil.validateError(result, i++, "variable 'msg' is not initialized", 290, 20);
        BAssertUtil.validateError(result, i++, "variable 'globalVar' is not initialized", 298, 1);
        BAssertUtil.validateError(result, i++, "variable 'globalVar' is not initialized", 305, 12);
        BAssertUtil.validateError(result, i++, "variable 'globalVar' is not initialized", 309, 13);
        BAssertUtil.validateError(result, i++, "uninitialized field 'd'", 312, 5);
        BAssertUtil.validateError(result, i++, "variable 'globalVar' is not initialized", 316, 18);
        BAssertUtil.validateError(result, i++, "undefined field 'f' in object 'Foo'", 320, 9);
        BAssertUtil.validateError(result, i++, "variable 'globalVar' is not initialized", 324, 16);
        BAssertUtil.validateError(result, i++, "variable 'd' is not initialized", 341, 16);
        BAssertUtil.validateError(result, i++, "variable 'val' may not have been initialized", 372, 12);
        BAssertUtil.validateError(result, i++, "variable 'x' is not initialized", 383, 20);
        BAssertUtil.validateError(result, i++, "variable 'x' is not initialized", 389, 20);
        BAssertUtil.validateError(result, i++, "variable 'yyy' is not initialized", 398, 20);
        BAssertUtil.validateError(result, i++, "variable 'yyy' is not initialized", 404, 20);
        BAssertUtil.validateError(result, i++, "variable 'a' is not initialized", 414, 5);
        BAssertUtil.validateError(result, i++, "variable 'a' is not initialized", 433, 13);
        BAssertUtil.validateError(result, i++, "variable 'b' may not have been initialized", 433, 16);
        BAssertUtil.validateError(result, i++, "uninitialized field 'a'", 461, 5);
        BAssertUtil.validateError(result, i++, "uninitialized field 'c'", 463, 5);
        BAssertUtil.validateError(result, i++, "variable 'publicGlobalVar_1' is not initialized", 469, 1);
        BAssertUtil.validateError(result, i++, "variable 'a' is not initialized", 500, 17);
        BAssertUtil.validateError(result, i++, "unreachable code", 525, 9);
        BAssertUtil.validateError(result, i++, "variable 'msg' is not initialized", 534, 12);
        BAssertUtil.validateError(result, i++, "unreachable code", 542, 9);
        BAssertUtil.validateError(result, i++, "uninitialized field 'a'", 555, 5);
        BAssertUtil.validateError(result, i++, "uninitialized field 'c'", 557, 5);
        BAssertUtil.validateError(result, i++, "missing non-defaultable required record field 'extra'", 585, 12);
        BAssertUtil.validateError(result, i++, "variable 'fa' is not initialized", 611, 13);
        BAssertUtil.validateError(result, i++, "variable 'fb' is not initialized", 612, 13);
        BAssertUtil.validateError(result, i++, "uninitialized field 'b'", 620, 5);
        BAssertUtil.validateError(result, i++, "uninitialized field 'c'", 621, 5);
        BAssertUtil.validateError(result, i, "uninitialized field 's'", 631, 14);
    }
}
