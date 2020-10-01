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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.test.utils.ByteArrayUtils;
import org.testng.Assert;
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

    @Test(description = "Test Map constrained with type negative semantic validations.",
            groups = { "brokenOnNewParser" })
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
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstrainedMapValueTypePositive");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "kevin");
    }

    @Test(description = "Test Map constrained with value type value retrieval negative case.",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map\\}KeyNotFound \\{\"message\":\"cannot find" +
                  " key 'names'.*")
    public void testConstrainedMapValueTypeNegative() {
        BRunUtil.invoke(compileResult, "testConstrainedMapValueTypeNegative");
    }

    @Test(description = "Test Map constrained with value type index based value retrieval positive case.")
    public void testConstrainedMapValueTypeIndexBasedPositive() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstrainedMapValueTypeIndexBasedPositive");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "kevin");
    }

    @Test(description = "Test Map constrained with value type index based value retrieval negative case.")
    public void testConstrainedMapValueTypeIndexBasedNegative() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstrainedMapValueTypeIndexBasedNegative");
        Assert.assertNull(returns[0]);
    }

    @Test(description = "Test Map constrained with user defined type value retrieval positive case.")
    public void testConstrainedMapStructTypePositive() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstrainedMapStructTypePositive");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[0].stringValue(), "Jack");
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 25);
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
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testConstrainedMapValueTypeAssignWithMemberAccessPositive");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[0].stringValue(), "kevin");
        Assert.assertEquals(returns[1].stringValue(), "ratnasekera");
    }

    @Test(description = "Test Map constrained with another constrained map.")
    public void testConstrainedMapConstrainedWithConstrainedMap() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testConstrainedMapConstrainedWithConstrainedMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[0].stringValue(), "kevin");
        Assert.assertEquals(returns[1].stringValue(), "ratnasekera");
    }

    @Test(description = "Test Map constrained with value type array.")
    public void testConstrainedMapConstrainedWithValueTypeArray() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testConstrainedMapConstrainedWithValueTypeArray");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 25);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 30);
    }

    @Test(description = "Test Map constrained with value type int positive.")
    public void testConstrainedMapIntTypePositive() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testConstrainedMapIntTypePositive");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 36);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 63);
    }

    @Test(description = "Test Map constrained with value type int negative.")
    public void testConstrainedMapIntTypeNegative() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testConstrainedMapIntTypeNegative");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test(description = "Test Map constrained with value type float positive.")
    public void testConstrainedMapFloatTypePositive() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testConstrainedMapFloatTypePositive");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertTrue(returns[0] instanceof BFloat);
        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 3.6);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 6.3);
    }

    @Test(description = "Test Map constrained with value type float negative.")
    public void testConstrainedMapFloatTypeNegative() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testConstrainedMapFloatTypeNegative");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertTrue(returns[0] instanceof BFloat);
        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 0.0);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 0.0);
    }

    @Test(description = "Test Map constrained with value type boolean positive.")
    public void testConstrainedMapBooleanTypePositive() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                                           "testConstrainedMapBooleanTypePositive");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(returns[1] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());
    }

    @Test(description = "Test Map constrained with value type boolean negative.")
    public void testConstrainedMapBooleanTypeNegative() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testConstrainedMapBooleanTypeNegative");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(returns[1] instanceof BBoolean);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());
    }

    @Test(description = "Test Map constrained with value type blob positive.")
    public void testConstrainedMapBlobTypePositive() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testConstrainedMapBlobTypePositive");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertTrue(returns[1] instanceof BValueArray);
        ByteArrayUtils.assertJBytesWithBBytes(((BValueArray) returns[0]).getBytes(), "hi".getBytes());
        ByteArrayUtils.assertJBytesWithBBytes(((BValueArray) returns[1]).getBytes(), "ballerina".getBytes());
    }

    @Test(description = "Test Map constrained with value type blob negative.")
    public void testConstrainedMapBlobTypeNegative() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testConstrainedMapBlobTypeNegative");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertTrue(returns[1] instanceof BValueArray);
        ByteArrayUtils.assertJBytesWithBBytes(((BValueArray) returns[0]).getBytes(), "hi".getBytes());
        ByteArrayUtils.assertJBytesWithBBytes(((BValueArray) returns[1]).getBytes(), "ballerina".getBytes());
    }

    @Test(description = "Test cast map constrained with value type from map any positive.")
    public void testConstrainedMapValueTypeCast() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstrainedMapValueTypeCast");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "kevin");
    }

    @Test(description = "Test cast map constrained with value type from map any positive.")
    public void testConstrainedMapValueTypeCastNegative() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstrainedMapValueTypeCastNegative");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BError);
        String errorMsg = ((BMap<String, BString>) ((BError) returns[0]).getDetails()).get("message").stringValue();
        Assert.assertTrue(errorMsg.startsWith("incompatible types: 'map<string>' cannot be cast to 'map<int>'"));

    }

    @Test(description = "Test cast map constrained with ref type from map any positive.")
    public void testConstrainedMapRefTypeCast() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstrainedMapRefTypeCast");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[0].stringValue(), "Jack");
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 25);
    }

    @Test(description = "Test cast map constrained with ref type from map any negative.")
    public void testConstrainedMapRefTypeCastNegative() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstrainedMapRefTypeCastNegative");
        Assert.assertTrue(returns[0] instanceof BError);
        String errorMsg = ((BMap<String, BString>) ((BError) returns[0]).getDetails()).get("message").stringValue();
        Assert.assertTrue(errorMsg.startsWith("incompatible types: 'map<Person>' cannot be cast to 'map<int>'"));

    }

    @Test(description = "Test map constrained with string update.")
    public void testUpdateStringMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUpdateStringMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "update");
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
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructConstrainedMapRuntimeCast");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[0].stringValue(), "Jack");
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 25);
    }

    @Test(description = "Test cast equivalent struct constrained maps in compile time.")
    public void testStructConstrainedMapStaticCast() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructConstrainedMapStaticCast");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[0].stringValue(), "Jack");
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 25);
    }

    @Test(description = "Test equivalent struct constrained map update negative.")
    public void testStructEquivalentMapUpdate() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructEquivalentMapUpdate");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[0].stringValue(), "Jack");
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 25);
    }

    @Test(description = "Test cast equivalent struct constrained maps in runtime time.")
    public void testStructEquivalentMapAccess() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructEquivalentMapAccess");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[0].stringValue(), "Mervin");
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 25);
    }

    @Test(description = "Test struct map update after casting to map any.")
    public void testStructMapUpdate() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructMapUpdate");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[0].stringValue(), "Arnold");
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 45);
    }

    @Test(description = "Test runtime cast for constrained maps of structurally not equivalent.")
    public void testStructNotEquivalentRuntimeCast() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructNotEquivalentRuntimeCast");
        Assert.assertTrue(returns[0] instanceof BError);
        String errorMsg = ((BMap<String, BString>) ((BError) returns[0]).getDetails()).get("message").stringValue();
        Assert.assertTrue(errorMsg.startsWith("incompatible types: 'map<Employee>' cannot be cast to 'map<Person>'"));
    }

    @Test(description = "Test runtime cast for any map to int map.")
    public void testAnyMapToValueTypeRuntimeCast() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnyMapToValueTypeRuntimeCast");
        Assert.assertTrue(returns[0] instanceof BError);
        String errorMsg = ((BMap<String, BString>) ((BError) returns[0]).getDetails()).get("message").stringValue();
        Assert.assertTrue(errorMsg.startsWith("incompatible types: 'map' cannot be cast to 'map<int>'"));
    }

    @Test(description = "Test runtime cast for any map to Employee map.")
    public void testAnyMapToRefTypeRuntimeCast() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnyMapToRefTypeRuntimeCast");
        Assert.assertTrue(returns[0] instanceof BError);
        String errorMsg = ((BMap<String, BString>) ((BError) returns[0]).getDetails()).get("message").stringValue();
        Assert.assertTrue(errorMsg.startsWith("incompatible types: 'map' cannot be cast to 'map<Employee>'"));
    }

    @Test(description = "Test struct to map conversion for constrained map.")
    public void testMapToStructConversion() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMapToStructConversion");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 63);
    }

    @Test(description = "Test struct to map conversion for constrained map negative.")
    public void testMapFunctionsOnConstrainedMaps() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMapFunctionsOnConstrainedMaps");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].size(), 2);
    }

    @Test(description = "Test struct to map conversion for constrained map negative.")
    public void testForEachOnConstrainedMaps() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testForEachOnConstrainedMaps");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "o");
        Assert.assertEquals(returns[1].stringValue(), "a");
    }

    @Test(description = "Test constrained map of string array.")
    public void testMapOfElementTypeArray() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMapOfElementTypeArray");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Kollupitiya");
        Assert.assertEquals(returns[1].stringValue(), "Ja-Ela");
    }

    @Test(description = "Test constrained map of ref array.")
    public void testMapOfElementTypeRefArray() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMapOfElementTypeRefArray");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[0].stringValue(), "Jack");
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 25);
    }

    @Test(description = "Test json to struct conversion where struct is included with constrained map.")
    public void testJsonToStructConversionStructWithConstrainedMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonToStructConversionStructWithConstrainedMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Colombo");
        Assert.assertEquals(returns[1].stringValue(), "SriLanka");
    }

    @Test(description = "Test json to struct conversion where struct is included with constrained map negative.")
    public void testJsonToStructConversionStructWithConstrainedMapNegative() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testJsonToStructConversionStructWithConstrainedMapNegative");
        Assert.assertTrue(returns[0] instanceof BError);
        String errorMsg = ((BMap<String, BString>) ((BError) returns[0]).getDetails()).get("message").stringValue();
        Assert.assertEquals(errorMsg, "'map<json>' value cannot be converted to 'PersonComplexTwo'");
    }

    @Test(description = "Test constrained map with union retrieving string value.")
    public void testConstrainedUnionRetrieveString() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testConstrainedUnionRetrieveString");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "kevin");
    }

    @Test(description = "Test constrained map with union retrieving int value.")
    public void testConstrainedUnionRetrieveInt() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testConstrainedUnionRetrieveInt");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 3);
    }

    @Test(description = "Test constrianed map Equivelent map insert.")
    public void testMapConstrianedWithStructEquivalency() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testMapConstrainedEquivalentMapInsert");
        Assert.assertEquals(returns.length, 2);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Jack");
        Assert.assertNotNull(returns[1]);
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 25);
    }

    @Test(description = "Test runtime struct equivalency with nested constrained maps.")
    public void testRuntimeStructEquivalencyWithNestedConstrainedMaps() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testRuntimeStructEquivalencyWithNestedConstrainedMaps");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "TR-ID");
    }

    @Test(description = "Test basic map constrained to union.")
    public void testMapConstrainedToUnion() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testMapConstrainedToUnion");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "test-value");
    }

    @Test(description = "Test basic map constrained to union case two.")
    public void testMapConstrainedToUnionCaseTwo() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testMapConstrainedToUnionCaseTwo");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
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
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testMapConstrainedToNullableUnion");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "test-nullable-union");
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
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testMapConstrainedToNullableUnionNonExistingKeyWithIndexAccess");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNull(returns[0]);
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

        BValue[] returns = BRunUtil.invoke(compileResult, "testMapAnyDataClosedRecordAssignment");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "Jack");
    }
}
