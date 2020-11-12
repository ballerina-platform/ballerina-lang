/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.expressions.access;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * Test cases for member access.
 *
 * @since 1.0
 */
public class MemberAccessTest {

    private CompileResult result;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/access/member_access.bal");
        negativeResult = BCompileUtil.compile("test-src/expressions/access/member_access_negative.bal");
    }

    @Test(groups = { "disableOnOldParser" })
    public void testNegativeCases() {
        int i = 0;
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'string'", 33, 12);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'string'", 34, 12);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'a|b'", 35, 12);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'string'", 37, 12);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'string'", 38, 12);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'a|b'", 39, 12);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'string'", 41, 12);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'string'", 42, 12);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'a|b'", 43, 12);
        validateError(negativeResult, i++, "invalid operation: type 'int[]?' does not support indexing", 53, 9);
        validateError(negativeResult, i++, "invalid operation: type 'Employee[3]?' does not support indexing", 54, 9);
        validateError(negativeResult, i++, "invalid operation: type '(int[]|Employee[3])?' does not support indexing"
                , 55, 9);
        validateError(negativeResult, i++, "list index out of range: index: '4'", 60, 12);
        validateError(negativeResult, i++, "list index out of range: index: '5'", 65, 12);
        validateError(negativeResult, i++, "incompatible types: expected 'boolean', found 'boolean?'", 74, 19);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'int?'", 75, 14);
        validateError(negativeResult, i++, "undefined field 'names' in 'Employee'", 76, 17);
        validateError(negativeResult, i++, "incompatible types: expected 'string', found '(string|boolean|int)?'", 77
                , 17);
        validateError(negativeResult, i++, "incompatible types: expected 'string', found '(int|string)?'", 86, 19);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found '(int|string)?'", 87, 14);
        validateError(negativeResult, i++, "incompatible types: expected 'string?', found '(int|string)?'", 88, 18);
        validateError(negativeResult, i++, "incompatible types: expected '(int|string)', found '(int|string)?'", 89,
                      21);
        validateError(negativeResult, i++, "incompatible types: expected 'string', found 'string?'", 95, 17);
        validateError(negativeResult, i++, "incompatible types: expected 'string', found 'string?'", 96, 17);
        validateError(negativeResult, i++, "incompatible types: expected 'map<int>', found 'map<int>?'", 99, 20);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'int?'", 100, 15);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'int?'", 101, 15);
        validateError(negativeResult, i++, "incompatible types: expected 'string', found 'string?'", 104, 17);
        validateError(negativeResult, i++, "incompatible types: expected 'boolean', found '(string|boolean|int)?'",
                      105, 18);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'string'", 124, 13);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found '" +
                "(string|int|anydata|boolean|float)'", 125, 14);
        validateError(negativeResult, i++, "incompatible types: expected '(int|boolean)', found '(int|boolean)?'",
                      126, 21);
        validateError(negativeResult, i++, "incompatible types: expected 'float', found '(anydata|float)'", 127, 15);
        validateError(negativeResult, i++, "invalid operation: type '(string|int|anydata|boolean|float)' does not " +
                "support indexing", 128, 16);
        validateError(negativeResult, i++, "incompatible types: expected 'string', found '(string|map<float>|xml)?'",
                      136, 16);
        validateError(negativeResult, i++, "incompatible types: expected 'string', found '(string|map<float>|xml)?'",
                      137, 17);
        validateError(negativeResult, i++, "invalid operation: type '(string|map<float>|xml)?' does not support " +
                "indexing", 138, 16);
        validateError(negativeResult, i++, "incompatible types: expected 'string', found '(string|int|map<float>)?'",
                      146, 16);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found '" +
                "(string|int|anydata|map<float>)'", 147, 14);
        validateError(negativeResult, i++, "incompatible types: expected 'map<float>?', found '(int|map<float>)?'",
                      148, 21);
        validateError(negativeResult, i++, "invalid operation: type '(anydata|int|map<float>)' does not support " +
                "indexing", 149, 17);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'string'", 156, 15);
        validateError(negativeResult, i++, "incompatible types: expected 'float', found 'string'", 157, 17);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'string'", 158, 21);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'string'", 159, 21);
        validateError(negativeResult, i++, "invalid operation: type 'foo|1' does not support indexing", 169, 17);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'foo|1'", 170, 20);
        validateError(negativeResult, i++, "invalid operation: type 'string' does not support member access for " +
                "assignment", 175, 5);
        validateError(negativeResult, i++, "list index out of range: index: '5'", 182, 12);
        validateError(negativeResult, i++, "list index out of range: index: '5'", 187, 12);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'string'", 195, 14);
        validateError(negativeResult, i++, "undefined field 'age' in 'Employee'", 196, 14);
        validateError(negativeResult, i++, "missing key expr in member access expr", 201, 26);
        validateError(negativeResult, i++, "invalid expression statement", 202, 5);
        validateError(negativeResult, i++, "missing key expr in member access expr", 202, 14);
        validateError(negativeResult, i++, "missing semicolon token", 203, 1);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @Test(dataProvider = "listMemberAccessFunctions")
    public void testListMemberAccess(String function) {
        BValue[] returns = BRunUtil.invoke(result, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @DataProvider(name = "listMemberAccessFunctions")
    public Object[][] listMemberAccessFunctions() {
        return new Object[][] {
            { "testOpenArrayMemberAccessByLiteralPositive" },
            { "testOpenArrayMemberAccessByConstPositive" },
            { "testOpenArrayMemberAccessByVariablePositive" },
            { "testOpenArrayMemberAccessByFiniteTypeVariablePositive" },
            { "testClosedArrayMemberAccessByLiteralPositive" },
            { "testClosedArrayMemberAccessByConstPositive" },
            { "testClosedArrayMemberAccessByVariablePositive" },
            { "testClosedArrayMemberAccessByFiniteTypeVariablePositive" },
            { "testTupleMemberAccessByLiteralPositive" },
            { "testTupleMemberAccessByConstPositive" },
            { "testTupleMemberAccessByVariablePositive" },
            { "testTupleMemberAccessByFiniteTypeVariablePositive" },
        };
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.array\\}IndexOutOfRange \\{\"message\":\"array " +
                    "index out of range: index: 4, size: 3\"\\}.*")
    public void testOpenArrayMemberAccessByLiteralIndexOutOfRange() {
        BRunUtil.invoke(result, "testOpenArrayMemberAccessByLiteralIndexOutOfRange");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.array\\}IndexOutOfRange \\{\"message\":\"array " +
                    "index out of range: index: 6, size: 2\"\\}.*")
    public void testOpenArrayMemberAccessByConstIndexOutOfRange() {
        BRunUtil.invoke(result, "testOpenArrayMemberAccessByConstIndexOutOfRange");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.array\\}IndexOutOfRange \\{\"message\":\"array " +
                    "index out of range: index: 5, size: 2\"\\}.*")
    public void testOpenArrayMemberAccessByVariableIndexOutOfRange() {
        BRunUtil.invoke(result, "testOpenArrayMemberAccessByVariableIndexOutOfRange");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.array\\}IndexOutOfRange \\{\"message\":\"array " +
                    "index out of range: index: 9, size: 2\"\\}.*")
    public void testOpenArrayMemberAccessByFiniteTypeVariableIndexOutOfRange() {
        BRunUtil.invoke(result, "testOpenArrayMemberAccessByFiniteTypeVariableIndexOutOfRange");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.array\\}IndexOutOfRange \\{\"message\":\"array " +
                    "index out of range: index: 5, size: 2\"\\}.*")
    public void testClosedArrayMemberAccessByVariableIndexOutOfRange() {
        BRunUtil.invoke(result, "testClosedArrayMemberAccessByVariableIndexOutOfRange");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.array\\}IndexOutOfRange \\{\"message\":\"array " +
                    "index out of range: index: 9, size: 4\"\\}.*")
    public void testClosedArrayMemberAccessByFiniteTypeVariableIndexOutOfRange() {
        BRunUtil.invoke(result, "testClosedArrayMemberAccessByFiniteTypeVariableIndexOutOfRange");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.array\\}IndexOutOfRange \\{\"message\":\"tuple " +
                    "index out of range: index: 3, size: 2\"\\}.*")
    public void testTupleMemberAccessByVariableIndexOutOfRange() {
        BRunUtil.invoke(result, "testTupleMemberAccessByVariableIndexOutOfRange");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.array\\}IndexOutOfRange \\{\"message\":\"tuple " +
                    "index out of range: index: 9, size: 3\"\\}.*")
    public void testTupleMemberAccessByFiniteTypeVariableIndexOutOfRange() {
        BRunUtil.invoke(result, "testTupleMemberAccessByFiniteTypeVariableIndexOutOfRange");
    }

    @Test(dataProvider = "recordMemberAccessFunctions")
    public void testRecordMemberAccess(String function) {
        BValue[] returns = BRunUtil.invoke(result, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @DataProvider(name = "recordMemberAccessFunctions")
    public Object[][] recordMemberAccessFunctions() {
        return new Object[][] {
            { "testRecordMemberAccessByLiteral" },
            { "testRecordMemberAccessByConstant" },
            { "testRecordMemberAccessByVariable" },
            { "testRecordMemberAccessForNonExistingKey" }
        };
    }

    @Test(dataProvider = "mapMemberAccessFunctions")
    public void testMapMemberAccess(String function) {
        BValue[] returns = BRunUtil.invoke(result, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @DataProvider(name = "mapMemberAccessFunctions")
    public Object[][] mapMemberAccessFunctions() {
        return new Object[][] {
            { "testMapMemberAccessByLiteral" },
            { "testMapMemberAccessByConstant" },
            { "testMapMemberAccessByVariable" },
            { "testMapAccessForNonExistingKey" }
        };
    }

    @Test(dataProvider = "optionalMappingMemberAccessFunctions")
    public void testOptionalMappingMemberAccess(String function) {
        BValue[] returns = BRunUtil.invoke(result, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @DataProvider(name = "optionalMappingMemberAccessFunctions")
    public Object[][] optionalMappingMemberAccessFunctions() {
        return new Object[][] {
            { "testMemberAccessOnNillableMap1" },
            { "testMemberAccessOnNillableMap2" },
            { "testMemberAccessNilLiftingOnNillableMap1" },
            { "testMemberAccessNilLiftingOnNillableMap2" },
            { "testMemberAccessOnNillableRecord1" },
            { "testMemberAccessOnNillableRecord2" },
            { "testMemberAccessNilLiftingOnNillableRecord1" },
            { "testMemberAccessNilLiftingOnNillableRecord2" },
        };
    }

    @Test(dataProvider = "mappingUnionMemberAccessFunctions")
    public void testMappingUnionMemberAccess(String function) {
        BValue[] returns = BRunUtil.invoke(result, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @DataProvider(name = "mappingUnionMemberAccessFunctions")
    public Object[][] mappingUnionMemberAccessFunctions() {
        return new Object[][] {
            { "testMemberAccessOnRecordUnion" },
            { "testMemberAccessOnMapUnion" }
        };
    }

    @Test(dataProvider = "stringMemberAccessFunctions")
    public void testStringMemberAccess(String function) {
        BValue[] returns = BRunUtil.invoke(result, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @DataProvider(name = "stringMemberAccessFunctions")
    public Object[][] stringMemberAccessFunctions() {
        return new Object[][] {
                { "testVariableStringMemberAccess" },
                { "testConstStringMemberAccess1" },
                { "testConstStringMemberAccess2" },
                { "testFiniteTypeStringMemberAccess" }
        };
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.string\\}IndexOutOfRange \\{\"message\":\"string "
                    + "index out of range: index: -1, size: 5\"\\}.*")
    public void testOutOfRangeStringMemberAccess1() {
        BValue[] returns = BRunUtil.invoke(result, "testOutOfRangeStringMemberAccess1");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.string\\}IndexOutOfRange \\{\"message\":\"string "
                    + "index out of range: index: 11, size: 11\"\\}.*")
    public void testOutOfRangeStringMemberAccess2() {
        BValue[] returns = BRunUtil.invoke(result, "testOutOfRangeStringMemberAccess2");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.string\\}IndexOutOfRange \\{\"message\":\"string "
                    + "index out of range: index: 25, size: 12\"\\}.*")
    public void testOutOfRangeStringMemberAccess3() {
        BValue[] returns = BRunUtil.invoke(result, "testOutOfRangeStringMemberAccess3");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.string\\}IndexOutOfRange \\{\"message\":\"string "
                    + "index out of range: index: 4, size: 3\"\\}.*")
    public void testOutOfRangeFiniteTypeStringMemberAccess() {
        BValue[] returns = BRunUtil.invoke(result, "testOutOfRangeFiniteTypeStringMemberAccess");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testMemberAccessInUnionType() {
        BRunUtil.invoke(result, "testMemberAccessInUnionType");
    }
}
