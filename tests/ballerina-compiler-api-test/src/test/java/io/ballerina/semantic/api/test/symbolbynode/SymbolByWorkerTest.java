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
import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.testng.Assert.assertEquals;

/**
 * Test cases for looking up a symbol given a worker.
 *
 * @since 2.0.0
 */
@Test
public class SymbolByWorkerTest extends SymbolByNodeTest {

    @Override
    String getTestSourcePath() {
        return "test-src/symbol-by-node/symbol_by_worker_test.bal";
    }

    @Override
    NodeVisitor getNodeVisitor(SemanticModel model) {
        return new NodeVisitor() {

            @Override
            public void visit(NamedWorkerDeclarationNode namedWorkerDeclarationNode) {
                assertSymbol(namedWorkerDeclarationNode, model, namedWorkerDeclarationNode.workerName().text());
                assertSymbol(namedWorkerDeclarationNode.workerName(), model,
                             namedWorkerDeclarationNode.workerName().text());

            }

            @Override
            public void visit(SimpleNameReferenceNode simpleNameReferenceNode) {
                assertSymbol(simpleNameReferenceNode, model, simpleNameReferenceNode.name().text());
                assertSymbol(simpleNameReferenceNode.name(), model, simpleNameReferenceNode.name().text());
            }
        };
    }

    @Override
    void verifyAssertCount() {
        assertEquals(getAssertCount(), 6);
    }

    private void assertSymbol(Node node, SemanticModel model, String name) {
        Optional<Symbol> symbol = model.symbol(node);
        assertEquals(symbol.get().kind(), SymbolKind.WORKER);
        assertEquals(symbol.get().name(), name);
        incrementAssertCount();
    }
}
