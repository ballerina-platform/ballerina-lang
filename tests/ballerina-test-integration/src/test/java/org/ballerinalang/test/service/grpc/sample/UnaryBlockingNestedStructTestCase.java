/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.service.grpc.sample;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test class for gRPC unary service with nested struct input/output.
 *
 */
public class UnaryBlockingNestedStructTestCase extends IntegrationTestCase {

    private ServerInstance ballerinaServer;
    
    @BeforeClass
    private void setup() throws Exception {
        ballerinaServer = ServerInstance.initBallerinaServer(9090);
        Path serviceBalPath = Paths.get("src", "test", "resources", "grpc", "advanced_type_service.bal");
        ballerinaServer.startBallerinaServer(serviceBalPath.toAbsolutePath().toString());
    }

    @Test
    public void testInputNestedStructClient() {
        //Person p = {name:"Danesh", address:{postalCode:10300, state:"Western", country:"Sri Lanka"}};
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "advanced_type_client.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        PackageInfo packageInfo = result.getProgFile().getPackageInfo(".");
        // Address struct
        StructInfo addressInfo = packageInfo.getStructInfo("Address");
        BStructType addressType = addressInfo.getType();
        BStruct addressStruct = new BStruct(addressType);
        addressStruct.setIntField(0, 10300);
        addressStruct.setStringField(0, "Western");
        addressStruct.setStringField(1, "Sri Lanka");
        // Person struct
        StructInfo personInfo = packageInfo.getStructInfo("Person");
        BStructType personType = personInfo.getType();
        BStruct personStruct = new BStruct(personType);
        personStruct.setStringField(0, "Sam");
        personStruct.setRefField(0, addressStruct);

        BValue[] responses = BRunUtil.invoke(result, "testInputNestedStruct", new BValue[]{personStruct});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        Assert.assertEquals(responses[0].stringValue(), "Client got response: Submitted name: Sam");
    }

    @Test
    public void testOutputNestedStructClient() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "advanced_type_client.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        BString request = new BString("WSO2");

        BValue[] responses = BRunUtil.invoke(result, "testOutputNestedStruct", new BValue[]{request});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BStruct);
        final BStruct response = (BStruct) responses[0];
        // Person person = {name: "Sam", address: {postalCode:10300, state:"CA", country:"USA"}};
        Assert.assertEquals(response.getStringField(0), "Sam");
        Assert.assertTrue(response.getRefField(0) instanceof BStruct);
        final BStruct nestedStruct = (BStruct) response.getRefField(0);
        Assert.assertEquals(nestedStruct.getIntField(0), 10300);
        Assert.assertEquals(nestedStruct.getStringField(0), "CA");
        Assert.assertEquals(nestedStruct.getStringField(1), "USA");
    }

    @Test
    public void testInputStructOutputStruct() {
        //StockRequest request = {name: "WSO2"};
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "advanced_type_client.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        PackageInfo packageInfo = result.getProgFile().getPackageInfo(".");
        // Address struct
        StructInfo requestInfo = packageInfo.getStructInfo("StockRequest");
        BStructType requestType = requestInfo.getType();
        BStruct requestStruct = new BStruct(requestType);
        requestStruct.setStringField(0, "WSO2");

        BValue[] responses = BRunUtil.invoke(result, "testInputStructOutputStruct", new BValue[]{requestStruct});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BStruct);
        final BStruct response = (BStruct) responses[0];
        // StockQuote res = {symbol: "WSO2", name: "WSO2.com", last: 149.52, low: 150.70, high:
        //        149.18};
        Assert.assertEquals(response.getStringField(0), "WSO2");
        Assert.assertEquals(response.getStringField(1), "WSO2.com");
        Assert.assertEquals(response.getFloatField(0), 149.52);
        Assert.assertEquals(response.getFloatField(1), 150.70);
        Assert.assertEquals(response.getFloatField(2), 149.18);
    }
    
    @AfterClass
    private void cleanup() throws BallerinaTestException {
        ballerinaServer.stopServer();
    }
}
