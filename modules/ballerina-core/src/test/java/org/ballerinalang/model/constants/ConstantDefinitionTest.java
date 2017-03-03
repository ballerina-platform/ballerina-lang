/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.ballerinalang.model.constants;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BDouble;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class ConstantDefinitionTest {
    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("lang/constants/constants-definitions.bal");
    }

    @Test
    public void testIntA() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testIntA");
        Assert.assertEquals(returns.length, 1);
        BValue returnVal = returns[0];
        Assert.assertEquals(returnVal.getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returnVal).intValue(), 5);
    }

    @Test
    public void testStringB() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testStringB");
        Assert.assertEquals(returns.length, 1);
        BValue returnVal = returns[0];
        Assert.assertEquals(returnVal.getClass(), BString.class);
        Assert.assertEquals(returnVal.stringValue(), "AB");
    }

    @Test
    public void testSum() {
        int expected = 5 + 4 + 5 * 4;
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testSum");
        Assert.assertEquals(returns.length, 1);
        BValue returnVal = returns[0];
        Assert.assertEquals(returnVal.getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returnVal).intValue(), expected);
    }

    @Test
    public void testResult() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testResult");
        Assert.assertEquals(returns.length, 1);
        BValue returnVal = returns[0];
        Assert.assertEquals(returnVal.getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returnVal).booleanValue(), false);
    }

    @Test
    public void testImplCast() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testImplCast");
        Assert.assertEquals(returns.length, 1);
        BValue returnVal = returns[0];
        Assert.assertEquals(returnVal.getClass(), BString.class);
        Assert.assertEquals(returnVal.stringValue(), "10");
    }

    @Test
    public void testExpCast() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testExpCast");
        Assert.assertEquals(returns.length, 1);
        BValue returnVal = returns[0];
        Assert.assertEquals(returnVal.getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returnVal).intValue(), 10);
    }

    @Test
    public void testDouble() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testDouble");
        Assert.assertEquals(returns.length, 1);
        BValue returnVal = returns[0];
        Assert.assertEquals(returnVal.getClass(), BDouble.class);
        Assert.assertEquals(((BDouble) returnVal).doubleValue(), 6.923456);
    }

    @Test(expectedExceptions = {SemanticException.class},
          expectedExceptionsMessageRegExp = "redeclared-constant.bal:2: redeclared symbol 'a'")
    public void testRedeclaredConstant() {
        BTestUtils.parseBalFile("lang/constants/redeclared-constant.bal");
    }

    @Test(expectedExceptions = {SemanticException.class},
          expectedExceptionsMessageRegExp = "template-expression-constant.bal:1: "
                  + "xml/json template expression is not allowed here")
    public void testTemplateExprConstant() {
        BTestUtils.parseBalFile("lang/constants/template-expression-constant.bal");
    }

    @Test(expectedExceptions = {SemanticException.class},
          expectedExceptionsMessageRegExp = "invalid-type-constant.bal:1: incompatible types: "
                  + "expected 'int', found 'string'")
    public void testInvalidTypeConstant() {
        BTestUtils.parseBalFile("lang/constants/invalid-type-constant.bal");
    }

    //todo add more test with referring to previously defined constant once the const init function is added

}
