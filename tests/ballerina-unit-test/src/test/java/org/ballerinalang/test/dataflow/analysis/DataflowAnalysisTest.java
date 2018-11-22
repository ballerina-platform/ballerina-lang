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
        Assert.assertEquals(result.getErrorCount(), 48);
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
        BAssertUtil.validateError(result, i++, "variable 'm' is not initialized", 238, 20);
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
        BAssertUtil.validateError(result, i++, "undefined field 'f' in object 'Foo'", 315, 18);
        BAssertUtil.validateError(result, i++, "variable 'globalVar' is not initialized", 316, 18);
        BAssertUtil.validateError(result, i++, "variable 'globalVar' is not initialized", 321, 16);
        BAssertUtil.validateError(result, i++, "variable 'd' is not initialized", 338, 16);
        BAssertUtil.validateError(result, i++, "variable 'val' may not have been initialized", 381, 12);
        BAssertUtil.validateError(result, i++, "variable 'val' may not have been initialized", 417, 12);
        BAssertUtil.validateError(result, i++, "variable 'val' may not have been initialized", 465, 12);
        BAssertUtil.validateError(result, i++, "variable 'x' is not initialized", 478, 20);
        BAssertUtil.validateError(result, i++, "variable 'x' is not initialized", 484, 20);
        BAssertUtil.validateError(result, i++, "variable 'yyy' is not initialized", 493, 20);
        BAssertUtil.validateError(result, i++, "variable 'yyy' is not initialized", 499, 20);
        BAssertUtil.validateError(result, i++, "variable 'a' is not initialized", 506, 5);
        BAssertUtil.validateError(result, i++, "variable 'a' is not initialized", 525, 13);
        BAssertUtil.validateError(result, i++, "variable 'b' may not have been initialized", 525, 16);
        BAssertUtil.validateError(result, i++, "uninitialized field 'a'", 550, 5);
        BAssertUtil.validateError(result, i++, "uninitialized field 'c'", 552, 5);
        BAssertUtil.validateError(result, i++, "variable 'publicGlobalVar_1' is not initialized", 558, 1);
        BAssertUtil.validateError(result, i++, "variable 'a' is not initialized", 590, 17);
        BAssertUtil.validateError(result, i++, "unreachable code", 615, 9);
        BAssertUtil.validateError(result, i++, "variable 'msg' is not initialized", 624, 12);
        BAssertUtil.validateError(result, i++, "unreachable code", 632, 9);
        BAssertUtil.validateError(result, i++, "uninitialized field 'a'", 645, 5);
        BAssertUtil.validateError(result, i++, "uninitialized field 'c'", 647, 5);
    }
}
