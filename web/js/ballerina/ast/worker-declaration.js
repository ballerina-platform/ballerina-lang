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
define(['lodash', './node', '../utils/common-utils'], function (_, ASTNode, CommonUtils) {

    var WorkerDeclaration = function (args) {
        this._isDefaultWorker = _.get(args, "isDefaultWorker", false);
        this._reply = _.get(args, "replyStatement", null);
        this._childrenList = [];
        this._workerDeclarationStatement = _.get(args, 'declarationStatement', 'worker1(message m)');

        ASTNode.call(this, "WorkerDeclaration");
    };

    WorkerDeclaration.prototype = Object.create(ASTNode.prototype);
    WorkerDeclaration.prototype.constructor = WorkerDeclaration;

    WorkerDeclaration.prototype.setIsDefaultWorker = function (isDefaultWorker, options) {
        if (!_.isNil(isDefaultWorker)) {
            this.setAttribute('_isDefaultWorker', isDefaultWorker, options);
        }
    };

    WorkerDeclaration.prototype.getReply = function () {
        return this._reply;
    };

    WorkerDeclaration.prototype.isDefaultWorker = function () {
        return this._isDefaultWorker;
    };

    /**
     * Set the worker declaration statement [workerName(message m)]
     * @param {string} declarationStatement
     */
    WorkerDeclaration.prototype.setWorkerDeclarationStatement = function (declarationStatement) {
        this._workerDeclarationStatement = declarationStatement;
    };

    /**
     * Get the worker declaration statement
     * @return {string} _workerDeclarationStatement
     */
    WorkerDeclaration.prototype.getWorkerDeclarationStatement = function () {
        return this._workerDeclarationStatement;
    };

    WorkerDeclaration.prototype.getWorkerName = function () {
        return "workerName";
    };

    /**
     * @inheritDoc
     * @override
     */
    WorkerDeclaration.prototype.generateUniqueIdentifiers = function () {
        // TODO : Implement
        // CommonUtils.generateUniqueIdentifier({
        //     node: this,
        //     attributes: [{
        //         defaultValue: "newAction",
        //         setter: this.setActionName,
        //         getter: this.getActionName,
        //         parents: [{
        //             // ballerina-ast-node
        //             node: this.parent,
        //             getChildrenFunc: this.parent.getConnectorActionDefinitions,
        //             getter: this.getActionName
        //         }]
        //     }]
        // });
    };

    return WorkerDeclaration;
});
