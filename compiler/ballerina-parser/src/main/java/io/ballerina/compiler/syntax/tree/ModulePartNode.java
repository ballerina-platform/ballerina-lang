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
public class ModulePartNode extends NonTerminalNode {

    public ModulePartNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public NodeList<ImportDeclarationNode> imports() {
        return new NodeList<>(childInBucket(0));
    }

    public NodeList<ModuleMemberDeclarationNode> members() {
        return new NodeList<>(childInBucket(1));
    }

    public Token eofToken() {
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
                "imports",
                "members",
                "eofToken"};
    }

    public ModulePartNode modify(
            NodeList<ImportDeclarationNode> imports,
            NodeList<ModuleMemberDeclarationNode> members,
            Token eofToken) {
        if (checkForReferenceEquality(
                imports.underlyingListNode(),
                members.underlyingListNode(),
                eofToken)) {
            return this;
        }

        return NodeFactory.createModulePartNode(
                imports,
                members,
                eofToken);
    }

    public ModulePartNodeModifier modify() {
        return new ModulePartNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ModulePartNodeModifier {
        private final ModulePartNode oldNode;
        private NodeList<ImportDeclarationNode> imports;
        private NodeList<ModuleMemberDeclarationNode> members;
        private Token eofToken;

        public ModulePartNodeModifier(ModulePartNode oldNode) {
            this.oldNode = oldNode;
            this.imports = oldNode.imports();
            this.members = oldNode.members();
            this.eofToken = oldNode.eofToken();
        }

        public ModulePartNodeModifier withImports(
                NodeList<ImportDeclarationNode> imports) {
            Objects.requireNonNull(imports, "imports must not be null");
            this.imports = imports;
            return this;
        }

        public ModulePartNodeModifier withMembers(
                NodeList<ModuleMemberDeclarationNode> members) {
            Objects.requireNonNull(members, "members must not be null");
            this.members = members;
            return this;
        }

        public ModulePartNodeModifier withEofToken(
                Token eofToken) {
            Objects.requireNonNull(eofToken, "eofToken must not be null");
            this.eofToken = eofToken;
            return this;
        }

        public ModulePartNode apply() {
            return oldNode.modify(
                    imports,
                    members,
                    eofToken);
        }
    }
}
