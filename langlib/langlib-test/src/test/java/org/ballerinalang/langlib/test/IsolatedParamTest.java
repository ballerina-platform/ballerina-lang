/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.langlib.test;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Test cases for the `@isolatedParam` annotation.
 *
 * @since Swan Lake
 */
public class IsolatedParamTest {
    private static final String NON_ISOLATED_ARG_FOR_ISOLATED_PARAM_ERROR =
            "incompatible types: expected an 'isolated' function";

    private CompileResult result;

    @BeforeClass
    private void setup() {
        result = BCompileUtil.compile("test-src/isolated-param/isolated_param.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
    }

    @Test
    public void testIsolatedParamSemanticNegative() {
        CompileResult result = BCompileUtil.compile("test-src/isolated-param/isolated_param_semantic_negative.bal");
        int index = 0;
        validateError(result, index++, "'isolatedParam' annotation is not allowed here", 17, 14);
        validateError(result, index++, "'isolatedParam' annotation is only allowed on a parameter of a function type",
                      17, 14);
        validateError(result, index++,
                      "'isolatedParam' annotation is only allowed on a parameter of an 'isolated' function", 17, 14);
        Assert.assertEquals(result.getErrorCount(), index);
    }

    @Test
    public void testIsolatedParamNegative() {
        CompileResult result = BCompileUtil.compile("test-src/isolated-param/isolated_param_negative.bal");
        int index = 0;
        validateError(result, index++, NON_ISOLATED_ARG_FOR_ISOLATED_PARAM_ERROR, 23, 23);
        validateError(result, index++, NON_ISOLATED_ARG_FOR_ISOLATED_PARAM_ERROR, 24, 20);
        validateError(result, index++, NON_ISOLATED_ARG_FOR_ISOLATED_PARAM_ERROR, 25, 15);
        validateError(result, index++, NON_ISOLATED_ARG_FOR_ISOLATED_PARAM_ERROR, 26, 15);
        validateError(result, index++, NON_ISOLATED_ARG_FOR_ISOLATED_PARAM_ERROR, 27, 15);
        validateError(result, index++, NON_ISOLATED_ARG_FOR_ISOLATED_PARAM_ERROR, 30, 18);
        validateError(result, index++, NON_ISOLATED_ARG_FOR_ISOLATED_PARAM_ERROR, 31, 18);
        validateError(result, index++, NON_ISOLATED_ARG_FOR_ISOLATED_PARAM_ERROR, 32, 24);
        validateError(result, index++, NON_ISOLATED_ARG_FOR_ISOLATED_PARAM_ERROR, 33, 24);
        validateError(result, index++, NON_ISOLATED_ARG_FOR_ISOLATED_PARAM_ERROR, 36, 28);
        validateError(result, index++, NON_ISOLATED_ARG_FOR_ISOLATED_PARAM_ERROR, 51, 59);
        validateError(result, index++, NON_ISOLATED_ARG_FOR_ISOLATED_PARAM_ERROR, 57, 82);
        validateError(result, index++, NON_ISOLATED_ARG_FOR_ISOLATED_PARAM_ERROR, 65, 22);
        Assert.assertEquals(result.getErrorCount(), index);
    }

    @Test(dataProvider = "testFunctions")
    public void testIsolatedParamAnalysis(String testFunc) {
        BRunUtil.invoke(result, testFunc);
    }

    @DataProvider
    public Object[] testFunctions() {
        return new String[]{
                "testIsolatedFunctionArgForIsolatedParam",
                "testIsolatedParamWithTypeRefTypedRestArg",
                "testArgAnalysisWithFixedLengthArrayRestArg",
                "testIsolatedFuncArgInFixedLengthArrayRestArg"
        };
    }
}
