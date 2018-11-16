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
import AbstractBlockNode from './abstract-tree/block-node';
import TreeUtil from './../tree-util';
import { ASTUtil } from "ast-model";
import Tree from 'plugins/ballerina/diagram/views/default/components/transformer/tree';
class BlockNode extends AbstractBlockNode {

    /**
     * Indicates whether the given instance of node can be accepted when dropped
     * on top of this node.
     *
     * @param {Node} node Node instance to be dropped
     * @param {Node} dropBefore before the node
     * @returns {Boolean} True if can be accepted.
     */
    canAcceptDrop(node, dropBefore) {
        const existingStatements = this.statements;
        let lastStatement;
        let isLastStatementReturn = false;
        let isLastStatementNext = false;
        if (!dropBefore && existingStatements.length > 0) {
            lastStatement = existingStatements[existingStatements.length - 1];
            isLastStatementReturn = lastStatement ? TreeUtil.isReturn(lastStatement) : false;
            isLastStatementNext = lastStatement ? TreeUtil.isNext(lastStatement) : false;
        }
        return (node.isStatement && !TreeUtil.isEndpointTypeVariableDef(node) && !isLastStatementReturn
            && !isLastStatementNext) || TreeUtil.isXmlns(node);
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
        const index = !_.isNil(dropBefore) ? this.getIndexOfStatements(dropBefore) : -1;
        if (TreeUtil.isAssignment(node)) {
            const variables = node.getVariables();
            variables.forEach((variable, i) => {
                variable.getVariableName().setValue(TreeUtil.generateVariableName(this, 'var', i));
            });
            ASTUtil.reconcileWS(node, this.getStatements(), this.getRoot(), this.getBlockStartWs());
            this.addStatements(node, index);
        } else if (TreeUtil.isVariableDef(node)) {
            node.getVariable().getName().setValue(TreeUtil.generateVariableName(this, 'var'));
            ASTUtil.reconcileWS(node, this.getStatements(), this.getRoot(), this.getBlockStartWs());
            this.addStatements(node, index);
        } else {
            ASTUtil.reconcileWS(node, this.getStatements(), this.getRoot(), this.getBlockStartWs());
            this.addStatements(node, index);
        }
    }


    getBlockStartWs() {
        if (this.getStatements().length > 0) {
            return -1;
        }
        // If parent is a function or a resource and has endpoints
        if (TreeUtil.isFunction(this.parent) || TreeUtil.isResource(this.parent)) {
            const epCount = this.parent.endpointNodes.length;
            if (epCount > 0) {
                const lastEp = this.parent.endpointNodes[epCount - 1];
                const endpointWSList = ASTUtil.extractWS(lastEp);
                return endpointWSList.pop().i + 1;
            }
        }

        const wsList = ASTUtil.extractWS(this.parent);
        const startWs = _.find(wsList, (element) => {
            return (element.text === "{");
        });
        return startWs.i + 1;
    }
}

export default BlockNode;
