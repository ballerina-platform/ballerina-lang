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

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Constant expression test cases.
 *
 * @since JBallerina 1.0.0
 */
public class ConstantExpressionTest {

    private static CompileResult compileResult;

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
        Assert.assertEquals(compileResult1.getErrorCount(), i);
    }

    @Test
    public void getConstAdditions() {

        BValue[] returns = BRunUtil.invoke(compileResult, "getConstAdditions");
        Assert.assertEquals(returns.length, 4);
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertNotNull(returns[2]);
        Assert.assertNotNull(returns[3]);
        Assert.assertEquals(returns[0].stringValue(), "15");
        Assert.assertEquals(returns[1].stringValue(), "15.0");
        Assert.assertEquals(returns[2].stringValue(), "15.5");
        Assert.assertEquals(returns[3].stringValue(), "helloworld");
    }

    @Test
    public void getConstSubtracts() {

        BValue[] returns = BRunUtil.invoke(compileResult, "getConstSubtracts");
        Assert.assertEquals(returns.length, 3);
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertNotNull(returns[2]);
        Assert.assertEquals(returns[0].stringValue(), "5");
        Assert.assertEquals(returns[1].stringValue(), "5.5");
        Assert.assertEquals(returns[2].stringValue(), "5.5");
    }

    @Test
    public void getConstMultiplications() {

        BValue[] returns = BRunUtil.invoke(compileResult, "getConstMultiplications");
        Assert.assertEquals(returns.length, 3);
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertNotNull(returns[2]);
        Assert.assertEquals(returns[0].stringValue(), "50");
        Assert.assertEquals(returns[1].stringValue(), "52.5");
        Assert.assertEquals(returns[2].stringValue(), "52.5");
    }

    @Test
    public void getConstDivisions() {

        BValue[] returns = BRunUtil.invoke(compileResult, "getConstDivisions");
        Assert.assertEquals(returns.length, 5);
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertNotNull(returns[2]);
        Assert.assertNotNull(returns[3]);
        Assert.assertNotNull(returns[4]);
        Assert.assertEquals(returns[0].stringValue(), "2");
        Assert.assertEquals(returns[1].stringValue(), "2.1");
        Assert.assertEquals(returns[2].stringValue(), "NaN");
        Assert.assertEquals(returns[3].stringValue(), "Infinity");
        Assert.assertEquals(returns[4].stringValue(), "2.1");
    }

    @Test
    public void getConstGrouping() {

        BValue[] returns = BRunUtil.invoke(compileResult, "getConstGrouping");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(returns[0].stringValue(), "20");
        Assert.assertEquals(returns[1].stringValue(), "-1");
        Assert.assertEquals(returns[2].stringValue(), "35");
    }

    @Test
    public void checkMapAccessReference() {

        BValue[] returns = BRunUtil.invoke(compileResult, "checkMapAccessReference");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "{\"v1\":3.0, \"v2\":5.0}");
    }

}
