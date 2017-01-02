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
define(['lodash', 'log', 'event_channel',  './canvas', './../ast/function-definition', './default-worker', 'd3utils', 'd3',
        './worker-declaration-view', './statement-view-factory', './point', './axis', './connector-declaration-view', './statement-container'],
    function (_, log, EventChannel, Canvas, FunctionDefinition, DefaultWorkerView, D3Utils, d3, WorkerDeclarationView,
              StatementViewFactory, Point, Axis, ConnectorDeclarationView, StatementContainer) {

        /**
         * The view to represent a function definition which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {FunctionDefinition} args.model - The function definition model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var FunctionDefinitionView = function (args) {
            Canvas.call(this, args);
            //set initial connector margin for the service
            this._lifelineMargin = new Axis(210, false);
            this._statementExpressionViewList = [];
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

        FunctionDefinitionView.prototype.canVisitWorkerDeclaration = function () {
            return true;
        };

        FunctionDefinitionView.prototype.canVisitFunctionDefinition = function () {
            return true;
        };

        /**
         * Calls the render method for a worker declaration.
         * @param {WorkerDeclaration} workerDeclaration - The resource definition model.
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
            this.diagramRenderingContext = diagramRenderingContext;
            this.drawAccordionCanvas(this._container, this._viewOptions, this._model.id, this._model.type.toLowerCase(), undefined);
            var divId = this._model.id;
            var currentContainer = $('#'+ divId);
            this._container = currentContainer;
            var self = this;

            // Creating default worker
            var defaultWorkerOpts = {};
            _.set(defaultWorkerOpts, 'container', this._rootGroup.node());
            _.set(defaultWorkerOpts, 'title', 'FunctionWorker');
            _.set(defaultWorkerOpts, 'centerPoint', new Point(130, 25));

            // Check whether there is already created default worker and otherwise we create a new one
            if (_.isUndefined(this._defaultWorkerLifeLine)) {
                this._defaultWorkerLifeLine = new DefaultWorkerView(defaultWorkerOpts);
            }
            this._defaultWorkerLifeLine.render();
            this._totalHeight = this._defaultWorkerLifeLine.getBoundingBox().getBottom() + 20;
            this.setServiceContainerHeight(this._totalHeight);
            this.renderStatementContainer();
            this.getModel().accept(this);
            this._model.on('child-added', function (child) {
                self.visit(child);
                self._model.trigger("childVisitedEvent", child);
            });
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
            this._statementContainer = new StatementContainer(statementContainerOpts);
            this.listenTo(this._statementContainer.getBoundingBox(), 'bottom-edge-moved', function(dy){
                this._defaultWorkerLifeLine.getBottomCenter().y(this._statementContainer.getBoundingBox().getBottom());
                this.getBoundingBox().h(this.getBoundingBox().h() + dy);
            });
            this._statementContainer.render(this.diagramRenderingContext);
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
            var topBottomTotalGap = 100;
            var connectorContainer = this.getChildContainer().node(),
                connectorOpts = {
                    model: connectorDeclaration,
                    container: connectorContainer,
                    parentView: this,
                    lineHeight: this.getBoundingBox().h() - topBottomTotalGap,
                    messageManager: this.messageManager
                },
                connectorDeclarationView,
                center;

            var startX = 0;
            if (!_.isEmpty(this._workerAndConnectorViews)) {
                startX = _.last(this._workerAndConnectorViews).getBoundingBox().getRight() + _.last(this._workerAndConnectorViews).getBoundingBox().w()/2 + offsetBetweenLifeLines;
            } else {
                startX = (this._defaultWorkerLifeLine).getBoundingBox().getRight() + (this._defaultWorkerLifeLine).getBoundingBox().w()/2 + offsetBetweenLifeLines;
            }

            center = new Point(startX, this._defaultWorkerLifeLine.getTopCenter().y());
            _.set(connectorOpts, 'centerPoint', center);
            connectorDeclarationView = new ConnectorDeclarationView(connectorOpts);
            this.diagramRenderingContext.getViewModelMap()[connectorDeclaration.id] = connectorDeclarationView;
            this._workerAndConnectorViews.push(connectorDeclarationView);

            connectorDeclarationView.render();

            // Creating property pane
            var editableProperties = [
                {
                    propertyType: "text",
                    key: "Name",
                    model: connectorDeclarationView._model,
                    getterMethod: connectorDeclarationView._model.getConnectorName,
                    setterMethod: connectorDeclarationView._model.setConnectorName
                },
                {
                    propertyType: "text",
                    key: "Uri",
                    model: connectorDeclarationView._model,
                    getterMethod: connectorDeclarationView._model.getUri,
                    setterMethod: connectorDeclarationView._model.setUri
                },
                {
                    propertyType: "text",
                    key: "Timeout",
                    model: connectorDeclarationView._model,
                    getterMethod: connectorDeclarationView._model.getTimeout,
                    setterMethod: connectorDeclarationView._model.setTimeout
                }
            ];
            connectorDeclarationView.createPropertyPane({
                model: connectorDeclarationView._model,
                lifeLineGroup:connectorDeclarationView._rootGroup,
                editableProperties: editableProperties
            });
            connectorDeclarationView.setParent(this);
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
            return this._rootGroup;
        };

        FunctionDefinitionView.prototype.getLifeLineMargin = function () {
            return this._lifelineMargin;
        };

        return FunctionDefinitionView;
    });