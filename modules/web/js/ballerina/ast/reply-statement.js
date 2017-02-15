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
define(['lodash', 'log', './statement'], function (_, log, Statement) {

    /**
     * Class for reply statement in ballerina.
     * @param message expression of a reply statement.
     * @constructor
     */
    var ReplyStatement = function (args) {
        Statement.call(this);
        this._message = _.get(args, 'message') || '';
        this.type = "ReplyStatement";
    };

    ReplyStatement.prototype = Object.create(Statement.prototype);
    ReplyStatement.prototype.constructor = ReplyStatement;

    ReplyStatement.prototype.setReplyMessage = function (message, options) {
        if (!_.isNil(message)) {
            this.setAttribute('_message', message, options);
        } else {
            log.error("Cannot set undefined to the reply statement.");
        }
    };

    ReplyStatement.prototype.getReplyMessage = function () {
        return this._message;
    };

    ReplyStatement.prototype.canBeAChildOf = function (node) {
        return this.getFactory().isResourceDefinition(node)
                || this.getFactory().isWorkerDeclaration(node)
                || this.getFactory().isStatement(node);
    };

    /**
     * initialize from json
     * @param jsonNode
     */
    ReplyStatement.prototype.initFromJson = function (jsonNode) {
        this.setReplyMessage(jsonNode.expression, {doSilently: true});
    };

    ReplyStatement.prototype.getReplyExpression = function () {
        return "reply " + this.getReplyMessage();
    };

    return ReplyStatement;
});
