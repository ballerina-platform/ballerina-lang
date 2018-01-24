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
import AbstractConnectorNode from './abstract-tree/connector-node';
import TreeUtil from './../tree-util';

class ConnectorNode extends AbstractConnectorNode {
    /**
     * Generate default name for service level statements.
     * @param {Node} parent - parent node.
     * @param {Node} node - current node.
     * @return {Object} undefined if unsuccessful.
     * */
    generateDefaultName(parent, node) {
        if (!parent) {
            return undefined;
        }

        const actionDefaultName = 'action';
        const actionNodes = parent.getActions();
        const names = {};
        for (let i = 0; i < actionNodes.length; i++) {
            const name = actionNodes[i].getName().value;
            names[name] = name;
        }

        if (actionNodes.length > 0) {
            for (let j = 1; j <= actionNodes.length; j++) {
                if (!names[`${actionDefaultName}${j}`]) {
                    node.getName().setValue(`${actionDefaultName}${j}`, true);
                    node.setName(node.getName(), false);
                    break;
                }
            }
        } else {
            node.getName().setValue(`${actionDefaultName}1`, true);
            node.setName(node.getName(), false);
        }
        return undefined;
    }

    /**
     * Indicates whether the given instance of node can be accepted when dropped
     * on top of this node.
     *
     * @param {Node} node Node instance to be dropped
     * @returns {Boolean} True if can be acceped.
     */
    canAcceptDrop(node) {
        return TreeUtil.isEndpointTypeVariableDef(node) || TreeUtil.isAction(node);
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
        if (TreeUtil.isEndpointTypeVariableDef(node)) {
            // If there are no variables we'll add it to 0
            let index = 0;
            const lastIndexOfEndpoints = _.findLastIndex(this.getVariableDefs(),
                variable => TreeUtil.isEndpointTypeVariableDef(variable));
            if (lastIndexOfEndpoints !== -1) {
                index = lastIndexOfEndpoints + 1;
            }
            node.getVariable().getName().setValue(TreeUtil.getNewTempVarName(this.getBody(), 'endpoint'));
            this.getBody().addStatements(node, index);
        } else if (TreeUtil.isAction(node)) {
            const index = !_.isNil(dropBefore) ? this.getIndexOfActions(dropBefore) : -1;
            this.addActions(node, index);
            this.generateDefaultName(this, node);
        }
    }

}

export default ConnectorNode;
