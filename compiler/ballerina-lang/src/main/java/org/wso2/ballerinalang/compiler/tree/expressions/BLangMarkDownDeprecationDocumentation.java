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
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.MarkDownDocumentationDeprecationAttributeNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.LinkedList;
import java.util.List;

public class BLangMarkDownDeprecationDocumentation extends BLangExpression implements MarkDownDocumentationDeprecationAttributeNode {
    public List<String> deprecationDocumentationLines;
    public String deprecationLine;
    public boolean isCorrectDeprecationLine = false;

    public BLangMarkDownDeprecationDocumentation() {
        deprecationDocumentationLines = new LinkedList<>();
    }

    @Override
    public void addDeprecationDocumentationLine(String text) {
        deprecationDocumentationLines.add(text);
    }

    @Override
    public void addDeprecationLine(String text) {
        deprecationLine = text;
        if (deprecationLine.equals("# # Deprecated")) {
            isCorrectDeprecationLine = true;
        }
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.DOCUMENTATION_DEPRECATION;
    }
}
