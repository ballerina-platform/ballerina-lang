/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.module.declarations;

import org.ballerinalang.model.tree.ClientDeclarationNode;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangClientDeclaration;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tests for module-level client declarations.
 *
 * @since 2201.3.0
 */
public class ClientDeclarationTest {

    private static final String REDECLARED_PREFIX_ERROR = "redeclared symbol '%s'";
    private static final String NO_MODULE_GENERATED_ERROR = "no module generated for the client declaration";
    private static final String NOT_SUPPORTED_IN_SINGLE_FILE_ERROR =
            "client declaration is not supported with standalone Ballerina file";

    @Test
    public void testInvalidRedeclaredPrefixNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/module.declarations/client-decl/client_decl_redeclared_prefix_negative_test.bal");
        int index = 0;
        BAssertUtil.validateError(result, index++, NOT_SUPPORTED_IN_SINGLE_FILE_ERROR, 21, 1);
        BAssertUtil.validateError(result, index++, NOT_SUPPORTED_IN_SINGLE_FILE_ERROR, 23, 1);
        BAssertUtil.validateError(result, index++, NOT_SUPPORTED_IN_SINGLE_FILE_ERROR, 24, 1);
        BAssertUtil.validateError(result, index++, NOT_SUPPORTED_IN_SINGLE_FILE_ERROR, 26, 1);
        BAssertUtil.validateError(result, index++, NOT_SUPPORTED_IN_SINGLE_FILE_ERROR, 29, 1);
        BAssertUtil.validateError(result, index++, NOT_SUPPORTED_IN_SINGLE_FILE_ERROR, 30, 1);
        BAssertUtil.validateError(result, index++, NOT_SUPPORTED_IN_SINGLE_FILE_ERROR, 33, 1);
        BAssertUtil.validateError(result, index++, NOT_SUPPORTED_IN_SINGLE_FILE_ERROR, 34, 1);
        BAssertUtil.validateError(result, index++, NOT_SUPPORTED_IN_SINGLE_FILE_ERROR, 37, 1);

