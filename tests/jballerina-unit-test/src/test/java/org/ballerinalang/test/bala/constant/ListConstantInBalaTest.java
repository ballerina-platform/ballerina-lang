/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.bala.constant;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases for reading list constants.
 *
 *  @since 2201.9.0
 */
public class ListConstantInBalaTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project");
        compileResult = BCompileUtil.compile("test-src/bala/test_bala/constant/list_literal_constant.bal");
    }

    @Test(dataProvider = "constantListAccessTestDataProvider")
    public void testConstantListAccess(String testCase) {
        BRunUtil.invoke(compileResult, testCase);
    }

    @DataProvider(name = "constantListAccessTestDataProvider")
    public Object[] constantListAccessTestDataProvider() {
        return new Object[]{
                "testSimpleArrayAccess",
                "testSimpleTupleAccess",
                "test2DTupleAccess",
                "test2DArrayAccess",
                "testFixedLengthArrayAccess",
                "testArrayWithRestAccess",
                "test2DUnionArrayAccess"
        };
    }

    @Test
    public void testConstantListAccessNegative() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/bala/test_bala/constant/list_constant_negative.bal");
        int i = 0;
        BAssertUtil.validateError(compileResult, i++,
                "incompatible types: expected '[string,string,int...]', found '[\"a\",\"b\",\"c\"] & readonly'", 20,
                34);
        BAssertUtil.validateError(compileResult, i++,
                "incompatible types: expected 'int[]', found '[true,false,true] & readonly'", 21, 15);
        BAssertUtil.validateError(compileResult, i++,
                "cannot update 'readonly' value of type '[\"a\",\"b\",\"c\"] & readonly'", 26, 5);
        BAssertUtil.validateError(compileResult, i++,
                "cannot update 'readonly' value of type '[\"a\",\"b\",\"c\"] & readonly'", 27, 5);
        BAssertUtil.validateError(compileResult, i++,
                "incompatible types: expected '(\"a\"|\"b\"|\"c\")', found 'string:Char'", 27, 12);
        BAssertUtil.validateError(compileResult, i++,
                "cannot update 'readonly' value of type '[1,\"f\",\"g\"] & readonly'", 30, 5);
        BAssertUtil.validateError(compileResult, i++, "incompatible types: expected '(1|\"f\"|\"g\")', found 'string'",
                30, 12);
        BAssertUtil.validateError(compileResult, i++, "undefined symbol 'l13'", 34, 9);
        BAssertUtil.validateError(compileResult, i++, "undefined symbol 'l14'", 35, 9);
        BAssertUtil.validateError(compileResult, i++, "attempt to refer to non-accessible symbol 'l10'", 39, 9);
        BAssertUtil.validateError(compileResult, i++, "undefined symbol 'l10'", 39, 9);
        BAssertUtil.validateError(compileResult, i++, "attempt to refer to non-accessible symbol 'l11'", 40, 9);
        BAssertUtil.validateError(compileResult, i++, "undefined symbol 'l11'", 40, 9);
        BAssertUtil.validateError(compileResult, i++, "attempt to refer to non-accessible symbol 'l12'", 41, 9);
        BAssertUtil.validateError(compileResult, i++, "undefined symbol 'l12'", 41, 9);
        Assert.assertEquals(compileResult.getErrorCount(), i);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
