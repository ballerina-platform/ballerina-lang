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

import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.tree.BLangFunctionBody;

import java.util.List;

/**
 * @since 0.94
 */
public interface InvokableNode extends AnnotatableNode, DocumentableNode {

    IdentifierNode getName();

    void setName(IdentifierNode name);
    
    List<? extends SimpleVariableNode> getParameters();
    
    void addParameter(SimpleVariableNode param);

    TypeNode getReturnTypeNode();

    void setReturnTypeNode(TypeNode typeNode);

    List<? extends AnnotationAttachmentNode> getReturnTypeAnnotationAttachments();

    void addReturnTypeAnnotationAttachment(AnnotationAttachmentNode annAttachment);

    BLangFunctionBody getBody();

    void setBody(FunctionBodyNode body);

    boolean hasBody();

    void addWorker(WorkerNode worker);

    List<? extends WorkerNode> getWorkers();

    SimpleVariableNode getRestParameters();

    void setRestParameter(SimpleVariableNode restParam);

}