        BAssertUtil.validateError(result, index++, NO_MODULE_GENERATED_ERROR, 21, 1);
        BAssertUtil.validateError(result, index++, getRedeclaredSymbolError("foo"), 21, 50);
        BAssertUtil.validateError(result, index++, NO_MODULE_GENERATED_ERROR, 23, 1);
        BAssertUtil.validateError(result, index++, getRedeclaredSymbolError("bar"), 23, 50);
        BAssertUtil.validateError(result, index++, NO_MODULE_GENERATED_ERROR, 24, 1);
        BAssertUtil.validateError(result, index++, getRedeclaredSymbolError("bar"), 24, 52);
        BAssertUtil.validateError(result, index++, NO_MODULE_GENERATED_ERROR, 26, 1);
        BAssertUtil.validateError(result, index++, getRedeclaredSymbolError("baz"), 26, 51);
        BAssertUtil.validateError(result, index++, getRedeclaredSymbolError("baz"), 27, 1);
        BAssertUtil.validateError(result, index++, NO_MODULE_GENERATED_ERROR, 29, 1);
        BAssertUtil.validateError(result, index++, NO_MODULE_GENERATED_ERROR, 30, 1);
        BAssertUtil.validateError(result, index++, getRedeclaredSymbolError("qux"), 30, 52);
        BAssertUtil.validateError(result, index++, getRedeclaredSymbolError("qux"), 31, 31);
        BAssertUtil.validateError(result, index++, NO_MODULE_GENERATED_ERROR, 33, 1);
        BAssertUtil.validateError(result, index++, NO_MODULE_GENERATED_ERROR, 34, 1);
        BAssertUtil.validateError(result, index++, getRedeclaredSymbolError("quux"), 34, 50);
        // Symbols for module client declarations are defined at symbol enter because they are required to resolve
        // constructs from the corresponding module, whereas for module XMLNS declarations symbols are defined at
        // semantic analysis. Therefore, the error is always logged for the XMLNS declaration even though it is defined
        // after the client declaration in the source.
        BAssertUtil.validateError(result, index++, getRedeclaredSymbolError("corge"), 36, 31);
        BAssertUtil.validateError(result, index++, NO_MODULE_GENERATED_ERROR, 37, 1);
        Assert.assertEquals(result.getErrorCount(), index);
    }

    @Test
    public void testClientDeclPrefixAsXmlnsPrefixNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/module.declarations/client-decl/client_decl_client_prefix_as_xmlns_prefix_negative_test.bal");
        int index = 0;
        BAssertUtil.validateError(result, index++, NOT_SUPPORTED_IN_SINGLE_FILE_ERROR, 17, 1);
        BAssertUtil.validateError(result, index++, NO_MODULE_GENERATED_ERROR, 17, 1);
        BAssertUtil.validateError(result, index++, "cannot find xml namespace prefix 'foo'", 20, 19);
        BAssertUtil.validateError(result, index++, "cannot find xml namespace prefix 'foo'", 21, 16);
        BAssertUtil.validateError(result, index++, "cannot find xml namespace prefix 'foo'", 22, 24);
        Assert.assertEquals(result.getErrorCount(), index);
    }

    @Test
    public void testUndefinedPrefixNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/module.declarations/client-decl/client_decl_undefined_prefix_negative_test.bal");
        int index = 0;
        BAssertUtil.validateError(result, index++, "undefined module 'foo'", 17, 11);
        Assert.assertEquals(result.getErrorCount(), index);
    }

    @Test
    public void testUnderscoreAsPrefixNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/module.declarations/client-decl/client_decl_underscore_as_prefix_negative_test.bal");
        int index = 0;
        BAssertUtil.validateError(result, index++, NOT_SUPPORTED_IN_SINGLE_FILE_ERROR, 17, 1);
        BAssertUtil.validateError(result, index++, NO_MODULE_GENERATED_ERROR, 17, 1);
        BAssertUtil.validateError(result, index++, "'_' is a keyword, and may not be used as an identifier", 17, 50);
        Assert.assertEquals(result.getErrorCount(), index);
    }

    @Test
    public void testClientDecl() {
        CompileResult result = BCompileUtil.compile(
                "test-src/module.declarations/client-decl/client_decl_test.bal");
        List<? extends ClientDeclarationNode> clientDeclarations = result.getAST().getClientDeclarations();
        Assert.assertEquals(clientDeclarations.size(), 3);

        final Map<String, String> expectedDeclDetails = Map.ofEntries(
            Map.entry("foo", "http://www.example.com/apis/one.yaml"),
            Map.entry("bar", "http://www.example.com/apis/two.yaml"),
            Map.entry("baz", "http://www.example.com/apis/two.yaml")
        );
        final Set<String> expectedPrefixes = expectedDeclDetails.keySet();

        for (ClientDeclarationNode clientDeclaration : clientDeclarations) {
            BLangClientDeclaration bLangClientDeclaration = (BLangClientDeclaration) clientDeclaration;

            String prefix = bLangClientDeclaration.prefix.value;
            Assert.assertTrue(expectedPrefixes.contains(prefix));
            Assert.assertEquals(bLangClientDeclaration.uri.value, expectedDeclDetails.get(prefix));
        }
    }

    @Test
    public void testClientAnnotationsNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/module.declarations/client-decl/client_decl_annotations_negative_test.bal");
        int index = 0;
        BAssertUtil.validateError(result, index++, NOT_SUPPORTED_IN_SINGLE_FILE_ERROR, 48, 1);
        BAssertUtil.validateError(result, index++, "annotation declaration with 'source' attach point(s) should be a " +
                "'const' declaration", 19, 1);
        BAssertUtil.validateError(result, index++, "missing source keyword", 19, 18);
        BAssertUtil.validateError(result, index++, "annotation declaration with 'source' attach point(s) should be a " +
                "'const' declaration", 21, 1);
        BAssertUtil.validateError(result, index++, "missing source keyword", 23, 24);
        BAssertUtil.validateError(result, index++, "invalid type 'map<ballerina/lang.value:0.0.0:Cloneable>' for " +
                "annotation with 'client' attachment point: expected a subtype of 'anydata'", 25, 18);
        BAssertUtil.validateError(result, index++, "annotation 'A5' is not allowed on function", 33, 1);
        BAssertUtil.validateError(result, index++, "annotation 'A6' is not allowed on function", 36, 1);
        BAssertUtil.validateError(result, index++, "annotation 'A5' is not allowed on var", 42, 1);
        BAssertUtil.validateError(result, index++, "annotation 'A6' is not allowed on var", 45, 1);
        BAssertUtil.validateError(result, index++, "annotation 'A7' is not allowed on client", 48, 1);
        BAssertUtil.validateError(result, index++, NO_MODULE_GENERATED_ERROR, 48, 1);
        BAssertUtil.validateError(result, index++, "annotation value expected for annotation of record type 'record " +
                "{| int i; |}' with required fields", 49, 1);
        BAssertUtil.validateError(result, index++, "cannot specify more than one annotation value for annotation " +
                "'A5'", 49, 1);
        BAssertUtil.validateError(result, index++, "missing non-defaultable required record field 'i'", 50, 5);
        Assert.assertEquals(result.getErrorCount(), index);
    }

    private String getRedeclaredSymbolError(String prefix) {
        return String.format(REDECLARED_PREFIX_ERROR, prefix);
    }
}
