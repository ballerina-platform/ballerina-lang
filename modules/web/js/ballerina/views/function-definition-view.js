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
import _ from 'lodash';
import log from 'log';
import Alerts from 'alerts';
import SVGCanvas from './svg-canvas';
import FunctionDefinition from './../ast/function-definition';
import DefaultWorkerView from './default-worker';
import WorkerDeclarationView from './worker-declaration-view';
import Point from './point';
import Axis from './axis';
import ConnectorDeclarationView from './connector-declaration-view';
import StatementContainer from './statement-container';
import ArgumentsView from './function-arguments-view';
import ReturnTypePaneView from './return-types-pane-view';
import BallerinaASTFactory from 'ballerina/ast/ballerina-ast-factory';
import $ from 'jquery';

/**
 * The view to represent a function definition which is an AST visitor.
 * @class FunctionDefinitionView
 * @extends SVGCanvas
 */
class FunctionDefinitionView extends SVGCanvas {

    /**
     * @param {Object} args - Arguments for creating the view.
     * @param {FunctionDefinition} args.model - The function definition model.
     * @param {Object} args.container - The HTML container to which the view should be added to.
     * @param {Object} [args.viewOptions={}] - Configuration values for the view.
     * @constructor
     */
    constructor(args) {
        super(args);
        this._statementExpressionViewList = [];
        // TODO: Instead of using the parentView use the parent. Fix this from BallerinaView.js and bellow
        this._parentView = _.get(args, 'parentView');
        //set panel icon for the function
        this._viewOptions.panelIcon = _.get(args.viewOptions, 'cssClass.function_icon');
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
        this._lifeLineCenterGap = 120;
        this._offsetLastStatementGap = 100;

        if (_.isNil(this._model) || !(this._model instanceof FunctionDefinition)) {
            log.error('Function definition is undefined or is of different type.' + this._model);
            throw 'Function definition is undefined or is of different type.' + this._model;
        }

        if (_.isNil(this._container)) {
            log.error('Container for function definition is undefined.' + this._container);
            throw 'Container for function definition is undefined.' + this._container;
        }
    }

