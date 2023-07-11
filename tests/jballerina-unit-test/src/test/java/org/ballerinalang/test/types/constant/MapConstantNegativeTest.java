/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Constant negative tests.
 */
public class MapConstantNegativeTest {

    @Test()
    public void testNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/constant/map_constant_negative.bal");
        int i = 0;
        validateError(compileResult, i++, "expression is not a constant expression", 18, 35);
        validateError(compileResult, i++, "expression is not a constant expression", 20, 27);
        validateError(compileResult, i++, "incompatible types: expected 'string', found 'other'", 20, 27);
        validateError(compileResult, i++, "expression is not a constant expression", 20, 38);
        validateError(compileResult, i++, "cannot update constant value", 34, 5);
        validateError(compileResult, i++, "invalid usage of finite literal: duplicate key 'a'", 39, 34);
        validateError(compileResult, i++, "illegal cyclic reference '[B1]'", 46, 1);
        validateError(compileResult, i++, "cannot declare a constant with type 'A1', expected a subtype of 'anydata'" +
                " that is not 'never'", 46, 7);
        Assert.assertEquals(compileResult.getErrorCount(), i);
    }

    @Test
    public void constMapSpreadFieldNegative() {
        CompileResult compileResult1 = BCompileUtil.compile(
                "test-src/types/constant/constant_map_spread_field_negative.bal");
        int i = 0;
        BAssertUtil.validateError(compileResult1, i++, "invalid usage of finite literal: duplicate key 'b'", 21, 46);
        BAssertUtil.validateError(compileResult1, i++, "invalid usage of finite literal: duplicate key 'b'", 22, 36);
        BAssertUtil.validateError(compileResult1, i++, "invalid usage of finite literal: duplicate key 'b'", 23, 33);
        BAssertUtil.validateError(compileResult1, i++, "invalid usage of finite literal: duplicate key 'b'", 24, 36);
        BAssertUtil.validateError(compileResult1, i++, "invalid usage of finite literal: duplicate key 'c'", 24, 36);
        Assert.assertEquals(compileResult1.getErrorCount(), i);
    }
}
