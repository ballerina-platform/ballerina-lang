/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.syntaxapicallsgen.segment.factories;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.syntaxapicallsgen.SyntaxApiCallsGenException;
import io.ballerina.syntaxapicallsgen.config.SyntaxApiCallsGenConfig;
import io.ballerina.syntaxapicallsgen.segment.Segment;
import io.ballerina.syntaxapicallsgen.segment.factories.cache.ChildNamesCache;
import io.ballerina.syntaxapicallsgen.segment.factories.cache.NodeFactoryMethodCache;

/**
 * Handles {@link Node} to {@link Segment} conversion.
 *
 * @since 2.0.0
 */
public class NodeSegmentFactory {
    private final NonTerminalSegmentFactory nonTerminalSegmentFactory;
    private final TokenSegmentFactory tokenSegmentFactory;

    private NodeSegmentFactory(ChildNamesCache childNamesCache,
                               NodeFactoryMethodCache methodCache,
                               boolean ignoreMinutiae) {
        this.nonTerminalSegmentFactory =
                new NonTerminalSegmentFactory(this, childNamesCache, methodCache);
        this.tokenSegmentFactory = new TokenSegmentFactory(ignoreMinutiae);
    }

    /**
     * Use the parameter name from the config to create a cache and the factory.
     *
     * @param config {@link SyntaxApiCallsGenConfig} object to load configurations.
     * @return Created factory.
     */
    public static NodeSegmentFactory fromConfig(SyntaxApiCallsGenConfig config) {
        return new NodeSegmentFactory(
                ChildNamesCache.fromConfig(config),
                NodeFactoryMethodCache.create(),
                config.ignoreMinutiae()
        );
    }

    /**
     * Coverts a {@link Node} to a {@link Segment} (NonTerminal or a Terminal/Token).
     *
     * @param node Parent node of the subtree to convert.
     * @return Parent segment of the converted subtree.
     */
    public Segment createNodeSegment(Node node) {
        if (node == null) {
            return SegmentFactory.createNullSegment();
        } else if (node instanceof Token token) {
            return tokenSegmentFactory.createTokenSegment(token);
        } else if (node instanceof NonTerminalNode nonTerminalNode) {
            return nonTerminalSegmentFactory.createNonTerminalSegment(nonTerminalNode);
        } else {
            throw new SyntaxApiCallsGenException("Expected non terminal or token. " +
                    "Found unexpected node type for: " + node);
        }
    }
}
