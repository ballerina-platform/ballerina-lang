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
define(['lodash', 'd3','log', './simple-statement-view', './../ast/action-invocation-expression', './point', 'd3utils', './../ast/ballerina-ast-factory', './message'],
    function (_, d3, log, SimpleStatementView, ActionInvocationExpression, Point, D3Utils, BallerinaASTFactory, MessageView) {

        /**
         * Worker receive statement view.
         * @param args {*} constructor arguments
         * @class WorkerReceive
         * @constructor
         * @extends SimpleStatementView
         */
        var WorkerReceive = function (args) {
            SimpleStatementView.call(this, args);
            this._connectorView = {};

            if (_.isNil(this._container)) {
                log.error("Container for action statement is undefined." + this._container);
                throw "Container for action statement is undefined." + this._container;
            }

            this.getBoundingBox().fromTopCenter(this._topCenter, this._viewOptions.width, this._viewOptions.height);
            this._processorConnector = undefined;
            this._processorConnector2 = undefined;
            this._forwardArrowHead = undefined;
            this._backArrowHead = undefined;
            this._arrowGroup = undefined;
            this._startRect = undefined;
            this._messageView = undefined;
            this._destinationReplyStatementView = undefined;

        };

        WorkerReceive.prototype = Object.create(SimpleStatementView.prototype);
        WorkerReceive.prototype.constructor = WorkerReceive;


        WorkerReceive.prototype.init = function(){
            // TODO: Event name should modify in order to tally for both connector action and other dynamic arrow draws
            this.getModel().on("drawConnectionForAction",this.renderWorkerReceive, this);
            Object.getPrototypeOf(this.constructor.prototype).init.call(this);
        };
        WorkerReceive.prototype.setDiagramRenderingContext = function(context){
            this._diagramRenderingContext = context;
        };
        WorkerReceive.prototype.getDiagramRenderingContext = function(){
            return this._diagramRenderingContext;
        };

        // TODO : Please revisit this method. Needs a refactor
        WorkerReceive.prototype.draw = function(startPoint){
            var source = this.getModel().getSource();
            var destination = this.getModel().getDestination();
        };

        WorkerReceive.prototype.setModel = function (model) {
            var actionInvocationModel = this._model.getChildren()[1].getChildren()[0];
            if (!_.isNil(model) && model instanceof ActionInvocationExpression) {
                actionInvocationModel = model;
            } else {
                log.error("Action statement definition is undefined or is of different type." + model);
                throw "Action statement definition is undefined or is of different type." + model;
            }
        };

        /**
         * Rendering the view for get-Action statement.
         * @returns {group} The svg group which contains the elements of the action statement view.
         */
        WorkerReceive.prototype.render = function (renderingContext) {
            var self = this;
            var model = this.getModel();
            // Calling super class's render function.
            (this.__proto__.__proto__).render.call(this, renderingContext);
            // Setting display text.
            this.renderDisplayText(model.getReceiveStatement());

            this.renderProcessorConnectPoint(renderingContext);

            if (!_.isNil(this.getModel().getDestination())) {
                this.renderReceiveArrows();
            }

            // Creating property pane
            var editableProperty = {
                propertyType: "text",
                key: "Worker Invocation",
                model: model,
                getterMethod: model.getReceiveStatement,
                setterMethod: model.setReceiveStatement
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
            this.processorConnectPoint.on("mousedown", function () {
                d3.event.preventDefault();
                d3.event.stopPropagation();
                var x =  parseFloat(self.processorConnectPoint.attr('cx'));
                var y =  parseFloat(self.processorConnectPoint.attr('cy'));
                var sourcePoint = self.toGlobalCoordinates(new Point(x, y));

                self.messageManager.startDrawMessage(self, self.getModel(), sourcePoint,
                    self.toGlobalCoordinates(new Point(x, y)));
                self.messageManager.setTypeBeingDragged(true);
            });
            this.processorConnectPoint.on("mouseover", function () {
                self.processorConnectPoint
                    .style("fill", "#444")
                    .style("fill-opacity", 0.5)
                    .style("cursor", 'url(images/BlackHandwriting.cur), pointer');
            });
            this.processorConnectPoint.on("mouseout", function () {
                self.processorConnectPoint.style("fill-opacity", 0.01);
                self.messageManager.setTypeBeingDragged(false);
            });

            this.getBoundingBox().on('top-edge-moved', function(dy){
                self.processorConnectPoint.attr('cy',  parseFloat(self.processorConnectPoint.attr('cy')) + dy);
            });

            this.listenTo(model, 'update-property-text', this.updateStatementText);
        };

        WorkerReceive.prototype.renderArrows = function (context) {
            this.setDiagramRenderingContext(context);

            var destination = this.getModel().getDestination();
            if(!_.isUndefined(destination)) {
                var parent = this.getStatementGroup();
                this._arrowGroup = D3Utils.group(parent).attr("transform", "translate(0,0)");
                var width = this.getBoundingBox().w();
                var height = this.getBoundingBox().h();
                var x = this.getBoundingBox().getLeft();
                var y = this.getBoundingBox().getTop();
                var sourcePointX = x + width;
                var sourcePointY = y + height / 2;

                var startPoint = new Point(sourcePointX, sourcePointY);
                var connectorCenterPointX = this.connector.getMiddleLineCenter().x();
                var connectorCenterPointY = this.connector.getMiddleLineCenter().y();
                var startX = Math.round(startPoint.x());
                this._processorConnector = D3Utils.line(Math.round(startPoint.x()), Math.round(startPoint.y()), Math.round(connectorCenterPointX),
                    Math.round(startPoint.y()), this._arrowGroup).classed("action-line", true);
                this._forwardArrowHead = D3Utils.inputTriangle(Math.round(connectorCenterPointX) - 5, Math.round(startPoint.y()), this._arrowGroup).classed("action-arrow", true);

                this.getBoundingBox().on('moved', function (offset) {
                    var transformX = this._arrowGroup.node().transform.baseVal.consolidate().matrix.e;
                    var transformY = this._arrowGroup.node().transform.baseVal.consolidate().matrix.f;
                    this._arrowGroup.node().transform.baseVal.getItem(0).setTranslate(transformX + offset.dx, transformY + offset.dy);
                }, this);

                this.processorConnectPoint.style("display", "none");

                var arrowHeadEnd = D3Utils.circle(Math.round(connectorCenterPointX) - 5, Math.round(startPoint.y()), 10, parent);
                arrowHeadEnd
                    .attr("fill-opacity", 0.01)
                    .style("fill", "#444");
            }
        };

        WorkerReceive.prototype.renderProcessorConnectPoint = function (renderingContext) {
            var boundingBox = this.getBoundingBox();
            var width = boundingBox.w();
            var height = boundingBox.h();
            var x = boundingBox.getLeft();
            var y = boundingBox.getTop();

            var processorConnectPoint = D3Utils.circle((x + width), ((y + height / 2)), 10, this.getStatementGroup());
            processorConnectPoint.attr("fill-opacity", 0.01);
            this.processorConnectPoint = processorConnectPoint;
        };

        /**
         * Remove the forward and the backward arrow heads
         */
        WorkerReceive.prototype.removeArrows = function () {
            if (!_.isNil(this._arrowGroup) && !_.isNil(this._arrowGroup.node())) {
                d3.select(this._arrowGroup).node().remove();
            }
        };

        /**
         * Covert a point in user space Coordinates to client viewport Coordinates.
         * @param {Point} point a point in current user coordinate system
         */
        WorkerReceive.prototype.toGlobalCoordinates = function (point) {
            var pt = this.processorConnectPoint.node().ownerSVGElement.createSVGPoint();
            pt.x = point.x();
            pt.y = point.y();
            pt = pt.matrixTransform(this.processorConnectPoint.node().getCTM());
            return new Point(pt.x, pt.y);
        };

        /**
         * Remove statement view callback
         */
        WorkerReceive.prototype.onBeforeModelRemove = function () {
            this.stopListening(this.getBoundingBox());
            d3.select("#_" +this._model.id).remove();
            this.removeArrows();
            // resize the bounding box in order to the other objects to resize
            var moveOffset = -this.getBoundingBox().h() - 30;
            this.getBoundingBox().move(0, moveOffset);
        };

        WorkerReceive.prototype.updateStatementText = function (newStatementText, propertyKey) {
            this._model.setStatementString(newStatementText);
            var displayText = this._model.getStatementString();
            this.renderDisplayText(displayText);
        };

        WorkerReceive.prototype.renderWorkerReceive = function (startPoint, container) {
            log.debug("Render the worker start");
            var activatedWorkerTarget = this.messageManager.getActivatedDropTarget();
            if (BallerinaASTFactory.isWorkerDeclaration(activatedWorkerTarget)) {
                this.getModel().setDestination(activatedWorkerTarget);
                this.renderReceiveArrows();
                this.messageManager.reset();
            }
        };

        WorkerReceive.prototype.renderReceiveArrows = function () {
            this._arrowGroup = D3Utils.group(d3.select(this._container));
            var destinationView = this.getDiagramRenderingContext().getViewOfModel(this.getModel().getDestination());
            var destinationStatementContainer = destinationView.getStatementContainer();
            var startX = destinationView.getBoundingBox().getLeft();
            var endX = this.getBoundingBox().getRight();
            var endY = 0;
            var startY = 0;
            var self = this;

            // Get the reply statement of the destination worker
            var destinationReplyStatement = _.find(destinationStatementContainer._managedStatements, function (node) {
                return BallerinaASTFactory.isReplyStatement(node);
            });
            var workerInvokeStatement = _.find(this.getParent().getModel().getChildren(), function (child) {
                return BallerinaASTFactory.isWorkerInvokeStatement(child);
            });
            this._destinationReplyStatementView = !_.isNil(destinationReplyStatement) ?
                this.getDiagramRenderingContext().getViewOfModel(destinationReplyStatement) : undefined;

            // We have added a reply statement to the worker and the invoker can receive a valid response from the worker
            // We do not allow to add the receive unless we have added a reply to the particular worker
            if (!_.isNil(this._destinationReplyStatementView) && !_.isNil(workerInvokeStatement)) {
                if (this.getBoundingBox().getBottom() > this._destinationReplyStatementView.getBoundingBox().getBottom()) {
                    // Worker receive statement is located bellow the reply statement.
                    // We need to move the reply statement down
                    startY = this.getBoundingBox().getTop() + this.getBoundingBox().h()/2;
                    this._destinationReplyStatementView.getBoundingBox().y(this.getBoundingBox().getTop());
                } else {
                    // Worker receive statement is located above the reply statement.
                    // We need to move the worker receive statement down
                    startY =  this._destinationReplyStatementView.getBoundingBox().getTop() + this.getBoundingBox().h()/2;
                    this.getBoundingBox().y(this._destinationReplyStatementView.getBoundingBox().getTop())
                }
                endY = startY;
                var messageStart = new Point(startX, startY);
                var messageEnd = new Point(endX, endY);
                this._messageView = new MessageView({container: this._arrowGroup.node(), start: messageStart, end: messageEnd, isInputArrow: false});
                this._messageView.render();

                // Set the reply receiver for the destination
                destinationView.getModel().setReplyReceiver(this.getModel());

                this.listenTo(destinationView.getStatementContainer().getBoundingBox(), 'center-x-moved', function (dx) {
                    this._messageView.getStart().move(dx, 0);
                });
            }

            /**
             * When the top edge move event triggered
             * @override
             */
            WorkerReceive.prototype.onTopEdgeMovedTrigger = function (dy) {
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
                        } else {
                            debugger;
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
            WorkerReceive.prototype.canMoveUp = function (dy) {
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
            WorkerReceive.prototype.onMoveInitiatedByReply = function (dy) {
                this.stopListening(this.getBoundingBox(), 'top-edge-moved');
                this.getBoundingBox().move(0, dy);
                this._messageView.move(0, dy);
                this.getSvgRect().attr('y', parseFloat(this.getSvgRect().attr('y')) + dy);
                this.getSvgText().attr('y', parseFloat(this.getSvgText().attr('y')) + dy);
                this.listenTo(this.getBoundingBox(), 'top-edge-moved', function (dy) {
                    this.onTopEdgeMovedTrigger(dy);
                });
            };
        };

        WorkerReceive.prototype.updateStatementText = function (newStatementText, propertyKey) {
            this._model.setReceiveStatement(newStatementText);
            var displayText = this._model.getReceiveStatement();
            this.renderDisplayText(displayText);
        };

        return WorkerReceive;

    });
