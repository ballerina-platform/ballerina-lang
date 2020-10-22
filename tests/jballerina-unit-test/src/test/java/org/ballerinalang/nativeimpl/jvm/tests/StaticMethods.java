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
package org.ballerinalang.nativeimpl.jvm.tests;

import io.ballerina.runtime.TypeChecker;
import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.api.values.BXML;
import io.ballerina.runtime.types.BArrayType;
import io.ballerina.runtime.types.BTupleType;
import io.ballerina.runtime.types.BUnionType;
import io.ballerina.runtime.util.exceptions.BallerinaException;
import io.ballerina.runtime.values.ArrayValue;
import io.ballerina.runtime.values.ArrayValueImpl;
import io.ballerina.runtime.values.BmpStringValue;
import io.ballerina.runtime.values.DecimalValue;
import io.ballerina.runtime.values.ErrorValue;
import io.ballerina.runtime.values.FPValue;
import io.ballerina.runtime.values.HandleValue;
import io.ballerina.runtime.values.ListInitialValueEntry;
import io.ballerina.runtime.values.MapValue;
import io.ballerina.runtime.values.MapValueImpl;
import io.ballerina.runtime.values.ObjectValue;
import io.ballerina.runtime.values.StringValue;
import io.ballerina.runtime.values.TableValue;
import io.ballerina.runtime.values.TupleValueImpl;
import io.ballerina.runtime.values.TypedescValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class contains a set of utility static methods required for interoperability testing.
 *
 * @since 1.0.0
 */
public class StaticMethods {

    private static final BArrayType intArrayType = new BArrayType(PredefinedTypes.TYPE_INT);
    private static final BArrayType jsonArrayType = new BArrayType(PredefinedTypes.TYPE_JSON);
    private static final BTupleType tupleType = new BTupleType(
            Arrays.asList(PredefinedTypes.TYPE_INT, PredefinedTypes.TYPE_FLOAT, PredefinedTypes.TYPE_STRING,
                          PredefinedTypes.TYPE_INT, PredefinedTypes.TYPE_STRING));

    private StaticMethods() {
    }

    public static void acceptNothingAndReturnNothing() {
    }

    public static Date acceptNothingButReturnDate() {
        return new Date();
    }

    public static StringValue acceptNothingButReturnString() {
        return new BmpStringValue("hello world");
    }

    public static BString stringParamAndReturn(BString a1) {
        return a1.concat(new BmpStringValue(" and Hadrian"));
    }

    public static Date acceptSomethingAndReturnSomething(Date date) {
        return date;
    }

    public static String acceptTwoParamsAndReturnSomething(String s1, String s2) {
        return s1 + s2;
    }

    public static Integer acceptThreeParamsAndReturnSomething(Integer s1, Integer s2, Integer s3) {
        return s1 + s2 + s3;
    }

    // This scenario is for map value to be passed to interop and return array value.
    public static ArrayValue getArrayValueFromMap(BString key, MapValue mapValue) {
        ArrayValue arrayValue = (ArrayValue) ValueCreator.createArrayValue(intArrayType);
        arrayValue.add(0, 1);
        long fromMap = (long) mapValue.get(key);
        arrayValue.add(1, fromMap);
        return arrayValue;
    }

    public static BMap<BString, Object> acceptRefTypesAndReturnMap(ObjectValue a, ArrayValue b, Object c,
                                                                   ErrorValue d, Object e, Object f, MapValue g) {
        BMap<BString, Object> mapValue = ValueCreator.createMapValue();
        mapValue.put(StringUtils.fromString("a"), a);
        mapValue.put(StringUtils.fromString("b"), b);
        mapValue.put(StringUtils.fromString("c"), c);
//        mapValue.put("d", d);
        mapValue.put(StringUtils.fromString("e"), e);
        mapValue.put(StringUtils.fromString("f"), f);
        mapValue.put(StringUtils.fromString("g"), g);
        return mapValue;
    }

    public static boolean acceptServiceObjectAndReturnBoolean(ObjectValue serviceObject) {
        return TypeTags.SERVICE_TAG == serviceObject.getType().getTag();
    }

