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

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.interpreter.BLangInterpreter;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.linker.BLangLinker;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.JSONValue;
import org.wso2.ballerina.core.model.values.MessageValue;
import org.wso2.ballerina.core.model.values.StringValue;
import org.wso2.ballerina.core.nativeimpl.lang.message.AddHeader;
import org.wso2.ballerina.core.nativeimpl.lang.message.Clone;
import org.wso2.ballerina.core.nativeimpl.lang.message.GetHeader;
import org.wso2.ballerina.core.nativeimpl.lang.message.GetHeaders;
import org.wso2.ballerina.core.nativeimpl.lang.message.GetJsonPayload;
import org.wso2.ballerina.core.nativeimpl.lang.message.GetStringPayload;
import org.wso2.ballerina.core.nativeimpl.lang.message.GetXMLPayload;
import org.wso2.ballerina.core.nativeimpl.lang.message.RemoveHeader;
import org.wso2.ballerina.core.nativeimpl.lang.message.SetHeader;
import org.wso2.ballerina.core.nativeimpl.lang.message.SetJsonPayload;
import org.wso2.ballerina.core.nativeimpl.lang.message.SetStringPayload;
import org.wso2.ballerina.core.nativeimpl.lang.message.SetXMLPayload;
import org.wso2.ballerina.core.nativeimpl.lang.system.LogString;
import org.wso2.ballerina.core.utils.FunctionUtils;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.Header;

import java.util.List;

/**
 * Test Native function in ballerina.lang.message
 */
public class MessageTest {

    private BallerinaFile bFile;
//    private static final String s1 = "<persons><person><name>Jack</name><address>wso2</address></person></persons>";

    @BeforeTest
    public void setup() {
        bFile = ParserUtils.parseBalFile("samples/nativeimpl/messageTest.bal");
        // Linking Native functions.
        SymScope symScope = new SymScope(null);
        FunctionUtils.addNativeFunction(symScope, new AddHeader());
        FunctionUtils.addNativeFunction(symScope, new Clone());
        FunctionUtils.addNativeFunction(symScope, new GetHeader());
        FunctionUtils.addNativeFunction(symScope, new GetHeaders());
        FunctionUtils.addNativeFunction(symScope, new GetJsonPayload());
        FunctionUtils.addNativeFunction(symScope, new GetStringPayload());
        FunctionUtils.addNativeFunction(symScope, new GetXMLPayload());
        FunctionUtils.addNativeFunction(symScope, new RemoveHeader());
        FunctionUtils.addNativeFunction(symScope, new SetHeader());
        FunctionUtils.addNativeFunction(symScope, new SetJsonPayload());
        FunctionUtils.addNativeFunction(symScope, new SetStringPayload());
        FunctionUtils.addNativeFunction(symScope, new SetXMLPayload());
        FunctionUtils.addNativeFunction(symScope, new LogString());
        BLangLinker linker = new BLangLinker(bFile);
        linker.link(symScope);
    }

    @Test
    public void testGetJSONPayload() {
        DefaultCarbonMessage carbonMsg = new DefaultCarbonMessage();
        final String payload = "{\"name\":\"Jack\",\"address\":\"WSO2\"}";
        carbonMsg.setStringMessageBody(payload);

        BValue[] arguments = {new MessageValue(carbonMsg)};
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "testGetJSONPayload", arguments);

        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);

        Assert.assertEquals(FunctionUtils.getValue(bContext).getJSON().toString(), payload);
    }

    @Test
    public void testSetJSONPayload() {
        DefaultCarbonMessage carbonMsg = new DefaultCarbonMessage();
        final String payload = "{\"name\":\"Jack\",\"address\":\"WSO2\"}";

        BValue[] arguments = {new MessageValue(carbonMsg), new JSONValue(payload)};
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "testSetJSONPayload", arguments);

        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);

        BValue<?> newPayload = ((MessageValue) FunctionUtils.getValue(bContext).getBValue()).getBuiltPayload();
        Assert.assertTrue(newPayload instanceof JSONValue);

        String value = newPayload.getValue().toString();
        Assert.assertEquals(value, payload);
    }

    @Test
    public void testSetHeader() {
        DefaultCarbonMessage carbonMsg = new DefaultCarbonMessage();
        String name = "Content-Type";
        String value = "text/plain";

        BValue[] arguments = {new MessageValue(carbonMsg), new StringValue(name), new StringValue(value)};
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "testSetHeader", arguments);

        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);

        List<Header> headers = ((MessageValue) FunctionUtils.getValue(bContext).getBValue()).getHeaders();
        Assert.assertEquals(headers.size(), 1, "Headers list can have only 1 header.");

        Assert.assertNotNull(headers.get(0));
        Assert.assertEquals(headers.get(0).getName(), name);
        Assert.assertEquals(headers.get(0).getValue(), value);
    }

    //    @Test
    public void testGetHeader() {
        // TODO : This is not working, because CMessage Headers won't get sync with BMessage value.
        DefaultCarbonMessage carbonMsg = new DefaultCarbonMessage();
        String name = "Content-Type";
        String value = "text/plain";
        carbonMsg.setHeader(name, value);
        carbonMsg.setHeader("Accept", "Application/json");
        // These headers are not getting set.

        BValue[] arguments = {new MessageValue(carbonMsg), new StringValue(name)};
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "testGetHeader", arguments);

        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);

        Assert.assertEquals(FunctionUtils.getValue(bContext).getString(), value);
    }

    @Test
    public void testGetStringPayload() {
        DefaultCarbonMessage carbonMsg = new DefaultCarbonMessage();
        final String payload = "Hello World...!!!";
        carbonMsg.setStringMessageBody(payload);

        BValue[] arguments = {new MessageValue(carbonMsg)};
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "testGetStringPayload", arguments);

        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);

        BValue<?> newPayload = ((MessageValue) FunctionUtils.getValue(bContext).getBValue()).getBuiltPayload();
        Assert.assertTrue(newPayload instanceof StringValue);

        String value = newPayload.getValue().toString();
        Assert.assertEquals(value, payload);
    }

    @Test
    public void testSetStringPayload() {
        DefaultCarbonMessage carbonMsg = new DefaultCarbonMessage();
        final String payload = "Hello World...!!!";

        BValue[] arguments = {new MessageValue(carbonMsg), new StringValue(payload)};
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "testSetStringPayload", arguments);

        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);

        BValue<?> newPayload = ((MessageValue) FunctionUtils.getValue(bContext).getBValue()).getBuiltPayload();
        Assert.assertTrue(newPayload instanceof StringValue);

        String value = newPayload.getValue().toString();
        Assert.assertEquals(value, payload);
    }

    @Test
    public void testClone() {
        final String payload1 = "Hello World...!!! I am the Original Copy.";
        final String payload2 = "Hello World...!!! I am the Cloned Copy.";

        DefaultCarbonMessage carbonMsg = new DefaultCarbonMessage();
        carbonMsg.setStringMessageBody(payload1);

        BValue[] arguments = {new MessageValue(carbonMsg), new StringValue(payload2)};
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "testClone", arguments);

        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);

        Assert.assertEquals(FunctionUtils.getValue(bContext).getInt(), 1);
    }

    // TODO : Add test cases fot other native functions.
}
