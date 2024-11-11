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
public class InlineCodeReferenceNode extends DocumentationNode {

    public InlineCodeReferenceNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token startBacktick() {
        return childInBucket(0);
    }

    public Token codeReference() {
        return childInBucket(1);
    }

    public Token endBacktick() {
        return childInBucket(2);
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
                "startBacktick",
                "codeReference",
                "endBacktick"};
    }

    public InlineCodeReferenceNode modify(
            Token startBacktick,
            Token codeReference,
            Token endBacktick) {
        if (checkForReferenceEquality(
                startBacktick,
                codeReference,
                endBacktick)) {
            return this;
        }

        return NodeFactory.createInlineCodeReferenceNode(
                startBacktick,
                codeReference,
                endBacktick);
    }

    public InlineCodeReferenceNodeModifier modify() {
        return new InlineCodeReferenceNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class InlineCodeReferenceNodeModifier {
        private final InlineCodeReferenceNode oldNode;
        private Token startBacktick;
        private Token codeReference;
        private Token endBacktick;

        public InlineCodeReferenceNodeModifier(InlineCodeReferenceNode oldNode) {
            this.oldNode = oldNode;
            this.startBacktick = oldNode.startBacktick();
            this.codeReference = oldNode.codeReference();
            this.endBacktick = oldNode.endBacktick();
        }

        public InlineCodeReferenceNodeModifier withStartBacktick(
                Token startBacktick) {
            Objects.requireNonNull(startBacktick, "startBacktick must not be null");
            this.startBacktick = startBacktick;
            return this;
        }

        public InlineCodeReferenceNodeModifier withCodeReference(
                Token codeReference) {
            Objects.requireNonNull(codeReference, "codeReference must not be null");
            this.codeReference = codeReference;
            return this;
        }

        public InlineCodeReferenceNodeModifier withEndBacktick(
                Token endBacktick) {
            Objects.requireNonNull(endBacktick, "endBacktick must not be null");
            this.endBacktick = endBacktick;
            return this;
        }

        public InlineCodeReferenceNode apply() {
            return oldNode.modify(
                    startBacktick,
                    codeReference,
                    endBacktick);
        }
    }
}
