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
package org.ballerinalang.test.types.constant;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Constant negative tests.
 */
public class ConstantNegativeTest {

    @Test
    public void testNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/constant/constant-negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 26);
        String expectedErrMsg1 = "only simple literals can be assigned to a constant";
        String expectedErrMsg2 = "cannot assign a value to a constant";
        int index = 0;
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg1, 3, 21);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg1, 4, 29);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg1, 8, 13);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg1, 9, 21);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg2, 16, 5);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '10', found 'int'", 16, 9);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg2, 17, 5);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg2, 23, 9);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '10', found 'int'", 26, 9);



        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'string', found 'int'", 30, 21);
        BAssertUtil.validateError(compileResult, index++, "only simple literals can be assigned to a constant", 32, 18);

        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'abc'", 37, 1);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'def'", 43, 5);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'GET', found 'XYZ'", 54, 21);
        BAssertUtil.validateError(compileResult, index++, "function invocation on type 'XYZ' is not supported", 60, 24);

        BAssertUtil.validateError(compileResult, index++, "operator '+' not defined for 'true' and 'string'", 66, 12);
        BAssertUtil.validateError(compileResult, index++, "operator '+' not defined for '24' and 'string'", 72, 12);
        BAssertUtil.validateError(compileResult, index++, "operator '+' not defined for '12' and 'string'", 78, 12);
        BAssertUtil.validateError(compileResult, index++, "operator '+' not defined for '25.5' and 'string'", 84, 12);
        BAssertUtil.validateError(compileResult, index++, "operator '+' not defined for 'Ballerina' and 'string'", 90
                , 12);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'boolean', found 'true'", 98,
                9);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'boolean', found 'true'", 109,
                12);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'int', found '20'", 115, 12);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'byte', found '120'", 121, 12);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'float', found '2.0'", 127, 12);
        BAssertUtil.validateError(compileResult, index, "incompatible types: expected 'string', found 'Ballerina " +
                "rocks'", 133, 12);
    }
}
