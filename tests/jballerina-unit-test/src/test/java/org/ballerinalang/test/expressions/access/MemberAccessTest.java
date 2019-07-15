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

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
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

    @Test
    public void testNegativeCases() {
        Assert.assertEquals(negativeResult.getErrorCount(), 41);
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
        validateError(negativeResult, i++, "list index out of range: index: '4'", 60, 9);
        validateError(negativeResult, i++, "list index out of range: index: '5'", 65, 9);
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
        validateError(negativeResult, i, "invalid operation: type '(anydata|int|map<float>)' does not support " +
                "indexing", 149, 17);
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
            { "testOpenArrayMemberAccessByVariablePositive" },
            { "testOpenArrayMemberAccessByFiniteTypeVariablePositive" },
            { "testClosedArrayMemberAccessByLiteralPositive" },
            { "testClosedArrayMemberAccessByVariablePositive" },
            { "testClosedArrayMemberAccessByFiniteTypeVariablePositive" },
            { "testTupleMemberAccessByLiteralPositive" },
            { "testTupleMemberAccessByVariablePositive" },
            { "testTupleMemberAccessByFiniteTypeVariablePositive" },
        };
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina\\}IndexOutOfRange message=array index out of range: " +
                    "index: 4, size: 3.*")
    public void testOpenArrayMemberAccessByLiteralIndexOutOfRange() {
        BValue[] returns = BRunUtil.invoke(result, "testOpenArrayMemberAccessByLiteralIndexOutOfRange");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina\\}IndexOutOfRange message=array index out of range: " +
                    "index: 5, size: 2.*")
    public void testOpenArrayMemberAccessByVariableIndexOutOfRange() {
        BValue[] returns = BRunUtil.invoke(result, "testOpenArrayMemberAccessByVariableIndexOutOfRange");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina\\}IndexOutOfRange message=array index out of range: " +
                    "index: 9, size: 2.*")
    public void testOpenArrayMemberAccessByFiniteTypeVariableIndexOutOfRange() {
        BValue[] returns = BRunUtil.invoke(result, "testOpenArrayMemberAccessByFiniteTypeVariableIndexOutOfRange");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina\\}IndexOutOfRange message=array index out of range: " +
                    "index: 5, size: 2.*")
    public void testClosedArrayMemberAccessByVariableIndexOutOfRange() {
        BValue[] returns = BRunUtil.invoke(result, "testClosedArrayMemberAccessByVariableIndexOutOfRange");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina\\}IndexOutOfRange message=array index out of range: " +
                    "index: 9, size: 4.*")
    public void testClosedArrayMemberAccessByFiniteTypeVariableIndexOutOfRange() {
        BValue[] returns = BRunUtil.invoke(result, "testClosedArrayMemberAccessByFiniteTypeVariableIndexOutOfRange");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina\\}IndexOutOfRange message=tuple index out of range: " +
                    "index: 3, size: 2.*")
    public void testTupleMemberAccessByVariableIndexOutOfRange() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleMemberAccessByVariableIndexOutOfRange");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina\\}IndexOutOfRange message=tuple index out of range: " +
                    "index: 9, size: 3.*")
    public void testTupleMemberAccessByFiniteTypeVariableIndexOutOfRange() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleMemberAccessByFiniteTypeVariableIndexOutOfRange");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
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
}
