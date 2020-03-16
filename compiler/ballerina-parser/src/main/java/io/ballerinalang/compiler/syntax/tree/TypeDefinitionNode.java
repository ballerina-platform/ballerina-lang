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
package io.ballerinalang.compiler.syntax.tree;

import io.ballerinalang.compiler.internal.parser.tree.STNode;

public class TypeDefinitionNode extends ModuleMemberDeclaration {

    private Token visibilityQual;
    private Token typeKeyword;
    private Token typeName;
    private Node typeDescriptor;
    private Token comma;

    public TypeDefinitionNode(STNode node, int position, NonTerminalNode parent) {
        super(node, position, parent);
    }

    public Token visibilityQualifier() {
        if (visibilityQual != null) {
            return visibilityQual;
        }

        visibilityQual = createToken(0);
        return visibilityQual;
    }

    public Token typeKeyword() {
        if (typeKeyword != null) {
            return typeKeyword;
        }

        typeKeyword = createToken(1);
        return typeKeyword;
    }

    public Token typeName() {
        if (typeName != null) {
            return typeName;
        }

        typeName = createToken(2);
        return typeName;
    }

    public Node typeDescriptor() {
        if (typeDescriptor != null) {
            return typeDescriptor;
        }

        typeDescriptor = node.childInBucket(3).createFacade(getChildPosition(3), this);
        childBuckets[3] = typeDescriptor;
        return typeDescriptor;
    }

    public Token comma() {
        if (comma != null) {
            return comma;
        }

        this.comma = createToken(4);
        return this.comma;
    }

    public Node childInBucket(int bucket) {
        switch (bucket) {
            case 0:
                return visibilityQualifier();
            case 1:
                return typeKeyword();
            case 2:
                return typeName();
            case 3:
                return typeDescriptor();
            case 4:
                return comma();
        }
        return null;
    }
}