    /**
     * Child remove callback
     * @param {ASTNode} child - removed child
     */
    childRemovedCallback(child) {
        var self = this;
        if (BallerinaASTFactory.isStatement(child)) {
            this.getStatementContainer().childStatementRemovedCallback(child);
        } else if (BallerinaASTFactory.isConnectorDeclaration(child)) {
            var connectorViews = this.getConnectorViews();
            var nextConnector = undefined;
            var previousConnector = undefined;
            var connectorViewIndex = _.findIndex(connectorViews, function (view) {
                return view.getModel().id === child.id;
            });

            var currentConnector = connectorViews[connectorViewIndex];

            if (connectorViewIndex === 0) {
                // Deleted the first connector/ only connector
                currentConnector.stopListening(this.getWorkerLifeLineMargin());
                if (!_.isNil(connectorViews[connectorViewIndex + 1])) {
                    nextConnector = connectorViews[connectorViewIndex + 1];
                    nextConnector.stopListening(currentConnector.getBoundingBox());
                    nextConnector.listenTo(this.getWorkerLifeLineMargin(), 'moved', function (offset) {
                        nextConnector.getBoundingBox().move(offset, 0);
                    });
                }
            } else if (connectorViewIndex === connectorViews.length - 1) {
                // Deleted the last connector
                currentConnector.stopListening(connectorViews[connectorViewIndex].getBoundingBox());
            } else {
                // Deleted an intermediate Connector
                previousConnector = connectorViews[connectorViewIndex - 1];
                nextConnector = connectorViews[connectorViewIndex + 1];
                currentConnector.stopListening(previousConnector.getBoundingBox());
                nextConnector.stopListening(currentConnector.getBoundingBox());
                nextConnector.listenTo(previousConnector.getBoundingBox(), 'right-edge-moved', function (offset) {
                    self.moveFunctionDefinitionLevelConnector(nextConnector, offset);
                });
            }
            var connectorViewOriginalIndex = _.findIndex(this.getWorkerAndConnectorViews(), function (view) {
                return view.getModel().id === child.id;
            });
            this.getWorkerAndConnectorViews()[connectorViewOriginalIndex] = null;
            this.getWorkerAndConnectorViews().splice(connectorViewOriginalIndex, 1);
        } else if (BallerinaASTFactory.isWorkerDeclaration(child)) {
            var workerViews = this.getWorkerViews();
            var nextWorker = undefined;
            var previousWorker = undefined;
            var workerViewIndex = _.findIndex(workerViews, function (view) {
                return view.getModel().id === child.id;
            });

            var currentWorker = workerViews[workerViewIndex];

            if (workerViewIndex === 0) {
                // Deleted the first worker/ only worker
                nextWorker = workerViews[workerViewIndex + 1];
                // Since we deleted the first worker, now the worker lifeline margin listen to the default worker
                this.getWorkerLifeLineMargin().stopListening(currentWorker.getBoundingBox());
                this.getWorkerLifeLineMargin().listenTo(this.getDefaultWorker().getBoundingBox(),
                    'right-edge-moved', function (offset) {
                        self.getWorkerLifeLineMargin().setPosition(self.getWorkerLifeLineMargin().getPosition() + offset);
                    });
            } else if (workerViewIndex === workerViews.length - 1) {
                // Deleted the last worker
                this.getWorkerLifeLineMargin().stopListening(currentWorker.getBoundingBox());
                previousWorker = workerViews[workerViewIndex - 1];
                if (!_.isNil(previousWorker)) {
                    this.getWorkerLifeLineMargin().listenTo(previousWorker.getBoundingBox(), 'right-edge-moved', function (offset) {
                        self.getWorkerLifeLineMargin().setPosition(self.getWorkerLifeLineMargin().getPosition() + offset);
                    });
                }
            } else {
                // Deleted an intermediate Worker
                previousWorker = workerViews[workerViewIndex - 1];
                nextWorker = workerViews[workerViewIndex + 1];
                currentWorker.stopListening(previousWorker.getBoundingBox());
                nextWorker.stopListening(currentWorker.getBoundingBox());
                nextWorker.listenTo(previousWorker.getBoundingBox(), 'right-edge-moved', function (offset) {
                    self.moveFunctionDefinitionLevelWorker(nextWorker, offset);
                });
            }
            var workerViewOriginalIndex = _.findIndex(this.getWorkerAndConnectorViews(), function (view) {
                return view.getModel().id === child.id;
            });
            this.getWorkerAndConnectorViews()[workerViewOriginalIndex] = null;
            this.getWorkerAndConnectorViews().splice(workerViewOriginalIndex, 1);
        }

        // Remove the connector/ worker from the diagram rendering context
        delete this.diagramRenderingContext.getViewModelMap()[child.id];
    }

    setModel(model) {
        if (!_.isNil(model) && model instanceof FunctionDefinition) {
            this._model = model;
        } else {
            log.error('Function definition undefined or is of different type.' + model);
            throw 'Function definition undefined or is of different type.' + model;
        }
    }

    setContainer(container) {
        if (!_.isNil(container)) {
            this._container = container;
        } else {
            log.error('Container for function definition is undefined.' + container);
            throw 'Container for function definition is undefined.' + container;
        }
    }

    setViewOptions(viewOptions) {
        this._viewOptions = viewOptions;
    }

    setChildContainer(svg) {
        if (!_.isNil(svg)) {
            this._childContainer = svg;
        }
    }

    getModel() {
        return this._model;
    }

    getContainer() {
        return this._container;
    }

    getViewOptions() {
        return this._viewOptions;
    }

    // TODO: this method this never used so is this._childContainer, remove them.
    // getChildContainer() {
    //     return this._childContainer ;
    // }

    canVisitFunctionDefinition() {
        return true;
    }

