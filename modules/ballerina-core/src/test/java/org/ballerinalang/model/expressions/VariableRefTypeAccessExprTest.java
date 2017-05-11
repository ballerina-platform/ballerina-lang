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

package org.ballerinalang.model.expressions;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Variable reference Type access expression test.
 *
 * @since 0.8.0
 */
public class VariableRefTypeAccessExprTest {

    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("lang/expressions/variable-ref-type-access-expr.bal");
        System.setProperty(Constants.SYS_PROP_ENABLE_NONBLOCKING, "true");
    }

    @Test(description = "Test reference type access expression trivial equality Positive case")
    public void testRefTypeAccessExprTrivialEqualityCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "refTypeAccessTestTrivialEqualityPositiveCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression trivial equality Negative case")
    public void testRefTypeAccessExprTrivialEqualityNegativeCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "refTypeAccessTestTrivialEqualityNegativeCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression trivial not equality case")
    public void testRefTypeAccessExprTrivialNotEqualityCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "refTypeAccessTestTrivialNotEqualityCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);
    }


    @Test(description = "Test reference type access expression Any type Negative case")
    public void testRefTypeAccessExprAnyTypeNegativeCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "refTypeAccessTestAnyTypeNegativeCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression Any type Positive case")
    public void testRefTypeAccessExprAnyTypePositiveCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "refTypeAccessTestAnyTypePositiveCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression Map access case")
    public void testRefTypeAccessExprMapAccessCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "refTypeAccessTestMapAccessCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }


    @Test(description = "Test reference type access expression Array access case")
    public void testRefTypeAccessExprArrayAccessCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "refTypeAccessTestArrayAccessCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression Array access case")
    public void testRefTypeAccessExprStructAccessCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "refTypeAccessTestStructAccessCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }

}
