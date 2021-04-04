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
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.TypeCastExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeTestExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeofExpressionNode;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.compiler.api.symbols.TypeDescKind.BOOLEAN;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPEDESC;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for getting the type of typeof and type cast exprs.
 *
 * @since 2.0.0
 */
@Test
public class TypeByTypeExprTest extends TypeByNodeTest {

    @Override
    String getTestSourcePath() {
        return "test-src/type-by-node/type_by_type_exprs.bal";
    }

    @Override
    NodeVisitor getNodeVisitor(SemanticModel model) {
        return new NodeVisitor() {

            @Override
            public void visit(TypeCastExpressionNode typeCastExpressionNode) {
                assertType(typeCastExpressionNode, model, STRING);
            }

            @Override
            public void visit(TypeofExpressionNode typeofExpressionNode) {
                assertType(typeofExpressionNode, model, TYPEDESC);
            }

            @Override
            public void visit(TypeTestExpressionNode typeTestExpressionNode) {
                assertType(typeTestExpressionNode, model, BOOLEAN);
            }
        };
    }

    void verifyAssertCount() {
        assertEquals(getAssertCount(), 3);
    }

    private Optional<TypeSymbol> assertType(Node node, SemanticModel model, TypeDescKind typeKind) {
        Optional<TypeSymbol> type = model.type(node);
        assertTrue(type.isPresent());
        assertEquals(type.get().typeKind(), typeKind);
        incrementAssertCount();
        return type;
    }
}
