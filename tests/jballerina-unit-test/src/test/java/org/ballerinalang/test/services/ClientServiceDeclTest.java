/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.services;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests calling of a client resource access action.
 */
public class ClientServiceDeclTest {
    CompileResult clientResourceDeclarationNegative, clientResourceCallNegative,
            clientResourcePathNegative, clientResourceParamsNegative, clientTransactionalResourceNegative;

    @BeforeClass
    public void setup() {
        clientResourceDeclarationNegative = BCompileUtil.
                compile("test-src/services/client_resource_declaration_negative.bal");
        clientResourceCallNegative = BCompileUtil.
                compile("test-src/services/client_resource_call_negative.bal");
        clientResourcePathNegative = BCompileUtil.
                compile("test-src/services/client_resource_path_negative.bal");
        clientResourceParamsNegative = BCompileUtil.
                compile("test-src/services/client_resource_params_negative.bal");
        clientTransactionalResourceNegative = BCompileUtil.
                compile("test-src/services/client_transactional_resource_negative.bal");
    }

    @Test
    public void testClientResourceDeclarationNegative() {
        int index = 0;
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "redeclared symbol 'a'", 25, 56);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "redeclared symbol 'name'", 29, 69);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "redeclared symbol '$anonType$_2.$get$path$*$foo2'", 33, 27);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "resource path segment is not allowed after resource path rest parameter",
                37, 47);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types " +
                        "are supported as path params, found 'string?'", 41, 38);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "missing resource path in resource accessor definition",
                45, 27);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as" +
                        " path params, found 'record {| int a; anydata...; |}'", 49, 43);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as" +
                        " path params, found 'record {| int a; anydata...; |}'", 53, 44);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as rest path " +
                        "param, found 'xml'", 57, 40);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path " +
                        "params, found 'xml'", 61, 41);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "redeclared symbol '$anonType$_2.$get$xmlPath2$*'", 65, 27);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path" +
                        " params, found 'xml'", 65, 41);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path " +
                        "params, found 'map<string>'", 69, 40);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path " +
                        "params, found 'map<string>'", 73, 41);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path " +
                        "params, found '(int|error)'", 77, 47);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path " +
                        "params, found '(int|error)'", 81, 48);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path " +
                        "params, found 'error'", 85, 42);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path " +
                        "params, found 'error'", 89, 43);
    }

    @Test
    public void testClientResourceCallNegative() {
        int index = 0;
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "incompatible types: expected 'string', found 'int'", 53, 51);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "too many arguments in call to 'get()'", 54, 14);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "too many arguments in call to 'get()'", 55, 14);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "incompatible types: expected 'string', found '()'", 56, 43);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "incompatible types: expected 'string', found 'string?'",
                57, 43);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "incompatible types: expected 'string', found 'int'", 59, 50);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "incompatible types: expected 'string', found 'int'", 60, 50);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "incompatible types: expected 'string', found 'int'", 61, 50);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "incompatible types: expected 'string', found 'boolean'",
                62, 50);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "too many arguments in call to 'get()'", 63, 50);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "incompatible types: expected 'string', found 'int'", 64, 50);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "too many arguments in call to 'get()'", 64, 54);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "missing required parameter 'name' in call to 'get()'",
                65, 14);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "missing required parameter 'name' in call to 'get()'",
                66, 14);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "undefined defaultable parameter 'b'", 66, 43);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "missing required parameter 'name' in call to 'get()'",
                67, 14);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "incompatible types: expected 'string', found 'int'", 67, 54);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "positional argument not allowed after named arguments",
                67, 58);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "too many arguments in call to 'post()'", 68, 14);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "missing required parameter 'address' in call to 'get()'",
                69, 14);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "positional argument not allowed after named arguments",
                69, 65);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "too many arguments in call to 'get()'", 70, 57);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "too many arguments in call to 'get()'", 71, 14);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "too many arguments in call to 'get()'", 72, 14);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "too many arguments in call to 'post()'", 73, 14);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "ambiguous resource access not yet supported", 99, 18);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "ambiguous resource access not yet supported", 100, 14);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "ambiguous resource access not yet supported", 101, 14);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "resource functions are only allowed in network object types",
                106, 9);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "client resource access action is only allowed on client objects",
                110, 18);
    }

    @Test
    public void testClientResourcePathNegative() {
        int index = 0;
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path() " +
                        "returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string) " +
                        "returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int); " +
                        "resource function get intPath/[int]() returns (int); }'", 70, 34);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource method 'put' on target resource in object 'isolated object " +
                        "{ resource function get path() returns (int); resource function get path/[int]() returns " +
                        "(int); resource function get path/[int]/foo(string) returns (int); resource function get " +
                        "path/[int]/foo2(string,string) returns (int); resource function get path/foo/bar() " +
                        "returns (); resource function get stringPath/[string]() returns (int); resource function " +
                        "get intQuotedPath/5() returns (int); resource function get intPath/[int]() returns (int); }'",
                        71, 14);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path() " +
                        "returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 72, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path() " +
                        "returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 73, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "resource access path segment is not allowed after resource access rest segment",
                73, 62);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path() " +
                        "returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string) " +
                        "returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 74, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "unsupported computed resource access path segment type: expected 'int', 'string', " +
                        "'float', 'boolean','decimal' but found 'string[]'", 74, 36);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 75, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path() " +
                        "returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int); " +
                        "resource function get intPath/[int]() returns (int); }'", 76, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path() " +
                        "returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int); " +
                        "resource function get intPath/[int]() returns (int); }'", 77, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 78, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 79, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 80, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 81, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 82, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 83, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 84, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 85, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 86, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 87, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 88, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "unsupported computed resource access path segment type: expected 'int', 'string', " +
                        "'float', 'boolean','decimal' but found 'int?'", 88, 36);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 89, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "unsupported computed resource access path segment type: expected 'int', 'string', " +
                        "'float', 'boolean','decimal' but found '()'", 89, 36);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 90, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "unsupported computed resource access path segment type: expected 'int', 'string', " +
                        "'float', 'boolean','decimal' but found 'int?'", 90, 36);
    }

    @Test
    public void testClientResourceParamsNegative() {
        int index = 0;
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'xml', found 'string'", 70, 24);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'xml', found 'int'", 71, 21);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'xml', found 'boolean'",
                72, 25);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'xml', found 'float'", 73, 23);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'xml', found 'decimal'",
                74, 25);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'int'",
                76, 60);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'int', found 'string'",
                77, 51);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'boolean', found 'float'",
                78, 63);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'float', found 'decimal'",
                79, 57);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'decimal', found 'boolean'",
                80, 58);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'record {| int a; anydata...; |}'",
                82, 27);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'xml'",
                83, 24);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'map<string>'",
                84, 24);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'int[]'",
                85, 29);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'CustomRecord'",
                86, 33);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'CustomIntegerType'",
                87, 38);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'record {| int a; anydata...; |}', found 'CustomRecord'",
                89, 69);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "a type compatible with mapping constructor expressions not found in type 'xml'",
                90, 51);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'map<string>', found 'xml:Element'",
                91, 59);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "a type compatible with mapping constructor expressions not found in type 'int[]'",
                92, 60);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'CustomRecord', found '[int,int,int]'",
                93, 76);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'int', found '[int,int,int]'",
                94, 80);
    }

    @Test
    public void testClientTransactionalResourceNegative() {
        int index = 0;
        BAssertUtil.validateError(clientTransactionalResourceNegative, index++,
                "invoking transactional function outside transactional scope is prohibited",
                23, 13);
    }
}
