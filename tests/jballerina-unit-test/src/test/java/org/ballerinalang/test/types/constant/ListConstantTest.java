/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests for list constructor expr in constant context.
 */
public class ListConstantTest {
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/constant/list_constructor_expr_as_constant_expr.bal");
    }

    @Test(dataProvider = "ListConstructorExprAsConstantExpr")
    public void testListConstructorExprAsConstantExpr(String funcName) {
        BRunUtil.invoke(compileResult, funcName);
    }

    @DataProvider(name = "ListConstructorExprAsConstantExpr")
    public Object[][] getTestForListConstructorExprAsConstantExpr() {
        return new Object[][]{
                {"testIntArrayAsExpectedType"},
                {"testSubTypeOfIntArrayAsExpectedType"},
                {"testByteArrayAsExpectedType"},
                {"testFloatArrayAsExpectedType"},
                {"testDecimalAsExpectedType"},
                {"testStringArrayAsExpectedType"},
                {"testListConstructorExprWithSpreadOpExpr"},
                {"testListConstructorExprWithIntFillMembers"},
                {"testListConstructorExprWithStringFillMembers"},
                {"testTupleAsExpectedType"},
                {"testTupleAsExpectedTypeWithFillMembers"},
                {"testListConstExprWithoutExpectedType"},
                {"testListConstantWithUnionAsExpectedType"},
                {"testListConstExprWithTypeRefFillMember"}
        };
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
