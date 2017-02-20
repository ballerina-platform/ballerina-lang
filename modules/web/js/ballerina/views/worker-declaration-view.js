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

define(['lodash', 'jquery', './ballerina-view', './../ast/worker-declaration', 'log', 'd3utils', './life-line', 'd3',
        './statement-container', 'ballerina/ast/ballerina-ast-factory'],
    function (_, $, BallerinaView, WorkerDeclaration, log, D3Utils, LifeLine, d3, StatementContainer, BallerinaASTFactory) {

        /**
         * The view to represent a worker declaration which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {WorkerDeclaration} args.model - The worker declaration model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var WorkerDeclarationView = function (args) {
            this._totalHeightGap = 50;
            this._parent = _.get(args, "parent");
            this._LifeLineCenterGap = 180;
            this._statementContainer = undefined;
            this._middleRectangle = undefined;

            if (_.isNil(_.get(args, 'toolPalette'))) {
                log.error("Tool Palette is undefined");
                throw "Tool Palette is undefined";
            }
            this._toolPalette = _.get(args, 'toolPalette');

            if (_.isNil(_.get(args, 'messageManager'))) {
                log.error("Message Manager is undefined");
                throw "Message Manager is undefined";
            }
            this._messageManager = _.get(args, 'messageManager');
            _.set(args, 'name',  _.get(args, 'name') || 'worker1');
            _.set(args, 'cssClass.group',  _.get(args, 'cssClass.group', 'worker-life-line'));
            _.set(args, 'cssClass.topPolygon', _.get(args, 'cssClass.topPolygon', 'connector-life-line-top-polygon worker-lifeline-grayed'));
            _.set(args, 'cssClass.bottomPolygon', _.get(args, 'cssClass.bottomPolygon', 'connector-life-line-top-polygon worker-lifeline-grayed'));
            _.set(args, 'line.height',  _.get(args, 'line.height', 290));
            _.set(args, 'content.offset',  {top: 10, bottom: 10});
            LifeLine.call(this, args);

            if (_.isNil(this._model) || !(this._model instanceof WorkerDeclaration)) {
                log.error("Worker declaration is undefined or is of different type." + this._model);
                throw "Worker declaration is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for Worker declaration is undefined." + this._container);
                throw "Container for Worker declaration is undefined." + this._container;
            }
            this.init();
        };

        WorkerDeclarationView.prototype = Object.create(LifeLine.prototype);
        WorkerDeclarationView.prototype.constructor = WorkerDeclarationView;

        WorkerDeclarationView.prototype.init = function () {
            var self = this;
            this._model.on('child-added', function(child){
                self.visit(child);
            });
            this._model.on('child-removed', this.childRemovedCallback, this);
            this._model.on('before-remove', this.onBeforeModelRemove, this);
        };

        WorkerDeclarationView.prototype.render = function (diagramRenderingContext) {
            Object.getPrototypeOf(this.constructor.prototype).render.call(this, diagramRenderingContext);
        };

        WorkerDeclarationView.prototype.setModel = function (model) {
            if (!_.isNil(model)) {
                this._model = model;
            } else {
                log.error("Worker declaration definition undefined or is of different type." + model);
                throw "Worker declaration definition undefined or is of different type." + model;
            }
        };

        WorkerDeclarationView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for worker declaration is undefined." + container);
                throw "Container for worker declaration is undefined." + container;
            }
        };

        WorkerDeclarationView.prototype.getModel = function () {
            return this._model;
        };

        WorkerDeclarationView.prototype.getContainer = function () {
            return this._container;
        };

        /**
         * Render the statement container
         */
        WorkerDeclarationView.prototype.renderStatementContainer = function (args) {
            var statementContainerOpts = {};
            _.set(statementContainerOpts, 'model', this._model);
            _.set(statementContainerOpts, 'topCenter', this.getTopCenter());
            _.set(statementContainerOpts, 'bottomCenter', this.getBottomCenter());
            _.set(statementContainerOpts, 'width', this.width());
            _.set(statementContainerOpts, 'container', this.getContentArea().node());
            _.set(statementContainerOpts, 'toolPalette', this._toolPalette);
            _.set(statementContainerOpts, 'offset', _.get(args, 'offset', {top: 100, bottom: 100}));
            this._statementContainer = new StatementContainer(statementContainerOpts);
            this._statementContainer.render(this.diagramRenderingContext);
            this.getModel().accept(this);
            return this._statementContainer;
        };

        /**
         * @param {BallerinaStatementView} statement
         */
        WorkerDeclarationView.prototype.visitStatement = function (statement) {
            var args = {model: statement, container: this.getContentArea().node(), viewOptions: {},
                toolPalette: this._toolPalette, messageManager: this._messageManager, parent: this, isChildOfWorker: true};
            var statementView = this._statementContainer.renderStatement(statement, args);
            statementView.listenTo(this.getBoundingBox(), 'right-edge-moved', function (dx) {
                statementView.getBoundingBox().move(dx, 0);
            });
        };

        /**
         * Get the statement contianer
         * @return {StatementContainerView} _statementContainer - Statement Container of the worker declaration
         */
        WorkerDeclarationView.prototype.getStatementContainer = function () {
            return this._statementContainer;
        };

        /**
         * Child remove callback
         * @param {ASTNode} child - removed child
         */
        WorkerDeclarationView.prototype.childRemovedCallback = function (child) {
            var self = this;
            if (BallerinaASTFactory.isStatement(child)) {
                this.getStatementContainer().childStatementRemovedCallback(child);
            }
            // Remove the child from the diagram rendering context
            delete this.diagramRenderingContext.getViewModelMap()[child.id];
        };

        WorkerDeclarationView.prototype.canVisitWorkerDeclaration = function (worker) {
            return true;
        };

        return WorkerDeclarationView;
    });
