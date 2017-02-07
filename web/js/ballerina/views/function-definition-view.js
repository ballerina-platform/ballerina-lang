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
define(['lodash', 'log', 'event_channel',  'alerts', './svg-canvas', './../ast/function-definition', './default-worker',
        'd3utils', 'd3', './worker-declaration-view', './statement-view-factory', './point', './axis',
        './connector-declaration-view', './statement-container', './variables-view', './function-arguments-view',
        './return-types-pane-view', 'ballerina/ast/ballerina-ast-factory'],
    function (_, log, EventChannel, Alerts, SVGCanvas, FunctionDefinition, DefaultWorkerView,
              D3Utils, d3, WorkerDeclarationView, StatementViewFactory, Point, Axis,
              ConnectorDeclarationView, StatementContainer, VariablesView, ArgumentsView,
              ReturnTypePaneView, BallerinaASTFactory) {

        /**
         * The view to represent a function definition which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {FunctionDefinition} args.model - The function definition model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         * @augments SVGCanvas
         */
        var FunctionDefinitionView = function (args) {
            SVGCanvas.call(this, args);
            //set initial connector margin for the service
            this._lifelineMargin = new Axis(210, false);
            this._statementExpressionViewList = [];
            // TODO: Instead of using the parentView use the parent. Fix this from BallerinaView.js and bellow
            this._parentView = _.get(args, "parentView");
            //set panel icon for the function
            this._viewOptions.panelIcon = _.get(args.viewOptions, "cssClass.function_icon");
            //set initial height for the service container svg
            this._totalHeight = 170;
            this._statementContainer = undefined;

            this._defaultWorkerLifeLine = undefined;
            // TODO: Check whether the possibility of adding this to the generic level
            this._workerAndConnectorViews = [];

            if (_.isNil(this._model) || !(this._model instanceof FunctionDefinition)) {
                log.error("Function definition is undefined or is of different type." + this._model);
                throw "Function definition is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for function definition is undefined." + this._container);
                throw "Container for function definition is undefined." + this._container;
            }
        };

        FunctionDefinitionView.prototype = Object.create(SVGCanvas.prototype);
        FunctionDefinitionView.prototype.constructor = FunctionDefinitionView;

        /**
         * Child remove callback
         * @param {ASTNode} child - removed child
         */
        FunctionDefinitionView.prototype.childRemovedCallback = function (child) {
            var self = this;
            if (BallerinaASTFactory.isStatement(child)) {
                this.getStatementContainer().childStatementRemovedCallback(child);
            } else if (BallerinaASTFactory.isConnectorDeclaration(child) || BallerinaASTFactory.isWorkerDeclaration(child)) {
                var childViewIndex = _.findIndex(this._workerAndConnectorViews, function (view) {
                    return view.getModel().id === child.id;
                });

                if (childViewIndex === 0) {
                    // Deleted the first connector/worker in the list (Addresses both first element scenario and the only element scenario
                    if (!_.isNil(this._workerAndConnectorViews[childViewIndex + 1])) {
                        // Unregister the listening event of the second element on the first element
                        this._workerAndConnectorViews[childViewIndex + 1].stopListening(this._workerAndConnectorViews[childViewIndex].getBoundingBox());
                    }
                } else if (childViewIndex === this._workerAndConnectorViews.length - 1) {
                    // Deleted the last connector/worker when there are more than one worker/ connector
                    this._workerAndConnectorViews[childViewIndex].stopListening(this._workerAndConnectorViews[childViewIndex - 1].getBoundingBox());
                } else {
                    // Deleted connector is in between two other connectors/ workers
                    // Connector being deleted, stop listening to it's previous connector
                    this._workerAndConnectorViews[childViewIndex].stopListening(this._workerAndConnectorViews[childViewIndex - 1].getBoundingBox());
                    this._workerAndConnectorViews[childViewIndex + 1].stopListening(this._workerAndConnectorViews[childViewIndex].getBoundingBox());
                    this._workerAndConnectorViews[childViewIndex + 1].listenTo(this._workerAndConnectorViews[childViewIndex - 1].getBoundingBox(), 'right-edge-moved', function (offset) {
                        self.moveFunctionDefinitionLevelConnector(this, offset);
                    });
                }
                this._workerAndConnectorViews[childViewIndex] = null;
                this._workerAndConnectorViews.splice(childViewIndex, 1);
            }
            // Remove the connector/ worker from the diagram rendering context
            delete this.diagramRenderingContext.getViewModelMap()[child.id];
        };

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

        FunctionDefinitionView.prototype.canVisitWorkerDeclaration = function () {
            return true;
        };

        FunctionDefinitionView.prototype.canVisitFunctionDefinition = function () {
            return true;
        };

        /**
         * @override
         */
        FunctionDefinitionView.prototype.isAValidNodeForCanvasDropArea = function (node) {
            var nodeFactory = this._model.getFactory();
            // IMPORTANT: override canvas's default validation logic
            // Canvas drop zone is for worker and connector declarations only.
            // Statements should only be allowed on top of function worker's drop zone.
            return nodeFactory.isConnectorDeclaration(node)
                || nodeFactory.isWorkerDeclaration(node);
        };

        /**
         * Calls the render method for a worker declaration.
         * @param {WorkerDeclaration} workerDeclaration - The worker declaration model.
         */
        FunctionDefinitionView.prototype.visitWorkerDeclaration = function (workerDeclaration) {
            var workerDeclarationOptions = {
                model: workerDeclaration,
                container: this._container
            };
            var workerDeclarationView = new WorkerDeclarationView(workerDeclarationOptions);
            workerDeclarationView.setParent(this);
            workerDeclarationView.render();
            this._workerAndConnectorViews.push(workerDeclarationView);
        };

        /**
         * Rendering the view for function definition.
         * @returns {group} The svg group which contains the elements of the function definition view.
         */
        FunctionDefinitionView.prototype.render = function (diagramRenderingContext) {
            this.setDiagramRenderingContext(diagramRenderingContext);

            // Draws the outlying body of the function.
            this.drawAccordionCanvas(this._viewOptions, this.getModel().getID(), this.getModel().type.toLowerCase(), this.getModel().getFunctionName());

            // Setting the styles for the canvas icon.
            this.getPanelIcon().addClass(_.get(this._viewOptions, "cssClass.function_icon", ""));

            var currentContainer = $('#' + this.getModel().getID());
            this._container = currentContainer;
            this.getBoundingBox().fromTopLeft(new Point(0, 0), currentContainer.width(), currentContainer.height());
            var self = this;

            $(this.getTitle()).text(this.getModel().getFunctionName())
                .on("change paste keyup", function () {
                    self.getModel().setFunctionName($(this).text());
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
                    self.getModel().setFunctionName(newServiceName);
                } catch (error) {
                    Alerts.error(error);
                    event.stopPropagation();
                    return false;
                }
            });

            // Creating default worker
            var defaultWorkerOpts = {};
            _.set(defaultWorkerOpts, 'container', this._rootGroup.node());
            _.set(defaultWorkerOpts, 'title', 'FunctionWorker');
            _.set(defaultWorkerOpts, 'centerPoint', new Point(130, 80));

            // Check whether there is already created default worker and otherwise we create a new one
            if (_.isUndefined(this._defaultWorkerLifeLine)) {
                this._defaultWorkerLifeLine = new DefaultWorkerView(defaultWorkerOpts);
            }
            this._defaultWorkerLifeLine.render();
            this._totalHeight = this._defaultWorkerLifeLine.getBoundingBox().h() + 85;
            this.setSVGHeight(this._totalHeight);
            this.renderStatementContainer();
            this.getModel().accept(this);
            //Removing all the registered 'child-added' event listeners for this model.
            //This is needed because we are not unregistering registered event while the diagram element deletion.
            //Due to that, sometimes we are having two or more view elements listening to the 'child-added' event of same model.
            this.getModel().off('child-added');
            this.getModel().on('child-added', function (child) {
                self.visit(child);
                self.getModel().trigger("child-visited", child);

                // Show/Hide scrolls.
                self._showHideScrolls(self._container, self.getChildContainer().node().ownerSVGElement);
            });

            var operationsPane = this.getOperationsPane();

            var operationButtons = [];

            // Creating arguments icon.
            var panelArgumentsIcon = $("<i/>", {
                class: "fw fw-import pull-right right-icon-clickable hoverable",
                title: "Arguments"
            }).appendTo(operationsPane).tooltip();

            $(panelArgumentsIcon).click(function (event) {
                event.stopPropagation();
            });

            operationButtons.push(panelArgumentsIcon);

            // Adding separator for arguments icon.
            $("<span class='pull-right canvas-operations-separator'>|</span>").appendTo(operationsPane);

            var argumentsProperties = {
                model: this._model,
                activatorElement: panelArgumentsIcon,
                paneAppendElement: this.getChildContainer().node().ownerSVGElement.parentElement,
                viewOptions: {
                    position: {
                        left: parseInt($(this.getChildContainer().node().ownerSVGElement.parentElement).width()),
                        top: 0
                    }
                }
            };

            // Creating arguments pane.
            ArgumentsView.createArgumentsPane(argumentsProperties, diagramRenderingContext);

            this.setSVGWidth(this._container.width());

            // Creating return type icon.
            var panelReturnTypeIcon = $("<i/>", {
                class: "fw fw-export pull-right right-icon-clickable hoverable",
                title: "Return Types"
            }).appendTo(operationsPane).tooltip();

            $(panelReturnTypeIcon).click(function (event) {
                event.stopPropagation();
            });

            operationButtons.push(panelReturnTypeIcon);

            // Adding separator for return type icon.
            $("<span class='pull-right canvas-operations-separator'>|</span>").appendTo(operationsPane);

            var returnTypeProperties = {
                model: this._model,
                activatorElement: panelReturnTypeIcon,
                paneAppendElement: this.getChildContainer().node().ownerSVGElement.parentElement,
                viewOptions: {
                    position: new Point(parseInt($(this.getChildContainer().node().ownerSVGElement.parentElement).width()),
                        0)
                },
                view: this
            };

            this._returnTypePaneView = new ReturnTypePaneView(returnTypeProperties);

            // Creating return type pane.
            this._returnTypePaneView.createReturnTypePane(diagramRenderingContext);

            // Closing the shown pane when another operation button is clicked.
            _.forEach(operationButtons, function (button) {
                button.click(function () {
                    _.forEach(operationButtons, function (buttonToClick) {
                        if (button !== buttonToClick && $(buttonToClick).data("showing-pane") == "true") {
                            $(buttonToClick).click();
                        }
                    });
                });
            });
        };

        /**
         * Shows and hide the custom scrolls depending on the amount scrolled.
         * @param {Element} container - The container of the SVG. i.e the parent of the SVG.
         * @param {Element} svgElement - The SVG element.
         */
        FunctionDefinitionView.prototype._showHideScrolls = function (container, svgElement) {
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
            if (parseInt($(container).width(), 10) >= parseInt($(svgElement).width(), 10)) {
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

        /**
         * Render statement container
         */
        FunctionDefinitionView.prototype.renderStatementContainer = function(){
            var statementContainerOpts = {};
            _.set(statementContainerOpts, 'model', this._model);
            _.set(statementContainerOpts, 'topCenter', this._defaultWorkerLifeLine.getTopCenter());
            _.set(statementContainerOpts, 'bottomCenter', this._defaultWorkerLifeLine.getBottomCenter());
            _.set(statementContainerOpts, 'width', this._defaultWorkerLifeLine.width());
            _.set(statementContainerOpts, 'container', this._defaultWorkerLifeLine.getContentArea().node());
            _.set(statementContainerOpts, 'toolPalette', this.toolPalette);
            _.set(statementContainerOpts, 'offset', {top: 40, bottom: 40});
            this._statementContainer = new StatementContainer(statementContainerOpts);
            this.listenTo(this._statementContainer.getBoundingBox(), 'bottom-edge-moved', this.defaultWorkerHeightChanged);
            this._statementContainer.render(this.diagramRenderingContext);
        };

        FunctionDefinitionView.prototype.defaultWorkerHeightChanged = function (dy) {
            this._defaultWorkerLifeLine.getBottomCenter().y(this._statementContainer.getBoundingBox().getBottom());
            this.getBoundingBox().h(this.getBoundingBox().h() + dy);
            this._totalHeight = this._totalHeight + dy;
            this.setSVGHeight(this._totalHeight);
        };

        /**
         * @param {BallerinaStatementView} statement
         */
        FunctionDefinitionView.prototype.visitStatement = function (statement) {
            var args = {model: statement, container: this._rootGroup.node(), viewOptions: {},
                toolPalette: this.toolPalette, messageManager: this.messageManager, parent: this};
            this._statementContainer.renderStatement(statement, args);
        };

        FunctionDefinitionView.prototype.canVisitConnectorDeclaration = function (connectorDeclaration) {
            return true;
        };

        /**
         * Calls the render method for a connector declaration.
         * @param connectorDeclaration
         */
        FunctionDefinitionView.prototype.visitConnectorDeclaration = function (connectorDeclaration) {
            // TODO: Get these values from the constants
            var offsetBetweenLifeLines = 50;
            var connectorContainer = this.getChildContainer().node(),
                connectorOpts = {
                    model: connectorDeclaration,
                    container: connectorContainer,
                    parentView: this,
                    lineHeight: this._defaultWorkerLifeLine.getTopCenter()
                                .absDistInYFrom(this._defaultWorkerLifeLine.getBottomCenter()),
                    messageManager: this.messageManager
                },
                connectorDeclarationView,
                center;

            var startX = 0;
            var self = this;

            if (!_.isEmpty(this._workerAndConnectorViews)) {
                startX = _.last(this._workerAndConnectorViews).getBoundingBox().getRight() + _.last(this._workerAndConnectorViews).getBoundingBox().w()/2 + offsetBetweenLifeLines;
            } else {
                startX = (this._defaultWorkerLifeLine).getBoundingBox().getRight() + (this._defaultWorkerLifeLine).getBoundingBox().w()/2 + offsetBetweenLifeLines;
            }

            center = new Point(startX, this._defaultWorkerLifeLine.getTopCenter().y());
            _.set(connectorOpts, 'centerPoint', center);
            connectorDeclarationView = new ConnectorDeclarationView(connectorOpts);
            this.diagramRenderingContext.getViewModelMap()[connectorDeclaration.id] = connectorDeclarationView;

            connectorDeclarationView.render();

            if (this._workerAndConnectorViews.length > 0) {
                // There are already added resource level connectors
                // New resource level connector listens to the current last resource level connector
                var lastConnector = _.last(this._workerAndConnectorViews);
                connectorDeclarationView.listenTo(lastConnector.getBoundingBox(), 'right-edge-moved', function (offset) {
                    self.moveFunctionDefinitionLevelConnector(this, offset);
                });
            }

            // Creating property pane
            var editableProperty = {
                propertyType: "text",
                key: "ConnectorDeclaration",
                model: connectorDeclarationView._model,
                getterMethod: connectorDeclarationView._model.getConnectorExpression,
                setterMethod: connectorDeclarationView._model.setConnectorExpression
            };
            connectorDeclarationView.createPropertyPane({
                model: connectorDeclarationView._model,
                lifeLineGroup:connectorDeclarationView._rootGroup,
                editableProperties: editableProperty
            });
            connectorDeclarationView.setParent(this);
            this._workerAndConnectorViews.push(connectorDeclarationView);
            this.getBoundingBox().on("bottom-edge-moved", function (dy) {
                this.getBoundingBox().h(this.getBoundingBox().h() + dy);

            }, connectorDeclarationView);

            connectorDeclarationView.listenTo(this.getLifeLineMargin(), 'moved', this.updateConnectorPositionCallback);
        };

        /**
         * @inheritDoc
         * @returns {_defaultWorker}
         */
        FunctionDefinitionView.prototype.getDefaultWorkerLifeLine = function () {
            return this._defaultWorkerLifeLine;
        };

        /**
         * @inheritDoc
         * @returns [_workerAndConnectorViews]
         */
        FunctionDefinitionView.prototype.getWorkerAndConnectorViews = function () {
            return this._workerAndConnectorViews;
        };

        FunctionDefinitionView.prototype.getChildContainer = function () {
            return this.getRootGroup();
        };

        FunctionDefinitionView.prototype.getLifeLineMargin = function () {
            return this._lifelineMargin;
        };

        /**
         * Return statement container
         * @return {StatementContainerView}
         */
        FunctionDefinitionView.prototype.getStatementContainer = function () {
            return this._statementContainer;
        };

        FunctionDefinitionView.prototype.moveFunctionDefinitionLevelConnector = function (connectorView, offset) {
            connectorView.getBoundingBox().move(offset, 0);
        };

        return FunctionDefinitionView;
    });
