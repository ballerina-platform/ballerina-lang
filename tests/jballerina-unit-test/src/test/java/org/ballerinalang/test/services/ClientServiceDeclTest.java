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
                "redeclared symbol 'a'", 9, 56);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "redeclared symbol 'name'", 13, 69);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "redeclared symbol '$anonType$_2.$get$path$*$foo2'", 17, 27);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "resource path segment is not allowed after resource path rest parameter",
                21, 47);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types " +
                        "are supported as path params, found 'string?'", 25, 38);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "missing resource path in resource accessor definition",
                29, 27);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as" +
                        " path params, found 'record {| int a; anydata...; |}'", 33, 43);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as" +
                        " path params, found 'record {| int a; anydata...; |}'", 37, 44);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as rest path " +
                        "param, found 'xml'", 41, 40);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path " +
                        "params, found 'xml'", 45, 41);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "redeclared symbol '$anonType$_2.$get$xmlPath2$*'", 49, 27);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path" +
                        " params, found 'xml'", 49, 41);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path " +
                        "params, found 'map<string>'", 53, 40);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path " +
                        "params, found 'map<string>'", 57, 41);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path " +
                        "params, found '(int|error)'", 61, 47);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path " +
                        "params, found '(int|error)'", 65, 48);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path " +
                        "params, found 'error'", 69, 42);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path " +
                        "params, found 'error'", 73, 43);
    }

    @Test
    public void testClientResourceCallNegative() {
        int index = 0;
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "incompatible types: expected 'string', found 'int'", 37, 51);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "too many arguments in call to 'get()'", 38, 14);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "too many arguments in call to 'get()'", 39, 14);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "incompatible types: expected 'string', found '()'", 40, 43);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "incompatible types: expected 'string', found 'string?'",
                41, 43);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "incompatible types: expected 'string', found 'int'", 43, 50);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "incompatible types: expected 'string', found 'int'", 44, 50);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "incompatible types: expected 'string', found 'int'", 45, 50);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "incompatible types: expected 'string', found 'boolean'",
                46, 50);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "too many arguments in call to 'get()'", 47, 50);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "incompatible types: expected 'string', found 'int'", 48, 50);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "too many arguments in call to 'get()'", 48, 54);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "missing required parameter 'name' in call to 'get()'",
                49, 14);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "missing required parameter 'name' in call to 'get()'",
                50, 14);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "undefined defaultable parameter 'b'", 50, 43);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "missing required parameter 'name' in call to 'get()'",
                51, 14);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "incompatible types: expected 'string', found 'int'", 51, 54);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "positional argument not allowed after named arguments",
                51, 58);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "too many arguments in call to 'post()'", 52, 14);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "missing required parameter 'address' in call to 'get()'",
                53, 14);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "positional argument not allowed after named arguments",
                53, 65);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "too many arguments in call to 'get()'", 54, 57);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "too many arguments in call to 'get()'", 55, 14);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "too many arguments in call to 'get()'", 56, 14);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "too many arguments in call to 'post()'", 57, 14);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "ambiguous resource access not yet supported", 83, 18);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "ambiguous resource access not yet supported", 84, 14);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "ambiguous resource access not yet supported", 85, 14);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "resource functions are only allowed in network object types",
                90, 9);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "client resource access action is only allowed on client objects",
                94, 18);
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
                        "resource function get intPath/[int]() returns (int); }'", 54, 34);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource method 'put' on target resource in object 'isolated object " +
                        "{ resource function get path() returns (int); resource function get path/[int]() returns " +
                        "(int); resource function get path/[int]/foo(string) returns (int); resource function get " +
                        "path/[int]/foo2(string,string) returns (int); resource function get path/foo/bar() " +
                        "returns (); resource function get stringPath/[string]() returns (int); resource function " +
                        "get intQuotedPath/5() returns (int); resource function get intPath/[int]() returns (int); }'",
                        55, 14);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path() " +
                        "returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 56, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path() " +
                        "returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 57, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "resource access path segment is not allowed after resource access rest segment",
                57, 62);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path() " +
                        "returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string) " +
                        "returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 58, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "unsupported computed resource access path segment type: expected 'int', 'string', " +
                        "'float', 'boolean','decimal' but found 'string[]'", 58, 36);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 59, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path() " +
                        "returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int); " +
                        "resource function get intPath/[int]() returns (int); }'", 60, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path() " +
                        "returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int); " +
                        "resource function get intPath/[int]() returns (int); }'", 61, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 62, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 63, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 64, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 65, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 66, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 67, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 68, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 69, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 70, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 71, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 72, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "unsupported computed resource access path segment type: expected 'int', 'string', " +
                        "'float', 'boolean','decimal' but found 'int?'", 72, 36);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 73, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "unsupported computed resource access path segment type: expected 'int', 'string', " +
                        "'float', 'boolean','decimal' but found '()'", 73, 36);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 74, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "unsupported computed resource access path segment type: expected 'int', 'string', " +
                        "'float', 'boolean','decimal' but found 'int?'", 74, 36);
    }

    @Test
    public void testClientResourceParamsNegative() {
        int index = 0;
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'xml', found 'string'", 54, 24);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'xml', found 'int'", 55, 21);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'xml', found 'boolean'",
                56, 25);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'xml', found 'float'", 57, 23);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'xml', found 'decimal'",
                58, 25);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'int'",
                60, 60);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'int', found 'string'",
                61, 51);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'boolean', found 'float'",
                62, 63);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'float', found 'decimal'",
                63, 57);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'decimal', found 'boolean'",
                64, 58);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'record {| int a; anydata...; |}'",
                66, 27);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'xml'",
                67, 24);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'map<string>'",
                68, 24);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'int[]'",
                69, 29);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'CustomRecord'",
                70, 33);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'CustomIntegerType'",
                71, 38);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'record {| int a; anydata...; |}', found 'CustomRecord'",
                73, 69);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "a type compatible with mapping constructor expressions not found in type 'xml'",
                74, 51);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'map<string>', found 'xml:Element'",
                75, 59);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "a type compatible with mapping constructor expressions not found in type 'int[]'",
                76, 60);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'CustomRecord', found '[int,int,int]'",
                77, 76);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'int', found '[int,int,int]'",
                78, 80);
    }

    @Test
    public void testClientTransactionalResourceNegative() {
        int index = 0;
        BAssertUtil.validateError(clientTransactionalResourceNegative, index++,
                "invoking transactional function outside transactional scope is prohibited",
                7, 13);
    }
}
