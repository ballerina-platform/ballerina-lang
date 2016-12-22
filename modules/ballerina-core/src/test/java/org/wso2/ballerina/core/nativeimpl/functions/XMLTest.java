/*
*   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.nativeimpl.functions;

import org.testng.annotations.BeforeTest;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.linker.BLangLinker;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.nativeimpl.lang.xml.GetString;
import org.wso2.ballerina.core.nativeimpl.lang.xml.GetXML;
import org.wso2.ballerina.core.nativeimpl.lang.xml.Remove;
import org.wso2.ballerina.core.nativeimpl.lang.xml.SetString;
import org.wso2.ballerina.core.nativeimpl.lang.xml.SetXML;
import org.wso2.ballerina.core.nativeimpl.lang.xml.ToString;
import org.wso2.ballerina.core.utils.FunctionUtils;
import org.wso2.ballerina.core.utils.ParserUtils;

/**
 * Test Native function in ballerina.lang.xml
 */
public class XMLTest {

    private BallerinaFile bFile;
    private static final String s1 = "<persons><person><name>Jack</name><address>wso2</address></person></persons>";

    @BeforeTest
    public void setup() {
        bFile = ParserUtils.parseBalFile("samples/nativeimpl/xmlTest.bal");
        // Linking Native functions.
        SymScope symScope = new SymScope(null);
        FunctionUtils.addNativeFunction(symScope, new GetString());
        FunctionUtils.addNativeFunction(symScope, new GetXML());
        FunctionUtils.addNativeFunction(symScope, new Remove());
        FunctionUtils.addNativeFunction(symScope, new SetString());
        FunctionUtils.addNativeFunction(symScope, new SetXML());
        FunctionUtils.addNativeFunction(symScope, new ToString());
        BLangLinker linker = new BLangLinker(bFile);
        linker.link(symScope);
    }

//    @Test
//    public void testGetString() {
//        BValue[] arguments = {new XMLValue(s1)};
//        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "getString", arguments);
//
//        Context bContext = FunctionUtils.createInvocationContext(1);
//        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
//        funcIExpr.accept(bLangInterpreter);
//
//        final String expected = "Jack";
//        Assert.assertEquals(FunctionUtils.getValue(bContext).getString(), expected);
//    }
//
//    @Test
//    public void testGetXML() {
//        BValue[] arguments = {new XMLValue(s1)};
//        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "getXML", arguments);
//
//        Context bContext = FunctionUtils.createInvocationContext(1);
//        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
//        funcIExpr.accept(bLangInterpreter);
//
//        Assert.assertTrue(FunctionUtils.getValue(bContext).getBValue() instanceof XMLValue);
//
//        OMElement returnElement = FunctionUtils.getValue(bContext).getXML();
//        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<person><name>Jack</name>" +
//                "<address>wso2</address></person>");
//    }
//
//    @Test
//    public void testRemove() {
//        BValue[] arguments = {new XMLValue(s1)};
//        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "remove", arguments);
//
//        Context bContext = FunctionUtils.createInvocationContext(1);
//        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
//        funcIExpr.accept(bLangInterpreter);
//
//        Assert.assertTrue(FunctionUtils.getValue(bContext).getBValue() instanceof XMLValue);
//
//        OMElement returnElement = FunctionUtils.getValue(bContext).getXML();
//        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<persons><person><name>Jack" +
//                "</name></person></persons>");
//    }

    // TODO : Add test cases for Other Native functions.

}
