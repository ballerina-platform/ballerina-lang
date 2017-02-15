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
package org.wso2.ballerina.lang.values;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.exception.SemanticException;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BNull;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

/**
 * This test class will test the behaviour of null values.
 */
public class BNullValueTest {
    private BallerinaFile bFile;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        bFile = ParserUtils.parseBalFile("lang/values/null-value.bal");
    }

    @Test(description = "Test null value assignment")
    public void testNullValueAssignment1() {
        BValue[] returns = Functions.invoke(bFile, "nullAssignment1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BNull.class);
        BNull nullValue = (BNull) returns[0];
        Assert.assertNotNull(nullValue);
    }


    @Test(description = "Test null value assignment from function invocation")
    public void testNullValueAssignment2() {
        BValue[] returns = Functions.invoke(bFile, "nullAssignment4");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BNull.class);
        BNull nullValue = (BNull) returns[0];
        Assert.assertNotNull(nullValue);
    }

    @Test(description = "Test null value assignment at the variable definition")
    public void testNullValueAssignmentInDefinition1() {
        BValue[] returns = Functions.invoke(bFile, "nullAssignment2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BNull.class);
        BNull nullValue = (BNull) returns[0];
        Assert.assertNotNull(nullValue);
    }

    @Test(description = "Test null value assignment at the variable definition from function invocation")
    public void testNullValueAssignmentInDefinition2() {
        BValue[] returns = Functions.invoke(bFile, "nullAssignment3");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BNull.class);
        BNull nullValue = (BNull) returns[0];
        Assert.assertNotNull(nullValue);
    }

    @Test(description = "Test null equel expression")
    public void testNullCheckExpression() {
        BValue[] returns = Functions.invoke(bFile, "nullCheck");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        BBoolean isNull = (BBoolean) returns[0];
        Assert.assertTrue(isNull.booleanValue(), "Null check failed");
    }

    @Test(description = "Test not null  expression")
    public void testNotNullCheckExpression() {
        BValue[] returns = Functions.invoke(bFile, "notNullCheck");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        BBoolean isNull = (BBoolean) returns[0];
        Assert.assertFalse(isNull.booleanValue(), "Not null check failed");
    }

    @Test(description = "Test function signature parsing null")
    public void testFunctionInvocationParsingNull1() {
        BValue[] returns = Functions.invoke(bFile, "callFunctionWithNULLValue1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BNull.class);
        BNull nullValue = (BNull) returns[0];
        Assert.assertNotNull(nullValue);
    }

    @Test(description = "Test function signature parsing null")
    public void testFunctionInvocationParsingNull2() {
        BValue[] returns = Functions.invoke(bFile, "callFunctionWithNULLValue2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BNull.class);
        BNull nullValue = (BNull) returns[0];
        Assert.assertNotNull(nullValue);
    }

    @Test(description = "Test null return statement")
    public void testNullReturnStatement() {
        BValue[] returns = Functions.invoke(bFile, "getNull");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BNull.class);
        BNull nullValue = (BNull) returns[0];
        Assert.assertNotNull(nullValue);
    }

    @Test(description = "Test null value assignment for struct")
    public void testNullValueAssignmentForStruct() {
        BValue[] returns = Functions.invoke(bFile, "setStructNull");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BNull.class);
        BNull nullValue = (BNull) returns[0];
        Assert.assertNotNull(nullValue);
    }

    @Test(description = "Test accessing a element of null assigned struct",
          expectedExceptions = BallerinaException.class,
          expectedExceptionsMessageRegExp = "null-value.bal:71: variable 'p' is null")
    public void testAccessingAElementOfNullStruct() {
        Functions.invoke(bFile, "accessElementOfNullStruct");
    }

    @Test(description = "Test setting a value to a element of null assigned struct",
          expectedExceptions = BallerinaException.class,
          expectedExceptionsMessageRegExp = "null-value.bal:78: variable 'p' is null")
    public void testSettingAElementOfNullStruct() {
        Functions.invoke(bFile, "settingElementOfNullStruct");
    }

    @Test(description = "Test accessing a value of a struct element of null assigned struct",
          expectedExceptions = BallerinaException.class,
          expectedExceptionsMessageRegExp = "null-value.bal:88: field 'family' is null")
    public void testAccessingAStructElementOfNullStruct() {
        Functions.invoke(bFile, "accessingStructElementOfNullStruct");
    }

    @Test(description = "Test setting a value to a element of struct of null assigned struct",
          expectedExceptions = BallerinaException.class,
          expectedExceptionsMessageRegExp = "null-value.bal:97: field 'family' is null")
    public void testSettingAStructElementOfNullStruct() {
        Functions.invoke(bFile, "settingStructElementOfNullStruct");
    }

    @Test(description = "Test string value define as null",
          expectedExceptions = SemanticException.class,
          expectedExceptionsMessageRegExp = "define-string-null-value.bal:3: string cannot be null")
    public void testDefineStringNull() {
        ParserUtils.parseBalFile("lang/values/define-string-null-value.bal");
    }

    @Test(description = "Test assign null to string",
          expectedExceptions = SemanticException.class,
          expectedExceptionsMessageRegExp = "assign-string-to-null-value.bal:4: string cannot be null")
    public void testAssignStringToNull() {
        ParserUtils.parseBalFile("lang/values/assign-string-to-null-value.bal");
    }

    @Test(description = "Test null return for string ",
          expectedExceptions = SemanticException.class,
          expectedExceptionsMessageRegExp = "return-string-null.bal:2: string cannot be null")
    public void testNullReturnAsString() {
        ParserUtils.parseBalFile("lang/values/return-string-null.bal");
    }

    @Test(description = "Test string null check",
          expectedExceptions = SemanticException.class,
          expectedExceptionsMessageRegExp = "string-left-null-check.bal:3: string cannot be null")
    public void testStringNullCheckLeft() {
        ParserUtils.parseBalFile("lang/values/string-left-null-check.bal");
    }

    @Test(description = "Test string null check",
          expectedExceptions = SemanticException.class,
          expectedExceptionsMessageRegExp = "string-right-null-check.bal:3: string cannot be null")
    public void testStringNullRight() {
        ParserUtils.parseBalFile("lang/values/string-right-null-check.bal");
    }

    @Test(description = "Test not supported operator with null",
          expectedExceptions = SemanticException.class,
          expectedExceptionsMessageRegExp = "grater-than-null.bal:3: null not allowed with the operator '>'")
    public void testBinaryOperatorValidityWithNull() {
        ParserUtils.parseBalFile("lang/values/grater-than-null.bal");
    }

    @Test(description = "Test accessing element of null array",
          expectedExceptions = BallerinaException.class,
          expectedExceptionsMessageRegExp = "null-value.bal:102: variable 'nullArray' is null")
    public void testAccessingElementInNullArray() {
        BValue[] returns = Functions.invoke(bFile, "accessElementInNullArray");
    }

    @Test(description = "Test accessing element of null map",
          expectedExceptions = BallerinaException.class,
          expectedExceptionsMessageRegExp = "null-value.bal:107: variable 'nullMap' is null")
    public void testAccessingElementInNullMap() {
        BValue[] returns = Functions.invoke(bFile, "accessElementInNullMap");
    }
}
