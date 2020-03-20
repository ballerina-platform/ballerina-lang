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

public class ObjectFieldNode extends NonTerminalNode {

    private Token visibilityQualifier;
    private Node fieldType;
    private Token fieldName;
    private Token equalsToken;
    private Node defaultValue;
    private Token semicolon;

    public ObjectFieldNode(STNode node, int position, NonTerminalNode parent) {
        super(node, position, parent);
    }

    public Token visibilityQualifier() {
        if (visibilityQualifier != null) {
            return visibilityQualifier;
        }

        visibilityQualifier = createToken(0);
        return visibilityQualifier;
    }

    public Node fieldType() {
        if (fieldType != null) {
            return fieldType;
        }

        fieldType = node.childInBucket(1).createFacade(getChildPosition(1), this);
        childBuckets[1] = fieldType;
        return fieldType;
    }

    public Token fieldName() {
        if (fieldName != null) {
            return fieldName;
        }

        fieldName = createToken(2);
        return fieldName;
    }

    public Token questionMark() {
        if (equalsToken != null) {
            return equalsToken;
        }

        equalsToken = createToken(3);
        return equalsToken;
    }

    public Node defaultValue() {
        if (defaultValue != null) {
            return defaultValue;
        }

        defaultValue = node.childInBucket(4).createFacade(getChildPosition(4), this);
        childBuckets[4] = defaultValue;
        return defaultValue;
    }

    public Token semicolon() {
        if (semicolon != null) {
            return semicolon;
        }

        semicolon = createToken(5);
        return semicolon;
    }

    public Node childInBucket(int bucket) {
        switch (bucket) {
            case 0:
                return visibilityQualifier();
            case 1:
                return fieldType();
            case 2:
                return fieldName();
            case 3:
                return questionMark();
            case 4:
                return defaultValue();
            case 5:
                return semicolon();
        }
        return null;
    }
}
