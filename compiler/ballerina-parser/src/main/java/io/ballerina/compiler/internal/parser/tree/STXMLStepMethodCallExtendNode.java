/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.internal.parser.tree;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.XMLStepMethodCallExtendNode;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2201.10.0
 */
public class STXMLStepMethodCallExtendNode extends STNode {
    public final STNode dotToken;
    public final STNode methodName;
    public final STNode arguments;

    STXMLStepMethodCallExtendNode(
            STNode dotToken,
            STNode methodName,
            STNode arguments) {
        this(
                dotToken,
                methodName,
                arguments,
                Collections.emptyList());
    }

    STXMLStepMethodCallExtendNode(
            STNode dotToken,
            STNode methodName,
            STNode arguments,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.XML_STEP_METHOD_CALL_EXTEND, diagnostics);
        this.dotToken = dotToken;
        this.methodName = methodName;
        this.arguments = arguments;

        addChildren(
                dotToken,
                methodName,
                arguments);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STXMLStepMethodCallExtendNode(
                this.dotToken,
                this.methodName,
                this.arguments,
                diagnostics);
    }

    public STXMLStepMethodCallExtendNode modify(
            STNode dotToken,
            STNode methodName,
            STNode arguments) {
        if (checkForReferenceEquality(
                dotToken,
                methodName,
                arguments)) {
            return this;
        }

        return new STXMLStepMethodCallExtendNode(
                dotToken,
                methodName,
                arguments,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new XMLStepMethodCallExtendNode(this, position, parent);
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
