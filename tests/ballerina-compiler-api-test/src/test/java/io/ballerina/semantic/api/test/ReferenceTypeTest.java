/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.semantic.api.test;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.symbols.BallerinaTypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TableConstructorExpressionNode;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
/**
 * This class contains constructor's type reference type related test cases.
 *
 * @since 2201.3.0
 */
public class ReferenceTypeTest {

    @Test
    public void testLookup() {
        Project project = BCompileUtil.loadProject("test-src/reference_type_test.bal");
        Package currentPackage = project.currentPackage();
        ModuleId defaultModuleId = currentPackage.getDefaultModule().moduleId();
        DocumentId docId = currentPackage.getDefaultModule().documentIds().iterator().next();
        SyntaxTree syntaxTree = currentPackage.getDefaultModule().document(docId).syntaxTree();
        SemanticModel model = currentPackage.getCompilation().getSemanticModel(defaultModuleId);
        syntaxTree.rootNode().accept(getNodeVisitor(model));
    }

    NodeVisitor getNodeVisitor(SemanticModel model) {
        return new NodeVisitor() {

            @Override
            public void visit(TableConstructorExpressionNode tableConstructorExpressionNode) {
                assertType(tableConstructorExpressionNode, model, "BarTable");
            }

            @Override
            public void visit(ImplicitNewExpressionNode implicitNewExpressionNode) {
                assertType(implicitNewExpressionNode, model, "ListenerRef");
            }
        };
    }

    private void assertType(Node node, SemanticModel model, String referenceTypeName) {
        Optional<TypeSymbol> type = model.typeOf(node);
        assertTrue(type.isPresent());
        assertEquals((((BallerinaTypeReferenceTypeSymbol) type.get()).getBType().toString()), referenceTypeName);
    }
}
