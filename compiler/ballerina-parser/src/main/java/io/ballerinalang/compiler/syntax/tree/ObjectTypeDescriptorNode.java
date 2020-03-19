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

    private Token objectKeyword;
    private Token openBrace;
    private Node members;
    private Token closeBrace;

    public ObjectTypeDescriptorNode(STNode node, int position, NonTerminalNode parent) {
        super(node, position, parent);
    }

    public Token objectKeyword() {
        if (objectKeyword != null) {
            return objectKeyword;
        }

        objectKeyword = createToken(0);
        return objectKeyword;
    }

    public Token openBrace() {
        if (openBrace != null) {
            return openBrace;
        }

        openBrace = createToken(1);
        return openBrace;
    }

    public Node members() {
        if (members != null) {
            return members;
        }

        members = createListNode(2);
        return members;
    }

    public Token closeBrace() {
        if (closeBrace != null) {
            return closeBrace;
        }

        closeBrace = createToken(3);
        return closeBrace;
    }

    public Node childInBucket(int bucket) {
        switch (bucket) {
            case 0:
                return objectKeyword();
            case 1:
                return openBrace();
            case 2:
                return members();
            case 3:
                return closeBrace();
        }
        return null;
    }
}
