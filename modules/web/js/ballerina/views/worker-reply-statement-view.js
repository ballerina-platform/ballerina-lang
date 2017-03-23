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
define(['lodash', 'd3','log', './simple-statement-view', './point', 'd3utils', './../ast/ballerina-ast-factory',
        './message'],
    function (_, d3, log, SimpleStatementView, Point, D3Utils, BallerinaASTFactory, MessageView) {

        /**
         * Worker receive statement view.
         * @param args {*} constructor arguments
         * @class WorkerReplyStatementView
         * @constructor
         * @extends SimpleStatementView
         */
        var WorkerReplyStatementView = function (args) {
            SimpleStatementView.call(this, args);
            this._connectorView = {};

            if (_.isNil(this._container)) {
                log.error("Container for action statement is undefined." + this._container);
                throw "Container for action statement is undefined." + this._container;
            }

            this.getBoundingBox().fromTopCenter(this._topCenter, this._viewOptions.width, this._viewOptions.height);
            this._processorConnector = undefined;
            this._processorConnector2 = undefined;
            this._workerReplyStartPoint = undefined;
            this._forwardArrowHead = undefined;
            this._backArrowHead = undefined;
            this._arrowGroup = undefined;
            this._startRect = undefined;
            this._messageView = undefined;
            this._destinationReplyStatementView = undefined;

        };

        WorkerReplyStatementView.prototype = Object.create(SimpleStatementView.prototype);
        WorkerReplyStatementView.prototype.constructor = WorkerReplyStatementView;


        WorkerReplyStatementView.prototype.init = function(){
            // TODO: Event name should modify in order to tally for both connector action and other dynamic arrow draws
            this.getModel().on("drawConnectionForAction",this.renderWorkerReply, this);
            Object.getPrototypeOf(this.constructor.prototype).init.call(this);
        };
        WorkerReplyStatementView.prototype.setDiagramRenderingContext = function(context){
            this._diagramRenderingContext = context;
        };
        WorkerReplyStatementView.prototype.getDiagramRenderingContext = function(){
            return this._diagramRenderingContext;
        };

        // TODO : Please revisit this method. Needs a refactor
        WorkerReplyStatementView.prototype.draw = function(startPoint){
            var source = this.getModel().getSource();
            var destination = this.getModel().getDestination();
        };

        WorkerReplyStatementView.prototype.setModel = function (model) {
            var actionInvocationModel = this._model.getChildren()[1].getChildren()[0];
            if (!_.isNil(model) && model instanceof ActionInvocationExpression) {
                actionInvocationModel = model;
            } else {
                log.error("Action statement definition is undefined or is of different type." + model);
                throw "Action statement definition is undefined or is of different type." + model;
            }
        };

        /**
         * Rendering the view for worker-receive statement.
         * @returns {group} The svg group which contains the elements of the action statement view.
         */
        WorkerReplyStatementView.prototype.render = function (renderingContext) {
            var self = this;
            var model = this.getModel();
            // Calling super class's render function.
            (this.__proto__.__proto__).render.call(this, renderingContext);
            // Setting display text.
            this.renderDisplayText(model.getReplyStatement());

            this.renderProcessorConnectPoint(renderingContext);

            if (!_.isNil(this.getModel().getDestination())) {
                this.renderReceiveArrows();
            }

            // Creating property pane
            var editableProperty = {
                propertyType: "text",
                key: "Worker Invocation",
                model: model,
                getterMethod: model.getReplyStatement,
                setterMethod: model.setReplyStatement
            };

            this._createPropertyPane({
                model: model,
                statementGroup: this.getStatementGroup(),
                editableProperties: editableProperty
            });

            this._createDebugIndicator({
                statementGroup: this.getStatementGroup()
            });

            this.listenTo(model, 'update-property-text', this.updateStatementText);

            // mouse events for 'processorConnectPoint'
            this.getWorkerReplyStartPoint().on("mousedown", function () {
                d3.event.preventDefault();
                d3.event.stopPropagation();
                var x =  parseFloat(self.getWorkerReplyStartPoint().attr('cx'));
                var y =  parseFloat(self.getWorkerReplyStartPoint().attr('cy'));
                var sourcePoint = self.toGlobalCoordinates(new Point(x, y));

                self.messageManager.startDrawMessage(self, self.getModel(), sourcePoint,
                    self.toGlobalCoordinates(new Point(x, y)));
                self.messageManager.setTypeBeingDragged(true);
            });
            this.getWorkerReplyStartPoint().on("mouseover", function () {
                self.getWorkerReplyStartPoint()
                    .style("fill", "#444")
                    .style("fill-opacity", 0.5)
                    .style("cursor", 'url(images/BlackHandwriting.cur), pointer');
            });
            this.getWorkerReplyStartPoint().on("mouseout", function () {
                self.getWorkerReplyStartPoint().style("fill-opacity", 0.01);
                self.messageManager.setTypeBeingDragged(undefined);
            });

            this.getBoundingBox().on('top-edge-moved', function(dy){
                self.getWorkerReplyStartPoint().attr('cy',  parseFloat(self.getWorkerReplyStartPoint().attr('cy')) + dy);
            });

            this.listenTo(this.getBoundingBox(), 'right-edge-moved', function (dx) {
                // Move the invoke start point
                self.getWorkerReplyStartPoint().attr('cx', parseFloat(self.getWorkerReplyStartPoint().attr('cx')) + dx);
            });
        };

        WorkerReplyStatementView.prototype.renderProcessorConnectPoint = function (renderingContext) {
            var boundingBox = this.getBoundingBox();
            var width = boundingBox.w();
            var height = boundingBox.h();
            var x = boundingBox.getLeft();
            var y = boundingBox.getTop();

            var workerReplyStartPoint = D3Utils.circle((x + width), ((y + height / 2)), 10, this.getStatementGroup());
            workerReplyStartPoint.attr("fill-opacity", 0.01);
            this._workerReplyStartPoint = workerReplyStartPoint;
        };

        /**
         * Remove the forward and the backward arrow heads
         */
        WorkerReplyStatementView.prototype.removeArrows = function () {
            if (!_.isNil(this._arrowGroup) && !_.isNil(this._arrowGroup.node())) {
                d3.select(this._arrowGroup).node().remove();
            }
        };

        /**
         * Covert a point in user space Coordinates to client viewport Coordinates.
         * @param {Point} point a point in current user coordinate system
         */
        WorkerReplyStatementView.prototype.toGlobalCoordinates = function (point) {
            var pt = this.getWorkerReplyStartPoint().node().ownerSVGElement.createSVGPoint();
            pt.x = point.x();
            pt.y = point.y();
            pt = pt.matrixTransform(this.getWorkerReplyStartPoint().node().getCTM());
            return new Point(pt.x, pt.y);
        };

        /**
         * Remove statement view callback
         */
        WorkerReplyStatementView.prototype.onBeforeModelRemove = function () {
            this.stopListening(this.getBoundingBox());
            d3.select("#_" +this._model.id).remove();
            this.removeArrows();
            // resize the bounding box in order to the other objects to resize
            var moveOffset = -this.getBoundingBox().h() - 30;
            this.getBoundingBox().move(0, moveOffset);
        };

        WorkerReplyStatementView.prototype.updateStatementText = function (newStatementText, propertyKey) {
            this._model.setReplyStatement(newStatementText);
            var displayText = this._model.getReplyStatement();
            this.renderDisplayText(displayText);
        };

        WorkerReplyStatementView.prototype.renderWorkerReply = function (startPoint, container) {
            log.debug("Render the worker start");
            var activatedWorkerTarget = this.messageManager.getActivatedDropTarget();
            if (BallerinaASTFactory.isWorkerDeclaration(activatedWorkerTarget)) {
                this.getModel().setDestination(activatedWorkerTarget);
                this.renderReceiveArrows();
                this.messageManager.reset();
            }
        };

        WorkerReplyStatementView.prototype.renderReceiveArrows = function () {
            this._arrowGroup = D3Utils.group(d3.select(this._container));
            var destinationView = this.getDiagramRenderingContext().getViewOfModel(this.getModel().getDestination());
            var destinationStatementContainer = destinationView.getStatementContainer();
            var startX = 0;
            var endX = this.getBoundingBox().getRight();
            var endY = 0;
            var startY = 0;
            var self = this;

            // Get the reply statement of the destination worker
            var destinationReplyStatement = _.find(destinationStatementContainer._managedStatements, function (node) {
                return BallerinaASTFactory.isReplyStatement(node);
            });
            var workerInvocationStatement = _.find(this.getParent().getModel().getChildren(), function (child) {
                return BallerinaASTFactory.isWorkerInvocationStatement(child);
            });
            this._destinationReplyStatementView = !_.isNil(destinationReplyStatement) ?
                this.getDiagramRenderingContext().getViewOfModel(destinationReplyStatement) : undefined;

            // We have added a reply statement to the worker and the invoker can receive a valid response from the worker
            // We do not allow to add the receive unless we have added a reply to the particular worker
            if (!_.isNil(this._destinationReplyStatementView) && !_.isNil(workerInvocationStatement)) {
                if (this.getBoundingBox().getBottom() > this._destinationReplyStatementView.getBoundingBox().getBottom()) {
                    // Worker receive statement is located bellow the reply statement.
                    // We need to move the reply statement down
                    startY = this.getBoundingBox().getTop() + this.getBoundingBox().h()/2;
                    this._destinationReplyStatementView.getBoundingBox().y(this.getBoundingBox().getTop());
                } else {
                    // Worker receive statement is located above the reply statement.
                    // We need to move the worker receive statement down
                    startY =  this._destinationReplyStatementView.getBoundingBox().getTop() + this.getBoundingBox().h()/2;
                    this.getBoundingBox().y(this._destinationReplyStatementView.getBoundingBox().getTop());
                }
                startX = this._destinationReplyStatementView.getBoundingBox().getLeft();
                endY = startY;
                var messageStart = new Point(startX, startY);
                var messageEnd = new Point(endX, endY);
                this._messageView = new MessageView({container: this._arrowGroup.node(), start: messageStart, end: messageEnd, isInputArrow: false});
                this._messageView.render();

                // Set the reply receiver for the destination
                destinationView.getModel().setReplyReceiver(this.getModel());
                this.listenTo(this.getBoundingBox(), 'right-edge-moved', this.moveArrowEndHorizontalCallback, this);
                this.listenTo(this._destinationReplyStatementView.getBoundingBox(), 'left-edge-moved',
                    this.moveArrowStartHorizontalCallback, this);
            }
        };

        /**
         * When the top edge move event triggered
         * @override
         */
        WorkerReplyStatementView.prototype.onTopEdgeMovedTrigger = function (dy) {
            var self = this;

            if (_.isNil(self._messageView)) {
                // We haven't drawn an arrow yet.
                self.getSvgRect().attr('y', parseFloat(self.getSvgRect().attr('y')) + dy);
                self.getSvgText().attr('y', parseFloat(self.getSvgText().attr('y')) + dy);
            } else {
                // There is already drawn arrow between the receiver and the reply statement
                if (dy > 0) {
                    // Moving the statement down
                    if (!_.isNil(self._messageView)) {
                        self._messageView.move(0, dy);
                    }
                    self.getSvgRect().attr('y', parseFloat(self.getSvgRect().attr('y')) + dy);
                    self.getSvgText().attr('y', parseFloat(self.getSvgText().attr('y')) + dy);
                    self._destinationReplyStatementView.onMoveInitiatedByReplyReceiver(dy);
                } else if (dy < 0) {
                    // Moving the statement up
                    self.stopListening(self.getBoundingBox(), 'top-edge-moved');
                    if (self._destinationReplyStatementView.canMoveUp(dy)) {
                        self._messageView.move(0, dy);
                        self.getSvgRect().attr('y', parseFloat(self.getSvgRect().attr('y')) + dy);
                        self.getSvgText().attr('y', parseFloat(self.getSvgText().attr('y')) + dy);
                        self._destinationReplyStatementView.onMoveInitiatedByReplyReceiver(dy);
                    } else {
                        self.getBoundingBox().move(0, -dy);
                    }
                    self.listenTo(self.getBoundingBox(), 'top-edge-moved', function (dy) {
                        self.onTopEdgeMovedTrigger(dy);
                    });
                }
            }
        };

        /**
         * Check whether the reply receiver statement can move upwards
         * @param {number} dy - delta y distance
         * @return {boolean}
         */
        WorkerReplyStatementView.prototype.canMoveUp = function (dy) {
            var self = this;
            var bBox = this.getBoundingBox();
            var previousStatement = undefined;
            var previousStatementView = undefined;
            var statementContainer = this.getParent().getStatementContainer();
            var innerDropZoneHeight = 30;
            var currentIndex = _.findIndex(statementContainer.getManagedStatements(), function (stmt) {
                return stmt.id === self.getModel().id;
            });
            var newBBoxTop = bBox.getTop() + dy;

            if (currentIndex > 0) {
                previousStatement = statementContainer.getManagedStatements()[currentIndex - 1];
                previousStatementView = self.getDiagramRenderingContext().getViewOfModel(previousStatement);
                return newBBoxTop >= previousStatementView.getBoundingBox().getBottom() + innerDropZoneHeight;
            }
        };

        /**
         * When the reply receive statement view move is initiated by the reply receiver view
         * @param {number} dy delta y distance
         */
        WorkerReplyStatementView.prototype.onMoveInitiatedByReply = function (dy) {
            this.stopListening(this.getBoundingBox(), 'top-edge-moved');
            this.getBoundingBox().move(0, dy);
            this._messageView.move(0, dy);
            this.getSvgRect().attr('y', parseFloat(this.getSvgRect().attr('y')) + dy);
            this.getSvgText().attr('y', parseFloat(this.getSvgText().attr('y')) + dy);
            this.listenTo(this.getBoundingBox(), 'top-edge-moved', function (dy) {
                this.onTopEdgeMovedTrigger(dy);
            });
        };

        /**
         * Get the worker reply start point
         * @return {SVGCircle}
         */
        WorkerReplyStatementView.prototype.getWorkerReplyStartPoint = function () {
            return this._workerReplyStartPoint;
        };

        /**
         * Move the arrow start horizontally
         * @param {number} dx
         */
        WorkerReplyStatementView.prototype.moveArrowStartHorizontalCallback = function (dx) {
            var self = this;
            // Move the arrow start point
            if (!_.isNil(self.getMessageView())) {
                self.getMessageView().getStart().move(dx, 0);
            }
        };

        /**
         * Move the arrow end horizontally
         * @param {number} dx
         */
        WorkerReplyStatementView.prototype.moveArrowEndHorizontalCallback = function (dx) {
            var self = this;
            // Move the arrow end point
            if (!_.isNil(self.getMessageView())) {
                self.getMessageView().getEnd().x(self.getBoundingBox().getRight());
            }
        };

        /**
         * Get the MessageView
         * @return {MessageView} - messageView
         */
        WorkerReplyStatementView.prototype.getMessageView = function () {
            return this._messageView;
        };

        return WorkerReplyStatementView;

    });
