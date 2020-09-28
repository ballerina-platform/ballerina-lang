/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.variable.shadowing;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Tests shadowing in Ballerina.
 *
 * @since 0.974.0
 */
public class ShadowingNegativeTest {

    @Test(description = "Test shadowed identifiers")
    public void testShadowedVariables() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/variable/shadowing/shadowing_negative.bal");

        int index = 0;
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'name'", 24, 9);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'ns'", 27, 46);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'name'", 29, 20);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'ns'", 35, 42);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'name'", 50, 16);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'ns'", 51, 46);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'name'", 61, 16);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'ns'", 62, 46);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'x'", 70, 16);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'x'", 78, 20);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'f'", 79, 20);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'x'", 91, 16);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'param'", 105, 12);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'Person'", 111, 8);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'Student'", 117, 1);
        BAssertUtil.validateError(compileResult, index++, "redeclared symbol 'Person'", 126, 6);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'Vehicle'", 134, 5);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'returnVal'", 139, 40);
        assertEquals(index, compileResult.getErrorCount());
    }
}
