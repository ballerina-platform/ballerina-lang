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
                "incompatible types: expected 'string', found 'int'", 65, 38);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "too many arguments in call to 'get()'", 66, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "too many arguments in call to 'get()'", 67, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "incompatible types: expected 'string', found '()'", 68, 38);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "incompatible types: expected 'string', found 'string?'",
                69, 38);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "incompatible types: expected 'string', found 'int'", 71, 45);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "incompatible types: expected 'string', found 'int'", 72, 45);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "incompatible types: expected 'string', found 'int'", 73, 45);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "incompatible types: expected 'string', found 'boolean'",
                74, 45);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "too many arguments in call to 'get()'", 75, 45);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "incompatible types: expected 'string', found 'int'", 76, 45);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "too many arguments in call to 'get()'", 76, 49);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "missing required parameter 'name' in call to 'get()'",
                77, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "missing required parameter 'name' in call to 'get()'",
                78, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "undefined defaultable parameter 'b'",
                78, 38);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "too many arguments in call to 'get()'", 79, 52);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "too many arguments in call to 'get()'", 80, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "too many arguments in call to 'get()'", 81, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "missing required parameter 'name' in call to 'get()'",
                82, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "incompatible types: expected 'string', found 'int'",
                82, 49);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "positional argument not allowed after named arguments",
                82, 53);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "missing required parameter 'address' in call to 'get()'",
                83, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "positional argument not allowed after named arguments",
                83, 60);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "incompatible types: expected 'string', found 'int'",
                84, 46);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "too many arguments in call to 'get()'", 85, 47);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "missing required parameter 'address' in call to 'get()'",
                86, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "incompatible types: expected 'string', found 'int'",
                87, 57);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "too many arguments in call to 'get()'", 88, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "too many arguments in call to 'get()'", 89, 79);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "too many arguments in call to 'post()'", 90, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "too many arguments in call to 'post()'", 91, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "ambiguous resource access not yet supported", 145, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "ambiguous resource access not yet supported", 146, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "ambiguous resource access not yet supported", 147, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "ambiguous resource access not yet supported", 148, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "ambiguous resource access not yet supported", 149, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "ambiguous resource access not yet supported", 150, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "ambiguous resource access not yet supported", 151, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "ambiguous resource access not yet supported", 152, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "ambiguous resource access not yet supported", 153, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "ambiguous resource access not yet supported", 154, 13);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "resource functions are only allowed in network object types",
                159, 9);
        BAssertUtil.validateError(clientResourceAccessNegative, index++,
                "client resource access action is only allowed on client objects",
                163, 13);
        Assert.assertEquals(clientResourceAccessNegative.getErrorCount(), index);
    }

    @Test
    public void testClientResourcePathNegative() {
        int index = 0;
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                111, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource method 'put' on target resource in object 'isolated object" +
                        " { resource function get path() returns (int); resource function get path/[int]() " +
                        "returns (int); resource function get path/[int]/foo(string) returns (int); resource" +
                        " function get path/[int]/foo2(string,string) returns (int); resource function get" +
                        " path/foo/bar() returns (); resource function get stringPath/[string]() returns (int);" +
                        " resource function get intQuotedPath/5() returns (int); resource function get " +
                        "intPath/[int]() returns (int); resource function get booleanPath/[boolean]() " +
                        "returns (int); resource function get stringRestPath/[string...]() returns (int);" +
                        " resource function get intRestPath/[int...]() returns (int); resource function get " +
                        "booleanRestPath/[boolean...]() returns (int); resource function get x(int) " +
                        "returns (string); resource function get y(int?) returns (string?); resource " +
                        "function get 5(string) returns (string); resource function get 6(string?) " +
                        "returns (string?); }'",
                112, 34);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                113, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                114, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "resource access path segment is not allowed after resource access rest segment",
                114, 61);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                115, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "unsupported computed resource access path segment type: expected 'int', " +
                        "'string', 'float', 'boolean','decimal' but found 'string[]'",
                115, 35);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                116, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                117, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                118, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                119, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                120, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                121, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                122, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                123, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                124, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                125, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                126, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                127, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                128, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                129, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                130, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                131, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                132, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                133, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                134, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                135, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                136, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                137, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                138, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                139, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                140, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                141, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                142, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "unsupported computed resource access path segment type: expected 'int', 'string'," +
                        " 'float', 'boolean','decimal' but found 'int?'",
                142, 35);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                143, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "unsupported computed resource access path segment type: expected 'int', 'string'," +
                        " 'float', 'boolean','decimal' but found '()'",
                143, 35);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                144, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "unsupported computed resource access path segment type: expected 'int', 'string'," +
                        " 'float', 'boolean','decimal' but found 'int?'",
                144, 35);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                145, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                146, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                147, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                148, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                149, 29);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                150, 29);

        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                151, 29);

        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path in object 'isolated object { resource function get path()" +
                        " returns (int); resource function get path/[int]() returns (int); resource function get" +
                        " path/[int]/foo(string) returns (int); resource function get path/[int]/foo2(string,string)" +
                        " returns (int); resource function get path/foo/bar() returns (); resource function get" +
                        " stringPath/[string]() returns (int); resource function get intQuotedPath/5()" +
                        " returns (int); resource function get intPath/[int]() returns (int); resource function" +
                        " get booleanPath/[boolean]() returns (int); resource function get " +
                        "stringRestPath/[string...]() returns (int); resource function get intRestPath/[int...]()" +
                        " returns (int); resource function get booleanRestPath/[boolean...]() returns (int);" +
                        " resource function get x(int) returns (string); resource function get y(int?) " +
                        "returns (string?); resource function get 5(string) returns (string); resource" +
                        " function get 6(string?) returns (string?); }'",
                152, 29);
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
        BAssertUtil.validateError(clientTransactionalResourceNegative, index++,
                "invoking transactional function outside transactional scope is prohibited",
                26, 13);
        Assert.assertEquals(clientTransactionalResourceNegative.getErrorCount(), index);
    }
}
