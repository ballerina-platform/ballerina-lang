/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.types.xml;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases to check invalid XML Text to String conversion.
 *
 * @since 2.0.0
 */
public class XMLTextToStringConversionNegativeTest {

    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        negativeResult = BCompileUtil.compile("test-src/types/xml/xml_text_to_string_conversion-negative.bal");
    }

    @Test
    public void testInvalidXMLToStringConversion() {
        int i = 0;
        Assert.assertEquals(negativeResult.getErrorCount(), 20);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: " +
                "expected 'string', found 'xml:Text'", 22, 17);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: " +
                "expected 'string', found 'xml:Text'", 23, 17);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: " +
                "expected 'string', found 'xml:Text'", 25, 17);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: " +
                "expected 'string', found 'xml:Text'", 28, 17);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: " +
                "expected 'string', found 'xml:Text'", 29, 37);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: " +
                "expected 'string', found 'xml:Text'", 31, 26);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: " +
                "expected 'string', found 'xml:Text'", 32, 8);

        BAssertUtil.validateError(negativeResult, i++, "incompatible types: " +
                "expected 'string', found 'xml:Text'", 40, 41);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: " +
                "expected 'string', found 'xml:Text'", 41, 31);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: " +
                "expected 'string', found 'xml:Text'", 42, 21);

        BAssertUtil.validateError(negativeResult, i++, "incompatible types: " +
                "expected 'string', found 'xml:Text'", 48, 9);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: " +
                "expected 'string', found 'xml:Text'", 48, 35);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: " +
                "expected 'string', found 'xml:Text'", 48, 63);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: " +
                "expected 'string', found 'xml:Text'", 49, 34);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: " +
                "expected 'string', found 'xml:Text'", 49, 69);

        BAssertUtil.validateError(negativeResult, i++, "incompatible types: " +
                "'xml:Text' cannot be cast to 'string'", 64, 16);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: " +
                "'xml' cannot be cast to 'string'", 65, 16);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: " +
                "'xml<xml:Text>' cannot be cast to 'string'", 66, 16);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: " +
                "'xml:Text' cannot be cast to 'string'", 67, 16);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: " +
                "'xml:Text' cannot be cast to '\"foo\"|\"bar\"'", 68, 16);
    }
}
