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
package org.ballerinalang.bre.nonblocking;

import org.ballerinalang.bre.ConnectorVarLocation;
import org.ballerinalang.bre.ConstantLocation;
import org.ballerinalang.bre.ServiceVarLocation;
import org.ballerinalang.bre.StackVarLocation;
import org.ballerinalang.bre.StructVarLocation;
import org.ballerinalang.bre.WorkerVarLocation;
import org.ballerinalang.model.LinkedNode;
import org.ballerinalang.model.LinkedNodeVisitor;
import org.ballerinalang.model.values.BException;
import org.ballerinalang.model.values.BValue;

/**
 * Interface for Implementing LinkedNode based Executors.
 */
public interface BLangExecutionVisitor extends LinkedNodeVisitor {

    /* Memory Locations */
    BValue access(ConnectorVarLocation connectorVarLocation);

    BValue access(ConstantLocation constantLocation);

    BValue access(ServiceVarLocation serviceVarLocation);

    BValue access(StackVarLocation stackVarLocation);

    BValue access(StructVarLocation structVarLocation);

    BValue access(WorkerVarLocation workerVarLocation);

    void handleBException(BException exception);

    /**
     * Indicate whether execution is completed or not.
     *
     * @return true, if execution is completed.
     */
    boolean isExecutionCompleted();

    /**
     * Start execution from given LinkedNode.
     *
     * @param linkedNode
     */
    void startExecution(LinkedNode linkedNode);

    /**
     * Continue execution from LinkedNode which explicitly set.
     */
    void continueExecution();

    /**
     * Cancel execution.
     */
    void cancelExecution();

    /**
     * Indicate whether execution is canceled or not.
     *
     * @return true if execution is canceled.
     */
    boolean isExecutionCanceled();

    BLangExecution getExecution();

    void notifyComplete();
}
