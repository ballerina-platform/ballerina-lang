/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
define(['lodash', 'log', 'd3', 'd3utils', 'jquery', './canvas', './point', './../ast/connector-definition',
        './client-life-line', './connector-action-view', 'ballerina/ast/ballerina-ast-factory', './axis',
        './connector-declaration-view', './../ast/variable-declaration', './variables-view', './annotation-view',
        './function-arguments-view'],
    function (_, log, d3, D3utils, $, Canvas, Point, ConnectorDefinition,
              ClientLifeLine, ConnectorActionView, BallerinaASTFactory, Axis,
              ConnectorDeclarationView, VariableDeclaration, VariablesView, AnnotationView, ArgumentsView) {

        /**
         * The view to represent a connector definition which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {ServiceDefinition} args.model - The service definition model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var ConnectorDefinitionView = function (args) {
            Canvas.call(this, args);
            this._connectorViewList =  _.get(args, "connectorViewList", []);
            this._viewOptions.LifeLineCenterGap = 180;
            this._actionViewList = _.get(args, "actionViewList", []);
            this._parentView = _.get(args, "parentView");
            this._viewOptions.offsetTop = _.get(args, "viewOptionsOffsetTop", 50);
            this._viewOptions.topBottomTotalGap = _.get(args, "viewOptionsTopBottomTotalGap", 100);
            //set panel icon for the connector
            this._viewOptions.panelIcon = _.get(args.viewOptions, "cssClass.connector_icon");
            //set initial height for the connector definition container svg
            this._totalHeight = 170;
            //set initial connector margin for the connector definition
            this._lifelineMargin = new Axis(210, false);

            if (_.isNil(this._model) || !(this._model instanceof ConnectorDefinition)) {
                log.error("Connector definition is undefined or is of different type." + this._model);
                throw "Connector definition is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for connector definition is undefined." + this._container);
                throw "Container for connector definition is undefined." + this._container;
            }
            this.init();
        };

        ConnectorDefinitionView.prototype = Object.create(Canvas.prototype);
        ConnectorDefinitionView.prototype.constructor = ConnectorDefinitionView;

        ConnectorDefinitionView.prototype.init = function(){
            //Registering event listeners
            this.listenTo(this._model, 'child-removed', this.childViewRemovedCallback);
        };

        ConnectorDefinitionView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof ConnectorDefinition) {
                this._model = model;
            } else {
                log.error("Connector definition is undefined or is of different type." + model);
                throw "Connector definition is undefined or is of different type." + model;
            }
        };

        ConnectorDefinitionView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for connector definition is undefined." + container);
                throw "Container for connector definition is undefined." + container;
            }
        };

        /**
         * Add new connector action view
         * @param {ConnectorActionView} view
         */
        ConnectorDefinitionView.prototype.addToActionViewList = function (view) {
            if (!_.isNil(view)) {
                //stop listening to current last connector action view - if any
                if(!_.isEmpty(this._actionViewList)){
                    this.stopListening(_.last(this._actionViewList).getBoundingBox(), 'bottom-edge-moved');

                    // make new view adjust y on last view's bottom edge move
                    _.last(this._actionViewList).getBoundingBox().on('bottom-edge-moved', function(dy){
                        view.getBoundingBox().move(0, dy);
                    })
                }
                this._actionViewList.push(view);

                // listen to new last connector action view
                this.listenTo(_.last(this._actionViewList).getBoundingBox(), 'bottom-edge-moved',
                    this.onLastActionBottomEdgeMoved);
            }
        };

        /**
         * Callback after last action's bottom edge moved
         * @param {number} dy - delta y change
         */
        ConnectorDefinitionView.prototype.onLastActionBottomEdgeMoved = function (dy) {
            this._totalHeight = this._totalHeight + dy;
            this.setServiceContainerHeight(this._totalHeight);
        };

        // /**
        //  * Set the child container
        //  * @param {object} svg - SVG container
        //  */
        // ConnectorDefinitionView.prototype.setChildContainer = function (svg) {
        //     if (!_.isNil(svg)) {
        //         this._childContainer = svg;
        //     }
        // };

        /**
         * Set the view options
         * @param {object} viewOptions
         */
        ConnectorDefinitionView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        /**
         * Get the connector definition model
         * @return {ConnectorDefinition} - Connector Definition Model
         */
        ConnectorDefinitionView.prototype.getModel = function () {
            return this._model;
        };

        /**
         * Get the container
         * @return {HTMLElement} - HTML Container div
         */
        ConnectorDefinitionView.prototype.getContainer = function () {
            return this._container;
        };

        /**
         * Get the action view list
         * @return {ConnectorActionView[]} connector action view list
         */
        ConnectorDefinitionView.prototype.getActionViewList = function () {
            return this._actionViewList;
        };

        /**
         * Get the child container
         * @return {SVG} svg container
         */
        ConnectorDefinitionView.prototype.getChildContainer = function () {
            return this._rootGroup;
        };

        /**
         * Ge the view options
         * @return {object[]} view options
         */
        ConnectorDefinitionView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        /**
         * Rendering the view of the connector definition.
         * @returns {Object} - The svg group which the connector definition view resides in.
         */
        ConnectorDefinitionView.prototype.render = function (diagramRenderingContext) {
            this.diagramRenderingContext = diagramRenderingContext;
            this.drawAccordionCanvas(this._container, this._viewOptions,
                this._model.id, this._model.type.toLowerCase(), this._model.getConnectorName());
            var divId = this._model.id;
            var currentContainer = $('#' + divId);
            this._container = currentContainer;
            this.getBoundingBox().fromTopLeft(new Point(0, 0), currentContainer.width(), currentContainer.height());
            this.getModel().accept(this);
            var self = this;

            $("#title-" + this._model.id).addClass("service-title-text").text(this._model.getConnectorName())
                .on("change paste keyup", function (e) {
                    self._model.setConnectorName($(this).text());
                }).on("click", function (event) {
                    event.stopPropagation();
                }).on("keydown", function (e) {
                    // Check whether the Enter key has been pressed. If so return false. Won't type the character
                    if (e.keyCode === 13) {
                        return false;
                    }
                });

            this._model.on('child-added', function (child) {
                self.visit(child);
                self._model.trigger("child-visited", child);

                // Show/Hide scrolls.
                self._showHideScrolls(self._container, self.getChildContainer().node().ownerSVGElement);
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
                    width: $(this.getChildContainer().node().ownerSVGElement.parentElement).width() - (2 * $(variableButton).width())
                }
            };

            VariablesView.createVariablePane(variableProperties, diagramRenderingContext);

            var operationsPane = this.getOperationsPane();

            // Creating arguments icon.
            var panelArgumentsIcon = $("<i/>", {
                class: "fw fw-import pull-right right-icon-clickable hoverable"
            }).appendTo(operationsPane);

            // Stopping event propagation to the elements behind.
            panelArgumentsIcon.click(function (event) {
                event.stopPropagation();
            });

            // Adding separator for arguments icon.
            $("<span class='pull-right canvas-operations-separator'>|</span>").appendTo(operationsPane);

            var argumentsProperties = {
                model: this._model,
                activatorElement: panelArgumentsIcon,
                paneAppendElement: this.getChildContainer().node().ownerSVGElement.parentElement,
                viewOptions: {
                    position: {
                        // "-1" to remove the svg stroke line
                        left: parseInt($(this.getChildContainer().node().ownerSVGElement.parentElement).width()),
                        top: 0
                    }
                }
            };

            this.setServiceContainerWidth(this._container.width());
            ArgumentsView.createArgumentsPane(argumentsProperties, diagramRenderingContext);
        };

        /**
         * Shows and hide the custom scrolls depending on the amount scrolled.
         * @param {Element} container - The container of the SVG. i.e the parent of the SVG.
         * @param {Element} svgElement - The SVG element.
         */
        ConnectorDefinitionView.prototype._showHideScrolls = function (container, svgElement) {
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

        ConnectorDefinitionView.prototype.canVisitConnectorDefinition = function (serviceDefinition) {
            return true;
        };

        /**
         * Calls the render method for a connector action
         * @param {ConnectorAction} connectorAction - The connector action model.
         */
        ConnectorDefinitionView.prototype.visitConnectorAction = function (connectorAction) {
            log.debug("Visiting connector action");
            var self = this;
            var actionContainer = this.getChildContainer();
            var connectorActionView = undefined;
            // If more than one action
            if (this.getActionViewList().length > 0) {
                var prevView = _.last(this._actionViewList);
                var prevActionHeight = prevView.getBoundingBox().h();
                var prevActionY = prevView.getBoundingBox().y();
                var newY = prevActionHeight + prevActionY + prevView.getGapBetweenConnectorActions();
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
                connectorActionView = new ConnectorActionView({model: connectorAction, container: actionContainer,
                    toolPalette: this.toolPalette, messageManager: this.messageManager, viewOptions: viewOpts,
                    parentView: this});
            }
            else {
                connectorActionView = new ConnectorActionView({model: connectorAction, container: actionContainer,
                    toolPalette: this.toolPalette,messageManager: this.messageManager, parentView: this});
            }
            this.diagramRenderingContext.getViewModelMap()[connectorAction.id] = connectorActionView;

            this.addToActionViewList(connectorActionView);
            connectorActionView.render(this.diagramRenderingContext);

            // Set the lifelineMargin
            this.setLifelineMargin(connectorActionView.getBoundingBox().getRight());
            // If the lifeline margin is changed then accordingly the action should move the bounding box
            this.getLifeLineMargin().on('moved', function (offset) {
                var newWidth = connectorActionView.getBoundingBox().w() + offset;
                var minWidth = connectorActionView.getContentMinWidth();
                if (newWidth > minWidth) {
                    connectorActionView.getBoundingBox().w(newWidth);
                } else {
                    // reset lifeline margin position
                    self.setLifelineMargin(minWidth + self._viewOptions.offsetTop);
                    connectorActionView.getBoundingBox().w(minWidth);
                }
            });

            //setting height of the connector definition view
            var childView = this.diagramRenderingContext.getViewModelMap()[connectorAction.id];
            var staticHeights = childView.getGapBetweenConnectorActions();
            this._totalHeight = this._totalHeight + childView.getBoundingBox().h() + staticHeights;
            this.setServiceContainerHeight(this._totalHeight);
        };

        ConnectorDefinitionView.prototype.canVisitConnectorDeclaration = function (connectorDeclaration) {
            return true;
        };

        /**
         * Calls the render method for a connector declaration.
         * @param connectorDeclaration
         */
        ConnectorDefinitionView.prototype.visitConnectorDeclaration = function (connectorDeclaration) {
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

            // Calculate the new connector's center point
            var widestAction = this.getWidestAction();

            if (_.isEmpty(this._connectorViewList)) {
                // If this is the first connector definition level connector adding
                center = new Point(this.getLifeLineMargin().getPosition() + 120, this._viewOptions.offsetTop);
            } else {
                center = new Point(_.last(this._connectorViewList).getBoundingBox().getTopCenterX(),
                    this._viewOptions.offsetTop).move(this._viewOptions.LifeLineCenterGap, 0);
            }
            _.set(connectorOpts, 'centerPoint', center);
            connectorDeclarationView = new ConnectorDeclarationView(connectorOpts);
            this.diagramRenderingContext.getViewModelMap()[connectorDeclaration.id] = connectorDeclarationView;
            this._connectorViewList.push(connectorDeclarationView);

            connectorDeclarationView.render();

            // Creating property pane
            var editableProperties = [
                {
                    propertyType: "text",
                    key: "Name",
                    model: connectorDeclarationView._model,
                    getterMethod: connectorDeclarationView._model.getConnectorVariable,
                    setterMethod: connectorDeclarationView._model.setConnectorVariable
                },
                {
                    propertyType: "text",
                    key: "Uri",
                    model: connectorDeclarationView._model,
                    getterMethod: connectorDeclarationView._model.getUri,
                    setterMethod: connectorDeclarationView._model.setUri
                }
            ];

            connectorDeclarationView.createPropertyPane({
                model: connectorDeclarationView._model,
                lifeLineGroup:connectorDeclarationView._rootGroup,
                editableProperties: editableProperties
            });

            connectorDeclarationView.setParent(this);
            // We render the service level connector first. Then call the ShrinkOrExpand of the resource
            // This will change the resource BBox if needed. If changed, we move the connector/ reposition it
            this.getLifeLineMargin().on('moved', function (offset) {
                connectorDeclarationView.getBoundingBox().move(offset, 0);
                // After moving the connector, if it go beyond the svg's width, we need to increase the parent svg width
                if (connectorDeclarationView.getBoundingBox().getRight() > self.getServiceContainer().width()) {
                    // Add an offset of 60 to the current connector's BBox's right value
                    self.setServiceContainerWidth(connectorDeclarationView.getBoundingBox().getRight() + 60);
                }
            });
            this.getBoundingBox().on('bottom-edge-moved', function (dh) {
                connectorDeclarationView.getBoundingBox().h(connectorDeclarationView.getBoundingBox().h() + dh);
            });

            this.getLifeLineMargin().setPosition(this.getLifeLineMargin().getPosition() - this._viewOptions.LifeLineCenterGap);
        };

        /**
         * Get the Widest Action
         * @returns {ConnectorAction} - The connector action view
         */
        ConnectorDefinitionView.prototype.getWidestAction = function () {
            var sortedArray = _.sortBy(this._actionViewList, [function (connectorActionView) {
                return connectorActionView.getBoundingBox().getRight();
            }]);
            return _.last(sortedArray);
        };

        /**
         * Set the Lifeline Margin
         * @param {number} position - New Axis Position
         */
        ConnectorDefinitionView.prototype.setLifelineMargin = function (position) {
            this._lifelineMargin.setPosition(position);
        };

        /**
         * Returns the Lifeline Margin
         * @returns {Axis} - The LifelineMargin
         */
        ConnectorDefinitionView.prototype.getLifeLineMargin = function () {
            return this._lifelineMargin;
        };

        return ConnectorDefinitionView;
    });
