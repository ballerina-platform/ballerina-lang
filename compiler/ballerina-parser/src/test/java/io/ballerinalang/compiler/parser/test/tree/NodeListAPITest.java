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

import io.ballerinalang.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerinalang.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.IdentifierToken;
import io.ballerinalang.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.ModulePartNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NodeFactory;
import io.ballerinalang.compiler.syntax.tree.NodeList;
import io.ballerinalang.compiler.syntax.tree.StatementNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerinalang.compiler.syntax.tree.VariableDeclarationNode;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.function.Function;

/**
 * Contains cases to test the {@code NodeList} API.
 *
 * @since 2.0.0
 */
public class NodeListAPITest extends AbstractSyntaxTreeAPITest {

    @Test(description = "Tests the NodeList.get(int index) method")
    public void testGetByIndexMethod() {
        ModulePartNode modulePartNode = parseFile("node_list_test_01.bal").modulePart();
        NodeList<ModuleMemberDeclarationNode> members = modulePartNode.members();
        ModuleMemberDeclarationNode member1 = members.get(1);

        // If you invoke the get(0) again, you should get the same instance
        // External tree nodes are created on-demand and only once.
        Assert.assertEquals(members.get(1), member1, "NodeList.get(index) should give the same instance");
        Assert.assertEquals(members.size(), 3);

        // Test getting the first member
        testTree(members.get(0), "node_list_test_01.json");

        // Test getting a middle member
        testTree(members.get(1), "node_list_test_02.json");

        // Test getting the last member
        testTree(members.get(2), "node_list_test_03.json");
    }

    @Test(description = "Tests the NodeList.add(int index, T node) method")
    public void testAddWithIndexMethod() {
        ModulePartNode modulePartNode = parseFile("node_list_test_01.bal").modulePart();
        NodeList<ModuleMemberDeclarationNode> members = modulePartNode.members();
        FunctionDefinitionNode funcDefNode = (FunctionDefinitionNode) members.get(0);
        FunctionBodyBlockNode funcBody = (FunctionBodyBlockNode) funcDefNode.functionBody();
        NodeList<StatementNode> statementNodes = funcBody.statements();

        // Test adding a node to the 0th index of the list
        testStmtNodeListOperation(funcBody, statementNodes, "var1",
                stmt -> statementNodes.add(0, stmt), "node_list_test_04.json");

        // Test adding a node to an index in the middle of the list
        testStmtNodeListOperation(funcBody, statementNodes, "var2",
                stmt -> statementNodes.add(4, stmt), "node_list_test_05.json");

        // Test adding a node to the last index of the list
        testStmtNodeListOperation(funcBody, statementNodes, "var3",
                stmt -> statementNodes.add(statementNodes.size(), stmt), "node_list_test_06.json");

        // Test adding to a empty list
        NodeList<StatementNode> emptyStmtList = NodeFactory.createEmptyNodeList();
        testStmtNodeListOperation(funcBody, statementNodes, "var4",
                stmt -> emptyStmtList.add(0, stmt), "node_list_test_08.json");

    }

    @Test(description = "Tests the NodeList.add(T node) method")
    public void testAddMethod() {
        ModulePartNode modulePartNode = parseFile("node_list_test_01.bal").modulePart();
        NodeList<ModuleMemberDeclarationNode> members = modulePartNode.members();
        FunctionDefinitionNode funcDefNode = (FunctionDefinitionNode) members.get(0);
        FunctionBodyBlockNode funcBody = (FunctionBodyBlockNode) funcDefNode.functionBody();
        NodeList<StatementNode> statementNodes = funcBody.statements();

        testStmtNodeListOperation(funcBody, statementNodes, "var1",
                statementNodes::add, "node_list_test_07.json");
    }

    protected SyntaxTree parseFile(String sourceFileName) {
        return super.parseFile(Paths.get("node_list_api").resolve(sourceFileName));
    }

    protected void testTree(Node node, String jsonFileName) {
        super.testTree(node, Paths.get("node_list_api").resolve(jsonFileName));
    }

    private StatementNode dupLocalVarDeclStmt(VariableDeclarationNode srcDeclNode, String varName) {
        TypedBindingPatternNode typedBindingPattern = srcDeclNode.typedBindingPattern();
        BuiltinSimpleNameReferenceNode srcTypeNameNode =
                (BuiltinSimpleNameReferenceNode) typedBindingPattern.typeDescriptor();
        CaptureBindingPatternNode bindingPattern = (CaptureBindingPatternNode) typedBindingPattern.bindingPattern();
        IdentifierToken srcVariableNameToken = (IdentifierToken) (bindingPattern).variableName();

        // Create new typename with the same formatting as the original one
        BuiltinSimpleNameReferenceNode typeNameNode = srcTypeNameNode.modify()
                .withName(NodeFactory.createToken(SyntaxKind.STRING_KEYWORD))
                .apply();
        // Create a variable name token
        IdentifierToken variableNameToken = srcVariableNameToken.modify(varName);

        CaptureBindingPatternNode newCaptureBP = NodeFactory.createCaptureBindingPatternNode(variableNameToken);
        TypedBindingPatternNode newTypedBP = NodeFactory.createTypedBindingPatternNode(typeNameNode, newCaptureBP);

        return srcDeclNode.modify()
                .withTypedBindingPattern(newTypedBP)
                .apply();
    }

    private void testStmtNodeListOperation(FunctionBodyBlockNode funcBody,
                                           NodeList<StatementNode> statementNodes,
                                           String varName,
                                           Function<StatementNode, NodeList<StatementNode>> nodeListFunction,
                                           String testJSONFileName) {
        StatementNode newVarDeclStmt = dupLocalVarDeclStmt((VariableDeclarationNode) statementNodes.get(0), varName);
        NodeList<StatementNode> statementNodes3 = nodeListFunction.apply(newVarDeclStmt);
        FunctionBodyBlockNode newFuncBody = funcBody.modify().withStatements(statementNodes3).apply();
        testTree(newFuncBody, testJSONFileName);
    }
}
