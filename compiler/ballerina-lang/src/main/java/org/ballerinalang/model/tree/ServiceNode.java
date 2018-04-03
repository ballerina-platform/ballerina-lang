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
package org.ballerinalang.model.tree;

import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.model.tree.expressions.SimpleVariableReferenceNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.ballerinalang.model.tree.types.UserDefinedTypeNode;

import java.util.List;

/**
 * @since 0.94
 */
public interface ServiceNode extends AnnotatableNode, DocumentableNode, TopLevelNode {
    
    IdentifierNode getName();
    
    void setName(IdentifierNode name);

    UserDefinedTypeNode getServiceTypeStruct();

    void setServiceTypeStruct(UserDefinedTypeNode endpointType);

    List<? extends VariableDefinitionNode> getVariables();
    
    void addVariable(VariableDefinitionNode var);

    List<? extends ResourceNode> getResources();
    
    void addResource(ResourceNode resource);

    List<? extends EndpointNode> getEndpointNodes();

    void setInitFunction(FunctionNode function);

    FunctionNode getInitFunction();

    void bindToEndpoint(SimpleVariableReferenceNode endpointRef);

    List<? extends SimpleVariableReferenceNode> getBoundEndpoints();

    RecordLiteralNode getAnonymousEndpointBind();

    void addAnonymousEndpointBind(RecordLiteralNode recordLiteralNode);

}