    /**
     * @override
     */
    isAValidNodeForCanvasDropArea(node) {
        var nodeFactory = this._model.getFactory();
        // IMPORTANT: override canvas's default validation logic
        // Canvas drop zone is for worker and connector declarations only.
        // Statements should only be allowed on top of function worker's drop zone.
        return nodeFactory.isConnectorDeclaration(node)
            || nodeFactory.isWorkerDeclaration(node);
    }

    /**
     * Calls the render method for a worker declaration.
     * @param {WorkerDeclaration} workerDeclaration - The worker declaration model.
     */
    visitWorkerDeclaration(workerDeclaration) {
        var self = this;
        var container = this._rootGroup.node();
        // If the default worker, we skip
        if (!workerDeclaration.isDefaultWorker()) {
            var lastWorkerIndex = this.getLastWorkerLifeLineIndex();
            var lastWorker = this.getWorkerAndConnectorViews()[lastWorkerIndex];
            var newWorkerPosition = lastWorkerIndex === -1 ? 0 : lastWorkerIndex + 1;
            var centerPoint = undefined;
            if (newWorkerPosition === 0) {
                centerPoint = new Point(this._defaultWorkerLifeLine.getBoundingBox().getRight(),
                    this._defaultWorkerLifeLine.getTopCenter().y());
                centerPoint.move(this._lifeLineCenterGap, 0);
            } else {
                centerPoint = new Point(this._workerAndConnectorViews[lastWorkerIndex].getBoundingBox().getRight(),
                    this._workerAndConnectorViews[lastWorkerIndex].getTopCenter().y());
                centerPoint.move(this._lifeLineCenterGap, 0);
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
                propertyType: 'text',
                key: 'WorkerDeclaration',
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

            workerDeclarationView.listenTo(this.getHorizontalMargin(), 'moved', function (dy) {
                workerDeclarationView.getBottomCenter().y(workerDeclarationView.getBottomCenter().y() + dy);
                var newDropZoneHeight = workerDeclarationView.getBottomCenter().y() - workerDeclarationView.getTopCenter().y();
                workerDeclarationView.getStatementContainer().getBoundingBox().h(newDropZoneHeight, true);
                workerDeclarationView.getStatementContainer().changeDropZoneHeight(newDropZoneHeight);
            });

            workerDeclarationView.listenTo(statementContainer, 'statement-container-height-adjusted', function (dh) {
                var childrenLength = workerDeclarationView.getStatementContainer().getManagedStatements().length;
                var deltaMove = self.getDeltaMove(self.getDeepestChild(self, dh), dh, childrenLength);
                self.getHorizontalMargin().setPosition(self.getHorizontalMargin().getPosition() + deltaMove);
            });

            // Set the workerLifeLineMargin to the right edge of the newly added worker
            this.getWorkerLifeLineMargin().setPosition(workerDeclarationView.getBoundingBox().getRight());

            if (newWorkerPosition > 0) {
                // There are already added workers
                this.getWorkerLifeLineMargin().stopListening(this.getWorkerAndConnectorViews()[lastWorkerIndex].getBoundingBox());
                workerDeclarationView.listenTo(lastWorker.getBoundingBox(), 'right-edge-moved', function (offset) {
                    self.moveFunctionDefinitionLevelWorker(workerDeclarationView, offset);
                });
            } else {
                this.getWorkerLifeLineMargin().stopListening(this.getDefaultWorker().getBoundingBox());
            }
            this.getWorkerLifeLineMargin().listenTo(workerDeclarationView.getBoundingBox(), 'right-edge-moved', function (offset) {
                self.getWorkerLifeLineMargin().setPosition(self.getWorkerLifeLineMargin().getPosition() + offset);
            });

            if (lastWorkerIndex === this.getWorkerAndConnectorViews().length -1 &&
                workerDeclarationView.getBoundingBox().getRight() >= this.getBoundingBox().getRight()) {
                // Worker is added as the last element for the ConnectorWorkerViewList.
                // Only Workers are there at the moment
                this.setSVGWidth(workerDeclarationView.getBoundingBox().getRight() + this._lifeLineCenterGap);
                this.setContentMinWidth(workerDeclarationView.getBoundingBox().getRight());
                this.setHeadingMinWidth(workerDeclarationView.getBoundingBox().getRight());
            }

            if (_.isEqual(newWorkerPosition, 0)) {
                workerDeclarationView.listenTo(this.getDefaultWorker().getBoundingBox(), 'right-edge-moved', function (dx) {
                    workerDeclarationView.getBoundingBox().move(dx, 0);
                });
            }

            statementContainer.listenTo(workerDeclarationView.getBoundingBox(), 'left-edge-moved', function (dx) {
                statementContainer.getBoundingBox().move(dx, 0);
            });

            this.getWorkerAndConnectorViews().splice(newWorkerPosition, 0, workerDeclarationView);
        }
    }

    /**
     * Rendering the view for function definition.
     * @returns {group} The svg group which contains the elements of the function definition view.
     */
    render(diagramRenderingContext) {
        this.setDiagramRenderingContext(diagramRenderingContext);

        // Draws the outlying body of the function.
        this.drawAccordionCanvas(this._viewOptions, this.getModel().getID(), this.getModel().type.toLowerCase(), this.getModel().getFunctionName());

        // Setting the styles for the canvas icon.
        if (!this.getModel().isMainFunction()) {
            this.getPanelIcon().addClass(_.get(this._viewOptions, 'cssClass.function_icon', ''));
        } else {
            this.getPanelIcon().addClass(_.get(this._viewOptions, 'cssClass.main_function_icon', ''));
        }

        var currentContainer = $('#' + this.getModel().getID());
        this._container = currentContainer;
        this.getBoundingBox().fromTopLeft(new Point(0, 0), currentContainer.width(), currentContainer.height());
        var self = this;

        //Scroll to the added position and highlight the heading
        $(_.get(this._viewOptions, 'design_view.container', '')).scrollTop(currentContainer.parent().position().top);
        var hadingBox = $('#' + this.getModel().getID() + '_heading');
        var canvas_heading_new = _.get(this._viewOptions, 'cssClass.canvas_heading_new', '');
        var new_drop_timeout = _.get(this._viewOptions, 'design_view.new_drop_timeout', '');
        hadingBox.addClass(canvas_heading_new);
        setTimeout(function(){hadingBox.removeClass(canvas_heading_new);}, new_drop_timeout);

        if (!this.getModel().isMainFunction()) {
            $(this.getTitle()).text(this.getModel().getFunctionName())
                .on('change paste keyup', function () {
                    self.getModel().setFunctionName($(this).text());
                }).on('click', function (event) {
                    event.stopPropagation();
                }).on('blur', function () {
                    if ($(this).text().length > 50) {
                        var textToDisplay = $(this).text().substring(0, 47) + '...';
                        $(this).text(textToDisplay);
                    }
                }).on('focus', function () {
                    $(this).text(self._model.getFunctionName());
                }).keypress(function (e) {
                    /* Ignore Delete and Backspace keypress in firefox and capture other keypress events.
                     (Chrome and IE ignore keypress event of these keys in browser level)*/
                    if (!_.isEqual(e.key, 'Delete') && !_.isEqual(e.key, 'Backspace')) {
                        var enteredKey = e.which || e.charCode || e.keyCode;
                        // Disabling enter key
                        if (_.isEqual(enteredKey, 13)) {
                            e.stopPropagation();
                            return false;
                        }

                        var newServiceName = $(this).text() + String.fromCharCode(enteredKey);

                        try {
                            self.getModel().setFunctionName(newServiceName);
                        } catch (error) {
                            Alerts.error(error);
                            e.stopPropagation();
                            return false;
                        }
                    }
                });
        } else {
            // Making the main function title non editable.
            $(this.getTitle()).removeAttr('contenteditable');
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
            var newDropZoneHeight = self._defaultWorkerLifeLine.getBottomCenter().y() - self._defaultWorkerLifeLine.getTopCenter().y();
            self.getStatementContainer().getBoundingBox().h(newDropZoneHeight, true);
            self.getStatementContainer().changeDropZoneHeight(newDropZoneHeight);
            self._totalHeight = this.getHorizontalMargin().getPosition() + 85;
            self.setSVGHeight(this._totalHeight);
        });

        this._totalHeight = this.getHorizontalMargin().getPosition() + 85;
        this.setSVGHeight(this._totalHeight);
        this.renderStatementContainer();

        this.listenTo(this.getStatementContainer().getBoundingBox(), 'width-changed', function (dw) {
            self.getDefaultWorker().getBoundingBox().w(self.getDefaultWorker().getBoundingBox().w() + dw);
            self.getDefaultWorker().move(dw/2, 0);
        });

        this.getWorkerLifeLineMargin().listenTo(this.getDefaultWorker().getBoundingBox(), 'right-edge-moved', function (dx) {
            self.getWorkerLifeLineMargin().setPosition(self.getWorkerLifeLineMargin().getPosition() + dx);
        });

        this.getModel().accept(this);
        //Removing all the registered 'child-added' event listeners for this model.
        //This is needed because we are not unregistering registered event while the diagram element deletion.
        //Due to that, sometimes we are having two or more view elements listening to the 'child-added' event of same model.
        this.getModel().off('child-added');
        this.getModel().on('child-added', function (child) {
            self.visit(child);
            self.getModel().trigger('child-visited', child);

            // Show/Hide scrolls.
            self._showHideScrolls(self._container, self.getChildContainer().node().ownerSVGElement);
        });

        var operationsPane = this.getOperationsPane();

        var operationButtons = [];

        this.setSVGWidth(this._container.width());

        if (!this.getModel().isMainFunction()) {
            // Creating return type icon.
            var panelReturnTypeIcon = $('<i/>', {
                class: 'fw fw-export pull-right right-icon-clickable hoverable',
                title: 'Return Types'
            }).appendTo(operationsPane).tooltip();

            $(panelReturnTypeIcon).click((event) => {
                this._returnTypePaneView.reloadReturnTypeDropDown();
                event.stopPropagation();
            });

            operationButtons.push(panelReturnTypeIcon);

            // Adding separator for return type icon.
            $('<span class=\'pull-right canvas-operations-separator\'>|</span>').appendTo(operationsPane);

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
        var panelArgumentsIcon = $('<i/>', {
            class: 'fw fw-import pull-right right-icon-clickable hoverable',
            title: 'Arguments'
        }).appendTo(operationsPane).tooltip();

        $(panelArgumentsIcon).click((event) => {
            event.stopPropagation();
            this._argumentsView.reloadArgumentTypeDropDown();
        });

        operationButtons.push(panelArgumentsIcon);

        // Adding separator for arguments icon.
        $('<span class=\'pull-right canvas-operations-separator\'>|</span>').appendTo(operationsPane);

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
            view: this,
            disableEditing: this.getModel().isMainFunction()
        };

        // Creating arguments pane.
        this._argumentsView = ArgumentsView.createArgumentsPane(argumentsProperties, diagramRenderingContext);

        // Closing the shown pane when another operation button is clicked.
        _.forEach(operationButtons, function (button) {
            button.click(function () {
                _.forEach(operationButtons, function (buttonToClick) {
                    if (button !== buttonToClick && $(buttonToClick).data('showing-pane') === 'true') {
                        $(buttonToClick).click();
                    }
                });
            });
        });
    }

