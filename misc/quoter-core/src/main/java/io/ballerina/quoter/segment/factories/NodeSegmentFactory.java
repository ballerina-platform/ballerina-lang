/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.quoter.segment.factories;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.quoter.QuoterException;
import io.ballerina.quoter.config.QuoterConfig;
import io.ballerina.quoter.segment.Segment;
import io.ballerina.quoter.segment.factories.cache.ChildNamesCache;
import io.ballerina.quoter.segment.factories.cache.NodeFactoryMethodCache;

/**
 * Handles {@link Node} to {@link Segment} conversion.
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
     * @param config {@link QuoterConfig} object to load configurations.
     * @return Created factory.
     */
    public static NodeSegmentFactory fromConfig(QuoterConfig config) {
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
        } else if (node instanceof Token) {
            return tokenSegmentFactory.createTokenSegment((Token) node);
        } else if (node instanceof NonTerminalNode) {
            return nonTerminalSegmentFactory.createNonTerminalSegment((NonTerminalNode) node);
        } else {
            throw new QuoterException("Expected non terminal or token. " +
                    "Found unexpected node type for: " + node);
        }
    }
}
