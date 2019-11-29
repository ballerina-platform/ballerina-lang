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
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.test.util.TestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.jvm.util.BLangConstants.ANON_ORG;
import static org.ballerinalang.jvm.util.BLangConstants.DOT;

/**
 * Test class for gRPC unary service with array input/output.
 */
@Test(groups = "grpc-test")
public class UnaryBlockingArrayValueTestCase extends GrpcBaseTest {

    private BPackage defaultPkg = new BPackage(ANON_ORG, DOT);

    private CompileResult result;

    @BeforeClass(alwaysRun = true)
    private void setup() throws Exception {
        TestUtils.prepareBalo(this);
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "src", "clients",
                "02_array_field_type_client.bal");
        result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
    }

    @Test
    public void testIntArrayInputClient() {
        //TestInt intArray = {values:[1,2,3,4,5]};
        MapValue<String, Object> requestStruct = BallerinaValues.createRecordValue(defaultPkg, "TestInt");

        ArrayValue intArray = new ArrayValueImpl(new long[]{1L, 2L, 3L, 4L, 5L});
        requestStruct.put("values", intArray);

        BValue[] responses = BRunUtil.invoke(result, "testIntArrayInput", new Object[]{requestStruct});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BInteger);
        final BInteger response = (BInteger) responses[0];
        Assert.assertEquals(response.intValue(), 15);
    }

    @Test
    public void testStringArrayInputClient() {
        //TestString stringArray = {values:["A", "B", "C"]};
        MapValue<String, Object> requestStruct = BallerinaValues.createRecordValue(defaultPkg, "TestString");
        ArrayValue stringArray = new ArrayValueImpl(new String[] {"A", "B", "C"});
        requestStruct.put("values", stringArray);

        BValue[] responses = BRunUtil.invoke(result, "testStringArrayInput", new Object[]{requestStruct});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        final BString response = (BString) responses[0];
        Assert.assertEquals(response.stringValue(), ",A,B,C");
    }

    @Test
    public void testFloatArrayInputClient() {
        //TestFloat floatArray = {values:[1.1, 1.2, 1.3, 1.4, 1.5]};
        MapValue<String, Object> requestStruct = BallerinaValues.createRecordValue(defaultPkg, "TestFloat");
        ArrayValue floatArray = new ArrayValueImpl(new double[] {1.1, 1.2, 1.3, 1.4, 1.5});
        requestStruct.put("values", floatArray);

        BValue[] responses = BRunUtil.invoke(result, "testFloatArrayInput", new Object[]{requestStruct});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BFloat);
        final BFloat response = (BFloat) responses[0];
        Assert.assertEquals(response.floatValue(), 6.5);
    }

    @Test
    public void testBooleanArrayInputClient() {
        //TestBoolean booleanArray = {values:[true, false, true]};
        MapValue<String, Object> requestStruct = BallerinaValues.createRecordValue(defaultPkg, "TestBoolean");
        ArrayValue booleanArray = new ArrayValueImpl(new boolean[] {true, false, true});
        requestStruct.put("values", booleanArray);

        BValue[] responses = BRunUtil.invoke(result, "testBooleanArrayInput", new Object[]{requestStruct});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BBoolean);
        final BBoolean response = (BBoolean) responses[0];
        Assert.assertTrue(response.booleanValue());
    }

    @Test
    public void testStructArrayInputClient() {
        //    A a1 = {name: "Sam"};
        //    A a2 = {name: "John"};
        //    TestStruct structArray = {values:[a1, a2]};
        BValue[] responses = BRunUtil.invoke(result, "testStructArrayInput", new Object[]{});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        final BString response = (BString) responses[0];
        Assert.assertEquals(response.stringValue(), ",Sam,John");
    }

    @Test
    public void testIntArrayOutputClient() {
        BValue[] responses = BRunUtil.invoke(result, "testIntArrayOutput", new Object[]{});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BMap);
        final BMap<String, BValue> response = (BMap<String, BValue>) responses[0];
//        {values:[1, 2, 3, 4, 5]}
        Assert.assertTrue(response.get("values") instanceof BValueArray);
        final BValueArray structArray = (BValueArray) response.get("values");
        Assert.assertEquals(structArray.size(), 5);
        Assert.assertEquals(structArray.getInt(0), 1);
        Assert.assertEquals(structArray.getInt(1), 2);
        Assert.assertEquals(structArray.getInt(2), 3);
        Assert.assertEquals(structArray.getInt(3), 4);
        Assert.assertEquals(structArray.getInt(4), 5);
    }

    @Test
    public void testStringArrayOutputClient() {
        BValue[] responses = BRunUtil.invoke(result, "testStringArrayOutput", new Object[]{});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BMap);
        final BMap<String, BValue> response = (BMap<String, BValue>) responses[0];
//      {values:["A", "B", "C"]}
        Assert.assertTrue(response.get("values") instanceof BValueArray);
        final BValueArray structArray = (BValueArray) response.get("values");
        Assert.assertEquals(structArray.size(), 3);
        Assert.assertEquals(structArray.getString(0), "A");
        Assert.assertEquals(structArray.getString(1), "B");
        Assert.assertEquals(structArray.getString(2), "C");
    }

    @Test
    public void testFloatArrayOutputClient() {
        BValue[] responses = BRunUtil.invoke(result, "testFloatArrayOutput", new Object[]{});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BMap);
        final BMap<String, BValue> response = (BMap<String, BValue>) responses[0];
//      {values:[1.1, 1.2, 1.3, 1.4, 1.5]}
        Assert.assertTrue(response.get("values") instanceof BValueArray);
        final BValueArray structArray = (BValueArray) response.get("values");
        Assert.assertEquals(structArray.size(), 5);
        Assert.assertEquals(structArray.getFloat(0), 1.1);
        Assert.assertEquals(structArray.getFloat(1), 1.2);
        Assert.assertEquals(structArray.getFloat(2), 1.3);
    }

    @Test
    public void testBooleanArrayOutputClient() {
        BValue[] responses = BRunUtil.invoke(result, "testBooleanArrayOutput", new Object[]{});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BMap);
        final BMap<String, BValue> response = (BMap<String, BValue>) responses[0];
//      {values:[true, false, true]}
        Assert.assertTrue(response.get("values") instanceof BValueArray);
        final BValueArray structArray = (BValueArray) response.get("values");
        Assert.assertEquals(structArray.size(), 3);
        Assert.assertEquals(structArray.getBoolean(0), 1);
        Assert.assertEquals(structArray.getBoolean(1), 0);
        Assert.assertEquals(structArray.getBoolean(2), 1);
    }

    @Test
    public void testStructArrayOutputClient() {
        BValue[] responses = BRunUtil.invoke(result, "testStructArrayOutput", new Object[]{});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BMap);
        final BMap<String, BValue> response = (BMap<String, BValue>) responses[0];
//      {values:[{name:"Sam"}, {name:"John"}]}
        Assert.assertTrue(response.get("values") instanceof BValueArray);
        final BValueArray structArray = (BValueArray) response.get("values");
        Assert.assertEquals(structArray.size(), 2);
        Assert.assertEquals(((BMap<String, BValue>) structArray.getRefValue(0)).get("name").stringValue(), "Sam");
    }
}
