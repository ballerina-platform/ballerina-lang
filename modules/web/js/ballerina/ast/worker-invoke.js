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
     * Class to represent an assignment in ballerina.
     * @constructor
     */
    var WorkerInvoker = function (args) {
        Statement.call(this, 'WorkerInvoker');
        this._source = _.get(args, 'source');
        this._destination = _.get(args, 'destination');
        this._message = _.get(args, 'message', 'm');
        this._invokeStatement = _.get(args, 'invokeStatement', 'messageName -> workerName');
    };

    WorkerInvoker.prototype = Object.create(Statement.prototype);
    WorkerInvoker.prototype.constructor = WorkerInvoker;

    WorkerInvoker.prototype.setSource = function (source) {
        this._source = source;
    };

    WorkerInvoker.prototype.getSource = function () {
        return this._source;
    };

    WorkerInvoker.prototype.setDestination = function (destination) {
        this._destination = destination;
    };

    WorkerInvoker.prototype.getDestination = function () {
        return this._destination;
    };

    WorkerInvoker.prototype.setMessage = function (message) {
        this._message = message;
    };

    WorkerInvoker.prototype.getMessage = function () {
        return this._message;
    };

    WorkerInvoker.prototype.setInvokeStatement = function (invokeStatement) {
        this._invokeStatement = invokeStatement;
    };

    WorkerInvoker.prototype.getInvokeStatement = function () {
        return this._invokeStatement;
    };

    WorkerInvoker.prototype.canBeAChildOf = function(node){
        return this.getFactory().isResourceDefinition(node)
            || this.getFactory().isFunctionDefinition(node)
            || this.getFactory().isConnectorAction(node)
            || (this.getFactory().isStatement(node) && !node._isChildOfWorker);
    };

    /**
     * initialize from json
     * @param jsonNode
     */
    WorkerInvoker.prototype.initFromJson = function (jsonNode) {
        var workerName = jsonNode.worker_name;
        var messageName = jsonNode.invoke_message[0].variable_reference_name;
        var invokeStatement = messageName + ' -> ' + workerName;
        this.setInvokeStatement(invokeStatement);
        var self = this;
        var workerInstance = _.find(this.getParent().getChildren(), function (child) {
            return self.getFactory().isWorkerDeclaration(child) && !child.isDefaultWorker() && child.getWorkerName() === workerName;
        });
        this.setDestination(workerInstance);
    };

    return WorkerInvoker;
});
