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

import AbstractAbortNode from './abstract-tree/abort-node';

/**
 * Class for abort node.
 * @extends AbstractAbortNode
 * @class AbortNode
 * */
class AbortNode extends AbstractAbortNode {
    /**
     * check whether this can be dropped on to the drop target.
     * @param {Node} dropTarget - node that this dragging node to be added to.
     * @param {Node} dropBefore - drop before node
     * @return {boolean} true if can be dropped, false if not.
     * */
    canBeDropped(dropTarget, dropBefore) {
        const transactionNode = this.findTransaction(dropTarget);
        return transactionNode ? transactionNode.viewState.alias === 'Transaction' : false;
    }

    /**
     * Traverse back up the tree and find the transactionBlock.
     * @param {Node} node - drop target node.
     * @return {Node} Transaction block or undefined.
     * */
    findTransaction(node) {
        if (node && node.viewState && node.viewState.alias !== 'Transaction') {
            return this.findTransaction(node.parent);
        } else {
            return node;
        }
    }
}

export default AbortNode;
