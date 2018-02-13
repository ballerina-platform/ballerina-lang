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

import org.ballerinalang.model.tree.DocumentationNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.DocumentationAttributeNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangDocumentationAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.94
 */
public class BLangDocumentation extends BLangNode implements DocumentationNode {

    public BLangExpression documentationText;
    public List<BLangDocumentationAttribute> attributes;


    public BLangDocumentation() {
        this.attributes = new ArrayList<>();
    }

    public List<BLangDocumentationAttribute> getAttributes() {
        return attributes;
    }

    public void addAttribute(DocumentationAttributeNode attribute) {
        attributes.add((BLangDocumentationAttribute) attribute);
    }

    @Override
    public BLangExpression getDocumentationText() {
        return documentationText;
    }

    @Override
    public void setDocumentationText(ExpressionNode documentationText) {
        this.documentationText = (BLangExpression) documentationText;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.DOCUMENTATION;
    }

    @Override
    public String toString() {
        return "BLangDocumentation: " + documentationText + " " + attributes;
    }
}
