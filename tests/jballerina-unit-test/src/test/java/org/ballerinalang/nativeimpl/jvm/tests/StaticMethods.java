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

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.TupleType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.api.values.BHandle;
import io.ballerina.runtime.api.values.BInitialValueEntry;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BMapInitialValueEntry;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.api.values.BXml;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class contains a set of utility static methods required for interoperability testing.
 *
 * @since 1.0.0
 */
public class StaticMethods {

    private static final ArrayType intArrayType = TypeCreator.createArrayType(PredefinedTypes.TYPE_INT);
    private static final ArrayType jsonArrayType = TypeCreator.createArrayType(PredefinedTypes.TYPE_JSON);
    private static final TupleType tupleType = TypeCreator.createTupleType(
            Arrays.asList(PredefinedTypes.TYPE_INT, PredefinedTypes.TYPE_FLOAT, PredefinedTypes.TYPE_STRING,
                          PredefinedTypes.TYPE_INT, PredefinedTypes.TYPE_STRING));

    private StaticMethods() {
    }

    public static void acceptNothingAndReturnNothing() {
    }

    public static Date acceptNothingButReturnDate() {
        return new Date();
    }

    public static BString acceptNothingButReturnString() {
        return StringUtils.fromString("hello world");
    }

    public static BString stringParamAndReturn(BString a1) {
        return a1.concat(StringUtils.fromString(" and Hadrian"));
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
    public static BArray getArrayValueFromMap(BString key, BMap mapValue) {
        BArray arrayValue = ValueCreator.createArrayValue(intArrayType);
        arrayValue.add(0, 1);
        long fromMap = (long) mapValue.get(key);
        arrayValue.add(1, fromMap);
        return arrayValue;
    }

    public static BMap<BString, Object> acceptRefTypesAndReturnMap(BObject a, BArray b, Object c,
                                                                   BError d, Object e, Object f, BMap g) {
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

    public static boolean acceptServiceObjectAndReturnBoolean(BObject serviceObject) {
        return TypeTags.SERVICE_TAG == serviceObject.getType().getTag();
    }

    public static BError acceptStringErrorReturn(BString msg) {
        return ErrorCreator.createError(msg, ValueCreator.createMapValue(PredefinedTypes.TYPE_ERROR_DETAIL));
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

    public static BObject acceptObjectAndObjectReturn(BObject p, int newVal) {
        p.set(StringUtils.fromString("age"), newVal);
        return p;
    }

    public static int acceptObjectAndReturnField(BObject p) {
        return ((Long) p.get(StringUtils.fromString("age"))).intValue();
    }

    public static BMap acceptRecordAndRecordReturn(BMap e, BString newVal) {
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

    public static BArray getArrayValueFromMapWhichThrowsCheckedException(BString key, BMap mapValue)
            throws JavaInteropTestCheckedException {
        BArray arrayValue = ValueCreator.createArrayValue(intArrayType);
        arrayValue.add(0, 1);
        long fromMap = mapValue.getIntValue(key);
        arrayValue.add(1, fromMap);
        return arrayValue;
    }

    public static BMap<BString, Object> acceptRefTypesAndReturnMapWhichThrowsCheckedException(BObject a,
                                                                                              BArray b, Object c,
                                                                                              BError d, Object e,
                                                                                              Object f,
                                                                                              BMap g)
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
        return ErrorCreator.createError(msg, ValueCreator.createMapValue(PredefinedTypes.TYPE_ERROR_DETAIL));
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

    public static BObject acceptObjectAndObjectReturnWhichThrowsCheckedException(BObject p, int newVal)
            throws JavaInteropTestCheckedException {
        p.set(StringUtils.fromString("age"), newVal);
        return p;
    }

    public static BMap acceptRecordAndRecordReturnWhichThrowsCheckedException(
            BMap<BString, Object> e, BString newVal) throws JavaInteropTestCheckedException {
        e.put(StringUtils.fromString("name"), newVal);
        return e;
    }

    public static BMap getMapOrError(BString swaggerFilePath, BMap apiDef)
            throws JavaInteropTestCheckedException {
        BString finalBasePath = StringUtils.fromString("basePath");
        AtomicLong runCount = new AtomicLong(0L);
        BArray arrayValue =
                ValueCreator.createArrayValue(TypeCreator.createArrayType(ValueCreator.createRecordValue(new Module(
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

    public static Object returnObjectOrError() {
        return ErrorCreator.createError(StringUtils.fromString("some reason"),
                                        ValueCreator.createMapValue(PredefinedTypes.TYPE_ERROR_DETAIL));
    }

    public static BArray getArrayValue() throws BError {
        String name = null;
        String type = null;
        try {
            return ValueCreator
                    .createTupleValue(new String[]{name, type}, TypeCreator.createTupleType(new ArrayList<Type>() {
                        {
                            add(PredefinedTypes.TYPE_STRING);
                            add(PredefinedTypes.TYPE_STRING);
                        }
                    }));
        } catch (BError e) {
            throw ErrorCreator.createError(StringUtils.fromString("Error occurred while creating BArray."), e);
        }
    }

    public static long funcWithAsyncDefaultParamExpression(long a, long b) {
        return a + (b * 2);
    }

    public static long usingParamValues(long a, long b) {
        return a + (b * 3);
    }

    public static BDecimal decimalParamAndReturn(BDecimal a) {
        return ValueCreator.createDecimalValue(new BigDecimal("99.7")).add(a);
    }

    public static Object decimalParamAndReturnAsObject(BDecimal a) {
        return ValueCreator.createDecimalValue(new BigDecimal("99.6")).add(a);
    }

    public static BDecimal decimalParamAndWithBigDecimal(BigDecimal a) {
        return ValueCreator.createDecimalValue(new BigDecimal("99.6")).add(ValueCreator.createDecimalValue(a));
    }

    public static BDecimal decimalParamAsObjectAndReturn(Object a) {
        return ValueCreator.createDecimalValue(new BigDecimal("99.4").add((BigDecimal) a));
    }

    public static String returnStringForBUnionFromJava() {
        return "99999";
    }
    /////////////


    public static BArray mockedNativeFuncWithOptionalParams(long a, double b, String c,
                                                                    long d, String e) {
        BArray tuple = (BArray) ValueCreator.createTupleValue(tupleType);
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
        BMap<BString, Object> map = ValueCreator.createMapValue(PredefinedTypes.TYPE_JSON);
        map.put(StringUtils.fromString("name"), StringUtils.fromString("John"));
        return map;
    }

    public static BMap<BString, Object> getJsonObject() {
        BMap<BString, Object> map = ValueCreator.createMapValue(PredefinedTypes.TYPE_JSON);
        map.put(StringUtils.fromString("name"), StringUtils.fromString("Doe"));
        return map;
    }

    public static BArray getJsonArray() {
        BArray array = ValueCreator.createArrayValue(jsonArrayType);
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

    public static BArray getValues(BMap<BString, Long> intMap, BMap<BString, BString> stringMap) {
        int length = intMap.size() + stringMap.size();
        BInitialValueEntry[] entries = new BInitialValueEntry[length];

        int index = 0;

        for (Map.Entry<BString, Long> intEntry : intMap.entrySet()) {
            entries[index++] = ValueCreator.createListInitialValueEntry(intEntry.getValue());
        }

        for (Map.Entry<BString, BString> stringEntry : stringMap.entrySet()) {
            entries[index++] = ValueCreator.createListInitialValueEntry(stringEntry.getValue());
        }

        return ValueCreator.createArrayValue(TypeCreator.createArrayType(TypeCreator.createUnionType(new ArrayList(2) {{
            add(PredefinedTypes.TYPE_INT);
            add(PredefinedTypes.TYPE_STRING);
        }}), length, true), length, entries);
    }

    public static Object echoAnydataAsAny(Object value) {
        return value;
    }

    public static BObject echoObject(BObject obj) {
        return obj;
    }

    public static boolean echoImmutableRecordField(BMap value, BString key) {
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

    public static Object returnNullString(boolean nullVal) {
        return nullVal ? null : StringUtils.fromString("NotNull");
    }

    public static void addTwoNumbersBuggy(Environment env, long a, long b) {
        // Buggy because env.markAsync() is not called
        // TODO: see if we can verify this
    }

    public static BString getCurrentModule(Environment env, long b) {
        Module callerModule = env.getCurrentModule();
        return StringUtils.fromString(callerModule.getOrg() + "#" + callerModule.getName() + "#" +
                                              callerModule.getVersion() + "#" + b);
    }

    public static BString getCurrentModuleForObject(Environment env, BObject a, long b) {
        Module callerModule = env.getCurrentModule();
        return StringUtils.fromString(callerModule.getOrg() + "#" + callerModule.getName() + "#" +
                                              callerModule.getVersion() + "#" +
                                              a.get(StringUtils.fromString("age")) + "#" + b);
    }

    public static long getDefaultValueWithBEnv(Environment env, long b) {
        return b;
    }

    public static long getDefaultValueWithBEnvForObject(Environment env, BObject a, long b) {
        return b;
    }

    private static void sleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            assert false;
        }
    }

    public static Object acceptAndReturnReadOnly(Object value) {
        Type type = TypeUtils.getType(value);

        switch (type.getTag()) {
            case TypeTags.INT_TAG:
                return 100L;
            case TypeTags.ARRAY_TAG:
            case TypeTags.OBJECT_TYPE_TAG:
                return value;
            case TypeTags.RECORD_TYPE_TAG:
            case TypeTags.MAP_TAG:
                return ((BMap) value).get(StringUtils.fromString("first"));
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

    public static BFunctionPointer getFunctionPointerAsReadOnly(BFunctionPointer func) {
        return func;
    }

    public static BObject getObjectOrServiceAsReadOnly(BObject ob) {
        return ob;
    }

    public static BTypedesc getTypedescAsReadOnly(BTypedesc t) {
        return t;
    }

    public static BHandle getHandleAsReadOnly(BHandle h) {
        return h;
    }

    public static BXml getXmlAsReadOnly(BXml x) {
        return x;
    }

    public static BArray getListAsReadOnly(BArray list) {
        return list;
    }

    public static BMap getMappingAsReadOnly(BMap mp) {
        return mp;
    }

    public static BTable getTableAsReadOnly(BTable tb) {
        return tb;
    }

    public static Object getValue() {
        return StringUtils.fromString("Ballerina");
    }


    public static BMap<BString, Object> createStudentUsingType() {
        Module module = new Module("$anon", ".", "0.0.0");
        BMap<BString, Object> bmap = ValueCreator.createRecordValue(module, "(Student & readonly)");
        BMapInitialValueEntry[] mapInitialValueEntries = {ValueCreator.createKeyFieldEntry(
                StringUtils.fromString("name"), StringUtils.fromString("Riyafa")), ValueCreator.createKeyFieldEntry(
                StringUtils.fromString("birth"), "Sri Lanka")};
        return ValueCreator.createMapValue(bmap.getType(), mapInitialValueEntries);
    }

    public static BMap<BString, Object> createStudent() {
        Module module = new Module("$anon", ".", "0.0.0");
        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        mapInitialValueEntries.put("name", StringUtils.fromString("Riyafa"));
        mapInitialValueEntries.put("birth", StringUtils.fromString("Sri Lanka"));
        return ValueCreator.createReadonlyRecordValue(module, "Student", mapInitialValueEntries);
    }

    public static BMap<BString, Object> createDetails() {
        Module module = new Module("$anon", ".", "0.0.0");
        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        mapInitialValueEntries.put("name", StringUtils.fromString("Riyafa"));
        mapInitialValueEntries.put("id", 123);
        return ValueCreator.createReadonlyRecordValue(module, "Details", mapInitialValueEntries);
    }

    public static BMap<BString, Object> createRawDetails() {
        Module module = new Module("$anon", ".", "0.0.0");
        Map<String, Field> fieldMap = new HashMap<>();
        fieldMap.put("name", TypeCreator
                .createField(PredefinedTypes.TYPE_STRING, "name", SymbolFlags.REQUIRED + SymbolFlags.PUBLIC));
        fieldMap.put("id", TypeCreator
                .createField(PredefinedTypes.TYPE_INT, "id", SymbolFlags.REQUIRED + SymbolFlags.PUBLIC));
        RecordType recordType = TypeCreator.createRecordType("Details", module, SymbolFlags.READONLY
                , fieldMap, null, true, 0);
        BMapInitialValueEntry[] mapInitialValueEntries = {ValueCreator.createKeyFieldEntry(
                StringUtils.fromString("name"), StringUtils.fromString("aee")), ValueCreator.createKeyFieldEntry(
                StringUtils.fromString("id"), 123L)};
        return ValueCreator.createMapValue(recordType, mapInitialValueEntries);
    }
}
