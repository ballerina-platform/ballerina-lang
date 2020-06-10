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

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * This test class will test the byte array value negative test cases.
 */
public class BByteArrayValueNegativeTest {

    @Test(description = "Test blob value negative")
    public void testBlobValueNegative() {
        CompileResult result = BCompileUtil.compile("test-src/types/byte/byte-array-value-negative.bal");
        Assert.assertEquals(result.getErrorCount(), 21);

        int index = 0;
        String msg1 = "expecting {'is', 'equals', ';', '.', '[', '?', '?.', '+', '-', '*', '/', '%', '==', '!=', '>'," +
                " '<', '>=', '<=', '&&', '||', '===', '!==', '&', '^', '@', '...', '|', '?:', '->>', '..<', '.@'}";
        
        BAssertUtil.validateError(result, index++, "mismatched input '6'. " + msg1, 2, 22);
        BAssertUtil.validateError(result, index++, "mismatched input '16'. " + msg1, 3, 21);
        BAssertUtil.validateError(result, index++, "mismatched input '`'. " + msg1, 4, 23);
        BAssertUtil.validateError(result, index++, "mismatched input '`'. " + msg1, 5, 23);
        BAssertUtil.validateError(result, index++, "mismatched input '`'. " + msg1, 6, 23);
        BAssertUtil.validateError(result, index++, "mismatched input '`'. expecting {'service', " +
                "'function', 'object', 'record', 'abstract', 'client', 'typeof', 'distinct', 'int', 'byte', 'float', " +
                "'decimal', 'boolean', 'string', 'error', 'map', 'json', 'xml', 'table', 'stream', 'any', " +
                "'typedesc', 'future', 'anydata', 'handle', 'readonly', 'never', 'new', '__init', 'foreach', " +
                "'continue', 'trap', 'start', 'check', 'checkpanic', 'flush', 'wait', 'from', 'let', '{', '(', " +
                "'[', '+', '-', '!', '<', '~', '<-', '@', DecimalIntegerLiteral, HexIntegerLiteral, " +
                "HexadecimalFloatingPointLiteral, DecimalFloatingPointNumber, BooleanLiteral, QuotedStringLiteral, " +
                "Base16BlobLiteral, Base64BlobLiteral, 'null', Identifier, XMLLiteralStart, " +
                "StringTemplateLiteralStart}", 6, 59);
        BAssertUtil.validateError(result, index, "mismatched input '`'. " + msg1, 7, 23);
    }
}
