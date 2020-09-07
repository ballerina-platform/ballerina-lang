/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for String Template Literal negative tests.
 */
@Test
public class StringTemplateLiteralNegativeTest {

    private CompileResult resultNegative;

    @Test(description = "Test string template literal with errors")
    public void testStringTemplateLiteralNegativeCases() {
        resultNegative = BCompileUtil.compile("test-src/types/string/string-template-literal-negative.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 2);
        //testUndefinedSymbol
        BAssertUtil.validateError(resultNegative, 0, "undefined symbol 'name'", 2, 32);
        //testIncompatibleTypes
        BAssertUtil.validateError(resultNegative, 1,
                "incompatible types: expected '(int|float|decimal|string|boolean)', found 'json'", 8, 32);
    }

    @Test(description = "Test string template literal syntax errors", groups = { "disableOnOldParser" })
    public void testStringTemplateLiteralSyntaxNegativeCases() {
        resultNegative = BCompileUtil.compile("test-src/types/string/string-template-literal-syntax-negative.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 14);
        int index = 0;
        BAssertUtil.validateError(resultNegative, index++, "invalid token ';'", 4, 74);
        BAssertUtil.validateError(resultNegative, index++, "invalid token ';'", 4, 74);
        BAssertUtil.validateError(resultNegative, index++, "invalid token 'return'", 4, 74);
        BAssertUtil.validateError(resultNegative, index++, "invalid token 's'", 4, 74);
        BAssertUtil.validateError(resultNegative, index++, "missing plus token", 10, 24);
        BAssertUtil.validateError(resultNegative, index++, "undefined symbol 'He'", 10, 24);
        BAssertUtil.validateError(resultNegative, index++, "missing semicolon token", 10, 26);
        BAssertUtil.validateError(resultNegative, index++, "invalid token '$'", 10, 33);
        BAssertUtil.validateError(resultNegative, index++, "invalid token '\\llo'", 10, 33);
        BAssertUtil.validateError(resultNegative, index++, "unknown type 'name'", 10, 34);
        BAssertUtil.validateError(resultNegative, index++, "missing identifier", 10, 38);
        BAssertUtil.validateError(resultNegative, index++, "missing semicolon token", 10, 38);
        BAssertUtil.validateError(resultNegative, index++, "invalid token ';\n    return s;\n}\n'", 13, 1);
        BAssertUtil.validateError(resultNegative, index++, "invalid token '`'", 13, 1);
    }
}
