/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.tree;

import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.FunctionBodyNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.InvokableNode;
import org.ballerinalang.model.tree.MarkdownDocumentationNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.tree.WorkerNode;
import org.ballerinalang.model.tree.statements.BlockNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @since 0.94
 */
public abstract class BLangInvokableNode extends BLangNode implements InvokableNode {

    public static final String DEFAULT_WORKER_NAME = "default";

    public BLangIdentifier name;
    public BLangIdentifier defaultWorkerName;
    public List<BLangSimpleVariable> requiredParams;
    public BLangType returnTypeNode;
    public List<BLangAnnotationAttachment> returnTypeAnnAttachments;
    public List<BLangAnnotationAttachment> externalAnnAttachments;
    public BLangBlockStmt body;
    public BLangFunctionBody funcBody;
    public Set<Flag> flagSet;
    public List<BLangAnnotationAttachment> annAttachments;
    public BLangMarkdownDocumentation markdownDocumentationAttachment;
    public List<BLangEndpoint> endpoints;
    public List<BLangWorker> workers;
    public BLangSimpleVariable restParam;

    public BInvokableSymbol symbol;
    /**
     * clonedEnv is used for function parameter variable scoping.
     * Scope of clonedEnv will be populated with function parameters one by one at semantic phase to support
     * referencing of previous function parameters.
     */
    // TODO: Should be properly fixed by doing symbolEnter of function parameters only when needed at semantic phase.
    public SymbolEnv clonedEnv;

    public boolean desugaredReturnType;

    public BLangInvokableNode() {
        this.requiredParams = new ArrayList<>();
        this.annAttachments = new ArrayList<>();
        this.returnTypeAnnAttachments = new ArrayList<>();
        this.externalAnnAttachments = new ArrayList<>();
        this.endpoints = new ArrayList<>();
        this.flagSet = EnumSet.noneOf(Flag.class);
        this.workers = new ArrayList<>();
        this.defaultWorkerName = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        this.defaultWorkerName.value = DEFAULT_WORKER_NAME;
    }

    @Override
    public BLangIdentifier getName() {
        return name;
    }

    @Override
    public void setName(IdentifierNode name) {
        this.name = (BLangIdentifier) name;
    }

    @Override
    public List<BLangSimpleVariable> getParameters() {
        return requiredParams;
    }

    @Override
    public void addParameter(SimpleVariableNode param) {
        this.getParameters().add((BLangSimpleVariable) param);
    }

    @Override
    public BLangType getReturnTypeNode() {
        return returnTypeNode;
    }

    @Override
    public void setReturnTypeNode(TypeNode returnTypeNode) {
        this.returnTypeNode = (BLangType) returnTypeNode;
    }

    @Override
    public List<BLangAnnotationAttachment> getReturnTypeAnnotationAttachments() {
        return returnTypeAnnAttachments;
    }

    @Override
    public List<BLangAnnotationAttachment> getExternalAnnotationAttachments() {
        return externalAnnAttachments;
    }

    @Override
    public void addReturnTypeAnnotationAttachment(AnnotationAttachmentNode annAttachment) {
        this.returnTypeAnnAttachments.add((BLangAnnotationAttachment) annAttachment);
    }

    @Override
    public void addExternalAnnotationAttachment(AnnotationAttachmentNode annAttachment) {
        this.externalAnnAttachments.add((BLangAnnotationAttachment) annAttachment);
    }

    @Override
    public BLangBlockStmt getBody() {
        return body;
    }

    @Override
    public void setBody(FunctionBodyNode body) {
        this.funcBody = (BLangFunctionBody) body;
    }

    @Override
    public boolean hasBody() {
        return this.funcBody != null && (this.funcBody.getKind() == NodeKind.BLOCK_FUNCTION_BODY ||
                this.funcBody.getKind() == NodeKind.EXPR_FUNCTION_BODY);
    }

    @Override
    public Set<Flag> getFlags() {
        return flagSet;
    }

    @Override
    public void addFlag(Flag flag) {
        this.getFlags().add(flag);
    }

    @Override
    public List<BLangAnnotationAttachment> getAnnotationAttachments() {
        return annAttachments;
    }

    @Override
    public void addAnnotationAttachment(AnnotationAttachmentNode annAttachment) {
        this.getAnnotationAttachments().add((BLangAnnotationAttachment) annAttachment);
    }

    @Override
    public BLangMarkdownDocumentation getMarkdownDocumentationAttachment() {
        return markdownDocumentationAttachment;
    }

    @Override
    public void setMarkdownDocumentationAttachment(MarkdownDocumentationNode documentationNode) {
        this.markdownDocumentationAttachment = (BLangMarkdownDocumentation) documentationNode;
    }

    @Override
    public void addWorker(WorkerNode worker) {
        this.workers.add((BLangWorker) worker);
    }

    @Override
    public List<BLangWorker> getWorkers() {
        return workers;
    }

    @Override
    public SimpleVariableNode getRestParameters() {
        return restParam;
    }

    @Override
    public void setRestParameter(SimpleVariableNode restParam) {
        this.restParam = (BLangSimpleVariable) restParam;
    }

    @Override
    public List<? extends EndpointNode> getEndpointNodes() {
        return endpoints;
    }

    @Override
    public String toString() {
        return this.flagSet + " " + this.getName() + " (" + this.requiredParams +
                ") (" + this.returnTypeNode + ") Body: {" + this.body + "}"
                + (!workers.isEmpty() ? " Workers: {" + Arrays.toString(workers.toArray()) + "}" : "");
    }

}
