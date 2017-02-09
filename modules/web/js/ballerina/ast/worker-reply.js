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
    var WorkerReply = function (args) {
        Statement.call(this, 'WorkerReply');
        this._source = _.get(args, 'source');
        this._destination = _.get(args, 'destination');
        this._message = _.get(args, 'message');
    };

    WorkerReply.prototype = Object.create(Statement.prototype);
    WorkerReply.prototype.constructor = WorkerReply;

    WorkerReply.prototype.setSource = function (source) {
        this._source = source;
    };

    WorkerReply.prototype.getSource = function () {
        return this._source;
    };

    WorkerReply.prototype.setDestination = function (destination) {
        this._destination = destination;
    };

    WorkerReply.prototype.getDestination = function () {
        return this._destination;
    };

    WorkerReply.prototype.setMessage = function (message) {
        this._message = message;
    };

    WorkerReply.prototype.getMessage = function () {
        return this._message;
    };

    /**
     * initialize from json
     * @param jsonNode
     */
    WorkerReply.prototype.initFromJson = function (jsonNode) {
        this.setExpression('a = b', {doSilently: true});
    };

    return WorkerReply;
});
