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
import AbstractFunctionNode from './abstract-tree/function-node';
import TreeUtil from './../tree-util';
import { ASTUtil } from "ast-model";

class FunctionNode extends AbstractFunctionNode {
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
                // If endpoints are defined should be last of endpoint
                let defaultWorkerStartPosition = ASTUtil.getStartPosition(this, "workers");
                ASTUtil.reconcileWS(defaultWorker, this.getWorkers(), this.getRoot(), defaultWorkerStartPosition);
                this.addWorkers(defaultWorker, -1, true);

                // Iterate statements and reconcile whitespaces to match the default worker.
                for (let statement in statements) {
                    let startPosition = ASTUtil.getStartPosition(defaultWorker, "statements");
                    ASTUtil.reconcileWS(statements[statement], defaultWorker.getBody().getStatements(),
                        this.getRoot(), startPosition);
                    defaultWorker.getBody().addStatements(statements[statement], -1, true);
                }
            }
            const index = !_.isNil(dropBefore) ? this.getIndexOfWorkers(dropBefore) : -1;
            TreeUtil.generateWorkerName(this, node);

            // This will add to end of worker array
            ASTUtil.reconcileWS(node, this.getWorkers(), this.getRoot());
            this.addWorkers(node, index);
        } else if (TreeUtil.isEndpoint(node)) {
            let startPosition = ASTUtil.getStartPosition(this, "endpointNodes");
            ASTUtil.reconcileWS(node, this.getEndpointNodes(), this.getRoot(), startPosition);
            this.addEndpointNodes(node);
        }
    }

    getClientTitle() {
        return 'caller';
    }
}

export default FunctionNode;
