/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.types.xml;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for XML type descriptor.
 *
 * @since 2201.9.0
 */
public class XmlTypeDescriptorTest {

    @Test
    public void testXmlTypeDescriptor() {
        CompileResult result = BCompileUtil.compile("test-src/types/xml/xml_type_descriptor.bal");
        BRunUtil.invoke(result, "testXmlTypeDescriptorWithTypeParameter");
    }

    @Test
    public void testXmlTypeDescriptorNegative() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/types/xml/xml_type_descriptor_negative.bal");
        int index = 0;
        validateError(negativeResult, index++, "incompatible types: 'xml' cannot be constrained with 'R'", 31, 9);
        validateError(negativeResult, index++, "incompatible types: 'xml' cannot be constrained with '(int|T)'", 32, 9);
        validateError(negativeResult, index++, "incompatible types: 'xml' cannot be constrained with '(T|Rec)'", 33, 9);
        validateError(negativeResult, index++, "incompatible types: expected 'RO_XML', found 'xml<xml:Element>'", 37,
                      16);
        validateError(negativeResult, index++, "incompatible types: expected 'RO_XML', found '" +
                "(xml:Element|xml:Comment)'", 40, 16);
        validateError(negativeResult, index++, "incompatible types: expected 'RO_XML', found 'xml<" +
                "(xml:Element|xml:Comment)>'", 43, 16);
        validateError(negativeResult, index++, "incompatible types: expected '(RO_C|RO_P|T)', found 'RO_XML'", 46, 21);
        assertEquals(negativeResult.getErrorCount(), index);
    }
}