    /**
     * Shows and hide the custom scrolls depending on the amount scrolled.
     * @param {Element} container - The container of the SVG. i.e the parent of the SVG.
     * @param {Element} svgElement - The SVG element.
     */
    _showHideScrolls(container, svgElement) {
        // Creating scroll panes.
        var leftScroll = $(this.getChildContainer().node().ownerSVGElement.parentElement)
            .find('.service-left-scroll').get(0);
        var rightScroll = $(this.getChildContainer().node().ownerSVGElement.parentElement)
            .find('.service-right-scroll').get(0);

        // Setting heights of the scrolls.
        $(leftScroll).height($(container).height());
        $(rightScroll).height($(container).height());

        // Positioning the arrows of the scrolls to the middle.
        $(leftScroll).find('i').css('padding-top', ($(container).height() / 2) - (parseInt($(leftScroll).find('i').css('font-size'), 10) / 2) + 'px');
        $(rightScroll).find('i').css('padding-top', ($(container).height() / 2) - (parseInt($(rightScroll).find('i').css('font-size'), 10) / 2) + 'px');

        // Showing/Hiding scrolls.
        if (parseInt($(container).width(), 10) >= parseInt($(svgElement).width(), 10)) {
            // If the svg width is less than or equal to the container, then no need to show the arrows.
            $(leftScroll).hide();
            $(rightScroll).hide();
        } else {
            // If the svg width is greater than the width of the container...
            if ($(container).scrollLeft() === 0) {
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
    }

    /**
     * Render statement container
     */
    renderStatementContainer() {
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

        this.listenTo(this.getStatementContainer(), 'statement-container-height-adjusted', function (dh) {
            var childrenLength = self.getStatementContainer().getManagedStatements().length;
            var deltaMove = self.getDeltaMove(self.getDeepestChild(self, dh), dh, childrenLength);
            self.getHorizontalMargin().setPosition(self.getHorizontalMargin().getPosition() + deltaMove);
        });
        this._statementContainer.render(this.diagramRenderingContext);
    }

    defaultWorkerHeightChanged(dy) {
        this._defaultWorkerLifeLine.getBottomCenter().y(this._statementContainer.getBoundingBox().getBottom());
        this.getBoundingBox().h(this.getBoundingBox().h() + dy);
        this._totalHeight = this._totalHeight + dy;
        this.setSVGHeight(this._totalHeight);
    }

    /**
     * @param {BallerinaStatementView} statement
     */
    visitStatement(statement) {
        var args = {model: statement, container: this._rootGroup.node(), viewOptions: {},
            toolPalette: this.toolPalette, messageManager: this.messageManager, parent: this};
        this._statementContainer.renderStatement(statement, args);
    }

    /**
     * This function will skip comment views rendering
     */
    visitCommentStatement() {
    }

    /**
     * Calls the render method for a connector declaration.
     * @param connectorDeclaration
     */
    visitConnectorDeclaration(connectorDeclaration) {
        var lastLifeLine = this.getLastLifeLine();
        var lastConnectorLifeLine = this.getWorkerAndConnectorViews()[this.getLastConnectorLifeLineIndex()];
        var self = this;
        var centerPoint = new Point(lastLifeLine.getBoundingBox().getRight(), lastLifeLine.getTopCenter().y());
        centerPoint.move(this._lifeLineCenterGap, 0);

        var connectorContainer = this.getChildContainer().node(),
            connectorOpts = {
                model: connectorDeclaration,
                container: connectorContainer,
                parentView: this,
                lineHeight: this._defaultWorkerLifeLine.getTopCenter()
                            .absDistInYFrom(this._defaultWorkerLifeLine.getBottomCenter()),
                messageManager: this.messageManager,
                centerPoint: centerPoint
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
        if (connectorDeclarationView.getBoundingBox().getRight() >= this.getBoundingBox().getRight()) {
            this.setSVGWidth(connectorDeclarationView.getBoundingBox().getRight() + this._lifeLineCenterGap);
        }

        var connectorBBox = connectorDeclarationView.getBoundingBox();
        connectorDeclarationView.listenTo(connectorBBox, 'right-edge-moved', function () {
            if (connectorBBox.getRight() >= self.getBoundingBox().getRight()) {
                self.setSVGWidth(connectorDeclarationView.getBoundingBox().getRight() + self._lifeLineCenterGap);
            }
        }, connectorDeclarationView);

        connectorDeclarationView.listenTo(this.getHorizontalMargin(), 'moved', function (dy) {
            connectorDeclarationView.getBoundingBox().h(connectorDeclarationView.getBoundingBox().h() + dy);
        });

        this._workerAndConnectorViews.push(connectorDeclarationView);
    }

    /**
     * @inheritDoc
     * @returns {_defaultWorker}
     */
    getDefaultWorkerLifeLine() {
        return this._defaultWorkerLifeLine;
    }

    /**
     * @inheritDoc
     * @returns [_workerAndConnectorViews]
     */
    getWorkerAndConnectorViews() {
        return this._workerAndConnectorViews;
    }

    getChildContainer() {
        return this.getRootGroup();
    }

    /**
     * Return statement container
     * @return {StatementContainerView}
     */
    getStatementContainer() {
        return this._statementContainer;
    }

    moveFunctionDefinitionLevelConnector(connectorView, offset) {
        connectorView.getBoundingBox().move(offset, 0);
    }

    moveFunctionDefinitionLevelWorker(resourceView, offset) {
        resourceView.getBoundingBox().move(offset, 0);
    }

    /**
     * Set the horizontal margin
     * @param {Axis} horizontalMargin - horizontal margin
     */
    setHorizontalMargin(horizontalMargin) {
        this._horizontalMargin = horizontalMargin;
    }

    /**
     * Get the horizontal margin
     * @return {Axis|*}
     */
    getHorizontalMargin() {
        return this._horizontalMargin;
    }

    /**
     * Get the worker lifeline margin
     * @return {Axis}
     */
    getWorkerLifeLineMargin() {
        return this._workerLifelineMargin;
    }

    /**
     * Set the worker lifeline margin
     * @param {Axis} workerLifeLineMargin
     */
    setWorkerLifeLineMargin(workerLifeLineMargin) {
        this._workerLifelineMargin = workerLifeLineMargin;
    }

    /**
     * Get the last lifeline
     * @return {lifeLine} LifeLine
     */
    getLastLifeLine() {
        if(this.getWorkerAndConnectorViews().length > 0 ){
            return _.last(this.getWorkerAndConnectorViews());
        }
        else{
            return this.getDefaultWorker();
        }
    }

    getDefaultWorker() {
        return this._defaultWorkerLifeLine;
    }

    /**
     * Set Minimum width of the content area
     * @param {number} minWidth - Minimum width
     */
    setContentMinWidth(minWidth) {
        this._viewOptions.contentMinWidth = minWidth;
    }

    getLastConnectorLifeLineIndex() {
        var index = _.findLastIndex(this.getWorkerAndConnectorViews(), function (lifeLine) {
            return lifeLine instanceof ConnectorDeclarationView;
        });
        return index;
    }

    getLastWorkerLifeLineIndex() {
        var index = _.findLastIndex(this.getWorkerAndConnectorViews(), function (lifeLine) {
            return lifeLine instanceof WorkerDeclarationView;
        });
        return index;
    }

    getDeltaMove(deepestChild, dy, childrenLength) {
        var deltaMove = 0;
        if (dy < 0) {
            if (_.isNil(deepestChild) || childrenLength === 1) {
                deltaMove = dy;
            } else {
                deltaMove = -(this.getHorizontalMargin().getPosition() -
                this.getDiagramRenderingContext().getViewOfModel(deepestChild).getBoundingBox().getBottom() - this._offsetLastStatementGap);
            }
        } else {
            deltaMove = dy;
        }
        return deltaMove;
    }

    // TODO: Need to generalize this method, since functionDefinition, actionDefinition and resourceDefinition uses this
    getDeepestChild() {
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
    }

    getWorkerViews() {
        var workers = _.filter(this.getWorkerAndConnectorViews(), function (view) {
            return BallerinaASTFactory.isWorkerDeclaration(view.getModel());
        });
        return workers;
    }

    getConnectorViews() {
        var connectors = _.filter(this.getWorkerAndConnectorViews(), function (view) {
            return BallerinaASTFactory.isConnectorDeclaration(view.getModel());
        });
        return connectors;
    }
}

export default FunctionDefinitionView;
