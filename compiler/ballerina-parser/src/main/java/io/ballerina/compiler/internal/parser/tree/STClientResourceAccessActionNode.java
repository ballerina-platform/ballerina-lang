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
package io.ballerina.compiler.internal.parser.tree;

import io.ballerina.compiler.syntax.tree.ClientResourceAccessActionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2201.2.0
 */
public class STClientResourceAccessActionNode extends STActionNode {
    public final STNode expression;
    public final STNode rightArrowToken;
    public final STNode slashToken;
    public final STNode resourceAccessPath;
    public final STNode dotToken;
    public final STNode methodName;
    public final STNode arguments;

    STClientResourceAccessActionNode(
            STNode expression,
            STNode rightArrowToken,
            STNode slashToken,
            STNode resourceAccessPath,
            STNode dotToken,
            STNode methodName,
            STNode arguments) {
        this(
                expression,
                rightArrowToken,
                slashToken,
                resourceAccessPath,
                dotToken,
                methodName,
                arguments,
                Collections.emptyList());
    }

    STClientResourceAccessActionNode(
            STNode expression,
            STNode rightArrowToken,
            STNode slashToken,
            STNode resourceAccessPath,
            STNode dotToken,
            STNode methodName,
            STNode arguments,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.CLIENT_RESOURCE_ACCESS_ACTION, diagnostics);
        this.expression = expression;
        this.rightArrowToken = rightArrowToken;
        this.slashToken = slashToken;
        this.resourceAccessPath = resourceAccessPath;
        this.dotToken = dotToken;
        this.methodName = methodName;
        this.arguments = arguments;

        addChildren(
                expression,
                rightArrowToken,
                slashToken,
                resourceAccessPath,
                dotToken,
                methodName,
                arguments);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STClientResourceAccessActionNode(
                this.expression,
                this.rightArrowToken,
                this.slashToken,
                this.resourceAccessPath,
                this.dotToken,
                this.methodName,
                this.arguments,
                diagnostics);
    }

    public STClientResourceAccessActionNode modify(
            STNode expression,
            STNode rightArrowToken,
            STNode slashToken,
            STNode resourceAccessPath,
            STNode dotToken,
            STNode methodName,
            STNode arguments) {
        if (checkForReferenceEquality(
                expression,
                rightArrowToken,
                slashToken,
                resourceAccessPath,
                dotToken,
                methodName,
                arguments)) {
            return this;
        }

        return new STClientResourceAccessActionNode(
                expression,
                rightArrowToken,
                slashToken,
                resourceAccessPath,
                dotToken,
                methodName,
                arguments,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ClientResourceAccessActionNode(this, position, parent);
    }

    @Override
    public void accept(STNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(STNodeTransformer<T> transformer) {
        return transformer.transform(this);
    }
}
