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
define(['lodash', 'log', 'event_channel', 'app/ballerina/ast/while-statement', 'd3utils'], function (_, log, EventChannel, WhileStatement, D3Utils) {

    /**
     * The view for the while statement model.
     * @param model return statement model.
     * @param container The SVG element.
     * @param viewOptions Options to configure the view.
     * @constructor
     */
    var WhileStatementView = function (model, container, viewOptions) {
        if (!_.isNil(model) && model instanceof WhileStatement && !_.isNil(container)) {
            this._model = model;
            this._container = container;
            this._viewOptions = viewOptions;
        } else {
            log.error("Invalid args received for creating a while statement view. Model: " + model
                + ". Container: " + container);
        }
    };

    WhileStatementView.prototype = Object.create(EventChannel.prototype);
    WhileStatementView.prototype.constructor = WhileStatementView;

    WhileStatementView.prototype.setModel = function (model) {
        if (!_.isNil(model)) {
            this._model = model;
        } else {
            log.error("Unknown definition received for while statement.");
        }
    };

    WhileStatementView.prototype.setContainer = function (container) {
        if (!_.isNil(container)) {
            this._container = container;
        } else {
            log.error("SVG container for the while statement is null or empty.");
        }
    };

    WhileStatementView.prototype.setViewOptions = function (viewOptions) {
        this._viewOptions = viewOptions;
    };

    WhileStatementView.prototype.getModel = function () {
        return this._model;
    };

    WhileStatementView.prototype.getContainer = function () {
        return this._container;
    };

    WhileStatementView.prototype.getViewOptions = function () {
        return this._viewOptions;
    };

    WhileStatementView.prototype.render = function () {
        //Draw while statement
    };

});