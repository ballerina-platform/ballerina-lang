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
package org.ballerinalang.net.http.nativeimpl.request;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Test cases for ballerina.net.http.request native functions.
 */
public class RequestNativeFunctionTest {

    private ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("net/http/nativeimpl/request/requestNativeFunctionSuccess.bal");
    }

    @Test
    public void testGetMethod() {
        BStruct request = BTestUtils.createAndGetStruct(programFile,
                Constants.PROTOCOL_PACKAGE_HTTP, Constants.REQUEST);
        HTTPCarbonMessage cMsg = new HTTPCarbonMessage();

        request.addNativeData(Constants.TRANSPORT_MESSAGE, cMsg);

        String headerName = "header1";
        String headerValue = "headerValue";
        Context ctx = new Context(programFile);
        BString key = new BString(headerName);
        BString value = new BString(headerValue);
        BValue[] inputArg = {request, key, value};
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testAddHeader", inputArg, ctx);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        HTTPCarbonMessage modified = (HTTPCarbonMessage) ((BStruct) returnVals[0])
                .getNativeData(Constants.TRANSPORT_MESSAGE);
        Assert.assertEquals(modified.getHeader(headerName), headerValue);
    }

    @Test
    public void testCloneMethod() {
        //TODO
    }

    @Test
    public void testGetBinaryPayloadMethod() {
        BStruct request = BTestUtils.createAndGetStruct(programFile,
                Constants.PROTOCOL_PACKAGE_HTTP, Constants.REQUEST);
        HTTPCarbonMessage cMsg = new HTTPCarbonMessage();


        request.addNativeData(Constants.TRANSPORT_MESSAGE, cMsg);

        String headerName = "header1";
        String headerValue = "headerValue";
        Context ctx = new Context(programFile);
        BString key = new BString(headerName);
        BString value = new BString(headerValue);
        BValue[] inputArg = {request, key, value};
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testAddHeader", inputArg, ctx);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStruct);
        HTTPCarbonMessage modified = (HTTPCarbonMessage) ((BStruct) returnVals[0])
                .getNativeData(Constants.TRANSPORT_MESSAGE);
        Assert.assertEquals(modified.getHeader(headerName), headerValue);
    }

}
