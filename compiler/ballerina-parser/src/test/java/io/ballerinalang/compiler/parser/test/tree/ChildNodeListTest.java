/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerinalang.compiler.parser.test.tree;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains cases to test the {@code ChildNodeList} API.
 *
 * @since 1.3.0
 */
public class ChildNodeListTest extends AbstractSyntaxTreeAPITest {

    @Test
    public void testGetChildByIndex() {
        SyntaxTree syntaxTree = parseFile("child_node_list_test_01.bal");
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        FunctionDefinitionNode firstFunctionNode = (FunctionDefinitionNode) modulePartNode.members().get(0);
        int actualChildCount = firstFunctionNode.children().size();
        Assert.assertEquals(actualChildCount, 5);
        // Check signature child count
        FunctionSignatureNode signature = firstFunctionNode.functionSignature();
        int actualSignatureChildCount = signature.children().size();
        Assert.assertEquals(actualSignatureChildCount, 6);

        // The 'public' keyword and the return type desc is missing in the second function
        FunctionDefinitionNode secondFunctionNode = (FunctionDefinitionNode) modulePartNode.members().get(1);
        actualChildCount = secondFunctionNode.children().size();
        Assert.assertEquals(actualChildCount, 4);
        // Check signature child count
        signature = secondFunctionNode.functionSignature();
        actualSignatureChildCount = signature.children().size();
        Assert.assertEquals(actualSignatureChildCount, 5);
    }

    @Test
    public void testIterator() {
        List<SyntaxKind> expectedKinds = new ArrayList<>();
        Collections.addAll(expectedKinds, SyntaxKind.PUBLIC_KEYWORD,
                SyntaxKind.FUNCTION_KEYWORD, SyntaxKind.IDENTIFIER_TOKEN, SyntaxKind.FUNCTION_SIGNATURE,
                SyntaxKind.FUNCTION_BODY_BLOCK);

        SyntaxTree syntaxTree = parseFile("child_node_list_test_01.bal");
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        FunctionDefinitionNode firstFunctionNode = (FunctionDefinitionNode) modulePartNode.members().get(0);
        List<SyntaxKind> actualKinds = new ArrayList<>(firstFunctionNode.children().size());
        for (Node child : firstFunctionNode.children()) {
            actualKinds.add(child.kind());
        }
        Assert.assertEquals(actualKinds, expectedKinds);
    }
}
