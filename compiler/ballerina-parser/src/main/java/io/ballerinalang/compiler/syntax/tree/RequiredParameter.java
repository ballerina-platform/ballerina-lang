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

public class RequiredParameter extends Parameter {
    private Token leadingComma;
    private Token visibilityQualifier;
    private Token type;
    private Token paramName;

    public RequiredParameter(STNode node, int position, NonTerminalNode parent) {
        super(node, position, parent);
    }

    public Token leadingComma() {
        if (leadingComma != null) {
            return leadingComma;
        }

        leadingComma = createToken(0);
        return leadingComma;
    }

    public Token visibilityQualifier() {
        if (visibilityQualifier != null) {
            return visibilityQualifier;
        }

        visibilityQualifier = createToken(1);
        return visibilityQualifier;
    }

    public Token type() {
        if (type != null) {
            return type;
        }

        type = createToken(2);
        return type;
    }

    public Token paramName() {
        if (paramName != null) {
            return paramName;
        }

        paramName = createToken(3);
        return paramName;
    }

    public Node childInBucket(int bucket) {
        switch (bucket) {
            case 0:
                return leadingComma();
            case 1:
                return visibilityQualifier();
            case 2:
                return type();
            case 3:
                return paramName();
        }
        return null;
    }

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visit(this);
    }
}
