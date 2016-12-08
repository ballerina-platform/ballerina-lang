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
         * The view to represent a function definition which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {FunctionDefinition} args.model - The function definition model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var FunctionDefinitionView = function (args) {
            this._model = _.get(args, "model");
            this._container = _.get(args, "container");
            this._viewOptions = _.get(args, "viewOptions", {});

            if (_.isNil(this._model) || !(this._model instanceof FunctionDefinition)) {
                log.error("Function definition is undefined or is of different type." + this._model);
                throw "Function definition is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for function definition is undefined." + this._container);
                throw "Container for function definition is undefined." + this._container;
            }

            Canvas.call(this);

        };

        FunctionDefinitionView.prototype = Object.create(Canvas.prototype);
        FunctionDefinitionView.prototype.constructor = FunctionDefinitionView;

        FunctionDefinitionView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof FunctionDefinition) {
                this._model = model;
            } else {
                log.error("Function definition undefined or is of different type." + model);
                throw "Function definition undefined or is of different type." + model;
            }
        };

        FunctionDefinitionView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for function definition is undefined." + container);
                throw "Container for function definition is undefined." + container;
            }
        };

        FunctionDefinitionView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        FunctionDefinitionView.prototype.setChildContainer = function(svg){
            if (!_.isNil(svg)) {
                this._childContainer = svg;
            }
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

        FunctionDefinitionView.prototype.getChildContainer = function(){
            return this._childContainer ;
        };

        /**
         * Rendering the view for function definition.
         * @returns {group} The svg group which contains the elements of the function definition view.
         */
        FunctionDefinitionView.prototype.render = function () {
            this.drawAccordionCanvas(this._container, this._options, this._model.id, 'function');
            var divId = this._model.id;
            var currentContainer = $('#'+ divId);
            this._container = currentContainer;

            //Store parent container for child elements of this serviceDefView
            this.setChildContainer(_.first($(this._container).children().children()));
            this.getModel().accept(this);
        };

        /**
         * @inheritDoc
         */
        FunctionDefinitionView.prototype.setWidth = function (newWidth) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        FunctionDefinitionView.prototype.setHeight = function (newHeight) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        FunctionDefinitionView.prototype.setXPosition = function (xPosition) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        FunctionDefinitionView.prototype.setYPosition = function (yPosition) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        FunctionDefinitionView.prototype.getWidth = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        FunctionDefinitionView.prototype.getHeight = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        FunctionDefinitionView.prototype.getXPosition = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        FunctionDefinitionView.prototype.getYPosition = function () {
            // TODO : Implement
        };

        return FunctionDefinitionView;
    });