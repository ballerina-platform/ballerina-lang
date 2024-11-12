/*
 *   Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
package org.ballerinalang.test.expressions.access;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Test cases for XML member access.
 *
 * @since 2201.11.0
 */
public class XmlMemberAccessTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/access/xml_member_access.bal");
    }

    @Test
    public void testNegativeCases() {
        CompileResult negativeResult = BCompileUtil.compile(
                "test-src/expressions/access/xml_member_access_negative.bal");
        int i = 0;
        validateError(negativeResult, i++, "incompatible types: expected 'xml:Element', " +
                "found '(xml:Element|xml:Comment|xml:ProcessingInstruction|xml:Text)'", 20, 21);
        validateError(negativeResult, i++, "incompatible types: expected " +
                "'(xml:Element|xml:Comment|xml:ProcessingInstruction)', found " +
                "'(xml:Element|xml:Comment|xml:ProcessingInstruction|xml:Text)'", 21, 59);
        validateError(negativeResult, i++, "incompatible types: expected " +
                "'(xml:Comment|xml:ProcessingInstruction|xml:Text)', found " +
                "'(xml:Element|xml:Comment|xml:ProcessingInstruction|xml:Text)'", 22, 56);
        validateError(negativeResult, i++, "incompatible types: expected 'xml:Element', " +
                "found '(xml:Element|xml<never>)'", 27, 21);
        validateError(negativeResult, i++, "incompatible types: expected 'xml<never>', " +
                "found '(xml:Element|xml<never>)'", 28, 20);
        validateError(negativeResult, i++, "incompatible types: expected 'xml:Comment', " +
                "found '(xml:Comment|xml<never>)'", 33, 21);
        validateError(negativeResult, i++, "incompatible types: expected 'xml<never>', " +
                "found '(xml:Comment|xml<never>)'", 34, 20);
        validateError(negativeResult, i++, "incompatible types: expected 'xml<never>', found 'xml:Text'", 39, 20);
        validateError(negativeResult, i++, "incompatible types: expected 'xml:ProcessingInstruction', " +
                "found '(xml:ProcessingInstruction|xml<never>)'", 44, 35);
        validateError(negativeResult, i++, "incompatible types: expected 'xml<never>', " +
                "found '(xml:ProcessingInstruction|xml<never>)'", 45, 20);
        validateError(negativeResult, i++, "incompatible types: expected 'xml:Element', " +
                "found '(xml:Element|xml<never>)'", 50, 21);
        validateError(negativeResult, i++, "incompatible types: expected 'xml<never>', found 'xml:Text'", 53, 20);
        validateError(negativeResult, i++, "incompatible types: expected 'xml:Comment', " +
                "found '(xml:Comment|xml<never>)'", 56, 21);
        validateError(negativeResult, i++, "incompatible types: expected 'xml:ProcessingInstruction', " +
                "found '(xml:ProcessingInstruction|xml<never>)'", 59, 35);
        validateError(negativeResult, i++, "incompatible types: expected '(xml:Element|xml:Comment)', " +
                "found '(xml:Element|xml:Comment|xml<never>)'", 64, 33);
        validateError(negativeResult, i++, "incompatible types: expected '(xml:Element|xml<never>)', " +
                "found '(xml:Element|xml:Comment|xml<never>)'", 65, 32);
        validateError(negativeResult, i++, "incompatible types: expected '(xml:Comment|xml<never>)', " +
                "found '(xml:Comment|xml:ProcessingInstruction|never|xml<never>)'", 68, 32);
        validateError(negativeResult, i++, "incompatible types: expected '(xml:Element|xml:Comment)', " +
                "found '(xml:Element|xml:Comment|xml<never>)'", 73, 33);
        validateError(negativeResult, i++, "incompatible types: expected '(xml:Comment|xml<never>)', " +
                "found '(xml:Element|xml:Comment|xml<never>)'", 74, 32);
        validateError(negativeResult, i++, "incompatible types: expected '(xml:Comment|xml<never>)', " +
                "found '(xml:Comment|xml:ProcessingInstruction|never|xml<never>)'", 77, 32);
        validateError(negativeResult, i++, "incompatible types: expected 'xml:Element', found 'xml<never>'", 82, 21);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @Test(dataProvider = "xmlMemberAccessFunctions")
    public void testXmlMemberAccess(String function) {
        BRunUtil.invoke(result, function);
    }

    @DataProvider(name = "xmlMemberAccessFunctions")
    public Object[][] xmlMemberAccessFunctions() {
        return new Object[][] {
                { "testXmlMemberAccessOnXml" },
                { "testXmlMemberAccessOnXmlElementSequences" },
                { "testXmlMemberAccessOnXmlCommentSequences" },
                { "testXmlMemberAccessOnXmlTextSequences" },
                { "testXmlMemberAccessOnXmlProcessingInstructionSequences" },
                { "testXmlMemberAccessOnXmlSingletons" },
                { "testXmlMemberAccessOnXmlUnions" },
                { "testXmlMemberAccessOnXmlUnionSequences" },
                { "testXmlMemberAccessOnEmptySequenceType" }
        };
    }
}
