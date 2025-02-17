/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class contains a set of test cases to test the string template expression as a constant expression.
 */
public class StringTemplateConstantTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/constant/string-template-constant.bal");
    }

    @Test
    public void testStringTemplateConstantExpr() {
        Object returns = BRunUtil.invoke(compileResult, "testStringTemplateConstantExpr");
        Assert.assertNull(returns);
    }

    @Test
    public void testStringTemplateConstantExprNegative() {
        CompileResult negativeResult = BCompileUtil.compile(
                "test-src/types/constant/string-template-constant-negative.bal");
        int index = 0;
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected '(int|float|" +
                        "decimal|string|boolean)', found '(record {| 4 a; |} & readonly)'", 17, 27);
        BAssertUtil.validateError(negativeResult, index++, "expression is not a constant expression",
                20, 30);
        BAssertUtil.validateError(negativeResult, index++, "expression is not a constant expression",
                21, 30);

        Assert.assertEquals(negativeResult.getErrorCount(), index);
    }
}
