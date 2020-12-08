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

package org.ballerinalang.model.tree;

import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;

import java.util.List;

/**
 * Represents ClassDefinition Top Level Node interface.
 *
 * @since 2.0
 */
public interface ClassDefinition extends AnnotatableNode, DocumentableNode, TopLevelNode, OrderedNode {

    IdentifierNode getName();

    void setName(IdentifierNode name);

    List<BLangFunction> getFunctions();

    void addFunction(FunctionNode function);

    FunctionNode getInitFunction();

    void addField(VariableNode field);

    void addTypeReference(TypeNode typeRef);
}

