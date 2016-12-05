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
define(['lodash', 'log', 'event_channel', 'app/ballerina/ast/throw-statement', 'd3utils'], function (_, log, EventChannel, ThrowStatement, D3Utils) {

    /**
     * The view for the throw statement model.
     * @param model throw statement model.
     * @param container The SVG element.
     * @param viewOptions Options to configure the view.
     * @constructor
     */
    var ThrowStatementView = function (model, container, viewOptions) {
        if (!_.isNil(model) && model instanceof ThrowStatement && !_.isNil(container)) {
            this._model = model;
            this._container = container;
            this._viewOptions = viewOptions;
        } else {
            log.error("Invalid args received for creating a throw statement view. Model: " + model
                + ". Container: " + container);
        }
    };

    ThrowStatementView.prototype = Object.create(EventChannel.prototype);
    ThrowStatementView.prototype.constructor = ThrowStatementView;

    ThrowStatementView.prototype.setModel = function (model) {
        if (!_.isNil(model)) {
            this._model = model;
        } else {
            log.error("Unknown definition received for throw statement.");
        }
    };

    ThrowStatementView.prototype.setContainer = function (container) {
        if (!_.isNil(container)) {
            this._container = container;
        } else {
            log.error("SVG container for the throw statement is null or empty.");
        }
    };

    ThrowStatementView.prototype.setViewOptions = function (viewOptions) {
        this._viewOptions = viewOptions;
    };

    ThrowStatementView.prototype.getModel = function () {
        return this._model;
    };

    ThrowStatementView.prototype.getContainer = function () {
        return this._container;
    };

    ThrowStatementView.prototype.getViewOptions = function () {
        return this._viewOptions;
    };

    ThrowStatementView.prototype.render = function () {
        var group = D3Utils.draw.group(this._container);
        var rect = D3Utils.draw.rect(50, 50, 100, 100, 0, 0, group, "#FFFFFF");
        window.console.log("Rendering the throw Statement.");
        group.rect = rect;
        return group;
    };

    return ThrowStatementView;
});