    public static BError acceptStringErrorReturn(BString msg) {
        return ErrorCreator.createError(msg, new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
    }

    public static Object acceptIntUnionReturn(int flag) {
        switch (flag) {
            case 1:
                return 25;
            case 2:
                return "sample value return";
            case 3:
                return 54.88;
            default:
                return true;
        }
    }

    public static ObjectValue acceptObjectAndObjectReturn(ObjectValue p, int newVal) {
        p.set(StringUtils.fromString("age"), newVal);
        return p;
    }

    public static int acceptObjectAndReturnField(ObjectValue p) {
        return ((Long) p.get(StringUtils.fromString("age"))).intValue();
    }

    public static MapValue acceptRecordAndRecordReturn(MapValue e, BString newVal) {
        e.put(StringUtils.fromString("name"), newVal);
        return e;
    }

    public static String getString(UUID uuid) {
        return uuid.toString() + ": Sameera";
    }

    public static void acceptNothingReturnNothingAndThrowsCheckedException() throws JavaInteropTestCheckedException {

    }

    public static void acceptNothingReturnNothingAndThrowsMultipleCheckedException()
            throws JavaInteropTestCheckedException, IOException, ClassNotFoundException {

    }

    public static void acceptNothingReturnNothingAndThrowsUncheckedException() throws UnsupportedOperationException {

    }

    public static void acceptNothingReturnNothingAndThrowsCheckedAndUncheckedException()
            throws JavaInteropTestCheckedException, UnsupportedOperationException, ClassNotFoundException {

    }


    public static Date acceptNothingReturnSomethingAndThrowsCheckedException() throws JavaInteropTestCheckedException {
        return new Date();
    }

    public static Date acceptNothingReturnSomethingAndThrowsMultipleCheckedException()
            throws JavaInteropTestCheckedException, IOException, ClassNotFoundException {
        return new Date();
    }

    public static Date acceptNothingReturnSomethingAndThrowsUncheckedException() throws UnsupportedOperationException {
        return new Date();
    }

    public static Date acceptNothingReturnSomethingAndThrowsCheckedAndUncheckedException()
            throws JavaInteropTestCheckedException, UnsupportedOperationException, ClassNotFoundException {
        return new Date();
    }

    public static Date acceptSomethingReturnSomethingAndThrowsCheckedAndUncheckedException(Date date)
            throws JavaInteropTestCheckedException, UnsupportedOperationException, ClassNotFoundException {
        return date;
    }

    public static Date acceptSomethingReturnSomethingAndThrowsCheckedException(Date date)
            throws JavaInteropTestCheckedException {
        return date;
    }

    public static Date acceptSomethingReturnSomethingAndThrowsMultipleCheckedException(Date date)
            throws JavaInteropTestCheckedException, IOException, ClassNotFoundException {
        return date;
    }

    public static Date acceptSomethingReturnSomethingAndThrowsUncheckedException(Date date)
            throws UnsupportedOperationException {
        return date;
    }

    public static int acceptIntReturnIntThrowsCheckedException(long a) throws JavaInteropTestCheckedException {
        return (int) (a + 5);
    }

    public static ArrayValue getArrayValueFromMapWhichThrowsCheckedException(BString key, MapValue mapValue)
            throws JavaInteropTestCheckedException {
        ArrayValue arrayValue = (ArrayValue) ValueCreator.createArrayValue(intArrayType);
        arrayValue.add(0, 1);
        long fromMap = mapValue.getIntValue(key);
        arrayValue.add(1, fromMap);
        return arrayValue;
    }

    public static BMap<BString, Object> acceptRefTypesAndReturnMapWhichThrowsCheckedException(ObjectValue a,
                                                                                              ArrayValue b, Object c,
                                                                                              ErrorValue d, Object e,
                                                                                              Object f,
                                                                                              MapValue g)
            throws JavaInteropTestCheckedException {
        BMap<BString, Object> mapValue = ValueCreator.createMapValue();
        mapValue.put(StringUtils.fromString("a"), a);
        mapValue.put(StringUtils.fromString("b"), b);
        mapValue.put(StringUtils.fromString("c"), c);
        mapValue.put(StringUtils.fromString("e"), e);
        mapValue.put(StringUtils.fromString("f"), f);
        mapValue.put(StringUtils.fromString("g"), g);
        return mapValue;
    }

    public static BError acceptStringErrorReturnWhichThrowsCheckedException(BString msg)
            throws JavaInteropTestCheckedException {
        return ErrorCreator.createError(msg, new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
    }

    public static Object acceptIntUnionReturnWhichThrowsCheckedException(int flag)
            throws JavaInteropTestCheckedException {
        switch (flag) {
            case 1:
                return 25;
            case 2:
                return "sample value return";
            case 3:
                return 54.88;
            default:
                return true;
        }
    }

    public static ObjectValue acceptObjectAndObjectReturnWhichThrowsCheckedException(ObjectValue p, int newVal)
            throws JavaInteropTestCheckedException {
        p.set(StringUtils.fromString("age"), newVal);
        return p;
    }

    public static MapValue acceptRecordAndRecordReturnWhichThrowsCheckedException(
            MapValue<BString, Object> e, BString newVal) throws JavaInteropTestCheckedException {
        e.put(StringUtils.fromString("name"), newVal);
        return e;
    }

    public static BMap getMapOrError(BString swaggerFilePath, MapValue apiDef)
            throws JavaInteropTestCheckedException {
        BString finalBasePath = StringUtils.fromString("basePath");
        AtomicLong runCount = new AtomicLong(0L);
        ArrayValue arrayValue = new ArrayValueImpl(new BArrayType(ValueCreator.createRecordValue(new Module(
                "", "."), "ResourceDefinition").getType()));
        BMap apiDefinitions = ValueCreator.createRecordValue(new Module("",
                                                                        "."), "ApiDefinition");
        BMap resource = ValueCreator.createRecordValue(new Module("",
                                                                  "."), "ResourceDefinition");
        resource.put(StringUtils.fromString("path"), finalBasePath);
        resource.put(StringUtils.fromString("method"), StringUtils.fromString("Method string"));
        arrayValue.add(runCount.getAndIncrement(), resource);
        apiDefinitions.put(StringUtils.fromString("resources"), arrayValue);
        return apiDefinitions;
    }

    public static TupleValueImpl getArrayValue() throws BallerinaException {
        String name = null;
        String type = null;
        try {
            return new TupleValueImpl(new String[]{name, type}, new BTupleType(new ArrayList<Type>() {
                {
                    add(PredefinedTypes.TYPE_STRING);
                    add(PredefinedTypes.TYPE_STRING);
                }
            }));
        } catch (BallerinaException e) {
            throw new BallerinaException("Error occurred while creating ArrayValue.", e);
        }
    }

    public static long funcWithAsyncDefaultParamExpression(long a, long b) {
        return a + (b * 2);
    }

    public static long usingParamValues(long a, long b) {
        return a + (b * 3);
    }

    public static BDecimal decimalParamAndReturn(BDecimal a) {
        return new DecimalValue(new BigDecimal("99.7")).add(a);
    }

    public static Object decimalParamAndReturnAsObject(BDecimal a) {
        return new DecimalValue(new BigDecimal("99.6")).add(a);
    }

    public static BDecimal decimalParamAndWithBigDecimal(BigDecimal a) {
        return new DecimalValue(new BigDecimal("99.6")).add(new DecimalValue(a));
    }

    public static BDecimal decimalParamAsObjectAndReturn(Object a) {
        return new DecimalValue(new BigDecimal("99.4").add((BigDecimal) a));
    }

    public static String returnStringForBUnionFromJava() {
        return "99999";
    }
    /////////////


    public static TupleValueImpl mockedNativeFuncWithOptionalParams(long a, double b, String c,
                                                                    long d, String e) {
        TupleValueImpl tuple = (TupleValueImpl) ValueCreator.createTupleValue(tupleType);
        tuple.add(0, Long.valueOf(a));
        tuple.add(1, Double.valueOf(b));
        tuple.add(2, (Object) c);
        tuple.add(3, Long.valueOf(d));
        tuple.add(4, (Object) e);
        return tuple;
    }

    public static UUID getUUId() {
        UUID uuid = UUID.randomUUID();
        return uuid;
    }

    public static Object getJson() {
        MapValueImpl<BString, Object> map = new MapValueImpl<>(PredefinedTypes.TYPE_JSON);
        map.put(StringUtils.fromString("name"), StringUtils.fromString("John"));
        return map;
    }

    public static MapValueImpl<BString, Object> getJsonObject() {
        MapValueImpl<BString, Object> map = new MapValueImpl<>(PredefinedTypes.TYPE_JSON);
        map.put(StringUtils.fromString("name"), StringUtils.fromString("Doe"));
        return map;
    }

    public static ArrayValue getJsonArray() {
        ArrayValue array = (ArrayValue) ValueCreator.createArrayValue(jsonArrayType);
        array.add(0, (Object) "John");
        return array;
    }

    public static Object getNullJson() {
        return null;
    }

    public static int getInt() {
        return 4;
    }

    public static int getIntFromJson(Object json) {
        return ((Number) json).intValue();
    }

    public static int getIntFromJsonInt(int json) {
        return json;
    }

    public static BFuture getFuture(BTypedesc typeDesc, BFuture future) {
        return future;
    }

    public static BTypedesc getTypeDesc(BTypedesc typeDesc, BFuture future) {
        return typeDesc;
    }

    public static BFuture getFutureOnly(BFuture future) {
        return future;
    }

    public static BTypedesc getTypeDescOnly(BTypedesc typeDesc) {
        return typeDesc;
    }

    public static ArrayValue getValues(MapValue<BString, Long> intMap, MapValue<BString, BString> stringMap) {
        int length = intMap.size() + stringMap.size();
        ListInitialValueEntry[] entries = new ListInitialValueEntry[length];

        int index = 0;

        for (Map.Entry<BString, Long> intEntry : intMap.entrySet()) {
            entries[index++] = new ListInitialValueEntry.ExpressionEntry(intEntry.getValue());
        }

        for (Map.Entry<BString, BString> stringEntry : stringMap.entrySet()) {
            entries[index++] = new ListInitialValueEntry.ExpressionEntry(stringEntry.getValue());
        }

        return new ArrayValueImpl(new BArrayType(new BUnionType(new ArrayList(2) {{
            add(PredefinedTypes.TYPE_INT);
            add(PredefinedTypes.TYPE_STRING);
        }}), length, true), length, entries);
    }

    public static Object echoAnydataAsAny(Object value) {
        return value;
    }

    public static ObjectValue echoObject(ObjectValue obj) {
        return obj;
    }

    public static boolean echoImmutableRecordField(MapValue value, BString key) {
        return value.getBooleanValue(key);
    }

    public static void addTwoNumbersSlowAsyncVoidSig(Environment env, long a, long b) {
        Future balFuture = env.markAsync();
        new Thread(() -> {
            sleep();
            balFuture.complete(a + b);
        }).start();
    }

    public static void addTwoNumbersFastAsyncVoidSig(Environment env, long a, long b) {
        Future balFuture = env.markAsync();
        balFuture.complete(a + b);
    }


    public static long addTwoNumbersSlowAsync(Environment env, long a, long b) {
        Future balFuture = env.markAsync();
        new Thread(() -> {
            sleep();
            balFuture.complete(a + b);
        }).start();

        return -38263;
    }

    public static long addTwoNumbersFastAsync(Environment env, long a, long b) {
        Future balFuture = env.markAsync();
        balFuture.complete(a + b);

        return -282619;
    }

    public static void addTwoNumbersBuggy(Environment env, long a, long b) {
        // Buggy because env.markAsync() is not called
        // TODO: see if we can verify this
    }

    private static void sleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            assert false;
        }
    }

