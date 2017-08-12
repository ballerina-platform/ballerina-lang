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
package org.ballerinalang.var;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.ParserException;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Var type assignment statement test.
 *
 * @since 0.88
 */
public class VarDeclaredAssignmentStmtTest {

    private ProgramFile bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.getProgramFile("lang/var/var-type-assign-stmt.bal");
    }

    @Test(description = "Test int to var assignment.")
    public void testIntToVarAssignment() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testIntToVarAssignment",
                new BValue[]{});
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 81);
    }

    @Test(description = "Test multiple int to var assignment.")
    public void testMultipleIntToVarAssignment() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testMultipleIntToVarAssignment",
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
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testMultipleIntToVarAssignmentWithUnderscore",
                new BValue[]{});
        Assert.assertEquals(returns.length, 2);

        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 200);
    }

    @Test(description = "Test multiple int var assignment with underscore.")
    public void testMultipleIntToVarAssignmentWithUnderscoreCaseOne() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram,
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
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram,
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
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testStringToVarAssignment",
                new BValue[]{});
        Assert.assertEquals(((BString) returns[0]).stringValue(), "name");
    }

    @Test(description = "Test multiple string to var assignment.")
    public void testMultipleStringToVarAssignment() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testMultipleStringToVarAssignment",
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
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testVarDeclarationWithAtLeaseOneNonDeclaredSymbol",
                new BValue[]{});
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
        Assert.assertNull(returns[1]);
    }

    @Test(description = "Test boolean to var assignment.")
    public void testBooleanToVarAssignment() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testBooleanToVarAssignment",
                new BValue[]{});
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true);
    }

    @Test(description = "Test var in variable def.", expectedExceptions = {ParserException.class})
    public void testVarTypeInVariableDefStatement() {
        //var type is not not allowed in variable def statements
        BTestUtils.getProgramFile("lang/var/var-type-variable-def-negative.bal");
    }

    @Test(description = "Test var in global variable def.", expectedExceptions = {ParserException.class})
    public void testVarTypeInGlobalVariableDefStatement() {
        //var type is not not allowed in global variable def statements
        BTestUtils.getProgramFile("lang/var/global-variable-def-var-type-negative.bal");
    }

    @Test(description = "Test var in service level var def.", expectedExceptions = {ParserException.class})
    public void testVarTypeInServiceLevelVariableDefStatement() {
        //var type is not not allowed in service level variable def statements
        BTestUtils.getProgramFile("lang/var/service-level-variable-def-with-var-type-negative.bal");
    }

    @Test(expectedExceptions = {SemanticException.class }, expectedExceptionsMessageRegExp = ".*invalid usage of var")
    public void testVarDeclarationWithStructFieldAssignmentLHSExpr() {
        //all the expression of LHS of var declaration should be variable references
        BTestUtils.getProgramFile("lang/var/var-invalid-usage-struct-field-access.bal");
    }

    @Test(expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = ".*'age' is repeated on the left side of assignment")
    public void testVarDeclarationWithDuplicateVariableRefs() {
        //all the expression of LHS of var declaration should be unique variable references
        BTestUtils.getProgramFile("lang/var/var-duplicate-variable-ref-lhs-expr.bal");
    }

    @Test(expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = ".*invalid usage of var")
    public void testVarDeclarationWithArrayInit() {
        //var declarations cannot have array init, json init over RHS expr
        BTestUtils.getProgramFile("lang/var/var-declaration-with-array-init.bal");
    }

    @Test(expectedExceptions = {SemanticException.class },
          expectedExceptionsMessageRegExp = "var-declared-symbols.bal:7: no new variables on left side")
    public void testVarDeclarationWithAllDeclaredSymbols() {
        //var declarations should at least one non declared var ref symbol
        BTestUtils.getProgramFile("lang/var/var-declared-symbols.bal");
    }

    @Test(expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "var-all-ignored-symbols.bal:3: no new variables on left side")
    public void testVarDeclarationWithAllIgnoredSymbols() {
        //var declarations should at least one non declared var ref symbol
        BTestUtils.getProgramFile("lang/var/var-all-ignored-symbols.bal");
    }

    @Test(description = "Test incompatible json to struct with errors.")
    public void testIncompatibleJsonToStructWithErrors() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testIncompatibleJsonToStructWithErrors",
                new BValue[]{});
        Assert.assertNull(returns[0]);
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "cannot convert 'json' to type 'Person': error while mapping" +
                " 'parent': incompatible types: expected 'json-object', found 'string'");
    }

    @Test(description = "Test incompatible json to struct with errors.")
    public void testJsonToStructWithErrors() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testJsonToStructWithErrors",
                new BValue[]{});
        Assert.assertNull(returns[0]);
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "cannot convert 'json' to type 'PersonA': error while mapping 'age': " +
                "incompatible types: expected 'int', found 'string' in json");
    }

    @Test(description = "Test compatible struct with force casting.")
    public void testCompatibleStructForceCasting() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testCompatibleStructForceCasting", new BValue[]{});
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct structC = (BStruct) returns[0];

        Assert.assertEquals(structC.getStringField(0), "updated-x-valueof-a");

        Assert.assertEquals(structC.getIntField(0), 4);

        // check whether error is null
        Assert.assertNull(returns[1]);
    }

    @Test(description = "Test incompatible struct with force casting.")
    public void testInCompatibleStructForceCasting() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testInCompatibleStructForceCasting", new BValue[]{});

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
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testAnyToStringWithErrors", new BValue[]{});

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
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testAnyNullToStringWithErrors", new BValue[]{});

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
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testAnyToBooleanWithErrors", new BValue[]{});

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
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testAnyNullToBooleanWithErrors", new BValue[]{});

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
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testAnyToIntWithErrors", new BValue[]{});

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
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testAnyNullToIntWithErrors", new BValue[]{});

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
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testAnyToFloatWithErrors", new BValue[]{});

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
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testAnyNullToFloatWithErrors", new BValue[]{});

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
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testAnyToMapWithErrors", new BValue[]{});

        // check whether map is null
        Assert.assertNull(returns[0]);

        // check the error
        Assert.assertTrue(returns[1] instanceof BStruct);
        BStruct error = (BStruct) returns[1];
        String errorMsg = error.getStringField(0);
        Assert.assertEquals(errorMsg, "'string' cannot be cast to 'map'");
    }

}
