/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.resourcepath.PathSegmentList;
import io.ballerina.compiler.api.symbols.resourcepath.ResourcePath;
import io.ballerina.compiler.syntax.tree.ClientResourceAccessActionNode;
import io.ballerina.compiler.syntax.tree.ComputedResourceAccessSegmentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static io.ballerina.compiler.api.symbols.SymbolKind.RESOURCE_METHOD;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for looking up a symbol given a client resource access action.
 *
 * @since 2.0.0
 */
@Test
public class SymbolByClientResourceAccessActionTest extends SymbolByNodeTest {

    @Override
    String getTestSourcePath() {
        return "test-src/symbol-by-node/symbol_by_client_resource_access_action_test.bal";
    }

    @Override
    NodeVisitor getNodeVisitor(SemanticModel model) {
        return new NodeVisitor() {

            @Override
            public void visit(ClientResourceAccessActionNode clientResourceAccessActionNode) {
                List<String> expPathSignatures = List.of("path2", "[string id]", "path4");
                ResourceMethodSymbol symbol =
                        (ResourceMethodSymbol) assertSymbol(clientResourceAccessActionNode, model, RESOURCE_METHOD,
                                                                "post");
                symbol.resourcePath();
                assertEquals(symbol.resourcePath().kind(), ResourcePath.Kind.PATH_SEGMENT_LIST);

                PathSegmentList pathSegmentList = (PathSegmentList) symbol.resourcePath();
                assertTrue(pathSegmentList.list().stream()
                        .allMatch(pathSeg -> expPathSignatures.contains(pathSeg.signature())));

                for (Node node: clientResourceAccessActionNode.resourceAccessPath()) {
                    node.accept(this);
                }
            }

            @Override
            public void visit(ComputedResourceAccessSegmentNode computedResourceAccessSegmentNode) {
                List<String> expPathSignatures = List.of("path2", "[string id]", "path4");
                ResourceMethodSymbol symbol =
                        (ResourceMethodSymbol) assertSymbol(computedResourceAccessSegmentNode, model, RESOURCE_METHOD,
                                "post");
                symbol.resourcePath();
                assertEquals(symbol.resourcePath().kind(), ResourcePath.Kind.PATH_SEGMENT_LIST);

                PathSegmentList pathSegmentList = (PathSegmentList) symbol.resourcePath();
                assertTrue(pathSegmentList.list().stream()
                        .allMatch(pathSeg -> expPathSignatures.contains(pathSeg.signature())));
            }
        };
    }

    @Override
    void verifyAssertCount() {
        assertEquals(getAssertCount(), 2);
    }

    private Symbol assertSymbol(Node node, SemanticModel model, SymbolKind kind, String name) {
        Optional<Symbol> symbol = model.symbol(node);
        assertEquals(symbol.get().kind(), kind);
        if (name != null) {
            assertEquals(symbol.get().getName().get(), name);
        }
        incrementAssertCount();
        return symbol.get();
    }
}
