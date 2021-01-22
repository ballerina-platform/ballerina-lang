/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semantic.api.test.typebynode;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ErrorConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ObjectConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.TableConstructorExpressionNode;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.compiler.api.symbols.TypeDescKind.ERROR;
import static io.ballerina.compiler.api.symbols.TypeDescKind.OBJECT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.RECORD;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TABLE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TUPLE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPE_REFERENCE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for getting the type of constructor expressions.
 *
 * @since 2.0.0
 */
@Test
public class TypeByConstructorExprTest extends TypeByNodeTest {

    @Override
    String getTestSourcePath() {
        return "test-src/type-by-node/type_by_constructor_expr_test.bal";
    }

    @Override
    NodeVisitor getNodeVisitor(SemanticModel model) {
        return new NodeVisitor() {

            @Override
            public void visit(ListConstructorExpressionNode listConstructorExpressionNode) {
                assertType(listConstructorExpressionNode, model, TUPLE);
            }

            @Override
            public void visit(MappingConstructorExpressionNode mappingConstructorExpressionNode) {
                assertType(mappingConstructorExpressionNode, model, RECORD);
            }

            @Override
            public void visit(TableConstructorExpressionNode tableConstructorExpressionNode) {
                assertType(tableConstructorExpressionNode, model, TABLE);
            }

            @Override
            public void visit(ObjectConstructorExpressionNode objectConstructorExpressionNode) {
                assertType(objectConstructorExpressionNode, model, OBJECT);
            }

            @Override
            public void visit(ExplicitNewExpressionNode explicitNewExpressionNode) {
                Optional<TypeSymbol> type = assertType(explicitNewExpressionNode, model, TYPE_REFERENCE);
                assertEquals(((TypeReferenceTypeSymbol) type.get()).name(), "PersonObj");
                assertEquals(((TypeReferenceTypeSymbol) type.get()).typeDescriptor().typeKind(), OBJECT);
            }

            @Override
            public void visit(ImplicitNewExpressionNode implicitNewExpressionNode) {
                Optional<TypeSymbol> type = assertType(implicitNewExpressionNode, model, TYPE_REFERENCE);
                assertEquals(((TypeReferenceTypeSymbol) type.get()).name(), "PersonObj");
                assertEquals(((TypeReferenceTypeSymbol) type.get()).typeDescriptor().typeKind(), OBJECT);
            }

            @Override
            public void visit(ErrorConstructorExpressionNode errorConstructorExpressionNode) {
                if (errorConstructorExpressionNode.typeReference().isEmpty()) {
                    assertType(errorConstructorExpressionNode, model, ERROR);
                } else {
                    Optional<TypeSymbol> type = assertType(errorConstructorExpressionNode, model, TYPE_REFERENCE);
                    assertEquals(((TypeReferenceTypeSymbol) type.get()).name(), "TimeOutError");
                    assertEquals(((TypeReferenceTypeSymbol) type.get()).typeDescriptor().typeKind(), ERROR);
                }
            }
        };
    }

    void verifyAssertCount() {
        assertEquals(getAssertCount(), 8);
    }

    private Optional<TypeSymbol> assertType(Node node, SemanticModel model, TypeDescKind typeKind) {
        Optional<TypeSymbol> type = model.type(node);
        assertTrue(type.isPresent());
        assertEquals(type.get().typeKind(), typeKind);
        incrementAssertCount();
        return type;
    }
}
