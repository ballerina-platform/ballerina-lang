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
define(['lodash', 'jquery', 'event_channel', 'log', './canvas', 'app/ballerina/ast/function-definition'],
    function (_, $, EventChannel, log, Canvas, FunctionDefinition) {
        /**
         * View for the function definition model.
         * @param model - Function definition model.
         * @param container - the SVG container.
         * @param viewOptions - Options to configure the view
         */
        var FunctionDefinitionView = function (model) {
            this._model = model;
            this._container = container;
            this._viewOptions = viewOptions
        };

        FunctionDefinitionView.prototype = Object.create(EventChannel.prototype);
        FunctionDefinitionView.prototype.constructor = FunctionDefinitionView;

        FunctionDefinitionView.prototype.init = function (model, container) {
            if (_.isUndefined(model)) {
                log.error('Model definition undefined');
                throw 'Model definition undefined'
            }
            this._model = model;

            if (_.isUndefined(container)) {
                log.error('Container is undefined');
                throw 'Container is undefined';
            }
            this._container = container;
        }

        FunctionDefinition.prototype.setModel = function (model) {
            if (!_.isNil(model)) {
                this._model = model;
            } else {
                log.error("Received unknown Function definition");
            }
        };

        FunctionDefinitionView.prototype.getModel = function () {
            return this.model;
        };

        FunctionDefinitionView.prototype.setContainer = function (container) {
            if (!_.isNil()) {
                this._container = container
            }
        };

        FunctionDefinitionView.prototype.getContainer = function () {
            return this._container;
        };

        FunctionDefinitionView.prototype.visitFunctionDefinition = function () {
        };

        FunctionDefinitionView.prototype.render = function () {
            var d3Ref = this.getD3Ref();
            var rect = d3Ref.draw.rectWithTitle(
                center,
                60,
                30,
                150,
                model.getHeight(),
                0,
                0,
                d3Ref,
                this.modelAttr('viewAttributes').colour,
                this.modelAttr('title')
            );

        };
    });