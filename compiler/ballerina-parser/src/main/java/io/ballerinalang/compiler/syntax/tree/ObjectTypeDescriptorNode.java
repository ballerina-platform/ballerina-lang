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

public class ObjectTypeDescriptorNode extends NonTerminalNode {

    private Node qualifiers;
    private Token objectKeyword;
    private Token openBrace;
    private Node members;
    private Token closeBrace;

    public ObjectTypeDescriptorNode(STNode node, int position, NonTerminalNode parent) {
        super(node, position, parent);
    }

    public Node qualifiers() {
        if (qualifiers != null) {
            return qualifiers;
        }

        qualifiers = createListNode(0);
        return qualifiers;
    }

    public Token objectKeyword() {
        if (objectKeyword != null) {
            return objectKeyword;
        }

        objectKeyword = createToken(1);
        return objectKeyword;
    }

    public Token openBrace() {
        if (openBrace != null) {
            return openBrace;
        }

        openBrace = createToken(2);
        return openBrace;
    }

    public Node members() {
        if (members != null) {
            return members;
        }

        members = createListNode(3);
        return members;
    }

    public Token closeBrace() {
        if (closeBrace != null) {
            return closeBrace;
        }

        closeBrace = createToken(4);
        return closeBrace;
    }

    public Node childInBucket(int bucket) {
        switch (bucket) {
            case 0:
                return qualifiers();
            case 1:
                return objectKeyword();
            case 2:
                return openBrace();
            case 3:
                return members();
            case 4:
                return closeBrace();
        }
        return null;
    }

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(SyntaxNodeTransformer<T> visitor) {
        return visitor.transform(this);
    }
}
