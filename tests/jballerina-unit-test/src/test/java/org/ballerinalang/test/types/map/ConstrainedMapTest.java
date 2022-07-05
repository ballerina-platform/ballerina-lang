/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.types.map;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.utils.ByteArrayUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for constraining map types with any other builtin or user defined types.
 */
public class ConstrainedMapTest {

    private CompileResult compileResult;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/map/constrained-map.bal");
        negativeResult = BCompileUtil.compile("test-src/types/map/constrained-map-negative.bal");
    }

    @Test(description = "Test Map constrained with type negative semantic validations.")
    public void testConstrainedMapNegative() {
        Assert.assertEquals(negativeResult.getErrorCount(), 7);
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'map<int>', found 'map'", 3, 12);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'string'", 7, 44);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found 'int'", 13, 23);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'map<int>', found 'map<string>'",
                19, 12);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'map<Person>', " + "found 'map<Employee>'", 35, 31);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'map<Person>', found 'map'", 45,
                31);
        BAssertUtil.validateError(negativeResult, i, "incompatible types: 'map<Person>' cannot be cast to" +
                " 'map<Student>'", 77, 29);
    }

    @Test(description = "Test Map constrained with value type value retrieval positive case.")
    public void testConstrainedMapValueTypePositive() {
        Object returns = BRunUtil.invoke(compileResult, "testConstrainedMapValueTypePositive");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "kevin");
    }

    @Test(description = "Test Map constrained with value type value retrieval negative case.",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.map\\}KeyNotFound \\{\"message\":\"cannot find" +
                            " key 'names'.*")
    public void testConstrainedMapValueTypeNegative() {
        BRunUtil.invoke(compileResult, "testConstrainedMapValueTypeNegative");
    }

    @Test(description = "Test Map constrained with value type index based value retrieval positive case.")
    public void testConstrainedMapValueTypeIndexBasedPositive() {
        Object returns = BRunUtil.invoke(compileResult, "testConstrainedMapValueTypeIndexBasedPositive");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "kevin");
    }

    @Test(description = "Test Map constrained with value type index based value retrieval negative case.")
    public void testConstrainedMapValueTypeIndexBasedNegative() {
        Object returns = BRunUtil.invoke(compileResult, "testConstrainedMapValueTypeIndexBasedNegative");
        Assert.assertNull(returns);
    }

    @Test(description = "Test Map constrained with user defined type value retrieval positive case.")
    public void testConstrainedMapStructTypePositive() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testConstrainedMapStructTypePositive");
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertEquals(returns.get(0).toString(), "Jack");
        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertEquals(returns.get(1), 25L);
    }

    @Test(description = "Test Map constrained with user defined type value retrieval negative case.",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map\\}KeyNotFound \\{\"message\":\"cannot " +
                    "find key 'item-not'.*")
    public void testConstrainedMapStructTypeNegative() {
        BRunUtil.invoke(compileResult, "testConstrainedMapStructTypeNegative");
    }

    @Test(description = "Test Map constrained with value type value retrieval positive case" +
            " assignment with member access expressions.")
    public void testConstrainedMapValueTypeAssignWithMemberAccessPositive() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult,
                "testConstrainedMapValueTypeAssignWithMemberAccessPositive");
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertEquals(returns.get(0).toString(), "kevin");
        Assert.assertEquals(returns.get(1).toString(), "ratnasekera");
    }

    @Test(description = "Test Map constrained with another constrained map.")
    public void testConstrainedMapConstrainedWithConstrainedMap() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult,
                "testConstrainedMapConstrainedWithConstrainedMap");
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertEquals(returns.get(0).toString(), "kevin");
        Assert.assertEquals(returns.get(1).toString(), "ratnasekera");
    }

    @Test(description = "Test Map constrained with value type array.")
    public void testConstrainedMapConstrainedWithValueTypeArray() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult,
                "testConstrainedMapConstrainedWithValueTypeArray");
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertEquals(returns.get(0), 25L);
        Assert.assertEquals(returns.get(1), 30L);
    }

    @Test(description = "Test Map constrained with value type int positive.")
    public void testConstrainedMapIntTypePositive() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult,
                "testConstrainedMapIntTypePositive");
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertEquals(returns.get(0), 36L);
        Assert.assertEquals(returns.get(1), 63L);
    }

    @Test(description = "Test Map constrained with value type int negative.")
    public void testConstrainedMapIntTypeNegative() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult,
                "testConstrainedMapIntTypeNegative");
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertEquals(returns.get(0), 0L);
        Assert.assertEquals(returns.get(1), 0L);
    }

    @Test(description = "Test Map constrained with value type float positive.")
    public void testConstrainedMapFloatTypePositive() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult,
                "testConstrainedMapFloatTypePositive");
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertTrue(returns.get(0) instanceof Double);
        Assert.assertTrue(returns.get(1) instanceof Double);
        Assert.assertEquals(returns.get(0), 3.6);
        Assert.assertEquals(returns.get(1), 6.3);
    }

    @Test(description = "Test Map constrained with value type float negative.")
    public void testConstrainedMapFloatTypeNegative() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult,
                "testConstrainedMapFloatTypeNegative");
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertTrue(returns.get(0) instanceof Double);
        Assert.assertTrue(returns.get(1) instanceof Double);
        Assert.assertEquals(returns.get(0), 0.0);
        Assert.assertEquals(returns.get(1), 0.0);
    }

    @Test(description = "Test Map constrained with value type boolean positive.")
    public void testConstrainedMapBooleanTypePositive() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult,
                "testConstrainedMapBooleanTypePositive");
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue(returns.get(1) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0));
        Assert.assertFalse((Boolean) returns.get(1));
    }

    @Test(description = "Test Map constrained with value type boolean negative.")
    public void testConstrainedMapBooleanTypeNegative() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult,
                "testConstrainedMapBooleanTypeNegative");
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue(returns.get(1) instanceof Boolean);
        Assert.assertFalse((Boolean) returns.get(0));
        Assert.assertFalse((Boolean) returns.get(1));
    }

    @Test(description = "Test Map constrained with value type blob positive.")
    public void testConstrainedMapBlobTypePositive() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult,
                "testConstrainedMapBlobTypePositive");
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertTrue(returns.get(0) instanceof BArray);
        Assert.assertTrue(returns.get(1) instanceof BArray);
        ByteArrayUtils.assertJBytesWithBBytes(((BArray) returns.get(0)).getBytes(), "hi".getBytes());
        ByteArrayUtils.assertJBytesWithBBytes(((BArray) returns.get(1)).getBytes(), "ballerina".getBytes());
    }

    @Test(description = "Test Map constrained with value type blob negative.")
    public void testConstrainedMapBlobTypeNegative() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult,
                "testConstrainedMapBlobTypeNegative");
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertTrue(returns.get(0) instanceof BArray);
        Assert.assertTrue(returns.get(1) instanceof BArray);
        ByteArrayUtils.assertJBytesWithBBytes(((BArray) returns.get(0)).getBytes(), "hi".getBytes());
        ByteArrayUtils.assertJBytesWithBBytes(((BArray) returns.get(1)).getBytes(), "ballerina".getBytes());
    }

    @Test(description = "Test cast map constrained with value type from map any positive.")
    public void testConstrainedMapValueTypeCast() {
        Object returns = BRunUtil.invoke(compileResult, "testConstrainedMapValueTypeCast");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "kevin");
    }

    @Test(description = "Test cast map constrained with value type from map any positive.")
    public void testConstrainedMapValueTypeCastNegative() {
        Object returns = BRunUtil.invoke(compileResult, "testConstrainedMapValueTypeCastNegative");
        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof BError);
        String errorMsg = ((BMap<String, BString>) ((BError) returns).getDetails()).get(
                StringUtils.fromString("message")).toString();
        Assert.assertTrue(errorMsg.startsWith("incompatible types: 'map<string>' cannot be cast to 'map<int>'"));

    }

    @Test(description = "Test cast map constrained with ref type from map any positive.")
    public void testConstrainedMapRefTypeCast() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testConstrainedMapRefTypeCast");
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertEquals(returns.get(0).toString(), "Jack");
        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertEquals(returns.get(1), 25L);
    }

    @Test(description = "Test cast map constrained with ref type from map any negative.")
    public void testConstrainedMapRefTypeCastNegative() {
        Object returns = BRunUtil.invoke(compileResult, "testConstrainedMapRefTypeCastNegative");
        Assert.assertTrue(returns instanceof BError);
        String errorMsg =
                ((BMap<String, BString>) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))
                        .toString();
        Assert.assertTrue(errorMsg.startsWith("incompatible types: 'map<Person>' cannot be cast to 'map<int>'"));

    }

    @Test(description = "Test map constrained with string update.")
    public void testUpdateStringMap() {
        Object returns = BRunUtil.invoke(compileResult, "testUpdateStringMap");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "update");
    }

    @Test(description = "Test map constrained with string update with invalid type negative.",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map\\}InherentTypeViolation " +
                    "\\{\"message\":\"invalid map" +
                    " insertion: expected value of type 'string', found 'int'.*")
    public void testStringMapUpdateWithInvalidTypeNegativeCase() {
        BRunUtil.invoke(compileResult, "testStringMapUpdateWithInvalidTypeNegativeCase");
    }

    @Test(description = "Test cast equivalent struct constrained maps in runtime time.")
    public void testStructConstrainedMapRuntimeCast() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testStructConstrainedMapRuntimeCast");
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertEquals(returns.get(0).toString(), "Jack");
        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertEquals(returns.get(1), 25L);
    }

    @Test(description = "Test cast equivalent struct constrained maps in compile time.")
    public void testStructConstrainedMapStaticCast() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testStructConstrainedMapStaticCast");
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertEquals(returns.get(0).toString(), "Jack");
        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertEquals(returns.get(1), 25L);
    }

    @Test(description = "Test equivalent struct constrained map update negative.")
    public void testStructEquivalentMapUpdate() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testStructEquivalentMapUpdate");
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertEquals(returns.get(0).toString(), "Jack");
        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertEquals(returns.get(1), 25L);
    }

    @Test(description = "Test cast equivalent struct constrained maps in runtime time.")
    public void testStructEquivalentMapAccess() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testStructEquivalentMapAccess");
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertEquals(returns.get(0).toString(), "Mervin");
        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertEquals(returns.get(1), 25L);
    }

    @Test(description = "Test struct map update after casting to map any.")
    public void testStructMapUpdate() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testStructMapUpdate");
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertEquals(returns.get(0).toString(), "Arnold");
        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertEquals(returns.get(1), 45L);
    }

    @Test(description = "Test runtime cast for constrained maps of structurally not equivalent.")
    public void testStructNotEquivalentRuntimeCast() {
        Object returns = BRunUtil.invoke(compileResult, "testStructNotEquivalentRuntimeCast");
        Assert.assertTrue(returns instanceof BError);
        String errorMsg =
                ((BMap<String, BString>) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))
                        .toString();
        Assert.assertTrue(errorMsg.startsWith("incompatible types: 'map<Employee>' cannot be cast to 'map<Person>'"));
    }

    @Test(description = "Test runtime cast for any map to int map.")
    public void testAnyMapToValueTypeRuntimeCast() {
        Object returns = BRunUtil.invoke(compileResult, "testAnyMapToValueTypeRuntimeCast");
        Assert.assertTrue(returns instanceof BError);
        String errorMsg =
                ((BMap<String, BString>) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))
                        .toString();
        Assert.assertTrue(errorMsg.startsWith("incompatible types: 'map' cannot be cast to 'map<int>'"));
    }

    @Test(description = "Test runtime cast for any map to Employee map.")
    public void testAnyMapToRefTypeRuntimeCast() {
        Object returns = BRunUtil.invoke(compileResult, "testAnyMapToRefTypeRuntimeCast");
        Assert.assertTrue(returns instanceof BError);
        String errorMsg =
                ((BMap<String, BString>) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))
                        .toString();
        Assert.assertTrue(errorMsg.startsWith("incompatible types: 'map' cannot be cast to 'map<Employee>'"));
    }

    @Test(description = "Test struct to map conversion for constrained map.")
    public void testMapToStructConversion() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testMapToStructConversion");
        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertEquals(returns.get(0), 100L);
        Assert.assertEquals(returns.get(1), 63L);
    }

    @Test(description = "Test struct to map conversion for constrained map negative.")
    public void testMapFunctionsOnConstrainedMaps() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testMapFunctionsOnConstrainedMaps");
        Assert.assertTrue(returns instanceof BArray);
        Assert.assertEquals(returns.size(), 2);
    }

    @Test(description = "Test struct to map conversion for constrained map negative.")
    public void testForEachOnConstrainedMaps() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testForEachOnConstrainedMaps");
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "o");
        Assert.assertEquals(returns.get(1).toString(), "a");
    }

    @Test(description = "Test constrained map of string array.")
    public void testMapOfElementTypeArray() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testMapOfElementTypeArray");
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "Kollupitiya");
        Assert.assertEquals(returns.get(1).toString(), "Ja-Ela");
    }

    @Test(description = "Test constrained map of ref array.")
    public void testMapOfElementTypeRefArray() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testMapOfElementTypeRefArray");
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertEquals(returns.get(0).toString(), "Jack");
        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertEquals(returns.get(1), 25L);
    }

    @Test(description = "Test json to struct conversion where struct is included with constrained map.")
    public void testJsonToStructConversionStructWithConstrainedMap() {
        BArray returns =
                (BArray) BRunUtil.invoke(compileResult, "testJsonToStructConversionStructWithConstrainedMap");
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "Colombo");
        Assert.assertEquals(returns.get(1).toString(), "SriLanka");
    }

    @Test(description = "Test json to struct conversion where struct is included with constrained map negative.")
    public void testJsonToStructConversionStructWithConstrainedMapNegative() {
        Object returns = BRunUtil.invoke(compileResult,
                "testJsonToStructConversionStructWithConstrainedMapNegative");
        Assert.assertTrue(returns instanceof BError);
        String errorMsg =
                ((BMap<String, BString>) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))
                        .toString();
        Assert.assertEquals(errorMsg, "'map<json>' value cannot be converted to 'PersonComplexTwo': " +
                "\n\t\tmap field 'address.city' should be of type 'int', found '\"Colombo\"'" +
                "\n\t\tmap field 'address.country' should be of type 'int', found '\"SriLanka\"'");
    }

    @Test(description = "Test constrained map with union retrieving string value.")
    public void testConstrainedUnionRetrieveString() {
        Object returns = BRunUtil.invoke(compileResult,
                "testConstrainedUnionRetrieveString");

        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "kevin");
    }

    @Test(description = "Test constrained map with union retrieving int value.")
    public void testConstrainedUnionRetrieveInt() {
        Object returns = BRunUtil.invoke(compileResult,
                "testConstrainedUnionRetrieveInt");

        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 3L);
    }

    @Test(description = "Test constrianed map Equivelent map insert.")
    public void testMapConstrianedWithStructEquivalency() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult,
                "testMapConstrainedEquivalentMapInsert");
        Assert.assertEquals(returns.size(), 2);
        Assert.assertNotNull(returns.get(0));
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "Jack");
        Assert.assertNotNull(returns.get(1));
        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertEquals(returns.get(1), 25L);
    }

    @Test(description = "Test runtime struct equivalency with nested constrained maps.")
    public void testRuntimeStructEquivalencyWithNestedConstrainedMaps() {
        Object returns = BRunUtil.invoke(compileResult,
                "testRuntimeStructEquivalencyWithNestedConstrainedMaps");

        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "TR-ID");
    }

    @Test(description = "Test basic map constrained to union.")
    public void testMapConstrainedToUnion() {
        Object returns = BRunUtil.invoke(compileResult,
                "testMapConstrainedToUnion");

        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "test-value");
    }

    @Test(description = "Test basic map constrained to union case two.")
    public void testMapConstrainedToUnionCaseTwo() {
        Object returns = BRunUtil.invoke(compileResult,
                "testMapConstrainedToUnionCaseTwo");

        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 2L);
    }

    @Test(description = "Test basic map constrained to union case three.",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map\\}KeyNotFound " +
                    "\\{\"message\":\"cannot find key 'non-existing-key'.*")
    public void testMapConstrainedToUnionCaseThree() {
        BRunUtil.invoke(compileResult,
                "testMapConstrainedToUnionCaseThree");
    }

    @Test(description = "Test basic map constrained to nullable union.")
    public void testMapConstrainedToNullableUnion() {
        Object returns = BRunUtil.invoke(compileResult,
                "testMapConstrainedToNullableUnion");

        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "test-nullable-union");
    }

    @Test(description = "Test basic map constrained to nullable union retrieve non existing key.",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map\\}KeyNotFound \\{\"message\":\"cannot " +
                    "find key 'nonexist'.*")
    public void testMapConstrainedToNullableUnionNonExistingKey() {
        BRunUtil.invoke(compileResult,
                "testMapConstrainedToNullableUnionNonExistingKey");
    }

    @Test(description = "Test basic map constrained to nullable union retrieve non existing key with index access.")
    public void testMapConstrainedToNullableUnionNonExistingKeyWithIndexAccess() {
        Object returns = BRunUtil.invoke(compileResult,
                "testMapConstrainedToNullableUnionNonExistingKeyWithIndexAccess");

        Assert.assertNull(returns);
    }

    @Test(description = "Test basic map constrained to string non existing key retrieve.",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map\\}KeyNotFound " +
                    "\\{\"message\":\"cannot find key 'nonexist-key'.*")
    public void testMapConstrainedStringNonExistingKeyRetrieve() {
        BRunUtil.invoke(compileResult,
                "testMapConstrainedStringNonExistingKeyRetrieve");
    }

    @Test(description = "Test inherent type violation with nil value.",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map\\}InherentTypeViolation " +
                    "\\{\"message\":\"invalid map" +
                    " insertion: expected value of type 'string', found '\\(\\)'.*")
    public void testInherentTypeViolationWithNilType() {
        BRunUtil.invoke(compileResult, "testInherentTypeViolationWithNilType");
    }

    @Test(description = "Test closed record assignment to map which is constrained to anydata.")
    public void testMapAnyDataClosedRecordAssignment() {

        Object returns = BRunUtil.invoke(compileResult, "testMapAnyDataClosedRecordAssignment");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Jack");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
        negativeResult = null;
    }
}
