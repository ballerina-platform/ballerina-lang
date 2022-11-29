/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.types.constant;

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Constant expression test cases.
 *
 * @since JBallerina 1.0.0
 */
public class ConstantExpressionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {

        compileResult = BCompileUtil.compile("test-src/types/constant/constant-expression.bal");
    }

    @Test
    public void constExpressionNegative() {

        CompileResult compileResult1 = BCompileUtil.compile("test-src/types/constant/constant-expression-negative.bal");
        int i = 0;
        BAssertUtil.validateError(compileResult1, i++, "type is required for constants with expressions", 19, 13);
        BAssertUtil.validateError(compileResult1, i++, "const expressions are not yet supported here", 21, 32);
        BAssertUtil.validateError(compileResult1, i++, "const expressions are not yet supported here", 21, 44);
        BAssertUtil.validateError(compileResult1, i++, "invalid constant expression, reason '/ by zero'", 23, 18);
        BAssertUtil.validateError(compileResult1, i++, "invalid constant expression, reason '/ by zero'", 25, 18);
        BAssertUtil.validateError(compileResult1, i++, "expression is not a constant expression", 27, 18);
        BAssertUtil.validateError(compileResult1, i++, "missing identifier", 27, 18);
        BAssertUtil.validateError(compileResult1, i++, "operator '+' not defined for 'string'", 29, 20);
        BAssertUtil.validateError(compileResult1, i++, "operator '!' not defined for 'int'", 31, 21);
        BAssertUtil.validateError(compileResult1, i++, "operator '~' not defined for 'boolean'", 33, 22);
        BAssertUtil.validateError(compileResult1, i++, "incompatible types: expected 'int', found 'boolean'", 35, 22);
        BAssertUtil.validateError(compileResult1, i++, "operator '~' not defined for 'boolean'", 37, 22);
        BAssertUtil.validateError(compileResult1, i++, "illegal cyclic reference '[A, B, C]'", 40, 15);
        BAssertUtil.validateError(compileResult1, i++, "illegal cyclic reference '[E, F]'", 45, 17);
        BAssertUtil.validateError(compileResult1, i++, "'9223372036854775808' is out of range for 'int'", 47, 21);
        Assert.assertEquals(compileResult1.getErrorCount(), i);
    }

    @Test
    public void getConstAdditions() {

        BArray returns = (BArray) BRunUtil.invoke(compileResult, "getConstAdditions");
        Assert.assertEquals(returns.size(), 4);
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertNotNull(returns.get(2));
        Assert.assertNotNull(returns.get(3));
        Assert.assertEquals(returns.get(0).toString(), "15");
        Assert.assertEquals(returns.get(1).toString(), "15.0");
        Assert.assertEquals(returns.get(2).toString(), "15.5");
        Assert.assertEquals(returns.get(3).toString(), "helloworld");
    }

    @Test
    public void getConstSubtracts() {

        BArray returns = (BArray) BRunUtil.invoke(compileResult, "getConstSubtracts");
        Assert.assertEquals(returns.size(), 3);
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertNotNull(returns.get(2));
        Assert.assertEquals(returns.get(0).toString(), "5");
        Assert.assertEquals(returns.get(1).toString(), "5.5");
        Assert.assertEquals(returns.get(2).toString(), "5.5");
    }

    @Test
    public void getConstMultiplications() {

        BArray returns = (BArray) BRunUtil.invoke(compileResult, "getConstMultiplications");
        Assert.assertEquals(returns.size(), 3);
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertNotNull(returns.get(2));
        Assert.assertEquals(returns.get(0).toString(), "50");
        Assert.assertEquals(returns.get(1).toString(), "52.5");
        Assert.assertEquals(returns.get(2).toString(), "52.5");
    }

    @Test
    public void getConstDivisions() {

        BArray returns = (BArray) BRunUtil.invoke(compileResult, "getConstDivisions");
        Assert.assertEquals(returns.size(), 5);
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertNotNull(returns.get(2));
        Assert.assertNotNull(returns.get(3));
        Assert.assertNotNull(returns.get(4));
        Assert.assertEquals(returns.get(0).toString(), "2");
        Assert.assertEquals(returns.get(1).toString(), "2.1");
        Assert.assertEquals(returns.get(2).toString(), "NaN");
        Assert.assertEquals(returns.get(3).toString(), "Infinity");
        Assert.assertEquals(returns.get(4).toString(), "2.1");
    }

    @Test
    public void getConstGrouping() {

        BArray returns = (BArray) BRunUtil.invoke(compileResult, "getConstGrouping");
        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(returns.get(0).toString(), "20");
        Assert.assertEquals(returns.get(1).toString(), "-1");
        Assert.assertEquals(returns.get(2).toString(), "35");
    }

    @Test
    public void checkMapAccessReference() {

        Object returns = BRunUtil.invoke(compileResult, "checkMapAccessReference");
        Assert.assertEquals(returns.toString(), "{\"v1\":3.0,\"v2\":5.0}");
    }

    @Test
    public void testBitwiseConstExpressions() {
        BRunUtil.invoke(compileResult, "testBitwiseConstExpressions");
    }

    @Test
    public void getConstUnaryExpressions() {
        BRunUtil.invoke(compileResult, "testConstUnaryExpressions");
    }

    @Test
    public void testConstRemainderOperation() {
        BRunUtil.invoke(compileResult, "testConstRemainderOperation");
    }

    @Test
    public void testConstDecimalSubnormals() {
        BRunUtil.invoke(compileResult, "testConstDecimalSubnormals");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
