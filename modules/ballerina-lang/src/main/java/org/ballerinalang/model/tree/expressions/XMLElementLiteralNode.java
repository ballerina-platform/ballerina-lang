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
package org.ballerinalang.model.tree.expressions;

import org.ballerinalang.model.Name;
import org.ballerinalang.model.symbols.Symbol;

import java.util.List;
import java.util.Map;

/**
 * @since 0.94
 */
public interface XMLElementLiteralNode extends XMLLiteralNode {

    ExpressionNode getStartTagName();

    void setStartTagName(ExpressionNode startTagName);

    ExpressionNode getEndTagName();

    void setEndTagName(ExpressionNode endTagName);

    List<? extends XMLAttributeNode> getAttributes();

    void addAttribute(XMLAttributeNode attribute);

    List<? extends ExpressionNode> getContent();

    void addChild(ExpressionNode content);
    
    Map<? extends Name, ? extends Symbol> getNamespaces();
    
    void addNamespace(Name prefix, Symbol namespaceUri);
}
