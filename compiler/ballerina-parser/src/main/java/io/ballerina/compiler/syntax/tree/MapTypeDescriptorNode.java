/*
 *  Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
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
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.tree.STNode;

import java.util.Objects;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class MapTypeDescriptorNode extends TypeDescriptorNode {

    public MapTypeDescriptorNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token mapKeywordToken() {
        return childInBucket(0);
    }

    public TypeParameterNode mapTypeParamsNode() {
        return childInBucket(1);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(NodeTransformer<T> visitor) {
        return visitor.transform(this);
    }

    @Override
    protected String[] childNames() {
        return new String[]{
                "mapKeywordToken",
                "mapTypeParamsNode"};
    }

    public MapTypeDescriptorNode modify(
            Token mapKeywordToken,
            TypeParameterNode mapTypeParamsNode) {
        if (checkForReferenceEquality(
                mapKeywordToken,
                mapTypeParamsNode)) {
            return this;
        }

        return NodeFactory.createMapTypeDescriptorNode(
                mapKeywordToken,
                mapTypeParamsNode);
    }

    public MapTypeDescriptorNodeModifier modify() {
        return new MapTypeDescriptorNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class MapTypeDescriptorNodeModifier {
        private final MapTypeDescriptorNode oldNode;
        private Token mapKeywordToken;
        private TypeParameterNode mapTypeParamsNode;

        public MapTypeDescriptorNodeModifier(MapTypeDescriptorNode oldNode) {
            this.oldNode = oldNode;
            this.mapKeywordToken = oldNode.mapKeywordToken();
            this.mapTypeParamsNode = oldNode.mapTypeParamsNode();
        }

        public MapTypeDescriptorNodeModifier withMapKeywordToken(
                Token mapKeywordToken) {
            Objects.requireNonNull(mapKeywordToken, "mapKeywordToken must not be null");
            this.mapKeywordToken = mapKeywordToken;
            return this;
        }

        public MapTypeDescriptorNodeModifier withMapTypeParamsNode(
                TypeParameterNode mapTypeParamsNode) {
            Objects.requireNonNull(mapTypeParamsNode, "mapTypeParamsNode must not be null");
            this.mapTypeParamsNode = mapTypeParamsNode;
            return this;
        }

        public MapTypeDescriptorNode apply() {
            return oldNode.modify(
                    mapKeywordToken,
                    mapTypeParamsNode);
        }
    }
}
