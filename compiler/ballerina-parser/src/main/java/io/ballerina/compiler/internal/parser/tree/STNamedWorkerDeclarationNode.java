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
package io.ballerina.compiler.internal.parser.tree;

import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarationNode;
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
public class STNamedWorkerDeclarationNode extends STNode {
    public final STNode annotations;
    public final STNode transactionalKeyword;
    public final STNode workerKeyword;
    public final STNode workerName;
    public final STNode returnTypeDesc;
    public final STNode workerBody;

    STNamedWorkerDeclarationNode(
            STNode annotations,
            STNode transactionalKeyword,
            STNode workerKeyword,
            STNode workerName,
            STNode returnTypeDesc,
            STNode workerBody) {
        this(
                annotations,
                transactionalKeyword,
                workerKeyword,
                workerName,
                returnTypeDesc,
                workerBody,
                Collections.emptyList());
    }

    STNamedWorkerDeclarationNode(
            STNode annotations,
            STNode transactionalKeyword,
            STNode workerKeyword,
            STNode workerName,
            STNode returnTypeDesc,
            STNode workerBody,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.NAMED_WORKER_DECLARATION, diagnostics);
        this.annotations = annotations;
        this.transactionalKeyword = transactionalKeyword;
        this.workerKeyword = workerKeyword;
        this.workerName = workerName;
        this.returnTypeDesc = returnTypeDesc;
        this.workerBody = workerBody;

        addChildren(
                annotations,
                transactionalKeyword,
                workerKeyword,
                workerName,
                returnTypeDesc,
                workerBody);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STNamedWorkerDeclarationNode(
                this.annotations,
                this.transactionalKeyword,
                this.workerKeyword,
                this.workerName,
                this.returnTypeDesc,
                this.workerBody,
                diagnostics);
    }

    public STNamedWorkerDeclarationNode modify(
            STNode annotations,
            STNode transactionalKeyword,
            STNode workerKeyword,
            STNode workerName,
            STNode returnTypeDesc,
            STNode workerBody) {
        if (checkForReferenceEquality(
                annotations,
                transactionalKeyword,
                workerKeyword,
                workerName,
                returnTypeDesc,
                workerBody)) {
            return this;
        }

        return new STNamedWorkerDeclarationNode(
                annotations,
                transactionalKeyword,
                workerKeyword,
                workerName,
                returnTypeDesc,
                workerBody,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new NamedWorkerDeclarationNode(this, position, parent);
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
