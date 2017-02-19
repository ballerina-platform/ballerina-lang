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
         * Worker invoke statement view.
         * @param args {*} constructor arguments
         * @class WorkerInvoke
         * @constructor
         * @extends SimpleStatementView
         */
        var WorkerInvoke = function (args) {
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
            this._startActionText = undefined;
            this._startRect = undefined;
            this._messageView = undefined;
        };

        WorkerInvoke.prototype = Object.create(SimpleStatementView.prototype);
        WorkerInvoke.prototype.constructor = WorkerInvoke;


        WorkerInvoke.prototype.init = function(){
            // TODO: Event name should modify in order to tally for both connector action and other dynamic arrow draws
            this.getModel().on("drawConnectionForAction",this.renderWorkerStart, this);
            Object.getPrototypeOf(this.constructor.prototype).init.call(this);
        };
        WorkerInvoke.prototype.setDiagramRenderingContext = function(context){
            this._diagramRenderingContext = context;
        };
        WorkerInvoke.prototype.getDiagramRenderingContext = function(){
            return this._diagramRenderingContext;
        };

        // TODO : Please revisit this method. Needs a refactor
        WorkerInvoke.prototype.draw = function(startPoint){
            var source = this.getModel().getSource();
            var destination = this.getModel().getDestination();
        };

        WorkerInvoke.prototype.setModel = function (model) {
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
        WorkerInvoke.prototype.render = function (renderingContext) {
            var self = this;
            var model = this.getModel();
            // Calling super class's render function.
            (this.__proto__.__proto__).render.call(this, renderingContext);
            // Setting display text.
            this.renderDisplayText(model.getInvokeStatement());

            this.renderProcessorConnectPoint(renderingContext);
            if (!_.isNil(this.getModel().getDestination())) {
                this.renderInvokeArrows();
            }

            // Creating property pane
            var editableProperty = {
                propertyType: "text",
                key: "Worker Invocation",
                model: model,
                getterMethod: model.getInvokeStatement,
                setterMethod: model.setInvokeStatement
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
                self.messageManager.setTypeBeingDragged(undefined);
            });

            this.getBoundingBox().on('top-edge-moved', function(dy){
                self.processorConnectPoint.attr('cy',  parseFloat(self.processorConnectPoint.attr('cy')) + dy);
            });

            this.listenTo(model, 'update-property-text', this.updateStatementText);
        };

        WorkerInvoke.prototype.renderArrows = function (context) {
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
                var destinationView = this.getDiagramRenderingContext().getViewOfModel(destination);

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

        WorkerInvoke.prototype.renderProcessorConnectPoint = function (renderingContext) {
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
        WorkerInvoke.prototype.removeArrows = function () {
            if (!_.isNil(this._arrowGroup) && !_.isNil(this._arrowGroup.node())) {
                d3.select(this._arrowGroup).node().remove();
            }
        };

        /**
         * Covert a point in user space Coordinates to client viewport Coordinates.
         * @param {Point} point a point in current user coordinate system
         */
        WorkerInvoke.prototype.toGlobalCoordinates = function (point) {
            var pt = this.processorConnectPoint.node().ownerSVGElement.createSVGPoint();
            pt.x = point.x();
            pt.y = point.y();
            pt = pt.matrixTransform(this.processorConnectPoint.node().getCTM());
            return new Point(pt.x, pt.y);
        };

        WorkerInvoke.prototype.updateStatementText = function (newStatementText, propertyKey) {
            this._model.setStatementString(newStatementText);
            var displayText = this._model.getStatementString();
            this.renderDisplayText(displayText);
        };

        WorkerInvoke.prototype.renderWorkerStart = function (startPoint, container) {
            log.debug("Render the worker start");
            var activatedWorkerTarget = this.messageManager.getActivatedDropTarget();
            if (BallerinaASTFactory.isWorkerDeclaration(activatedWorkerTarget)) {
                this.getModel().setDestination(activatedWorkerTarget);
                this.renderInvokeArrows();
                this.messageManager.reset();
            }
        };

        WorkerInvoke.prototype.renderInvokeArrows = function () {
            var self = this;
            var group = D3Utils.group(d3.select(this._container));
            var destinationView = this.getDiagramRenderingContext().getViewOfModel(this.getModel().getDestination());
            var startX = this.getBoundingBox().getRight();
            var startY = this.getBoundingBox().getTop() + this.getBoundingBox().h()/2;
            var endX = destinationView.getBoundingBox().getCenterX();
            var endY = startY;
            var startRectHeight = 30;
            var startRectWidth = 120;
            var messageStart = new Point(startX, startY);
            var messageEnd = new Point(endX - startRectWidth/2, endY);
            this._messageView = new MessageView({container: group.node(), start: messageStart, end: messageEnd});

            var newY = this.getBoundingBox().getBottom() + 30;
            var destinationStatementContainer = destinationView.getStatementContainer();
            if (destinationStatementContainer._managedStatements.length > 0) {
                // TODO: Use the getter method
                this.getDiagramRenderingContext().getViewOfModel(destinationStatementContainer._managedStatements[0]).getBoundingBox().y(newY);
                // Move the first inner drop zone down
                // TODO: use the getter method
                destinationStatementContainer._managedInnerDropzones[0].d3el.attr('y', newY - 30);
            }
            this.listenTo(destinationStatementContainer, 'statement-added', function (d) {
                if (_.isEqual(destinationStatementContainer._managedStatements.length, 1)) {
                    // TODO: Use the getter method
                    this.getDiagramRenderingContext().getViewOfModel(destinationStatementContainer._managedStatements[0]).getBoundingBox().y(newY);
                    // Move the first inner drop zone down
                    // TODO: use the getter method
                    destinationStatementContainer._managedInnerDropzones[0].d3el.attr('y', newY - 30);
                }
            });
            this._messageView.render();

            // Set the invoker for the destination model (worker)
            destinationView.getModel().setInvoker(this.getModel());

            // Draw the start rect on the worker
            this._startRect = D3Utils.centeredRect(new Point(endX, endY), startRectWidth, startRectHeight, 0, 0, group)
                .classed('statement-rect', true);
            this._startActionText = D3Utils.centeredText(new Point(endX, endY), 'Start', group)
                .classed('statement-text', true);
            this._startActionGroup = group;

            // Triggers when we add a new element above the worker invoke
            this.getBoundingBox().on('bottom-edge-moved', function (dy) {
                // If the bounding box of the invoker moved, we move the start action, arrow and the top most connector
                // Here we force fully move the top most statement of the destination
                self._startRect.attr('y', parseFloat(self._startRect.attr('y')) + dy);
                self._startActionText.attr('y', parseFloat(self._startActionText.attr('y')) + dy);
                if (destinationStatementContainer._managedStatements. length > 0) {
                    self.getDiagramRenderingContext().getViewOfModel(destinationStatementContainer._managedStatements[0]).getBoundingBox().move(0, dy);
                    destinationStatementContainer._managedInnerDropzones[0].d3el.attr('y',
                        parseFloat(destinationStatementContainer._managedInnerDropzones[0].d3el.attr('y')) + dy);
                    self._messageView.move(0, dy);
                }
            });

            // Triggers when we delete an element above the worker-invoke
            this.getBoundingBox().on('top-edge-moved', function (dy) {
                // If the bounding box of the invoker moved, we move the start action, arrow and the top most connector
                // Here we force fully move the top most statement of the destination
                self._startRect.attr('y', parseFloat(self._startRect.attr('y')) + dy);
                self._startActionText.attr('y', parseFloat(self._startActionText.attr('y')) + dy);
                if (destinationStatementContainer._managedStatements. length > 0) {
                    self.getDiagramRenderingContext().getViewOfModel(destinationStatementContainer._managedStatements[0]).getBoundingBox().move(0, dy);
                    destinationStatementContainer._managedInnerDropzones[0].d3el.attr('y',
                        parseFloat(destinationStatementContainer._managedInnerDropzones[0].d3el.attr('y')) + dy);
                    self._messageView.move(0, dy);
                }
            });

            // Listen to the destination's before-remove event and remove arrow and start box.
            this.getModel().getDestination().on('before-remove', function(){
                self._messageView.removeArrow();
                self._startRect.node().remove();
                self._startActionText.node().remove();
                self._startActionGroup.remove();
            });

            this.listenTo(destinationView.getStatementContainer().getBoundingBox(), 'center-x-moved', function (dx) {
                this._messageView.getEnd().move(dx, 0);
                self._startRect.attr('x', parseFloat(self._startRect.attr('x')) + dx);
                self._startActionText.attr('x', parseFloat(self._startActionText.attr('x')) + dx);
            });
        };

        WorkerInvoke.prototype.updateStatementText = function (newStatementText, propertyKey) {
            this._model.setInvokeStatement(newStatementText);
            var displayText = this._model.getInvokeStatement();
            this.renderDisplayText(displayText);
        };

        /**
         * Remove statement view callback
         */
        WorkerInvoke.prototype.onBeforeModelRemove = function () {
            d3.select("#_" +this.getModel().getID()).remove();
            this._startRect.node().remove();
            this._startActionText.node().remove();
            // resize the bounding box in order to the other objects to resize
            var gap = this.getParent().getStatementContainer().getInnerDropZoneHeight();
            this.getBoundingBox().move(0, -this.getBoundingBox().h() - gap).w(0);
            this._messageView.removeArrow();
        };

        return WorkerInvoke;

    });
