/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.types.xml;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @since 1.2.0
 */
public class XMLAttributeAccessTest {

    CompileResult compileResult;
    CompileResult lexCompileRes;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/xml/xml-attribute-access-syntax.bal");
        lexCompileRes = BCompileUtil.compile("test-src/types/xml/xml-attribute-access-lax-behavior.bal");
    }

    @Test
    public void testBasicAttributeAccessSyntax() {
        BValue[] result = BRunUtil.invoke(compileResult, "getElementAttrBasic");
        Assert.assertEquals(result[0].stringValue(), "attr-val");
    }

    @Test
    public void testAttributeAccessSyntaxWithNS() {
        BValue[] result = BRunUtil.invoke(compileResult, "getElementAttrWithNSPrefix");
        Assert.assertEquals(result[0].stringValue(), "attr-with-ns-val");
    }

    @Test
    public void testGetAttrOfASequence() {
        BValue[] result = BRunUtil.invoke(compileResult, "getAttrOfASequence");
        Assert.assertEquals(result[0].stringValue(),
                "{ballerina/lang.xml}XMLOperationError {\"message\":\"Invalid xml attribute access on xml sequence\"}");
    }

    @Test
    public void testXMLAttributeAccessNegative() {
        CompileResult negative = BCompileUtil.compile("test-src/types/xml/xml-attribute-access-syntax-neg.bal");
        Assert.assertEquals(negative.getErrorCount(), 2);
        BAssertUtil.validateError(negative, 0, "invalid character ':' in field access expression", 7, 13);
        BAssertUtil.validateError(negative, 1, "invalid character ':' in field access expression", 10, 13);
    }

    @Test
    public void testXMLAsMapContent() {
        BValue[] result = BRunUtil.invoke(lexCompileRes, "testXMLAsMapContent");
        Assert.assertEquals(result[0].stringValue(), "val");
        Assert.assertEquals(result[1].stringValue(), "val");
        Assert.assertEquals(result[2].stringValue(), "true");
    }

    @Test
    public void testXMLAttributeWithNSPrefix() {
        BValue[] result = BRunUtil.invoke(lexCompileRes, "testXMLAttributeWithNSPrefix");
        Assert.assertEquals(result[0].stringValue(), "preserve");
        Assert.assertEquals(result[1].stringValue(), "preserve");
        Assert.assertEquals(result[2].stringValue(), "{lang.map}InvalidKey {\"key\":\"b\"}");
    }

    @Test
    public void testXMLASMapContentInvalidKey() {
        BValue[] result = BRunUtil.invoke(lexCompileRes, "testXMLASMapContentInvalidKey");
        Assert.assertEquals(result[0].stringValue(), "{lang.map}InvalidKey {\"key\":\"b\"}");
    }

    @Test
    public void testXMLDirectAttributeAccess() {
        BValue[] result = BRunUtil.invoke(lexCompileRes, "testXMLDirectAttributeAccess");
        Assert.assertTrue(((BBoolean) result[0]).booleanValue());
        Assert.assertTrue(((BBoolean) result[0]).booleanValue());
        Assert.assertTrue(((BBoolean) result[0]).booleanValue());
        Assert.assertTrue(((BBoolean) result[0]).booleanValue());
    }

}
