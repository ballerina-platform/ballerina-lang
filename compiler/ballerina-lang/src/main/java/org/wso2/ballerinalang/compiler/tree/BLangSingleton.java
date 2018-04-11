/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.SingletonNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;

import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Singleton Node Top level Node.
 */
public class BLangSingleton extends BLangNode implements SingletonNode {

    public BLangIdentifier name;
    public BLangExpression valueSpace;
    public List<BLangAnnotationAttachment> annAttachments;
    public List<BLangDocumentation> docAttachments;
    public List<BLangDeprecatedNode> deprecatedAttachments;
    public Set<Flag> flagSet;

    public BSymbol symbol;

    public BLangSingleton() {
        this.annAttachments = new ArrayList<>();
        this.docAttachments = new ArrayList<>();
        this.deprecatedAttachments = new ArrayList<>();
        this.flagSet = EnumSet.noneOf(Flag.class);
    }

    @Override
    public BLangIdentifier getName() {
        return name;
    }

    public void setName(IdentifierNode name) {
        this.name = (BLangIdentifier) name;
    }

    public BLangExpression getValueSpace() {
        return valueSpace;
    }

    public void setValueSpace(ExpressionNode valueSpace) {
        this.valueSpace = (BLangExpression) valueSpace;
    }

    @Override
    public void addFlag(Flag flag) {
        this.getFlags().add(flag);
    }

    @Override
    public Set<Flag> getFlags() {
        return flagSet;
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
    public NodeKind getKind() {
        return NodeKind.SINGLETON;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "BLangSingleton: " + this.name;
    }

}
