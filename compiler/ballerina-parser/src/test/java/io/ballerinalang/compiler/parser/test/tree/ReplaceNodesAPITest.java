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

import io.ballerinalang.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerinalang.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.ModulePartNode;
import io.ballerinalang.compiler.syntax.tree.NodeFactory;
import io.ballerinalang.compiler.syntax.tree.ReturnStatementNode;
import io.ballerinalang.compiler.syntax.tree.StatementNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.syntax.tree.Token;
import io.ballerinalang.compiler.text.TextRange;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Contains cases to test replace-node functionality.
 *
 * @since 2.0.0
 */
public class ReplaceNodesAPITest extends AbstractSyntaxTreeAPITest {

    @Test
    public void testReplaceAPIBasic() {
        SyntaxTree syntaxTree = parseFile("replace_node_test_01.bal");
        ModulePartNode modulePartNode = syntaxTree.modulePart();

        // Get the 4th function definition and then get the first statement
        ReturnStatementNode returnStmtNode = (ReturnStatementNode) getStatementInFunction(modulePartNode, 3, 0);

        // Create a new semicolon token with no minutiae
        Token replacement = NodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
        Assert.assertEquals(replacement.toString(), ";");
        Token toBeReplaced = returnStmtNode.semicolonToken();

        // Create a new return statement wit the new semicolon token
        ReturnStatementNode newReturnStmtNode = returnStmtNode.replace(toBeReplaced, replacement);
        Assert.assertEquals(newReturnStmtNode.toString(), "    return a + b;");

        // Create the new ModulePartNode
        ModulePartNode newModulePartNode = modulePartNode.replace(returnStmtNode, newReturnStmtNode);
        ReturnStatementNode newRetStmtInRoot = (ReturnStatementNode) getStatementInFunction(newModulePartNode, 3, 0);
        Assert.assertEquals(newRetStmtInRoot.toString(), "    return a + b;");
    }

    @Test
    public void testReplaceAPISyntaxTree() {
        SyntaxTree syntaxTree = parseFile("replace_node_test_01.bal");
        ModulePartNode modulePartNode = syntaxTree.modulePart();

        // Get the 4th function definition and then get the first statement
        ReturnStatementNode returnStmt = (ReturnStatementNode) getStatementInFunction(modulePartNode, 1, 0);

        // Update the return keyword with no leading minutiae
        Token toBeReplacedReturnKw = returnStmt.returnKeyword();
        Token replacementReturnKw = toBeReplacedReturnKw.modify(NodeFactory.createEmptyMinutiaeList(),
                toBeReplacedReturnKw.trailingMinutiae());

        // Create a new semicolon token with no minutiae
        Token replacementSemicolon = NodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
        Token toBeReplacedSemicolon = returnStmt.semicolonToken();

        ReturnStatementNode replacementReturnStmt = returnStmt.modify().withReturnKeyword(replacementReturnKw)
                .withSemicolonToken(replacementSemicolon).apply();


        // Use the replace node method in the syntax tree to update the semicolon token
        SyntaxTree newSyntaxTree = syntaxTree.replaceNode(returnStmt, replacementReturnStmt);
        ReturnStatementNode newRetStmtInRoot = (ReturnStatementNode) getStatementInFunction(
                newSyntaxTree.modulePart(), 1, 0);

        // Since we removed all minutiae nodes the minutiae width should be zero
        TextRange textRangeWithMinutiae = newRetStmtInRoot.textRangeWithMinutiae();
        int minutiaeWidth = textRangeWithMinutiae.length() - newRetStmtInRoot.textRange().length();
        Assert.assertEquals(minutiaeWidth, 0);
    }

    private StatementNode getStatementInFunction(ModulePartNode modulePartNode, int funcIndex, int stmtIndex) {
        FunctionDefinitionNode funcDefNode = (FunctionDefinitionNode) modulePartNode.members().get(funcIndex);
        FunctionBodyBlockNode funcBody = (FunctionBodyBlockNode) funcDefNode.functionBody();
        return funcBody.statements().get(stmtIndex);
    }
}
