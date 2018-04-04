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
import org.ballerinalang.model.tree.TypeDefinition;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Class which hold top level construction Type Definition.
 */
public class BLangTypeDefinition extends BLangNode implements TypeDefinition {

    public BLangIdentifier name;
    public BLangType typeNode;
    public List<BLangExpression> valueSpace;
    public List<BLangAnnotationAttachment> annAttachments;
    public List<BLangDocumentation> docAttachments;
    public List<BLangDeprecatedNode> deprecatedAttachments;
    public Set<Flag> flagSet;

    public BSymbol symbol;

    public BLangTypeDefinition() {
        this.annAttachments = new ArrayList<>();
        this.docAttachments = new ArrayList<>();
        this.deprecatedAttachments = new ArrayList<>();
        this.valueSpace = new ArrayList<>();
        this.flagSet = EnumSet.noneOf(Flag.class);
    }

    @Override
    public BLangIdentifier getName() {
        return name;
    }

    public void setName(IdentifierNode name) {
        this.name = (BLangIdentifier) name;
    }

    public BLangType getTypeNode() {
        return typeNode;
    }

    public void setTypeNode(TypeNode typeNode) {
        this.typeNode = (BLangType) typeNode;
    }

    public List<BLangExpression> getValueSet() {
        return valueSpace;
    }

    public void addValue(ExpressionNode value) {
        valueSpace.add((BLangExpression) value);
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
        return NodeKind.TYPE_DEFINITION;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "BLangTypeDefinition: " + this.name;
    }

}
