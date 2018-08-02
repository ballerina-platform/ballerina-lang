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

package org.ballerinalang.model.tree.expressions;

import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.List;

/**
 * Represents attributes in parameter documentation.
 *
 * @since 0.981.0
 */
public interface MarkdownDocumentationReturnParameterAttributeNode extends ExpressionNode {

    List<String> getReturnParameterDocumentationLines();

    void addReturnParameterDocumentationLine(String text);

    String getReturnParameterDocumentation();

    BType getReturnType();

    void setReturnType(BType type);
}
