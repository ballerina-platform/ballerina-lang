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

import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains test cases to test {@code SyntaxNodeVisitor} functionality.
 *
 * @since 1.3.0
 */
public class NodeVisitorTest extends AbstractSyntaxTreeAPITest {

    @Test
    public void testTokenTraversal() {
        SyntaxKind[] expectedKinds = {SyntaxKind.PUBLIC_KEYWORD, SyntaxKind.FUNCTION_KEYWORD,
                SyntaxKind.IDENTIFIER_TOKEN, SyntaxKind.OPEN_PAREN_TOKEN, SyntaxKind.CLOSE_PAREN_TOKEN,
                SyntaxKind.OPEN_BRACE_TOKEN, SyntaxKind.INT_KEYWORD, SyntaxKind.IDENTIFIER_TOKEN,
                SyntaxKind.EQUAL_TOKEN, SyntaxKind.IDENTIFIER_TOKEN, SyntaxKind.PLUS_TOKEN,
                SyntaxKind.IDENTIFIER_TOKEN, SyntaxKind.SEMICOLON_TOKEN, SyntaxKind.CLOSE_BRACE_TOKEN,
                SyntaxKind.EOF_TOKEN};

        SyntaxTree syntaxTree = parseFile("token_traversal.bal");
        TokenVisitor tokenVisitor = new TokenVisitor();
        syntaxTree.rootNode().accept(tokenVisitor);
        SyntaxKind[] actualKinds = tokenVisitor.tokenList.toArray(new SyntaxKind[0]);

        Assert.assertEquals(actualKinds, expectedKinds);
    }

    @Test
    public void testAssignmentStmtNodeVisit() {
        SyntaxTree syntaxTree = parseFile("assignment_stmt_traversal.bal");
        AssignmentStmtVisitor visitor = new AssignmentStmtVisitor();
        syntaxTree.rootNode().accept(visitor);
        int actualStmtCount = visitor.stmtList.size();

        Assert.assertEquals(actualStmtCount, 12);
    }

    /**
     * A simple implementation of the {@code SyntaxNodeVisitor} that collects {@code Token} instances.
     *
     * @since 1.3.0
     */
    private static class TokenVisitor extends NodeVisitor {
        List<SyntaxKind> tokenList = new ArrayList<>();

        public void visit(Token token) {
            tokenList.add(token.kind());
        }
    }

    /**
     * A simple implementation of the {@code SyntaxNodeVisitor} that collects {@code AssignmentStatement} nodes.
     *
     * @since 1.3.0
     */
    private static class AssignmentStmtVisitor extends NodeVisitor {
        List<AssignmentStatementNode> stmtList = new ArrayList<>();

        public void visit(AssignmentStatementNode assignmentStatement) {
            stmtList.add(assignmentStatement);
        }
    }
}
