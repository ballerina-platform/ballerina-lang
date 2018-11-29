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
        Assert.assertEquals(resultNegative.getErrorCount(), 25);
        BAssertUtil.validateError(resultNegative, 0, "invalid token 'World'", 23, 26);
        BAssertUtil.validateError(resultNegative, 1, "extraneous input 'World'", 23, 26);
        BAssertUtil.validateError(resultNegative, 2, "token recognition error at: '$'", 24, 25);
        BAssertUtil.validateError(resultNegative, 3, "mismatched input '0'. expecting {'is', ';', " +
                "'?', '+', '-', '*', '/', '%', '==', '!=', '>', '<', '>=', '<=', '&&', '||', '===', '!==', '&', '^', " +
                "'...', '|', '?:', '->>', '..<'}", 25, 23);
        BAssertUtil.validateError(resultNegative, 4, "token recognition error at: '\\'", 26, 25);
        BAssertUtil.validateError(resultNegative, 5, "extraneous input 'nWorld'", 26, 26);
        BAssertUtil.validateError(resultNegative, 6, "token recognition error at: '\\'", 27, 25);
        BAssertUtil.validateError(resultNegative, 7, "extraneous input 'uFFFEWorld'", 27, 26);
        BAssertUtil.validateError(resultNegative, 8, "extraneous input '\uDB80\uDC07'", 28, 25);
        BAssertUtil.validateError(resultNegative, 9, "extraneous input '\uDBBF\uDFFD'", 29, 25);
        BAssertUtil.validateError(resultNegative, 10, "extraneous input 'Lavinia'", 32, 39);
        BAssertUtil.validateError(resultNegative, 11, "mismatched input '['. expecting Identifier", 33, 12);
        BAssertUtil.validateError(resultNegative, 12, "mismatched input ']'. expecting {'[', '?', '|', Identifier}",
                33, 24);
        BAssertUtil.validateError(resultNegative, 13, "missing token ';' before 'stu'", 35, 13);
        BAssertUtil.validateError(resultNegative, 14, "mismatched input '='. expecting {'[', '?', '|', Identifier}",
                35, 17);
        BAssertUtil.validateError(resultNegative, 15, "mismatched input ':'. expecting ';'", 35, 25);
        BAssertUtil.validateError(resultNegative, 16, "extraneous input ','", 35, 37);
        BAssertUtil.validateError(resultNegative, 17, "mismatched input ':'. expecting ';'", 35, 42);
        BAssertUtil.validateError(resultNegative, 18, "mismatched input '['. expecting Identifier", 36, 15);
        BAssertUtil.validateError(resultNegative, 19, "token recognition error at: '$'", 36, 21);
        BAssertUtil.validateError(resultNegative, 20, "mismatched input ']'. expecting {'[', '?', '|', Identifier}",
                36, 26);
        BAssertUtil.validateError(resultNegative, 21, "extraneous input 'world'", 38, 43);
        BAssertUtil.validateError(resultNegative, 22, "token recognition error at: '\\'", 40, 27);
        BAssertUtil.validateError(resultNegative, 23, "extraneous input 'uFFFFStallone'", 40, 28);
        BAssertUtil.validateError(resultNegative, 24, "extraneous input '}'", 41, 1);
    }
}
