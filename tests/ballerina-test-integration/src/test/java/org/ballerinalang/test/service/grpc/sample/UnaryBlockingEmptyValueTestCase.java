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
import org.ballerinalang.model.values.BRefValueArray;
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
 * Test class for gRPC unary service with empty input/output.
 *
 */
public class UnaryBlockingEmptyValueTestCase extends IntegrationTestCase {

    private ServerInstance ballerinaServer;
    
    @BeforeClass
    private void setup() throws Exception {
        ballerinaServer = ServerInstance.initBallerinaServer(9090);
        Path serviceBalPath = Paths.get("src", "test", "resources", "grpc", "advanced_type_service.bal");
        ballerinaServer.startBallerinaServer(serviceBalPath.toAbsolutePath().toString());
    }

    @Test
    public void testNoInputOutputStructClient() {
        //Person p = {name:"Danesh", address:{postalCode:10300, state:"Western", country:"Sri Lanka"}};
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "advanced_type_client.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());

        BValue[] responses = BRunUtil.invoke(result, "testNoInputOutputStruct", new BValue[]{});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BStruct);
        final BStruct response = (BStruct) responses[0];
//        StockQuote res = {symbol: "WSO2", name: "WSO2 Inc.", last: 14, low: 15, high: 16};
//        StockQuote res1 = {symbol: "Google", name: "Google Inc.", last: 100, low: 101, high: 102};
        Assert.assertTrue(response.getRefField(0) instanceof BRefValueArray);
        final BRefValueArray structArray = (BRefValueArray) response.getRefField(0);
        Assert.assertEquals(structArray.size(), 2);
    }

    @Test
    public void testInputStructNoOutputClient() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "advanced_type_client.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        PackageInfo packageInfo = result.getProgFile().getPackageInfo(".");
        // Stock Quote struct
        // StockQuote quote2 = {symbol: "Ballerina", name:"ballerina.io", last:1.0, low:0.5, high:2.0};
        StructInfo requestInfo = packageInfo.getStructInfo("StockQuote");
        BStructType requestType = requestInfo.getType();
        BStruct requestStruct = new BStruct(requestType);
        requestStruct.setStringField(0, "Ballerina");
        requestStruct.setStringField(1, "ballerina.io");
        requestStruct.setFloatField(0, 1.0);
        requestStruct.setFloatField(1, 0.5);
        requestStruct.setFloatField(2, 2.0);

        BValue[] responses = BRunUtil.invoke(result, "testInputStructNoOutput", new BValue[]{requestStruct});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        final BString response = (BString) responses[0];
        Assert.assertEquals(response.stringValue() , "No Response");
    }
    
    @AfterClass
    private void cleanup() throws BallerinaTestException {
        ballerinaServer.stopServer();
    }
}
