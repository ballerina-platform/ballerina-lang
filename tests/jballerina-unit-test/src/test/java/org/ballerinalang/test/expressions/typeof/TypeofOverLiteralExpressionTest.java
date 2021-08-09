/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.expressions.typeof;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BDecimal;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests to test thypeof operator over literal expressions.
 *
 * @since 0.995
 */
public class TypeofOverLiteralExpressionTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/typeof/typeof.bal");
    }

    @Test (dataProvider = "simple-value-provider")
    public void testTypeDescOperatorOnLiterals(BValue value, String expected) {
        BRunUtil.invoke(result, "typeDescOfLiterals", new BValue[] {value, new BString(expected)});
    }

    @DataProvider(name = "simple-value-provider")
    public Object[][] provideSimpleTypValues() {
        return new Object[][]{
                {new BInteger(1L), "1"},
                {new BDecimal("2.0"), "2.0"},
                {new BFloat(2.1), "2.1"},
                {new BString("str-literal"), "str-literal"},
                {new BBoolean(true), "true"},
                {new BBoolean(false), "false"},
                {null, "()"},
        };
    }

    @Test (dataProvider = "function-provider")
    public void testTypeOfExpression(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider(name = "function-provider")
    public Object[] provideFunctionNames() {
        return new String[]{"typeDescOfExpressionsOfLiterals", "passTypeofToAFunction", "typeDescOfARecord",
                "typeDescOrAObject", "passTypeofAsRestParams", "compareTypeOfValues"};
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
