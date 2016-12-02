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
define(['lodash', 'log', 'event_channel', 'app/ballerina/ast/return-statement', 'd3utils'], function (_, log, EventChannel, ReturnStatement, D3Utils) {

    var ReturnStatementView = function (model, container, viewOptions) {
        if (!_.isNil(model) && model instanceof ReturnStatement && !_.isNil(container)) {
            this._model = model;
            this._container = container;
            this._viewOptions = viewOptions;
        } else {
            log.error("Invalid args received for creating a return statement view. Model: " + model
                + ". Container: " + container);
        }
    };

    ReturnStatementView.prototype = Object.create(EventChannel.prototype);
    ReturnStatementView.prototype.constructor = ReturnStatementView;

    ReturnStatementView.prototype.setModel = function (model) {
        if (!_.isNil(model)) {
            this._model = model;
        } else {
            log.error("Unknown definition received for return statement.");
        }
    };

    ReturnStatementView.prototype.setContainer = function (container) {
        if (!_.isNil(container)) {
            this._container = container;
        } else {
            log.error("SVG container for the return statement is null or empty.");
        }
    };

    ReturnStatementView.prototype.setViewOptions = function (viewOptions) {
        this._viewOptions = viewOptions;
    };

    ReturnStatementView.prototype.getModel = function () {
        return this._model;
    };

    ReturnStatementView.prototype.getContainer = function () {
        return this._container;
    };

    ReturnStatementView.prototype.getViewOptions = function () {
        return this._viewOptions;
    };

    ReturnStatementView.prototype.render = function () {
        var group = D3Utils.draw.group(this._container);
        var rect = D3Utils.draw.rect(10, 10, 100, 100, 0, 0, group, "#FFFFFF");
        window.console.log("Rendering the Return Statement.");
        group.rect = rect;
        return group;
    };

});