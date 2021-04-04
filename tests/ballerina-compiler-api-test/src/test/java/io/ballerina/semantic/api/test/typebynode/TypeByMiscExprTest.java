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
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.ConditionalExpressionNode;
import io.ballerina.compiler.syntax.tree.LetExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.TypeTestExpressionNode;
import io.ballerina.compiler.syntax.tree.UnaryExpressionNode;
import io.ballerina.compiler.syntax.tree.XMLFilterExpressionNode;
import io.ballerina.compiler.syntax.tree.XMLStepExpressionNode;
import org.ballerinalang.model.tree.OperatorKind;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.compiler.api.symbols.TypeDescKind.BOOLEAN;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.OBJECT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static io.ballerina.compiler.api.symbols.TypeDescKind.XML;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for getting the type of misc. exprs.
 *
 * @since 2.0.0
 */
@Test
public class TypeByMiscExprTest extends TypeByNodeTest {

    @Override
    String getTestSourcePath() {
        return "test-src/type-by-node/type_by_misc_exprs.bal";
    }

    @Override
    NodeVisitor getNodeVisitor(SemanticModel model) {
        return new NodeVisitor() {
            @Override
            public void visit(UnaryExpressionNode unaryExpressionNode) {
                assertType(unaryExpressionNode, model, INT);
            }

            @Override
            public void visit(BinaryExpressionNode binaryExpressionNode) {
                TypeDescKind typeKind;
                switch (OperatorKind.valueFrom(binaryExpressionNode.operator().text())) {
                    case SUB:
                    case MUL:
                    case ADD:
                    case DIV:
                        typeKind = INT;
                        break;
                    case GREATER_EQUAL:
                    case NOT_EQUAL:
                    case EQUAL:
                    case AND:
                        typeKind = BOOLEAN;
                        break;
                    case CLOSED_RANGE:
                        typeKind = OBJECT;
                        break;
                    default:
                        throw new IllegalStateException();
                }

                assertType(binaryExpressionNode, model, typeKind);
                binaryExpressionNode.rhsExpr().accept(this);
                binaryExpressionNode.lhsExpr().accept(this);
            }

            @Override
            public void visit(TypeTestExpressionNode typeTestExpressionNode) {
                assertType(typeTestExpressionNode, model, BOOLEAN);
            }

            @Override
            public void visit(ConditionalExpressionNode conditionalExpressionNode) {
                assertType(conditionalExpressionNode, model, STRING);
                conditionalExpressionNode.lhsExpression().accept(this);
            }

            @Override
            public void visit(LetExpressionNode letExpressionNode) {
                assertType(letExpressionNode, model, INT);
            }

            @Override
            public void visit(XMLFilterExpressionNode xmlFilterExpressionNode) {
                assertType(xmlFilterExpressionNode, model, XML);
            }

            @Override
            public void visit(XMLStepExpressionNode xmlStepExpressionNode) {
                assertType(xmlStepExpressionNode, model, XML);
            }
        };
    }

    void verifyAssertCount() {
        assertEquals(getAssertCount(), 16);
    }

    private void assertType(Node node, SemanticModel model, TypeDescKind typeKind) {
        Optional<TypeSymbol> type = model.type(node);
        assertTrue(type.isPresent());
        assertEquals(type.get().typeKind(), typeKind);
        incrementAssertCount();
    }
}
