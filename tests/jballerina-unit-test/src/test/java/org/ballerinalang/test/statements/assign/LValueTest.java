/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.statements.assign;

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
 * Class to test lvalues of assignments.
 *
 * An lvalue is what the left hand side of an assignment evaluates to. An lvalue refers to a storage location which
 * is one of the following:
 *
 * - a variable;
 * - a specific named field of an object;
 * - the member of a container having a specific key, which will be either an integer or a string according as the
 * container is a list or a mapping.
 *
 * An lvalue that is both defined and initialized refers to a storage location that holds a value:
 * - an lvalue referring to a variable is always defined but may be uninitialized;
 * - an lvalue referring to a specific named field of an object is always defined but may not be initialized until the
 * init method returns
 * - an lvalue referring to member of a container having a specific key is undefined if the container does not have a
 * member with that key; if such an lvalue is defined, it is also initialized; note that an lvalue always refers to a
 * container that is already constructed.
 *
 * lvexpr :=
 *    variable-reference-lvexpr
 *    | field-access-lvexpr
 *    | member-access-lvexpr
 *
 * @since 1.0
 */
public class LValueTest {

    private CompileResult result, negativeResult, semanticsNegativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/assign/lvalue.bal");
        negativeResult = BCompileUtil.compile("test-src/statements/assign/lvalue_negative.bal");
        semanticsNegativeResult = BCompileUtil.compile("test-src/statements/assign/lvalue-semantics-negative.bal");
    }

    @Test
    public void testNegativeCases() {
        Assert.assertEquals(negativeResult.getErrorCount(), 1);
        validateError(negativeResult, 0, "uninitialized field 's'", 19, 5);
    }

    @Test(groups = "disableOnOldParser")
    public void testSemanticsNegativeCases() {
        Assert.assertEquals(semanticsNegativeResult.getErrorCount(), 9);
        int i = 0;
        validateError(semanticsNegativeResult, i++, "incompatible types: expected 'int', found 'string'", 18, 13);
        validateError(semanticsNegativeResult, i++, "undefined field 'y' in object 'A'", 27, 6);
        validateError(semanticsNegativeResult, i++, "invalid operation: type 'A' does not support indexing", 28, 5);
        validateError(semanticsNegativeResult, i++, "invalid expr in compound assignment lhs", 38, 10);
        validateError(semanticsNegativeResult, i++, "invalid expr in compound assignment lhs", 39, 10);
        validateError(semanticsNegativeResult, i++, "invalid operation: type 'map<int>?' does not support member " +
                "access for assignment", 61, 5);
        validateError(semanticsNegativeResult, i++, "undefined field 'y' in record 'E'", 75, 5);
        validateError(semanticsNegativeResult, i++, "invalid operation: type 'map<int>?' does not support member " +
                "access for assignment", 78, 5);
        validateError(semanticsNegativeResult, i, "invalid operation: type 'E?' does not support field access",
                79, 5);
    }

    @Test(groups = "disableOnOldParser")
    public void testNegativeLvexpr() {
        CompileResult negative = BCompileUtil.compile("test-src/statements/assign/lvexpr_negative.bal");
        int i = 0;
        validateError(negative, i++, "invalid expr in assignment lhs", 26, 19);
        validateError(negative, i++, "invalid expr in assignment lhs", 27, 19);
        Assert.assertEquals(i, negative.getErrorCount());
    }

    @Test(dataProvider = "valueStoreFunctions")
    public void testValueStore(String function) {
        BValue[] returns = BRunUtil.invoke(result, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @DataProvider(name = "valueStoreFunctions")
    public Object[][] valueStoreFunctions() {
        return new Object[][] {
            { "testBasicValueStoreForVariable" },
            { "testValueStoreForMap" },
            { "testValueStoreForRecord" },
            { "testValueStoreForObject" },
            { "testBasicValueStoreForUnionVariable" },
            { "testValueStoreForMapUnion" },
            { "testValueStoreForRecordUnion" },
            { "testValueStoreForObjectUnion" },
            { "testFieldUpdateOfElementForMapWithNoFillerValue" },
            { "testFieldUpdateOfElementForRecordWithNoFillerValue" }
        };
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.map\\}InherentTypeViolation " +
                    "\\{\"message\":\"invalid value for record field 'i': expected value of type 'int', found " +
                    "'string'\"\\}.*")
    public void testInherentTypeViolatingUpdate1() {
        BRunUtil.invoke(result, "testInherentTypeViolatingUpdate1");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.object\\}InherentTypeViolation " +
                    "\\{\"message\":\"invalid value for object field 'i': expected value of type 'boolean', found " +
                    "'int'\"\\}.*")
    public void testInherentTypeViolatingUpdate2() {
        BRunUtil.invoke(result, "testInherentTypeViolatingUpdate2");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.array\\}InherentTypeViolation " +
                    "\\{\"message\":\"incompatible types: expected 'int', found 'string'\"\\}.*")
    public void testInherentTypeViolatingUpdate3() {
        BRunUtil.invoke(result, "testInherentTypeViolatingUpdate3");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.map\\}KeyNotFound \\{\"message\":\"invalid field "
                    + "access: field 'g' not found in record type 'DRec'\"\\}.*")
    public void testInvalidUpdateOnClosedRecord() {
        BRunUtil.invoke(result, "testInvalidUpdateOnClosedRecord");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.array\\}IndexOutOfRange \\{\"message\":\"array " +
                    "index out of range: index: 2, size: 2\"\\}.*")
    public void testInvalidUpdateOnClosedArray() {
        BRunUtil.invoke(result, "testInvalidUpdateOnClosedArray");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.map\\}InvalidUpdate \\{\"message\":\"Invalid " +
                    "update of record field: modification not allowed on readonly value\"\\}.*")
    public void testFrozenValueUpdate() {
        BRunUtil.invoke(result, "testFrozenValueUpdate");
    }

    @Test
    public void testListFillMember() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayFillSuccess1");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());

        returns = BRunUtil.invoke(result, "testArrayFillSuccess2");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.array\\}IllegalListInsertion " +
                    "\\{\"message\":\"array of length 2 cannot be expanded into array of length 4 without filler " +
                    "values\"\\}.*")
    public void testArrayFillFailure() {
        BRunUtil.invoke(result, "testArrayFillFailure");
    }

    @Test(dataProvider = "mappingFillingReadFunctions")
    public void testMappingFillingRead(String function) {
        BValue[] returns = BRunUtil.invoke(result, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @DataProvider(name = "mappingFillingReadFunctions")
    public Object[][] mappingFillingReadFunctions() {
        return new Object[][] {
            { "testFillingReadOnInitializedObjectField" },
            { "testFillingReadOnMapPositive" },
            { "testFillingReadOnRecordPositive" }
        };
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.map\\}KeyNotFound \\{\"message\":\"cannot " +
                    "find key 'one'\"\\}.*")
    public void testFillingReadOnMappingNegative() {
        BRunUtil.invoke(result, "testFillingReadOnMappingNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.map\\}KeyNotFound \\{\"message\":\"cannot find key" +
                    " 'l'\"\\}.*")
    public void testFillingReadOnRecordNegativeFieldAccessLvExpr() {
        BRunUtil.invoke(result, "testFillingReadOnRecordNegativeFieldAccessLvExpr");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*\\{ballerina/lang.map\\}KeyNotFound \\{\"message\":\"cannot find key" +
                    " 'l'\"\\}\n.*")
    public void testFillingReadOnRecordNegativeMemberAccessLvExpr() {
        BRunUtil.invoke(result, "testFillingReadOnRecordNegativeMemberAccessLvExpr");
    }
}
