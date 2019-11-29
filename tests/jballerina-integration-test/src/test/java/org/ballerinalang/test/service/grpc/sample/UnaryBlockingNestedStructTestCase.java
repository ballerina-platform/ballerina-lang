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
 * Test class for gRPC unary service with nested struct input/output.
 */
@Test(groups = "grpc-test")
public class UnaryBlockingNestedStructTestCase extends GrpcBaseTest {

    private CompileResult result;

    @BeforeClass
    private void setup() throws Exception {
        TestUtils.prepareBalo(this);
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "src", "clients", "01_advanced_type_client" +
                ".bal");
        result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
    }

    @Test
    public void testInputNestedStructClient() {
        BValue[] responses = BRunUtil.invoke(result, "testInputNestedStruct", new Object[]{});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        Assert.assertEquals(responses[0].stringValue(), "Client got response: Submitted name: Sam");
    }

    @Test
    public void testOutputNestedStructClient() {
        BValue[] responses = BRunUtil.invoke(result, "testOutputNestedStruct", new Object[]{"WSO2"});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BMap);
        final BMap<String, BValue> response = (BMap<String, BValue>) responses[0];
        // Person person = {name: "Sam", address: {postalCode:10300, state:"CA", country:"USA"}};
        Assert.assertEquals(response.get("name").stringValue(), "Sam");
        Assert.assertTrue(response.get("address") instanceof BMap);
        final BMap<String, BValue> nestedStruct = (BMap<String, BValue>) response.get("address");
        Assert.assertEquals(((BInteger) nestedStruct.get("postalCode")).intValue(), 10300);
        Assert.assertEquals(nestedStruct.get("state").stringValue(), "CA");
        Assert.assertEquals(nestedStruct.get("country").stringValue(), "USA");
    }

    @Test
    public void testInputStructOutputStruct() {
        BValue[] responses = BRunUtil.invoke(result, "testInputStructOutputStruct", new BValue[]{});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BMap);
        final BMap<String, BValue> response = (BMap<String, BValue>) responses[0];
        // StockQuote res = {symbol: "WSO2", name: "WSO2.com", last: 149.52, low: 150.70, high:
        //        149.18};
        Assert.assertEquals(response.get("symbol").stringValue(), "WSO2");
        Assert.assertEquals(response.get("name").stringValue(), "WSO2.com");
        Assert.assertEquals(((BFloat) response.get("last")).floatValue(), 149.52);
        Assert.assertEquals(((BFloat) response.get("low")).floatValue(), 150.70);
        Assert.assertEquals(((BFloat) response.get("high")).floatValue(), 149.18);
    }
}
