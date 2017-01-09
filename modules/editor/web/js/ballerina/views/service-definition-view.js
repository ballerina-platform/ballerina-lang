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
define(['lodash', 'log', 'd3', 'd3utils', 'jquery', './canvas', './point', './../ast/service-definition',
        './client-life-line', './resource-definition-view', 'ballerina/ast/ballerina-ast-factory', './axis',
        './connector-declaration-view', './../ast/variable-declaration', './variables-view', './annotation-view'],
    function (_, log, d3, D3utils, $, Canvas, Point, ServiceDefinition,
              ClientLifeLine, ResourceDefinitionView, BallerinaASTFactory, Axis,
              ConnectorDeclarationView, VariableDeclaration, VariablesView, AnnotationView) {

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

            this._connectorViewList =  _.get(args, "connectorViewList", []);
            this._viewOptions.LifeLineCenterGap = 180;
            this._resourceViewList = _.get(args, "resourceViewList", []);
            this._parentView = _.get(args, "parentView");
            this._viewOptions.offsetTop = _.get(args, "viewOptionsOffsetTop", 50);
            this._viewOptions.topBottomTotalGap = _.get(args, "viewOptionsTopBottomTotalGap", 100);
            //set initial height for the service container svg
            this._totalHeight = 170;
            //set initial connector margin for the service
            this._lifelineMargin = new Axis(210, false);

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
            this.listenTo(this._model, 'childRemovedEvent', this.childViewRemovedCallback);
        };

        ServiceDefinitionView.prototype.childVisitedCallback = function (child) {

        };

        ServiceDefinitionView.prototype.childViewAddedCallback = function (child) {
            if (BallerinaASTFactory.isServiceDefinition(child)) {
                if (child !== this._model) {
                    log.debug("[Eventing] Service view added : ");
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

        ServiceDefinitionView.prototype.addToResourceViewList = function (view) {
            if (!_.isNil(view)) {
                //stop listening to current last resource view - if any
                if(!_.isEmpty(this._resourceViewList)){
                    this.stopListening(_.last(this._resourceViewList).getBoundingBox(), 'bottom-edge-moved');

                    // make new view adjust y on last view's bottom edge move
                    _.last(this._resourceViewList).getBoundingBox().on('bottom-edge-moved', function(dy){
                        view.getBoundingBox().move(0, dy);
                    })
                }
                this._resourceViewList.push(view);

                // listen to new last resource view
                this.listenTo(_.last(this._resourceViewList).getBoundingBox(), 'bottom-edge-moved',
                    this.onLastResourceBottomEdgeMoved);
            }
        };

        ServiceDefinitionView.prototype.onLastResourceBottomEdgeMoved = function (dy) {
            this._totalHeight = this._totalHeight + dy;
            this.setServiceContainerHeight(this._totalHeight);
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
            return this._rootGroup;
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

            this.getModel().accept(this);
            var self = this;

            $("#title-" + this._model.id).addClass("service-title-text").text(this._model.getServiceName())
                .on("change paste keydown", function (e) {
                    if (e.which == 13) {
                        return false;
                    }
                    self._model.setServiceName($(this).text());
                }).on("click", function (event) {
                event.stopPropagation();
            });

            this._model.on('child-added', function (child) {
                self.visit(child);
                self._model.trigger("childVisitedEvent", child);
            });

            var variableButton = VariablesView.createVariableButton(this.getChildContainer().node(), 14, 10);

            var variableProperties = {
                model: this._model,
                activatorElement: variableButton,
                paneAppendElement: this.getChildContainer().node().ownerSVGElement.parentElement,
                viewOptions: {
                    position: {
                        x: parseInt(this.getChildContainer().attr("x")) + 17,
                        y: parseInt(this.getChildContainer().attr("y")) + 6
                    },
                    width: parseInt(this.getChildContainer().node().parentElement.getBoundingClientRect().width) - 36
                }
            };

            VariablesView.createVariablePane(variableProperties);

            var operationsPane = this.getOperationsPane();

            // Creating annotation icon.
            var panelAnnotationIcon = $("<i/>", {
                class: "fw fw-annotation pull-right right-icon-clickable hoverable"
            }).appendTo(operationsPane);

            // Stopping event propagation to the elements behind.
            panelAnnotationIcon.click(function (event) {
                event.stopPropagation();
            });

            // Adding separator for annotation icon.
            $("<span class='pull-right canvas-operations-separator'>|</span>").appendTo(operationsPane);

            var annotationProperties = {
                model: this._model,
                activatorElement: panelAnnotationIcon,
                paneAppendElement: this.getChildContainer().node().ownerSVGElement.parentElement,
                viewOptions: {
                    position: {
                        left: parseInt(this.getChildContainer().node().parentElement.getBoundingClientRect().width),
                        top: 0
                    }
                }
            };

            AnnotationView.createAnnotationPane(annotationProperties);
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
            log.debug("Visiting resource definition");
            var resourceContainer = this.getChildContainer();
            // If more than 1 resource
            if (this.getResourceViewList().length > 0) {
                var prevView = _.last(this._resourceViewList);
                var prevResourceHeight = prevView.getBoundingBox().h();
                var prevResourceY = prevView.getBoundingBox().y();
                var newY = prevResourceHeight + prevResourceY + prevView.getGapBetweenResources();
                var viewOpts = { topLeft: new Point( 50, newY)};
                var resourceDefinitionView = new ResourceDefinitionView({model: resourceDefinition,container: resourceContainer,
                    toolPalette: this.toolPalette, messageManager: this.messageManager, viewOptions: viewOpts, parentView: this});
            }
            else{
                var resourceDefinitionView = new ResourceDefinitionView({model: resourceDefinition, container: resourceContainer,
                    toolPalette: this.toolPalette,messageManager: this.messageManager, parentView: this});
            }
            this.diagramRenderingContext.getViewModelMap()[resourceDefinition.id] = resourceDefinitionView;

            this.addToResourceViewList(resourceDefinitionView);

            this.listenTo(resourceDefinitionView, 'childConnectorViewAddedEvent', this.childConnectorViewAddedCallback);
            this.listenTo(resourceDefinitionView, 'defaultWorkerViewAddedEvent',this.defaultWorkerViewAddedCallback);
            resourceDefinitionView.render(this.diagramRenderingContext);

            //setting height of the service view
            var childView = this.diagramRenderingContext.getViewModelMap()[resourceDefinition.id];
            var staticHeights = childView.getGapBetweenResources();
            this._totalHeight = this._totalHeight + childView.getBoundingBox().h() + staticHeights;
            this.setServiceContainerHeight(this._totalHeight);

            //setting client lifeline's height. Value is calculated by reducing required amount of height from the total height of the service.
            // this.setClientLifelineHeight(this._totalHeight);

            this.trigger("childViewAddedEvent", resourceDefinition);
        };

        /**
         * callback function for connector view added event
         * @param connectorView
         */
        ServiceDefinitionView.prototype.childConnectorViewAddedCallback = function (connectorView) {
            this.updateLifelineMargin(connectorView);
        };

        /**
         * callback function for default worker view added event
         * @param defaultWorkerView
         */
        ServiceDefinitionView.prototype.defaultWorkerViewAddedCallback = function (defaultWorkerView) {
            this.updateLifelineMargin(defaultWorkerView);
        };

        /**
         * updates lifeline margin of this service
         * @param lifeLineView
         */
        ServiceDefinitionView.prototype.updateLifelineMargin = function (lifeLineView) {
            var centerX = lifeLineView.getBoundingBox().getTopCenterX();
            if (centerX > this.getLifelineMargin().getPosition()) {
                this.getLifelineMargin().setPosition(centerX);
            }
        };


        ServiceDefinitionView.prototype.canVisitConnectorDeclaration = function (connectorDeclaration) {
            return true;
        };

        /**
         * Calls the render method for a connector declaration.
         * @param connectorDeclaration
         */
        ServiceDefinitionView.prototype.visitConnectorDeclaration = function (connectorDeclaration) {
            var connectorContainer = this.getChildContainer().node(),
                connectorOpts = {
                    model: connectorDeclaration,
                    container: connectorContainer,
                    parentView: this,
                    lineHeight: this.getBoundingBox().h() - this._viewOptions.topBottomTotalGap
                },
                connectorDeclarationView,
                center;

            center = new Point(this.getLifelineMargin().getPosition(), this._viewOptions.offsetTop).move(this._viewOptions.LifeLineCenterGap, 0);
            _.set(connectorOpts, 'centerPoint', center);
            connectorDeclarationView = new ConnectorDeclarationView(connectorOpts);
            this.diagramRenderingContext.getViewModelMap()[connectorDeclaration.id] = connectorDeclarationView;
            this._connectorViewList.push(connectorDeclarationView);

            connectorDeclarationView.render();
            connectorDeclarationView.setParent(this);
            connectorDeclarationView.listenTo(this.getLifelineMargin(), 'moved', this.updateConnectorPositionCallback);
        };

        /**
         * updates connector position
         * @param dx
         */
        ServiceDefinitionView.prototype.updateConnectorPositionCallback = function (dx) {
            // "this" will be a connector instance
            this.position(dx, 0);
            this.getBoundingBox().move(dx, 0);
        };

        /**
         * set the lifeline margin
         * @param position
         */
        ServiceDefinitionView.prototype.setLifelineMargin = function (position) {
            this._lifelineMargin.setPosition(position);
        };

        /**
         * get the lifeline margin
         * @returns {Axis|*}
         */
        ServiceDefinitionView.prototype.getLifelineMargin = function () {
            return this._lifelineMargin;
        };

        return ServiceDefinitionView;
    });
