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

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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

    @Test(enabled = false) // disabling until providing semantic support for step extension
    public void testBasicAttributeAccessSyntax() {
        BRunUtil.invoke(compileResult, "getElementAttrBasic");
    }

    @Test(enabled = false) // disabling until providing semantic support for step extension
    public void testBasicOptionalAttributeAccessSyntax() {
        BRunUtil.invoke(compileResult, "getOptionalElementAttrBasic");
    }

    @Test(enabled = false) // disabling until providing semantic support for step extension
    public void testUserDefinedAttributeAccessSyntax() {
        BRunUtil.invoke(compileResult, "getUserDefinedTypeElementAttrBasic");
    }

    @Test(enabled = false) // disabling until providing semantic support for step extension
    public void testUserDefinedOptionalAttributeAccessSyntax() {
        BRunUtil.invoke(compileResult, "getUserDefinedTypeOptionalElementAttrBasic");
    }

    @Test(enabled = false) // disabling until providing semantic support for step extension
    public void testAttributeAccessSyntaxWithNS() {
        Object result = BRunUtil.invoke(compileResult, "getElementAttrWithNSPrefix");
        Assert.assertEquals(result.toString(), "attr-with-ns-val");
    }

    @Test(enabled = false) // disabling until providing semantic support for step extension
    public void testGetAttrOfASequence() {
        Object result = BRunUtil.invoke(compileResult, "getAttrOfASequence");
        Assert.assertEquals(result.toString(),
                "error(\"{ballerina/lang.xml}XMLOperationError\",message=\"invalid xml attribute access on xml " +
                        "sequence\")");
    }

    @Test
    public void testXMLAttributeAccessNegative() {
        CompileResult negative = BCompileUtil.compile("test-src/types/xml/xml-attribute-access-syntax-neg.bal");
        Assert.assertEquals(negative.getErrorCount(), 2);
        BAssertUtil.validateError(negative, 0, "invalid character ':' in field access expression", 7, 13);
        BAssertUtil.validateError(negative, 1, "invalid character ':' in field access expression", 10, 13);
    }

    @Test
    public void testXMLAttributeWithNSPrefix() {
        BArray result = (BArray) BRunUtil.invoke(lexCompileRes, "testXMLAttributeWithNSPrefix");
        Assert.assertEquals(result.get(0).toString(), "preserve");
        Assert.assertEquals(result.get(1).toString(), "preserve");
    }

    @Test
    public void testXMLDirectAttributeAccess() {
        BArray result = (BArray) BRunUtil.invoke(lexCompileRes, "testXMLDirectAttributeAccess");
        Assert.assertTrue((Boolean) result.get(0));
        Assert.assertTrue((Boolean) result.get(1));
        Assert.assertTrue((Boolean) result.get(2));
        Assert.assertTrue((Boolean) result.get(3));
    }

    @Test(enabled = false) // disabling until providing semantic support for step extension
    public void testXMLAfterRemoveAttribute() {
        BRunUtil.invoke(compileResult, "testXMLAfterRemoveAttribute");
    }

    @Test(enabled = false) // disabling until providing semantic support for step extension
    public void testXMLIndexedOptionalAttributeAccess() {
        BRunUtil.invoke(compileResult, "testXMLIndexedOptionalAttributeAccess");
    }

    @Test(enabled = false) // disabling until providing semantic support for step extension
    public void testErrorsOnXMLIndexedOptionalAttributeAccess() {
        BRunUtil.invoke(compileResult, "testErrorsOnXMLIndexedOptionalAttributeAccess");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
        lexCompileRes = null;
    }

}
