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
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.EnumMemberNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.compiler.api.symbols.SymbolKind.CONSTANT;
import static io.ballerina.compiler.api.symbols.SymbolKind.ENUM;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for looking up a symbol given an enum or enum fields.
 *
 * @since 2.0.0
 */
@Test
public class SymbolByEnumTest extends SymbolByNodeTest {

    @Override
    String getTestSourcePath() {
        return "test-src/symbol-by-node/symbol_by_enum_test.bal";
    }

    @Override
    NodeVisitor getNodeVisitor(SemanticModel model) {
        return new NodeVisitor() {
            @Override
            public void visit(EnumDeclarationNode enumDeclarationNode) {
                assertSymbol(enumDeclarationNode, model, ENUM, "Colour");
                assertSymbol(enumDeclarationNode.identifier(), model, ENUM, "Colour");

                for (Node node : enumDeclarationNode.enumMemberList()) {
                    node.accept(this);
                }
            }

            @Override
            public void visit(EnumMemberNode enumMemberNode) {
                assertSymbol(enumMemberNode, model, CONSTANT, enumMemberNode.identifier().text());
            }

            @Override
            public void visit(VariableDeclarationNode variableDeclarationNode) {
                assertSymbol(variableDeclarationNode.initializer().get(), model, CONSTANT, "RED");
            }
        };
    }

    @Override
    void verifyAssertCount() {
        assertEquals(getAssertCount(), 6);
    }

    private void assertSymbol(Node node, SemanticModel model, SymbolKind kind, String name) {
        Optional<Symbol> symbol = model.symbol(node);
        assertEquals(symbol.get().kind(), kind);
        assertEquals(symbol.get().name(), name);
        incrementAssertCount();
    }
}
