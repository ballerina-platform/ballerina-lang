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
            clientResourcePathNegative, clientResourceParamsNegative;

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
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path params, " +
                        "found 'record {| int a; anydata...; |}'", 33, 43);
        BAssertUtil.validateError(clientResourceDeclarationNegative, index++,
                "only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path params, " +
                        "found 'record {| int a; anydata...; |}'", 37, 44);
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
                "redeclared argument 'name'", 47, 50);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "incompatible types: expected 'string', found 'int'", 48, 50);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "redeclared argument 'name'", 48, 54);
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
                "resource functions are only allowed in network object types",
                57, 9);
        BAssertUtil.validateError(clientResourceCallNegative, index++,
                "client resource access action is only allowed on client objects",
                61, 18);
    }

    @Test
    public void testClientResourcePathNegative() {
        int index = 0;
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path", 54, 34);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource method 'put' on target resource", 55, 14);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path", 56, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path", 57, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "resource access path segment is not allowed after resource access rest segment",
                57, 62);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path", 58, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "unsupported computed resource access path segment type: expected 'int', 'string', " +
                        "'float', 'boolean','decimal' but found 'string[]'", 58, 36);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path", 59, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path", 60, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path", 61, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path", 62, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path", 63, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path", 64, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path", 65, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path", 66, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path", 67, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path", 68, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path", 69, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path", 70, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path", 71, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path", 72, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "unsupported computed resource access path segment type: expected 'int', 'string', " +
                        "'float', 'boolean','decimal' but found 'int?'", 72, 36);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path", 73, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "unsupported computed resource access path segment type: expected 'int', 'string', " +
                        "'float', 'boolean','decimal' but found '()'", 73, 36);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "undefined resource path", 74, 30);
        BAssertUtil.validateError(clientResourcePathNegative, index++,
                "unsupported computed resource access path segment type: expected 'int', 'string', " +
                        "'float', 'boolean','decimal' but found 'int?'", 74, 36);
    }

    @Test
    public void testClientResourceParamsNegative() {
        int index = 0;
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'xml', found 'string'", 47, 24);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'xml', found 'int'", 48, 21);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'xml', found 'boolean'",
                49, 25);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'xml', found 'float'", 50, 23);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'xml', found 'decimal'",
                51, 25);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'int'",
                53, 60);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'int', found 'string'",
                54, 51);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'boolean', found 'float'",
                55, 63);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'float', found 'decimal'",
                56, 57);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'decimal', found 'boolean'",
                57, 58);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'record {| int a; anydata...; |}'",
                59, 27);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'xml'",
                60, 24);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'map<string>'",
                61, 24);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'int[]'",
                62, 29);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'string', found 'CustomRecord'",
                63, 33);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'record {| int a; anydata...; |}', found 'CustomRecord'",
                65, 69);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "a type compatible with mapping constructor expressions not found in type 'xml'",
                66, 51);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'map<string>', found 'xml:Element'",
                67, 59);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "a type compatible with mapping constructor expressions not found in type 'int[]'",
                68, 60);
        BAssertUtil.validateError(clientResourceParamsNegative, index++,
                "incompatible types: expected 'CustomRecord', found '[int,int,int]'",
                69, 76);
    }
}
