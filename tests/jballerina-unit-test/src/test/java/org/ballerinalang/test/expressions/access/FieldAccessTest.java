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

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Test cases for field access.
 *
 * @since 1.0
 */
public class FieldAccessTest {

    private CompileResult result;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/access/field_access.bal");
        negativeResult = BCompileUtil.compile("test-src/expressions/access/field_access_negative.bal");
    }

    @Test
    public void testNegativeCases() {
        int i = 0;
        validateError(negativeResult, i++, "field access cannot be used to access an optional field of a type " +
                "that includes nil, use optional field access or member access", 32, 9);
        validateError(negativeResult, i++, "invalid field access: 'salary' is not a required field in record " +
                "'Employee', use member access to access a field that may have been specified as a rest field", 33, 9);
        validateError(negativeResult, i++, "field access can only be used to access required fields or optional " +
                "fields of non-nilable types, field 'salary' is undeclared in record(s) 'Employee'", 39, 9);
        validateError(negativeResult, i++, "incompatible types: expected 'string', found '(int|string)'", 56, 17);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found '(int|string)'", 57, 15);
        validateError(negativeResult, i++, "invalid operation: type 'map<string>' does not support field access",
                62, 16);
        validateError(negativeResult, i++, "invalid operation: type '(map<string>|EmployeeTwo)' does not support " +
                "field access", 68, 16);
        validateError(negativeResult, i++, "invalid operation: type 'EmployeeTwo?' does not support field access",
                74, 17);
        validateError(negativeResult, i++, "invalid operation: type '(map<string>|map<int>)' does not support " +
                "field access", 80, 20);
        validateError(negativeResult, i++, "incompatible types: expected 'json', found '(json|error)'", 85, 14);
        validateError(negativeResult, i++, "incompatible types: expected 'json', found '(json|error)'", 90, 14);
        validateError(negativeResult, i++, "invalid operation: type '(json|error)' does not support field access", 96,
                22);
        validateError(negativeResult, i++, "incompatible types: expected '(map<json>|error)', " +
                "found '(map<json>|json|error)'", 102, 26);
        validateError(negativeResult, i++, "incompatible types: expected 'map<json>', found '(json|map<json>|error)'",
                106, 20);
        validateError(negativeResult, i++, "invalid operation: type 'Foo?' does not support field access", 131, 14);
        validateError(negativeResult, i++, "function invocation on type 'Foo' is not supported", 134, 19);
        validateError(negativeResult, i++, "invalid operation: type 'Foo[]' does not support field access", 138, 9);

        validateError(negativeResult, i++, "undeclared field 'a' in record 'R1'", 155, 13);
        validateError(negativeResult, i++, "field access cannot be used to access an optional field of a type that " +
                "includes nil, use optional field access or member access", 164, 13);
        validateError(negativeResult, i++, "field access cannot be used to access an optional field of a type that " +
                "includes nil, use optional field access or member access", 173, 13);
        validateError(negativeResult, i++, "invalid field access: 'y' is not a required field in record 'R5', use " +
                "member access to access a field that may have been specified as a rest field", 182, 17);
        validateError(negativeResult, i++, "invalid field access: 'y' is not a required field in record 'R6', use " +
                "member access to access a field that may have been specified as a rest field", 191, 17);
        validateError(negativeResult, i++, "invalid field access: 'y' is not a required field in record 'R7', use " +
                "member access to access a field that may have been specified as a rest field", 202, 17);
        validateError(negativeResult, i++, "field access can only be used to access required fields or optional " +
                "fields of non-nilable types, type of field 'a' includes nil in record(s) 'SA', 'UA', and 'VA'",
                247, 17);
        validateError(negativeResult, i++, "field access can only be used to access required fields or optional " +
                "fields of non-nilable types, type of field 'b' includes nil in record(s) 'SA', and 'UA'",
                248, 17);
        validateError(negativeResult, i++, "field access can only be used to access required fields or optional " +
                "fields of non-nilable types, type of field 'c' includes nil in record(s) 'TA'",
                249, 17);

        validateError(negativeResult, i++, "field access can only be used to access required fields or optional " +
                "fields of non-nilable types, field 'x' is undeclared in record(s) 'SA', 'UA', and 'VA'", 251, 17);
        validateError(negativeResult, i++, "field access can only be used to access required fields or optional " +
                "fields of non-nilable types, field 'y' is undeclared in record(s) 'SA', and 'UA'", 252, 17);
        validateError(negativeResult, i++, "field access can only be used to access required fields or optional " +
                "fields of non-nilable types, field 'z' is undeclared in record(s) 'TA'", 253, 17);

        validateError(negativeResult, i++, "field access can only be used to access required fields or optional " +
                "fields of non-nilable types, field 'x' is undeclared in record(s) 'RB', 'TB', and 'UB'",
                293, 17);
        validateError(negativeResult, i++, "field access can only be used to access required fields or optional " +
                "fields of non-nilable types, field 'y' is undeclared in record(s) 'QB', and 'TB'",
                294, 17);
        validateError(negativeResult, i++, "field access can only be used to access required fields or optional " +
                "fields of non-nilable types, field 'z' is undeclared in record(s) 'RB', 'SB', and 'VB'",
                295, 17);

        validateError(negativeResult, i++, "field access can only be used to access required fields or optional " +
                "fields of non-nilable types, type of field 'i' includes nil in record(s) 'BarOne'",
                308, 14);

        validateError(negativeResult, i++, "field access can only be used to access required fields or optional " +
                "fields of non-nilable types, field 'x' is undeclared in record(s) 'CD' and type includes nil " +
                "in record(s) 'BC'", 329, 17);
        validateError(negativeResult, i++, "field access can only be used to access required fields or optional " +
                "fields of non-nilable types, field 'y' is undeclared in record(s) 'BC' and type includes nil in " +
                "record(s) 'AB'", 330, 17);
        validateError(negativeResult, i++, "field access can only be used to access required fields or optional " +
                "fields of non-nilable types, field 'z' is undeclared in record(s) 'CD' and type includes nil in " +
                "record(s) 'BC'", 331, 17);
        validateError(negativeResult, i++, "undefined field 'id' in union '(AB|BC)'", 337, 5);

        validateError(negativeResult, i++, "'remote' methods of an object cannot be accessed using the field access " +
                "expression", 369, 20);
        validateError(negativeResult, i++, "'remote' methods of an object cannot be accessed using the field access " +
                "expression", 371, 11);
        validateError(negativeResult, i++, "'remote' methods of an object cannot be accessed using the field access " +
                "expression", 373, 26);
        validateError(negativeResult, i++, "'remote' methods of an object cannot be accessed using the field access " +
                "expression", 375, 15);
        validateError(negativeResult, i++, "'remote' methods of an object cannot be accessed using the field access " +
                "expression", 377, 15);
        validateError(negativeResult, i++, "invalid operation: type 'map<xml>' does not support field access"
                , 382, 19);
        validateError(negativeResult, i++, "invalid operation: type 'map<xml>' does not support field access"
                , 387, 19);
        validateError(negativeResult, i++, "invalid operation: type 'map<xml>' does not support field access"
                , 393, 19);
        validateError(negativeResult, i++, "invalid operation: type 'map<(xml|json)>' does not support field access"
                , 399, 24);
        validateError(negativeResult, i++, "invalid operation: type 'map<xml>' does not support " +
                        "optional field access", 404, 13);
        validateError(negativeResult, i++, "invalid operation: type 'map<xml>' does not support " +
                "optional field access", 409, 14);
        validateError(negativeResult, i++, "invalid operation: type 'map<xml>' does not support " +
                "optional field access", 414, 13);

        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @Test(dataProvider = "recordFieldAccessFunctions")
    public void testRecordFieldAccess(String function) {
        Object returns = BRunUtil.invoke(result, function);
        Assert.assertTrue((Boolean) returns);
    }

    @DataProvider(name = "recordFieldAccessFunctions")
    public Object[][] recordFieldAccessFunctions() {
        return new Object[][] {
            { "testRecordFieldAccess1" },
            { "testRecordFieldAccess2" },
            { "testRecordFieldAccess3" }
        };
    }

    @Test
    public void testJsonFieldAccessPositive() {
        Object returns = BRunUtil.invoke(result, "testJsonFieldAccessPositive");
        BArray array = ((BArray) returns);
        Assert.assertEquals(array.size(), 2);
        for (int i = 0; i < 2; i++) {
            Assert.assertEquals(array.getBoolean(i), true);
        }
    }

    @Test
    public void testJsonFieldAccessNegative() {
        Object returns = BRunUtil.invoke(result, "testJsonFieldAccessNegative");
        BArray array = ((BArray) returns);
        Assert.assertEquals(array.size(), 5);
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(array.getBoolean(i), true);
        }
    }

    @Test
    public void testMapJsonFieldAccessPositive() {
        Object returns = BRunUtil.invoke(result, "testMapJsonFieldAccessPositive");
        BArray array = ((BArray) returns);
        Assert.assertEquals(array.size(), 2);
        for (int i = 0; i < 2; i++) {
            Assert.assertEquals(array.getBoolean(i), true);
        }
    }

    @Test
    public void testMapJsonFieldAccessNegative() {
        Object returns = BRunUtil.invoke(result, "testMapJsonFieldAccessNegative");
        BArray array = ((BArray) returns);
        Assert.assertEquals(array.size(), 5);
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(array.getBoolean(i), true);
        }
    }

    @Test(dataProvider = "nonNilLiftingJsonFieldAccessFunctions")
    public void testNonNilLiftingJsonFieldAccess(String function) {
        Object returns = BRunUtil.invoke(result, function);
        Assert.assertTrue((Boolean) returns);
    }

    @DataProvider(name = "nonNilLiftingJsonFieldAccessFunctions")
    public Object[][] nonNilLiftingJsonFieldAccessFunctions() {
        return new Object[][] {
            { "testNonNilLiftingJsonAccess1" },
            { "testNonNilLiftingJsonAccess2" },
            { "testNonNilLiftingJsonAccess3" }
        };
    }

    @Test
    public void testLaxUnionFieldAccessPositive() {
        Object returns = BRunUtil.invoke(result, "testLaxUnionFieldAccessPositive");
        Assert.assertTrue((Boolean) returns);
    }

    @Test(dataProvider = "laxUnionFieldAccessNegativeFunctions")
    public void testLaxUnionFieldAccessNegative(String function) {
        Object returns = BRunUtil.invoke(result, function);
        Assert.assertTrue((Boolean) returns);
    }

    @DataProvider(name = "laxUnionFieldAccessNegativeFunctions")
    public Object[][] laxUnionFieldAccessNegativeFunctions() {
        return new Object[][] {
            { "testLaxUnionFieldAccessNegative1" },
            { "testLaxUnionFieldAccessNegative2" },
            { "testLaxUnionFieldAccessNegative3" }
        };
    }

    @Test
    public void testLaxFieldAccessWithCheckOnVariableDefinedAtModuleLevel() {
        BRunUtil.invoke(result, "testLaxFieldAccessWithCheckOnVariableDefinedAtModuleLevel");
    }

    @Test
    public void negativeTestLaxFieldAccessWithCheckOnVariableDefinedAtModuleLevel() {
        BRunUtil.invoke(result, "negativeTestLaxFieldAccessWithCheckOnVariableDefinedAtModuleLevel");
    }

    @Test(dataProvider = "mapJsonFieldAccessTypePositiveFunctions")
    public void testMapJsonFieldAccessTypePositive(String function) {
        Object returns = BRunUtil.invoke(result, function);
        Assert.assertTrue((Boolean) returns);
    }

    @DataProvider(name = "mapJsonFieldAccessTypePositiveFunctions")
    public Object[][] mapJsonFieldAccessTypePositiveFunctions() {
        return new Object[][] {
                { "testMapJsonFieldAccessTypePositive1" },
                { "testMapJsonFieldAccessTypePositive2" }
        };
    }

    @Test
    public void testFieldAccessOnInvocation() {
        Object returns = BRunUtil.invoke(result, "testFieldAccessOnInvocation");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testJsonFieldAccessOnInvocation() {
        Object returns = BRunUtil.invoke(result, "testJsonFieldAccessOnInvocation");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testFieldAccessOnMapConstruct() {
        Object returns = BRunUtil.invoke(result, "testFieldAccessOnMapConstruct");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testAccessOptionalFieldWithFieldAccess1() {
        Object returns = BRunUtil.invoke(result, "testAccessOptionalFieldWithFieldAccess1");
    }

    @Test
    public void testAccessOptionalFieldWithFieldAccess2() {
        Object returns = BRunUtil.invoke(result, "testAccessOptionalFieldWithFieldAccess2");
    }

    @Test
    public void testAccessingMethodOnUnionObjectType() {
        BRunUtil.invoke(result, "testAccessingMethodOnUnionObjectType");
    }

    @Test
    public void testValidXMLmapFieldAccess() {
        BRunUtil.invoke(result, "testValidXMLmapFieldAccess");
    }

    @Test(dataProvider = "fieldAccessOnJsonTypedRecordFields")
    public void testFieldAccessOnJsonTypedRecordFields(String function) {
        BRunUtil.invoke(result, function);
    }

    @Test
    public void testFieldAccessOnIntSubtypeRecordFields() {
        Object returns = BRunUtil.invoke(result, "testFieldAccessOnIntSubtype");
        Assert.assertEquals(returns, 1L);
    }

    @DataProvider(name = "fieldAccessOnJsonTypedRecordFields")
    public Object[][] fieldAccessOnJsonTypedRecordFields() {
        return new Object[][] {
                { "testFieldAccessOnJsonTypedRecordFields" },
                { "testFieldAccessOnJsonTypedRecordFieldsResultingInError" },
                { "testFieldAccessOnJsonTypedRecordFieldsResultingInErrorWithCheckExpr" },
                { "testOptionalFieldAccessOnOptionalJsonTypedRecordFields" },
                { "testOptionalFieldAccessOnOptionalJsonTypedRecordFieldsResultingInError" }
        };
    }

    @AfterClass
    public void tearDown() {
        result = null;
        negativeResult = null;
    }
}
