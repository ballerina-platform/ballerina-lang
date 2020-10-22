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
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.scheduling.Scheduler;
import io.ballerina.runtime.util.exceptions.BLangRuntimeException;
import io.ballerina.runtime.values.ErrorValue;
import io.ballerina.runtime.values.FPValue;
import io.ballerina.runtime.values.FutureValue;
import io.ballerina.runtime.values.HandleValue;
import io.ballerina.runtime.values.TypedescValue;
import io.ballerina.runtime.values.TypedescValueImpl;
import io.ballerina.runtime.values.XMLItem;
import io.ballerina.runtime.values.XMLValue;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BHandleValue;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BTypeDescValue;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.model.values.BValueType;
import org.ballerinalang.core.model.values.BXML;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
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
@Test(groups = { "brokenOnOldParser" })
public class RefTypeTests {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/javainterop/ballerina_types_as_interop_types.bal");
    }

    @Test(description = "Test interoperability with ballerina array value as return and map as a param")
    public void testInteropWithBalArrayAndMap() {
        BValue[] returns = BRunUtil.invoke(result, "interopWithArrayAndMap");
        Assert.assertEquals(returns.length, 1);

        Assert.assertTrue(returns[0] instanceof BValueArray);
        BValueArray arr = (BValueArray) returns[0];
        Assert.assertEquals(arr.stringValue(), "[1, 8]");
    }

    @Test(description = "Test interoperability with ballerina service type value as a param")
    public void testInteropWithServiceTypesAndStringReturn() {
        BValue[] returns = BRunUtil.invoke(result, "acceptServiceAndBooleanReturn");
        Assert.assertEquals(returns.length, 1);

        Assert.assertTrue(returns[0] instanceof BBoolean);
        BBoolean bool = (BBoolean) returns[0];
        Assert.assertTrue(bool.booleanValue());
    }

    @Test(description = "Test interoperability with ballerina ref types as params and map return")
    public void testInteropWithRefTypesAndMapReturn() {
        BValue[] returns = BRunUtil.invoke(result, "interopWithRefTypesAndMapReturn");
        Assert.assertEquals(returns.length, 1);

        Assert.assertTrue(returns[0] instanceof BMap);
        BMap bMap = (BMap) returns[0];
        Assert.assertEquals(bMap.stringValue(), "{\"a\":{}, \"b\":[5, \"hello\", {}]," +
                " \"c\":{name:\"sameera\"}, \"e\":{}, \"f\":83, \"g\":{name:\"sample\"}}");
    }

    @Test(description = "Test interoperability with ballerina error return")
    public void testInteropWithErrorReturn() {
        BValue[] returns = BRunUtil.invoke(result, "interopWithErrorReturn");
        Assert.assertEquals(returns.length, 1);

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "example error with given reason");
    }

    @Test(description = "Test interoperability with ballerina union return")
    public void testInteropWithUnionReturn() {
        BValue[] returns = BRunUtil.invoke(result, "interopWithUnionReturn");
        Assert.assertEquals(returns.length, 1);

        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test interoperability with ballerina object return")
    public void testInteropWithObjectReturn() {
        BValue[] returns = BRunUtil.invoke(result, "interopWithObjectReturn");
        Assert.assertEquals(returns.length, 1);

        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test interoperability with ballerina record return")
    public void testInteropWithRecordReturn() {
        BValue[] returns = BRunUtil.invoke(result, "interopWithRecordReturn");
        Assert.assertEquals(returns.length, 1);

        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test interoperability with ballerina any return")
    public void testInteropWithAnyReturn() {
        BValue[] returns = BRunUtil.invoke(result, "interopWithAnyReturn");
        Assert.assertEquals(returns.length, 1);

        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test interoperability with ballerina anydata return")
    public void testInteropWithAnydataReturn() {
        BValue[] returns = BRunUtil.invoke(result, "interopWithAnydataReturn");
        Assert.assertEquals(returns.length, 1);

        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test interoperability with ballerina json return")
    public void testInteropWithJsonReturns() {
        BValue[] returns = BRunUtil.invoke(result, "testJsonReturns");
        Assert.assertEquals(returns.length, 5);

        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].toString(), "{\"name\":\"John\"}");

        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 4);

        Assert.assertTrue(returns[2] instanceof BMap);
        Assert.assertEquals(returns[2].toString(), "{\"name\":\"Doe\"}");

        Assert.assertTrue(returns[3] instanceof BValueArray);
        Assert.assertEquals(returns[3].toString(), "[\"John\"]");

        Assert.assertNull(returns[4]);
    }

    @Test(description = "Test interoperability with ballerina json params")
    public void testInteropWithJsonParams() {
        BValue[] returns = BRunUtil.invoke(result, "testJsonParams");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 7);
    }

    @Test
    public void testGetXML() {
        BValue[] returns = BRunUtil.invoke(result, "getXML");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<hello></hello>");
    }

    @Test
    public void testPassXML() {
        BValue[] returns = BRunUtil.invoke(result, "testPassingXML");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "<foo></foo>");
    }

    @Test
    public void testGetAllInts() {
        BValue[] returns = BRunUtil.invoke(result, "getAllInts");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
    }

    @Test
    public void testAcceptAllInts() {
        BValue[] returns = BRunUtil.invoke(result, "testAcceptAllInts");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 4);
        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 4.0);
        Assert.assertTrue(returns[2] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 4);
    }

    @Test
    public void testGetMixType() {
        BValue[] returns = BRunUtil.invoke(result, "getMixType");
        Assert.assertTrue(returns[0] instanceof BValueType);
        Assert.assertEquals(((BValueType) returns[0]).intValue(), 5);
    }

    @Test
    public void testGetIntegersAsMixType() {
        BValue[] returns = BRunUtil.invoke(result, "getIntegersAsMixType");
        Assert.assertTrue(returns[0] instanceof BValueType);
        Assert.assertEquals(((BValueType) returns[0]).intValue(), 2);
    }

    @Test(expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}TypeCastError \\{\"message\":\"incompatible " +
                    "types: 'int' cannot be cast to 'MIX_TYPE'.*", enabled = false)
    public void testGetInvalidIntegerAsMixType() {
        BValue[] returns = BRunUtil.invoke(result, "getInvalidIntegerAsMixType");
        Assert.assertTrue(returns[0] instanceof BValueType);
        Assert.assertEquals(((BValueType) returns[0]).intValue(), 3);
    }

    @Test
    public void testAcceptMixType() {
        BValue[] returns = BRunUtil.invoke(result, "testAcceptMixTypes");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "hello");
        Assert.assertTrue(returns[2] instanceof BBoolean);
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
    }

    @Test
    public void testUseFunctionPointer() {
        BValue[] returns = BRunUtil.invoke(result, "testUseFunctionPointer");
        Assert.assertTrue(returns[0] instanceof BValueType);
        Assert.assertEquals(((BValueType) returns[0]).intValue(), 7);
    }

    @Test
    public void testGetFunctionPointer() {
        BValue[] returns = BRunUtil.invoke(result, "testGetFunctionPointer");
        Assert.assertTrue(returns[0] instanceof BValueType);
        Assert.assertEquals(((BValueType) returns[0]).intValue(), 10);
    }

    @Test
    public void testUseTypeDesc() {
        BValue[] returns = BRunUtil.invoke(result, "testUseTypeDesc");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "typedesc json");
    }

    @Test
    public void testGetTypeDesc() {
        BValue[] returns = BRunUtil.invoke(result, "testGetTypeDesc");
        Assert.assertTrue(returns[0] instanceof BTypeDescValue);
        Assert.assertEquals(returns[0].stringValue(), "xml");
    }

    @Test
    public void testUseFuture() {
        BValue[] returns = BRunUtil.invoke(result, "testUseFuture");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 4);
    }

    @Test
    public void testGetFuture() {
        BValue[] returns = BRunUtil.invoke(result, "testGetFuture");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 4);
    }

    @Test
    public void testGetHandle() {
        BValue[] returns = BRunUtil.invoke(result, "testGetHandle");
        Assert.assertTrue(returns[0] instanceof BHandleValue);
        BHandleValue handle = (BHandleValue) returns[0];
        Assert.assertTrue(handle.getValue() instanceof Map);
        Map map = (Map) handle.getValue();
        Assert.assertEquals(map.size(), 1);
        Assert.assertEquals(map.get("name"), "John");
    }

    @Test
    public void testUseHandle() {
        BValue[] returns = BRunUtil.invoke(result, "testUseHandle");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(((BString) returns[0]).stringValue(), "John");
    }

    @Test
    public void testUseHandleInUnion() {
        BValue[] returns = BRunUtil.invoke(result, "testUseHandleInUnion");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(((BString) returns[0]).stringValue(), "John");
    }

    @Test
    public void testThrowJavaException() {
        Object returns = BRunUtil.invokeAndGetJVMResult(result, "testThrowJavaException2");
        Assert.assertTrue(returns instanceof ErrorValue);
        ErrorValue error = (ErrorValue) returns;
        Assert.assertEquals(error.getPrintableStackTrace(), "java.util.EmptyStackException\n" +
                "\tat ballerina_types_as_interop_types:javaStackPop(ballerina_types_as_interop_types.bal:400)\n" +
                "\t   ballerina_types_as_interop_types:testThrowJavaException2(ballerina_types_as_interop_types.bal:" +
                "392)");
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

    public static XMLValue getXML() {
        return new XMLItem(new QName("hello"));
    }

    public static io.ballerina.runtime.api.values.BString getStringFromXML(XMLValue x) {
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
}
