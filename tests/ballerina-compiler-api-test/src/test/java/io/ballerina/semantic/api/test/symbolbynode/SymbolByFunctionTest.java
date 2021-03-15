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
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.RestParameterNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.compiler.api.symbols.ParameterKind.DEFAULTABLE;
import static io.ballerina.compiler.api.symbols.ParameterKind.REQUIRED;
import static io.ballerina.compiler.api.symbols.ParameterKind.REST;
import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.SymbolKind.PARAMETER;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for looking up a symbol given a function/param nodes etc.
 *
 * @since 2.0.0
 */
@Test
public class SymbolByFunctionTest extends SymbolByNodeTest {

    @Override
    String getTestSourcePath() {
        return "test-src/symbol-by-node/symbol_by_function_test.bal";
    }

    @Override
    NodeVisitor getNodeVisitor(SemanticModel model) {
        return new NodeVisitor() {

            @Override
            public void visit(FunctionDefinitionNode functionDefinitionNode) {
                assertSymbol(functionDefinitionNode, model, FUNCTION, functionDefinitionNode.functionName().text());
                assertSymbol(functionDefinitionNode.functionName(), model, FUNCTION,
                             functionDefinitionNode.functionName().text());

                functionDefinitionNode.functionSignature().accept(this);
                functionDefinitionNode.functionBody().accept(this);
            }

            @Override
            public void visit(RequiredParameterNode requiredParameterNode) {
                Symbol symbol = assertSymbol(requiredParameterNode, model, PARAMETER, "a");
                assertEquals(((ParameterSymbol) symbol).paramKind(), REQUIRED);

                symbol = assertSymbol(requiredParameterNode.paramName().get(), model, PARAMETER, "a");
                assertEquals(((ParameterSymbol) symbol).paramKind(), REQUIRED);
            }

            @Override
            public void visit(DefaultableParameterNode defaultableParameterNode) {
                Symbol symbol = assertSymbol(defaultableParameterNode, model, PARAMETER, "b");
                assertEquals(((ParameterSymbol) symbol).paramKind(), DEFAULTABLE);

                symbol = assertSymbol(defaultableParameterNode.paramName().get(), model, PARAMETER, "b");
                assertEquals(((ParameterSymbol) symbol).paramKind(), DEFAULTABLE);
            }

            @Override
            public void visit(RestParameterNode restParameterNode) {
                Symbol symbol = assertSymbol(restParameterNode, model, PARAMETER, "c");
                assertEquals(((ParameterSymbol) symbol).paramKind(), REST);

                symbol = assertSymbol(restParameterNode.paramName().get(), model, PARAMETER, "c");
                assertEquals(((ParameterSymbol) symbol).paramKind(), REST);
            }

            @Override
            public void visit(VariableDeclarationNode variableDeclarationNode) {
                assertSymbol(variableDeclarationNode.initializer().get(), model, FUNCTION, "foo");
                variableDeclarationNode.initializer().get().accept(this);
            }

            @Override
            public void visit(FunctionCallExpressionNode functionCallExpressionNode) {
                assertSymbol(functionCallExpressionNode, model, FUNCTION, "foo");
                assertSymbol(functionCallExpressionNode.functionName(), model, FUNCTION, "foo");
            }
        };
    }

    @Override
    void verifyAssertCount() {
        assertEquals(getAssertCount(), 13);
    }

    private Symbol assertSymbol(Node node, SemanticModel model, SymbolKind kind, String name) {
        Optional<Symbol> symbol = model.symbol(node);
        assertEquals(symbol.get().kind(), kind);
        assertEquals(symbol.get().getName().get(), name);
        incrementAssertCount();
        return symbol.get();
    }
}
