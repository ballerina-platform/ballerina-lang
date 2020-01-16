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
package org.ballerinalang.langlib.test;

import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Test cases for the type_params library.
 *
 * @since 1.0
 */
public class TypeParamTest {

    @Test
    public void testTypeParamNegative() {

        CompileResult result = BCompileUtil.compile("test-src/type-param/type_param_test_negative.bal");
        int err = 0;
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'boolean[]', found 'int[]'", 20, 20);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'anydata', found 'function (string) " +
                "returns ()[]'", 24, 18);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'float[]', found 'function (string) " +
                "returns ()[]'", 24, 18);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'float', found 'string'", 30, 16);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'object { public function next () " +
                "returns (record {| string value; |}?); }', found 'object { public function next () returns " +
                "(record {| record {| string x; anydata...; |} value; |}?); }'", 37, 12);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'boolean', found 'Foo'", 47, 18);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'Bar', found 'Foo'", 50, 14);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'boolean', found 'BarDetail'", 64, 18);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'string', found 'int'", 69, 16);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'string', found '(Person|error)'",
                88, 16);
        Assert.assertEquals(result.getErrorCount(), err);
    }

    @Test
    public void testTypeInferring() {

        CompileResult result = BCompileUtil.compile("test-src/type-param/type_param_infer_test.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
        BRunUtil.invoke(result, "testArrayFunctionInfer");
        BRunUtil.invoke(result, "testMapFunctionInfer");
    }

    @Test
    public void testLangLibImports() {

        CompileResult result = BCompileUtil.compile("test-src/type-param/imported_type_param.bal");
        Assert.assertEquals(result.getErrorCount(), 0, "compilation contains error\n"
                + Arrays.toString(result.getDiagnostics()));
        BValue[] ret1 = BRunUtil.invoke(result, "testImportedModuleTypeParam1");
        Assert.assertEquals(ret1.length, 1);
        Assert.assertEquals(ret1[0].stringValue(), "[20, 40, 60, 80]");
        BValue[] ret2 = BRunUtil.invoke(result, "testImportedModuleTypeParam2");
        Assert.assertEquals(ret2.length, 1);
        Assert.assertEquals(ret2[0].stringValue(), "100");
    }

}
