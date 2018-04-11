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

import java.util.List;
import java.util.Set;

/**
 * @since 0.94
 */
public class BLangConnector extends BLangNode implements ConnectorNode {

    @Override
    public IdentifierNode getName() {
        return null;
    }

    @Override
    public void setName(IdentifierNode name) {

    }

    @Override
    public List<? extends VariableNode> getParameters() {
        return null;
    }

    @Override
    public void addParameter(VariableNode param) {

    }

    @Override
    public List<? extends VariableDefinitionNode> getVariableDefs() {
        return null;
    }

    @Override
    public void addVariableDef(VariableDefinitionNode var) {

    }

    @Override
    public List<? extends ActionNode> getActions() {
        return null;
    }

    @Override
    public void addAction(ActionNode action) {

    }

    @Override
    public List<? extends EndpointNode> getEndpointNodes() {
        return null;
    }

    @Override
    public void setInitFunction(FunctionNode function) {

    }

    @Override
    public FunctionNode getInitFunction() {
        return null;
    }

    @Override
    public void setInitAction(ActionNode action) {

    }

    @Override
    public ActionNode getInitAction() {
        return null;
    }

    @Override
    public Set<? extends Flag> getFlags() {
        return null;
    }

    @Override
    public void addFlag(Flag flag) {

    }

    @Override
    public List<? extends AnnotationAttachmentNode> getAnnotationAttachments() {
        return null;
    }

    @Override
    public void addAnnotationAttachment(AnnotationAttachmentNode annAttachment) {

    }

    @Override
    public List<? extends DocumentationNode> getDocumentationAttachments() {
        return null;
    }

    @Override
    public void addDocumentationAttachment(DocumentationNode annAttachment) {

    }

    @Override
    public List<? extends DeprecatedNode> getDeprecatedAttachments() {
        return null;
    }

    @Override
    public void addDeprecatedAttachment(DeprecatedNode deprecatedAttachment) {

    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
    }

    @Override
    public NodeKind getKind() {
        return null;
    }
}
