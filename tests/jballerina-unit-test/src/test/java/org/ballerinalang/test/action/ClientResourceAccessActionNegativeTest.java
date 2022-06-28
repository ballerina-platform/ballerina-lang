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

package org.ballerinalang.test.action;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for invalid client resource access action.
 */
public class ClientResourceAccessActionNegativeTest {
    CompileResult clientResourceAccessNegative, clientResourcePathNegative,
            clientResourceParamsNegative, clientTransactionalResourceNegative;

    @BeforeClass
    public void setup() {
        clientResourceAccessNegative = BCompileUtil.
                compile("test-src/action/client_resource_call_negative.bal");
        clientResourcePathNegative = BCompileUtil.
                compile("test-src/action/client_resource_path_negative.bal");
        clientResourceParamsNegative = BCompileUtil.
                compile("test-src/action/client_resource_access_return_type_negative_test.bal");
        clientTransactionalResourceNegative = BCompileUtil.
                compile("test-src/action/client_transactional_resource_negative.bal");
    }

    @Test
    public void testClientResourceCallNegative() {
        int index = 0;
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "incompatible types: expected 'string', found 'int'", 53, 46);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "too many arguments in call to 'get()'", 54, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "too many arguments in call to 'get()'", 55, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "incompatible types: expected 'string', found '()'", 56, 42);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "incompatible types: expected 'string', found 'string?'",
                57, 42);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "incompatible types: expected 'string', found 'int'", 59, 49);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "incompatible types: expected 'string', found 'int'", 60, 49);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "incompatible types: expected 'string', found 'int'", 61, 49);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "incompatible types: expected 'string', found 'boolean'",
                62, 49);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "too many arguments in call to 'get()'", 63, 49);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "incompatible types: expected 'string', found 'int'", 64, 49);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "too many arguments in call to 'get()'", 64, 53);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "missing required parameter 'name' in call to 'get()'",
                65, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "missing required parameter 'name' in call to 'get()'",
                66, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "undefined defaultable parameter 'b'", 66, 42);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "missing required parameter 'name' in call to 'get()'",
                67, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "incompatible types: expected 'string', found 'int'", 67, 53);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "positional argument not allowed after named arguments",
                67, 57);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "too many arguments in call to 'post()'", 68, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "missing required parameter 'address' in call to 'get()'",
                69, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "positional argument not allowed after named arguments",
                69, 64);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "too many arguments in call to 'get()'", 70, 56);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "too many arguments in call to 'get()'", 71, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "too many arguments in call to 'get()'", 72, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "too many arguments in call to 'post()'", 73, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "ambiguous resource access not yet supported", 99, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "ambiguous resource access not yet supported", 100, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "ambiguous resource access not yet supported", 101, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "resource functions are only allowed in network object types",
                106, 9);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "client resource access action is only allowed on client objects",
                110, 13);
        Assert.assertEquals(clientResourceAccessNegative.getErrorCount(), index);
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
                        "resource function get intPath/[int]() returns (int); }'", 70, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource method 'put' on target resource in object 'isolated object " +
                        "{ resource function get path() returns (int); resource function get path/[int]() returns " +
                        "(int); resource function get path/[int]/foo(string) returns (int); resource function get " +
                        "path/[int]/foo2(string,string) returns (int); resource function get path/foo/bar() " +
                        "returns (); resource function get stringPath/[string]() returns (int); resource function " +
                        "get intQuotedPath/5() returns (int); resource function get intPath/[int]() returns (int); }'",
                        71, 13);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path() " +
                        "returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 72, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path() " +
                        "returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 73, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "resource access path segment is not allowed after resource access rest segment",
                73, 61);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path() " +
                        "returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string) " +
                        "returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 74, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "unsupported computed resource access path segment type: expected 'int', 'string', " +
                        "'float', 'boolean','decimal' but found 'string[]'", 74, 35);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 75, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path() " +
                        "returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int); " +
                        "resource function get intPath/[int]() returns (int); }'", 76, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path() " +
                        "returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int); " +
                        "resource function get intPath/[int]() returns (int); }'", 77, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 78, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 79, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 80, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 81, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 82, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 83, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 84, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 85, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 86, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 87, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 88, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "unsupported computed resource access path segment type: expected 'int', 'string', " +
                        "'float', 'boolean','decimal' but found 'int?'", 88, 35);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 89, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "unsupported computed resource access path segment type: expected 'int', 'string', " +
                        "'float', 'boolean','decimal' but found '()'", 89, 35);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get " +
                        "path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get " +
                        "stringPath/[string]() returns (int); resource function get intQuotedPath/5() returns (int);" +
                        " resource function get intPath/[int]() returns (int); }'", 90, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "unsupported computed resource access path segment type: expected 'int', 'string', " +
                        "'float', 'boolean','decimal' but found 'int?'", 90, 35);
        Assert.assertEquals(clientResourcePathNegative.getErrorCount(), index);
    }

    @Test
    public void testClientResourceParamsNegative() {
        int index = 0;
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'xml', found 'string'", 70, 13);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'xml', found 'int'", 71, 13);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'xml', found 'boolean'",
                72, 13);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'xml', found 'float'", 73, 13);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'xml', found 'decimal'",
                74, 13);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'int'",
                76, 48);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'int', found 'string'",
                77, 42);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'boolean', found 'float'",
                78, 50);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'float', found 'decimal'",
                79, 46);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'decimal', found 'boolean'",
                80, 45);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'record {| int a; anydata...; |}'",
                82, 16);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'xml'",
                83, 16);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'map<string>'",
                84, 16);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'int[]'",
                85, 16);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'CustomRecord'",
                86, 16);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'CustomIntegerType'",
                87, 16);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'record {| int a; anydata...; |}', found 'CustomRecord'",
                89, 57);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "a type compatible with mapping constructor expressions not found in type 'xml'",
                90, 42);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'map<string>', found 'xml:Element'",
                91, 50);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "a type compatible with mapping constructor expressions not found in type 'int[]'",
                92, 46);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'CustomRecord', found '[int,int,int]'",
                93, 58);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'int', found '[int,int,int]'",
                94, 66);
        Assert.assertEquals(clientResourceParamsNegative.getErrorCount(), index);
    }

    @Test
    public void testClientTransactionalResourceNegative() {
        int index = 0;
        BAssertUtil.validateError(clientTransactionalResourceNegative, index++,
                "invoking transactional function outside transactional scope is prohibited",
                23, 13);
        Assert.assertEquals(clientTransactionalResourceNegative.getErrorCount(), index);
    }
}
