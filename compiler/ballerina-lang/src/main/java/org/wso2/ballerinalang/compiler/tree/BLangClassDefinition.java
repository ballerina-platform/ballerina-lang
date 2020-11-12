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

package org.wso2.ballerinalang.compiler.tree;

import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ClassDefinition;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.MarkdownDocumentationNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Represent class definition.
 *
 * @since 2.0
 */
public class BLangClassDefinition extends BLangNode implements ClassDefinition {
    public BLangIdentifier name;
    public List<BLangFunction> functions;
    public BLangFunction initFunction;
    public BLangFunction generatedInitFunction;
    public BLangSimpleVariable receiver;
    public List<BLangSimpleVariable> fields;
    public List<BLangType> typeRefs;
    public BTypeSymbol symbol;
    public Set<Flag> flagSet;
    public List<BLangAnnotationAttachment> annAttachments;
    public BLangMarkdownDocumentation markdownDocumentationAttachment;
    public List<BLangSimpleVariable> referencedFields;
    public int precedence;

    public BLangClassDefinition() {
        this.functions = new ArrayList<>();
        this.typeRefs = new ArrayList<>();
        this.fields = new ArrayList<>();
        this.flagSet = EnumSet.noneOf(Flag.class);
        this.flagSet.add(Flag.CLASS);
        this.annAttachments = new ArrayList<>();
        this.referencedFields = new ArrayList<>();
    }

    @Override
    public IdentifierNode getName() {
        return name;
    }

    @Override
    public void setName(IdentifierNode name) {
        this.name = (BLangIdentifier) name;
    }

    @Override
    public List<BLangFunction> getFunctions() {
        return functions;
    }

    @Override
    public void addFunction(FunctionNode function) {
        this.functions.add((BLangFunction) function);
    }

    @Override
    public FunctionNode getInitFunction() {
        return initFunction;
    }

    @Override
    public void addField(VariableNode field) {
        fields.add((BLangSimpleVariable) field);
    }

    @Override
    public void addTypeReference(TypeNode typeRef) {
        typeRefs.add((BLangType) typeRef);
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.CLASS_DEFN;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("class ").append(this.name.value).append(" { ");
        for (BLangType typeRef : this.typeRefs) {
            sb.append("*").append(typeRef.toString()).append(";\n");
        }
        for (BLangSimpleVariable field : this.fields) {
            sb.append(field.toString()).append(";\n");
        }
        for (BLangFunction function : this.functions) {
            sb.append(function.toString()).append("\n");
        }
        return sb.append(" }").toString();
    }

    @Override
    public Set<? extends Flag> getFlags() {
        return flagSet;
    }

    @Override
    public void addFlag(Flag flag) {
        flagSet.add(flag);
    }

    @Override
    public List<BLangAnnotationAttachment> getAnnotationAttachments() {
        return annAttachments;
    }

    @Override
    public void addAnnotationAttachment(AnnotationAttachmentNode annAttachment) {
        annAttachments.add((BLangAnnotationAttachment) annAttachment);
    }

    @Override
    public BLangMarkdownDocumentation getMarkdownDocumentationAttachment() {
        return markdownDocumentationAttachment;
    }

    @Override
    public void setMarkdownDocumentationAttachment(MarkdownDocumentationNode documentationNode) {
        markdownDocumentationAttachment = (BLangMarkdownDocumentation) documentationNode;
    }

    @Override
    public int getPrecedence() {
        return precedence;
    }

    @Override
    public void setPrecedence(int precedence) {
        this.precedence = precedence;
    }

}
