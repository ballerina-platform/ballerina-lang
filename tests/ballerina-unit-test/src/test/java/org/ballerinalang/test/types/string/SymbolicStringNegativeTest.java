/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.types.string;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for Symbolic String Literal negative tests.
 * @since 0.982.1
 */
public class SymbolicStringNegativeTest {

    @Test
    public void testNegativeTest() {
        CompileResult resultNegative = BCompileUtil.compile("test-src/types/string/symbolic-string-negative-test.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 17);
        BAssertUtil.validateError(resultNegative, 0, "extraneous input 'World'", 23, 26);
        BAssertUtil.validateError(resultNegative, 1, "token recognition error at: '$'", 24, 25);
        BAssertUtil.validateError(resultNegative, 2, "mismatched input '0'. expecting " +
                "{'but', ';', '?', '+', '-', '*', '/', '%', '==', '!=', '>', '<', '>=', '<=', '&&', '||', '&', '^', " +
                "'...', '|', '?:', '..<'}", 25, 23);
        BAssertUtil.validateError(resultNegative, 3, "token recognition error at: '\\'", 26, 25);
        BAssertUtil.validateError(resultNegative, 4, "extraneous input 'nWorld'", 26, 26);
        BAssertUtil.validateError(resultNegative, 5, "token recognition error at: '\\'", 27, 25);
        BAssertUtil.validateError(resultNegative, 6, "extraneous input 'uFFFEWorld'", 27, 26);
        BAssertUtil.validateError(resultNegative, 7, "extraneous input '\uDB80\uDC07'", 28, 25);
        BAssertUtil.validateError(resultNegative, 8, "extraneous input '\uDBBF\uDFFD'", 29, 25);
        BAssertUtil.validateError(resultNegative, 9, "extraneous input 'Lavinia'", 32, 34);
        BAssertUtil.validateError(resultNegative, 10, "invalid token 'sl'", 33, 22);
        BAssertUtil.validateError(resultNegative, 11, "extraneous input 'Page'", 35, 33);
        BAssertUtil.validateError(resultNegative, 12, "token recognition error at: '$'", 36, 21);
        BAssertUtil.validateError(resultNegative, 14, "extraneous input 'world'", 38, 43);
        BAssertUtil.validateError(resultNegative, 15, "token recognition error at: '\\'", 40, 27);
        BAssertUtil.validateError(resultNegative, 16, "extraneous input 'uFFFFStallone'", 40, 28);
    }
}
