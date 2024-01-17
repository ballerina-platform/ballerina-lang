/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.klass;

import org.ballerinalang.nativeimpl.jvm.servicetests.ServiceValue;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Test cases for service classes.
 *
 * @since 2.0
 */
public class ServiceClassTest {

    @Test
    public void testBasicStructAsObject() {
        CompileResult compileResult = BCompileUtil.compile("test-src/klass/simple_service_class.bal");
        BRunUtil.invoke(compileResult, "testServiceObjectValue");
    }

    @Test
    public void testResourceMethodsDoesNotAffectAssignability() {
        CompileResult result = BCompileUtil.compile("test-src/klass/resource_method_assignability_negative_test.bal");
        int index = 0;
        validateError(result, index++, "incompatible types: expected 'Foo', found 'Bar'", 38, 13);
        validateError(result, index++, "incompatible types: expected 'Bar', found 'isolated object { " +
                "public function hello () returns (); function foo () returns (int); }'",
                40, 15);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                ".() returns (); }', found 'isolated object { resource function post .() returns (); }'",
                56, 11);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                "[int]() returns (); }', found 'isolated object { resource function get [string]() returns (); }'",
                63, 11);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                "foo/[int]() returns (); }', found 'isolated object { resource function get foo/[string]() returns " +
                        "(); }'", 70, 11);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                "[int]() returns (); }', found 'isolated object { resource function get [string]() returns (); }'",
                78, 11);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                "foo/[int]() returns (); }', found 'isolated object { resource function get foo/[string]() " +
                        "returns (); }'", 85, 11);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                "[int]() returns (); }', found 'isolated object { resource function get [byte]() returns (); }'",
                93, 11);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                "[int]() returns (); }', found 'isolated object { resource function get [string]() returns (); " +
                "resource function post [int]() returns (); }'", 100, 11);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                "[int]() returns (); }', found 'isolated object { resource function get [string]() returns (); " +
                "resource function post [int]() returns (); }'", 109, 11);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                "bar/[int...]() returns (); }', found 'isolated object { resource function get bar/[string...]() " +
                "returns (); }'", 118, 11);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                "bar/[int...]() returns (); }', found 'isolated object { resource function get bar/[byte...]() " +
                "returns (); }'", 125, 11);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                "bar/[int...]() returns (); }', found 'isolated object { resource function get bar/[int]() " +
                "returns (); }'", 132, 11);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                "foo/[int]() returns (); }', found 'isolated object { resource function get foo2/[int]() returns " +
                "(); }'", 139, 11);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                "foo/[int]() returns (); }', found 'isolated object { resource function get foo/[string]() returns " +
                "(); }'", 146, 11);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                ".(int) returns (); }', found 'isolated object { resource function get .() returns (); }'",
                153, 11);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                ".() returns (); }', found 'isolated object { resource function get .(int) returns (); }'",
                160, 11);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                ".(int...) returns (); }', found 'isolated object { resource function get .(int) returns (); }'",
                167, 11);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                "foo() returns (); }', found 'isolated object { resource function get foo(int) returns (); }'",
                174, 11);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                "[int]() returns (); }', found 'isolated object { resource function get .(int) returns (); }'",
                181, 11);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                "[int](int) returns (); }', found 'isolated object { resource function get .(int) returns (); }'",
                188, 11);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                ".(int) returns (); }', found 'isolated object { resource function get [int]() returns (); }'",
                195, 11);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                ".(int) returns (); }', found 'isolated object { resource function get [int]() returns (); }'",
                202, 11);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                ".() returns (); }', found 'isolated object { resource function post .() returns (); }'",
                209, 11);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                "[int]() returns (); }', found 'isolated object { resource function get [string]() returns (); }'",
                216, 11);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                "foo/[int]() returns (); }', found 'isolated object { resource function get foo/[string]() returns " +
                "(); }'", 223, 11);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                "[int]() returns (); }', found 'isolated object { resource function get [string]() returns (); }'",
                230, 11);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                "[int]() returns (); }', found 'isolated object { resource function get [int]() returns (); }'",
                237, 11);
        validateError(result, index++, "incompatible types: expected 'object { resource function get " +
                        "foo/[string...]() returns (); }', found 'isolated object { " +
                        "resource function get foo/[int...]() returns (); }'",
                244, 11);

        Assert.assertEquals(index, result.getErrorCount());
    }

    @Test
    public void testResourcePathParamNegative() {
        CompileResult result = BCompileUtil.compile("test-src/klass/simple_service_class_neg_path_param.bal");
        int index = 0;
        validateError(result, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path params, " +
                        "found 'json'", 37, 32);
        validateError(result, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path params, " +
                        "found 'anydata'", 37, 41);
        validateError(result, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as rest path param, " +
                        "found 'anydata'", 37, 65);
        Assert.assertEquals(index, 3);
    }

    @Test
    public void testServiceObjectAndUsingServiceObjectAsATypeInclusionNegative() {
        CompileResult result =
                BCompileUtil.compile("test-src/klass/service_type_resource_method_decl_neg.bal");
        int index = 0;
        validateError(result, index++, "no implementation found for the method 'onMesage' of class 'SClass'",
                23, 1);
        validateError(result, index++, "no implementation found for the method 'resource function get " +
                "foo/[string]() returns ()' of class 'SClass'", 23, 1);
        validateError(result, index++, "no implementation found for the method 'resource function get " +
                "foo/bar() returns ()' of class 'SClass'", 23, 1);
        validateError(result, index++, "no implementation found for the method 'resource function get " +
                "foo() returns (string)' of class 'RTwo'", 34, 1);
        validateError(result, index++, "no implementation found for the method 'resource function get " +
                "foo() returns (string)' of class 'RTypeImpl'", 43, 1);
        validateError(result, index++, "no implementation found for the method 'resource function do " +
                "f() returns (int)' of class 'Do'", 56, 1);
        validateError(result, index++, "no implementation found for the method 'resource function done " +
                "f() returns (int)' of class 'Do'", 56, 1);
        validateError(result, index++, "incompatible types: expected 'Foo', found 'Bar'", 80, 13);
        validateError(result, index++, "incompatible types: expected 'Foo', found 'Baz'", 88, 13);
        Assert.assertEquals(result.getErrorCount(), index);
    }

    @Test
    public void testResourceFunctionWithInvalidPathParam() {
        CompileResult result =
                BCompileUtil.compile("test-src/klass/resource_function_with_invalid_path_param_type_negative.bal");
        int index = 0;
        validateError(result, index++, "only 'int', 'string', 'float', 'boolean', 'decimal' types " +
                        "are supported as path params, found 'other'",
                24, 29);
        validateError(result, index++, "undefined module 'module1'",
                24, 29);
        validateError(result, index++, "unknown type 'RequestMessage'", 24, 29);
        BAssertUtil.validateError(result, index++,
                "redeclared symbol 'a'", 35, 56);
        BAssertUtil.validateError(result, index++,
                "redeclared symbol 'name'", 39, 69);
        BAssertUtil.validateError(result, index++,
                "redeclared symbol '$anonType$_2.$get$path$^$foo2'", 43, 27);
        BAssertUtil.validateError(result, index++,
                "resource path segment is not allowed after resource path rest parameter",
                47, 47);
        BAssertUtil.validateError(result, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types " +
                        "are supported as path params, found 'string?'", 51, 38);
        BAssertUtil.validateError(result, index++,
                "missing resource path in resource accessor definition",
                55, 27);
        BAssertUtil.validateError(result, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as" +
                        " path params, found 'record {| int a; anydata...; |}'", 59, 43);
        BAssertUtil.validateError(result, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as" +
                        " path params, found 'record {| int a; anydata...; |}'", 63, 44);
        BAssertUtil.validateError(result, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as rest path " +
                        "param, found 'xml'", 67, 40);
        BAssertUtil.validateError(result, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path " +
                        "params, found 'xml'", 71, 41);
        BAssertUtil.validateError(result, index++,
                "redeclared symbol '$anonType$_2.$get$xmlPath2$^'", 75, 27);
        BAssertUtil.validateError(result, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path" +
                        " params, found 'xml'", 75, 41);
        BAssertUtil.validateError(result, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path " +
                        "params, found 'map<string>'", 79, 40);
        BAssertUtil.validateError(result, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path " +
                        "params, found 'map<string>'", 83, 41);
        BAssertUtil.validateError(result, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path " +
                        "params, found '(int|error)'", 87, 47);
        BAssertUtil.validateError(result, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path " +
                        "params, found '(int|error)'", 91, 48);
        BAssertUtil.validateError(result, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path " +
                        "params, found 'error'", 95, 42);
        BAssertUtil.validateError(result, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path " +
                        "params, found 'error'", 99, 43);
        Assert.assertEquals(result.getErrorCount(), index);
    }

    @Test
    public void testResourceMethodAssignability() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/klass/resource_method_assignability_test.bal");
        BRunUtil.invoke(compileResult, "testResourceMethodAssignability");
    }

    @AfterClass
    public void reset() {
        ServiceValue.reset();
    }
}
