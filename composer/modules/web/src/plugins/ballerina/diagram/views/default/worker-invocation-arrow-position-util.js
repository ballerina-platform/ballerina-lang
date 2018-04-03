/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import SimpleBBox from 'plugins/ballerina/model/view/simple-bounding-box';
import TreeUtil from './../../../model/tree-util';

/**
 * Util for positioning the worker invocation arrows
 */

class InvocationArrowPositionUtil {

    /**
     * Arrow position from worker send node
     * @param {object} node - worker send node
     */
    positionArrowWorkerSendNode(node) {
        const viewState = node.viewState;
        const statementBox = viewState.components['statement-box'];
        const workerList = node.parent.parent.parent.workers;
        const sendTo = TreeUtil.getWorkerByName(node.workerName.value, workerList);

        if (sendTo) {
            // TODO: Currently, assume that the sender is a direct child of a worker
            const workerOwningSender = node.parent.parent;
            const receiverNode = TreeUtil.getReceiverForSender(sendTo, workerOwningSender);

            if (receiverNode) {
                const receiverStatementBox = receiverNode.viewState.components['statement-box'];
                const arrowStart = new SimpleBBox();
                const arrowEnd = new SimpleBBox();
                let backwardArrow = false;

                if (receiverStatementBox.x < statementBox.x) {
                    backwardArrow = true;
                    arrowStart.x = statementBox.x;
                    arrowEnd.x = receiverStatementBox.x;
                } else {
                    arrowStart.x = statementBox.x;
                    arrowEnd.x = receiverStatementBox.x;
                }

                arrowStart.y = statementBox.y + (statementBox.h * 0.75);
                arrowEnd.y = arrowStart.y;

                viewState.components['invocation-arrow'] = {
                    start: arrowStart,
                    end: arrowEnd,
                    backward: backwardArrow,
                };
            }
        }
    }
}

export default InvocationArrowPositionUtil;
