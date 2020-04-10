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
package io.ballerinalang.compiler.internal.parser.tree;

import io.ballerinalang.compiler.syntax.tree.DefaultableParameter;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 1.3.0
 */
public class STDefaultableParameter extends STParameter {
    public final STNode leadingComma;
    public final STNode visibilityQualifier;
    public final STNode type;
    public final STNode paramName;
    public final STNode equalsToken;
    public final STNode expression;

    STDefaultableParameter(
            STNode leadingComma,
            STNode visibilityQualifier,
            STNode type,
            STNode paramName,
            STNode equalsToken,
            STNode expression) {
        super(SyntaxKind.PARAMETER);
        this.leadingComma = leadingComma;
        this.visibilityQualifier = visibilityQualifier;
        this.type = type;
        this.paramName = paramName;
        this.equalsToken = equalsToken;
        this.expression = expression;

        addChildren(
                leadingComma,
                visibilityQualifier,
                type,
                paramName,
                equalsToken,
                expression);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new DefaultableParameter(this, position, parent);
    }
}
