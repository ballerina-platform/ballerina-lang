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
package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.tree.STNode;

import java.util.Objects;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class ImportOrgNameNode extends NonTerminalNode {

    public ImportOrgNameNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token orgName() {
        return childInBucket(0);
    }

    public Token slashToken() {
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
                "orgName",
                "slashToken"};
    }

    public ImportOrgNameNode modify(
            Token orgName,
            Token slashToken) {
        if (checkForReferenceEquality(
                orgName,
                slashToken)) {
            return this;
        }

        return NodeFactory.createImportOrgNameNode(
                orgName,
                slashToken);
    }

    public ImportOrgNameNodeModifier modify() {
        return new ImportOrgNameNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ImportOrgNameNodeModifier {
        private final ImportOrgNameNode oldNode;
        private Token orgName;
        private Token slashToken;

        public ImportOrgNameNodeModifier(ImportOrgNameNode oldNode) {
            this.oldNode = oldNode;
            this.orgName = oldNode.orgName();
            this.slashToken = oldNode.slashToken();
        }

        public ImportOrgNameNodeModifier withOrgName(
                Token orgName) {
            Objects.requireNonNull(orgName, "orgName must not be null");
            this.orgName = orgName;
            return this;
        }

        public ImportOrgNameNodeModifier withSlashToken(
                Token slashToken) {
            Objects.requireNonNull(slashToken, "slashToken must not be null");
            this.slashToken = slashToken;
            return this;
        }

        public ImportOrgNameNode apply() {
            return oldNode.modify(
                    orgName,
                    slashToken);
        }
    }
}
