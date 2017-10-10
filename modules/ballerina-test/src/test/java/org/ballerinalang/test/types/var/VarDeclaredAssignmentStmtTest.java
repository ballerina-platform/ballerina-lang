/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.types.var;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Var type assignment statement test.
 *
 * @since 0.88
 */
public class VarDeclaredAssignmentStmtTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BTestUtils.compile("test-src/types/var/var-type-assign-stmt.bal");
    }

    @Test(description = "Test int to var assignment.")
    public void testIntToVarAssignment() {
        BValue[] returns = BTestUtils.invoke(result, "testIntToVarAssignment",
                new BValue[]{});
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 81);
    }

    @Test(description = "Test multiple int to var assignment.")
    public void testMultipleIntToVarAssignment() {
        BValue[] returns = BTestUtils.invoke(result, "testMultipleIntToVarAssignment",
                new BValue[]{});
        Assert.assertEquals(returns.length, 4);

        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertNotNull(returns[2]);
        Assert.assertNotNull(returns[3]);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 200);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 300);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 400);
    }

    @Test(description = "Test multiple int var assignment with underscore.")
    public void testMultipleIntToVarAssignmentWithUnderscore() {
        BValue[] returns = BTestUtils.invoke(result, "testMultipleIntToVarAssignmentWithUnderscore",
                new BValue[]{});
        Assert.assertEquals(returns.length, 2);

        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 200);
    }

    @Test(description = "Test multiple int var assignment with underscore.")
    public void testMultipleIntToVarAssignmentWithUnderscoreCaseOne() {
        BValue[] returns = BTestUtils.invoke(result,
                "testMultipleIntToVarAssignmentWithUnderscoreOrderCaseOne",
                new BValue[]{});
        Assert.assertEquals(returns.length, 2);

        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 300);
    }

    @Test(description = "Test multiple int var assignment with underscore.")
    public void testMultipleIntToVarAssignmentWithUnderscoreCaseTwo() {
        BValue[] returns = BTestUtils.invoke(result,
                "testMultipleIntToVarAssignmentWithUnderscoreOrderCaseTwo",
                new BValue[]{});
        Assert.assertEquals(returns.length, 2);

        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 400);
    }

    @Test(description = "Test string to var assignment.")
    public void testStringToVarAssignment() {
        BValue[] returns = BTestUtils.invoke(result, "testStringToVarAssignment",
                new BValue[]{});
        Assert.assertEquals(((BString) returns[0]).stringValue(), "name");
    }

    @Test(description = "Test multiple string to var assignment.")
    public void testMultipleStringToVarAssignment() {
        BValue[] returns = BTestUtils.invoke(result, "testMultipleStringToVarAssignment",
                new BValue[]{});
        Assert.assertEquals(returns.length, 4);

        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertNotNull(returns[2]);
        Assert.assertNotNull(returns[3]);

        Assert.assertEquals(((BString) returns[0]).stringValue(), "name_1");
        Assert.assertEquals(((BString) returns[1]).stringValue(), "name_2");
        Assert.assertEquals(((BString) returns[2]).stringValue(), "name_3");
        Assert.assertEquals(((BString) returns[3]).stringValue(), "name_4");
    }

    @Test(description = "Test var with at least non declared ref in LHS expr.")
    public void testVarDeclarationWithAtLeaseOneNonDeclaredSymbol() {
        BValue[] returns = BTestUtils.invoke(result, "testVarDeclarationWithAtLeaseOneNonDeclaredSymbol",
                new BValue[]{});
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
        Assert.assertNull(returns[1]);
    }

    @Test(description = "Test boolean to var assignment.")
    public void testBooleanToVarAssignment() {
        BValue[] returns = BTestUtils.invoke(result, "testBooleanToVarAssignment",
                new BValue[]{});
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true);
    }

    @Test(description = "Test var in variable def.")
    public void testVarTypeInVariableDefStatement() {
        //var type is not not allowed in variable def statements
        CompileResult res = BTestUtils.compile("test-src/types/var/var-type-variable-def-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 1);
        BTestUtils.validateError(res, 0, "mismatched input ';'. expecting {'.', ',', '[', '=', '@'}", 2, 12);
    }

    @Test(description = "Test var in global variable def.")
    public void testVarTypeInGlobalVariableDefStatement() {
        //var type is not not allowed in global variable def statements
        CompileResult res = BTestUtils.compile("test-src/types/var/global-variable-def-var-type-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 2);
        BTestUtils.validateError(res, 0, "extraneous input 'var'", 1, 1);
        BTestUtils.validateError(res, 1, "mismatched input '='. expecting {'[', Identifier}", 1, 15);
    }

    @Test(enabled = false)
    public void testVarTypeInServiceLevelVariableDefStatement() {
        //var type is not not allowed in service level variable def statements
        CompileResult res = BTestUtils.compile("test-src/types/var/service-level-variable-def-with-var-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 1);
    }

    @Test(enabled = false)
    public void testVarDeclarationWithStructFieldAssignmentLHSExpr() {
        CompileResult res = BTestUtils.compile("test-src/types/var/var-invalid-usage-struct-field-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 1);
    }

    @Test
    public void testVarDeclarationWithDuplicateVariableRefs() {
        CompileResult res = BTestUtils.compile("test-src/types/var/var-duplicate-variable-ref-lhs-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 1);
        BTestUtils.validateError(res, 0, "redeclared symbol 'age'", 2, 14);
    }

    @Test
    public void testVarDeclarationWithArrayInit() {
        CompileResult res = BTestUtils.compile("test-src/types/var/var-declaration-with-array-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 1);
        BTestUtils.validateError(res, 0, "array literal not allowed here", 2, 17);
    }

    @Test
    public void testVarDeclarationWithAllDeclaredSymbols() {
        CompileResult res = BTestUtils.compile("test-src/types/var/var-declared-symbols-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 1);
        BTestUtils.validateError(res, 0, "no new variables on left side", 5, 5);
    }

    @Test
    public void testVarDeclarationWithAllIgnoredSymbols() {
        CompileResult res = BTestUtils.compile("test-src/types/var/var-all-ignored-symbols-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 1);
        BTestUtils.validateError(res, 0, "no new variables on left side", 3, 5);
    }

    @Test(enabled = false, description = "Test incompatible json to struct with errors.")
    public void testIncompatibleJsonToStructWithErrors() {
        BValue[] returns = BTestUtils.invoke(result, "testIncompatibleJsonToStructWithErrors",
                new BValue[]{});
        Assert.assertNull(returns[0]);
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "cannot convert 'json' to type 'Person': error while mapping" +
                " 'parent': incompatible types: expected 'json-object', found 'string'");
    }

    @Test(enabled = false, description = "Test incompatible json to struct with errors.")
    public void testJsonToStructWithErrors() {
        BValue[] returns = BTestUtils.invoke(result, "testJsonToStructWithErrors",
                new BValue[]{});
        Assert.assertNull(returns[0]);
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "cannot convert 'json' to type 'PersonA': error while mapping 'age': " +
                "incompatible types: expected 'int', found 'string' in json");
    }

    @Test(enabled = false, description = "Test compatible struct with force casting.")
    public void testCompatibleStructForceCasting() {
        BValue[] returns = BTestUtils.invoke(result, "testCompatibleStructForceCasting", new BValue[]{});
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct structC = (BStruct) returns[0];

        Assert.assertEquals(structC.getStringField(0), "updated-x-valueof-a");

        Assert.assertEquals(structC.getIntField(0), 4);

        // check whether error is null
        Assert.assertNull(returns[1]);
    }

    @Test(enabled = false, description = "Test incompatible struct with force casting.")
    public void testInCompatibleStructForceCasting() {
        BValue[] returns = BTestUtils.invoke(result, "testInCompatibleStructForceCasting", new BValue[]{});

        // check whether struct is null
        Assert.assertNull(returns[0]);

        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "'B' cannot be cast to 'A'");

        String sourceType = error.getStringField(1);
        Assert.assertEquals(sourceType, "B");

        String targetType = error.getStringField(2);
        Assert.assertEquals(targetType, "A");
    }

    @Test(description = "Test any to string with errors.")
    public void testAnyToStringWithErrors() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyToStringWithErrors", new BValue[]{});

        // check whether string is empty
        Assert.assertEquals(returns[0].stringValue(), "");

        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "'int' cannot be cast to 'string'");
    }

    @Test(description = "Test any null to string with errors.")
    public void testAnyNullToStringWithErrors() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyNullToStringWithErrors", new BValue[]{});

        // check whether string is empty
        Assert.assertEquals(returns[0].stringValue(), "");

        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "'null' cannot be cast to 'string'");
    }

    @Test(description = "Test any to boolean with errors.")
    public void testAnyToBooleanWithErrors() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyToBooleanWithErrors", new BValue[]{});

        // check whether string is empty
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), false);

        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "'int' cannot be cast to 'boolean'");
    }

    @Test(description = "Test any null to boolean with errors.")
    public void testAnyNullToBooleanWithErrors() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyNullToBooleanWithErrors", new BValue[]{});

        // check whether string is empty
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), false);

        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "'null' cannot be cast to 'boolean'");
    }

    @Test(description = "Test any to int with errors.")
    public void testAnyToIntWithErrors() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyToIntWithErrors", new BValue[]{});

        // check whether int is zero
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);

        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "'string' cannot be cast to 'int'");
    }

    @Test(description = "Test any null to int with errors.")
    public void testAnyNullToIntWithErrors() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyNullToIntWithErrors", new BValue[]{});

        // check whether int is zero
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);

        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "'null' cannot be cast to 'int'");
    }

    @Test(description = "Test any to float with errors.")
    public void testAnyToFloatWithErrors() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyToFloatWithErrors", new BValue[]{});

        // check whether float is zero
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 0.0);

        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "'string' cannot be cast to 'float'");
    }

    @Test(description = "Test any null to float with errors.")
    public void testAnyNullToFloatWithErrors() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyNullToFloatWithErrors", new BValue[]{});

        // check whether float is zero
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 0.0);

        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "'null' cannot be cast to 'float'");
    }

    @Test(description = "Test any to map with errors.")
    public void testAnyToMapWithErrors() {
        BValue[] returns = BTestUtils.invoke(result, "testAnyToMapWithErrors", new BValue[]{});

        // check whether map is null
        Assert.assertNull(returns[0]);

        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "'string' cannot be cast to 'map'");
    }

}
