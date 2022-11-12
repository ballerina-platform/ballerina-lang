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
    @Test
    public void testUndefinedPrefixNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/module.declarations/client-decl/client_decl_undefined_prefix_negative_test.bal");
        int index = 0;
        BAssertUtil.validateError(result, index++, "undefined module 'foo'", 17, 11);
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

    private String getRedeclaredSymbolError(String prefix) {
        return String.format(REDECLARED_PREFIX_ERROR, prefix);
    }
}
