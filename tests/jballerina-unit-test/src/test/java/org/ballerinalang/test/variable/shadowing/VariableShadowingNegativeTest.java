/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.variable.shadowing;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests variable shadowing in Ballerina.
 *
 * @since 0.974.0
 */
public class VariableShadowingNegativeTest {

    @Test(description = "Test shadowed variables")
    public void testShadowedVariables() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/variable/shadowing/variable-shadowing-negative.bal");

        Assert.assertEquals(compileResult.getErrorCount(), 12);

        int index = 0;
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'name'", 7, 20);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'name'", 10, 5);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'name'", 15, 13);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'ns'", 21, 5);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'name'", 31, 5);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'name'", 34, 16);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'name'", 36, 9);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'ns'", 37, 9);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'name'", 43, 5);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'name'", 45, 27);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'name'", 47, 9);
        BAssertUtil.validateError(compileResult, index, "redeclared symbol 'ns'", 48, 9);
    }
}
