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

import org.ballerinalang.model.tree.MarkdownDocumentationNode;
import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a markdown documentation node.
 *
 * @since 0.981.0
 */
public class BLangMarkdownDocumentation extends BLangNode implements MarkdownDocumentationNode {

    public LinkedList<BLangMarkdownDocumentationLine> documentationLines;
    public LinkedList<BLangMarkdownParameterDocumentation> parameters;
    public LinkedList<BLangMarkdownReferenceDocumentation> references;
    public BLangMarkdownReturnParameterDocumentation returnParameter;

    public BLangMarkdownDocumentation() {
        this.documentationLines = new LinkedList<>();
        this.parameters = new LinkedList<>();
        this.references = new LinkedList<>();
    }

    @Override
    public LinkedList<BLangMarkdownDocumentationLine> getDocumentationLines() {
        return documentationLines;
    }

    @Override
    public void addDocumentationLine(BLangMarkdownDocumentationLine description) {
        documentationLines.add(description);
    }

    @Override
    public LinkedList<BLangMarkdownParameterDocumentation> getParameters() {
        return parameters;
    }

    @Override
    public void addParameter(BLangMarkdownParameterDocumentation parameter) {
        parameters.add(parameter);
    }

    @Override
    public BLangMarkdownReturnParameterDocumentation getReturnParameter() {
        return returnParameter;
    }

    @Override
    public void setReturnParameter(BLangMarkdownReturnParameterDocumentation returnParameter) {
        this.returnParameter = returnParameter;
    }

    @Override
    public String getDocumentation() {
        return documentationLines.stream()
                .map(BLangMarkdownDocumentationLine::getText)
                .collect(Collectors.joining("\n")).replaceAll("\r", "");
    }

    @Override
    public Map<String, BLangMarkdownParameterDocumentation> getParameterDocumentations() {
        Map<String, BLangMarkdownParameterDocumentation> parameterDocumentations = new HashMap<>();
        parameters.forEach(p -> parameterDocumentations.put(p.getParameterName().getValue(), p));
        return parameterDocumentations;
    }

    @Override
    public String getReturnParameterDocumentation() {
        return returnParameter == null ? null : returnParameter.getReturnParameterDocumentation();
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.MARKDOWN_DOCUMENTATION;
    }

    @Override
    public String toString() {
        return "BLangMarkdownDocumentation: " + documentationLines + " " + references + " " + parameters + " " +
                (returnParameter == null ? "" : returnParameter);
    }

    @Override
    public LinkedList<BLangMarkdownReferenceDocumentation> getReferences() {
        return references;
    }

    @Override
    public void addReference(BLangMarkdownReferenceDocumentation reference) {
        if (reference != null) {
            references.add(reference);
        }
    }
}
