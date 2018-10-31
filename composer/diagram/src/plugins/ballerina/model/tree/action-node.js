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
import _ from 'lodash';
import AbstractActionNode from './abstract-tree/action-node';
import TreeUtil from './../tree-util';

class ActionNode extends AbstractActionNode {
    /**
     * Indicates whether the given instance of node can be accepted when dropped
     * on top of this node.
     *
     * @param {Node} node Node instance to be dropped
     * @returns {Boolean} True if can be acceped.
     */
    canAcceptDrop(node) {
        return TreeUtil.isWorker(node) || TreeUtil.isEndpointTypeVariableDef(node);
    }

    /**
     * Accept a node which is dropped
     * on top of this node.
     *
     * @param {Node} node Node instance to be dropped
     * @param {Node} dropBefore Drop before given node
     *
     */
    acceptDrop(node, dropBefore) {
        if (TreeUtil.isWorker(node)) {
            // if a worker does not exist we need to create aanother worker for default.
            if (this.getWorkers().length === 0) {
                const defaultWorker = node.meta;
                delete node.meta;
                const connectors = this.getBody().getStatements()
                    .filter((statement) => { return TreeUtil.isEndpointTypeVariableDef(statement); });
                const statements = this.getBody().getStatements()
                    .filter((statement) => { return !TreeUtil.isEndpointTypeVariableDef(statement); });
                this.getBody().setStatements(connectors, true);
                defaultWorker.getBody().setStatements(statements);
                this.addWorkers(defaultWorker, -1, true);
            }
            const index = !_.isNil(dropBefore) ? this.getIndexOfWorkers(dropBefore) : -1;
            TreeUtil.generateWorkerName(this, node);
            this.addWorkers(node, index);
        } else if (TreeUtil.isEndpointTypeVariableDef(node)) {
            // If there are no statements we'll add it to 0
            let index = 0;
            const lastIndexOfConnectors = _.findLastIndex(this.getBody().getStatements(),
                variable => TreeUtil.isEndpointTypeVariableDef(variable));
            if (lastIndexOfConnectors !== -1) {
                index = lastIndexOfConnectors + 1;
            }
            TreeUtil.generateEndpointName(this.getBody(), node);
            this.getBody().addStatements(node, index);
        }
    }

    getClientTitle() {
        return 'caller';
    }
}

export default ActionNode;
