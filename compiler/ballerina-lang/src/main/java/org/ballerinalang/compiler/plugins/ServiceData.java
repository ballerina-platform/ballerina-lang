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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.compiler.plugins;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.ServiceNode;

import java.util.List;
import java.util.Set;

/**
 * Wrapper class to hold service node model.
 *
 * @since 0.985.0
 */
public interface ServiceData {

    ServiceNode getServiceNode();

    void setServiceNode(ServiceNode serviceNode);

    Set<? extends FunctionNode> getResourceNodes();

    List<? extends AnnotationAttachmentNode> getResourceAnnotations(FunctionNode functionNode);

    void addResource(FunctionNode functionNode, List<? extends AnnotationAttachmentNode> annotationAttachments);
}
