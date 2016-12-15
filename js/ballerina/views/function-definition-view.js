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
define(['lodash', 'log', 'event_channel',  './canvas', './../ast/function-definition', './life-line', 'd3utils', 'd3',
        './worker-declaration-view', './statement-view-factory'],
    function (_, log, EventChannel, Canvas, FunctionDefinition, LifeLine, D3Utils, d3, WorkerDeclarationView, StatementViewFactory) {

        /**
         * The view to represent a function definition which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {FunctionDefinition} args.model - The function definition model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var FunctionDefinitionView = function (args) {
            this._statementExpressionViewList = [];
            Canvas.call(this, args);

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

            // TODO: Default worker's center point coordinates has to get with respect to the outer container margins
            // Drawing default worker
            var defaultWorkerOptions = {
                editable: true,
                centerPoint: {
                    x: 130,
                    y: 25
                },
                class: "lifeline",
                polygon: {
                    shape: "rect",
                    width: 120,
                    height: 30,
                    roundX: 0,
                    roundY: 0,
                    class: "lifeline-polygon"
                },
                droppableRect: {
                    width: 100,
                    height: 300,
                    roundX: 0,
                    roundY: 0,
                    class: "lifeline-droppableRect"
                },
                line: {
                    height: 280,
                    class: "lifeline-line"
                },
                text: {
                    value: "Default Worker",
                    class: "lifeline-text"
                },
                action: {
                    value: "Start"
                },
                child: {
                    value: true
                }
            };

            // Add a group to the svg
            var containerGroup = D3Utils.group(d3.select(_.first($(this._container).children().children())));

            // Check whether there is already created default worker and otherwise we create a new one
            if (_.isUndefined(this._defaultWorkerLifeLine)) {
                this._defaultWorkerLifeLine = new LifeLine(containerGroup, defaultWorkerOptions);
            }
            this._defaultWorkerLifeLine.render();
            this.getModel().accept(this);
        };

        /**
         * @param {BallerinaStatementView} statement
         */
        FunctionDefinitionView.prototype.visitStatement = function (statement) {
            var statementViewFactory = new StatementViewFactory();
            var containerGroup = d3.select(_.first($(this._container).children().children())).node();
            var args = {model: statement, container: containerGroup, viewOptions: undefined, parent:this};
            var statementView = statementViewFactory.getStatementView(args);
            this.diagramRenderingContext.getViewModelMap()[statement.id] = statementView;
            var x = 0;
            var y = 0;
            statementView.setParent(this);
            if(statementViewFactory.isGetActionStatement(statement)){
                _.each(this.diagramRenderingContext.getViewModelMap(),function(view){
                    var matchFound =  _.isEqual(statement.getConnector(),view.getModel());
                    if(matchFound) {
                        statementView.setConnectorView(view);
                    }
                });
                //_.each(this.getConnectorViewList(), function (view) {
                //  var matchFound =  _.isEqual(statement.getConnector(),view.getModel());
                //    if(matchFound) {
                //        statementView.setConnectorView(view);
                //    }
                //});
            }

            // TODO: we need to keep this value as a configurable value and read from constants
            var statementsGap = 40;
            var statementsWidth = 120;
            if (this._statementExpressionViewList.length > 0) {
                var lastStatement = this._statementExpressionViewList[this._statementExpressionViewList.length - 1];
                x = lastStatement.getXPosition();
                y = lastStatement.getYPosition() + lastStatement.getHeight() + statementsGap;
            } else {
                var defaultWorkerRectBottom = 40;
                y = defaultWorkerRectBottom;
                x = parseInt(this._defaultWorkerLifeLine.getMiddleLine().attr('x1')) - parseInt(statementsWidth/2);

            }
            this.diagramRenderingContext.getViewModelMap()[statement.id] = statementView;
            statementView.setBoundingBox(0, 0, x, y);
            this._statementExpressionViewList.push(statementView);
            statementView.render(this.diagramRenderingContext);
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

        return FunctionDefinitionView;
    });