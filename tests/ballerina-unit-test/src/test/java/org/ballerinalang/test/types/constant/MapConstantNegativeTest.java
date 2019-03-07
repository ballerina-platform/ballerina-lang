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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Constant negative tests.
 */
public class MapConstantNegativeTest {

    @Test
    public void testNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/constant/" +
                "map-literal-constant-negative.bal");

        Assert.assertEquals(compileResult.getErrorCount(), 42);

        int index = 0;
        int offset = 0;

        // boolean
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'bm1'", offset += 4, 38);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'bm3'", offset += 3, 38);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'bm5'", offset += 3, 32);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'bm7'", offset += 3, 32);
        BAssertUtil.validateError(compileResult, index++, "expression is not a constant expression", offset += 3, 36);
        BAssertUtil.validateError(compileResult, index++, "expression is not a constant expression", offset += 3, 36);

        // int
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'im1'", offset += 5, 34);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'im3'", offset += 3, 34);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'im5'", offset += 3, 28);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'im7'", offset += 3, 28);
        BAssertUtil.validateError(compileResult, index++, "expression is not a constant expression", offset += 3, 32);
        BAssertUtil.validateError(compileResult, index++, "expression is not a constant expression", offset += 3, 32);

        // byte
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'bytem1'", offset += 5, 41);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'bytem3'", offset += 3, 41);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'bytem5'", offset += 3, 35);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'bytem7'", offset += 3, 35);
        BAssertUtil.validateError(compileResult, index++, "expression is not a constant expression", offset += 3, 36);
        BAssertUtil.validateError(compileResult, index++, "expression is not a constant expression", offset += 3, 36);

        // float
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'fm1'", offset += 5, 36);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'fm3'", offset += 3, 36);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'fm5'", offset += 3, 30);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'fm7'", offset += 3, 30);
        BAssertUtil.validateError(compileResult, index++, "expression is not a constant expression", offset += 3, 34);
        BAssertUtil.validateError(compileResult, index++, "expression is not a constant expression", offset += 3, 34);

        // decimal
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'dm1'", offset += 5, 38);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'dm3'", offset += 3, 38);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'dm5'", offset += 3, 32);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'dm7'", offset += 3, 32);
        BAssertUtil.validateError(compileResult, index++, "expression is not a constant expression", offset += 3, 36);
        BAssertUtil.validateError(compileResult, index++, "expression is not a constant expression", offset += 3, 36);

        // string
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'sm1'", offset += 5, 37);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'sm3'", offset += 3, 37);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'sm5'", offset += 3, 31);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'sm7'", offset += 3, 31);
        BAssertUtil.validateError(compileResult, index++, "expression is not a constant expression", offset += 3, 35);
        BAssertUtil.validateError(compileResult, index++, "expression is not a constant expression", offset += 3, 35);

        // nil
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'nm1'", offset += 5, 33);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'nm3'", offset += 3, 33);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'nm5'", offset += 3, 27);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'nm7'", offset += 3, 27);
        BAssertUtil.validateError(compileResult, index++, "expression is not a constant expression", offset += 3, 31);
        BAssertUtil.validateError(compileResult, index++, "expression is not a constant expression", offset += 3, 31);
    }
}
