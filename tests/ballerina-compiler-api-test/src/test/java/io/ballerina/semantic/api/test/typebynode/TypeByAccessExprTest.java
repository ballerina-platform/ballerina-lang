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
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.OptionalFieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static io.ballerina.compiler.api.symbols.TypeDescKind.ERROR;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.NIL;
import static io.ballerina.compiler.api.symbols.TypeDescKind.RECORD;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPE_REFERENCE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.UNION;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for getting the type of access exprs.
 *
 * @since 2.0.0
 */
@Test
public class TypeByAccessExprTest extends TypeByNodeTest {

    @Override
    String getTestSourcePath() {
        return "test-src/type-by-node/type_by_access_expr.bal";
    }

    @Override
    NodeVisitor getNodeVisitor(SemanticModel model) {
        return new NodeVisitor() {

            @Override
            public void visit(FieldAccessExpressionNode fieldAccessExpressionNode) {
                if (((SimpleNameReferenceNode) fieldAccessExpressionNode.fieldName()).name().text().equals("name")) {
                    assertType(fieldAccessExpressionNode, model, STRING);
                } else {
                    Optional<TypeSymbol> type = assertType(fieldAccessExpressionNode, model, UNION);
                    assertUnionMembers((UnionTypeSymbol) type.get(), STRING, ERROR);
                }
            }

            @Override
            public void visit(OptionalFieldAccessExpressionNode optionalFieldAccessExpressionNode) {
                TypeDescKind[] kinds;
                if (((SimpleNameReferenceNode) optionalFieldAccessExpressionNode.fieldName())
                        .name().text().equals("age")) {
                    kinds = new TypeDescKind[]{INT, NIL};
                } else {
                    kinds = new TypeDescKind[]{STRING, ERROR, NIL};
                }

                Optional<TypeSymbol> type = assertType(optionalFieldAccessExpressionNode, model, UNION);
                assertUnionMembers((UnionTypeSymbol) type.get(), kinds);
            }

            @Override
            public void visit(IndexedExpressionNode indexedExpressionNode) {
                if (indexedExpressionNode.keyExpression().size() == 1) {
                    assertType(indexedExpressionNode, model, STRING);
                } else {
                    Optional<TypeSymbol> type = assertType(indexedExpressionNode, model, TYPE_REFERENCE);
                    assertEquals(((TypeReferenceTypeSymbol) type.get()).name(), "Person");
                    assertEquals(((TypeReferenceTypeSymbol) type.get()).typeDescriptor().typeKind(), RECORD);
                }
            }

            @Override
            public void visit(AnnotAccessExpressionNode annotAccessExpressionNode) {
                Optional<TypeSymbol> type = assertType(annotAccessExpressionNode, model, UNION);
                List<TypeSymbol> members = ((UnionTypeSymbol) type.get()).memberTypeDescriptors();
                assertEquals(members.get(0).typeKind(), TYPE_REFERENCE);
                assertEquals(((TypeReferenceTypeSymbol) members.get(0)).name(), "Annot");
                assertEquals(members.get(1).typeKind(), NIL);
            }
        };
    }

    void verifyAssertCount() {
        assertEquals(getAssertCount(), 7);
    }

    private Optional<TypeSymbol> assertType(Node node, SemanticModel model, TypeDescKind typeKind) {
        Optional<TypeSymbol> type = model.type(node);
        assertTrue(type.isPresent());
        assertEquals(type.get().typeKind(), typeKind);
        incrementAssertCount();
        return type;
    }

    private void assertUnionMembers(UnionTypeSymbol type, TypeDescKind... kinds) {
        List<TypeSymbol> members = type.memberTypeDescriptors();
        for (int i = 0; i < kinds.length; i++) {
            assertEquals(members.get(i).typeKind(), kinds[i]);
        }
    }
}
