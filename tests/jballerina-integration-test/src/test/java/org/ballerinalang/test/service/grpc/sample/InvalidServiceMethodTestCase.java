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

import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.test.util.TestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test class for invalid service methods.
 */
@Test(groups = "grpc-test")
public class InvalidServiceMethodTestCase extends GrpcBaseTest {
    private CompileResult result;

    @BeforeClass
    private void setup() throws Exception {
        TestUtils.prepareBalo(this);
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "src", "clients",
                "05_invalid_resource_client.bal");
        result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
    }

    @Test(description = "Test invoking service method with invalid method name. Connector error is expected " +
            "with missing method descriptor.")
    public void testInvalidRemoteMethod() {
        BString request = new BString("WSO2");
        final String expectedMsg = "Error from Connector: {ballerina/grpc}InternalError - No registered method " +
                "descriptor for 'grpcservices.HelloWorld98/hello1'";

        BValue[] responses = BRunUtil.invoke(result, "testInvalidRemoteMethod", new BValue[]{request});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        Assert.assertEquals(responses[0].stringValue(), expectedMsg);
    }

    @Test(description = "Test invoking service method with invalid input value type. Null value is expected at " +
            "service level")
    public void testInvalidInputParameter() {
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
        BFloat request = new BFloat(1000.5);
        final String expectedMsg = "Error from Connector: {ballerina/grpc}InternalError - Error while constructing " +
                "the message";

        BValue[] responses = BRunUtil.invoke(result, "testInvalidOutputResponse", new BValue[]{request});
        Assert.assertEquals(responses.length, 1);
        Assert.assertEquals(responses[0].stringValue(), expectedMsg);
    }
}
