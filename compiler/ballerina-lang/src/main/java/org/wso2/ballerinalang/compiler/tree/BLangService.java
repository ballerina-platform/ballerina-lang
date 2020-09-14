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
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.MarkdownDocumentationNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.ServiceNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @since 0.94
 *
 *  TODO : Fix me.
 */
public class BLangService extends BLangNode implements ServiceNode {

    public Set<Flag> flagSet;
    public List<BLangAnnotationAttachment> annAttachments;
    public BLangMarkdownDocumentation markdownDocumentationAttachment;

    public BSymbol symbol;
    public BLangIdentifier name;
    public BLangClassDefinition serviceClass;
    public List<BLangExpression> attachedExprs;

    // Reference to global variable of this is a module level service.
    public BLangVariable variableNode;
    public boolean isAnonymousServiceValue;

    // Cached values.
    public BType listenerType;
    public List<BLangFunction> resourceFunctions;

    public BLangService() {
        this.flagSet = EnumSet.noneOf(Flag.class);
        this.annAttachments = new ArrayList<>();
        this.resourceFunctions = new ArrayList<>();
        this.attachedExprs = new ArrayList<>();
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
    public boolean isAnonymousService() {
        return this.isAnonymousServiceValue;
    }

    public List<BLangFunction> getResources() {
        return resourceFunctions;
    }

    public List<BLangExpression> getAttachedExprs() {
        return this.attachedExprs;
    }

    @Override
    public BLangClassDefinition getServiceClass() {
        return this.serviceClass;
    }

    @Override
    public Set<Flag> getFlags() {
        return null;
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
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.SERVICE;
    }

    @Override
    public String toString() {
        return "BLangService: " + flagSet + " " + annAttachments + " " + getName();
    }
}
