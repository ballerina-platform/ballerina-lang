/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.nativeimpl.functions;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.runtime.internal.BuiltInNativeConstructLoader;
import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;
import org.wso2.ballerina.nativeimpl.connectors.http.Constants;
import org.wso2.ballerina.nativeimpl.util.Functions;
import org.wso2.ballerina.nativeimpl.util.ParserUtils;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.Headers;

/**
 * Test cases for ballerina.net.http native functions.
 */
public class NetHttpTest {

    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        // Add Native functions.
        SymScope symScope = GlobalScopeHolder.getInstance().getScope();
        if (symScope.lookup(new SymbolName("ballerina.lang.system:print_string")) == null) {
            BuiltInNativeConstructLoader.loadConstructs();
        }
        bFile = ParserUtils.parseBalFile("samples/netHttp.bal", symScope);
    }

    @Test
    public void testGetMethod() {
        DefaultCarbonMessage cMsg = new DefaultCarbonMessage();
        BMessage msg = new BMessage();
        cMsg.setProperty(Constants.HTTP_METHOD, Constants.HTTP_METHOD_GET);
        msg.setValue(cMsg);
        Context ctx = new Context(cMsg);
        BValue[] inputArg = {msg};
        BValue[] returnVals = Functions.invoke(bFile, "testGetMethod", inputArg, ctx);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), Constants.HTTP_METHOD_GET, "Method didn't match.");
    }

    @Test
    public void testConvertToResponse() {
        DefaultCarbonMessage cMsg = new DefaultCarbonMessage();
        BMessage msg = new BMessage();
        Headers headers = new Headers();
        headers.set("test", "testValue");
        cMsg.setProperty(Constants.HTTP_METHOD, Constants.HTTP_METHOD_GET);
        cMsg.setProperty(org.wso2.ballerina.core.runtime.Constants.INTERMEDIATE_HEADERS, headers);
        msg.setValue(cMsg);
        Context ctx = new Context(cMsg);
        BValue[] inputArg = {msg};
        Functions.invoke(bFile, "testConvertToResponse", inputArg, ctx);
        Assert.assertTrue(msg.value().getHeaders().contains("test"), "Can't find header test.");
    }

    @Test
    public void testSetStatusCode() {
        DefaultCarbonMessage cMsg = new DefaultCarbonMessage();
        BMessage msg = new BMessage();
        msg.setValue(cMsg);
        Context ctx = new Context(cMsg);
        int httpSC = 200;
        BValue[] inputArgs = { msg, new BInteger(httpSC) };
        Functions.invoke(bFile, "testSetStatusCode", inputArgs, ctx);
        int sc = (int) msg.value().getProperty("HTTP_STATUS_CODE");
        Assert.assertEquals(sc, httpSC);
    }

    @Test(expectedExceptions = { BallerinaException.class },
          expectedExceptionsMessageRegExp = "Invalid message or Status Code")
    public void testSetStatusCodeWithString() {
        DefaultCarbonMessage cMsg = new DefaultCarbonMessage();
        BMessage msg = new BMessage();
        msg.setValue(cMsg);
        Context ctx = new Context(cMsg);
        BValue[] inputArgs = { msg, new BString("hello") };
        Functions.invoke(bFile, "testSetStatusCode", inputArgs, ctx);
    }

    @Test
    public void testSetContentLength() {
        DefaultCarbonMessage cMsg = new DefaultCarbonMessage();
        BMessage msg = new BMessage();
        msg.setValue(cMsg);
        Context ctx = new Context(cMsg);
        int length = 123;
        BValue[] inputArgs = { msg, new BInteger(length) };
        Functions.invoke(bFile, "testSetContentLength", inputArgs, ctx);
        String lenStr = msg.value().getHeader("Content-Length");
        Assert.assertEquals(Integer.parseInt(lenStr), length);
    }

    @Test
    public void testGetStatusCode() {
        DefaultCarbonMessage cMsg = new DefaultCarbonMessage();
        int httpSC = 200;
        cMsg.setProperty("HTTP_STATUS_CODE", httpSC);
        BMessage msg = new BMessage();
        msg.setValue(cMsg);
        Context ctx = new Context(cMsg);
        BValue[] inputArgs = { msg };
        BValue[] returnVals = Functions.invoke(bFile, "testGetStatusCode", inputArgs, ctx);
        Assert.assertEquals(returnVals.length, 1);
        BInteger sc = (BInteger) returnVals[0];
        Assert.assertEquals(sc.intValue(), httpSC);
    }

    @Test
    public void testGetContentLength() {
        DefaultCarbonMessage cMsg = new DefaultCarbonMessage();
        int cntLen = 123;
        cMsg.setHeader("Content-Length", String.valueOf(cntLen));
        BMessage msg = new BMessage();
        msg.setValue(cMsg);
        Context ctx = new Context(cMsg);
        BValue[] inputArgs = { msg };
        BValue[] returnVals = Functions.invoke(bFile, "testGetContentLength", inputArgs, ctx);
        Assert.assertEquals(returnVals.length, 1);
        BInteger sc = (BInteger) returnVals[0];
        Assert.assertEquals(sc.intValue(), cntLen);
    }

    @Test(expectedExceptions = { BallerinaException.class },
          expectedExceptionsMessageRegExp = "Invalid content length")
    public void testGetInvalidContentLength() {
        DefaultCarbonMessage cMsg = new DefaultCarbonMessage();
        cMsg.setHeader("Content-Length", String.valueOf("hello"));
        BMessage msg = new BMessage();
        msg.setValue(cMsg);
        Context ctx = new Context(cMsg);
        BValue[] inputArgs = { msg };
        BValue[] returnVals = Functions.invoke(bFile, "testGetContentLength", inputArgs, ctx);
    }

    @Test
    public void testSetReasonPhrase() {
        DefaultCarbonMessage cMsg = new DefaultCarbonMessage();
        BMessage msg = new BMessage();
        msg.setValue(cMsg);
        Context ctx = new Context(cMsg);
        String hello = "hello";
        BValue[] inputArgs = { msg, new BString(hello) };
        Functions.invoke(bFile, "testSetReasonPhrase", inputArgs, ctx);
        String reasonPhrase = (String) msg.value().getProperty("HTTP_REASON_PHRASE");
        Assert.assertEquals(reasonPhrase, hello);
    }


}
