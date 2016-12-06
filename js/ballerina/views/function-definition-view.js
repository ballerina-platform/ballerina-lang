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
define(['lodash', 'log', 'event_channel',  './canvas', './../ast/function-definition'],
    function (_, log, EventChannel, Canvas, FunctionDefinition) {

        /**
         * View for the function definition model.
         * @param model - Function definition model.
         * @param container - the SVG container.
         * @param viewOptions - Options to configure the view
         */
        var FunctionDefinitionView = function (model, container, viewOptions) {
            if (!_.isNil(model) && model instanceof FunctionDefinition && !_.isNil(container)) {
                this._model = model;
                this._container = container;
                this._viewOptions = viewOptions;
            } else {
                log.error("Invalid args received for creating a function definition view. Model: " + model
                    + ". Container: " + container);
            }

        };

        FunctionDefinitionView.prototype = Object.create(EventChannel.prototype);
        FunctionDefinitionView.prototype.constructor = FunctionDefinitionView;

        FunctionDefinitionView.prototype.setModel = function (model) {
            if (!_.isNil(model)) {
                this._model = model;
            } else {
                log.error("Unknown definition received for function definition.");
            }
        };

        FunctionDefinitionView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("SVG container for the function is null or empty.");
            }
        };

        FunctionDefinitionView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        FunctionDefinitionView.prototype.getModel = function () {
            return this._model;
        };

        FunctionDefinitionView.prototype.getContainer = function () {
            return this._container;
        };

        FunctionDefinitionView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        FunctionDefinitionView.prototype.render = function () {
        };

        return FunctionDefinitionView;
    });