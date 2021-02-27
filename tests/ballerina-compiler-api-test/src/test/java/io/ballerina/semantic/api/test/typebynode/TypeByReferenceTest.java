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
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.compiler.api.symbols.TypeDescKind.FLOAT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for getting the type of name references.
 *
 * @since 2.0.0
 */
@Test
public class TypeByReferenceTest extends TypeByNodeTest {

    @Override
    String getTestSourcePath() {
        return "test-src/type-by-node/type_by_reference_test.bal";
    }

    @Override
    NodeVisitor getNodeVisitor(SemanticModel model) {
        return new NodeVisitor() {

            @Override
            public void visit(SimpleNameReferenceNode simpleNameReferenceNode) {
                assertType(simpleNameReferenceNode, model, FLOAT);
            }

            @Override
            public void visit(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
                assertType(qualifiedNameReferenceNode, model, INT);
            }

            @Override
            public void visit(BuiltinSimpleNameReferenceNode builtinSimpleNameReferenceNode) {
                TypeDescKind typeKind;
                switch (builtinSimpleNameReferenceNode.kind()) {
                    case FLOAT_TYPE_DESC:
                        typeKind = FLOAT;
                        break;
                    case INT_TYPE_DESC:
                        typeKind = INT;
                        break;
                    default:
                        throw new IllegalStateException();
                }
                assertType(builtinSimpleNameReferenceNode, model, typeKind);
            }
        };
    }

    void verifyAssertCount() {
        assertEquals(getAssertCount(), 4);
    }

    private void assertType(Node node, SemanticModel model, TypeDescKind typeKind) {
        Optional<TypeSymbol> type = model.type(node);
        assertTrue(type.isPresent());
        assertEquals(type.get().typeKind(), typeKind);
        incrementAssertCount();
    }
}
