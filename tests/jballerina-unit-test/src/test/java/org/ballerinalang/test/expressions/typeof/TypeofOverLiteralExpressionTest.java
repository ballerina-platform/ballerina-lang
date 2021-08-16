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

    @Test (dataProvider = "function-provider")
    public void testTypeOfExpression(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider(name = "function-provider")
    public Object[] provideFunctionNames() {
        return new String[]{"typeDescOfExpressionsOfLiterals", "passTypeofToAFunction", "typeDescOfARecord",
                "typeDescOrAObject", "passTypeofAsRestParams", "compareTypeOfValues", "typeDescOfLiterals",
                "typeOfImmutableStructuralValues", "typeOfWithCloneReadOnly"};
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
