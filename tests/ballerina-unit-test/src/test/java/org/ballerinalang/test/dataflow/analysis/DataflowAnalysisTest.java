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
import org.testng.annotations.Test;

/**
 * Test cases related to data flow analysis.
 *
 * @since 0.985.0
 */
public class DataflowAnalysisTest {

    @Test
    public void testUninitializedVariables() {
        CompileResult result = BCompileUtil.compile("test-src/dataflow/analysis/dataflow-analysis-negative.bal");
        // Assert.assertEquals(result.getErrorCount(), 1);
        System.out.println(result);
        int i = 0;
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 53, 12);
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 70, 12);
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 83, 12);
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 98, 12);
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 113, 12);
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 209, 12);
        BAssertUtil.validateError(result, i++, "variable 's' may not have been initialized", 216, 21);
        BAssertUtil.validateError(result, i++, "variable 'a' may not have been initialized", 219, 13);
        BAssertUtil.validateError(result, i++, "variable 'a' may not have been initialized", 222, 9);
        BAssertUtil.validateError(result, i++, "variable 's' may not have been initialized", 226, 24);
        BAssertUtil.validateError(result, i++, "variable 'm' may not have been initialized", 238, 20);
        BAssertUtil.validateError(result, i++, "variable 'm' may not have been initialized", 261, 9);
        BAssertUtil.validateError(result, i++, "variable 'm' may not have been initialized", 262, 9);
        BAssertUtil.validateError(result, i++, "variable 's' may not have been initialized", 262, 11);
        BAssertUtil.validateError(result, i++, "variable 'm' may not have been initialized", 265, 9);
        BAssertUtil.validateError(result, i++, "variable 's' may not have been initialized", 265, 18);
        BAssertUtil.validateError(result, i++, "variable 's' may not have been initialized", 266, 21);
        BAssertUtil.validateError(result, i++, "variable 'x' may not have been initialized", 270, 9);
        BAssertUtil.validateError(result, i++, "variable 's' may not have been initialized", 270, 12);
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 290, 20);
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 292, 16);
        BAssertUtil.validateError(result, i++, "variable 'globalVar' may not have been initialized", 305, 12);
        BAssertUtil.validateError(result, i++, "variable 'globalVar' may not have been initialized", 309, 13);
        BAssertUtil.validateError(result, i++, "undefined field 'f' in object 'Foo'", 315, 18);
        BAssertUtil.validateError(result, i++, "variable 'globalVar' may not have been initialized", 316, 13);
        BAssertUtil.validateError(result, i++, "variable 'globalVar' may not have been initialized", 321, 16);
        BAssertUtil.validateError(result, i++, "variable 'd' may not have been initialized", 338, 16);
        BAssertUtil.validateError(result, i++, "variable 'val' may not have been initialized", 381, 12);
        BAssertUtil.validateError(result, i++, "variable 'val' may not have been initialized", 417, 12);
        BAssertUtil.validateError(result, i++, "variable 'val' may not have been initialized", 465, 12);
        BAssertUtil.validateError(result, i++, "variable 'x' may not have been initialized", 478, 20);
        BAssertUtil.validateError(result, i++, "variable 'x' may not have been initialized", 484, 20);
        BAssertUtil.validateError(result, i++, "variable 'x' may not have been initialized", 493, 20);
        BAssertUtil.validateError(result, i++, "variable 'x' may not have been initialized", 499, 20)
    }
}
