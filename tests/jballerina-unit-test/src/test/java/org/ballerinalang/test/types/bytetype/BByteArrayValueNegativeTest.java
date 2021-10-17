/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.types.bytetype;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * This test class will test the byte array value negative test cases.
 */
public class BByteArrayValueNegativeTest {

    //TODO Transaction -- need to fix the error message due to new keywords introduced with predicates for transaction.
    @Test(description = "Test blob value negative")
    public void testBlobValueNegative() {
        CompileResult result = BCompileUtil.compile("test-src/types/byte/byte-array-value-negative.bal");
        int index = 0;
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'byte[]', found 'other'", 2, 16);
        BAssertUtil.validateError(result, index++, "undefined symbol 'base1'", 2, 16);
        BAssertUtil.validateError(result, index++, "invalid expr in assignment lhs", 2, 22);
        BAssertUtil.validateError(result, index++, "missing semicolon token", 2, 22);
        BAssertUtil.validateError(result, index++, "invalid literal for type 'other': raw templates can only " +
                "be assigned to abstract subtypes of 'ballerina/lang.object:0.0.0:RawTemplate'", 2, 24);
        BAssertUtil.validateError(result, index++, "missing equal token", 2, 24);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'byte[]', found 'other'", 3, 16);
        BAssertUtil.validateError(result, index++, "undefined symbol 'base'", 3, 16);
        BAssertUtil.validateError(result, index++, "invalid expr in assignment lhs", 3, 21);
        BAssertUtil.validateError(result, index++, "missing semicolon token", 3, 21);
        BAssertUtil.validateError(result, index++, "invalid literal for type 'other': raw templates can only " +
                "be assigned to abstract subtypes of 'ballerina/lang.object:0.0.0:RawTemplate'", 3, 24);
        BAssertUtil.validateError(result, index++, "missing equal token", 3, 24);
        BAssertUtil.validateError(result, index++, "invalid base16 content in byte array literal", 4, 24);
        BAssertUtil.validateError(result, index++, "invalid base16 content in byte array literal", 5, 24);
        BAssertUtil.validateError(result, index++, "invalid base16 content in byte array literal", 6, 24);
        BAssertUtil.validateError(result, index++, "invalid base16 content in byte array literal", 7, 24);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'byte[]', found 'other'", 8, 16);
        BAssertUtil.validateError(result, index++, "missing byte array content", 8, 16);
        BAssertUtil.validateError(result, index++, "operator '+' not defined for 'byte[]' and 'string'", 8, 16);
        BAssertUtil.validateError(result, index++, "missing binary operator", 8, 23);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'byte[]', found 'other'", 12, 16);
        BAssertUtil.validateError(result, index++, "undefined symbol 'base6'", 12, 16);
        BAssertUtil.validateError(result, index++, "invalid expr in assignment lhs", 12, 22);
        BAssertUtil.validateError(result, index++, "missing semicolon token", 12, 22);
        BAssertUtil.validateError(result, index++, "invalid literal for type 'other': raw templates can only " +
                "be assigned to abstract subtypes of 'ballerina/lang.object:0.0.0:RawTemplate'", 12, 24);
        BAssertUtil.validateError(result, index++, "missing equal token", 12, 24);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'byte[]', found 'other'", 13, 16);
        BAssertUtil.validateError(result, index++, "undefined symbol 'base'", 13, 16);
        BAssertUtil.validateError(result, index++, "invalid expr in assignment lhs", 13, 21);
        BAssertUtil.validateError(result, index++, "missing semicolon token", 13, 21);
        BAssertUtil.validateError(result, index++, "invalid literal for type 'other': raw templates can only " +
                "be assigned to abstract subtypes of 'ballerina/lang.object:0.0.0:RawTemplate'", 13, 24);
        BAssertUtil.validateError(result, index++, "missing equal token", 13, 24);
        BAssertUtil.validateError(result, index++, "invalid base64 content in byte array literal", 14, 24);
        BAssertUtil.validateError(result, index++, "invalid base64 content in byte array literal", 15, 24);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'byte[]', found 'other'", 16, 16);
        BAssertUtil.validateError(result, index++, "missing byte array content", 16, 16);
        BAssertUtil.validateError(result, index++, "operator '+' not defined for 'byte[]' and 'string'", 16, 16);
        BAssertUtil.validateError(result, index++, "missing binary operator", 16, 23);
        BAssertUtil.validateError(result, index++, "invalid base64 content in byte array literal", 17, 24);
        BAssertUtil.validateError(result, index++, "invalid base64 content in byte array literal", 18, 24);
        Assert.assertEquals(result.getErrorCount(), index);
    }
}
