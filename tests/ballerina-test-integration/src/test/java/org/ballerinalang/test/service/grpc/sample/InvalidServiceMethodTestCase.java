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
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.util.TestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test class for invalid service methods.
 */
public class InvalidServiceMethodTestCase extends IntegrationTestCase {

    private ServerInstance ballerinaServer;
    
    @BeforeClass
    private void setup() throws Exception {
        ballerinaServer = ServerInstance.initBallerinaServer(9090);
        Path serviceBalPath = Paths.get("src", "test", "resources", "grpc", "invalid_resource_service.bal");
        ballerinaServer.startBallerinaServer(serviceBalPath.toAbsolutePath().toString());
        TestUtils.prepareBalo(this);
    }

    @Test(description = "Test invoking service method with invalid method name. Connector error is expected " +
            "with missing method descriptor.")
    public void testInvalidRemoteMethod() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "invalid_resource_client.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        BString request = new BString("WSO2");
        final String expectedMsg = "Error from Connector: No registered method descriptor for 'HelloWorld/hello1'";

        BValue[] responses = BRunUtil.invoke(result, "testInvalidRemoteMethod", new BValue[]{request});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        Assert.assertEquals(responses[0].stringValue(), expectedMsg);
    }

    @Test(description = "Test invoking service method with invalid input value type. Null value is expected at " +
            "service level")
    public void testInvalidInputParameter() {

        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "invalid_resource_client.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        BInteger request = new BInteger(10);
        final int serverMsg = -1;

        BValue[] responses = BRunUtil.invoke(result, "testInvalidInputParameter", new BValue[]{request});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BInteger);
        Assert.assertEquals(Integer.parseInt(responses[0].stringValue()), serverMsg);
    }

    @Test(description = "Test invoking service method and expecting response value with different type. Connector " +
            "error is expected with Invalid protobuf byte sequence")
    public void testInvalidOutputResponse() {

        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "invalid_resource_client.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        BFloat request = new BFloat(1000.5);
        final String expectedMsg = "Error from Connector: Status{ code CANCELLED, description Failed to read message" +
                "., cause INTERNAL: Invalid protobuf byte sequence}";

        BValue[] responses = BRunUtil.invoke(result, "testInvalidOutputResponse", new BValue[]{request});
        Assert.assertEquals(responses.length, 1);
        Assert.assertEquals(responses[0].stringValue(), expectedMsg);
    }

    @Test(description = "Test invoking non existence remote method. Connector error is expected with method not found" +
            " message")
    public void testNonExistenceRemoteMethod() {

        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "invalid_resource_client.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        BBoolean request = new BBoolean(false);
        final String expectedMsg = "Error from Connector: Status{ code UNIMPLEMENTED, description HTTP status code " +
                "404\n" +
                "invalid content-type: text/plain\n" +
                "MESSAGE DATA: method not found: HelloWorld/testBoolean, cause null}";

        BValue[] responses = BRunUtil.invoke(result, "testNonExistenceRemoteMethod", new BValue[]{request});
        Assert.assertEquals(responses.length, 1);
        Assert.assertEquals(responses[0].stringValue(), expectedMsg);
    }
    
    @AfterClass
    private void cleanup() throws BallerinaTestException {
        ballerinaServer.stopServer();
    }
}
