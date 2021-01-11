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
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.LetVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FLOAT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for looking up a symbol given a var decl node.
 *
 * @since 2.0.0
 */
@Test
public class SymbolByVarDeclTest extends SymbolByNodeTest {

    @Override
    String getTestSourcePath() {
        return "test-src/symbol-by-node/var_decl_symbol_test.bal";
    }

    @Override
    NodeVisitor getNodeVisitor(SemanticModel model) {
        Map<String, Object[]> expVarDetails = new HashMap<>();
        expVarDetails.put("x", new Object[]{"x", INT});
        expVarDetails.put("y", new Object[]{"y", FLOAT});
        expVarDetails.put("z", new Object[]{"z", INT});

        return new NodeVisitor() {

            @Override
            public void visit(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
                assertSymbol(moduleVariableDeclarationNode, model, "x", INT);
                moduleVariableDeclarationNode.typedBindingPattern().accept(this);
            }

            @Override
            public void visit(VariableDeclarationNode variableDeclarationNode) {
                assertSymbol(variableDeclarationNode, model, "y", FLOAT);
                variableDeclarationNode.typedBindingPattern().accept(this);
            }

            @Override
            public void visit(LetVariableDeclarationNode letVariableDeclarationNode) {
                assertSymbol(letVariableDeclarationNode, model, "z", INT);
                letVariableDeclarationNode.typedBindingPattern().accept(this);
            }

            @Override
            public void visit(TypedBindingPatternNode typedBindingPatternNode) {
                Optional<Symbol> symbol = model.symbol(typedBindingPatternNode);
                Object[] expVals = expVarDetails.get(symbol.get().name());
                assertSymbol(symbol.get(), (String) expVals[0], (TypeDescKind) expVals[1]);

                typedBindingPatternNode.bindingPattern().accept(this);
            }

            @Override
            public void visit(CaptureBindingPatternNode captureBindingPatternNode) {
                Optional<Symbol> symbol = model.symbol(captureBindingPatternNode);
                Object[] expVals = expVarDetails.get(symbol.get().name());
                assertSymbol(symbol.get(), (String) expVals[0], (TypeDescKind) expVals[1]);
            }
        };
    }

    @Override
    void verifyAssertCount() {
        assertEquals(getAssertCount(), 9);
    }

    private void assertSymbol(Symbol symbol, String name, TypeDescKind typeKind) {
        assertEquals(symbol.kind(), VARIABLE);
        assertEquals(symbol.name(), name);
        assertEquals(((VariableSymbol) symbol).typeDescriptor().typeKind(), typeKind);
        incrementAssertCount();
    }

    private void assertSymbol(Node node, SemanticModel model, String name, TypeDescKind typeKind) {
        Optional<Symbol> symbol = model.symbol(node);
        assertSymbol(symbol.get(), name, typeKind);
    }
}
