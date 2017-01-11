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
package org.wso2.ballerina.core.nativeimpl.functions;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.connectors.http.Constants;
import org.wso2.ballerina.core.runtime.internal.BuiltInNativeConstructLoader;
import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;
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
        bFile = ParserUtils.parseBalFile("samples/nativeimpl/netHttp.bal", symScope);
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


}
