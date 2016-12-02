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
define(['lodash', 'log', './canvas', '../ast/reply-statement', 'd3'], function (_, log, Canvas, ReplyStatement, d3) {

    var ReplyStatementView = function (model, container, viewOptions) {
        if (model instanceof ReplyStatement) {
            this._model = model;
        } else {
            log.error("Unknown definition received for reply statement.");
        }
        this._container = container;
        this._viewOptions = viewOptions;
        //this.super(model, container, viewOptions);
    };

    ReplyStatementView.prototype = Object.create(Canvas.prototype);
    ReplyStatementView.prototype.constructor = ReplyStatementView;

    ReplyStatementView.prototype.setModel = function (model) {
        if (!_.isNil(ReplyStatement)) {
            this._model = model;
        } else {
            log.error("Unknown definition received for reply statement.");
        }
    };

    ReplyStatementView.prototype.setContainer = function (container) {
        this._container = container;
    };

    ReplyStatementView.prototype.setViewOptions = function (viewOptions) {
        this._viewOptions = viewOptions;
    };

    ReplyStatementView.prototype.getModel = function () {
        return this._model;
    };

    ReplyStatementView.prototype.getContainer = function () {
        return this._container;
    };

    ReplyStatementView.prototype.getViewOptions = function () {
        return this._viewOptions;
    };

    ReplyStatementView.prototype.render = function () {
        //Draw ReplyStatement

    };

    ReplyStatement.prototype.accept = function (visitor) {
        visitor.visitReplyStatementChildren(this);

        //invoke accept methods of children
    };

    ReplyStatement.prototype.visitChildren = function () {
        //invoke accept methods of children
    };

});