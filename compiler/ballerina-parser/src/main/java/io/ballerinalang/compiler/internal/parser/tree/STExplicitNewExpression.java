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

import io.ballerinalang.compiler.syntax.tree.ExplicitNewExpression;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 1.3.0
 */
public class STExplicitNewExpression extends STNewExpression {
    public final STNode NewKeyword;
    public final STNode TypeDescriptor;
    public final STNode openParenToken;
    public final STNode arguments;
    public final STNode closeParenToken;

    STExplicitNewExpression(
            STNode NewKeyword,
            STNode TypeDescriptor,
            STNode openParenToken,
            STNode arguments,
            STNode closeParenToken) {
        super(SyntaxKind.EXPLICIT_NEW);
        this.NewKeyword = NewKeyword;
        this.TypeDescriptor = TypeDescriptor;
        this.openParenToken = openParenToken;
        this.arguments = arguments;
        this.closeParenToken = closeParenToken;

        addChildren(
                NewKeyword,
                TypeDescriptor,
                openParenToken,
                arguments,
                closeParenToken);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ExplicitNewExpression(this, position, parent);
    }
}
