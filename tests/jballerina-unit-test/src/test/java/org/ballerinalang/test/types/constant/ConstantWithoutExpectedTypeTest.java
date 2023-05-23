/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
 * Constant expression without expected type test cases.
 *
 * @since 2201.7.0
 */
public class ConstantWithoutExpectedTypeTest {
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/constant/constant_without_expected_type.bal");
    }

    @Test(dataProvider = "constantWithoutExpectedTypeFunctions")
    public void testConstantWithoutExpectedType(String functionName) {
        BRunUtil.invoke(compileResult, functionName);
    }

    @DataProvider(name = "constantWithoutExpectedTypeFunctions")
    public Object[] constantWithoutExpectedTypeFunctions() {
        return new Object[] {
                "testConstAdditions",
                "testConstSubtracts",
                "testConstMultiplications",
                "testConstDivisions",
                "testConstGrouping",
                "testMapAccessReference",
                "testBitwiseConstExpressions",
                "testConstUnaryExpressions",
                "testConstRemainderOperation"
        };
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
