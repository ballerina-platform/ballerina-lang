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

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Contains test cases to test {@code SyntaxNodeTransformer} functionality.
 *
 * @since 1.3.0
 */
public class NodeTransformerTest extends AbstractSyntaxTreeAPITest {

    @Test(description = "This test case proves that SyntaxNodeTransformer traverses through " +
            "the whole tree from the given node.")
    public void testTransformerByCountingTokens() {
        SyntaxTree syntaxTree = parseFile("token_counter.bal");
        TokenCounter tokenCounter = new TokenCounter();
        int actualNoOfTokens = syntaxTree.rootNode().apply(tokenCounter);
        int expectedNoOfTokens = 33;

        Assert.assertEquals(actualNoOfTokens, expectedNoOfTokens);
    }

    @Test
    public void testTransformerByFindingDeepestToken() {
        SyntaxTree syntaxTree = parseFile("deepest_token.bal");
        DeepestTokenFinder deepestTokenFinder = new DeepestTokenFinder();
        TokenWrapper deepestToken = syntaxTree.rootNode().apply(deepestTokenFinder);

        Assert.assertEquals(deepestToken.depth, 11);
        Assert.assertEquals(deepestToken.token.toString(), "a ");
    }

    /**
     * An implementation of {@code SyntaxNodeTransformer} that counts tokens
     * in the syntax tree.
     *
     * @since 1.3.0
     */
    private static class TokenCounter extends NodeTransformer<Integer> {

        @Override
        public Integer transform(Token token) {
            return 1;
        }

        @Override
        protected Integer transformSyntaxNode(Node node) {
            if (node instanceof Token) {
                return node.apply(this);
            }

            int tokenCount = 0;
            NonTerminalNode nonTerminalNode = (NonTerminalNode) node;
            for (Node child : nonTerminalNode.children()) {
                tokenCount += child.apply(this);
            }
            return tokenCount;
        }
    }

    /**
     * Wraps a {@code Token} with it's depth.
     *
     * @since 1.3.0
     */
    private static class TokenWrapper {
        Token token;
        int depth;

        TokenWrapper(Token token) {
            this.token = token;
            this.depth = 0;
        }
    }

    /**
     * An implementation of {@code SyntaxNodeTransformer} that finds the
     * deepest token in the syntax tree.
     *
     * @since 1.3.0
     */
    private static class DeepestTokenFinder extends NodeTransformer<TokenWrapper> {

        @Override
        public TokenWrapper transform(Token token) {
            return new TokenWrapper(token);
        }

        @Override
        protected TokenWrapper transformSyntaxNode(Node node) {
            if (node instanceof Token) {
                return node.apply(this);
            }

            TokenWrapper deepestToken = null;
            NonTerminalNode nonTerminalNode = (NonTerminalNode) node;
            for (Node child : nonTerminalNode.children()) {
                TokenWrapper tokenWrapper = child.apply(this);
                if (tokenWrapper == null) {
                    continue;
                }

                tokenWrapper.depth++;
                if (deepestToken == null || deepestToken.depth < tokenWrapper.depth) {
                    deepestToken = tokenWrapper;
                }
            }
            return deepestToken;
        }
    }
}
