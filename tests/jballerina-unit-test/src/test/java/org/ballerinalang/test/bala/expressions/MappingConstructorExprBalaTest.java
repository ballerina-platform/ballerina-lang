/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.bala.expressions;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Bala test cases for mapping constructor expression.
 *
 * @since 2201.9.0
 */
public class MappingConstructorExprBalaTest {

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project");
    }

    @Test
    public void testModuleConstantsInMappingConstructor() {
        CompileResult compileResult =
                BCompileUtil.compile("test-src/bala/test_bala/expressions/mapping_constructor_expr.bal");
        BRunUtil.invoke(compileResult, "testModuleConstantsInMappingConstructor");
    }

    @Test
    public void testMappingConstructorExprNegative() {
        CompileResult negativeCompileResult = BCompileUtil.compile(
                "test-src/bala/test_bala/expressions/mapping_constructor_expr_negative.bal");
        int i = 0;
        BAssertUtil.validateError(negativeCompileResult, i++,
                "attempt to refer to non-accessible symbol 'MAPPING_NAME'", 24, 23);
        BAssertUtil.validateError(negativeCompileResult, i++, "undefined symbol 'MAPPING_NAME'", 24, 23);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible mapping constructor expression for type 'map<string>?'", 25, 22);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "attempt to refer to non-accessible symbol 'MAPPING_NAME'", 25, 24);
        BAssertUtil.validateError(negativeCompileResult, i++, "undefined symbol 'MAPPING_NAME'", 25, 24);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible mapping constructor expression for type 'map<string>?'", 26, 22);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "attempt to refer to non-accessible symbol 'MAPPING_NAME'", 26, 24);
        BAssertUtil.validateError(negativeCompileResult, i++, "undefined symbol 'MAPPING_NAME'", 26, 24);
        BAssertUtil.validateError(negativeCompileResult, i++, "attempt to refer to non-accessible symbol 'MAPPING_C'",
                26, 43);
        BAssertUtil.validateError(negativeCompileResult, i++, "undefined symbol 'MAPPING_C'", 26, 43);
        BAssertUtil.validateError(negativeCompileResult, i++, "missing non-defaultable required record field 'name'",
                27, 14);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "attempt to refer to non-accessible symbol 'MAPPING_NAME'", 27, 16);
        BAssertUtil.validateError(negativeCompileResult, i++, "undefined symbol 'MAPPING_NAME'", 27, 16);
        BAssertUtil.validateError(negativeCompileResult, i++, "attempt to refer to non-accessible symbol 'MAPPING_C'",
                27, 35);
        BAssertUtil.validateError(negativeCompileResult, i++, "undefined symbol 'MAPPING_C'", 27, 35);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible mapping constructor expression for type '(map<string>|Rec)'", 28, 25);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "attempt to refer to non-accessible symbol 'MAPPING_NAME'", 28, 27);
        BAssertUtil.validateError(negativeCompileResult, i++, "undefined symbol 'MAPPING_NAME'", 28, 27);
        BAssertUtil.validateError(negativeCompileResult, i++, "attempt to refer to non-accessible symbol 'MAPPING_C'",
                28, 46);
        BAssertUtil.validateError(negativeCompileResult, i++, "undefined symbol 'MAPPING_C'", 28, 46);
        Assert.assertEquals(negativeCompileResult.getErrorCount(), i);
    }
}
