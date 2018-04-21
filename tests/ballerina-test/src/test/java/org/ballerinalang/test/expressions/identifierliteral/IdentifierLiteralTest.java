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

package org.ballerinalang.test.expressions.identifierliteral;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Identifier literal test cases.
 */
public class IdentifierLiteralTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/identifierliteral/identifier-literal-success.bal");
    }

    @Test(description = "Test defining global variable with Identifier Literal and refer within a function")
    public void testGlobalVarWithIdentifierLiteral() {
        BValue[] returns = BRunUtil.invoke(result, "getGlobalVarWithIL");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        String actual = ((BString) returns[0]).stringValue();
        Assert.assertEquals(actual, "this is a IL with global var");
    }

    @Test(description = "Test defining constant with Identifier Literal and refer within a function")
    public void testConstantWithIdentifierLiteral() {
        BValue[] returns = BRunUtil.invoke(result, "getConstWithIL");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        double actual = ((BFloat) returns[0]).floatValue();
        Assert.assertEquals(actual, 88.90);
    }

    @Test(description = "Test defining local variables with Identifier Literal")
    public void testIdentifierLiteralsAsLocalVariables() {
        BValue[] returns = BRunUtil.invoke(result, "defineAndGetIL");
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
        BValue[] returns = BRunUtil.invoke(result, "useILWithinStruct");
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
        BValue[] returns = BRunUtil.invoke(result, "useILInStructVar");
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
        BValue[] returns = BRunUtil.invoke(result, "useILAsrefType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BJSON.class);
        String actualFirstName = ((BJSON) returns[0]).stringValue();
        Assert.assertEquals(actualFirstName, "{\"name\":\"James\",\"age\":30}");
    }

    @Test(description = "Test using identifier literals in arrays and array indexes")
    public void testUsingIdentifierLiteralAsArrayIndex() {
        BValue[] returns = BRunUtil.invoke(result, "useILAsArrayIndex");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        double actual = ((BFloat) returns[0]).floatValue();
        Assert.assertEquals(actual, 8834.834);
    }

    @Test(description = "Test using identifier literals in function parameters")
    public void testUsingIdentifierLiteralAsFunctionParams() {
        BValue[] returns = BRunUtil.invoke(result, "passILValuesToFunction");
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
        BValue[] returns = BRunUtil.invoke(result, "testCharInIL");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        String actualString = ((BString) returns[0]).stringValue();
        Assert.assertEquals(actualString, "sample value");
    }

    @Test(description = "Test function name with identifier literal")
    public void testFunctionNameWithInIdentifierLiteral() {
        BValue[] returns = BRunUtil.invoke(result, "testFunctionNameWithIL");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        String actualString = ((BString) returns[0]).stringValue();
        Assert.assertEquals(actualString, "sample test");
    }

    @Test(description = "Test connector name with identifier literal", enabled = false)
    public void testConnectorWithIdentifierLiteral() {
        BValue[] returns = BRunUtil.invoke(result, "testConnectorNameWithIL");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[0]).stringValue(), "this is a sample");
    }

    @Test(description = "Test connector action with identifier literal", enabled = false)
    public void testConnectorActionWithIdentifierLiteral() {
        BValue[] returns = BRunUtil.invoke(result, "testConnectorActionWithIL");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[0]).stringValue(), "sample string");
    }

    @Test(description = "Test defining local variables with Identifier Literal")
    public void testIdentifierLiteralInStructName() {
        BValue[] returns = BRunUtil.invoke(result, "useILInStructName");
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        String actualFirstName = ((BString) returns[0]).stringValue();
        Assert.assertEquals(actualFirstName, "Tom");

        String actualLastName = ((BString) returns[1]).stringValue();
        Assert.assertEquals(actualLastName, "hank");

        long actualInt = ((BInteger) returns[2]).intValue();
        Assert.assertEquals(actualInt, 50);

        Assert.assertEquals(returns[3].stringValue(), "Tom");
    }

    @Test(description = "Test unicode with identifier literal")
    public void testUnicodeWithIntegerLiteral() {
        BValue[] returns = BRunUtil.invoke(result, "testUnicodeInIL");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[0]).stringValue(), "සිංහල වාක්‍යක්");
    }

    //Error scenarios
    @Test(description = "Test error message when trying to access undefined global var with identifier literal")
    public void testTryToAccessUndefinedGlobalVarWithIdentifierLiteral() {
        CompileResult resultNeg = BCompileUtil.compile("test-src/expressions/identifierliteral" +
                "/identifier-literal-undefined-variable-negative.bal");
        Assert.assertEquals(resultNeg.getErrorCount(), 1);
        BAssertUtil.validateError(resultNeg, 0, "undefined symbol 'global v \" ar'", 5, 12);
    }

    @Test(description = "Test wrong character in identifier literal")
    public void testIdentifierLiteralWithWrongCharacter() {
        CompileResult resultNeg = BCompileUtil.compile("test-src/expressions/identifierliteral" +
                "/identifier-literal-wrong-character-negative.bal");
        Assert.assertEquals(resultNeg.getErrorCount(), 3);
        BAssertUtil.validateError(resultNeg, 0, "missing token ';' before 'var'", 3, 23);
        BAssertUtil.validateError(resultNeg, 1, "token recognition error at: '\";\\n}\\n\\n\\n'", 4, 25);
        BAssertUtil.validateError(resultNeg, 2, "missing token ';' before 'ar'", 4, 23);
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
        Assert.assertSame(returns[0].getClass(), BJSON.class);
        Assert.assertEquals(returns[0].stringValue(), "I am an integer");
    }
}
