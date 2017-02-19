/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model.values;

import org.ballerinalang.BLangProgramLoader;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This test class will test the behaviour of null values.
 */
public class BNullValueTest {
    Path programPath = Paths.get(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
    private BLangProgram bLangProgram;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        bLangProgram = new BLangProgramLoader().loadLibrary(programPath,
                Paths.get("lang/values/null-value.bal"));
    }

    @Test(description = "Test null value assignment")
    public void testNullValueAssignment1() {
        BValue[] returns =  BLangFunctions.invoke(bLangProgram, "nullAssignment1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BNull.class);
        BNull nullValue = (BNull) returns[0];
        Assert.assertNotNull(nullValue);
    }


    @Test(description = "Test null value assignment from function invocation")
    public void testNullValueAssignment2() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram,  "nullAssignment4");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BNull.class);
        BNull nullValue = (BNull) returns[0];
        Assert.assertNotNull(nullValue);
    }

    @Test(description = "Test null value assignment at the variable definition")
    public void testNullValueAssignmentInDefinition1() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram,  "nullAssignment2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BNull.class);
        BNull nullValue = (BNull) returns[0];
        Assert.assertNotNull(nullValue);
    }

    @Test(description = "Test null value assignment at the variable definition from function invocation")
    public void testNullValueAssignmentInDefinition2() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram,  "nullAssignment3");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BNull.class);
        BNull nullValue = (BNull) returns[0];
        Assert.assertNotNull(nullValue);
    }

    @Test(description = "Test null equel expression")
    public void testNullCheckExpression() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram,  "nullCheck");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        BBoolean isNull = (BBoolean) returns[0];
        Assert.assertTrue(isNull.booleanValue(), "Null check failed");
    }

    @Test(description = "Test not null  expression")
    public void testNotNullCheckExpression() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram,  "notNullCheck");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        BBoolean isNull = (BBoolean) returns[0];
        Assert.assertFalse(isNull.booleanValue(), "Not null check failed");
    }

    @Test(description = "Test function signature parsing null")
    public void testFunctionInvocationParsingNull1() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram,  "callFunctionWithNULLValue1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BNull.class);
        BNull nullValue = (BNull) returns[0];
        Assert.assertNotNull(nullValue);
    }

    @Test(description = "Test function signature parsing null")
    public void testFunctionInvocationParsingNull2() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram,  "callFunctionWithNULLValue2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BNull.class);
        BNull nullValue = (BNull) returns[0];
        Assert.assertNotNull(nullValue);
    }

    @Test(description = "Test null return statement")
    public void testNullReturnStatement() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram,  "getNull");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BNull.class);
        BNull nullValue = (BNull) returns[0];
        Assert.assertNotNull(nullValue);
    }

    @Test(description = "Test null value assignment for struct")
    public void testNullValueAssignmentForStruct() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram,  "setStructNull");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BNull.class);
        BNull nullValue = (BNull) returns[0];
        Assert.assertNotNull(nullValue);
    }

    @Test(description = "Test accessing a element of null assigned struct",
          expectedExceptions = BallerinaException.class,
          expectedExceptionsMessageRegExp = "null-value.bal:71: variable 'p' is null")
    public void testAccessingAElementOfNullStruct() {
        BLangFunctions.invoke(bLangProgram,  "accessElementOfNullStruct");
    }

    @Test(description = "Test setting a value to a element of null assigned struct",
          expectedExceptions = BallerinaException.class,
          expectedExceptionsMessageRegExp = "null-value.bal:78: variable 'p' is null")
    public void testSettingAElementOfNullStruct() {
        BLangFunctions.invoke(bLangProgram,  "settingElementOfNullStruct");
    }

    @Test(description = "Test accessing a value of a struct element of null assigned struct",
          expectedExceptions = BallerinaException.class,
          expectedExceptionsMessageRegExp = "null-value.bal:88: field 'family' is null")
    public void testAccessingAStructElementOfNullStruct() {
        BLangFunctions.invoke(bLangProgram,  "accessingStructElementOfNullStruct");
    }

    @Test(description = "Test setting a value to a element of struct of null assigned struct",
          expectedExceptions = BallerinaException.class,
          expectedExceptionsMessageRegExp = "null-value.bal:97: field 'family' is null")
    public void testSettingAStructElementOfNullStruct() {
        BLangFunctions.invoke(bLangProgram,  "settingStructElementOfNullStruct");
    }

    @Test(description = "Test string value define as null",
          expectedExceptions = SemanticException.class,
          expectedExceptionsMessageRegExp = "define-string-null-value.bal:3: string cannot be null")
    public void testDefineStringNull() {
        new BLangProgramLoader().loadLibrary(programPath, Paths.get("lang/values/define-string-null-value.bal"));
    }

    @Test(description = "Test assign null to string",
          expectedExceptions = SemanticException.class,
          expectedExceptionsMessageRegExp = "assign-string-to-null-value.bal:4: string cannot be null")
    public void testAssignStringToNull() {
        new BLangProgramLoader().loadLibrary(programPath, Paths.get("lang/values/assign-string-to-null-value.bal"));
    }

    @Test(description = "Test null return for string ",
          expectedExceptions = SemanticException.class,
          expectedExceptionsMessageRegExp = "return-string-null.bal:2: string cannot be null")
    public void testNullReturnAsString() {
        new BLangProgramLoader().loadLibrary(programPath, Paths.get("lang/values/return-string-null.bal"));
    }

    @Test(description = "Test string null check",
          expectedExceptions = SemanticException.class,
          expectedExceptionsMessageRegExp = "string-left-null-check.bal:3: string cannot be null")
    public void testStringNullCheckLeft() {
        new BLangProgramLoader().loadLibrary(programPath, Paths.get("lang/values/string-left-null-check.bal"));
    }

    @Test(description = "Test string null check",
          expectedExceptions = SemanticException.class,
          expectedExceptionsMessageRegExp = "string-right-null-check.bal:3: string cannot be null")
    public void testStringNullRight() {
        new BLangProgramLoader().loadLibrary(programPath, Paths.get("lang/values/string-right-null-check.bal"));
    }

    @Test(description = "Test not supported operator with null",
          expectedExceptions = SemanticException.class,
          expectedExceptionsMessageRegExp = "grater-than-null.bal:3: null not allowed here with the operator >")
    public void testBinaryOperatorValidityWithNull() {
        new BLangProgramLoader().loadLibrary(programPath, Paths.get("lang/values/grater-than-null.bal"));
    }

    @Test(description = "Test accessing element of null array",
          expectedExceptions = BallerinaException.class,
          expectedExceptionsMessageRegExp = "null-value.bal:102: variable 'nullArray' is null")
    public void testAccessingElementInNullArray() {
        BLangFunctions.invoke(bLangProgram, "accessElementInNullArray");
    }

    @Test(description = "Test setting element of null array",
          expectedExceptions = BallerinaException.class,
          expectedExceptionsMessageRegExp = "null-value.bal:112: variable 'nullArray' is null")
    public void testSetElementInNullArray() {
        BLangFunctions.invoke(bLangProgram, "setElementInNullArray");
    }

    @Test(description = "Test accessing element of null map",
          expectedExceptions = BallerinaException.class,
          expectedExceptionsMessageRegExp = "null-value.bal:107: variable 'nullMap' is null")
    public void testAccessingElementInNullMap() {
        BLangFunctions.invoke(bLangProgram, "accessElementInNullMap");
    }

    @Test(description = "Test setting element of null map",
          expectedExceptions = BallerinaException.class,
          expectedExceptionsMessageRegExp = "null-value.bal:117: variable 'nullMap' is null")
    public void testSetElementInNullMap() {
        BLangFunctions.invoke(bLangProgram, "setElementInNullMap");
    }

    @Test(description = "Test null as a if condition",
          expectedExceptions = BallerinaException.class,
          expectedExceptionsMessageRegExp = "null-as-if-condition.bal:2: null not allowed here")
    public void testNullAsIfCondition() {
        new BLangProgramLoader().loadLibrary(programPath, Paths.get("lang/values/null-as-if-condition.bal"));
    }

    @Test(description = "Test null as a else-if condition",
          expectedExceptions = BallerinaException.class,
          expectedExceptionsMessageRegExp = "null-as-elseif-condition.bal:3: null not allowed here")
    public void testNullAsElseIfCondition() {
        new BLangProgramLoader().loadLibrary(programPath, Paths.get("lang/values//null-as-elseif-condition.bal"));
    }

    @Test(description = "Test null in unary expression",
          expectedExceptions = BallerinaException.class,
          expectedExceptionsMessageRegExp = "null-in-unary-expr.bal:2: null not allowed here")
    public void testNullInUnaryExpression() {
        new BLangProgramLoader().loadLibrary(programPath, Paths.get("lang/values/null-in-unary-expr.bal"));
    }

    @Test(description = "Test null as while condition",
          expectedExceptions = BallerinaException.class,
          expectedExceptionsMessageRegExp = "null-as-while-condition.bal:2: null not allowed here")
    public void testNullAsWhileCondition() {
        new BLangProgramLoader().loadLibrary(programPath, Paths.get("lang/values/null-as-while-condition.bal"));
    }
}
