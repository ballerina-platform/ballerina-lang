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
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class BallerinaNameReferenceNode extends DocumentationNode {

    public BallerinaNameReferenceNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Optional<Token> referenceType() {
        return optionalChildInBucket(0);
    }

    public Token startBacktick() {
        return childInBucket(1);
    }

    public Node nameReference() {
        return childInBucket(2);
    }

    public Token endBacktick() {
        return childInBucket(3);
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
                "referenceType",
                "startBacktick",
                "nameReference",
                "endBacktick"};
    }

    public BallerinaNameReferenceNode modify(
            Token referenceType,
            Token startBacktick,
            Node nameReference,
            Token endBacktick) {
        if (checkForReferenceEquality(
                referenceType,
                startBacktick,
                nameReference,
                endBacktick)) {
            return this;
        }

        return NodeFactory.createBallerinaNameReferenceNode(
                referenceType,
                startBacktick,
                nameReference,
                endBacktick);
    }

    public BallerinaNameReferenceNodeModifier modify() {
        return new BallerinaNameReferenceNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class BallerinaNameReferenceNodeModifier {
        private final BallerinaNameReferenceNode oldNode;
        private Token referenceType;
        private Token startBacktick;
        private Node nameReference;
        private Token endBacktick;

        public BallerinaNameReferenceNodeModifier(BallerinaNameReferenceNode oldNode) {
            this.oldNode = oldNode;
            this.referenceType = oldNode.referenceType().orElse(null);
            this.startBacktick = oldNode.startBacktick();
            this.nameReference = oldNode.nameReference();
            this.endBacktick = oldNode.endBacktick();
        }

        public BallerinaNameReferenceNodeModifier withReferenceType(
                Token referenceType) {
            this.referenceType = referenceType;
            return this;
        }

        public BallerinaNameReferenceNodeModifier withStartBacktick(
                Token startBacktick) {
            Objects.requireNonNull(startBacktick, "startBacktick must not be null");
            this.startBacktick = startBacktick;
            return this;
        }

        public BallerinaNameReferenceNodeModifier withNameReference(
                Node nameReference) {
            Objects.requireNonNull(nameReference, "nameReference must not be null");
            this.nameReference = nameReference;
            return this;
        }

        public BallerinaNameReferenceNodeModifier withEndBacktick(
                Token endBacktick) {
            Objects.requireNonNull(endBacktick, "endBacktick must not be null");
            this.endBacktick = endBacktick;
            return this;
        }

        public BallerinaNameReferenceNode apply() {
            return oldNode.modify(
                    referenceType,
                    startBacktick,
                    nameReference,
                    endBacktick);
        }
    }
}
