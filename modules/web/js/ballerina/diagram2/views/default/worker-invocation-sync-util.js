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

import _ from 'lodash';
import TreeUtil from './../../../model/tree-util';

/**
 * Visitor util class for synchronizing the worker invocation statements
 */
class WorkerInvocationSyncUtil {

    syncWorkerSendNode(node) {
        if (node.viewState.dimensionsSynced) {
            return;
        }
        const workerList = node.parent.parent.parent.workers;
        const sendTo = this.getWorkerByName(node.workerName.value, workerList);
        const parentWorker = this.getWorkerByName(node.parent.parent.name.value, workerList);
        const receiverNode = this.getReceiverForSender(sendTo);
        const heightToCurrentNode = this.getHeightUptoNode(parentWorker.body.statements, node);
        const heightToReceiver = this.getHeightUptoNode(sendTo.body.statements, receiverNode);

        if (heightToCurrentNode > heightToReceiver) {
            receiverNode.viewState.components['drop-zone'].h += (heightToCurrentNode - heightToReceiver);
        } else {
            node.viewState.components['drop-zone'].h += (heightToReceiver - heightToCurrentNode);
        }

        node.viewState.dimensionsSynced = true;
        receiverNode.viewState.dimensionsSynced = true;
    }

    syncWorkerReceiverNode(node) {
        if (node.viewState.dimensionsSynced) {
            return;
        }
        const workerList = node.parent.parent.parent.workers;
        const receiveFrom = this.getWorkerByName(node.workerName.value, workerList);
        const parentWorker = this.getWorkerByName(node.parent.parent.name.value, workerList);
        const senderNode = this.getSenderForReceiver(receiveFrom);
        const heightToCurrentNode = this.getHeightUptoNode(parentWorker.body.statements, node);
        const heightToSender = this.getHeightUptoNode(receiveFrom.body.statements, senderNode);

        if (heightToCurrentNode > heightToSender) {
            senderNode.viewState.components['drop-zone'].h += (heightToCurrentNode - heightToSender);
        } else {
            node.viewState.components['drop-zone'].h += (heightToSender - heightToCurrentNode);
        }

        node.viewState.dimensionsSynced = true;
        senderNode.viewState.dimensionsSynced = true;
    }

    getReceiverForSender(worker) {
        const statements = worker.body.statements;
        const receiverIndex = _.findIndex(statements, (stmt) => {
            return TreeUtil.isWorkerReceive(stmt);
        });

        if (receiverIndex >= 0) {
            return statements[receiverIndex];
        } else {
            return undefined;
        }
    }

    getSenderForReceiver(worker) {
        const statements = worker.body.statements;
        const receiverIndex = _.findIndex(statements, (stmt) => {
            return TreeUtil.isWorkerSend(stmt);
        });

        if (receiverIndex >= 0) {
            return statements[receiverIndex];
        } else {
            return undefined;
        }
    }

    getHeightUptoNode(statements, node) {
        let height = 0;

        for (let itr = 0; itr < statements.length; itr++) {
            height += statements[itr].viewState.bBox.h;
            if (node.id === statements[itr].id) {
                return height;
            }
        }
    }

    getWorkerByName(workerName, workerList) {
        const index = _.findIndex(workerList, (worker) => {
            return worker.name.value === workerName;
        });

        return workerList[index];
    }
}

export default WorkerInvocationSyncUtil;