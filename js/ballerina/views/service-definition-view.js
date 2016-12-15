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
define(['lodash', 'log', 'd3', 'd3utils', 'jquery', './canvas', './point', './../ast/service-definition', './life-line', './resource-definition-view', 'ballerina/ast/ballerina-ast-factory'],
    function (_, log, d3, D3utils, $, Canvas, Point, ServiceDefinition, LifeLine, ResourceDefinitionView, BallerinaASTFactory) {

        /**
         * The view to represent a service definition which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {ServiceDefinition} args.model - The service definition model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var ServiceDefinitionView = function (args) {
            Canvas.call(this, args);

            this._resourceViewList = _.get(args, "resourceViewList", []);
            this._parentView = _.get(args, "parentView");
            //set initial height for the service container svg
            this._totalHeight = 170;

            if (_.isNil(this._model) || !(this._model instanceof ServiceDefinition)) {
                log.error("Service definition is undefined or is of different type." + this._model);
                throw "Service definition is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for service definition is undefined." + this._container);
                throw "Container for service definition is undefined." + this._container;
            }
            this.init();
        };

        ServiceDefinitionView.prototype = Object.create(Canvas.prototype);
        ServiceDefinitionView.prototype.constructor = ServiceDefinitionView;

        ServiceDefinitionView.prototype.init = function(){
            //Registering event listeners
            this.listenTo(this._model, 'childVisitedEvent', this.childVisitedCallback);
            this.listenTo(this._parentView, 'childViewAddedEvent', this.childViewAddedCallback);
        };

        ServiceDefinitionView.prototype.childVisitedCallback = function (child) {
            //setting height of the service view
            var childView = this.diagramRenderingContext.getViewModelMap()[child.id];
            var staticHeights = childView.getGapBetweenResources() + childView.getResourceHeadingHeight();
            this._totalHeight = this._totalHeight + childView.getBoundingBox().height + staticHeights;
            this.setServiceContainerHeight(this._totalHeight);

            //setting client lifeline's height. Value is calculated by reducing required amount of height from the total height of the service.
            this.setClientLifelineHeight(this._totalHeight - 150);

            this.trigger("childViewAddedEvent", child);
        };

        ServiceDefinitionView.prototype.childViewAddedCallback = function (child) {
            if (BallerinaASTFactory.isServiceDefinition(child)) {
                if (child !== this._model) {
                    log.info("[Eventing] Service view added : ");
                }
            }
        };

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
        ServiceDefinitionView.prototype.render = function (diagramRenderingContext) {
            this.diagramRenderingContext = diagramRenderingContext;
            this.drawAccordionCanvas(this._container, this._viewOptions, this._model.id, this._model.type.toLowerCase(), this._model._serviceName);
            var divId = this._model.id;
            var currentContainer = $('#' + divId);
            this._container = currentContainer;

            // Creating client lifeline.

            this._clientLifeLine = new LifeLine(_.first($(this._container).children().children()));
            //Store parent container for child elements of this serviceDefView
            this.setChildContainer(_.first($(this._container).children().children()));
            this._clientLifeLine.render();
            this.getModel().accept(this);

             //  var annotationButton = this._createAnnotationButton(this.getChildContainer());
             var variableButton = this._createVariableButton(this.getChildContainer());

             var variableProperties = {
                model: this._model,
                editableProperties: [{
                    propertyType: "text",
                    key: "Service Name",
                    model: this._model,
                    getterMethod: this._model.getServiceName,
                    setterMethod: this._model.setServiceName
                },
                {
                    propertyType: "text",
                    key: "Base Path",
                    model: this._model,
                    getterMethod: this._model.getBasePath,
                    setterMethod: this._model.setBasePath
                }],
                activatorElement: variableButton.node(),
                paneAppendElement: _.first($(this._container).children()),
                viewOptions: {
                    position: {
                        x: parseFloat(variableButton.attr("cx")),
                        y: parseFloat(variableButton.attr("cy")) + parseFloat(variableButton.attr("r"))
                    }
                }
             };

            this.createVariablePane(variableProperties);

            var self = this;
            this._model.on('child-added', function(child){
                self.visit(child);
                self._model.trigger("childVisitedEvent", child);
            });
        };

        ServiceDefinitionView.prototype.createVariablePane = function (args) {
            var activatorElement = _.get(args, "activatorElement");;
            var serviceModel = _.get(args, "model");
            var paneElement = _.get(args, "paneAppendElement");

            if (_.isNil(activatorElement)) {
                log.error("Unable to render property pane as the html element is undefined." + activatorElement);
                throw "Unable to render property pane as the html element is undefined." + activatorElement;
            }

            var variablePaneWrapper = $('<div id="variableSection" style="position:absolute;top:32px;right:109px;display:none"/>').appendTo($(paneElement));
            var variableForm = $('<form></form>').appendTo(variablePaneWrapper);
            var variableText = $("<input id='inputbox'/>").appendTo(variableForm);
            var variableSelect = $("<select id='customSelect'><option value='message'>message</option><option value='exception'>exception</option><option value='int'>int</option></select>").appendTo(variableForm);
            var addVariable = $("<button type='button'>Add</button>").appendTo(variableForm);

            $(addVariable).click(serviceModel, function (serviceModel){
                var variableList = serviceModel.data.getVariableDeclarations();
                var variable2 = BallerinaASTFactory.createVariableDeclaration();
                variable2.setType('int');
                variable2.setIdentifier('2');
                serviceModel.data.getVariableDeclarations().push(variable2)
                log.info($(variableText).val());
            });

            var variableList = serviceModel.getVariableDeclarations();

            for(variableCount = 0; variableCount < variableList.length; variableCount++){
                var variableSelect = $("<p><label for=" + variableList[variableCount].getIdentifier() + ">" +
                    variableList[variableCount].getType()  +":" +variableList[variableCount].getIdentifier() +"</label></p>").
                    appendTo(variablePaneWrapper);
            }

            $(activatorElement).click(serviceModel, function (serviceModel) {
                if(paneElement.children[1].style.display=="none"){
                    paneElement.children[1].style.display = "inline";
                } else {
                    paneElement.children[1].style.display = "none";
                }
            });
        };

        ServiceDefinitionView.prototype.canVisitServiceDefinition = function (serviceDefinition) {
            return true;
        };

        ServiceDefinitionView.prototype.visitServiceDefinition = function (serviceDefinition) {

        };

        ServiceDefinitionView.prototype.canVisitResourceDefinition = function (resourceDefinition) {
            return false;
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
                var resourceDefinitionView = new ResourceDefinitionView({model: resourceDefinition,container: resourceContainer,
                    toolPalette: this.toolPalette, messageManager: this.messageManager, viewOptions: viewOpts});
            }
            else{
                var resourceDefinitionView = new ResourceDefinitionView({model: resourceDefinition,container: resourceContainer,
                    toolPalette: this.toolPalette,messageManager: this.messageManager, parentView: this});
            }
            this.diagramRenderingContext.getViewModelMap()[resourceDefinition.id] = resourceDefinitionView;
            resourceDefinitionView.render(this.diagramRenderingContext);
            this.addResourceViewList(resourceDefinitionView);
        };

        /**
         * Sets height of the client lifeline
         * @param height
         */
        ServiceDefinitionView.prototype.setClientLifelineHeight = function (height) {
            this._clientLifeLine.setLineHeight(height);
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

        // TODO : Move this to SVG
        ServiceDefinitionView.prototype._createAnnotationButton = function(serviceContentSvg) {
            var svgDefinitions = d3.select(serviceContentSvg).append("defs");

            var annotationButtonPattern = svgDefinitions.append("pattern")
                .attr("id", "annotationIcon")
                .attr("width", "100%")
                .attr("height", "100%");

            annotationButtonPattern.append("image")
                .attr("xlink:href", "images/annotation.svg")
                .attr("x", 0)
                .attr("y", 0)
                .attr("width", 18.67)
                .attr("height", 18.67);

            var annotationIconGroup = D3utils.group(d3.select(serviceContentSvg));

            var annotationIconBackgroundCircle = D3utils.circle(1435, 30, 18.675, annotationIconGroup)
                .classed("annotation-icon-background-circle", true);

            var annotationIconRect = D3utils.centeredRect(new Point(1435, 30), 18.67, 18.67, 0, 0, annotationIconGroup)
                .classed("annotation-icon-rect", true);

            return annotationIconBackgroundCircle;
        };

        ServiceDefinitionView.prototype._createVariableButton = function(serviceContentSvg) {
            var svgDefinitions = d3.select(serviceContentSvg).append("defs");

            var variableButtonPattern = svgDefinitions.append("pattern")
                .attr("id", "variableIcon")
                .attr("width", "100%")
                .attr("height", "100%");

            variableButtonPattern.append("image")
                .attr("xlink:href", "images/variable.svg")
                .attr("x", 0)
                .attr("y", 0)
                .attr("width", 18.67)
                .attr("height", 18.67);

            var variableIconGroup = D3utils.group(d3.select(serviceContentSvg));

            var variableIconBackgroundCircle = D3utils.circle(1435, 30, 18.675, variableIconGroup)
                .classed("variable-icon-background-circle", true);

            var variableIconRect = D3utils.centeredRect(new Point(1435, 30), 18.67, 18.67, 0, 0, variableIconGroup)
                .classed("variable-icon-rect", true);

            return variableIconBackgroundCircle;
        };

        return ServiceDefinitionView;
    });