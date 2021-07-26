/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.ballerinalang.langlib.test.statements.foreach;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Negative test cases for Foreach Statement.
 *
 * @since 0.96.0
 */
public class ForeachNegativeTests {

    @Test
    public void testForeachSemanticsNegative() {
        CompileResult compile = BCompileUtil.compile("test-src/statements/foreach/foreach-semantics-negative.bal");
        Assert.assertEquals(compile.getErrorCount(), 17);
        int index = 0;
        BAssertUtil.validateError(compile, index++,
                "invalid list binding pattern: attempted to infer a list type, but found 'string'",
                39, 17);
        BAssertUtil.validateError(compile, index++,
                "invalid list binding pattern: attempted to infer a list type, but found 'string'",
                48, 17);
        BAssertUtil.validateError(compile, index++, "redeclared symbol 'i'", 48, 18);
        BAssertUtil.validateError(compile, index++, "redeclared symbol 's'", 48, 21);
        BAssertUtil.validateError(compile, index++,
                "invalid list binding pattern: attempted to infer a list type, but found 'string'",
                55, 17);
        BAssertUtil.validateError(compile, index++, "undefined symbol 'i'", 58, 13);
        BAssertUtil.validateError(compile, index++, "incompatible types: 'int' is not an iterable collection", 63,
                22);
        BAssertUtil.validateError(compile, index++,
                "invalid list binding pattern: attempted to infer a list type, but found 'string'",
                76, 17);
        BAssertUtil.validateError(compile, index++,
                "invalid list binding pattern: attempted to infer a list type, but found 'string'",
                84, 17);
        BAssertUtil.validateError(compile, index++,
                "invalid list binding pattern: attempted to infer a list type, but found 'json'",
                93, 17);
        BAssertUtil.validateError(compile, index++,
                "invalid list binding pattern: attempted to infer a list type, but found 'string'",
                119, 17);
        BAssertUtil.validateError(compile, index++, "undefined function 'Error'", 141, 18);
        BAssertUtil.validateError(compile, index++, "undefined function 'Error'", 142, 18);
        BAssertUtil.validateError(compile, index++, "undefined function 'Error'", 143, 18);
        BAssertUtil.validateError(compile, index++,
                "incompatible types: '(json|error)' cannot be cast to 'json'", 166, 21);
        BAssertUtil.validateError(compile, index++, "invalid record binding pattern with type 'anydata'", 206, 17);
        BAssertUtil.validateError(compile, index, "invalid record binding pattern with type 'any'", 213, 17);
    }

    @Test
    public void testForeachNegative() {
        CompileResult compile = BCompileUtil.compile("test-src/statements/foreach/foreach-negative.bal");
        Assert.assertEquals(compile.getErrorCount(), 3);
        int index = 0;
        BAssertUtil.validateError(compile, index++, "unreachable code", 8, 9);
        BAssertUtil.validateError(compile, index++, "unreachable code", 13, 9);
        BAssertUtil.validateError(compile, index, "continue cannot be used outside of a loop", 15, 5);
    }

    @Test
    public void testForeachVarTypeNegative() {
        CompileResult compile = BCompileUtil.compile("test-src/statements/foreach/foreach-var-type-negative.bal");
        Assert.assertEquals(compile.getErrorCount(), 7);
        int index = 0;
        BAssertUtil.validateError(compile, index++,
                "incompatible types: expected 'anydata', found 'json'", 34, 13);
        BAssertUtil.validateError(compile, index++,
                "incompatible types: expected '(string|float|int|boolean)', found 'boolean'", 41, 13);
        BAssertUtil.validateError(compile, index++,
                "incompatible types: expected '(string|float|int|boolean)', found '(boolean|float)'", 48, 14);
        BAssertUtil.validateError(compile, index++,
                "incompatible types: expected 'xml', found '(string|float|int|boolean)'", 56, 17);
        BAssertUtil.validateError(compile, index++, "incompatible types: expected 'json', found 'xml'", 63, 13);
        BAssertUtil.validateError(compile, index++, "incompatible types: expected 'anydata', " +
                "found '(boolean|float|xml)'", 70, 14);
        BAssertUtil.validateError(compile, index, "incompatible types: expected 'string:Char', " +
                "found 'int'", 77, 13);
    }
}
