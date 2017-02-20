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
        './connector-declaration-view', './statement-container', './variable-definitions-pane-view', './function-arguments-view',
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
            //set initial worker margin for the function
            this._workerLifelineMargin = new Axis(0, false);
            // Set the initial height control margin
            this._horizontalMargin = new Axis(0, true);
            this._lifeLineCenterGap = 180;
            this._offsetLastStatementGap = 100;

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
            var self = this;
            var container = this._rootGroup.node();
            // If the default worker, we skip
            if (!workerDeclaration.isDefaultWorker()) {
                var lastWorkerIndex = this.getLastWorkerLifeLineIndex();
                var newWorkerPosition = lastWorkerIndex === -1 ? 0 : lastWorkerIndex + 1;
                var centerPoint = undefined;
                if (newWorkerPosition === 0) {
                    centerPoint = this._defaultWorkerLifeLine.getTopCenter().clone().move(this._lifeLineCenterGap, 0);
                } else {
                    centerPoint = this._workerAndConnectorViews[lastWorkerIndex].getTopCenter()
                        .clone().move(this._lifeLineCenterGap, 0)
                }
                var lineHeight = this.getDefaultWorker().getBottomCenter().y() - centerPoint.y();
                var workerDeclarationOptions = {
                    model: workerDeclaration,
                    container: container,
                    centerPoint: centerPoint,
                    toolPalette: this.toolPalette,
                    messageManager: this.messageManager,
                    diagramRenderContext: this.getDiagramRenderingContext(),
                    line: {height: lineHeight},
                    title: workerDeclaration.getWorkerName()
                };
                var workerDeclarationView = new WorkerDeclarationView(workerDeclarationOptions);
                workerDeclarationView.setParent(this);
                workerDeclarationView.render();

                // Creating Expression Editor
                var editableProperty = {
                    propertyType: "text",
                    key: "WorkerDeclaration",
                    model: workerDeclarationView._model,
                    getterMethod: workerDeclarationView._model.getWorkerDeclarationStatement,
                    setterMethod: workerDeclarationView._model.setWorkerDeclarationStatement,
                    getDisplayTitle: workerDeclarationView._model.getWorkerName
                };
                workerDeclarationView.createPropertyPane({
                    model: workerDeclarationView._model,
                    lifeLineGroup: workerDeclarationView._rootGroup,
                    editableProperties: editableProperty
                });

                var statementContainer = workerDeclarationView.renderStatementContainer({offset: {top: 40, bottom: 50}});
                this.diagramRenderingContext.getViewModelMap()[workerDeclaration.id] = workerDeclarationView;
                this.listenWorkerToHorizontalMargin(workerDeclarationView);

                // Worker listen to its statement container
                workerDeclarationView.listenTo(statementContainer.getBoundingBox(), 'bottom-edge-moved', function(dy) {
                    var deltaMove = self.getDeltaMove(self.getDeepestChild(workerDeclarationView, dy), dy);
                    // Bellow logic is for properly align all the workers and the connectors
                    // Statement Container expands. we move the horizontal line
                    workerDeclarationView.getBottomCenter().y(workerDeclarationView.getBottomCenter().y() + deltaMove);
                    workerDeclarationView.getBoundingBox().h(workerDeclarationView.getBoundingBox().h() + deltaMove);
                    workerDeclarationView.stopListening(self.getHorizontalMargin());
                    self.getHorizontalMargin().setPosition(self.getHorizontalMargin().getPosition() + deltaMove);
                    self.listenWorkerToHorizontalMargin(workerDeclarationView);
                    // We need to change the height of the statement container, silently. This is because when this
                    // particular event is triggered, bounding box of the statement container has already changed,
                    // before hand we manipulate it with this logic
                    var statementContainerNewH = workerDeclarationView.getBottomCenter().y() - workerDeclarationView.getTopCenter().y();
                    // TODO: re consider stopListening of the following event of the statement container. This is because we silently change the heights
                    self.getStatementContainer().stopListening(self.getStatementContainer().getBoundingBox(), 'height-changed');
                    workerDeclarationView.getStatementContainer().changeHeightSilent(statementContainerNewH);
                    // Re initialize the above disabled event
                    // self.getStatementContainer().listenTo(self.getStatementContainer().getBoundingBox(), 'height-changed', function (offset) {
                    //     self.getStatementContainer()._mainDropZone.attr('height', parseFloat(self.getStatementContainer()._mainDropZone.attr('height')) + offset);
                    // });
                });

                // Set the workerLifeLineMargin to the right edge of the newly added worker
                this.getWorkerLifeLineMargin().setPosition(workerDeclarationView.getBoundingBox().getRight());
                if (lastWorkerIndex === this.getWorkerAndConnectorViews().length -1 &&
                    workerDeclarationView.getBoundingBox().getRight() > this.getBoundingBox().getRight()) {
                    // Worker is added as the last element for the ConnectorWorkerViewList.
                    // Only Connectors are there at the moment
                    this._parentView.getLifeLineMargin().setPosition(this._parentView.getLifeLineMargin().getPosition() + this._lifeLineCenterGap);
                    this.setContentMinWidth(workerDeclarationView.getBoundingBox().getRight());
                    this.setHeadingMinWidth(workerDeclarationView.getBoundingBox().getRight());
                }

                if (_.isEqual(newWorkerPosition, 0)) {
                    workerDeclarationView.listenTo(this.getDefaultWorker().getBoundingBox(), 'right-edge-moved', function (dx) {
                        workerDeclarationView.getBoundingBox().move(dx, 0);
                    });
                }

                statementContainer.listenTo(workerDeclarationView.getBoundingBox(), 'right-edge-moved', function (dx) {
                    statementContainer.getBoundingBox().move(dx, 0);
                });

                this.getWorkerAndConnectorViews().splice(newWorkerPosition, 0, workerDeclarationView);
            }
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
            if (!this.getModel().isMainFunction()) {
                this.getPanelIcon().addClass(_.get(this._viewOptions, "cssClass.function_icon", ""));
            } else {
                this.getPanelIcon().addClass(_.get(this._viewOptions, "cssClass.main_function_icon", ""));
            }

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

            if (!this.getModel().isMainFunction()) {
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
            } else {
                // Making the main function title non editable.
                $(this.getTitle()).removeAttr("contenteditable");
            }

            // Creating default worker
            var defaultWorkerOpts = {};
            _.set(defaultWorkerOpts, 'container', this._rootGroup.node());
            _.set(defaultWorkerOpts, 'title', 'FunctionWorker');
            _.set(defaultWorkerOpts, 'centerPoint', new Point(200, 80));

            // Check whether there is already created default worker and otherwise we create a new one
            if (_.isUndefined(this._defaultWorkerLifeLine)) {
                this._defaultWorkerLifeLine = new DefaultWorkerView(defaultWorkerOpts);
            }
            this._defaultWorkerLifeLine.render();

            // Set the workerLifeLineMargin to the end of the default worker
            this.getHorizontalMargin().setPosition(this._defaultWorkerLifeLine.getBoundingBox().getBottom());
            this.getWorkerLifeLineMargin().setPosition(this._defaultWorkerLifeLine.getBoundingBox().getRight());
            this.listenTo(this.getHorizontalMargin(), 'moved', function (dy) {
                self._defaultWorkerLifeLine.getBottomCenter().y(self._defaultWorkerLifeLine.getBottomCenter().y() + dy);
                // Silently increase the bounding box of the worker. Because this size change is due to the
                // horizontal margin movement, in other sense, for balancing with the other connectors/ workers' height
                // therefore we need to manually change the bbox height and the drop zone size of the statement container
                self.getStatementContainer().getBoundingBox().h(self.getStatementContainer().getBoundingBox().h() + dy, true);
                self.getStatementContainer().changeDropZoneHeight(dy);
                self._totalHeight = this.getHorizontalMargin().getPosition() + 85;
                self.setSVGHeight(this._totalHeight);
            });

            this._totalHeight = this.getHorizontalMargin().getPosition() + 85;
            this.setSVGHeight(this._totalHeight);
            this.renderStatementContainer();

            // TODO: change this accordingly, after the worker declaration introduced
            this.getWorkerLifeLineMargin().listenTo(this.getStatementContainer().getBoundingBox(), 'right-edge-moved', function (dx) {
                self.getWorkerLifeLineMargin().setPosition(self.getWorkerLifeLineMargin().getPosition() + dx);
            });

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

            this.setSVGWidth(this._container.width());

            if (!this.getModel().isMainFunction()) {
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

            }

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
                },
                disableEditing: this.getModel().isMainFunction()
            };

            // Creating arguments pane.
            ArgumentsView.createArgumentsPane(argumentsProperties, diagramRenderingContext);

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
            var self = this;

            this.listenTo(this._statementContainer.getBoundingBox(), 'bottom-edge-moved', function(dy){

                var deltaMove = this.getDeltaMove(this.getDeepestChild(this, dy), dy);
                // Statement Container expands. we move the horizontal line
                self._defaultWorkerLifeLine.getBottomCenter().y(self._defaultWorkerLifeLine.getBottomCenter().y() + deltaMove);
                // self._defaultWorker.getBoundingBox().h(self._defaultWorker.getBoundingBox().h() + dy);
                self.stopListening(self.getHorizontalMargin());
                self.getHorizontalMargin().setPosition(self.getHorizontalMargin().getPosition() + deltaMove);
                self.getBoundingBox().h(this.getBoundingBox().h() + deltaMove);
                // We need to change the height of the statement container, silently. This is because when this
                // particular event is triggered, bounding box of the statement container has already changed,
                // before hand we manipulate it with this logic
                var statementContainerNewH = self._defaultWorkerLifeLine.getBottomCenter().y() - self._defaultWorkerLifeLine.getTopCenter().y();
                // TODO: re consider stopListening of the following event of the statement container. This is because we silently change the heights
                self.getStatementContainer().stopListening(self.getStatementContainer().getBoundingBox(), 'height-changed');
                self.getStatementContainer().changeHeightSilent(statementContainerNewH);
                self._totalHeight = this.getHorizontalMargin().getPosition() + 85;
                self.setSVGHeight(this._totalHeight);
                // Re initialize the above disabled event
                // self.getStatementContainer().listenTo(self.getStatementContainer().getBoundingBox(), 'height-changed', function (offset) {
                //     self.getStatementContainer()._mainDropZone.attr('height', parseFloat(self.getStatementContainer()._mainDropZone.attr('height')) + offset);
                // });
                self.listenTo(self.getHorizontalMargin(), 'moved', function (dy) {
                    self._defaultWorkerLifeLine.getBottomCenter().y(self._defaultWorkerLifeLine.getBottomCenter().y() + dy);
                    // Silently increase the bounding box of the worker. Because this size change is due to the
                    // horizontal margin movement, in other sense, for balancing with the other connectors/ workers' height
                    // therefore we need to manually change the bbox height and the drop zone size of the statement container
                    self.getStatementContainer().getBoundingBox().h(self.getStatementContainer().getBoundingBox().h() + dy, true);
                    self.getStatementContainer().changeDropZoneHeight(dy);
                    self._totalHeight = self.getHorizontalMargin().getPosition() + 85;
                    self.setSVGHeight(self._totalHeight);
                });
            }, this);

            /* When the width of the statement container's bounding box changes, width of this resource definition's
             bounding box should also change.*/
            this._statementContainer.getBoundingBox().on('right-edge-moved', function (dw) {
                this._defaultWorkerLifeLine.getBoundingBox().zoomWidth(this._statementContainer.getBoundingBox().w());
            }, this);
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

        /**
         * This function will skip comment views rendering
         * @param commentStatement
         */
        FunctionDefinitionView.prototype.visitCommentStatement = function (commentStatement) {
        };

        /**
         * Calls the render method for a connector declaration.
         * @param connectorDeclaration
         */
        FunctionDefinitionView.prototype.visitConnectorDeclaration = function (connectorDeclaration) {
            var lastLifeLine = this.getLastLifeLine();
            var lastConnectorLifeLine = this.getWorkerAndConnectorViews()[this.getLastConnectorLifeLineIndex()];
            var self = this;
            // TODO: Get these values from the constants
            var offsetBetweenLifeLines = 50;
            var connectorContainer = this.getChildContainer().node(),
                connectorOpts = {
                    model: connectorDeclaration,
                    container: connectorContainer,
                    parentView: this,
                    lineHeight: this._defaultWorkerLifeLine.getTopCenter()
                                .absDistInYFrom(this._defaultWorkerLifeLine.getBottomCenter()),
                    messageManager: this.messageManager,
                    centerPoint: lastLifeLine.getTopCenter().clone().move(this._lifeLineCenterGap, 0)
                },
                connectorDeclarationView;

            connectorDeclarationView = new ConnectorDeclarationView(connectorOpts);
            this.diagramRenderingContext.getViewModelMap()[connectorDeclaration.id] = connectorDeclarationView;
            connectorDeclarationView.setParent(this);
            connectorDeclarationView.render();

            connectorDeclarationView.createPropertyPane();

            if (_.isNil(lastConnectorLifeLine)) {
                // This is the first connector we are adding
                connectorDeclarationView.listenTo(this.getWorkerLifeLineMargin(), 'moved', function (offset) {
                    self.moveFunctionDefinitionLevelConnector(this, offset);
                });
            } else {
                // There are already added connectors
                // Previously added connector stop listening to bounding box move.
                // Based on this event we increase the service container width
                lastConnectorLifeLine.stopListening(lastConnectorLifeLine.getBoundingBox(), 'right-edge-moved');
                connectorDeclarationView.listenTo(lastConnectorLifeLine.getBoundingBox(), 'right-edge-moved', function (offset) {
                    self.moveFunctionDefinitionLevelConnector(this, offset);
                });
            }

            /* If the adding connector (connectorDeclarationView) goes out of this function definition's view,
             then we need to expand this function definition's view. */
            if (connectorDeclarationView.getBoundingBox().getRight() > this.getBoundingBox().getRight()) {
                this._parentView.getLifeLineMargin().setPosition(this._parentView.getLifeLineMargin().getPosition()
                    + this._lifeLineCenterGap);
            }

            var connectorBBox = connectorDeclarationView.getBoundingBox();
            connectorDeclarationView.listenTo(connectorBBox, 'right-edge-moved', function (offset) {
                if (connectorBBox.getRight() > self.getBoundingBox().getRight()) {
                    self._parentView.getLifeLineMargin().setPosition(self._parentView.getLifeLineMargin().getPosition() + self._lifeLineCenterGap);
                }
            }, connectorDeclarationView);

            connectorDeclarationView.listenTo(this.getHorizontalMargin(), 'moved', function (dy) {
                connectorDeclarationView.getBoundingBox().h(connectorDeclarationView.getBoundingBox().h() + dy);
            });

            this._workerAndConnectorViews.push(connectorDeclarationView);
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

        /**
         * Set the horizontal margin
         * @param {Axis} horizontalMargin - horizontal margin
         */
        FunctionDefinitionView.prototype.setHorizontalMargin = function (horizontalMargin) {
            this._horizontalMargin = horizontalMargin;
        };

        /**
         * Get the horizontal margin
         * @return {Axis|*}
         */
        FunctionDefinitionView.prototype.getHorizontalMargin = function () {
            return this._horizontalMargin;
        };

        /**
         * Get the worker lifeline margin
         * @return {Axis}
         */
        FunctionDefinitionView.prototype.getWorkerLifeLineMargin = function () {
            return this._workerLifelineMargin;
        };

        /**
         * Set the worker lifeline margin
         * @param {Axis} workerLifeLineMargin
         */
        FunctionDefinitionView.prototype.setWorkerLifeLineMargin = function (workerLifeLineMargin) {
            this._workerLifelineMargin = workerLifeLineMargin;
        };

        /**
         * Get the last lifeline
         * @return {lifeLine} LifeLine
         */
        FunctionDefinitionView.prototype.getLastLifeLine = function () {
            if(this.getWorkerAndConnectorViews().length > 0 ){
                return _.last(this.getWorkerAndConnectorViews());
            }
            else{
                return this.getDefaultWorker();
            }
        };

        FunctionDefinitionView.prototype.getDefaultWorker = function () {
            return this._defaultWorkerLifeLine;
        };

        FunctionDefinitionView.prototype.getWorkerAndConnectorViews = function () {
            return this._workerAndConnectorViews;
        };

        /**
         * Set Minimum width of the content area
         * @param {number} minWidth - Minimum width
         */
        FunctionDefinitionView.prototype.setContentMinWidth = function (minWidth) {
            this._viewOptions.contentMinWidth = minWidth;
        };

        FunctionDefinitionView.prototype.getLastConnectorLifeLineIndex = function () {
            var index = _.findLastIndex(this.getWorkerAndConnectorViews(), function (lifeLine) {
                return lifeLine instanceof ConnectorDeclarationView;
            });
            return index;
        };

        FunctionDefinitionView.prototype.getLastWorkerLifeLineIndex = function () {
            var index = _.findLastIndex(this.getWorkerAndConnectorViews(), function (lifeLine) {
                return lifeLine instanceof WorkerDeclarationView;
            });
            return index;
        };

        FunctionDefinitionView.prototype.listenWorkerToHorizontalMargin = function (workerDeclarationView) {
            workerDeclarationView.listenTo(this.getHorizontalMargin(), 'moved', function (dy) {
                workerDeclarationView.getBottomCenter().y(workerDeclarationView.getBottomCenter().y() + dy);
                workerDeclarationView.getBoundingBox().h(workerDeclarationView.getBoundingBox().h() + dy);
                // Silently increase the bounding box of the worker. Because this size change is due to the
                // horizontal margin movement, in other sense, for balancing with the other connectors/ workers' height
                // therefore we need to manually change the bbox height and the drop zone size of the statement container
                workerDeclarationView.getStatementContainer().getBoundingBox().h(workerDeclarationView.getStatementContainer().getBoundingBox().h() + dy, true);
                workerDeclarationView.getStatementContainer().changeDropZoneHeight(dy);
            });
        };

        FunctionDefinitionView.prototype.getDeltaMove = function (deepestChild, dy) {
            var deltaMove = 0;
            if (dy > 0) {
                deltaMove = dy;
            } else {
                if (_.isNil(deepestChild)) {
                    deltaMove = dy;
                } else {
                    deltaMove = -(this.getHorizontalMargin().getPosition() -
                    this.getDiagramRenderingContext().getViewOfModel(deepestChild).getBoundingBox().getBottom() - this._offsetLastStatementGap);
                }
            }
            return deltaMove;
        };

        // TODO: Need to generalize this method, since functionDefinition, actionDefinition and resourceDefinition uses this
        FunctionDefinitionView.prototype.getDeepestChild = function (currentWorker, dy) {
            var self = this;
            var lastChildArr = [];

            this.getWorkerAndConnectorViews().forEach(function (worker) {
                if (worker instanceof WorkerDeclarationView) {
                    // if (worker.getModel().id === currentWorker.getModel().id && dy < 0) {
                    //     // TODO: Refactor logic
                    //     // Child we are removing, have not removed from the view list yet
                    //     lastChildArr.push(worker.getStatementContainer().getManagedStatements()[worker.getStatementContainer().getManagedStatements() - 2]);
                    // } else {
                    //     lastChildArr.push(_.last(worker.getStatementContainer().getManagedStatements()));
                    // }
                    // TODO: We need to rewrite this logic due to the current limitation of the element remove in statement container
                    lastChildArr.push(_.last(worker.getStatementContainer().getManagedStatements()));
                }
            });

            var sortedLastChildArr = _.sortBy(lastChildArr, function (child) {
                var stmtView = _.isNil(child) ? undefined : self.getDiagramRenderingContext().getViewOfModel(child);
                return _.isNil(stmtView) ? -1 : stmtView.getBoundingBox().getBottom();
            });

            var deepestChildStatement = _.last(sortedLastChildArr);
            var defaultWorkerLastChild = _.last(this.getStatementContainer().getManagedStatements());

            if (!_.isNil(deepestChildStatement) && _.isNil(defaultWorkerLastChild)) {
                return deepestChildStatement;
            } else if (!_.isNil(deepestChildStatement) && !_.isNil(defaultWorkerLastChild)) {
                if (this.getDiagramRenderingContext().getViewOfModel(deepestChildStatement).getBoundingBox().getBottom()
                    > this.getDiagramRenderingContext().getViewOfModel(defaultWorkerLastChild).getBoundingBox().getBottom()) {
                    return deepestChildStatement;
                } else {
                    return defaultWorkerLastChild;
                }
            } else {
                return defaultWorkerLastChild;
            }
        };

        return FunctionDefinitionView;
    });
