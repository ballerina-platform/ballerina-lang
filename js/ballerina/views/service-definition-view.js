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
define(['lodash', 'log', './canvas', './../ast/service-definition', './life-line', './resource-definition-view'],
    function (_, log, Canvas, ServiceDefinition, LifeLine, ResourceDefinitionView) {

        /**
         * The view to represent a service definition which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {ServiceDefinition} args.model - The service definition model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var ServiceDefinitionView = function (args) {
            this._model = _.get(args, "model");
            this._container = _.get(args, "container");
            this._resourceViewList = _.get(args, "resourceViewList", []);
            this._viewOptions = _.get(args, "viewOptions", {});

            if (_.isNil(this._model) || !(this._model instanceof ServiceDefinition)) {
                log.error("Service definition is undefined or is of different type." + this._model);
                throw "Service definition is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for service definition is undefined." + this._container);
                throw "Container for service definition is undefined." + this._container;
            }

            Canvas.call(this);
        };

        ServiceDefinitionView.prototype = Object.create(Canvas.prototype);
        ServiceDefinitionView.prototype.constructor = ServiceDefinitionView;

        ServiceDefinitionView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof ServiceDefinition) {
                this._model = model;
            } else {
                log.error("Service definition is undefined or is of different type." + model);
                throw "Service definition is undefined or is of different type." + model;
            }
        };

        ServiceDefinitionView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for service definition is undefined." + container);
                throw "Container for service definition is undefined." + container;
            }
        };

        ServiceDefinitionView.prototype.addResourceViewList = function (view) {
            if (!_.isNil(view)) {
                this._resourceViewList.push(view);
            }
        };

        ServiceDefinitionView.prototype.setChildContainer = function (svg) {
            if (!_.isNil(svg)) {
                this._childContainer = svg;
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

        ServiceDefinitionView.prototype.getResourceViewList = function () {
            return this._resourceViewList;
        };

        ServiceDefinitionView.prototype.getChildContainer = function () {
            return this._childContainer;
        };

        ServiceDefinitionView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        /**
         * Rendering the view of the service definition.
         * @returns {Object} - The svg group which the service definition view resides in.
         */
        ServiceDefinitionView.prototype.render = function () {
            this.drawAccordionCanvas(this._container, this._viewOptions, this._model.id, 'service');
            var divId = this._model.id;
            var currentContainer = $('#' + divId);
            this._container = currentContainer;

            // Creating client lifeline.
            var clientLifeLine = new LifeLine(_.first($(this._container).children().children()));
            //Store parent container for child elements of this serviceDefView
            this.setChildContainer(_.first($(this._container).children().children()));
            clientLifeLine.render();
            this.getModel().accept(this);
        };

        ServiceDefinitionView.prototype.canVisitServiceDefinition = function (serviceDefinition) {
            return true;
        };

        ServiceDefinitionView.prototype.visitServiceDefinition = function (serviceDefinition) {

        };

        ServiceDefinitionView.prototype.canVisitResourceDefinition = function (resourceDefinition) {
            return true;
        };

        /**
         * Calls the render method for a resource definition.
         * @param {ResourceDefinition} resourceDefinition - The resource definition model.
         */
        ServiceDefinitionView.prototype.visitResourceDefinition = function (resourceDefinition) {
            log.info("Visiting resource definition");
            var resourceContainer  = this.getChildContainer();
            // If more than 1 resource
            if(this.getResourceViewList().length > 0 ){
                var prevView = this.getResourceViewList().pop(this.getResourceViewList().length-1);
                var prevResourceHeight = prevView.getBoundingBox().height;
                var prevResourceY = prevView.getBoundingBox().y;
                var newCenterPointY = prevResourceHeight + prevResourceY + 10;
                var viewOpts = { centerPoint: {y:newCenterPointY}};
                var resourceDefinitionView = new ResourceDefinitionView({model: resourceDefinition,container: resourceContainer,viewOptions: viewOpts});

            }
            else{
                var resourceDefinitionView = new ResourceDefinitionView({model: resourceDefinition,container: resourceContainer});
            }
            resourceDefinitionView.render();
            this.addResourceViewList(resourceDefinitionView);
        };

        /**
         * @inheritDoc
         */
        ServiceDefinitionView.prototype.setHeight = function (newHeight) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ServiceDefinitionView.prototype.setXPosition = function (xPosition) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ServiceDefinitionView.prototype.setYPosition = function (yPosition) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ServiceDefinitionView.prototype.getWidth = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ServiceDefinitionView.prototype.getHeight = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ServiceDefinitionView.prototype.getXPosition = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ServiceDefinitionView.prototype.getYPosition = function () {
            // TODO : Implement
        };

        return ServiceDefinitionView;
    });