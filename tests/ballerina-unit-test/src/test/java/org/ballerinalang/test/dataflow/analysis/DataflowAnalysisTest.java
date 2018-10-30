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
        CompileResult result = BCompileUtil.compile("test-src/dataflow/analysis/dataflow-analysis.bal");
        // Assert.assertEquals(result.getErrorCount(), 1);
        System.out.println(result);
        int i = 0;
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 50, 12);
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 67, 12);
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 80, 12);
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 95, 12);
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 110, 12);
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 206, 12);
        BAssertUtil.validateError(result, i++, "variable 'a' may not have been initialized", 216, 13);
        BAssertUtil.validateError(result, i++, "variable 'a' may not have been initialized", 219, 9);
        BAssertUtil.validateError(result, i++, "variable 'e' may not have been initialized", 223, 15);
        BAssertUtil.validateError(result, i++, "variable 'm' may not have been initialized", 235, 20);
        BAssertUtil.validateError(result, i++, "variable 'm' may not have been initialized", 258, 9);
        BAssertUtil.validateError(result, i++, "variable 'm' may not have been initialized", 259, 9);
        BAssertUtil.validateError(result, i++, "variable 's' may not have been initialized", 259, 11);
        BAssertUtil.validateError(result, i++, "variable 'm' may not have been initialized", 262, 9);
        BAssertUtil.validateError(result, i++, "variable 's' may not have been initialized", 262, 18);
        BAssertUtil.validateError(result, i++, "variable 's' may not have been initialized", 263, 21);
        BAssertUtil.validateError(result, i++, "variable 'x' may not have been initialized", 267, 9);
        BAssertUtil.validateError(result, i++, "variable 's' may not have been initialized", 267, 12);
        BAssertUtil.validateError(result, i++, "variable 'msg' may not have been initialized", 283, 20);
        BAssertUtil.validateError(result, i++, "variable 'globalVar' may not have been initialized", 294, 12);
        BAssertUtil.validateError(result, i++, "variable 'globalVar' may not have been initialized", 298, 13);
        BAssertUtil.validateError(result, i++, "variable 'globalVar' may not have been initialized", 304, 13);
        BAssertUtil.validateError(result, i++, "variable 'globalVar' may not have been initialized", 309, 16);
        BAssertUtil.validateError(result, i++, "variable 'd' may not have been initialized", 326, 16);
        BAssertUtil.validateError(result, i++, "variable 'globalVar' may not have been initialized", 315, 12);
    }
}
