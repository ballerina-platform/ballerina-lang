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

import AbstractNextNode from './abstract-tree/next-node';

/**
 * Class for Next node model.
 * @abstract AbstractNextNode
 * @class NextNode
 * */
class NextNode extends AbstractNextNode {

    /**
     * check whether this can be dropped on to the drop target.
     * @param {Node} dropTarget - node that this dragging node to be added to.
     * @param {Node} dropBefore - node which is adding this dragging node before.
     * @return {boolean} true if can be dropped, false if not.
     * */
    canBeDropped(dropTarget, dropBefore) {
        const transactionNode = this.findIterativeBlock(dropTarget);
        return (transactionNode ? transactionNode.kind === 'While' : false) && !dropBefore;
    }

    /**
     * Traverse back up the tree and find the iterative block.
     * @param {Node} node - drop target node.
     * @return {Node} Iterative block or undefined.
     * */
    findIterativeBlock(node) {
        if (node && node.kind && node.kind !== 'While') {
            return this.findIterativeBlock(node.parent);
        } else {
            return node;
        }
    }
}

export default NextNode;
