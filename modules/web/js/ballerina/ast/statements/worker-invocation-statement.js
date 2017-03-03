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
define(['lodash', './statement'], function (_, Statement) {

    /**
     * Class to represent a worker invocation statement in ballerina.
     * @constructor
     */
    var WorkerInvocationStatement = function (args) {
        Statement.call(this, 'WorkerInvocationStatement');
        this._source = _.get(args, 'source');
        this._destination = _.get(args, 'destination');
        this._message = _.get(args, 'message', 'm');
        this._invokeStatement = _.get(args, 'invokeStatement', 'messageName -> workerName');
    };

    WorkerInvocationStatement.prototype = Object.create(Statement.prototype);
    WorkerInvocationStatement.prototype.constructor = WorkerInvocationStatement;

    WorkerInvocationStatement.prototype.setSource = function (source) {
        this._source = source;
    };

    WorkerInvocationStatement.prototype.getSource = function () {
        return this._source;
    };

    WorkerInvocationStatement.prototype.setDestination = function (destination) {
        this._destination = destination;
    };

    WorkerInvocationStatement.prototype.getDestination = function () {
        return this._destination;
    };

    WorkerInvocationStatement.prototype.setMessage = function (message) {
        this._message = message;
    };

    WorkerInvocationStatement.prototype.getMessage = function () {
        return this._message;
    };

    WorkerInvocationStatement.prototype.setInvocationStatement = function (invocationStatement) {
        this._invokeStatement = invocationStatement;
    };

    WorkerInvocationStatement.prototype.getInvocationStatement = function () {
        return this._invokeStatement;
    };

    WorkerInvocationStatement.prototype.canBeAChildOf = function(node){
        return this.getFactory().isResourceDefinition(node)
            || this.getFactory().isFunctionDefinition(node)
            || this.getFactory().isConnectorAction(node)
            || (this.getFactory().isStatement(node) && !node._isChildOfWorker);
    };

    /**
     * initialize from json
     * @param jsonNode
     */
    WorkerInvocationStatement.prototype.initFromJson = function (jsonNode) {
        var workerName = jsonNode.worker_name;
        var messageName = jsonNode.invoke_message[0].variable_reference_name;
        var invokeStatement = messageName + ' -> ' + workerName;
        this.setInvocationStatement(invokeStatement);
        var self = this;
        var workerInstance = _.find(this.getParent().getChildren(), function (child) {
            return self.getFactory().isWorkerDeclaration(child) && !child.isDefaultWorker() && child.getWorkerName() === workerName;
        });
        this.setDestination(workerInstance);
    };

    return WorkerInvocationStatement;
});
