/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.semantic.api.test.symbolbynode;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Optional;

import static io.ballerina.compiler.api.symbols.SymbolKind.CONSTANT;
import static io.ballerina.compiler.api.symbols.SymbolKind.MODULE;
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for looking up a symbol given a module import/prefix.
 *
 * @since 2.0.0
 */
@Test
public class SymbolByImportTest extends SymbolByNodeTest {

    @BeforeClass
    public void setup() {
        CompileResult compileResult = BCompileUtil.compileAndCacheBala("test-src/testproject");
        if (compileResult.getErrorCount() != 0) {
            Arrays.stream(compileResult.getDiagnostics()).forEach(System.out::println);
            Assert.fail("Compilation contains error");
        }
    }

    @Override
    String getTestSourcePath() {
        return "test-src/symbol-by-node/symbol_by_import_test.bal";
    }

    @Override
    NodeVisitor getNodeVisitor(SemanticModel model) {
        return new NodeVisitor() {

            @Override
            public void visit(ImportDeclarationNode importDeclarationNode) {
                if (importDeclarationNode.prefix().isEmpty()) {
                    assertSymbol(importDeclarationNode, model, MODULE, "testproject");
                } else {
                    assertSymbol(importDeclarationNode, model, MODULE, "lang.int");
                }
            }

            @Override
            public void visit(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
                assertSymbol(qualifiedNameReferenceNode.modulePrefix(), model, MODULE, "testproject");
            }

            @Override
            public void visit(PositionalArgumentNode positionalArgumentNode) {
                if (positionalArgumentNode.expression().kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                    assertSymbol(positionalArgumentNode, model, CONSTANT, "PI");
                }

                if (positionalArgumentNode.expression().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                    assertSymbol(positionalArgumentNode, model, VARIABLE, "abc");
                }
            }
        };
    }

    @Override
    void verifyAssertCount() {
        assertEquals(getAssertCount(), 5);
    }

    private void assertSymbol(Node node, SemanticModel model, SymbolKind kind, String name) {
        Optional<Symbol> symbol = model.symbol(node);

        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), kind);

        assertTrue(symbol.get().getName().isPresent());
        assertEquals(symbol.get().getName().get(), name);

        incrementAssertCount();
    }
}
