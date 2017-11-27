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

import TreeUtil from './../../../model/tree-util';

/**
 * Visitor util class for synchronizing the worker invocation statements
 */
class WorkerInvocationSyncUtil {

    setDefaultSizingUtil(sizingUtil) {
        this.sizingUtil = sizingUtil;
    }

    syncWorkerSendNode(node) {
        if (node.viewState.dimensionsSynced) {
            return;
        }
        const workerList = node.parent.parent.parent.workers;
        const sendTo = TreeUtil.getWorkerByName(node.workerName.value, workerList);

        if (sendTo) {
            const parentWorker = TreeUtil.getWorkerByName(node.parent.parent.name.value, workerList);
            // TODO: Currently, assume that the sender is a direct child of a worker
            const workerOwningSender = node.parent.parent;
            const receiverNode = TreeUtil.getReceiverForSender(sendTo, workerOwningSender);

            if (receiverNode) {
                const heightToCurrentNode = this.getHeightUptoNode(parentWorker.body.statements, node);
                const heightToReceiver = this.getHeightUptoNode(sendTo.body.statements, receiverNode);

                if (heightToCurrentNode > heightToReceiver) {
                    receiverNode.viewState.components['drop-zone'].h += (heightToCurrentNode - heightToReceiver);
                    receiverNode.viewState.bBox.h += (heightToCurrentNode - heightToReceiver);
                } else {
                    node.viewState.components['drop-zone'].h += (heightToReceiver - heightToCurrentNode);
                    node.viewState.bBox.h += (heightToReceiver - heightToCurrentNode);
                }

                receiverNode.viewState.dimensionsSynced = true;
            }
        }

        node.viewState.dimensionsSynced = true;
    }

    syncWorkerReceiverNode(node) {
        if (node.viewState.dimensionsSynced) {
            return;
        }
        const workerList = node.parent.parent.parent.workers;
        const receiveFrom = TreeUtil.getWorkerByName(node.workerName.value, workerList);

        if (receiveFrom) {
            const parentWorker = TreeUtil.getWorkerByName(node.parent.parent.name.value, workerList);
            const senderNode = TreeUtil.getSenderForReceiver(receiveFrom);

            if (senderNode) {
                const heightToCurrentNode = this.getHeightUptoNode(parentWorker.body.statements, node);
                const heightToSender = this.getHeightUptoNode(receiveFrom.body.statements, senderNode);

                if (heightToCurrentNode > heightToSender) {
                    senderNode.viewState.components['drop-zone'].h += (heightToCurrentNode - heightToSender);
                    senderNode.viewState.bBox.h += (heightToCurrentNode - heightToSender);
                } else {
                    node.viewState.components['drop-zone'].h += (heightToSender - heightToCurrentNode);
                    node.viewState.bBox.h += (heightToSender - heightToCurrentNode);
                }

                senderNode.viewState.dimensionsSynced = true;
            }
        }

        node.viewState.dimensionsSynced = true;
    }

    getHeightUptoNode(statements, node) {
        let height = 0;

        for (let itr = 0; itr < statements.length; itr++) {
            height += statements[itr].viewState.bBox.h;
            if (node.id === statements[itr].id) {
                return height;
            }
        }

        return undefined;
    }

    // Height re-sizing functions

    resizeFunctionNode() {
        // this.sizingUtil.sizeFunctionNode(node);
    }

    resizeResourceNode(node) {
        this.sizingUtil.sizeResourceNode(node);
    }

    resizeServiceNode(node) {
        this.sizingUtil.sizeServiceNode(node);
    }

    resizeWorkerNode(node) {
        this.sizingUtil.sizeWorkerNode(node);
    }

    resizeBlockNode(node) {
        this.sizingUtil.sizeBlockNode(node);
    }

    resizeForkJoinNode(node) {
        this.sizingUtil.sizeForkJoinNode(node);
    }

    resizeIfNode(node) {
        this.sizingUtil.sizeIfNode(node);
    }

    resizeTransactionNode(node) {
        this.sizingUtil.sizeTransactionNode(node);
    }

    resizeTryNode(node) {
        this.sizingUtil.sizeTryNode(node);
    }

    resizeWhileNode(node) {
        this.sizingUtil.sizeWhileNode(node);
    }
}

export default WorkerInvocationSyncUtil;
