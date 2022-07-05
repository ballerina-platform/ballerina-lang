/*
 *   Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.bala.types;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Test cases for importing a user-defined union type containing the null literal.
 * @since 2.0.0
 */
public class UnionTypeWithNullLiteralBalaTest {

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test-union-type-with-null-literal");
    }

    @Test
    public void unionTypeWithNullLiteralValueAssignment() {
        CompileResult result = BCompileUtil.compile(
                "test-src/bala/test_bala/types/union_type_with_null_literal_test.bal");
        BRunUtil.invoke(result, "testPositiveAssignment");
    }

    @Test
    public void testUnionTypeWithNullLiteralNegative() {
        CompileResult negativeCompileResult = BCompileUtil.compile(
                "test-src/bala/test_bala/types/union_type_with_null_literal_test_negative.bal");
        Assert.assertEquals(negativeCompileResult.getErrorCount(), 1);
        validateError(negativeCompileResult, 0,
                "incompatible types: expected 'string', found 'testorg/testType:0.1.0:TestType'",
                20, 16);
    }

}
