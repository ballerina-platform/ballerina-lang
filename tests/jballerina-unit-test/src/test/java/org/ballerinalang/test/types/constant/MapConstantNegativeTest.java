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

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Constant negative tests.
 */
public class MapConstantNegativeTest {

    @Test()
    public void testNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/constant/" +
                "map-literal-constant-negative.bal");

        Assert.assertEquals(compileResult.getErrorCount(), 69);

        String expectedErrMsg = "expression is not a constant expression";

        int index = 0;
        int offset = 0;

        // boolean
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'bm1'", offset += 4, 39);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'bm3'", offset += 3, 39);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'bm5'", offset += 3, 33);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'bm7'", offset += 3, 33);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'boolean', found 'boolean?'",
                                  offset + 3, 36);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 3, 36);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'boolean', found 'boolean?'",
                                  offset + 3, 36);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 3, 36);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 2, 29);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset, 39);

        // int
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'im1'", offset += 9, 35);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'im3'", offset += 3, 35);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'im5'", offset += 3, 29);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'im7'", offset += 3, 29);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'int', found 'int?'",
                                  offset + 3, 32);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 3, 32);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'int', found 'int?'",
                                  offset + 3, 32);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 3, 32);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 2, 25);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset, 35);

        // byte
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'bytem1'", offset += 9, 42);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'bytem3'", offset += 3, 42);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'bytem5'", offset += 3, 36);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'bytem7'", offset += 3, 36);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'byte', found 'byte?'",
                                  offset + 3, 36);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 3, 36);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'byte', found 'byte?'",
                                  offset + 3, 36);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 3, 36);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 2, 29);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset, 39);

        // float
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'fm1'", offset += 9, 37);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'fm3'", offset += 3, 37);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'fm5'", offset += 3, 31);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'fm7'", offset += 3, 31);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'float', found 'float?'",
                                  offset + 3, 34);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 3, 34);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'float', found 'float?'",
                                  offset + 3, 34);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 3, 34);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 2, 27);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset, 37);

        // decimal
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'dm1'", offset += 9, 39);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'dm3'", offset += 3, 39);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'dm5'", offset += 3, 33);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'dm7'", offset += 3, 33);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'decimal', found 'decimal?'",
                                  offset + 3, 36);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 3, 36);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'decimal', found 'decimal?'",
                                  offset + 3, 36);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 3, 36);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 2, 29);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset, 39);

        // string
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'sm1'", offset += 9, 38);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'sm3'", offset += 3, 38);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'sm5'", offset += 3, 32);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'sm7'", offset += 3, 32);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'string', found 'string?'",
                                  offset + 3, 35);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 3, 35);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'string', found 'string?'",
                                  offset + 3, 35);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 3, 35);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 2, 28);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset, 38);

        // nil
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'nm1'", offset += 9, 34);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'nm3'", offset += 3, 34);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'nm5'", offset += 3, 28);
        BAssertUtil.validateError(compileResult, index++, "key 'key' not found in 'nm7'", offset += 3, 28);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 3, 31);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 3, 31);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 2, 24);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset, 34);

        // Test invalid update via compund assignment
        BAssertUtil.validateError(compileResult, index, "cannot update constant value", 193, 5);
    }
}
