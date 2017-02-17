/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model.nodes;

import org.ballerinalang.model.LinkedNodeVisitor;

/**
 * Represents the start node of a linked execution.
 */
public class StartNode extends AbstractLinkedNode {
    private final Originator type;

    public StartNode(Originator type) {
        this.type = type;
    }

    public boolean isMainFunctionInvocation() {
        return this.type == Originator.MAIN_FUNCTION;
    }

    /**
     * Start Link type.
     */
    public enum Originator {
        MAIN_FUNCTION,
        RESOURCE,
        WORKER,
        TEST
    }

    @Override
    public void accept(LinkedNodeVisitor nodeVisitor) {
        // Nothing to do.
    }

}
