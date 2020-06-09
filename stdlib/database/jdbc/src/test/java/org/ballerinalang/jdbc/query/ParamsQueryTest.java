/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.jdbc.query;

import org.ballerinalang.jdbc.utils.SQLDBUtils;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.io.File;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;

/**
 * This test class verifies the behaviour of the ParameterizedString passed into the testQuery operation.
 *
 * @since 1.3.0
 */
public class ParamsQueryTest {
    private CompileResult result;
    private static final String DB_NAME = "TEST_SQL_PARAMS_QUERY";
    private static final String JDBC_URL = "jdbc:h2:file:" + SQLDBUtils.DB_DIR + DB_NAME;
    private BValue[] args = {new BString(JDBC_URL), new BString(SQLDBUtils.DB_USER),
            new BString(SQLDBUtils.DB_PASSWORD)};

    @BeforeClass
    public void setup() throws SQLException {
        result = BCompileUtil.compileOffline(SQLDBUtils.getBalFilesDir("query", "simple-params-query-test.bal"));
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIR), DB_NAME);
        SQLDBUtils.initH2Database(SQLDBUtils.DB_DIR, DB_NAME,
                SQLDBUtils.getSQLResourceDir("query", "simple-params-test-data.sql"));
    }

    @Test
    public void testQuerySingleIntParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "querySingleIntParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryDoubleIntParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryDoubleIntParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryIntAndLongParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryIntAndLongParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryStringParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryStringParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryIntAndStringParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryIntAndStringParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryDoubleParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryDoubleParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryFloatParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryFloatParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryDoubleAndFloatParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryDoubleAndFloatParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryDecimalParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryDecimalParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryDecimalAnFloatParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryDecimalAnFloatParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryByteArrayParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryByteArrayParam", args);
        validateComplexTableResult(returns);
    }

    @Test
    public void testQueryTypeVarcharStringParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeVarcharStringParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryTypeCharStringParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeCharStringParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryTypeNCharStringParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeNCharStringParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryTypeNVarCharStringParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeNVarCharStringParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryTypeVarCharIntegerParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeVarCharIntegerParam", args);
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returns[0]).getMap();
        Assert.assertEquals(result.size(), 8);
        Assert.assertEquals(((BInteger) result.get("ROW_ID")).intValue(), 3);
        Assert.assertEquals(((BString) result.get("STRING_TYPE")).stringValue(), "1");
    }

    @Test
    public void testQueryTypBooleanBooleanParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypBooleanBooleanParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryTypBitIntParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypBitIntParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryTypBitStringParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypBitStringParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryTypBitInvalidIntParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypBitInvalidIntParam", args);
        Assert.assertEquals(returns[0].getType().getTag(), TypeTags.ERROR);
        Assert.assertTrue(((BError) returns[0]).getMessage().contains("Only 1 or 0 can be passed"));
    }

    @Test
    public void testQueryTypeIntIntParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeIntIntParam", args);
        validateNumericTableResult(returns);
    }

    @Test
    public void testQueryTypeTinyIntIntParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeTinyIntIntParam", args);
        validateNumericTableResult(returns);
    }

    @Test
    public void testQueryTypeSmallIntIntParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeSmallIntIntParam", args);
        validateNumericTableResult(returns);
    }

    @Test
    public void testQueryTypeBigIntIntParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeBigIntIntParam", args);
        validateNumericTableResult(returns);
    }

    @Test
    public void testQueryTypeDoubleDoubleParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeDoubleDoubleParam", args);
        validateNumericTableResult(returns);
    }

    @Test
    public void testQueryTypeDoubleIntParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeDoubleIntParam", args);
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returns[0]).getMap();
        Assert.assertEquals(result.size(), 10);
        Assert.assertEquals(result.get("ID"), new BInteger(2));
        Assert.assertEquals(((BFloat) result.get("FLOAT_TYPE")).floatValue(), 1234.0);
    }

    @Test
    public void testQueryTypeDoubleDecimalParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeDoubleDecimalParam", args);
        validateNumericTableResult(returns);
    }

    @Test
    public void testQueryTypeFloatDoubleParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeFloatDoubleParam", args);
        validateNumericTableResult(returns);
    }

    @Test
    public void testQueryTypeRealDoubleParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeRealDoubleParam", args);
        validateNumericTableResult(returns);
    }

    @Test
    public void testQueryTypeNumericDoubleParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeNumericDoubleParam", args);
        validateNumericTableResult(returns);
    }

    @Test
    public void testQueryTypeNumericIntParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeNumericIntParam", args);
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returns[0]).getMap();
        Assert.assertEquals(result.size(), 10);
        Assert.assertEquals(result.get("ID"), new BInteger(2));
        Assert.assertEquals(((BFloat) result.get("FLOAT_TYPE")).floatValue(), 1234.0);
    }

    @Test
    public void testQueryTypeNumericDecimalParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeNumericDecimalParam", args);
        validateNumericTableResult(returns);
    }

    @Test
    public void testQueryTypeDecimalDoubleParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeDecimalDoubleParam", args);
        validateNumericTableResult(returns);
    }

    @Test
    public void testQueryTypeDecimalDecimalParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeDecimalDecimalParam", args);
        validateNumericTableResult(returns);
    }

    @Test
    public void testQueryTypeBinaryByteParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeBinaryByteParam", args);
        validateComplexTableResult(returns);
    }

    @Test
    public void testQueryTypeBinaryReadableByteChannelParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeBinaryReadableByteChannelParam", args);
        validateComplexTableResult(returns);
    }

    @Test
    public void testQueryTypeVarBinaryReadableByteChannelParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeVarBinaryReadableByteChannelParam", args);
        validateComplexTableResult(returns);
    }

    @Test
    public void testQueryTypeBlobByteParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeBlobByteParam", args);
        validateComplexTableResult(returns);
    }

    @Test
    public void testQueryTypeBlobReadableByteChannelParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeBlobReadableByteChannelParam", args);
        validateComplexTableResult(returns);
    }

    @Test
    public void testQueryTypeClobStringParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeClobStringParam", args);
        validateComplexTableResult(returns);
    }

    @Test
    public void testQueryTypeClobReadableCharChannelParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeClobReadableCharChannelParam", args);
        validateComplexTableResult(returns);
    }

    @Test
    public void testQueryTypeNClobReadableCharChannelParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTypeNClobReadableCharChannelParam", args);
        validateComplexTableResult(returns);
    }

    @Test
    public void testQueryDateStringParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryDateStringParam", args);
        validateDateTimeTypesTableResult(returns);
    }

    @Test
    public void testQueryDateString2Param() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryDateString2Param", args);
        validateDateTimeTypesTableResult(returns);
    }

    @Test
    public void testQueryDateStringInvalidParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryDateStringInvalidParam", args);
        Assert.assertEquals(returns[0].getType().getTag(), TypeTags.ERROR);
        Assert.assertTrue(((BError) returns[0]).getMessage().contains("IllegalArgumentException"));
    }

    @Test
    public void testQueryDateLongParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryDateLongParam", args);
        validateDateTimeTypesTableResult(returns);
    }

    @Test
    public void testQueryDateTimeRecordParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryDateTimeRecordParam", args);
        validateDateTimeTypesTableResult(returns);
    }

    @Test
    public void testQueryDateTimeRecordWithTimeZoneParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryDateTimeRecordWithTimeZoneParam", args);
        validateDateTimeTypesTableResult(returns);
    }

    @Test
    public void testQueryTimeDateStringParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTimeStringParam", args);
        validateDateTimeTypesTableResult(returns);
    }

    @Test
    public void testQueryTimeDateStringInvalidParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTimeStringInvalidParam", args);
        Assert.assertEquals(returns[0].getType().getTag(), TypeTags.ERROR);
        Assert.assertTrue(((BError) returns[0]).getMessage().contains("IllegalArgumentException"));
    }

    @Test
    public void testQueryTimeDateLongParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTimeLongParam", args);
        validateDateTimeTypesTableResult(returns);
    }

    @Test
    public void testQueryTimeDateTimeRecordParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTimeTimeRecordParam", args);
        validateDateTimeTypesTableResult(returns);
    }

    @Test
    public void testQueryTimeDateTimeRecordWithTimeZoneParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTimeTimeRecordWithTimeZoneParam", args);
        validateDateTimeTypesTableResult(returns);
    }

    @Test
    public void testQueryTimestampDateStringParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTimestampStringParam", args);
        validateDateTimeTypesTableResult(returns);
    }

    @Test
    public void testQueryTimestampDateStringInvalidParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTimestampStringInvalidParam", args);
        Assert.assertEquals(returns[0].getType().getTag(), TypeTags.ERROR);
        Assert.assertTrue(((BError) returns[0]).getMessage().contains(" Timestamp format must be yyyy-mm-dd hh:mm:ss"));
    }

    @Test
    public void testQueryTimestampDateLongParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTimestampLongParam", args);
        validateDateTimeTypesTableResult(returns);
    }

    @Test
    public void testQueryTimestampDateTimeRecordParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTimestampTimeRecordParam", args);
        validateDateTimeTypesTableResult(returns);
    }

    @Test
    public void testQueryTimestampDateTimeRecordWithTimeZoneParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTimestampTimeRecordWithTimeZoneParam", args);
        validateDateTimeTypesTableResult(returns);
    }

    @Test
    public void testQueryDateTimeDateTimeRecordWithTimeZoneParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryDateTimeTimeRecordWithTimeZoneParam", args);
        validateDateTimeTypesTableResult(returns);
    }

    @Test
    public void testQueryTimestampDateTimeRecordWithTimeZone2Param() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryTimestampTimeRecordWithTimeZone2Param", args);
        validateDateTimeTypesTableResult(returns);
    }

    @Test
    public void testQueryArrayBasicParams() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryArrayBasicParams", args);
        validateArrayTypeTableResult(returns);
    }

    @Test
    public void testQueryArrayBasicNullParams() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryArrayBasicNullParams", args);
        Assert.assertEquals(returns.length, 1);
        SQLDBUtils.assertNotError(returns[0]);
        LinkedHashMap result = ((BMap) returns[0]).getMap();
        Assert.assertEquals(result.size(), 9);

        Assert.assertNull(result.get("INT_ARRAY"));
        Assert.assertNull(result.get("LONG_ARRAY"));
        Assert.assertNull(result.get("FLOAT_ARRAY"));
        Assert.assertNull(result.get("DOUBLE_ARRAY"));
        Assert.assertNull(result.get("DECIMAL_ARRAY"));
        Assert.assertNull(result.get("DECIMAL_ARRAY"));
        Assert.assertNull(result.get("BOOLEAN_ARRAY"));
        Assert.assertNull(result.get("STRING_ARRAY"));
        Assert.assertNull(result.get("BLOB_ARRAY"));
    }

    @Test
    public void testQueryUUIDParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryUUIDParam", args);
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returns[0]).getMap();
        Assert.assertEquals(result.size(), 2);
        Assert.assertEquals(((BInteger) result.get("id")).intValue(), 1);
        Assert.assertNotNull(result.get("data"));
    }

    @Test
    public void testQueryEnumStringParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryEnumStringParam", args);
        validateEnumTable(returns, false);
    }

    @Test
    public void testQueryEnumStringParam2() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryEnumStringParam2", args);
        validateEnumTable(returns, true);
    }

    @Test
    public void testQueryGeoParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryGeoParam", args);
        validateGeoTable(returns, false);
    }

    @Test
    public void testQueryGeoParam2() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryGeoParam2", args);
        validateGeoTable(returns, false);
    }

    @Test
    public void testQueryJsonParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryJsonParam", args);
        validateJsonTable(returns, false);
    }

    @Test
    public void testQueryJsonParam2() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryJsonParam2", args);
        validateJsonTable(returns, true);
    }

    @Test
    public void testQueryJsonParam3() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryJsonParam3", args);
        validateJsonTable(returns, true);
    }

    @Test
    public void testQueryIntervalParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryIntervalParam", args);
        validateIntervalTable(returns, false);
    }

    private void validateDataTableResult(BValue[] returns) {
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returns[0]).getMap();
        Assert.assertEquals(result.size(), 8);
        Assert.assertEquals(((BInteger) result.get("ROW_ID")).intValue(), 1);
        Assert.assertEquals(((BInteger) result.get("INT_TYPE")).intValue(), 1);
        Assert.assertEquals(((BInteger) result.get("LONG_TYPE")).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) result.get("FLOAT_TYPE")).floatValue(), 123.34);
        Assert.assertEquals(((BFloat) result.get("DOUBLE_TYPE")).floatValue(), 2139095039D);
        Assert.assertTrue(((BBoolean) result.get("BOOLEAN_TYPE")).booleanValue());
        Assert.assertEquals(((BDecimal) result.get("DECIMAL_TYPE")).decimalValue().doubleValue(), 23.45);
        Assert.assertEquals(((BString) result.get("STRING_TYPE")).stringValue(), "Hello");
    }

    private void validateComplexTableResult(BValue[] returns) {
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returns[0]).getMap();
        Assert.assertEquals(result.size(), 5);
        Assert.assertEquals(((BInteger) result.get("ROW_ID")).intValue(), 1);
        Assert.assertEquals(result.get("CLOB_TYPE").toString(), "very long text");
    }

    private void validateDateTimeTypesTableResult(BValue[] returns) {
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returns[0]).getMap();
        Assert.assertEquals(result.size(), 7);
        Assert.assertEquals(((BInteger) result.get("ROW_ID")).intValue(), 1);
        Assert.assertTrue(result.get("DATE_TYPE").toString().startsWith("2017-02-03"));
    }

    private void validateNumericTableResult(BValue[] returns) {
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returns[0]).getMap();
        Assert.assertEquals(result.size(), 10);
        Assert.assertEquals(result.get("ID"), new BInteger(1));
        Assert.assertEquals(result.get("INT_TYPE"), new BInteger(2147483647));
        Assert.assertEquals(result.get("BIGINT_TYPE"), new BInteger(9223372036854774807L));
        Assert.assertEquals(result.get("SMALLINT_TYPE"), new BInteger(32767));
        Assert.assertEquals(result.get("TINYINT_TYPE"), new BInteger(127));
        Assert.assertEquals(result.get("BIT_TYPE"), new BBoolean(true));
        Assert.assertEquals(((BDecimal) result.get("DECIMAL_TYPE")).value().doubleValue(), 1234.567);
        Assert.assertEquals(((BDecimal) result.get("NUMERIC_TYPE")).value().doubleValue(), 1234.567);
        DecimalFormat df = new DecimalFormat("###.###");
        Assert.assertEquals(df.format(((BFloat) result.get("FLOAT_TYPE")).floatValue()), "1234.567");
        Assert.assertEquals(df.format(((BFloat) result.get("REAL_TYPE")).floatValue()), "1234.567");
    }

    private void validateArrayTypeTableResult(BValue[] returns) {
        Assert.assertEquals(returns.length, 1);
        SQLDBUtils.assertNotError(returns[0]);
        LinkedHashMap result = ((BMap) returns[0]).getMap();
        Assert.assertEquals(result.size(), 9);

        BValueArray intArray = (BValueArray) result.get("INT_ARRAY");
        Assert.assertEquals(((BInteger) intArray.getBValue(0)).intValue(), 1);
        Assert.assertEquals(((BInteger) intArray.getBValue(1)).intValue(), 2);
        Assert.assertEquals(((BInteger) intArray.getBValue(2)).intValue(), 3);

        BValueArray longArray = (BValueArray) result.get("LONG_ARRAY");
        Assert.assertEquals(((BInteger) longArray.getBValue(0)).intValue(), 100000000);
        Assert.assertEquals(((BInteger) longArray.getBValue(1)).intValue(), 200000000);
        Assert.assertEquals(((BInteger) longArray.getBValue(2)).intValue(), 300000000);

        BValueArray floatArray = (BValueArray) result.get("FLOAT_ARRAY");
        Assert.assertEquals(((BDecimal) floatArray.getBValue(0)).floatValue(), 245.23);
        Assert.assertEquals(((BDecimal) floatArray.getBValue(1)).floatValue(), 5559.49);
        Assert.assertEquals(((BDecimal) floatArray.getBValue(2)).floatValue(), 8796.123);

        BValueArray doubleArray = (BValueArray) result.get("DOUBLE_ARRAY");
        Assert.assertEquals(doubleArray.getBValue(0).stringValue(), "245.23");
        Assert.assertEquals(doubleArray.getBValue(1).stringValue(), "5559.49");
        Assert.assertEquals(doubleArray.getBValue(2).stringValue(), "8796.123");

        BValueArray decimalArray = (BValueArray) result.get("DECIMAL_ARRAY");
        Assert.assertEquals(((BDecimal) decimalArray.getBValue(0)).decimalValue().doubleValue(), 245.0);
        Assert.assertEquals(((BDecimal) decimalArray.getBValue(1)).decimalValue().doubleValue(), 5559.0);
        Assert.assertEquals(((BDecimal) decimalArray.getBValue(2)).decimalValue().doubleValue(), 8796.0);

        BValueArray booleanArray = (BValueArray) result.get("BOOLEAN_ARRAY");
        Assert.assertTrue(((BBoolean) booleanArray.getBValue(0)).booleanValue());
        Assert.assertFalse(((BBoolean) booleanArray.getBValue(1)).booleanValue());
        Assert.assertTrue(((BBoolean) booleanArray.getBValue(2)).booleanValue());

        BValueArray stringArray = (BValueArray) result.get("STRING_ARRAY");
        Assert.assertEquals(stringArray.getBValue(0).stringValue().trim(), "Hello");
        Assert.assertEquals(stringArray.getBValue(1).stringValue().trim(), "Ballerina");
    }

    private void validateEnumTable(BValue[] returns, boolean isLowercase) {
        String id = "ID";
        String enumType = "ENUM_TYPE";
        if (isLowercase) {
            id = id.toLowerCase();
            enumType = enumType.toLowerCase();
        }
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returns[0]).getMap();
        Assert.assertEquals(result.size(), 2);
        Assert.assertEquals(((BInteger) result.get(id)).intValue(), 1);
        Assert.assertEquals(result.get(enumType).toString(), "doctor");
    }

    private void validateGeoTable(BValue[] returns, boolean isLowercase) {
        String id = "ID";
        String type = "GEOM";
        if (isLowercase) {
            id = id.toLowerCase();
            type = type.toLowerCase();
        }
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returns[0]).getMap();
        Assert.assertEquals(result.size(), 2);
        Assert.assertEquals(((BInteger) result.get(id)).intValue(), 1);
        Assert.assertEquals(result.get(type).toString(), "POINT (7 52)");
    }

    private void validateJsonTable(BValue[] returns, boolean isLowercase) {
        String id = "ID";
        String type = "JSON_TYPE";
        if (isLowercase) {
            id = id.toLowerCase();
            type = type.toLowerCase();
        }
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returns[0]).getMap();
        Assert.assertEquals(result.size(), 2);
        Assert.assertEquals(((BInteger) result.get(id)).intValue(), 1);
        String jsonString = result.get(type).toString();
        Assert.assertTrue(jsonString.equalsIgnoreCase("{\"id\":100,\"name\":\"Joe\",\"groups\":[2,5]}")
                || jsonString.equalsIgnoreCase("{\"id\":100, \"name\":\"Joe\", \"groups\":[2, 5]}"));
    }

    private void validateIntervalTable(BValue[] returns, boolean isLowercase) {
        String id = "ID";
        String type = "INTERVAL_TYPE";
        if (isLowercase) {
            id = id.toLowerCase();
            type = type.toLowerCase();
        }
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returns[0]).getMap();
        Assert.assertEquals(result.size(), 2);
        Assert.assertEquals(((BInteger) result.get(id)).intValue(), 1);
        Assert.assertEquals(result.get(type).toString(), "INTERVAL '2:00' HOUR TO MINUTE");
    }
}
