/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for XML index-based and field-based access.
 *
 * @since 0.94.0
 */
public class XMLAccessTest {

    CompileResult result;
    CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/xml/xml-indexed-access.bal");
        negativeResult = BCompileUtil.compile("test-src/types/xml/xml-indexed-access-negative.bal");
    }

    @Test
    public void testInvalidXMLAccessWithIndex() {
        BAssertUtil.validateError(negativeResult, 0, "cannot update an xml sequence", 5, 5);
        
        BAssertUtil.validateError(negativeResult, 1, "cannot update an xml sequence", 13, 5);
    }

    @Test
    public void testXMLAccessWithIndex() {
        BValue[] returns = BRunUtil.invoke(result, "testXMLAccessWithIndex");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root><!-- comment node--><name>supun</name><city>colombo</city></root>");

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(), "<!-- comment node-->");

        Assert.assertTrue(returns[2] instanceof BXML);
        Assert.assertEquals(returns[2].stringValue(), "<name>supun</name>");
    }
    
    @Test(expectedExceptions = {BLangRuntimeException.class}, 
            expectedExceptionsMessageRegExp = ".*index out of range: index: 1, size: 1.*")
    public void testXMLAccessWithOutOfIndex() {
        BRunUtil.invoke(result, "testXMLAccessWithOutOfIndex");
    }
    
    @Test(expectedExceptions = {BLangRuntimeException.class}, 
            expectedExceptionsMessageRegExp = ".*array index out of range: index: 5, size: 3.*")
    public void testXMLSequenceAccessWithOutOfIndex() {
        BRunUtil.invoke(result, "testXMLSequenceAccessWithOutOfIndex");
    }

    @Test
    public void testLengthOfXMLSequence() {
        BValue[] returns = BRunUtil.invoke(result, "testLengthOfXMLSequence");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 3);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 2);
    }

    @Test
    public void testFieldBasedAccess() {
        BValue[] returns = BRunUtil.invoke(result, "testFieldBasedAccess");
        Assert.assertEquals(returns[0].stringValue(),
                "<fname><foo>1</foo><bar>2</bar></fname><lname1><foo>3</foo><bar>4</bar></lname1><fname><foo>5</foo>" +
                        "<bar>6</bar></fname><lname2><foo>7</foo><bar>8</bar></lname2>apple");
        Assert.assertEquals(returns[1].stringValue(), "<fname><foo>1</foo><bar>2</bar></fname>");
        Assert.assertEquals(returns[2].stringValue(), "<foo>5</foo>");
        Assert.assertEquals(returns[3].stringValue(), "<foo>5</foo>");
        Assert.assertEquals(returns[4].stringValue(), "<bar>4</bar>");
        Assert.assertEquals(returns[5].stringValue(),
                "<foo>1</foo><bar>2</bar><foo>3</foo><bar>4</bar><foo>5</foo><bar>6</bar><foo>7</foo><bar>8</bar>");
    }

    @Test
    public void testFieldBasedAccessWithNamespaces() {
        BValue[] returns = BRunUtil.invoke(result, "testFieldBasedAccessWithNamespaces");
        Assert.assertEquals(returns[0].stringValue(),
                "<ns0:fname xmlns:ns0=\"http://test.com\" xmlns=\"http://test.com/default\">John</ns0:fname>");
        Assert.assertEquals(returns[1].stringValue(),
                "<ns0:fname xmlns:ns0=\"http://test.com\" xmlns=\"http://test.com/default\">John</ns0:fname>");
        Assert.assertTrue(((BXML<?>) returns[2]).isEmpty().booleanValue());
        Assert.assertEquals(returns[3].stringValue(),
                "<ns0:fname xmlns:ns0=\"http://test.com\" xmlns=\"http://test.com/default\">John</ns0:fname>");
    }
}
