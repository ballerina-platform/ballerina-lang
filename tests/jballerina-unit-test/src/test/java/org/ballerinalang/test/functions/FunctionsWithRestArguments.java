/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
 *
 */
package org.ballerinalang.test.functions;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Function Arguments with mapping type rest Expression.
 *
 * @since 2.0.0
 */
public class FunctionsWithRestArguments {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/functions/functions_with_mapping_type_rest_argument.bal");
    }

    @Test(description = "Test functions arguments with function calls as default value")
    public void testFunctionWithMappingTypeRestArg() {
        BRunUtil.invoke(result, "testFunctionWithMappingTypeRestArg");
    }

    @Test
    public void testFuncSignatureSemanticsNegative() {
        int i = 0;
        CompileResult result = BCompileUtil.compile("test-src/functions/" +
                "functions_with_mapping_type_rest_argument_negative.bal");
        BAssertUtil.validateError(result, i++, "incompatible types: expected '([int,int]|record {| int c; " +
                "int d?; |})', found 'Foo'", 50, 33);
        BAssertUtil.validateError(result, i++, "incompatible types: expected '([int,int,int]|record {| int a; " +
                "int b; int c; |})', found 'Bar'", 53, 25);
        BAssertUtil.validateError(result, i++, "missing required parameter 'c' in call to 'getAvg()'", 56, 16);
        BAssertUtil.validateError(result, i++, "rest argument not allowed after named arguments", 56, 35);
        BAssertUtil.validateError(result, i++, "incompatible types: expected '([int,int,int,int...]|record " +
                "{| int a; int b; int c; |})', found 'Bar'", 59, 29);
        BAssertUtil.validateError(result, i++, "incompatible types: expected 'int[]', found 'Val'", 62, 42);

        Assert.assertEquals(i, result.getErrorCount());
    }
}
