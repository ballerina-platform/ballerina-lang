/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.javainterop;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import io.ballerina.runtime.internal.values.ErrorValue;
import io.ballerina.runtime.internal.values.FPValue;
import io.ballerina.runtime.internal.values.FutureValue;
import io.ballerina.runtime.internal.values.HandleValue;
import io.ballerina.runtime.internal.values.TypedescValue;
import io.ballerina.runtime.internal.values.TypedescValueImpl;
import io.ballerina.runtime.internal.values.XmlItem;
import io.ballerina.runtime.internal.values.XmlValue;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Test cases for java interop with ballerina ref types.
 *
 * @since 1.0.0
 */
public class RefTypeTests {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/javainterop/ballerina_types_as_interop_types.bal");
    }

    @Test(description = "Test interoperability with ballerina array value as return and map as a param")
    public void testInteropWithBalArrayAndMap() {
        Object returns = BRunUtil.invoke(result, "interopWithArrayAndMap");

        Assert.assertTrue(returns instanceof BArray);
        BArray arr = (BArray) returns;
        Assert.assertEquals(arr.toString(), "[1,8]");
    }

    @Test(description = "Test interoperability with ballerina service type value as a param")
    public void testInteropWithServiceTypesAndStringReturn() {
        Object returns = BRunUtil.invoke(result, "acceptServiceAndBooleanReturn");

        Assert.assertTrue(returns instanceof Boolean);
        Assert.assertTrue((Boolean) returns);
    }

    @Test(description = "Test interoperability with ballerina ref types as params and map return")
    public void testInteropWithRefTypesAndMapReturn() {
        Object returns = BRunUtil.invoke(result, "interopWithRefTypesAndMapReturn");

        Assert.assertTrue(returns instanceof BMap);
        BMap bMap = (BMap) returns;
        Assert.assertEquals(bMap.toString(), "{\"a\":object Person,\"b\":[5,\"hello\",object Person]," +
                "\"c\":{\"name\":\"sameera\"},\"e\":object Person,\"f\":83,\"g\":{\"name\":\"sample\"}}");
    }

    @Test(description = "Test interoperability with ballerina error return")
    public void testInteropWithErrorReturn() {
        Object returns = BRunUtil.invoke(result, "interopWithErrorReturn");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "example error with given reason");
    }

    @Test(description = "Test interoperability with ballerina union return")
    public void testInteropWithUnionReturn() {
        Object returns = BRunUtil.invoke(result, "interopWithUnionReturn");

        Assert.assertTrue(returns instanceof Boolean);
        Assert.assertTrue((Boolean) returns);
    }

    @Test(description = "Test interoperability with ballerina error union return")
    public void testInteropWithErrorUnionReturn() {
        BRunUtil.invoke(result, "testInteropWithErrorUnionReturn");
    }

    @Test(description = "Test interoperability with ballerina object return")
    public void testInteropWithObjectReturn() {
        Object returns = BRunUtil.invoke(result, "interopWithObjectReturn");

        Assert.assertTrue(returns instanceof Boolean);
        Assert.assertTrue((Boolean) returns);
    }

    @Test(description = "Test interoperability with ballerina record return")
    public void testInteropWithRecordReturn() {
        Object returns = BRunUtil.invoke(result, "interopWithRecordReturn");

        Assert.assertTrue(returns instanceof Boolean);
        Assert.assertTrue((Boolean) returns);
    }

    @Test(description = "Test interoperability with ballerina any return")
    public void testInteropWithAnyReturn() {
        Object returns = BRunUtil.invoke(result, "interopWithAnyReturn");

        Assert.assertTrue(returns instanceof Boolean);
        Assert.assertTrue((Boolean) returns);
    }

    @Test(description = "Test interoperability with ballerina anydata return")
    public void testInteropWithAnydataReturn() {
        Object returns = BRunUtil.invoke(result, "interopWithAnydataReturn");

        Assert.assertTrue(returns instanceof Boolean);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void interopWithHandleOrErrorReturn() {
        BRunUtil.invoke(result, "interopWithHandleOrErrorReturn");
    }

    @Test(description = "Test interoperability with ballerina json return")
    public void testInteropWithJsonReturns() {
        Object val = BRunUtil.invoke(result, "testJsonReturns");
        BArray returns = (BArray) val;
        Assert.assertEquals(returns.size(), 5);

        Assert.assertTrue(returns.get(0) instanceof BMap);
        Assert.assertEquals(returns.get(0).toString(), "{\"name\":\"John\"}");

        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertEquals(returns.get(1), 4L);

        Assert.assertTrue(returns.get(2) instanceof BMap);
        Assert.assertEquals(returns.get(2).toString(), "{\"name\":\"Doe\"}");

        Assert.assertTrue(returns.get(3) instanceof BArray);
        Assert.assertEquals(returns.get(3).toString(), "[\"John\"]");

        Assert.assertNull(returns.get(4));
    }

    @Test(description = "Test interoperability with ballerina json params")
    public void testInteropWithJsonParams() {
        Object returns = BRunUtil.invoke(result, "testJsonParams");
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 7L);
    }

    @Test
    public void testGetXML() {
        Object returns = BRunUtil.invoke(result, "getXML");
        Assert.assertTrue(returns instanceof BXml);
        Assert.assertEquals(returns.toString(), "<hello></hello>");
    }

    @Test
    public void testPassXML() {
        Object returns = BRunUtil.invoke(result, "testPassingXML");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "<foo></foo>");
    }

    @Test
    public void testGetAllInts() {
        Object returns = BRunUtil.invoke(result, "getAllInts");
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 2L);
    }

    @Test
    public void testAcceptAllInts() {
        Object val = BRunUtil.invoke(result, "testAcceptAllInts");
        BArray returns = (BArray) val;
        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertEquals(returns.get(0), 4L);
        Assert.assertTrue(returns.get(1) instanceof Double);
        Assert.assertEquals(returns.get(1), 4.0);
        Assert.assertTrue(returns.get(2) instanceof Long);
        Assert.assertEquals(returns.get(2), 4L);
    }

    @Test
    public void testGetMixType() {
        Object returns = BRunUtil.invoke(result, "getMixType");
        Assert.assertTrue(returns instanceof Integer);
        Assert.assertEquals(returns, 5);
    }

    @Test
    public void testGetIntegersAsMixType() {
        Object returns = BRunUtil.invoke(result, "getIntegersAsMixType");
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 2L);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeCastError \\{\"message\":\"incompatible " +
                    "types: 'int' cannot be cast to 'MIX_TYPE'.*")
    public void testGetInvalidIntegerAsMixType() {
        Object returns = BRunUtil.invoke(result, "getInvalidIntegerAsMixType");
        Assert.assertTrue(returns instanceof ObjectType);
        Assert.assertEquals(returns, 3);
    }

    @Test
    public void testInteropWithJavaStringReturn() {
        Exception expectedException = null;
        try {
            BRunUtil.invoke(result, "interopWithJavaStringReturn");
        } catch (Exception e) {
            expectedException = e;
        }
        Assert.assertNotNull(expectedException);
        String message = expectedException.getMessage();
        Assert.assertEquals(message, "error: 'class java.lang.String' cannot be assigned to type 'anydata'\n" +
                "\tat ballerina_types_as_interop_types:" +
                "acceptNothingInvalidAnydataReturn(ballerina_types_as_interop_types.bal:203)\n" +
                "\t   ballerina_types_as_interop_types:" +
                "interopWithJavaStringReturn(ballerina_types_as_interop_types.bal:174)");
    }

    @Test
    public void testAcceptMixType() {
        Object val = BRunUtil.invoke(result, "testAcceptMixTypes");
        BArray returns = (BArray) val;
        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertEquals(returns.get(0), 2L);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(1).toString(), "hello");
        Assert.assertTrue(returns.get(2) instanceof Boolean);
        Assert.assertFalse((Boolean) returns.get(2));
    }

    @Test
    public void testUseFunctionPointer() {
        Object returns = BRunUtil.invoke(result, "testUseFunctionPointer");
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 7L);
    }

    @Test
    public void testGetFunctionPointer() {
        Object returns = BRunUtil.invoke(result, "testGetFunctionPointer");
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 10L);
    }

    @Test
    public void testUseTypeDesc() {
        Object returns = BRunUtil.invoke(result, "testUseTypeDesc");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "typedesc json");
    }

    @Test
    public void testGetTypeDesc() {
        Object returns = BRunUtil.invoke(result, "testGetTypeDesc");
        Assert.assertTrue(returns instanceof BTypedesc);
        Assert.assertEquals(returns.toString(), "typedesc xml<(lang.xml:Element|lang.xml:Comment|lang" +
                ".xml:ProcessingInstruction|lang.xml:Text)>");
    }

    @Test
    public void testUseFuture() {
        Object returns = BRunUtil.invoke(result, "testUseFuture");
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 4L);
    }

    @Test
    public void testGetFuture() {
        Object returns = BRunUtil.invoke(result, "testGetFuture");
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 4L);
    }

    @Test
    public void testGetHandle() {
        Object returns = BRunUtil.invoke(result, "testGetHandle");
        Assert.assertTrue(returns instanceof HandleValue);
        HandleValue handle = (HandleValue) returns;
        Assert.assertTrue(handle.getValue() instanceof Map);
        Map map = (Map) handle.getValue();
        Assert.assertEquals(map.size(), 1);
        Assert.assertEquals(map.get("name"), "John");
    }

    @Test
    public void testUseHandle() {
        Object returns = BRunUtil.invoke(result, "testUseHandle");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "John");
    }

    @Test
    public void testUseHandleInUnion() {
        Object returns = BRunUtil.invoke(result, "testUseHandleInUnion");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "John");
    }

    @Test
    public void testThrowJavaException() {
        Object returns = BRunUtil.invokeAndGetJVMResult(result, "testThrowJavaException2");
        Assert.assertTrue(returns instanceof ErrorValue);
        ErrorValue error = (ErrorValue) returns;
        Assert.assertEquals(error.getPrintableStackTrace(), "java.util.EmptyStackException\n" +
                "\tat ballerina_types_as_interop_types:javaStackPop(ballerina_types_as_interop_types.bal:450)\n" +
                "\t   ballerina_types_as_interop_types:testThrowJavaException2(ballerina_types_as_interop_types.bal:" +
                "439)");
    }

    @Test
    public void testDifferentRefTypesForIntersectionEffectiveType() {
        BRunUtil.invoke(result, "testDifferentRefTypesForIntersectionEffectiveType");
    }

    @Test
    public void testUsingIntersectionEffectiveType() {
        BRunUtil.invoke(result, "testUsingIntersectionEffectiveType");
    }

    @Test
    public void testReadOnlyAsParamAndReturnTypes() {
        BRunUtil.invoke(result, "testReadOnlyAsParamAndReturnTypes");
    }

    @Test
    public void testNarrowerTypesAsReadOnlyReturnTypes() {
        BRunUtil.invoke(result, "testNarrowerTypesAsReadOnlyReturnTypes");
    }

    // static methods

    public static XmlValue getXML() {
        return new XmlItem(new QName("hello"));
    }

    public static io.ballerina.runtime.api.values.BString getStringFromXML(XmlValue x) {
        return StringUtils.fromString(x.toString());
    }

    public static int getAllInts() {
        return 2;
    }

    public static int getInvalidMixType() {
        return 456;
    }

    public static int acceptAllInts(int x) {
        return x;
    }

    public static float getAllFloats() {
        return (float) 3.54;
    }

    public static float acceptAllFloats(float x) {
        return x;
    }

    public static Object getAny() {
        return 5;
    }

    public static Object acceptAny(Object x) {
        return x;
    }

    public static int useFunctionPointer(FPValue fp) {
        return ((Long) fp.call(new Object[]{Scheduler.getStrand(), 3, true, 4, true})).intValue();
    }

    public static FPValue getFunctionPointer(Object fp) {
        return (FPValue) fp;
    }

    public static io.ballerina.runtime.api.values.BString useTypeDesc(TypedescValue type) {
        return StringUtils.fromString(type.stringValue(null));
    }

    public static TypedescValue getTypeDesc() {
        return new TypedescValueImpl(PredefinedTypes.TYPE_XML);
    }

    public static Object useFuture(FutureValue future) {
        return future.getResult();
    }

    public static FutureValue getFuture(Object a) {
        return (FutureValue) a;
    }

    public static HandleValue getHandle() {
        Map<String, String> m = new HashMap<>();
        m.put("name", "John");
        return new HandleValue(m);
    }

    public static io.ballerina.runtime.api.values.BString useHandle(HandleValue h) {
        Map<String, String> m = (Map<String, String>) h.getValue();
        return StringUtils.fromString(m.get("name"));
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
