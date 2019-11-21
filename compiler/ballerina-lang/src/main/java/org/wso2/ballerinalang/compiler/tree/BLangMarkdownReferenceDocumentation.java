/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.model.tree.DocumentationReferenceType;
import org.ballerinalang.model.tree.MarkdownDocumentationReferenceAttributeNode;
import org.ballerinalang.model.tree.NodeKind;

/**
 * Represents a service reference documentation node.
 *
 * @since 1.1.0
 */
public class BLangMarkdownReferenceDocumentation extends BLangNode
        implements MarkdownDocumentationReferenceAttributeNode {

    public String qualifier = "";
    public String typeName =  "";
    public String identifier = "";
    public String referenceName; // Complete reference inside backticked block
    public NodeKind kind;
    public DocumentationReferenceType type;

    @Override
    public NodeKind getKind() {
        return NodeKind.DOCUMENTATION_REFERENCE;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public DocumentationReferenceType getType() {
        return type;
    }
}
