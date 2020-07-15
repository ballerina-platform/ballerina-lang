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
 * Test class for oneof field values.
 */
@Test(groups = "grpc-test")
public class UnaryBlockingOneofFieldTestCase extends GrpcBaseTest {

    private CompileResult result;

    @BeforeClass
    private void setup() throws Exception {
        TestUtils.prepareBalo(this);
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "src", "clients",
                "15_grpc_oneof_field_client.bal");
        result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
    }

    @Test(description = "Testing one of field value using blocking client")
    public void testOneofFieldValueClient() {
        final String serverMsg = "Hello Sam";
        BValue[] responses = BRunUtil.invoke(result, "testOneofFieldValue");
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        Assert.assertEquals(responses[0].stringValue(), serverMsg);
    }

    @Test(description = "Testing one of double field value using blocking client")
    public void testDoubleFieldValueClient() {
        final String serverMsg = "1.7976931348623157E308";
        BValue[] responses = BRunUtil.invoke(result, "testDoubleFieldValue");
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        Assert.assertEquals(responses[0].stringValue(), serverMsg);
    }

    @Test(description = "Testing one of float field value using blocking client")
    public void testFloatFieldValueClient() {
        final String serverMsg = "3.4028235E38";
        BValue[] responses = BRunUtil.invoke(result, "testFloatFieldValue");
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        Assert.assertEquals(responses[0].stringValue(), serverMsg);
    }

    @Test(description = "Testing one of int64 field value using blocking client", enabled = false)
    public void testInt64FieldValueClient() {
        final String serverMsg = "-9223372036854775808";
        BValue[] responses = BRunUtil.invoke(result, "testInt64FieldValue");
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        Assert.assertEquals(responses[0].stringValue(), serverMsg);
    }

    @Test(description = "Testing one of uint64 field value using blocking client")
    public void testUInt64FieldValueClient() {
        final String serverMsg = "9223372036854775807";
        BValue[] responses = BRunUtil.invoke(result, "testUInt64FieldValue");
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        Assert.assertEquals(responses[0].stringValue(), serverMsg);
    }

    @Test(description = "Testing one of int32 field value using blocking client")
    public void testInt32FieldValueClient() {
        final String serverMsg = "-2147483648";
        BValue[] responses = BRunUtil.invoke(result, "testInt32FieldValue");
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        Assert.assertEquals(responses[0].stringValue(), serverMsg);
    }

    @Test(description = "Testing one of fixed64 field value using blocking client")
    public void testFixed64FieldValueClient() {
        final String serverMsg = "9223372036854775807";
        BValue[] responses = BRunUtil.invoke(result, "testFixed64FieldValue");
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        Assert.assertEquals(responses[0].stringValue(), serverMsg);
    }

    @Test(description = "Testing one of fixed32 field value using blocking client")
    public void testFixed32FieldValueClient() {
        final String serverMsg = "2147483647";
        BValue[] responses = BRunUtil.invoke(result, "testFixed32FieldValue");
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        Assert.assertEquals(responses[0].stringValue(), serverMsg);
    }

    @Test(description = "Testing one of boolean field value using blocking client")
    public void testBolFieldValueClient() {
        final String serverMsg = "true";
        BValue[] responses = BRunUtil.invoke(result, "testBolFieldValue");
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        Assert.assertEquals(responses[0].stringValue(), serverMsg);
    }

    @Test(description = "Testing one of string field value using blocking client")
    public void testStringFieldValueClient() {
        final String serverMsg = "Testing";
        BValue[] responses = BRunUtil.invoke(result, "testStringFieldValue");
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        Assert.assertEquals(responses[0].stringValue(), serverMsg);
    }

    @Test(description = "Testing one of message field value using blocking client")
    public void testMessageFieldValueClient() {
        final String serverMsg = "Testing";
        BValue[] responses = BRunUtil.invoke(result, "testMessageFieldValue");
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        Assert.assertEquals(responses[0].stringValue(), serverMsg);
    }

    @Test(description = "Testing one of byte field value using blocking client")
    public void testBytesFieldValueClient() {
        final String serverMsg = "true";
        BValue[] responses = BRunUtil.invoke(result, "testBytesFieldValue");
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        Assert.assertEquals(responses[0].stringValue(), serverMsg);
    }
}
