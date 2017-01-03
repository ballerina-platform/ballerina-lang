/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.interpreter;

import org.wso2.ballerina.core.model.values.BValue;

/**
 * {@code StackFrame} represents frame in a control stack.
 * Holds references to parameters, local variables and return values
 *
 * @since 1.0.0
 */
public class StackFrame {
    public BValue[] valuesNew;
    public BValue[] returnValuesNew;
    private NodeInfo frameInfo;

    public StackFrame(BValue[] valuesNew, BValue[] returnValuesNew) {
        this.valuesNew = valuesNew;
        this.returnValuesNew = returnValuesNew;
    }
    
    /**
     * Create a Stack frame.
     * 
     * @param valuesNew         Parameter and local variable values
     * @param returnValuesNew   Return values
     * @param frameInfo         Meta info of the node.   
     */
    public StackFrame(BValue[] valuesNew, BValue[] returnValuesNew, NodeInfo frameInfo) {
        this.valuesNew = valuesNew;
        this.returnValuesNew = returnValuesNew;
        this.frameInfo = frameInfo;
    }

    /**
     * Get the meta info (see {@link NodeInfo}) of this node.
     * 
     * @return  Meta info (see {@link NodeInfo}) of this node.
     */
    public NodeInfo getNodeInfo() {
        return this.frameInfo;
    }
}
