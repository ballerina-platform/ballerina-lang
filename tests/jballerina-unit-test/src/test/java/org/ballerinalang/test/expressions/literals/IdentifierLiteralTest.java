/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.expressions.literals;

import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Identifier literal test cases.
 */
@Test(groups = {"disableOnOldParser"})
public class IdentifierLiteralTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/literals/identifierliteral/identifier-literal-success.bal");
    }

    @Test(description = "Test defining final variables with Identifier Literal and refer within a function")
    public void testILInFinalVariables() {
        BRunUtil.invoke(result, "testFinalVariableIL");
    }

    @Test(description = "Test defining global variable with Identifier Literal and refer within a function")
    public void testILInGlobalVariables() {
        BRunUtil.invoke(result, "testGlobalVariableIL");
    }

    @Test(description = "Test defining local variable with Identifier Literal and refer within a function")
    public void testILInLocalVariables() {
        BRunUtil.invoke(result, "testLocalVariableIL");
    }

    @Test(description = "Test defining local variables with Identifier Literal and get them as return parameters")
    public void testIdentifierLiteralsAsLocalVariables() {
        BValue[] returns = BRunUtil.invoke(result, "defineAndGetIL");
        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BFloat.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        String actualString = returns[0].stringValue();
        Assert.assertEquals(actualString, "IL with global var");
        double actualFloat = ((BFloat) returns[1]).floatValue();
        Assert.assertEquals(actualFloat, 77.80);
        long actualInt = ((BInteger) returns[2]).intValue();
        Assert.assertEquals(actualInt, 99934);
    }

    @Test(description = "Test defining struct type fields with Identifier Literal")
    public void testIdentifierLiteralInStructs() {
        BRunUtil.invoke(result, "useILWithinStruct");
    }

    @Test(description = "Test defining struct type variales with Identifier Literal")
    public void testStructVarWithIdentifierLiteral() {
        BRunUtil.invoke(result, "useILInStructVar");
    }

    @Test(description = "Test defining reference type with identifier literal and initialize later")
    public void testUsingIdentifierLiteralAsReferenceType() {
        BValue[] returns = BRunUtil.invoke(result, "useILAsrefType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BMap.class);
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":\"James\", \"age\":30}");
    }

    @Test(description = "Test using identifier literals in arrays and array indexes")
    public void testUsingIdentifierLiteralAsArrayIndex() {
        BRunUtil.invoke(result, "useILAsArrayIndex");
    }

    @Test(description = "Test using identifier literals in function parameters")
    public void testUsingIdentifierLiteralAsFunctionParams() {
        BRunUtil.invoke(result, "passILValuesToFunction");
    }

    @Test(description = "Test character range in identifier literal")
    public void testCharacterRangeInIdentifierLiteral() {
        BValue[] returns = BRunUtil.invoke(result, "testCharInIL");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        String actualString = returns[0].stringValue();
        Assert.assertEquals(actualString, "sample value");
    }

    @Test(description = "Test function name with identifier literal")
    public void testFunctionNameWithInIdentifierLiteral() {
        BRunUtil.invoke(result, "testFunctionNameWithIL");
    }

    @Test(description = "Test connector name with identifier literal")
    public void testConnectorWithIdentifierLiteral() {
        BRunUtil.invoke(result, "testConnectorNameWithIL");
    }

    @Test(description = "Test connector action with identifier literal")
    public void testConnectorActionWithIdentifierLiteral() {
        BRunUtil.invoke(result, "testConnectorActionWithIL");
    }

    @Test(description = "Test defining local variables with Identifier Literal")
    public void testIdentifierLiteralInStructName() {
        BRunUtil.invoke(result, "useILInStructName");
    }

    @Test(description = "Test unicode with identifier literal")
    public void testUnicodeWithIntegerLiteral() {
        BValue[] returns = BRunUtil.invoke(result, "testUnicodeInIL");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "සිංහල වාක්‍යක්");
    }

    //Error scenarios
    @Test(description = "Test error message when trying to access undefined global var with identifier literal")
    public void testTryToAccessUndefinedGlobalVarWithIdentifierLiteral() {
        CompileResult resultNeg = BCompileUtil.compile("test-src/expressions/literals/identifierliteral" +
                "/identifier-literal-undefined-variable-negative.bal");
        Assert.assertEquals(resultNeg.getErrorCount(), 1);
        BAssertUtil.validateError(resultNeg, 0, "undefined symbol 'global\\ v\\ \\\"\\ ar'", 5, 12);
    }

    @Test(description = "Test wrong character in identifier literal")
    public void testIdentifierLiteralWithWrongCharacter() {
        CompileResult resultNeg = BCompileUtil.compile("test-src/expressions/literals/identifierliteral" +
                "/identifier-literal-wrong-character-negative.bal");
        Assert.assertEquals(resultNeg.getErrorCount(), 6);
        BAssertUtil.validateError(resultNeg, 0, "missing semicolon token", 3, 24);
        BAssertUtil.validateError(resultNeg, 1, "incompatible types: expected ' = ', found 'string'", 3, 32);
        BAssertUtil.validateError(resultNeg, 2, "missing double quote", 3, 32);
        BAssertUtil.validateError(resultNeg, 3, "missing equal token", 3, 32);
        BAssertUtil.validateError(resultNeg, 4, "missing semicolon token", 4, 1);
        BAssertUtil.validateError(resultNeg, 5, "undefined symbol 'global\\ v\\ ar'", 4, 12);
    }

    @Test(description = "Tests quoted identifier literal containing invalid special characters")
    public void testInvalidILSpecialChar() {
        CompileResult resultNeg =
                BCompileUtil.compile("test-src/expressions/literals/identifierliteral/invalid_IL_special_char.bal");
        Assert.assertEquals(resultNeg.getErrorCount(), 12);
        BAssertUtil.validateError(resultNeg, 0, "no new variables on left side", 18, 5);
        BAssertUtil.validateError(resultNeg, 1, "invalid intersection type '$missingNode$_0 & *%_var = ': no " +
                "intersection", 18, 14);
        BAssertUtil.validateError(resultNeg, 2, "missing semicolon token", 18, 14);
        BAssertUtil.validateError(resultNeg, 3, "missing type desc", 18, 14);
        BAssertUtil.validateError(resultNeg, 4, "missing equal token", 18, 33);
        BAssertUtil.validateError(resultNeg, 5, "undefined symbol 'value'", 18, 33);
        BAssertUtil.validateError(resultNeg, 6, "missing double quote", 18, 38);
        BAssertUtil.validateError(resultNeg, 7, "missing plus token", 18, 38);
        BAssertUtil.validateError(resultNeg, 8, "missing semicolon token", 19, 1);
        BAssertUtil.validateError(resultNeg, 9, "underscore is not allowed here", 19, 12);
        BAssertUtil.validateError(resultNeg, 10, "missing double quote", 19, 15);
        BAssertUtil.validateError(resultNeg, 11, "missing semicolon token", 20, 1);
    }

    @Test(description = "Tests quoted identifier literal containing invalid escape characters")
    public void testInvalidILEscapeChar() {
        CompileResult resultNeg =
                BCompileUtil.compile("test-src/expressions/literals/identifierliteral/invalid_IL_escape_char.bal");
        Assert.assertEquals(resultNeg.getErrorCount(), 12);
        BAssertUtil.validateError(resultNeg, 0, "invalid escape sequence '\\B'", 18, 12);
        BAssertUtil.validateError(resultNeg, 1, "invalid escape sequence '\\a'", 18, 12);
        BAssertUtil.validateError(resultNeg, 2, "invalid escape sequence '\\c'", 18, 12);
        BAssertUtil.validateError(resultNeg, 3, "invalid escape sequence '\\x'", 18, 12);
        BAssertUtil.validateError(resultNeg, 4, "invalid escape sequence '\\y'", 18, 12);
        BAssertUtil.validateError(resultNeg, 5, "invalid escape sequence '\\z'", 18, 12);
        BAssertUtil.validateError(resultNeg, 6, "invalid escape sequence '\\B'", 19, 12);
        BAssertUtil.validateError(resultNeg, 7, "invalid escape sequence '\\a'", 19, 12);
        BAssertUtil.validateError(resultNeg, 8, "invalid escape sequence '\\c'", 19, 12);
        BAssertUtil.validateError(resultNeg, 9, "invalid escape sequence '\\x'", 19, 12);
        BAssertUtil.validateError(resultNeg, 10, "invalid escape sequence '\\y'", 19, 12);
        BAssertUtil.validateError(resultNeg, 11, "invalid escape sequence '\\z'", 19, 12);
    }

    @Test(description = "Tests quoted identifier literal containing invalid unicode characters")
    public void testInvalidILUnicodeChar() {
        CompileResult resultNeg =
                BCompileUtil.compile("test-src/expressions/literals/identifierliteral/invalid_IL_unicode_char.bal");
        Assert.assertEquals(resultNeg.getErrorCount(), 4);
        BAssertUtil.validateError(resultNeg, 0, "invalid token 'whiteSpace'", 18, 28);
        BAssertUtil.validateError(resultNeg, 1, "undefined symbol ''", 19, 12);
        BAssertUtil.validateError(resultNeg, 2, "missing plus token", 19, 17);
        BAssertUtil.validateError(resultNeg, 3, "undefined symbol 'whiteSpace'", 19, 17);
    }

    @Test
    public void testAcessILWithoutPipe() {
        BValue[] returns = BRunUtil.invoke(result, "testAcessILWithoutPipe");
        Assert.assertEquals(returns.length, 2);

        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "hello");

        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertEquals(returns[1].stringValue(), "hello");
    }

    @Test
    public void testAcessJSONFielAsIL() {
        BValue[] returns = BRunUtil.invoke(result, "testAcessJSONFielAsIL");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "I am an integer");
    }

    @Test(description = "Test quoted identifier literals in workers")
    public void testILInWorkers() {
        BRunUtil.invoke(result, "useILAsWorkerName");
    }

    @Test(description = "Test quoted identifier literals in workers")
    public void testILInWorkerInteraction() {
        BRunUtil.invoke(result, "testWorkerInteractionWithIL");
    }

    @Test(description = "Test struct type member access with literal string")
    public void testMemberAccessWithIL() {
        BValue[] returns = BRunUtil.invoke(result, "testMemberAccessWithIL");
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(returns[0].stringValue(), "Jack");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 50);
        Assert.assertEquals(returns[2].stringValue(), "John");
        Assert.assertEquals(returns[3].stringValue(), "25");
    }

    @Test(description = "Test struct type to string returning expected value for IL")
    public void testStructTypeToStringMethod() {
        BRunUtil.invoke(result, "testToStringWithIL");
    }

    @Test(description = "Test ToString method returning expected value for struct type field name with IL")
    public void testStructFieldToStringMethod() {
        BRunUtil.invoke(result, "testToStringStructFieldsWithIL");
    }

    @Test(description = "Test IL with immutable type")
    public void testImmutableTypeIL() {
        BRunUtil.invoke(result, "testImmutableTypeIL");
    }

}
