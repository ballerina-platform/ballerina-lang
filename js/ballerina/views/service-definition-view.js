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
define(['lodash', 'log', './canvas', './../ast/service-definition', './life-line','./resource-definition-view'],
    function (_, log, Canvas, ServiceDefinition, LifeLine,ResourceDefinitionView) {

        /**
         * The view for the service definition model.
         * @param model Service definition model.
         * @param container The SVG element.
         * @param viewOptions Options to configure the view.
         * @constructor
         */
        var ServiceDefinitionView = function (args) {
            this._model =  _.get(args, 'model', null);
            this._viewOptions =  _.get(args, 'viewOptions', {});
            this._container = _.get(args, 'container', null);
            this._options =  _.get(args, 'options', null);
            this._resourceViewList = [];
            if (_.isNull(this._model) && _.isNil(this._container)){
                log.error("Invalid args received for creating a service definition. Model: " + model + ". Container: " +
                    container);
            }
        };

        ServiceDefinitionView.prototype = Object.create(Canvas.prototype);
        ServiceDefinitionView.prototype.constructor = ServiceDefinitionView;

        ServiceDefinitionView.prototype.addResourceViewList = function(view){
            if(!_.isNil(view)){
                this._resourceViewList.push(view);
            }
        }
        ServiceDefinitionView.prototype.getResourceViewList = function(){
            return this._resourceViewList;
        }
        ServiceDefinitionView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof ServiceDefinition) {
                this._model = model;
            } else {
                log.error("Unknown definition received for service definition. Model: " + model);
            }
        };

        ServiceDefinitionView.prototype.setChildContainer = function(svg){
            if (!_.isNil(svg)) {
                this._childContainer = svg;
            }
        };
        ServiceDefinitionView.prototype.getChildContainer = function(){
            return this._childContainer ;

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
            this.drawAccordionCanvas(this._container, this._options, this._model.id, 'service');
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
        ServiceDefinitionView.prototype.canVisitServiceDefinition = function(serviceDefinition){
            return true;
        };
        ServiceDefinitionView.prototype.visitServiceDefinition = function(serviceDefinition){

        };

        ServiceDefinitionView.prototype.canVisitResourceDefinition = function(resourceDefinition){
            return true;
        };
        ServiceDefinitionView.prototype.visitResourceDefinition = function(resourceDefinition){
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
        return ServiceDefinitionView;
    });