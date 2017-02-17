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
        this._invoker = undefined;
        this._replyReceiver = undefined;
        this._workerName = undefined;
        this._argumentsList = [];

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
        var tokens = this._workerDeclarationStatement.split("(");
        this.setWorkerName(tokens[0].trim());
    };

    /**
     * Get the worker declaration statement
     * @return {string} _workerDeclarationStatement
     */
    WorkerDeclaration.prototype.getWorkerDeclarationStatement = function () {
        return this._workerDeclarationStatement;
    };

    WorkerDeclaration.prototype.getWorkerName = function () {
        return this._workerName;
    };

    WorkerDeclaration.prototype.setWorkerName = function (workerName) {
        this._workerName = workerName;
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

    /**
     * Get the invoker statement
     * @return {ASTNode} invoker statement
     */
    WorkerDeclaration.prototype.getInvoker = function () {
        return this._invoker;
    };

    /**
     * Set the invoker statement
     * @param {ASTNode} invoker
     */
    WorkerDeclaration.prototype.setInvoker = function (invoker) {
        this._invoker = invoker;
    };

    /**
     * Get the invoker statement
     * @return {ASTNode} reply receiver statement
     */
    WorkerDeclaration.prototype.getReplyReceiver = function () {
        return this._replyReceiver;
    };

    /**
     * Set the reply receiver statement
     * @param {ASTNode} replyReceiver
     */
    WorkerDeclaration.prototype.setReplyReceiver = function (replyReceiver) {
        this._replyReceiver = replyReceiver;
    };
    
    WorkerDeclaration.prototype.addArgument = function (paramType, paramName) {
        this.getArgumentsList().push({
            parameter_type: paramName,
            parameter_name: paramType
        });
    };

    WorkerDeclaration.prototype.getArgumentsList = function () {
        return this._argumentsList;
    };
    WorkerDeclaration.prototype.initFromJson = function (jsonNode) {
        var self = this;
        var BallerinaASTFactory = this.getFactory();
        this.setWorkerName(jsonNode.worker_name);
        var args = jsonNode.argument_declaration;

        _.forEach(args, function(argument) {
            self.addArgument(argument.parameter_type, argument.parameter_name);
        });

        // TODO: check whether return types are allowed
        _.each(jsonNode.children, function (childNode) {
            var child = undefined;
            var childNodeTemp = undefined;
            if (childNode.type === "variable_definition_statement" && !_.isNil(childNode.children[1]) && childNode.children[1].type === 'connector_init_expr') {
                child = BallerinaASTFactory.createConnectorDeclaration();
                childNodeTemp = childNode;
            } else if (childNode.type === "variable_definition_statement" && !_.isNil(childNode.children[1]) && childNode.children[1].type === 'action_invocation_expression') {
                child = BallerinaASTFactory.createActionInvocationExpression();
                childNodeTemp = childNode;
            } else if (childNode.type === "assignment_statement" && childNode.children[1].children[0].type === "action_invocation_expression") {
                child = BallerinaASTFactory.createActionInvocationExpression();
                childNodeTemp = {};
                childNodeTemp.children = [childNode.children[0].children[0], childNode.children[1].children[0]];
            } else if (childNode.type === "action_invocation_statement") {
                child = BallerinaASTFactory.createActionInvocationExpression();
                childNodeTemp = {};
                childNodeTemp.children = [undefined, childNode.children[0]];
            } else {
                child = BallerinaASTFactory.createFromJson(childNode);
                childNodeTemp = childNode;
            }
            self.addChild(child);
            child.initFromJson(childNodeTemp);
        });
    };

    return WorkerDeclaration;
});
