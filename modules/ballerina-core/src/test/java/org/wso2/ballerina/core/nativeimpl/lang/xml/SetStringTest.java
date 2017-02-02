/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.nativeimpl.lang.xml;

import org.apache.axiom.om.OMElement;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BXML;
import org.wso2.ballerina.core.utils.FunctionUtils;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

import javax.xml.namespace.QName;

/**
 * This class test the xml:setString native function.
 * xml:set(xml, xpath, string);
 * Ex:
 * xml:set(xml, "/CheckAvailability/doctorName", name);
 * xml:set(xml, "/CheckAvailability/doctorName/text()", name);
 */
public class SetStringTest {
    private BallerinaFile bFile;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        SymScope symScope = new SymScope(null);
        FunctionUtils.addNativeFunction(symScope, new SetString());
        bFile = ParserUtils.parseBalFile("lang/native/function/xml-setString.bal", symScope);
    }

    @Test(description = "Test xml element string value replacement")
    public void testSetXmlElementText() {
        BValue[] returns = Functions.invoke(bFile, "xmlSetString1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BXML.class);
        BXML returnedXml = (BXML) returns[0];
        OMElement xmlMessage = returnedXml.value();
        String actualDName = xmlMessage.getFirstChildWithName(new QName("doctorName")).getText();
        Assert.assertEquals(actualDName, "DName1", "XML Element text not set properly");
    }

    @Test(description = "Test xml text value replacement")
    public void testSetXmlText() {
        BValue[] returns = Functions.invoke(bFile, "xmlSetString2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BXML.class);
        BXML returnedXml = (BXML) returns[0];
        OMElement xmlMessage = returnedXml.value();
        String actualDName = xmlMessage.getFirstChildWithName(new QName("doctorName")).getText();
        Assert.assertEquals(actualDName, "DName2", "XML Element text not set properly");
    }
}
