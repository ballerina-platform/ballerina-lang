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

package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.MarkdownDocumentationReturnParameterAttributeNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeModifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a return parameter node.
 *
 * @since 0.981.0
 */
public class BLangMarkdownReturnParameterDocumentation extends BLangExpression
        implements MarkdownDocumentationReturnParameterAttributeNode {

    // Parser Flags and Data
    public List<String> returnParameterDocumentationLines;

    // Semantic Data
    public BType type;

    public BLangMarkdownReturnParameterDocumentation() {
        returnParameterDocumentationLines = new LinkedList<>();
    }

    @Override
    public List<String> getReturnParameterDocumentationLines() {
        return returnParameterDocumentationLines;
    }

    @Override
    public void addReturnParameterDocumentationLine(String text) {
        returnParameterDocumentationLines.add(text);
    }

    @Override
    public String getReturnParameterDocumentation() {
        return returnParameterDocumentationLines.stream().collect(Collectors.joining("\n")).replaceAll("\r", "");
    }

    @Override
    public BType getReturnType() {
        return type;
    }

    @Override
    public void setReturnType(BType type) {
        this.type = type;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
        analyzer.visit(this, props);
    }

    @Override
    public <T, R> R apply(BLangNodeModifier<T, R> modifier, T props) {
        return modifier.modify(this, props);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.DOCUMENTATION_PARAMETER;
    }

    @Override
    public String toString() {
        return "BLangMarkdownReturnParameterDocumentation: " + (returnParameterDocumentationLines == null ? "" :
                returnParameterDocumentationLines);
    }
}
