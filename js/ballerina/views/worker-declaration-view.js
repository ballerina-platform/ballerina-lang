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

define(['lodash', 'jquery', './ballerina-view', './../ast/worker-declaration', 'log', 'd3utils', './life-line', 'd3'],
    function (_, $, BallerinaView, WorkerDeclaration, log, D3Utils, LifeLine, d3) {

        /**
         * The view to represent a worker declaration which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {WorkerDeclaration} args.model - The worker declaration model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var WorkerDeclarationView = function (args) {
            this._model = _.get(args, "model");
            this._container = _.get(args, "container");
            this._viewOptions = _.get(args, "viewOptions", {});
            this._workerLifeLine = undefined;

            if (_.isNil(this._model) || !(this._model instanceof WorkerDeclaration)) {
                log.error("Worker declaration definition undefined or is of different type." + this._model);
                throw "Worker declaration definition undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for worker declaration is undefined." + this._container);
                throw "Container for worker declaration is undefined." + this._container;
            }

            BallerinaView.call(this);
        };

        WorkerDeclarationView.prototype = Object.create(BallerinaView.prototype);
        WorkerDeclarationView.prototype.constructor = WorkerDeclarationView;

        WorkerDeclarationView.prototype.setModel = function (model) {
            if (!_.isNil(model) && this._model instanceof WorkerDeclaration) {
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
         * Rendering the view of the worker declaration.
         * @returns {Object} - The svg group which the worker declaration view resides in.
         */
        WorkerDeclarationView.prototype.render = function () {
            var centerX = 0;
            var centerY = 25;
            var lifeLineOffset = 50;

            // CenterX calculated with respect to the already drawn workers, connectors or default worker
            if (this.getParent().getWorkerAndConnectorViews().length > 0) {
                var lastDrawnLifeLine = this.getParent().getWorkerAndConnectorViews()[this.getParent().
                getWorkerAndConnectorViews().length - 1].getWorkerLifeLine();
                centerX = lastDrawnLifeLine.getViewOptions().centerPoint.x + lastDrawnLifeLine
                        .getViewOptions().polygon.width + lifeLineOffset;
            } else if (!_.isUndefined(this.getParent().getDefaultWorkerLifeLine())) {
                centerX = this.getParent().getDefaultWorkerLifeLine().getViewOptions().centerPoint.x + this.getParent()
                        .getDefaultWorkerLifeLine().getViewOptions().polygon.width + lifeLineOffset;
            }
            var defaultWorkerOptions = {
                editable: true,
                centerPoint: {
                    x: centerX,
                    y: centerY
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
                    value: "Worker",
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

            this._workerLifeLine = new LifeLine(containerGroup, defaultWorkerOptions);
            this._workerLifeLine.render();
            this.getModel().accept(this);
            return containerGroup;
        };

        /**
         * @inheritDoc
         */
        WorkerDeclarationView.prototype.setWidth = function (newWidth) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        WorkerDeclarationView.prototype.setHeight = function (newHeight) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        WorkerDeclarationView.prototype.setXPosition = function (xPosition) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        WorkerDeclarationView.prototype.setYPosition = function (yPosition) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        WorkerDeclarationView.prototype.getWidth = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        WorkerDeclarationView.prototype.getHeight = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        WorkerDeclarationView.prototype.getXPosition = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        WorkerDeclarationView.prototype.getYPosition = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         * return {_workerLifeLine}
         */
        WorkerDeclarationView.prototype.getWorkerLifeLine = function () {
            return this._workerLifeLine;
        };

        return WorkerDeclarationView;
    });