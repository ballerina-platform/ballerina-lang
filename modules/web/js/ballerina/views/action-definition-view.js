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
define(['lodash', 'log', './ballerina-view', './../ast/action-definition', 'd3utils'],
    function (_, log, BallerinaView, ActionDefinition, D3Utils) {

        /**
         * The view to represent a action definition which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {ActionDefinition} args.model - The action statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var ActionDefinitionView = function (args) {

            BallerinaView.call(this, args);

            if (_.isNil(this._model) || !(this._model instanceof ActionDefinition)) {
                log.error("Action definition undefined or is of different type." + this._model);
                throw "Action definition undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for action definition is undefined." + this._container);
                throw "Container for action definition is undefined." + this._container;
            }

        };

        ActionDefinitionView.prototype = Object.create(BallerinaView.prototype);
        ActionDefinitionView.prototype.constructor = ActionDefinitionView;

        ActionDefinitionView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof ActionDefinition) {
                this._model = model;
            } else {
                log.error("Action definition undefined or is of different type." + model);
                throw "Action definition undefined or is of different type." + model;
            }
        };

        ActionDefinitionView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for action definition is undefined." + container);
                throw "Container for action definition is undefined." + container;
            }
        };

        ActionDefinitionView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        ActionDefinitionView.prototype.getModel = function () {
            return this._model;
        };

        ActionDefinitionView.prototype.getContainer = function () {
            return this._container;
        };

        ActionDefinitionView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        /**
         * Renders the view for action definition.
         * @returns {group} - The SVG group which holds the elements of the action definition.
         */
        ActionDefinitionView.prototype.render = function () {
            var group = D3Utils.group(this._container);
            // TODO : Draw the view of the action definition and add it to the above svg group.
            log.debug("Rendering action view.");
            return group;
        };

        return ActionDefinitionView;
    });