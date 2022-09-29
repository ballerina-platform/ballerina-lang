/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.semantic.api.test.typebynode.newapi;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.StartActionNode;
import io.ballerina.compiler.syntax.tree.WaitActionNode;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static io.ballerina.compiler.api.symbols.TypeDescKind.ERROR;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FUTURE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.UNION;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test
public class TypeByActionTest extends TypeByNodeTest {

    @Override
    String getTestSourcePath() {
        return "test-src/type-by-node/type_by_action.bal";
    }

    @Override
    NodeVisitor getNodeVisitor(SemanticModel model) {

        return new NodeVisitor() {
            @Override
            public void visit(StartActionNode startActionNode) {
                assertType(startActionNode, model, FUTURE);
            }

            @Override
            public void visit(WaitActionNode waitActionNode) {
                Optional<TypeSymbol> typeSymbol = assertType(waitActionNode, model, UNION);
                assertUnionMembers((UnionTypeSymbol) typeSymbol.get(), INT, ERROR);
            }
        };
    }

    @Override
    void verifyAssertCount() {
        assertEquals(getAssertCount(), 2);
    }

    private Optional<TypeSymbol> assertType(Node node, SemanticModel model, TypeDescKind typeKind) {
        Optional<TypeSymbol> type = model.typeOf(node);
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
