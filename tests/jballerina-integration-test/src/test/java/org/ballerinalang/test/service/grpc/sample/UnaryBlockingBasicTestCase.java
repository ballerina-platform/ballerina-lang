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

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
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
 * Test class for gRPC unary service with blocking and non-blocking client.
 */
@Test(groups = "grpc-test")
public class UnaryBlockingBasicTestCase extends GrpcBaseTest {

    private CompileResult result;

    @BeforeClass
    private void setup() throws Exception {
        TestUtils.prepareBalo(this);
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "clients", "07_unary_blocking_client.bal");
        result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
    }

    @Test
    public void testBlockingBallerinaClient() {
        final String serverMsg = "Hello WSO2";

        BValue[] responses = BRunUtil.invoke(result, "testUnaryBlockingClient", new Object[]{"WSO2"});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        Assert.assertEquals(responses[0].stringValue(), "Client got response: " + serverMsg);
    }

    @Test
    public void testBlockingErrorResponse() {
        final String serverMsg = "Error from Connector: {ballerina/grpc}ABORTED - Operation aborted";

        BValue[] responses = BRunUtil.invoke(result, "testUnaryBlockingClient", new Object[]{"invalid"});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        Assert.assertEquals(responses[0].stringValue(), serverMsg);
    }

    @Test
    public void testIntBlockingBallerinaClient() {
        final int serverMsg = 8;

        BValue[] responses = BRunUtil.invoke(result, "testUnaryBlockingIntClient", new Object[]{10});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BInteger);
        Assert.assertEquals(Integer.parseInt(responses[0].stringValue()), serverMsg);
    }

    @Test
    public void testFloatBlockingBallerinaClient() {
        final double response = 880.44;

        BValue[] responses = BRunUtil.invoke(result, "testUnaryBlockingFloatClient", new Object[]{1000.5});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BFloat);
        Assert.assertEquals(Double.parseDouble(responses[0].stringValue()), response);
    }

    @Test
    public void testBooleanBlockingBallerinaClient() {
        final boolean response = true;
        BValue[] responses = BRunUtil.invoke(result, "testUnaryBlockingBoolClient", new Object[]{Boolean.FALSE});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BBoolean);
        Assert.assertEquals(Boolean.parseBoolean(responses[0].stringValue()), response);
    }

    @Test
    public void testStructBlockingBallerinaClient() {
        MapValue<String, Object> request = BallerinaValues.createRecordValue(".", "Request");
        request.put("name", "Sam");
        request.put("message", "Testing.");
        request.put("age", 10);

        BValue[] responses = BRunUtil.invoke(result, "testUnaryBlockingStructClient", new Object[]{request});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BMap);
        final BMap<String, BValue> response = (BMap<String, BValue>) responses[0];
        Assert.assertEquals(response.get("resp").stringValue(), "Acknowledge Sam");
    }

    @Test(description = "Test deriving gRPC service response type when send expression inside match statement")
    public void testResponseInsideMatch() {
        final String serverMsg = "Acknowledge WSO2";

        BValue[] responses = BRunUtil.invoke(result, "testResponseInsideMatch", new Object[]{"WSO2"});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BMap);
        final BMap<String, BValue> response = (BMap<String, BValue>) responses[0];
        Assert.assertEquals(response.get("resp").stringValue(), serverMsg);
    }

    @Test
    public void testNonBlockingBallerinaClient() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "clients", "07_unary_nonblocking_client.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());

        BValue[] responses = BRunUtil.invoke(result, "testUnaryNonBlockingClient", new Object[]{});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BBoolean);
        BBoolean response = (BBoolean) responses[0];
        Assert.assertTrue(response.booleanValue());
    }
}
