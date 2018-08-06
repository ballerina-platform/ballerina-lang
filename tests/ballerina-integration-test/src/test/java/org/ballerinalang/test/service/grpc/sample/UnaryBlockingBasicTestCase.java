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
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.util.TestUtils;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Test class for gRPC unary service with blocking and non-blocking client.
 */
@Test(groups = "grpc-test")
public class UnaryBlockingBasicTestCase extends BaseTest {

    private CompileResult result;

    @BeforeClass
    private void setup() throws Exception {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "clients", "unary1_blocking_client.bal");
        result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        TestUtils.prepareBalo(this);
    }

    @Test
    public void testBlockingBallerinaClient() {
        BString request = new BString("WSO2");
        final String serverMsg = "Hello WSO2";

        BValue[] responses = BRunUtil.invoke(result, "testUnaryBlockingClient", new BValue[]{request});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        Assert.assertEquals(responses[0].stringValue(), "Client got response: " + serverMsg);
    }

    @Test
    public void testBlockingErrorResponse() {
        BString request = new BString("invalid");
        final String serverMsg = "Error from Connector: Status{ code ABORTED, description Operation aborted, cause " +
                "null}";

        BValue[] responses = BRunUtil.invoke(result, "testUnaryBlockingClient", new BValue[]{request});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        Assert.assertEquals(responses[0].stringValue(), serverMsg);
    }

    @Test
    public void testIntBlockingBallerinaClient() {
        BInteger request = new BInteger(10);
        final int serverMsg = 8;

        BValue[] responses = BRunUtil.invoke(result, "testUnaryBlockingIntClient", new BValue[]{request});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BInteger);
        Assert.assertEquals(Integer.parseInt(responses[0].stringValue()), serverMsg);
    }

    @Test
    public void testFloatBlockingBallerinaClient() {
        BFloat request = new BFloat(1000.5);
        final double response = 880.44;

        BValue[] responses = BRunUtil.invoke(result, "testUnaryBlockingFloatClient", new BValue[]{request});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BFloat);
        Assert.assertEquals(Double.parseDouble(responses[0].stringValue()), response);
    }

    @Test
    public void testBooleanBlockingBallerinaClient() {
        BBoolean request = new BBoolean(false);
        final boolean response = true;

        BValue[] responses = BRunUtil.invoke(result, "testUnaryBlockingBoolClient", new BValue[]{request});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BBoolean);
        Assert.assertEquals(Boolean.parseBoolean(responses[0].stringValue()), response);
    }

    @Test
    public void testStructBlockingBallerinaClient() {
        PackageInfo httpPackageInfo = result.getProgFile().getPackageInfo(".");
        StructureTypeInfo structInfo = httpPackageInfo.getStructInfo("Request");
        BStructureType structType = structInfo.getType();
        BMap<String, BValue> request = new BMap<String, BValue>(structType);
        request.put("name", new BString("Sam"));
        request.put("message", new BString("Testing."));
        request.put("age", new BInteger(10));

        BValue[] responses = BRunUtil.invoke(result, "testUnaryBlockingStructClient", new BValue[]{request});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BMap);
        final BMap<String, BValue> response = (BMap<String, BValue>) responses[0];
        Assert.assertEquals(response.get("resp").stringValue(), "Acknowledge Sam");
    }

    @Test
    public void testNonBlockingBallerinaClient() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "clients", "unary1_nonblocking_client.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        final String serverMsg = "Hello WSO2";

        BValue[] responses = BRunUtil.invoke(result, "testUnaryNonBlockingClient", new BValue[]{});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BStringArray);
        BStringArray responseValues = (BStringArray) responses[0];
        Assert.assertEquals(responseValues.size(), 2);
        Assert.assertTrue(Stream.of(responseValues.getStringArray()).anyMatch(serverMsg::equals));
        Assert.assertTrue(Stream.of(responseValues.getStringArray()).anyMatch(("Server Complete Sending Response" +
                ".")::equals));
    }
}
