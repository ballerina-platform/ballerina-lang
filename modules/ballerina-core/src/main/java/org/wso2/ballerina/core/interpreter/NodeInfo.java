/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.interpreter;

import org.wso2.ballerina.core.model.Position;

/**
 * Bean class to hold meta information of a node in the control stack (i.e: a function/action/resource/service),
 * including name, type, package and the location in the ballerina source (see {@link Position}).
 */
public class NodeInfo {
    private String nodeName;
    private StackFrameType nodeType;
    private String nodePackage;
    private Position location;

    /**
     * Creates a {@link NodeInfo}.
     * 
     * @param nodeName      Identifier of the node
     * @param type          Type of the node
     * @param nodePackage   Package of the node
     * @param location      Location in the ballerina source
     */
    public NodeInfo(String nodeName, StackFrameType type, String nodePackage, Position location) {
        this.nodeName = nodeName;
        this.nodeType = type;
        this.nodePackage = nodePackage;
        this.location = location;
    }
    
    /**
     * Get the name of this node.
     * 
     * @return  Name of this node
     */
    public String getNodeName() {
        return this.nodeName;
    }

    /**
     * Get the type of this node.
     * 
     * @return  Type of this node
     */
    public StackFrameType getNodeType() {
        return this.nodeType;
    }

    /**
     * Get the package of this node.
     * 
     * @return  Package of this node
     */
    public String getNodePackage() {
        return this.nodePackage;
    }

    /**
     * Get the location of this node in the ballerina source file.
     * 
     * @return  Location of this node in the ballerina source
     */
    public Position getLocation() {
        return location;
    }
}
