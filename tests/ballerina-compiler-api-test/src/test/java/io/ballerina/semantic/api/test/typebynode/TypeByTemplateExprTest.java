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
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.compiler.api.symbols.TypeDescKind.OBJECT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPE_REFERENCE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.XML_ELEMENT;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for getting the type of template expressions.
 *
 * @since 2.0.0
 */
@Test
public class TypeByTemplateExprTest extends TypeByNodeTest {

    @Override
    String getTestSourcePath() {
        return "test-src/type-by-node/type_by_template_expr.bal";
    }

    @Override
    NodeVisitor getNodeVisitor(SemanticModel model) {
        return new NodeVisitor() {

            @Override
            public void visit(TemplateExpressionNode templateExpressionNode) {
                Optional<TypeSymbol> type = model.type(templateExpressionNode);
                assertTrue(type.isPresent());

                switch (templateExpressionNode.kind()) {
                    case STRING_TEMPLATE_EXPRESSION:
                        assertEquals(type.get().typeKind(), STRING);
                        break;
                    case XML_TEMPLATE_EXPRESSION:
                        assertEquals(type.get().typeKind(), TYPE_REFERENCE);
                        assertEquals(((TypeReferenceTypeSymbol) type.get()).name(), "Element");
                        assertEquals(((TypeReferenceTypeSymbol) type.get()).typeDescriptor().typeKind(), XML_ELEMENT);
                        break;
                    case RAW_TEMPLATE_EXPRESSION:
                        assertEquals(type.get().typeKind(), TYPE_REFERENCE);
                        assertEquals(((TypeReferenceTypeSymbol) type.get()).name(), "RawTemplate");
                        assertEquals(((TypeReferenceTypeSymbol) type.get()).typeDescriptor().typeKind(), OBJECT);
                        break;
                    default:
                        throw new IllegalStateException();
                }

                incrementAssertCount();
            }
        };
    }

    void verifyAssertCount() {
        assertEquals(getAssertCount(), 3);
    }
}
