/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.tree.STNode;

import java.util.Objects;

/**
 * This is a generated syntax tree node.
 *
 * @since 2201.3.0
 */
public class ReSimpleCharClassEscapeNode extends NonTerminalNode {

    public ReSimpleCharClassEscapeNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token slashToken() {
        return childInBucket(0);
    }

    public Node reSimpleCharClassCode() {
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
                "slashToken",
                "reSimpleCharClassCode"};
    }

    public ReSimpleCharClassEscapeNode modify(
            Token slashToken,
            Node reSimpleCharClassCode) {
        if (checkForReferenceEquality(
                slashToken,
                reSimpleCharClassCode)) {
            return this;
        }

        return NodeFactory.createReSimpleCharClassEscapeNode(
                slashToken,
                reSimpleCharClassCode);
    }

    public ReSimpleCharClassEscapeNodeModifier modify() {
        return new ReSimpleCharClassEscapeNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ReSimpleCharClassEscapeNodeModifier {
        private final ReSimpleCharClassEscapeNode oldNode;
        private Token slashToken;
        private Node reSimpleCharClassCode;

        public ReSimpleCharClassEscapeNodeModifier(ReSimpleCharClassEscapeNode oldNode) {
            this.oldNode = oldNode;
            this.slashToken = oldNode.slashToken();
            this.reSimpleCharClassCode = oldNode.reSimpleCharClassCode();
        }

        public ReSimpleCharClassEscapeNodeModifier withSlashToken(
                Token slashToken) {
            Objects.requireNonNull(slashToken, "slashToken must not be null");
            this.slashToken = slashToken;
            return this;
        }

        public ReSimpleCharClassEscapeNodeModifier withReSimpleCharClassCode(
                Node reSimpleCharClassCode) {
            Objects.requireNonNull(reSimpleCharClassCode, "reSimpleCharClassCode must not be null");
            this.reSimpleCharClassCode = reSimpleCharClassCode;
            return this;
        }

        public ReSimpleCharClassEscapeNode apply() {
            return oldNode.modify(
                    slashToken,
                    reSimpleCharClassCode);
        }
    }
}
