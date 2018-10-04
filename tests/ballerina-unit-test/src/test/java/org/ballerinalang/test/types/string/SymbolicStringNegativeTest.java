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
 * Test Native functions in ballerina.model.string.
 */
public class SymbolicStringNegativeTest {

    @Test
    public void testNegativeTest() {
        CompileResult resultNegative = BCompileUtil.compile("test-src/types/string/symbolic-string-negative-test.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 15);
        BAssertUtil.validateError(resultNegative, 0, "extraneous input 'World'", 7, 26);
        BAssertUtil.validateError(resultNegative, 1, "token recognition error at: '$'", 8, 25);
        BAssertUtil.validateError(resultNegative, 2, "mismatched input '0'. expecting " +
                "{'but', ';', '?', '+', '-', '*', '/', '%', '==', '!=', '>', '<', '>=', '<=', '&&', '||', '&', '^', " +
                "'...', '|', '?:', '..<'}", 9, 23);
        BAssertUtil.validateError(resultNegative, 3, "token recognition error at: '\\'", 10, 25);
        BAssertUtil.validateError(resultNegative, 4, "extraneous input 'nWorld'", 10, 26);
        BAssertUtil.validateError(resultNegative, 5, "token recognition error at: '\\'", 11, 25);
        BAssertUtil.validateError(resultNegative, 6, "extraneous input 'uFFFEWorld'", 11, 26);
        BAssertUtil.validateError(resultNegative, 7, "extraneous input 'Lavinia'", 13, 34);
        BAssertUtil.validateError(resultNegative, 8, "invalid token 'sl'", 14, 22);
        BAssertUtil.validateError(resultNegative, 9, "extraneous input 'Page'", 16, 33);
        BAssertUtil.validateError(resultNegative, 10, "token recognition error at: '$'", 17, 21);
        BAssertUtil.validateError(resultNegative, 12, "extraneous input 'world'", 19, 43);
        BAssertUtil.validateError(resultNegative, 13, "token recognition error at: '\\'", 21, 27);
        BAssertUtil.validateError(resultNegative, 14, "extraneous input 'uFFFFStallone'", 21, 28);
    }
}
