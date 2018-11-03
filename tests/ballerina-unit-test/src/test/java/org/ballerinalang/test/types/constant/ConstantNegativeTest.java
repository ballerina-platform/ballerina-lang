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
        Assert.assertEquals(compileResult.getErrorCount(), 32);
        String expectedErrMsg1 = "only simple literals can be assigned to a constant";
        String expectedErrMsg2 = "cannot assign a value to a constant";
        int index = 0;
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'boolean', found 'int'", 1, 29);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'int', found 'string'", 2, 21);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'byte', found 'int'", 3, 23);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'float', found 'boolean'", 4,
                25);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'string', found 'int'", 5, 27);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg1, 9, 21);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg1, 10, 29);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg1, 14, 13);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg1, 15, 21);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg2, 22, 5);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '10', found 'int'", 22, 9);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg2, 23, 5);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg2, 29, 9);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '10', found 'int'", 32, 9);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'string', found 'int'", 36, 21);
        BAssertUtil.validateError(compileResult, index++, "only simple literals can be assigned to a constant", 38, 18);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'abc'", 43, 1);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'def'", 49, 5);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'GET', found 'XYZ'", 60, 21);
        BAssertUtil.validateError(compileResult, index++, "function invocation on type 'XYZ' is not supported", 66, 24);
        BAssertUtil.validateError(compileResult, index++, "operator '+' not defined for 'true' and 'string'", 72, 12);
        BAssertUtil.validateError(compileResult, index++, "operator '+' not defined for '24' and 'string'", 78, 12);
        BAssertUtil.validateError(compileResult, index++, "operator '+' not defined for '12' and 'string'", 84, 12);
        BAssertUtil.validateError(compileResult, index++, "operator '+' not defined for '25.5' and 'string'", 90, 12);
        BAssertUtil.validateError(compileResult, index++, "operator '+' not defined for 'Ballerina' and 'string'", 96,
                12);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'boolean', found 'true'", 104,
                9);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'boolean', found 'true'", 115,
                12);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'int', found '20'", 121, 12);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'byte', found '120'", 127, 12);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'float', found '2.0'", 133, 12);
        BAssertUtil.validateError(compileResult, index, "incompatible types: expected 'string', found 'Ballerina " +
                        "rocks'", 139, 12);
    }
}
