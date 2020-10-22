/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.parser;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test the resilient parsing.
 *
 * @since 2.0.0
 */
public class ResilientParserTest {

    @Test(enabled = false)
    public void testResilientParsing() {
        CompileResult result = BCompileUtil.compile("test-src/parser/resilient-parser.bal");
        Assert.assertEquals(result.getErrorCount(), 96);

        BAssertUtil.validateError(result, 0, "missing function name", 1, 17);
        BAssertUtil.validateError(result, 1, "missing identifier", 5, 25);
        BAssertUtil.validateError(result, 2, "missing identifier", 9, 29);
        BAssertUtil.validateError(result, 3, "missing type desc", 9, 29);
        BAssertUtil.validateError(result, 4, "redeclared symbol 'foo2'", 13, 17);
        BAssertUtil.validateError(result, 5, "missing identifier", 13, 29);
        BAssertUtil.validateError(result, 6, "redeclared symbol 'x1'", 15, 9);
        BAssertUtil.validateError(result, 7, "incompatible types: expected '()', found 'int'", 19, 12);
        BAssertUtil.validateError(result, 8, "missing type desc", 22, 37);
        BAssertUtil.validateError(result, 9, "missing identifier", 25, 29);
        BAssertUtil.validateError(result, 10, "missing type desc", 25, 29);
        BAssertUtil.validateError(result, 11, "missing identifier", 25, 47);
        BAssertUtil.validateError(result, 12, "missing identifier", 27, 25);
        BAssertUtil.validateError(result, 13, "missing close bracket token", 28, 11);
        BAssertUtil.validateError(result, 14, "missing close bracket token", 29, 18);
        BAssertUtil.validateError(result, 15, "undefined symbol 'ar'", 30, 5);
        BAssertUtil.validateError(result, 16, "undefined symbol 'arr1'", 31, 13);
        BAssertUtil.validateError(result, 17, "invalid token ']'", 31, 18);
        BAssertUtil.validateError(result, 18, "missing equal token", 43, 12);
        BAssertUtil.validateError(result, 19, "undefined symbol 'ff'", 44, 13);
        BAssertUtil.validateError(result, 20, "unknown type 'func2'", 50, 5);
        BAssertUtil.validateError(result, 21, "missing equal token", 50, 10);
        BAssertUtil.validateError(result, 22, "missing identifier", 50, 10);
        BAssertUtil.validateError(result, 23, "missing semicolon token", 50, 14);
        BAssertUtil.validateError(result, 24, "missing type desc", 50, 14);
        BAssertUtil.validateError(result, 25, "invalid token '{'", 51, 9);
        BAssertUtil.validateError(result, 26, "redeclared symbol 'x'", 51, 9);
        BAssertUtil.validateError(result, 27, "self referenced variable 'x'", 51, 13);
        BAssertUtil.validateError(result, 28, "missing semicolon token", 52, 1);
        BAssertUtil.validateError(result, 29, "missing at token", 53, 1);
        BAssertUtil.validateError(result, 30, "missing type keyword", 53, 1);
        BAssertUtil.validateError(result, 31, "undefined annotation '$missingNode$_12'", 53, 1);
        BAssertUtil.validateError(result, 32, "invalid token '}'", 55, 2);
        BAssertUtil.validateError(result, 33, "redeclared symbol 'Foo1'", 55, 2);
        BAssertUtil.validateError(result, 34, "redeclared symbol 'Foo1'", 59, 6);
        BAssertUtil.validateError(result, 35, "missing object keyword", 59, 11);
        BAssertUtil.validateError(result, 36, "missing semicolon token", 62, 1);
        BAssertUtil.validateError(result, 37, "missing identifier", 63, 6);
        BAssertUtil.validateError(result, 38, "missing semicolon token", 66, 1);
        BAssertUtil.validateError(result, 39, "missing identifier", 67, 6);
        BAssertUtil.validateError(result, 40, "unknown type 'str'", 69, 5);
        BAssertUtil.validateError(result, 41, "missing identifier", 69, 8);
        BAssertUtil.validateError(result, 42, "missing semicolon token", 71, 1);
        BAssertUtil.validateError(result, 43, "undefined module 'http'", 72, 16);
        BAssertUtil.validateError(result, 44, "unknown type 'Listener'", 72, 16);
        BAssertUtil.validateError(result, 45, "undefined module 'http'", 73, 32);
        BAssertUtil.validateError(result, 46, "unknown type 'Caller'", 73, 32);
        BAssertUtil.validateError(result, 47, "undefined module 'http'", 73, 52);
        BAssertUtil.validateError(result, 48, "unknown type 'Request'", 73, 52);
        BAssertUtil.validateError(result, 49, "missing equal token", 77, 21);
        BAssertUtil.validateError(result, 50, "undefined module 'http'", 77, 25);
        BAssertUtil.validateError(result, 51, "unknown type 'Listener'", 77, 25);
        BAssertUtil.validateError(result, 52, "missing object keyword", 77, 45);
        BAssertUtil.validateError(result, 53, "missing semicolon token", 77, 45);
        BAssertUtil.validateError(result, 54, "invalid token 'resource'", 78, 14);
        BAssertUtil.validateError(result, 55, "undefined module 'http'", 78, 32);
        BAssertUtil.validateError(result, 56, "unknown type 'Caller'", 78, 32);
        BAssertUtil.validateError(result, 57, "undefined module 'http'", 78, 52);
        BAssertUtil.validateError(result, 58, "unknown type 'Request'", 78, 52);
        BAssertUtil.validateError(result, 59, "missing identifier", 81, 1);
        BAssertUtil.validateError(result, 60, "missing semicolon token", 81, 1);
        BAssertUtil.validateError(result, 61, "redeclared symbol 'httpService'", 82, 9);
        BAssertUtil.validateError(result, 62, "cannot infer type of the object from 'other'", 82, 24);
        BAssertUtil.validateError(result, 63, "undefined module 'http'", 83, 32);
        BAssertUtil.validateError(result, 64, "unknown type 'Caller'", 83, 32);
        BAssertUtil.validateError(result, 65, "undefined module 'http'", 83, 52);
        BAssertUtil.validateError(result, 66, "unknown type 'Request'", 83, 52);
        BAssertUtil.validateError(result, 67, "redeclared symbol 'httpService'", 87, 9);
        BAssertUtil.validateError(result, 68, "undefined module 'http'", 87, 28);
        BAssertUtil.validateError(result, 69, "unknown type 'Listener'", 87, 28);
        BAssertUtil.validateError(result, 70, "undefined module 'http'", 88, 24);
        BAssertUtil.validateError(result, 71, "unknown type 'Caller'", 88, 24);
        BAssertUtil.validateError(result, 72, "undefined module 'http'", 88, 44);
        BAssertUtil.validateError(result, 73, "unknown type 'Request'", 88, 44);
        BAssertUtil.validateError(result, 74, "redeclared symbol 'httpService'", 92, 9);
        BAssertUtil.validateError(result, 75, "undefined module 'http'", 92, 28);
        BAssertUtil.validateError(result, 76, "unknown type 'Listener'", 92, 28);
        BAssertUtil.validateError(result, 77, "undefined module 'http'", 93, 23);
        BAssertUtil.validateError(result, 78, "unknown type 'Caller'", 93, 23);
        BAssertUtil.validateError(result, 79, "undefined module 'http'", 93, 43);
        BAssertUtil.validateError(result, 80, "unknown type 'Request'", 93, 43);
        BAssertUtil.validateError(result, 81, "redeclared symbol 'httpService'", 97, 9);
        BAssertUtil.validateError(result, 82, "undefined module 'http'", 97, 28);
        BAssertUtil.validateError(result, 83, "unknown type 'Listener'", 97, 28);
        BAssertUtil.validateError(result, 84, "unknown type 'caller'", 98, 32);
        BAssertUtil.validateError(result, 85, "missing identifier", 98, 38);
        BAssertUtil.validateError(result, 86, "undefined module 'http'", 98, 40);
        BAssertUtil.validateError(result, 87, "unknown type 'Request'", 98, 40);
        BAssertUtil.validateError(result, 88, "redeclared symbol 'httpService'", 102, 9);
        BAssertUtil.validateError(result, 89, "undefined module 'http'", 102, 28);
        BAssertUtil.validateError(result, 90, "unknown type 'Listener'", 102, 28);
        BAssertUtil.validateError(result, 91, "missing open brace token", 103, 1);
        BAssertUtil.validateError(result, 92, "undefined module 'http'", 103, 32);
        BAssertUtil.validateError(result, 93, "unknown type 'Caller'", 103, 32);
        BAssertUtil.validateError(result, 94, "undefined module 'http'", 103, 52);
        BAssertUtil.validateError(result, 95, "unknown type 'Request'", 103, 52);
    }

