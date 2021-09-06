/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.semantic.api.test.symbolbynode;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AbsResourcePathAttachPoint;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.ServiceAttachPoint;
import io.ballerina.compiler.api.symbols.ServiceAttachPointKind;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.PathSegmentList;
import io.ballerina.compiler.api.symbols.resourcepath.ResourcePath;
import io.ballerina.compiler.api.symbols.resourcepath.util.NamedPathSegment;
import io.ballerina.compiler.api.symbols.resourcepath.util.PathSegment;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.METHOD;
import static io.ballerina.compiler.api.symbols.SymbolKind.PATH_PARAMETER;
import static io.ballerina.compiler.api.symbols.SymbolKind.RESOURCE_METHOD;
import static io.ballerina.compiler.api.symbols.SymbolKind.SERVICE_DECLARATION;
import static io.ballerina.compiler.api.symbols.SymbolKind.TYPE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for looking up a symbol given an isolated service decl.
 *
 * @since 2.0.0
 */
@Test
public class SymbolByIsolatedServiceDeclTest extends SymbolByNodeTest {

    @Override
    String getTestSourcePath() {
        return "test-src/symbol-by-node/symbol_by_isolated_service_decl_test.bal";
    }

    @Override
    NodeVisitor getNodeVisitor(SemanticModel model) {
        return new NodeVisitor() {

            @Override
            public void visit(ServiceDeclarationNode serviceDeclarationNode) {
                ServiceDeclarationSymbol symbol =
                        (ServiceDeclarationSymbol) assertSymbol(serviceDeclarationNode, model, SERVICE_DECLARATION,
                                null);

                ServiceAttachPoint attachPoint = symbol.attachPoint().get();
                assertEquals(attachPoint.kind(), ServiceAttachPointKind.ABSOLUTE_RESOURCE_PATH);
                assertEquals(((AbsResourcePathAttachPoint) attachPoint).segments().toString(), "[foo, bar]");

                TypeSymbol type = symbol.typeDescriptor().get();
                assertEquals(type.typeKind(), TypeDescKind.TYPE_REFERENCE);
                assertEquals(((TypeReferenceTypeSymbol) type).name(), "HelloWorld");

                for (Node member : serviceDeclarationNode.members()) {
                    member.accept(this);
                }
            }

            @Override
            public void visit(ClassDefinitionNode classDefinitionNode) {
                // Do nothing. This is to prevent traversing the listener class.
            }

            @Override
            public void visit(ObjectFieldNode objectFieldNode) {
                assertSymbol(objectFieldNode, model, CLASS_FIELD, objectFieldNode.fieldName().text());
                assertSymbol(objectFieldNode.fieldName(), model, CLASS_FIELD, objectFieldNode.fieldName().text());
            }

            @Override
            public void visit(FunctionDefinitionNode functionDefinitionNode) {
                if (functionDefinitionNode.kind() == SyntaxKind.RESOURCE_ACCESSOR_DEFINITION) {
                    ResourceMethodSymbol method =
                            (ResourceMethodSymbol) assertSymbol(functionDefinitionNode, model, RESOURCE_METHOD,
                                    functionDefinitionNode.functionName().text());

                    if (functionDefinitionNode.functionName().text().equals("delete")) {
                        visitSyntaxNode(functionDefinitionNode.functionSignature());
                        return;
                    }

                    assertEquals(method.signature(),
                            "isolated resource function get greet/[int x]/hello/[float y]/[string... rest] () " +
                                    "returns json");
                    assertEquals(method.resourcePath().kind(), ResourcePath.Kind.PATH_SEGMENT_LIST);
                    assertEquals(method.resourcePath().signature(),
                            "greet/[int x]/hello/[float y]/[string... rest]");

                    PathSegmentList resourcePath = (PathSegmentList) method.resourcePath();

                    List<PathParameterSymbol> pathParams = resourcePath.pathParameters();
                    assertEquals(pathParams.get(0).typeDescriptor().typeKind(), TypeDescKind.INT);
                    assertEquals(pathParams.get(0).getName().get(), "x");
                    assertEquals(pathParams.get(1).typeDescriptor().typeKind(), TypeDescKind.FLOAT);
                    assertEquals(pathParams.get(1).getName().get(), "y");

                    assertEquals(resourcePath.pathRestParameter().get().getName().get(), "rest");
                    assertEquals(resourcePath.pathRestParameter().get().typeDescriptor().typeKind(),
                            TypeDescKind.ARRAY);

                    List<PathSegment> segments = resourcePath.list();
                    assertEquals(segments.size(), 5);
                    assertEquals(segments.get(0).pathSegmentKind(), PathSegment.Kind.NAMED_SEGMENT);
                    assertEquals(((NamedPathSegment) segments.get(0)).name(), "greet");
                    assertEquals(segments.get(2).pathSegmentKind(), PathSegment.Kind.NAMED_SEGMENT);
                    assertEquals(((NamedPathSegment) segments.get(2)).name(), "hello");

                    assertEquals(segments.get(1), pathParams.get(0));
                    assertEquals(segments.get(3), pathParams.get(1));
                    assertEquals(segments.get(4), resourcePath.pathRestParameter().get());

                    for (Node child : functionDefinitionNode.children()) {
                        child.accept(this);
                    }
                }

                if (functionDefinitionNode.kind() == SyntaxKind.OBJECT_METHOD_DEFINITION) {
                    assertSymbol(functionDefinitionNode, model, METHOD, functionDefinitionNode.functionName().text());
                }
            }

            @Override
            public void visit(ResourcePathParameterNode resourcePathParameterNode) {
                String pathParamName = resourcePathParameterNode.paramName().text();
                PathParameterSymbol pathParam =
                        (PathParameterSymbol) assertSymbol(resourcePathParameterNode, model, PATH_PARAMETER,
                                pathParamName);

                switch (pathParamName) {
                    case "x":
                        assertPathParam(pathParam, pathParamName, PathSegment.Kind.PATH_PARAMETER, TypeDescKind.INT,
                                "[int x]");
                        return;
                    case "y":
                        assertPathParam(pathParam, pathParamName, PathSegment.Kind.PATH_PARAMETER, TypeDescKind.FLOAT,
                                "[float y]");
                        return;
                    case "rest":
                        assertPathParam(pathParam, pathParamName, PathSegment.Kind.PATH_REST_PARAMETER,
                                TypeDescKind.ARRAY, "[string... rest]");
                        assertEquals(((ArrayTypeSymbol) pathParam.typeDescriptor()).memberTypeDescriptor().typeKind(),
                                TypeDescKind.STRING);
                        return;
                }

                throw new AssertionError("Unexpected path param: " + pathParam.getName().get());
            }

            @Override
            public void visit(RecordFieldNode recordFieldNode) {
                if (recordFieldNode.typeName().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                    TypeReferenceTypeSymbol type = (TypeReferenceTypeSymbol) assertSymbol(recordFieldNode.typeName(),
                            model, TYPE, "Error");
                    assertEquals(type.typeDescriptor().typeKind(), TypeDescKind.RECORD);
                }
            }
        };
    }

    @Override
    void verifyAssertCount() {
        assertEquals(getAssertCount(), 11);
    }

    private Symbol assertSymbol(Node node, SemanticModel model, SymbolKind kind, String name) {
        Optional<Symbol> symbol = model.symbol(node);
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), kind);
        if (name != null) {
            assertEquals(symbol.get().getName().get(), name);
        }
        incrementAssertCount();
        return symbol.get();
    }

    private void assertPathParam(PathParameterSymbol pathParam, String name, PathSegment.Kind kind,
                                 TypeDescKind typeKind, String signature) {
        assertTrue(pathParam.getName().isPresent());
        assertEquals(pathParam.getName().get(), name);
        assertEquals(pathParam.kind(), PATH_PARAMETER);
        assertEquals(pathParam.pathSegmentKind(), kind);
        assertEquals(pathParam.typeDescriptor().typeKind(), typeKind);
        assertEquals(pathParam.signature(), signature);
    }
}
