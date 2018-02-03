/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import AbstractForkJoinNode from './abstract-tree/fork-join-node';

class ForkJoinNode extends AbstractForkJoinNode {
    /**
     * Generate default name for fork block level workers.
     * @param {Node} parent - parent node.
     * @param {Node} node - current node.
     * @return {Object} undefined if unsuccessful.
     * */
    generateDefaultName(parent, node) {
        if (!parent) {
            return undefined;
        }

        const workerDefaultName = 'worker';
        const workerNodes = parent.getWorkers();
        const names = {};
        for (let i = 0; i < workerNodes.length; i++) {
            const name = workerNodes[i].getName().value;
            names[name] = name;
        }

        if (workerNodes.length > 0) {
            for (let j = 1; j <= workerNodes.length; j++) {
                if (!names[`${workerDefaultName}${j}`]) {
                    node.getName().setValue(`${workerDefaultName}${j}`, true);
                    node.setName(node.getName(), false);
                    break;
                }
            }
        } else {
            node.getName().setValue(`${workerDefaultName}1`, true);
            node.setName(node.getName(), false);
        }
        return undefined;
    }

    /**
     * Get join condition as a string.
     * @return {string} join condition.
     * */
    getJoinConditionString() {
        const forkJoinNode = this;
        const joinWorkers = forkJoinNode.getJoinedWorkerIdentifiers();
        const joinType = forkJoinNode.getJoinType();
        const joinCount = forkJoinNode.getJoinCount();

        let workers = '';
        for (let i = 0; i < joinWorkers.length; i++) {
            workers += joinWorkers[i].getValue();
            if (i < joinWorkers.length - 1) {
                workers += ', ';
            }
        }
        let condition;
        if (joinType === 'some') {
            condition = 'some ' + (joinCount !== -1 ? joinCount : '') + workers;
        } else {
            condition = joinType + ' ' + workers;
        }
        return condition;
    }

    /**
     * set whether the children are compound
     * */
    setChildrenCompoundStatus() {
        if (this.joinBody) {
            this.joinBody.viewState.compound = true;
        }

        if (this.timeoutBody) {
            this.timeoutBody.viewState.compound = true;
        }
    }

    /**
     * Indicates whether the given instance of node can be accepted when dropped
     * on top of this node.
     * @param {Node} node Node instance to be dropped.
     * @returns {Boolean} True if can be acceped.
     */
    canAcceptDrop(node) {
        return node.kind === 'Worker';
    }

    /**
     * Accept a node which is dropped
     * on top of this node.
     *
     * @param {Node} node Node instance to be dropped
     * @param {Node} dropBefore Drop before given node
     */
    acceptDrop(node, dropBefore) {
        this.addWorkers(node);
        this.generateDefaultName(this, node);
    }
}

export default ForkJoinNode;
