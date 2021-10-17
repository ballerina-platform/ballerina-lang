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

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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
        Assert.assertEquals(resultNegative.getErrorCount(), 5);
        //testUndefinedSymbol
        BAssertUtil.validateError(resultNegative, 0, "undefined symbol 'name'", 2, 32);
        //testIncompatibleTypes
        BAssertUtil.validateError(resultNegative, 1,
                "incompatible types: expected '(int|float|decimal|string|boolean)', found 'json'", 8, 32);
        BAssertUtil.validateError(resultNegative, 2,
                "incompatible types: expected '(int|float|decimal|string|boolean)', found 'Foo'", 16, 21);
        BAssertUtil.validateError(resultNegative, 3,
                "incompatible types: expected '(int|float|decimal|string|boolean)', found '()'", 21, 21);
        BAssertUtil.validateError(resultNegative, 4,
                "incompatible types: expected '(int|float|decimal|string|boolean)', found '(int[]|string[])'", 26, 21);
    }

    @Test(description = "Test string template literal syntax errors")
    public void testStringTemplateLiteralSyntaxNegativeCases() {
        resultNegative = BCompileUtil.compile("test-src/types/string/string-template-literal-syntax-negative.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 13);
        int index = 0;
        BAssertUtil.validateError(resultNegative, index++, "invalid token ';'", 4, 75);
        BAssertUtil.validateError(resultNegative, index++, "invalid token 'return'", 5, 5);
        BAssertUtil.validateError(resultNegative, index++, "invalid token 's'", 5, 12);
        BAssertUtil.validateError(resultNegative, index++, "invalid token ';'", 5, 13);
        BAssertUtil.validateError(resultNegative, index++, "invalid escape sequence '\\l'", 10, 24);
        BAssertUtil.validateError(resultNegative, index++, "missing semicolon token", 10, 24);
        BAssertUtil.validateError(resultNegative, index++, "unknown type 'He\\llo'", 10, 24);
        BAssertUtil.validateError(resultNegative, index++, "invalid record binding pattern with type 'other'", 10, 33);
        BAssertUtil.validateError(resultNegative, index++, "invalid token '$'", 10, 33);
        BAssertUtil.validateError(resultNegative, index++, "missing semicolon token", 10, 39);
        BAssertUtil.validateError(resultNegative, index++,
                "variable declaration having binding pattern must be initialized", 10, 39);
        BAssertUtil.validateError(resultNegative, index++, "invalid token '`'", 10, 40);
        BAssertUtil.validateError(resultNegative, index, "invalid token ';" + System.lineSeparator() +
                "    return s;" + System.lineSeparator() + "}" + System.lineSeparator() + "'", 10, 41);
    }

    @AfterClass
    public void tearDown() {
        resultNegative = null;
    }
}
