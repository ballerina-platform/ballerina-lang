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
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
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
 * Test class for gRPC unary service with array input/output.
 *
 */
public class UnaryBlockingArrayValueTestCase extends IntegrationTestCase {

    private ServerInstance ballerinaServer;
    
    @BeforeClass
    private void setup() throws Exception {
        ballerinaServer = ServerInstance.initBallerinaServer(9090);
        Path serviceBalPath = Paths.get("src", "test", "resources", "grpc", "array_field_type_service.bal");
        ballerinaServer.startBallerinaServer(serviceBalPath.toAbsolutePath().toString());
    }

    @Test
    public void testIntArrayInputClient() {
        //TestInt intArray = {values:[1,2,3,4,5]};
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "array_field_type_client.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        PackageInfo packageInfo = result.getProgFile().getPackageInfo(".");
        StructInfo requestInfo = packageInfo.getStructInfo("TestInt");
        BStructType requestType = requestInfo.getType();
        BStruct requestStruct = new BStruct(requestType);
        BIntArray intArray = new BIntArray();
        intArray.add(0, 1);
        intArray.add(1, 2);
        intArray.add(2, 3);
        intArray.add(3, 4);
        intArray.add(4, 5);
        requestStruct.setRefField(0, intArray);

        BValue[] responses = BRunUtil.invoke(result, "testIntArrayInput", new BValue[]{requestStruct});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BInteger);
        final BInteger response = (BInteger) responses[0];
        Assert.assertEquals(response.intValue(), 15);
    }

    @Test
    public void testStringArrayInputClient() {
        //TestString stringArray = {values:["A", "B", "C"]};
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "array_field_type_client.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        PackageInfo packageInfo = result.getProgFile().getPackageInfo(".");
        StructInfo requestInfo = packageInfo.getStructInfo("TestString");
        BStructType requestType = requestInfo.getType();
        BStruct requestStruct = new BStruct(requestType);
        BStringArray stringArray = new BStringArray();
        stringArray.add(0, "A");
        stringArray.add(1, "B");
        stringArray.add(2, "C");
        requestStruct.setRefField(0, stringArray);

        BValue[] responses = BRunUtil.invoke(result, "testStringArrayInput", new BValue[]{requestStruct});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        final BString response = (BString) responses[0];
        Assert.assertEquals(response.stringValue(), ",A,B,C");
    }

    @Test
    public void testFloatArrayInputClient() {
        //TestFloat floatArray = {values:[1.1, 1.2, 1.3, 1.4, 1.5]};
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "array_field_type_client.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        PackageInfo packageInfo = result.getProgFile().getPackageInfo(".");
        StructInfo requestInfo = packageInfo.getStructInfo("TestFloat");
        BStructType requestType = requestInfo.getType();
        BStruct requestStruct = new BStruct(requestType);
        BFloatArray floatArray = new BFloatArray();
        floatArray.add(0, 1.1);
        floatArray.add(1, 1.2);
        floatArray.add(2, 1.3);
        floatArray.add(3, 1.4);
        floatArray.add(4, 1.5);
        requestStruct.setRefField(0, floatArray);

        BValue[] responses = BRunUtil.invoke(result, "testFloatArrayInput", new BValue[]{requestStruct});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BFloat);
        final BFloat response = (BFloat) responses[0];
        Assert.assertEquals(response.floatValue(), 6.5);
    }

    @Test
    public void testBooleanArrayInputClient() {
        //TestBoolean booleanArray = {values:[true, false, true]};
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "array_field_type_client.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        PackageInfo packageInfo = result.getProgFile().getPackageInfo(".");
        StructInfo requestInfo = packageInfo.getStructInfo("TestBoolean");
        BStructType requestType = requestInfo.getType();
        BStruct requestStruct = new BStruct(requestType);
        BBooleanArray booleanArray = new BBooleanArray();
        booleanArray.add(0, 1);
        booleanArray.add(1, 0);
        booleanArray.add(2, 1);
        requestStruct.setRefField(0, booleanArray);

        BValue[] responses = BRunUtil.invoke(result, "testBooleanArrayInput", new BValue[]{requestStruct});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BBoolean);
        final BBoolean response = (BBoolean) responses[0];
        Assert.assertEquals(response.booleanValue(), true);
    }

    @Test
    public void testStructArrayInputClient() {
        //    A a1 = {name: "Sam"};
        //    A a2 = {name: "John"};
        //    TestStruct structArray = {values:[a1, a2]};
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "array_field_type_client.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        PackageInfo packageInfo = result.getProgFile().getPackageInfo(".");
        StructInfo requestInfo = packageInfo.getStructInfo("TestStruct");
        BStructType requestType = requestInfo.getType();
        BStruct requestStruct = new BStruct(requestType);
        BRefValueArray refArray = new BRefValueArray();
        StructInfo aInfo = packageInfo.getStructInfo("A");
        BStructType aType = aInfo.getType();
        BStruct a1 = new BStruct(aType);
        a1.setStringField(0, "Sam");
        BStruct a2 = new BStruct(aType);
        a2.setStringField(0, "John");
        refArray.add(0, a1);
        refArray.add(1, a2);
        requestStruct.setRefField(0, refArray);

        BValue[] responses = BRunUtil.invoke(result, "testStructArrayInput", new BValue[]{requestStruct});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        final BString response = (BString) responses[0];
        Assert.assertEquals(response.stringValue(), ",Sam,John");
    }

    @Test
    public void testIntArrayOutputClient() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "array_field_type_client.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());

        BValue[] responses = BRunUtil.invoke(result, "testIntArrayOutput", new BValue[]{});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BStruct);
        final BStruct response = (BStruct) responses[0];
