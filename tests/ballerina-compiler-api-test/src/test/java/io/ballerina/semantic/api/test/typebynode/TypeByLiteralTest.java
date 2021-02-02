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
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.ByteArrayLiteralNode;
import io.ballerina.compiler.syntax.tree.NilLiteralNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.api.symbols.TypeDescKind.ARRAY;
import static io.ballerina.compiler.api.symbols.TypeDescKind.BOOLEAN;
import static io.ballerina.compiler.api.symbols.TypeDescKind.BYTE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.DECIMAL;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FLOAT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.NIL;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for getting the type of basic literals.
 *
 * @since 2.0.0
 */
@Test
public class TypeByLiteralTest extends TypeByNodeTest {

    @Override
    String getTestSourcePath() {
        return "test-src/type-by-node/type_by_literal.bal";
    }

    @Override
    NodeVisitor getNodeVisitor(SemanticModel model) {
        Map<String, TypeDescKind> literalKinds = new HashMap<>();
        literalKinds.put("5", INT);
        literalKinds.put("12.34", FLOAT);
        literalKinds.put("34.5d", DECIMAL);
        literalKinds.put("true", BOOLEAN);
        literalKinds.put("\"foo\"", STRING);
        literalKinds.put("null", NIL);
        literalKinds.put("base64 `SGVsbG8gQmFsbGVyaW5h`", ARRAY);

        return new NodeVisitor() {

            @Override
            public void visit(NilLiteralNode nilLiteralNode) {
                assertType(nilLiteralNode, model, NIL);
            }

            @Override
            public void visit(ByteArrayLiteralNode byteArrayLiteralNode) {
                Optional<TypeSymbol> type = assertType(byteArrayLiteralNode, model, ARRAY);
                assertEquals(((ArrayTypeSymbol) type.get()).memberTypeDescriptor().typeKind(), BYTE);
                incrementAssertCount();
            }

            @Override
            public void visit(BasicLiteralNode basicLiteralNode) {
                assertType(basicLiteralNode, model, literalKinds.get(basicLiteralNode.literalToken().text()));
            }
        };
    }

    void verifyAssertCount() {
        assertEquals(getAssertCount(), 9);
    }

    private Optional<TypeSymbol> assertType(Node node, SemanticModel model, TypeDescKind typeKind) {
        Optional<TypeSymbol> type = model.type(node);
        assertTrue(type.isPresent());
        assertEquals(type.get().typeKind(), typeKind);
        incrementAssertCount();
        return type;
    }
}
