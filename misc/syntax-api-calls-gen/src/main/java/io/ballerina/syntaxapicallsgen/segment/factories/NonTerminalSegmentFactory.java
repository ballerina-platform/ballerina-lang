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

import io.ballerina.compiler.syntax.tree.ChildNodeEntry;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.syntaxapicallsgen.segment.NodeFactorySegment;
import io.ballerina.syntaxapicallsgen.segment.Segment;
import io.ballerina.syntaxapicallsgen.segment.factories.cache.ChildNamesCache;
import io.ballerina.syntaxapicallsgen.segment.factories.cache.NodeFactoryMethodCache;
import io.ballerina.syntaxapicallsgen.segment.factories.cache.NodeFactoryMethodReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts {@link NonTerminalNode} into a {@link Segment}.
 *
 * @since 2.0.0
 */
public class NonTerminalSegmentFactory {
    private static final String CREATE_SEP_NODE_LIST_METHOD_NAME = "createSeparatedNodeList";
    private static final String CREATE_NODE_LIST_METHOD_NAME = "createNodeList";
    private static final String CREATE_METHOD_PREFIX = "create";

    private final ChildNamesCache childNamesCache;
    private final NodeFactoryMethodCache methodCache;
    private final NodeSegmentFactory nodeSegmentFactory;

    public NonTerminalSegmentFactory(NodeSegmentFactory nodeSegmentFactory,
                                     ChildNamesCache childNamesCache,
                                     NodeFactoryMethodCache methodCache) {
        this.nodeSegmentFactory = nodeSegmentFactory;
        this.childNamesCache = childNamesCache;
        this.methodCache = methodCache;
    }

    /**
     * Converts every {@link NonTerminalNode} to a Segment.
     * Uses reflection to find the required factory method call in runtime.
     *
     * @param node Non terminal node to convert.
     * @return Created segment.
     */
    public Segment createNonTerminalSegment(NonTerminalNode node) {
        NodeFactoryMethodReference method = getNonTerminalNodeProcessMethod(node);
        NodeFactorySegment root = method.toSegment();

        // Get all the possible child names for the current node type
        List<String> parameterName = childNamesCache.getChildNames(node.getClass().getSimpleName());

        if (method.requiresSyntaxKind()) {
            root.addParameter(SegmentFactory.createSyntaxKindSegment(node.kind()));
        }

        List<ChildNodeEntry> childNodeEntries = new ArrayList<>(node.childEntries());
        int childNodeEntriesIndex = 0; // Current processing childNodeEntry
        for (int i = 0; i < parameterName.size(); i++) {
            String childName = parameterName.get(i);

            if (childNodeEntriesIndex < childNodeEntries.size()) {
                ChildNodeEntry childNodeEntry = childNodeEntries.get(childNodeEntriesIndex);
                if (childNodeEntry.name().equals(childName)) {
                    childNodeEntriesIndex++;
                    root.addParameter(createNodeOrNodeListSegment(childNodeEntry, method, i));
                    continue;
                }
            }
            // Not processed
            root.addParameter(SegmentFactory.createNullSegment());
        }

        return root;
    }


    /**
     * Create a node or a node list segment from a given {@link ChildNodeEntry}.
     * Since NodeList can be either {@link SeparatedNodeList} or {@link NodeList},
     * type required by the method is needed.
     * So the method and the param index is also passed.
     *
     * @param nodeEntry  Entry to convert.
     * @param method     Method call reference of the method that creates parent.
     * @param paramIndex Index of the parameter that conform to the nodeEntry.
     * @return Created segment.
     */
    private Segment createNodeOrNodeListSegment(ChildNodeEntry nodeEntry,
                                                NodeFactoryMethodReference method,
                                                int paramIndex) {
        if (nodeEntry.isList()) {
            return createNodeListSegment(nodeEntry.nodeList(), method, paramIndex);
        } else if (nodeEntry.node().isPresent()) {
            Node childNode = nodeEntry.node().get();
            return nodeSegmentFactory.createNodeSegment(childNode);
        } else {
            return SegmentFactory.createNullSegment();
        }
    }

    /**
     * Creates a Node List segment if the node is actually a NodeList.
     * Parameter type which is required by the parent function call determines whether to
     * create a SeparatedNodeList or a NodeList.
     * This is found via method reference and param index.
     *
     * @param nodes      node list object to convert.
     * @param method     Method call reference of the method that creates parent.
     * @param paramIndex Index of the parameter that conform to the nodeEntry.
     * @return Created segment.
     */
    private Segment createNodeListSegment(NodeList<Node> nodes, NodeFactoryMethodReference method, int paramIndex) {
        // Create a Segment array list from the child nodes.
        ArrayList<Segment> segments = new ArrayList<>();
        nodes.stream().filter(node -> !node.isMissing())
                .forEach(node -> segments.add(nodeSegmentFactory.createNodeSegment(node)));

        String genericType = method.getParameterGeneric(paramIndex);
        String nodeListMethodName = (method.getParameterType(paramIndex) == SeparatedNodeList.class)
                ? CREATE_SEP_NODE_LIST_METHOD_NAME
                : CREATE_NODE_LIST_METHOD_NAME;
        NodeFactorySegment root = SegmentFactory.createNodeFactorySegment(nodeListMethodName, genericType);
        segments.forEach(root::addParameter);
        return root;
    }

    /**
     * Retrieves the method to process the said node.
     * If the node is a IdentifierToken then it searches for createIdentifierToken.
     *
     * @param node Node to find the creation method reference.
     * @return Method reference of the method that creates the node.
     */
    private NodeFactoryMethodReference getNonTerminalNodeProcessMethod(NonTerminalNode node) {
        return methodCache.getMethod(CREATE_METHOD_PREFIX + node.getClass().getSimpleName());
    }
}
