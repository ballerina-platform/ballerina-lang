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
define(['lodash', 'log', './canvas', 'ast/service-definition'],
    function (_, log, Canvas, ServiceDefinition) {

        /**
         * The view for the service definition model.
         * @param model Service definition model.
         * @param container The SVG element.
         * @param viewOptions Options to configure the view.
         * @constructor
         */
        var ServiceDefinitionView = function (model, container, viewOptions) {
            if (!_.isNull(model) && model instanceof ServiceDefinition && !_.isNil(container)) {
                this._model = model;
                this._container = container;
                this._viewOptions = viewOptions;
            } else {
                log.error("Invalid args received for creating a service definition. Model: " + model + ". Container: " +
                    container);
            }
        };

        ServiceDefinitionView.prototype = Object.create(Canvas.prototype);
        ServiceDefinitionView.prototype.constructor = ServiceDefinitionView;

        ServiceDefinitionView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof ServiceDefinition) {
                this._model = model;
            } else {
                log.error("Unknown definition received for service definition. Model: " + model);
            }
        };

        ServiceDefinitionView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("SVG container for the service is null or empty.");
            }
        };

        ServiceDefinitionView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        ServiceDefinitionView.prototype.getModel = function () {
            return this._model;
        };

        ServiceDefinitionView.prototype.getContainer = function () {
            return this._container;
        };

        ServiceDefinitionView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        ServiceDefinitionView.prototype.render = function () {

        };

        return ServiceDefinitionView;
    });