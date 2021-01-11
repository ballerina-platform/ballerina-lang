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
import io.ballerina.compiler.syntax.tree.AnnotationDeclarationNode;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.compiler.api.symbols.SymbolKind.ANNOTATION;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for looking up a symbol given an annotation decl or an annotation.
 *
 * @since 2.0.0
 */
@Test
public class SymbolByAnnotationTest extends SymbolByNodeTest {

    @Override
    String getTestSourcePath() {
        return "test-src/symbol-by-node/symbol_by_annotation_test.bal";
    }

    @Override
    NodeVisitor getNodeVisitor(SemanticModel model) {
        return new NodeVisitor() {

            @Override
            public void visit(AnnotationDeclarationNode annotationDeclarationNode) {
                assertSymbol(annotationDeclarationNode.annotationTag(), model);
                assertSymbol(annotationDeclarationNode, model);
            }

            @Override
            public void visit(AnnotationNode annotationNode) {
                assertSymbol(annotationNode, model);
                assertSymbol(annotationNode.annotReference(), model);
            }
        };
    }

    @Override
    void verifyAssertCount() {
        assertEquals(getAssertCount(), 4);
    }

    private void assertSymbol(Node node, SemanticModel model) {
        Optional<Symbol> symbol = model.symbol(node);
        assertEquals(symbol.get().kind(), ANNOTATION);
        assertEquals(symbol.get().name(), "v1");
        incrementAssertCount();
    }
}
