/**
 * Copyright (c) 2016-2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
define(['lodash', 'log', 'd3', 'd3utils', 'jquery', 'alerts', './svg-canvas', './point', './../ast/service-definition',
        './client-life-line', './resource-definition-view', 'ballerina/ast/ballerina-ast-factory', './axis',
        './connector-declaration-view', './../ast/variable-declaration', './variable-definitions-pane-view', './annotation-view'],
    function (_, log, d3, D3utils, $, Alerts, SVGCanvas, Point, ServiceDefinition,
              ClientLifeLine, ResourceDefinitionView, BallerinaASTFactory, Axis,
              ConnectorDeclarationView, VariableDeclaration, VariableDefinitionsPaneView, AnnotationView) {

        /**
         * The view to represent a service definition which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {ServiceDefinition} args.model - The service definition model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         * @augments SVGCanvas
         */
        var ServiceDefinitionView = function (args) {
            SVGCanvas.call(this, args);

            this._connectorViewList =  _.get(args, "connectorViewList", []);
            this._viewOptions.LifeLineCenterGap = 180;
            this._resourceViewList = _.get(args, "resourceViewList", []);
            this._parentView = _.get(args, "parentView");
            this._viewOptions.offsetTop = _.get(args, "viewOptionsOffsetTop", 50);
            this._viewOptions.topBottomTotalGap = _.get(args, "viewOptionsTopBottomTotalGap", 100);
            this._viewOptions.panelIcon = _.get(args.viewOptions, "cssClass.service_icon");
            this._viewOptions.minHeight = _.get(args, "minHeight", 300);
            //set initial height for the service container svg
            this._totalHeight = 170;
            //set initial connector margin for the service
            this._lifelineMargin = new Axis(0, false);

            if (_.isNil(this._model) || !(this._model instanceof ServiceDefinition)) {
                log.error("Service definition is undefined or is of different type." + this._model);
                throw "Service definition is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for service definition is undefined." + this._container);
                throw "Container for service definition is undefined." + this._container;
            }
        };

        ServiceDefinitionView.prototype = Object.create(SVGCanvas.prototype);
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

        ServiceDefinitionView.prototype.addToResourceViewList = function (view) {
            if (!_.isNil(view)) {
                //stop listening to current last resource view - if any
                if(!_.isEmpty(this._resourceViewList)){
                    _.last(this._resourceViewList).getBoundingBox().off('bottom-edge-moved');

                    // make new view adjust y on last view's bottom edge move
                    _.last(this._resourceViewList).getBoundingBox().on('bottom-edge-moved', function(dy){
                        view.getBoundingBox().move(0, dy);
                    })
                }
                this._resourceViewList.push(view);

                // listen to new last resource view
                _.last(this._resourceViewList).getBoundingBox().on('bottom-edge-moved',
                    this.onLastResourceBottomEdgeMoved, this);
            }
        };

        ServiceDefinitionView.prototype.onLastResourceBottomEdgeMoved = function (dy) {
            this._totalHeight = this._totalHeight + dy;
            this.setSVGHeight(this._totalHeight);
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
            return this.getRootGroup();
        };

        ServiceDefinitionView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        /**
         * Rendering the view of the service definition.
         * @returns {Object} - The svg group which the service definition view resides in.
         */
        ServiceDefinitionView.prototype.render = function (diagramRenderingContext) {
            this.setDiagramRenderingContext(diagramRenderingContext);

            // Draws the outlying body of the service.
            this.drawAccordionCanvas(this._viewOptions, this.getModel().getID(), this.getModel().type.toLowerCase(), this.getModel().getServiceName());

            // Setting the styles for the canvas icon.
            this.getPanelIcon().addClass(_.get(this._viewOptions, "cssClass.service_icon", ""));

            var currentContainer = $('#' + this.getModel().getID());
            this._container = currentContainer;
            this.getBoundingBox().fromTopLeft(new Point(0, 0), currentContainer.width(), currentContainer.height());
            var self = this;

       	    //Scroll to the added position and highlight the heading
            $(_.get(this._viewOptions, "design_view.container", "")).scrollTop(currentContainer.parent().position().top);
            var hadingBox = $('#' + this.getModel().getID() + "_heading");
            var canvas_heading_new = _.get(this._viewOptions, "cssClass.canvas_heading_new", "");
            var new_drop_timeout = _.get(this._viewOptions, "design_view.new_drop_timeout", "");
            hadingBox.addClass(canvas_heading_new);
            setTimeout(function(){hadingBox.removeClass(canvas_heading_new)}, new_drop_timeout);

            $(this.getTitle()).text(this.getModel().getServiceName())
                .on("change paste keyup", function () {
                    self.getModel().setServiceName($(this).text());
                }).on("click", function (event) {
                    event.stopPropagation();
                }).keypress(function (e) {
                    var enteredKey = e.which || e.charCode || e.keyCode;
                    // Disabling enter key
                    if (enteredKey == 13) {
                        event.stopPropagation();
                        return false;
                    }

                    var newServiceName = $(this).text() + String.fromCharCode(enteredKey);

                    try {
                        self.getModel().setServiceName(newServiceName);
                    } catch (error) {
                        Alerts.error(error);
                        event.stopPropagation();
                        return false;
                    }
                });

            this.getModel().on('child-added', function (child) {
                self.visit(child);
                self.getModel().trigger("child-visited", child);

                // Show/Hide scrolls.
                self._showHideScrolls(self._container, self.getChildContainer().node().ownerSVGElement);
            });

            var operationsPane = this.getOperationsPane();

            // Creating annotation icon.
            var panelAnnotationIcon = $("<i/>", {
                class: "fw fw-annotation pull-right right-icon-clickable hoverable",
                title: "Annotations"
            }).appendTo(operationsPane).tooltip();

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
                        left: parseInt($(this.getChildContainer().node().ownerSVGElement.parentElement).width()),
                        top: 0
                    }
                }
            };

            var variableProperties = {
                model: this._model,
                paneAppendElement: this.getChildContainer().node().ownerSVGElement.parentElement,
                viewOfModel: this,
                viewOptions: {
                    position: {
                        x: 8,
                        y: 7
                    },
                    width: $(this.getChildContainer().node().ownerSVGElement.parentElement).width() - (2 * 36)
                }
            };

            var variableDefinitionsPaneView = new VariableDefinitionsPaneView(variableProperties);
            variableDefinitionsPaneView.createVariablePane();

            this.setSVGWidth(this._container.width());
            AnnotationView.createAnnotationPane(annotationProperties);
            this.getModel().accept(this);
        };

        /**
         * Shows and hide the custom scrolls depending on the amount scrolled.
         * @param {Element} container - The container of the SVG. i.e the parent of the SVG.
         * @param {Element} svgElement - The SVG element.
         */
        ServiceDefinitionView.prototype._showHideScrolls = function (container, svgElement) {
            // Creating scroll panes.
            var leftScroll = $(this.getChildContainer().node().ownerSVGElement.parentElement)
                .find(".service-left-scroll").get(0);
            var rightScroll = $(this.getChildContainer().node().ownerSVGElement.parentElement)
                .find(".service-right-scroll").get(0);

            // Setting heights of the scrolls.
            $(leftScroll).height($(container).height());
            $(rightScroll).height($(container).height());

            // Positioning the arrows of the scrolls to the middle.
            $(leftScroll).find("i").css("padding-top", ($(container).height() / 2) - (parseInt($(leftScroll).find("i").css("font-size"), 10) / 2) + "px");
            $(rightScroll).find("i").css("padding-top", ($(container).height() / 2) - (parseInt($(rightScroll).find("i").css("font-size"), 10) / 2) + "px");

            // Showing/Hiding scrolls.
            if (Math.abs($(container).width() - $(svgElement).width()) < 5) {
                // If the svg width is less than or equal to the container, then no need to show the arrows.
                $(leftScroll).hide();
                $(rightScroll).hide();
            } else {
                // If the svg width is greater than the width of the container...
                if ($(container).scrollLeft() == 0) {
                    // When scrollLeft is 0, means that it is already scrolled to the left corner.
                    $(rightScroll).show();
                    $(leftScroll).hide();
                } else if (Math.abs(parseInt($(container).scrollLeft()) -
                        (parseInt($(svgElement).width(), 10) -
                        parseInt($(container).width(), 10))) < 5) {
                    $(leftScroll).show();
                    $(rightScroll).hide();
                } else {
                    $(leftScroll).show();
                    $(rightScroll).show();
                }
            }
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
            var self = this;
            var resourceContainer = this.getChildContainer();
            // If more than 1 resource
            if (this.getResourceViewList().length > 0) {
                var prevView = _.last(this._resourceViewList);
                var prevResourceHeight = prevView.getBoundingBox().h();
                var prevResourceY = prevView.getBoundingBox().y();
                var newY = prevResourceHeight + prevResourceY + prevView.getGapBetweenResources();
                var newX = 50;
                var width = undefined;

                if (!_.isEmpty(this._connectorViewList)) {
                    width = this.getLifeLineMargin().getPosition() - newX - 60;
                }
                var viewOpts = {
                    topLeft: new Point(newX, newY),
                    contentWidth: width,
                    heading: {
                        width:width
                    }
                };
                var resourceDefinitionView = new ResourceDefinitionView({model: resourceDefinition,container: resourceContainer,
                    toolPalette: this.toolPalette, messageManager: this.messageManager, viewOptions: viewOpts, parentView: this});
            }
            else {
                var resourceDefinitionView = new ResourceDefinitionView({model: resourceDefinition, container: resourceContainer,
                    toolPalette: this.toolPalette,messageManager: this.messageManager, parentView: this});
            }
            this.diagramRenderingContext.getViewModelMap()[resourceDefinition.id] = resourceDefinitionView;

            this.addToResourceViewList(resourceDefinitionView);
            resourceDefinitionView.render(this.diagramRenderingContext);

            // Set the lifelineMargin
            this.setLifelineMargin(resourceDefinitionView.getBoundingBox().getRight());
            // If the lifeline margin is changed then accordingly the resource should move the bounding box
            resourceDefinitionView.listenTo(this.getLifeLineMargin(), 'moved', function (offset) {
                resourceDefinitionView.getBoundingBox().w(resourceDefinitionView.getBoundingBox().w() + offset)
            });

            //setting height of the service view
            var childView = this.diagramRenderingContext.getViewModelMap()[resourceDefinition.id];
            var staticHeights = childView.getGapBetweenResources();
            this._totalHeight = this._totalHeight + childView.getBoundingBox().h() + staticHeights;
            this.setSVGHeight(this._totalHeight);
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
                    lineHeight: this.getBoundingBox().h() - this._viewOptions.topBottomTotalGap,
                    messageManager: this.messageManager
                },
                connectorDeclarationView,
                center;
            var self = this;

            if (_.isEmpty(this._connectorViewList)) {
                // If this is the first service level connector adding
                center = new Point(this.getLifeLineMargin().getPosition() + 120, this._viewOptions.offsetTop);
            } else {
                // We have service level connectors already
                center = new Point(_.last(this._connectorViewList).getBoundingBox().getTopCenterX(),
                    this._viewOptions.offsetTop).move(this._viewOptions.LifeLineCenterGap, 0);
            }
            _.set(connectorOpts, 'centerPoint', center);
            connectorDeclarationView = new ConnectorDeclarationView(connectorOpts);
            this.diagramRenderingContext.getViewModelMap()[connectorDeclaration.id] = connectorDeclarationView;

            connectorDeclarationView.createPropertyPane();

            connectorDeclarationView.setParent(this);

            var siblingConnectors = connectorDeclarationView._parent._model.children;

            connectorDeclarationView.render();

            if (this._connectorViewList.length === 0) {
                // Always the first connector is listening to the lifeline margin
                // Use the listen to here, since more than one other objects are listening to the lifeLineMargin.
                connectorDeclarationView.listenTo(this.getLifeLineMargin(), 'moved', function (offset) {
                    self.moveServiceLevelConnector(this, offset);
                });
            } else {
                // When there are already added connectors in the service level
                connectorDeclarationView.listenTo(_.last(this._connectorViewList).getBoundingBox(), 'right-edge-moved', function (offset) {
                    self.moveServiceLevelConnector(this, offset);
                });
            }

            // Add the new connector to the connector views list
            this._connectorViewList.push(connectorDeclarationView);

            if (this.getResourceViewList().length > 0) {
                // If we have added resources
                var newLifeLineMarginPosition = this.getLifeLineMargin().getPosition() - this._viewOptions.LifeLineCenterGap;
                this.getLifeLineMargin().setPosition(newLifeLineMarginPosition);
            } else {
                // When there are no resources added
                this.setSVGWidth(connectorDeclarationView.getBoundingBox().getRight() + this._viewOptions.LifeLineCenterGap);
            }

            connectorDeclarationView.listenTo(this.getBoundingBox(), 'height-changed', function (dh) {
                var newHeight = this.getBoundingBox().h() + dh;
                this.getBoundingBox().h(newHeight);
            }, connectorDeclarationView);

        };

        /**
         * Get the Widest resource
         * @returns {ResourceDefinition} - The resource definition view
         */
        ServiceDefinitionView.prototype.getWidestResource = function () {
            var sortedArray = _.sortBy(this._resourceViewList, [function (resourceDefView) {
                return resourceDefView.getBoundingBox().getRight();
            }]);
            return _.last(sortedArray);
        };

        /**
         * Set the Lifeline Margin
         * @param {number} position - New Axis Position
         */
        ServiceDefinitionView.prototype.setLifelineMargin = function (position) {
            this._lifelineMargin.setPosition(position);
        };

        /**
         * Returns the Lifeline Margin
         * @returns {Axis} - The LifelineMargin
         */
        ServiceDefinitionView.prototype.getLifeLineMargin = function () {
            return this._lifelineMargin;
        };

        /**
         * Child Remove Callback
         * @param {ASTNode} child - removed child node
         */
        ServiceDefinitionView.prototype.childRemovedCallback = function (child) {
            var self = this;
            var childView = this.diagramRenderingContext.getViewModelMap()[child.id];

            // If the child trying to delete is a resource definition
            if (BallerinaASTFactory.isResourceDefinition(child)) {
                var removingChildIndex = _.findIndex(this._resourceViewList, function (child) {
                    return child.id === childView.id;
                });

                var previousResource = this._resourceViewList[removingChildIndex - 1];
                var nextResource = this._resourceViewList[removingChildIndex + 1];

                // Unregister all the events listening to the child view
                childView.getBoundingBox().off();
                if (_.isNil(previousResource)) {
                    // We have deleted the only element
                    if (this._resourceViewList.length === 1) {
                        // If the last remaining child has been removed we re-position the lifelineMargin to 0;
                        // Here the event is un registered since the lifelineMargin reposition also triggers the width to change
                        // as well as the unPlugView does. If this event is not un registered before the lifeLineMargin
                        // re positioning twice we will try to adjust the container widths by throwing errors
                        childView.stopListening(this.getLifeLineMargin());
                        this.getLifeLineMargin().setPosition(0);
                    }
                } else if (_.isNil(nextResource)) {
                    // We have deleted the last resource having a previous resource
                    previousResource.getBoundingBox().on('bottom-edge-moved',
                        this.onLastResourceBottomEdgeMoved, this);
                } else {
                    // We have deleted a resource in between two another resources
                    previousResource.getBoundingBox().on('bottom-edge-moved', function (dy) {
                        nextResource.getBoundingBox().move(0, dy);
                    });
                }
                // Remove the view from the view list
                this._resourceViewList.splice(removingChildIndex, 1);
            } else if (BallerinaASTFactory.isConnectorDeclaration(child) || BallerinaASTFactory.isWorkerDeclaration(child)) {
                // If we deleted the firstChild
                var childId = child.id;
                var childViewIndex = _.findIndex(this._connectorViewList, function (view) {
                    return view.getModel().id === childId;
                });

                // Unregister the listening event for the service view's bounding box's changes
                this._connectorViewList[childViewIndex].stopListening(this.getBoundingBox());
                if (childViewIndex === 0) {
                    // We have deleted the first child (Addresses the scenarios of first child and being the only child
                    this._connectorViewList[childViewIndex].stopListening(this.getLifeLineMargin());
                    if (!_.isNil(this._connectorViewList[childViewIndex + 1])) {
                        this._connectorViewList[childViewIndex + 1].stopListening(this._connectorViewList[childViewIndex].getBoundingBox());
                        this._connectorViewList[childViewIndex + 1].listenTo(this.getLifeLineMargin(), 'moved', function (offset) {
                            self.moveServiceLevelConnector(this, offset);
                        })
                    }
                } else if (this._connectorViewList.length - 1 === childViewIndex){
                    // We are deleting the last child
                    this._connectorViewList[childViewIndex].stopListening(this._connectorViewList[childViewIndex - 1]);
                } else {
                    // We are deleting a connector which is between two connectors
                    // Connector being deleted, stop listening to it's previous connector
                    this._connectorViewList[childViewIndex].stopListening(this._connectorViewList[childViewIndex - 1].getBoundingBox());
                    this._connectorViewList[childViewIndex + 1].stopListening(this._connectorViewList[childViewIndex].getBoundingBox());
                    this._connectorViewList[childViewIndex + 1].listenTo(this._connectorViewList[childViewIndex - 1].getBoundingBox(), 'right-edge-moved', function (offset) {
                        self.moveServiceLevelConnector(this, offset);
                    });
                }
                // Remove the view from the view list
                this._connectorViewList[childViewIndex] = null;
                this._connectorViewList.splice(childViewIndex, 1);
            }
            // Remove the connector/ worker from the diagram rendering context
            delete this.diagramRenderingContext.getViewModelMap()[child.id];
            childView = null;
        };

        /**
         * Move the service level connector
         * @param {BallerinaView} connectorView - Connector view being moved
         * @param {number} offset - move offset
         */
        ServiceDefinitionView.prototype.moveServiceLevelConnector = function (connectorView, offset) {
            connectorView.getBoundingBox().move(offset, 0);
            // After moving the connector, if it go beyond the svg's width, we need to increase the parent svg width
            if (connectorView.getBoundingBox().getRight() > this.getSVG().width()) {
                // Add an offset of 60 to the current connector's BBox's right value
                this.setSVGWidth(connectorView.getBoundingBox().getRight() + 60);
            }
        };

        return ServiceDefinitionView;
    });
