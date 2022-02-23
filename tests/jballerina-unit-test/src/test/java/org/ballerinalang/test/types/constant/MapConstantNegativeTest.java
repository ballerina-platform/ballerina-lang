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

        Assert.assertEquals(compileResult.getErrorCount(), 5);

        validateError(compileResult, 0, "expression is not a constant expression", 18, 35);
        validateError(compileResult, 1, "incompatible types: expected 'boolean', found 'boolean?'", 18, 35);
        validateError(compileResult, 2, "expression is not a constant expression", 20, 27);
        validateError(compileResult, 3, "expression is not a constant expression", 20, 38);
        validateError(compileResult, 4, "cannot update constant value", 34, 5);
    }

    @Test
    public void constMapSpreadFieldNegative() {
        CompileResult compileResult1 = BCompileUtil.compile(
                "test-src/types/constant/constant_map_spread_field_negative.bal");
        int i = 0;
        BAssertUtil.validateError(compileResult1, i++, "invalid usage of record literal: duplicate key 'b' via " +
                "spread operator '...CMS2'", 21, 46);
        BAssertUtil.validateError(compileResult1, i++, "invalid usage of record literal: duplicate key 'b' via " +
                "spread operator '...CMS1'", 22, 36);
        BAssertUtil.validateError(compileResult1, i++, "invalid usage of map literal: duplicate key 'b'", 23, 33);
        BAssertUtil.validateError(compileResult1, i++, "invalid usage of record literal: duplicate key 'b' via " +
                "spread operator '...CMS2'", 24, 36);
        BAssertUtil.validateError(compileResult1, i++, "invalid usage of record literal: duplicate key 'c' via " +
                "spread operator '...CMS2'", 24, 36);
        Assert.assertEquals(compileResult1.getErrorCount(), i);
    }
}