    public static Object acceptAndReturnReadOnly(Object value) {
        Type type = TypeChecker.getType(value);

        switch (type.getTag()) {
            case TypeTags.INT_TAG:
                return 100L;
            case TypeTags.ARRAY_TAG:
            case TypeTags.OBJECT_TYPE_TAG:
                return value;
            case TypeTags.RECORD_TYPE_TAG:
            case TypeTags.MAP_TAG:
                return ((MapValue) value).get(StringUtils.fromString("first"));
        }
        return StringUtils.fromString("other");
    }

    public static void getNilAsReadOnly() {
    }

    public static boolean getBooleanAsReadOnly() {
        return true;
    }

    public static Long getIntAsReadOnly() {
        return 100L;
    }

    public static double getFloatAsReadOnly(double f) {
        return f;
    }

    public static BDecimal getDecimalAsReadOnly(BDecimal d) {
        return d;
    }

    public static BString getStringAsReadOnly(BString s1, BString s2) {
        return s1.concat(s2);
    }

    public static BError getErrorAsReadOnly(BError e) {
        return e;
    }

    public static FPValue getFunctionPointerAsReadOnly(FPValue func) {
        return func;
    }

    public static ObjectValue getObjectOrServiceAsReadOnly(ObjectValue ob) {
        return ob;
    }

    public static TypedescValue getTypedescAsReadOnly(TypedescValue t) {
        return t;
    }

    public static HandleValue getHandleAsReadOnly(HandleValue h) {
        return h;
    }

    public static BXML getXmlAsReadOnly(BXML x) {
        return x;
    }

    public static ArrayValue getListAsReadOnly(ArrayValue list) {
        return list;
    }

    public static MapValue getMappingAsReadOnly(MapValue mp) {
        return mp;
    }

    public static TableValue getTableAsReadOnly(TableValue tb) {
        return tb;
    }
}
