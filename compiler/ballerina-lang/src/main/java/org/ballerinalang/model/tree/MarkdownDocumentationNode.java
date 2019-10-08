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

package org.ballerinalang.model.tree;

import org.wso2.ballerinalang.compiler.tree.BLangMarkdownReferenceDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Represents Ballerina Markdown Documentation Node.
 *
 * @since 0.981.0
 */
public interface MarkdownDocumentationNode extends Node {

    LinkedList<BLangMarkdownDocumentationLine> getDocumentationLines();

    void addDocumentationLine(BLangMarkdownDocumentationLine documentationText);

    LinkedList<BLangMarkdownParameterDocumentation> getParameters();

    void addParameter(BLangMarkdownParameterDocumentation attribute);

    BLangMarkdownReturnParameterDocumentation getReturnParameter();

    void setReturnParameter(BLangMarkdownReturnParameterDocumentation attribute);

    String getDocumentation();

    Map<String, BLangMarkdownParameterDocumentation> getParameterDocumentations();

    String getReturnParameterDocumentation();

    List<BLangMarkdownReferenceDocumentation> getReferences();

    void addReference(BLangMarkdownReferenceDocumentation reference);
}
