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
import org.ballerinalang.model.tree.ActionNode;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ConnectorNode;
import org.ballerinalang.model.tree.DeprecatedNode;
import org.ballerinalang.model.tree.DocumentationNode;
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConnectorSymbol;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @since 0.94
 */
public class BLangConnector extends BLangNode implements ConnectorNode {
    
    public BLangIdentifier name;
    public List<BLangVariable> params;
    public List<BLangVariableDef> varDefs;
    public List<BLangAction> actions;
    public Set<Flag> flagSet;
    public List<BLangAnnotationAttachment> annAttachments;
    public List<BLangDocumentation> docAttachments;
    public List<BLangDeprecatedNode> deprecatedAttachments;
    public List<BLangEndpoint> endpoints;
    public BLangFunction initFunction;
    public BLangAction initAction;

    public BConnectorSymbol symbol;

    public BLangConnector() {
        this.params = new ArrayList<>();
        this.varDefs = new ArrayList<>();
        this.actions = new ArrayList<>();
        this.flagSet = EnumSet.noneOf(Flag.class);
        this.endpoints = new ArrayList<>();
        this.annAttachments = new ArrayList<>();
        this.docAttachments = new ArrayList<>();
        this.deprecatedAttachments = new ArrayList<>();
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
        return params;
    }
    
    @Override
    public void addParameter(VariableNode param) {
        this.getParameters().add((BLangVariable) param);
    }

    @Override
    public List<BLangVariableDef> getVariableDefs() {
        return varDefs;
    }
    
    @Override
    public void addVariableDef(VariableDefinitionNode varDef) {
        this.getVariableDefs().add((BLangVariableDef) varDef);
    }

    @Override
    public List<BLangAction> getActions() {
        return actions;
    }
    
    @Override
    public void addAction(ActionNode action) {
        this.getActions().add((BLangAction) action);
    }

    @Override
    public void setInitFunction(FunctionNode function) {
        this.initFunction = (BLangFunction) function;
    }

    @Override
    public FunctionNode getInitFunction() {
        return initFunction;
    }

    @Override
    public void setInitAction(ActionNode action) {
        this.initAction = (BLangAction) action;
    }

    @Override
    public ActionNode getInitAction() {
        return initAction;
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
    public void addAnnotationAttachment(AnnotationAttachmentNode annAttachement) {
        this.getAnnotationAttachments().add((BLangAnnotationAttachment) annAttachement);
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
    public List<? extends EndpointNode> getEndpointNodes() {
        return endpoints;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.CONNECTOR;
    }
    
    @Override
    public String toString() {
        return "BLangConnector: " + this.name + " (" + this.params +
                ")\n\t Variable Defs: " + this.varDefs + "\n\t Actions: " + this.actions;
    }

}
