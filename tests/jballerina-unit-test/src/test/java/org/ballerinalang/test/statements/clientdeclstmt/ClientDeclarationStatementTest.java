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
package org.ballerinalang.test.statements.clientdeclstmt;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangClientDeclaration;
import org.wso2.ballerinalang.compiler.tree.statements.BLangClientDeclarationStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tests for client declaration statements.
 *
 * @since 2201.3.0
 */
public class ClientDeclarationStatementTest {

    private static final String REDECLARED_PREFIX_ERROR = "redeclared symbol '%s'";
    private static final String NO_MODULE_GENERATED_ERROR = "no module generated for the client declaration";

    @Test
    public void testInvalidRedeclaredPrefixNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/statements/clientdeclstmt/client_decl_stmt_redeclared_prefix_negative_test.bal");
        int index = 0;
        BAssertUtil.validateError(result, index++, NO_MODULE_GENERATED_ERROR, 22, 5);
        BAssertUtil.validateError(result, index++, getRedeclaredSymbolError("foo"), 22, 54);
        BAssertUtil.validateError(result, index++, NO_MODULE_GENERATED_ERROR, 24, 5);
        BAssertUtil.validateError(result, index++, getRedeclaredSymbolError("bar"), 24, 54);
        BAssertUtil.validateError(result, index++, NO_MODULE_GENERATED_ERROR, 25, 5);
        BAssertUtil.validateError(result, index++, getRedeclaredSymbolError("bar"), 25, 56);
        BAssertUtil.validateError(result, index++, NO_MODULE_GENERATED_ERROR, 27, 5);
        BAssertUtil.validateError(result, index++, getRedeclaredSymbolError("baz"), 27, 55);
        BAssertUtil.validateError(result, index++, getRedeclaredSymbolError("baz"), 28, 5);
        BAssertUtil.validateError(result, index++, NO_MODULE_GENERATED_ERROR, 30, 5);
        BAssertUtil.validateError(result, index++, NO_MODULE_GENERATED_ERROR, 31, 5);
        BAssertUtil.validateError(result, index++, getRedeclaredSymbolError("qux"), 31, 56);
        BAssertUtil.validateError(result, index++, getRedeclaredSymbolError("qux"), 32, 35);
        BAssertUtil.validateError(result, index++, NO_MODULE_GENERATED_ERROR, 34, 5);
        BAssertUtil.validateError(result, index++, NO_MODULE_GENERATED_ERROR, 35, 5);
        BAssertUtil.validateError(result, index++, getRedeclaredSymbolError("quux"), 35, 54);
        BAssertUtil.validateError(result, index++, NO_MODULE_GENERATED_ERROR, 38, 5);
        BAssertUtil.validateError(result, index++, getRedeclaredSymbolError("corge"), 38, 54);
        Assert.assertEquals(result.getErrorCount(), index);
    }

    @Test
    public void testClientDeclPrefixAsXmlnsPrefixNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/statements/clientdeclstmt/client_decl_stmt_client_prefix_as_xmlns_prefix_negative_test.bal");
        int index = 0;
        BAssertUtil.validateError(result, index++, NO_MODULE_GENERATED_ERROR, 18, 5);
        BAssertUtil.validateError(result, index++, "cannot find xml namespace prefix 'foo'", 20, 19);
        BAssertUtil.validateError(result, index++, "cannot find xml namespace prefix 'foo'", 21, 16);
        BAssertUtil.validateError(result, index++, "cannot find xml namespace prefix 'foo'", 22, 24);
        Assert.assertEquals(result.getErrorCount(), index);
    }

    @Test
    public void testUndefinedPrefixNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/statements/clientdeclstmt/client_decl_stmt_undefined_prefix_negative_test.bal");
        int index = 0;
        BAssertUtil.validateError(result, index++, "undefined module 'foo'", 18, 15);
        Assert.assertEquals(result.getErrorCount(), index);
    }

    @Test
    public void testClientDeclStmt() {
        CompileResult result = BCompileUtil.compile(
                "test-src/statements/clientdeclstmt/client_decl_stmt_test.bal");
        List<BLangStatement> stmts = ((BLangBlockFunctionBody) result.getAST().getFunctions().get(0).getBody()).stmts;
        Assert.assertEquals(stmts.size(), 3);

        final Map<String, String> expectedDeclDetails = Map.ofEntries(
            Map.entry("foo", "http://www.example.com/apis/one.yaml"),
            Map.entry("bar", "http://www.example.com/apis/two.yaml"),
            Map.entry("baz", "http://www.example.com/apis/two.yaml")
        );
        final Set<String> expectedPrefixes = expectedDeclDetails.keySet();

        for (int i = 0; i < 3; i++) {
            BLangStatement statement = stmts.get(i);
            Assert.assertTrue(statement instanceof BLangClientDeclarationStatement);

            BLangClientDeclaration clientDeclaration = ((BLangClientDeclarationStatement) statement).clientDeclaration;
            String prefix = clientDeclaration.prefix.value;
            Assert.assertTrue(expectedPrefixes.contains(prefix));
            Assert.assertEquals(clientDeclaration.uri.value, expectedDeclDetails.get(prefix));
        }
    }

    @Test
    public void testClientAnnotationsNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/statements/clientdeclstmt/client_decl_stmt_annotations_negative_test.bal");
        int index = 0;
        BAssertUtil.validateError(result, index++, "annotation 'A7' is not allowed on client", 22, 5);
        BAssertUtil.validateError(result, index++, NO_MODULE_GENERATED_ERROR, 22, 5);
        BAssertUtil.validateError(result, index++, "annotation value expected for annotation of record type 'record " +
                "{| int i; |}' with required fields", 23, 5);
        BAssertUtil.validateError(result, index++, "cannot specify more than one annotation value for annotation " +
                "'A5'", 23, 5);
        BAssertUtil.validateError(result, index++, "missing non-defaultable required record field 'i'", 24, 9);
        Assert.assertEquals(result.getErrorCount(), index);
    }

    private String getRedeclaredSymbolError(String prefix) {
        return String.format(REDECLARED_PREFIX_ERROR, prefix);
    }
}
