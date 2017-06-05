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

package org.ballerinalang.model.identifierliteral;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.ParserException;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Identifier literal test cases.
 */
public class IdentifierLiteralTest {
    private ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("lang/identifierliteral/identifier-literal-success.bal");
    }

    @Test(description = "Test defining global variable with Identifier Literal and refer within a function")
    public void testGlobalVarWithIdentifierLiteral() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "getGlobalVarWithIL");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        String actual = ((BString) returns[0]).stringValue();
        Assert.assertEquals(actual, "this is a IL with global var");
    }

    @Test(description = "Test defining constant with Identifier Literal and refer within a function")
    public void testConstantWithIdentifierLiteral() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "getConstWithIL");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        double actual = ((BFloat) returns[0]).floatValue();
        Assert.assertEquals(actual, 88.90);
    }

    @Test(description = "Test defining local variables with Identifier Literal")
    public void testIdentifierLiteralsAsLocalVariables() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "defineAndGetIL");
        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BFloat.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        String actualString = ((BString) returns[0]).stringValue();
        Assert.assertEquals(actualString, "this is a IL with global var");
        double actualFloat = ((BFloat) returns[1]).floatValue();
        Assert.assertEquals(actualFloat, 88.90);
        long actualInt = ((BInteger) returns[2]).intValue();
        Assert.assertEquals(actualInt, 99934);
    }

    @Test(description = "Test defining struct variables with Identifier Literal")
    public void testIdentifierLiteralInStructs() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "useILWithinStruct");
        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        String actualFirstName = ((BString) returns[0]).stringValue();
        Assert.assertEquals(actualFirstName, "Tom");
        String actualLastName = ((BString) returns[1]).stringValue();
        Assert.assertEquals(actualLastName, "hank");
        long actualInt = ((BInteger) returns[2]).intValue();
        Assert.assertEquals(actualInt, 50);
    }

    @Test(description = "Test defining struct with identifier literal")
    public void testStructVarWithIdentifierLiteral() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "useILInStructVar");
        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        String actualFirstName = ((BString) returns[0]).stringValue();
        Assert.assertEquals(actualFirstName, "Harry");
        String actualLastName = ((BString) returns[1]).stringValue();
        Assert.assertEquals(actualLastName, "potter");
        long actualInt = ((BInteger) returns[2]).intValue();
        Assert.assertEquals(actualInt, 25);
    }

    @Test(description = "Test defining reference type with identifier literal and initialize later")
    public void testUsingIdentifierLiteralAsReferenceType() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "useILAsrefType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BJSON.class);
        String actualFirstName = ((BJSON) returns[0]).stringValue();
        Assert.assertEquals(actualFirstName, "{\"name\":\"James\",\"age\":30}");
    }

    @Test(description = "Test using identifier literals in arrays and array indexes")
    public void testUsingIdentifierLiteralAsArrayIndex() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "useILAsArrayIndex");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        double actual = ((BFloat) returns[0]).floatValue();
        Assert.assertEquals(actual, 8834.834);
    }

    @Test(description = "Test using identifier literals in function parameters")
    public void testUsingIdentifierLiteralAsFunctionParams() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "passILValuesToFunction");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BInteger.class);
        String actualString = ((BString) returns[0]).stringValue();
        Assert.assertEquals(actualString, "Bill Kary");
        long actualInt = ((BInteger) returns[1]).intValue();
        Assert.assertEquals(actualInt, 40);
    }

    @Test(description = "Test character range in identifier literal")
    public void testCharacterRangeInIdentifierLiteral() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testCharInIL");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        String actualString = ((BString) returns[0]).stringValue();
        Assert.assertEquals(actualString, "sample value");
    }

    @Test(description = "Test function name with identifier literal")
    public void testFunctionNameWithInIdentifierLiteral() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testFunctionNameWithIL");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        String actualString = ((BString) returns[0]).stringValue();
        Assert.assertEquals(actualString, "sample test");
    }

    @Test(description = "Test connector name with identifier literal")
    public void testConnectorWithIdentifierLiteral() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testConnectorNameWithIL");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[0]).stringValue(), "this is a sample");
    }

    @Test(description = "Test connector action with identifier literal")
    public void testConnectorActionWithIdentifierLiteral() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testConnectorActionWithIL");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[0]).stringValue(), "sample string");
    }

    @Test(description = "Test defining local variables with Identifier Literal")
    public void testIdentifierLiteralInStructName() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "useILInStructName");
        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        String actualFirstName = ((BString) returns[0]).stringValue();
        Assert.assertEquals(actualFirstName, "Tom");
        String actualLastName = ((BString) returns[1]).stringValue();
        Assert.assertEquals(actualLastName, "hank");
        long actualInt = ((BInteger) returns[2]).intValue();
        Assert.assertEquals(actualInt, 50);
    }

    @Test(description = "Test unicode with identifier literal")
    public void testUnicodeWithIntegerLiteral() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testUnicodeInIL");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[0]).stringValue(), "සිංහල වාක්‍යක්");
    }

    //Error scenarios
    @Test(description = "Test error message when trying to access undefined global var with identifier literal",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "identifier-literal-undefined-variable.bal:5: undefined " +
                    "symbol 'global v \" ar'")
    public void testTryToAccessUndefinedGlobalVarWithIdentifierLiteral() {
        BTestUtils.getProgramFile("lang/identifierliteral/identifier-literal-undefined-variable.bal");
    }

    @Test(description = "Test wrong character in identifier literal",
            expectedExceptions = {ParserException.class},
            expectedExceptionsMessageRegExp = "identifier-literal-wrong-character.bal:3:21: missing ';' before 'var'")
    public void testIdentifierLiteralWithWrongCharacter() {
        BTestUtils.getProgramFile("lang/identifierliteral/identifier-literal-wrong-character.bal");
    }
}