//        {values:[1, 2, 3, 4, 5]}
        Assert.assertTrue(response.getRefField(0) instanceof BIntArray);
        final BIntArray structArray = (BIntArray) response.getRefField(0);
        Assert.assertEquals(structArray.size(), 5);
        Assert.assertEquals(structArray.get(0), 1);
        Assert.assertEquals(structArray.get(1), 2);
        Assert.assertEquals(structArray.get(2), 3);
        Assert.assertEquals(structArray.get(3), 4);
        Assert.assertEquals(structArray.get(4), 5);
    }

    @Test
    public void testStringArrayOutputClient() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "array_field_type_client.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());

        BValue[] responses = BRunUtil.invoke(result, "testStringArrayOutput", new BValue[]{});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BStruct);
        final BStruct response = (BStruct) responses[0];
//      {values:["A", "B", "C"]}
        Assert.assertTrue(response.getRefField(0) instanceof BStringArray);
        final BStringArray structArray = (BStringArray) response.getRefField(0);
        Assert.assertEquals(structArray.size(), 3);
        Assert.assertEquals(structArray.get(0), "A");
        Assert.assertEquals(structArray.get(1), "B");
        Assert.assertEquals(structArray.get(2), "C");
    }

    @Test
    public void testFloatArrayOutputClient() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "array_field_type_client.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());

        BValue[] responses = BRunUtil.invoke(result, "testFloatArrayOutput", new BValue[]{});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BStruct);
        final BStruct response = (BStruct) responses[0];
//      {values:[1.1, 1.2, 1.3, 1.4, 1.5]}
        Assert.assertTrue(response.getRefField(0) instanceof BFloatArray);
        final BFloatArray structArray = (BFloatArray) response.getRefField(0);
        Assert.assertEquals(structArray.size(), 5);
        Assert.assertEquals(structArray.get(0), 1.1);
        Assert.assertEquals(structArray.get(1), 1.2);
        Assert.assertEquals(structArray.get(2), 1.3);
    }

    @Test
    public void testBooleanArrayOutputClient() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "array_field_type_client.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());

        BValue[] responses = BRunUtil.invoke(result, "testBooleanArrayOutput", new BValue[]{});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BStruct);
        final BStruct response = (BStruct) responses[0];
//      {values:[true, false, true]}
        Assert.assertTrue(response.getRefField(0) instanceof BBooleanArray);
        final BBooleanArray structArray = (BBooleanArray) response.getRefField(0);
        Assert.assertEquals(structArray.size(), 3);
        Assert.assertEquals(structArray.get(0), 1);
        Assert.assertEquals(structArray.get(1), 0);
        Assert.assertEquals(structArray.get(2), 1);
    }

    @Test
    public void testStructArrayOutputClient() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "array_field_type_client.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());

        BValue[] responses = BRunUtil.invoke(result, "testStructArrayOutput", new BValue[]{});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BStruct);
        final BStruct response = (BStruct) responses[0];
//      {values:[{name:"Sam"}, {name:"John"}]}
        Assert.assertTrue(response.getRefField(0) instanceof BRefValueArray);
        final BRefValueArray structArray = (BRefValueArray) response.getRefField(0);
        Assert.assertEquals(structArray.size(), 2);
        Assert.assertEquals(((BStruct) structArray.get(0)).getStringField(0), "Sam");
    }
    
    @AfterClass
    private void cleanup() throws BallerinaTestException {
        ballerinaServer.stopServer();
    }
}
