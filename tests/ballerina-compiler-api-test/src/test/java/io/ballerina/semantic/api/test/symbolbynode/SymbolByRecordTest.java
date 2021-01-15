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
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.compiler.api.symbols.SymbolKind.TYPE_DEFINITION;
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for looking up a symbol given a record def/record fields.
 *
 * @since 2.0.0
 */
@Test
public class SymbolByRecordTest extends SymbolByNodeTest {

    @Override
    String getTestSourcePath() {
        return "test-src/symbol-by-node/symbol_by_record_test.bal";
    }

    @Override
    NodeVisitor getNodeVisitor(SemanticModel model) {
        return new NodeVisitor() {

            @Override
            public void visit(TypeDefinitionNode typeDefinitionNode) {
                assertSymbol(typeDefinitionNode, model, TYPE_DEFINITION, typeDefinitionNode.typeName().text());
                assertSymbol(typeDefinitionNode.typeName(), model, TYPE_DEFINITION,
                             typeDefinitionNode.typeName().text());
            }

            @Override
            public void visit(RecordFieldNode recordFieldNode) {
                assertSymbol(recordFieldNode, model, VARIABLE, recordFieldNode.fieldName().text());
                assertSymbol(recordFieldNode.fieldName(), model, VARIABLE, recordFieldNode.fieldName().text());
            }

            @Override
            public void visit(RecordFieldWithDefaultValueNode recordFieldWithDefaultValueNode) {
                assertSymbol(recordFieldWithDefaultValueNode, model, VARIABLE,
                             recordFieldWithDefaultValueNode.fieldName().text());
                assertSymbol(recordFieldWithDefaultValueNode.fieldName(), model, VARIABLE,
                             recordFieldWithDefaultValueNode.fieldName().text());
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
