/*
 *  Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
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

import io.ballerina.compiler.syntax.tree.BallerinaNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STBallerinaNameReferenceNode extends STDocumentationNode {
    public final STNode referenceType;
    public final STNode startBacktick;
    public final STNode nameReference;
    public final STNode endBacktick;

    STBallerinaNameReferenceNode(
            STNode referenceType,
            STNode startBacktick,
            STNode nameReference,
            STNode endBacktick) {
        this(
                referenceType,
                startBacktick,
                nameReference,
                endBacktick,
                Collections.emptyList());
    }

    STBallerinaNameReferenceNode(
            STNode referenceType,
            STNode startBacktick,
            STNode nameReference,
            STNode endBacktick,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.BALLERINA_NAME_REFERENCE, diagnostics);
        this.referenceType = referenceType;
        this.startBacktick = startBacktick;
        this.nameReference = nameReference;
        this.endBacktick = endBacktick;

        addChildren(
                referenceType,
                startBacktick,
                nameReference,
                endBacktick);
    }

    @Override
    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STBallerinaNameReferenceNode(
                this.referenceType,
                this.startBacktick,
                this.nameReference,
                this.endBacktick,
                diagnostics);
    }

    public STBallerinaNameReferenceNode modify(
            STNode referenceType,
            STNode startBacktick,
            STNode nameReference,
            STNode endBacktick) {
        if (checkForReferenceEquality(
                referenceType,
                startBacktick,
                nameReference,
                endBacktick)) {
            return this;
        }

        return new STBallerinaNameReferenceNode(
                referenceType,
                startBacktick,
                nameReference,
                endBacktick,
                diagnostics);
    }

    @Override
    public Node createFacade(int position, NonTerminalNode parent) {
        return new BallerinaNameReferenceNode(this, position, parent);
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