    public void testResilientParsingModuleDeclaration() {
        CompileResult result = BCompileUtil.compile("test-src/parser/resilient-parsing-for-module-decl.bal");
        Assert.assertEquals(result.getErrorCount(), 53);

        BAssertUtil.validateError(result, 0, "redeclared symbol ''", 2, 1);
        BAssertUtil.validateError(result, 1, "missing identifier", 2, 12);
        BAssertUtil.validateError(result, 2, "cannot resolve module '$missingNode$_0/bar'", 3, 1);
        BAssertUtil.validateError(result, 3, "missing identifier", 3, 8);
        BAssertUtil.validateError(result, 4, "cannot resolve module 'foo/bar version 1.0.0 as $missingNode$_1'", 4, 1);
        BAssertUtil.validateError(result, 5, "missing identifier", 4, 32);
        BAssertUtil.validateError(result, 6, "cannot resolve module 'foo/bar version 1.0.0 as $missingNode$_2'", 5, 1);
        BAssertUtil.validateError(result, 7, "cannot resolve module 'foo/bar as foobar'", 6, 1);
        BAssertUtil.validateError(result, 8, "missing identifier", 6, 1);
        BAssertUtil.validateError(result, 9, "missing semicolon token", 6, 1);
        BAssertUtil.validateError(result, 10, "missing decimal integer literal", 6, 24);
        BAssertUtil.validateError(result, 11, "cannot resolve module 'foo/bar.foobar as foobar'", 7, 1);
        BAssertUtil.validateError(result, 12, "missing dot token", 7, 16);
        BAssertUtil.validateError(result, 13, "cannot resolve module 'foo/bar as foobar'", 8, 1);
        BAssertUtil.validateError(result, 14, "missing decimal integer literal", 8, 24);
        BAssertUtil.validateError(result, 15, "cannot resolve module 'foo/bar version 1.0.0 as foobar'", 9, 1);
        BAssertUtil.validateError(result, 16, "missing as keyword", 9, 30);
        BAssertUtil.validateError(result, 17, "cannot resolve module 'foobar'", 10, 1);
        BAssertUtil.validateError(result, 18, "cannot resolve module 'foobar'", 11, 1);
        BAssertUtil.validateError(result, 19, "missing semicolon token", 12, 1);
        BAssertUtil.validateError(result, 20, "missing identifier", 15, 13);
        BAssertUtil.validateError(result, 21, "missing type keyword", 16, 1);
        BAssertUtil.validateError(result, 22, "missing type desc", 17, 10);
        BAssertUtil.validateError(result, 23, "missing identifier", 18, 6);
        BAssertUtil.validateError(result, 24, "unknown type 'i2'", 21, 1);
        BAssertUtil.validateError(result, 25, "missing identifier", 21, 4);
        BAssertUtil.validateError(result, 26, "missing equal token", 22, 8);
        BAssertUtil.validateError(result, 27, "incompatible types: expected 'int', found 'typedesc<int>'", 24, 1);
        BAssertUtil.validateError(result, 28, "missing semicolon token", 24, 5);
        BAssertUtil.validateError(result, 29, "unknown type 'i5'", 24, 5);
        BAssertUtil.validateError(result, 30, "missing identifier", 24, 8);
        BAssertUtil.validateError(result, 31, "missing semicolon token", 25, 1);
        BAssertUtil.validateError(result, 32, "missing semicolon token", 27, 1);
        BAssertUtil.validateError(result, 33, "missing identifier", 27, 18);
        BAssertUtil.validateError(result, 34, "missing identifier", 30, 13);
        BAssertUtil.validateError(result, 35, "invalid token 'SQ2'", 37, 9);
        BAssertUtil.validateError(result, 36, "missing identifier", 40, 6);
        BAssertUtil.validateError(result, 37, "invalid token 'string'", 47, 12);
        BAssertUtil.validateError(result, 38, "missing as keyword", 50, 32);
        BAssertUtil.validateError(result, 39, "invalid token 'as'", 51, 29);
        BAssertUtil.validateError(result, 40, "missing semicolon token", 53, 1);
        BAssertUtil.validateError(result, 41, "missing identifier", 54, 1);
        BAssertUtil.validateError(result, 42, "missing semicolon token", 54, 1);
        BAssertUtil.validateError(result, 43, "annotation declaration requires a subtype of 'true', " +
                "'map<anydata|readonly>' or 'map<anydata|readonly>[]', but found 'other'", 55, 19);
        BAssertUtil.validateError(result, 44, "unknown type 'typeName'", 55, 19);
        BAssertUtil.validateError(result, 45, "missing annotation attach point", 55, 34);
        BAssertUtil.validateError(result, 46, "annotation declaration requires a subtype of 'true', " +
                "'map<anydata|readonly>' or 'map<anydata|readonly>[]', but found 'other'", 56, 1);
        BAssertUtil.validateError(result, 47, "missing annotation keyword", 56, 1);
        BAssertUtil.validateError(result, 48, "unknown type 'typeName'", 56, 1);
        BAssertUtil.validateError(result, 49, "missing function keyword", 56, 27);
        BAssertUtil.validateError(result, 50, "annotation declaration requires a subtype of 'true', " +
                "'map<anydata|readonly>' or 'map<anydata|readonly>[]', but found 'other'", 57, 19);
        BAssertUtil.validateError(result, 51, "unknown type 'typeName'", 57, 19);
        BAssertUtil.validateError(result, 52, "missing semicolon token", 58, 1);
    }

    @Test
    public void testResilientParsingClassDefn() {
        CompileResult result = BCompileUtil.compile("test-src/parser/resilient-parsing-class-defn.bal",
                CompilerPhase.COMPILER_PLUGIN);
        Assert.assertEquals(result.getErrorCount(), 4);
        BAssertUtil.validateError(result, 0, "missing class keyword", 1, 16);
        BAssertUtil.validateError(result, 1, "missing close brace token", 1, 16);
        BAssertUtil.validateError(result, 2, "missing identifier", 1, 16);
        BAssertUtil.validateError(result, 3, "missing open brace token", 1, 16);
    }

    @Test
    public void testResilientParsingError() {
        CompileResult result = BCompileUtil.compile("test-src/parser/resilient-parsing-error.bal",
                CompilerPhase.COMPILER_PLUGIN);
        Assert.assertEquals(result.getErrorCount(), 4);
        BAssertUtil.validateError(result, 0, "missing error message binding pattern", 2, 5);
        BAssertUtil.validateError(result, 1, "incompatible types: expected 'error', found 'other'", 2, 12);
        BAssertUtil.validateError(result, 2, "missing equal token", 2, 12);
        BAssertUtil.validateError(result, 3, "missing identifier", 2, 12);
    }
}
