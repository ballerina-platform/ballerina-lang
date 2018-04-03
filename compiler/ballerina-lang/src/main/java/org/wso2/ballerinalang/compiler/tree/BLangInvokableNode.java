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

import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.DeprecatedNode;
import org.ballerinalang.model.tree.DocumentationNode;
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.InvokableNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.WorkerNode;
import org.ballerinalang.model.tree.statements.BlockNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
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

    public BLangIdentifier name;
    public List<BLangVariable> requiredParams;
    public BLangType returnTypeNode;
    public List<BLangAnnotationAttachment> returnTypeAnnAttachments;
    public BLangBlockStmt body;
    public Set<Flag> flagSet;
    public List<BLangAnnotationAttachment> annAttachments;
    public List<BLangDocumentation> docAttachments;
    public List<BLangDeprecatedNode> deprecatedAttachments;
    public List<BLangEndpoint> endpoints;
    public List<BLangWorker> workers;
    public List<BLangVariableDef> defaultableParams;
    public BLangVariable restParam;

    public BInvokableSymbol symbol;

    public boolean desugaredReturnType;

    public BLangInvokableNode() {
        this.requiredParams = new ArrayList<>();
        this.annAttachments = new ArrayList<>();
        this.returnTypeAnnAttachments = new ArrayList<>();
        this.endpoints = new ArrayList<>();
        this.flagSet = EnumSet.noneOf(Flag.class);
        this.workers = new ArrayList<>();
        this.docAttachments = new ArrayList<>();
        this.deprecatedAttachments = new ArrayList<>();
        this.defaultableParams = new ArrayList<>();
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
    public List<BLangVariable> getParameters() {
        return requiredParams;
    }

    @Override
    public void addParameter(VariableNode param) {
        this.getParameters().add((BLangVariable) param);
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
    public void addReturnTypeAnnotationAttachment(AnnotationAttachmentNode annAttachment) {
        this.returnTypeAnnAttachments.add((BLangAnnotationAttachment) annAttachment);
    }

    @Override
    public BLangBlockStmt getBody() {
        return body;
    }

    @Override
    public void setBody(BlockNode body) {
        this.body = (BLangBlockStmt) body;
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
    public List<BLangDocumentation> getDocumentationAttachments() {
        return docAttachments;
    }

    @Override
    public void addDocumentationAttachment(DocumentationNode docAttachment) {
        this.docAttachments.add((BLangDocumentation) docAttachment);
    }

    @Override
    public List<BLangDeprecatedNode> getDeprecatedAttachments() {
        return deprecatedAttachments;
    }

    @Override
    public void addDeprecatedAttachment(DeprecatedNode deprecatedNode) {
        this.deprecatedAttachments.add((BLangDeprecatedNode) deprecatedNode);
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
    public List<BLangVariableDef> getDefaultableParameters() {
        return defaultableParams;
    }

    @Override
    public void addDefaultableParameter(VariableDefinitionNode param) {
        this.defaultableParams.add((BLangVariableDef) param);
    }

    @Override
    public VariableNode getRestParameters() {
        return restParam;
    }

    @Override
    public void setRestParameter(VariableNode restParam) {
        this.restParam = (BLangVariable) restParam;
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
