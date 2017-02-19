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
        this._workerDeclarationStatement = _.get(args, 'declarationStatement', '');
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
    WorkerDeclaration.prototype.setWorkerDeclarationStatement = function (declarationStatement, options) {
        this.setAttribute('_workerDeclarationStatement', declarationStatement, options);
        var tokens = this._workerDeclarationStatement.split("(");
        this.setWorkerName(tokens[0].trim());
    };

    /**
     * Get the worker declaration statement
     * @return {string} _workerDeclarationStatement
     */
    WorkerDeclaration.prototype.getWorkerDeclarationStatement = function () {
        if (this._workerDeclarationStatement === '') {
            return this.getWorkerName() + "(message m)";
        } else {
            return this._workerDeclarationStatement;
        }
    };

    WorkerDeclaration.prototype.getWorkerName = function () {
        return this._workerName;
    };

    WorkerDeclaration.prototype.setWorkerName = function (workerName, options) {
        this.setAttribute('_workerName', workerName, options);
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
     * @param {object} options
     */
    WorkerDeclaration.prototype.setInvoker = function (invoker, options) {
        this.setAttribute('_invoker', invoker, options);
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
     * @param {object} options
     */
    WorkerDeclaration.prototype.setReplyReceiver = function (replyReceiver, options) {
        this.setAttribute('_replyReceiver', replyReceiver, options);
    };
    
    WorkerDeclaration.prototype.addArgument = function (paramType, paramName) {
        this.getArgumentsList().push({
            parameter_type: paramType,
            parameter_name: paramName
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

        this.setWorkerDeclarationStatement(this.getWorkerName() + "(" + this.getArgumentsAsString() + ")");

        // TODO: check whether return types are allowed
        _.each(jsonNode.children, function (childNode) {
            var child = undefined;
            var childNodeTemp = undefined;
            if (childNode.type === "variable_definition_statement" && !_.isNil(childNode.children[1]) && childNode.children[1].type === 'connector_init_expr') {
                child = BallerinaASTFactory.createConnectorDeclaration();
                childNodeTemp = childNode;
            } else {
                child = BallerinaASTFactory.createFromJson(childNode);
                childNodeTemp = childNode;
            }
            self.addChild(child);
            child.initFromJson(childNodeTemp);
        });
    };

    /**
     * @inheritDoc
     * @override
     */
    WorkerDeclaration.prototype.generateUniqueIdentifiers = function () {
        CommonUtils.generateUniqueIdentifier({
            node: this,
            attributes: [{
                defaultValue: "newWorker",
                setter: this.setWorkerName,
                getter: this.getWorkerName,
                parents: [{
                    // function-def/connector-action/resource
                    node: this.parent,
                    getChildrenFunc: this.parent.getWorkerDeclarations,
                    getter: this.getWorkerName
                }]
            }]
        });
    };

    /**
     * Returns the list of arguments as a string separated by commas.
     * @return {string} - Arguments as string.
     */
    WorkerDeclaration.prototype.getArgumentsAsString = function () {
        var argsAsString = "";
        var args = this.getArgumentsList();
        _.forEach(args, function(argument, index){
            argsAsString += argument.parameter_type + " ";
            argsAsString += argument.parameter_name;
            if (args.length - 1 != index) {
                argsAsString += " , ";
            }
        });
        return argsAsString;
    };

    return WorkerDeclaration;
});
