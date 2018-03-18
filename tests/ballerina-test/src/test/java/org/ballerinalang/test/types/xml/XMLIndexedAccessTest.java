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
 * Local function invocation test.
 *
 * @since 0.94.0
 */
public class XMLIndexedAccessTest {

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
}
