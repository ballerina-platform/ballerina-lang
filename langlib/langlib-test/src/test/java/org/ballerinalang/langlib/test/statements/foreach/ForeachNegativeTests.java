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
        BAssertUtil.validateError(compile, index++, "invalid record binding pattern with type 'any'", 213, 17);
        BAssertUtil.validateError(compile, index++, "incompatible types: expected 'string', found 'int'", 238, 20);
        BAssertUtil.validateError(compile, index++, "incompatible types: expected 'int[]', found 'int'", 239, 19);
        BAssertUtil.validateError(compile, index++, "incompatible types: expected 'string', found 'int'", 249, 18);
        BAssertUtil.validateError(compile, index++, "incompatible types: expected 'string', found 'int'", 249, 21);
        BAssertUtil.validateError(compile, index++, "incompatible types: expected 'string', found 'int'", 257, 20);
        BAssertUtil.validateError(compile, index++, "incompatible types: expected 'boolean', found 'string'", 258, 21);
        BAssertUtil.validateError(compile, index++, "incompatible types: expected 'int[]', found 'int'", 267, 19);
        BAssertUtil.validateError(compile, index++, "incompatible types: expected '(int|string)', found '" +
                "(int|string|boolean)'", 278, 24);
        BAssertUtil.validateError(compile, index++, "incompatible types: expected '(string|boolean)', found '" +
                "(int|string|boolean)'", 279, 28);
        // https://github.com/ballerina-platform/ballerina-lang/issues/33366
        BAssertUtil.validateError(compile, index++, "incompatible types: expected 'string', " +
                "found '(int|string|boolean)'", 280, 20);
        BAssertUtil.validateError(compile, index++, "incompatible types: expected 'map<int>', found 'record {| " +
                "(int|string)...; |}'", 288, 22);
        BAssertUtil.validateError(compile, index++, "incompatible types: expected 'int', found '(int|boolean)'", 296,
                                  17);
        BAssertUtil.validateError(compile, index++, "incompatible types: expected 'int', found '(string|int)'", 297,
                                  17);
        BAssertUtil.validateError(compile, index++, "incompatible types: expected '(int|boolean)', found '" +
                "(int|string|boolean)'", 307, 18);
        BAssertUtil.validateError(compile, index++, "incompatible types: expected '(int|boolean)', found '" +
                "(int|string|boolean)'", 307, 21);
        // https://github.com/ballerina-platform/ballerina-lang/issues/33366
        BAssertUtil.validateError(compile, index++, "incompatible types: expected '(int|string)', found '" +
                "(int|string|boolean)'", 308, 24);
        Assert.assertEquals(compile.getErrorCount(), index);
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
        BAssertUtil.validateError(compile, index++, "incompatible types: expected 'string:Char', " +
                "found 'int'", 77, 13);
        BAssertUtil.validateError(compile, index++, "incompatible types: expected '(int[2] & readonly)', found " +
                "'int[2][]'", 85, 13);
        BAssertUtil.validateError(compile, index++, "incompatible types: expected '(int[2] & readonly)', found '" +
                "(byte[2] & readonly)'", 92, 13);
        BAssertUtil.validateError(compile, index++, "incompatible types: expected '([int,string,boolean] & readonly)" +
                "', found '[string,int,boolean]'", 99, 13);
        BAssertUtil.validateError(compile, index++, "incompatible types: expected '(([int,int] & readonly)|([int,int]" +
                " & readonly))', found '[[int,int],[string,string]]'", 106, 13);
        BAssertUtil.validateError(compile, index++, "incompatible types: expected '((int[2] & readonly)|(string[2] &" +
                                          " readonly)|(boolean[3] & readonly))', found '[(int|boolean),(int|boolean)," +
                                          "(int|string)...]'", 113, 13);
        BAssertUtil.validateError(compile, index++, "incompatible types: expected '((record {| int a; int b; |} & " +
                "readonly)|(record {| string[] a; boolean? b; |} & readonly))', found 'record {| int a; boolean? b; " +
                "|}'", 122, 13);
        BAssertUtil.validateError(compile, index++, "incompatible types: expected '(([int,string] & readonly)|" +
                "([boolean,int] & readonly))', found '([int,int] & readonly)'", 129, 13);
        BAssertUtil.validateError(compile, index++, "incompatible types: expected '(([int,string,boolean.." +
                ".]|[boolean,int]) & readonly)', found '[int,(string|int),int...]'", 136, 13);
        Assert.assertEquals(compile.getErrorCount(), index);
    }
}
