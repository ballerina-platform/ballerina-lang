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
define(['lodash', 'd3','log', './simple-statement-view', './../ast/action-invocation-expression', './point', 'd3utils', './../ast/ballerina-ast-factory'],
    function (_, d3, log, SimpleStatementView, ActionInvocationExpression, Point, D3Utils, BallerinaASTFactory) {

        /**
         * Action invocation statement view.
         * @param args {*} constructor arguments
         * @class ActionInvocationStatementView
         * @constructor
         * @extends SimpleStatementView
         */
        var ActionInvocationStatementView = function (args) {
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

        };

        ActionInvocationStatementView.prototype = Object.create(SimpleStatementView.prototype);
        ActionInvocationStatementView.prototype.constructor = ActionInvocationStatementView;


        ActionInvocationStatementView.prototype.init = function(){
            this.getModel().on("drawConnectionForAction", this.drawActionConnections, this);
            Object.getPrototypeOf(this.constructor.prototype).init.call(this);
        };
        ActionInvocationStatementView.prototype.setDiagramRenderingContext = function(context){
            this._diagramRenderingContext = context;
        };
        ActionInvocationStatementView.prototype.getDiagramRenderingContext = function(){
            return this._diagramRenderingContext;
        };

        ActionInvocationStatementView.prototype.drawActionConnections = function(startPoint){
            var actionInvocationModel = this.getActionInvocationExpressionModel();
            if(this.isAtValidDropTarget()){
                this.updateActivatedTarget(this.messageManager.getActivatedDropTarget(), actionInvocationModel);
            }

            if(!_.isNil(actionInvocationModel.getConnector())) {
                this.renderDisplayText(this.getModel().getStatementString());
                this.renderArrows(this.getDiagramRenderingContext());
            }
        };

        ActionInvocationStatementView.prototype.setConnectorView = function(view){
            this._connectorView = view;
        };
        ActionInvocationStatementView.prototype.getConnectorView = function(){
            return this._connectorView;
        };


        /**
         * Rendering the view for get-Action statement.
         * @returns {group} The svg group which contains the elements of the action statement view.
         */
        ActionInvocationStatementView.prototype.render = function (renderingContext) {
            var self = this;
            var model = this.getModel();
            // Calling super class's render function.
            (this.__proto__.__proto__).render.call(this, renderingContext);
            // Disable the event registered at the super class for the top-edge-moved, since we override the event action
            // TODO: we need to properly handle this at the super class level
            this.stopListening(this.getBoundingBox(), 'top-edge-moved');
            var actionInvocationExpressionModel = this.getActionInvocationExpressionModel();
            var connectorModel = actionInvocationExpressionModel.getConnector();
            model.accept(this);
            if (!_.isUndefined(connectorModel)) {
                this.connector = this.getDiagramRenderingContext().getViewOfModel(connectorModel);
            }
            else {
                var siblingConnectors = this._parent._model.children;
                _.some(siblingConnectors, function (key, i) {
                    if (BallerinaASTFactory.isConnectorDeclaration(siblingConnectors[i])) {
                        var connectorReference = siblingConnectors[i];
                        var actionInvocationExpressionModel = self.getActionInvocationExpressionModel();
                        actionInvocationExpressionModel.setConnector(connectorReference);
                        self.messageManager.setMessageSource(model);
                        self.updateActivatedTarget(connectorReference, actionInvocationExpressionModel);
                        return true;
                    }
                });
            }

            // Setting display text.
            this.renderDisplayText(model.getStatementString());
            this.renderProcessorConnectPoint(renderingContext);
            this.renderArrows(renderingContext);

            // Creating property pane
            var editableProperty = {
                    propertyType: "text",
                    key: "Action Invocation",
                    model: model,
                    getterMethod: model.getStatementString,
                    setterMethod: model.setStatementString
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

                self.messageManager.startDrawMessage(self, model, sourcePoint);
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
            });

            this.getBoundingBox().on('top-edge-moved', function(dy){
                self.processorConnectPoint.attr('cy',  parseFloat(self.processorConnectPoint.attr('cy')) + dy);
            });
        };

        ActionInvocationStatementView.prototype.renderArrows = function (renderingContext) {
            this.setDiagramRenderingContext(renderingContext);
            var actionInvocationExpressionModel = this.getActionInvocationExpressionModel();
            var connectorModel = actionInvocationExpressionModel.getConnector();

            if(!_.isUndefined(connectorModel)) {
                this.connector = this.getDiagramRenderingContext().getViewOfModel(connectorModel);
            }

            if(!_.isUndefined(this.connector)) {
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
                this._forwardArrowHead.attr("transform", "translate(0,0)");
                this._processorConnector2 = D3Utils.line(Math.round(startPoint.x()), Math.round(startPoint.y()) + 8, Math.round(connectorCenterPointX),
                    Math.round(startPoint.y()) + 8, this._arrowGroup).classed("action-dash-line", true);
                this._backArrowHead = D3Utils.outputTriangle(Math.round(startPoint.x()), Math.round(startPoint.y()) + 8, this._arrowGroup).classed("action-arrow", true);
                this._backArrowHead.attr("transform", "translate(0,0)");

                this.renderProcessorConnectEndPoint(renderingContext);

                this.listenTo(this.getBoundingBox(), 'top-edge-moved', function (offset) {
                    this.getSvgRect().attr('y', parseFloat(this.getSvgRect().attr('y')) + offset);
                    this.getSvgText().attr('y', parseFloat(this.getSvgText().attr('y')) + offset);
                    var currentY1ProcessorConnector = this._processorConnector.attr('y1');
                    var currentY1ProcessorConnector2 = this._processorConnector2.attr('y1');
                    var currentY2ProcessorConnector = this._processorConnector.attr('y2');
                    var currentY2ProcessorConnector2 = this._processorConnector2.attr('y2');
                    var forwardArrowTransformX = this._forwardArrowHead.node().transform.baseVal.consolidate().matrix.e;
                    var forwardArrowTransformY = this._forwardArrowHead.node().transform.baseVal.consolidate().matrix.f;
                    var backwardArrowTransformX = this._backArrowHead.node().transform.baseVal.consolidate().matrix.e;
                    var backwardArrowTransformY = this._backArrowHead.node().transform.baseVal.consolidate().matrix.f;

                    this._processorConnector.attr('y1', parseFloat(currentY1ProcessorConnector) + offset);
                    this._processorConnector2.attr('y1', parseFloat(currentY1ProcessorConnector2) + offset);
                    this._processorConnector.attr('y2', parseFloat(currentY2ProcessorConnector) + offset);
                    this._processorConnector2.attr('y2', parseFloat(currentY2ProcessorConnector2) + offset);
                    this._forwardArrowHead.node().transform.baseVal.getItem(0).setTranslate(forwardArrowTransformX + 0, forwardArrowTransformY + offset);
                    this._backArrowHead.node().transform.baseVal.getItem(0).setTranslate(backwardArrowTransformX + 0, backwardArrowTransformY + offset);

                    d3.select(this.processorConnectPoint.node()).attr("transform", "translate(" + backwardArrowTransformX + "," + (backwardArrowTransformY + offset) + ")");
                    d3.select(this.processorConnectEndPoint.node()).attr("transform", "translate(" + forwardArrowTransformX + "," + (forwardArrowTransformY + offset) + ")");
                }, this);

                this.getBoundingBox().on('right-edge-moved', function (offset) {
                    var currentX1ProcessorConnector = this._processorConnector.attr('x1');
                    var currentX1ProcessorConnector2 = this._processorConnector2.attr('x1');
                    var backwardArrowTransformX = this._backArrowHead.node().transform.baseVal.consolidate().matrix.e;
                    var backwardArrowTransformY = this._backArrowHead.node().transform.baseVal.consolidate().matrix.f;

                    this._processorConnector.attr('x1', parseFloat(currentX1ProcessorConnector) + offset);
                    this._processorConnector2.attr('x1', parseFloat(currentX1ProcessorConnector2) + offset);
                    this._backArrowHead.node().transform.baseVal.getItem(0).setTranslate(backwardArrowTransformX + offset, backwardArrowTransformY + 0);
                }, this);

                this.connector.getBoundingBox().on('moved', function (offset) {
                    var currentX2ProcessorConnector = this._processorConnector.attr('x2');
                    var currentX2ProcessorConnector2 = this._processorConnector2.attr('x2');
                    var forwardArrowTransformX = this._forwardArrowHead.node().transform.baseVal.consolidate().matrix.e;
                    var forwardArrowTransformY = this._forwardArrowHead.node().transform.baseVal.consolidate().matrix.f;

                    this._processorConnector.attr('x2', parseFloat(currentX2ProcessorConnector) + offset.dx);
                    this._processorConnector2.attr('x2', parseFloat(currentX2ProcessorConnector2) + offset.dx);
                    this._forwardArrowHead.node().transform.baseVal.getItem(0).setTranslate(forwardArrowTransformX + offset.dx, forwardArrowTransformY + offset.dy);
                    d3.select(this.processorConnectEndPoint.node()).attr("transform", "translate(" + (forwardArrowTransformX + offset.dx) + "," + (forwardArrowTransformY + offset.dy) + ")");
                }, this);

                this.processorConnectPoint.style("display", "none");

                if(!_.isUndefined(this.getParent()._contentGroup)) {
                    var thisNodeGroup = parent.node();
                    var thisNodeGroupParent = this.getParent()._contentGroup.node();

                    thisNodeGroupParent.appendChild(thisNodeGroup);
                }

                var self = this;

                connectorModel.addConnectorActionReference(this);

                this.processorConnectEndPoint.on("mousedown", function () {
                    d3.event.preventDefault();
                    d3.event.stopPropagation();

                    var x =  parseFloat(self.processorConnectEndPoint.attr('cx'));
                    var y =  parseFloat(self.processorConnectEndPoint.attr('cy'));
                    var x1 =  parseFloat(self._processorConnector.attr('x1'));
                    var y1 =  parseFloat(self._processorConnector.attr('y1'));

                    var sourcePoint = self.toGlobalCoordinates(new Point(x, y));
                    var connectorPoint = self.toGlobalCoordinates(new Point(x1, y1));

                    connectorModel.removeConnectorActionReference(self.getModel().id);

                    self.messageManager.startDrawMessage(self, actionInvocationExpressionModel, sourcePoint, connectorPoint);
                    self.messageManager.setTypeBeingDragged(true);

                    self.removeArrows();
                    self.processorConnectEndPoint.remove();
                });

                this.processorConnectEndPoint.on("mouseover", function () {
                    self.processorConnectEndPoint
                        .style("fill-opacity", 0.5)
                        .style("cursor", 'url(images/BlackHandwriting.cur), pointer');
                });

                this.processorConnectEndPoint.on("mouseout", function () {
                    self.processorConnectEndPoint.style("fill-opacity", 0.01);
                });
            }
        };

        ActionInvocationStatementView.prototype.renderProcessorConnectEndPoint = function (renderingContext) {
            var boundingBox = this.getBoundingBox();
            var width = boundingBox.w();
            var height = boundingBox.h();
            var x = boundingBox.getRight();
            var y = boundingBox.getTop();

            var processorConnectEndPoint = D3Utils.circle(parseFloat(this._processorConnector2.attr('x2')) - 3, ((y + height / 2)), 10, this.getStatementGroup());
            processorConnectEndPoint
                .attr("fill-opacity", 0.01)
                .style("fill", "#444");
            this.processorConnectEndPoint = processorConnectEndPoint;
        };

        ActionInvocationStatementView.prototype.renderProcessorConnectPoint = function (renderingContext) {
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
         * Remove related arrow group
         */
        ActionInvocationStatementView.prototype.removeArrows = function () {
            if (!_.isNil(this._arrowGroup) && !_.isNil(this._arrowGroup.node())) {
                d3.select(this._arrowGroup).node().remove();
            }
        };

        /**
         * Covert a point in user space Coordinates to client viewport Coordinates.
         * @param {Point} point a point in current user coordinate system
         */
        ActionInvocationStatementView.prototype.toGlobalCoordinates = function (point) {
            var pt = this.processorConnectPoint.node().ownerSVGElement.createSVGPoint();
            pt.x = point.x();
            pt.y = point.y();
            pt = pt.matrixTransform(this.processorConnectPoint.node().getCTM());
            return new Point(pt.x, pt.y);
        };

        /**
         * Remove statement view callback
         */
        ActionInvocationStatementView.prototype.onBeforeModelRemove = function () {
            this.stopListening(this.getBoundingBox());
            d3.select("#_" +this._model.id).remove();
            this.removeArrows();
            // resize the bounding box in order to the other objects to resize
            this.getBoundingBox().h(0).w(0);
        };

        ActionInvocationStatementView.prototype.updateStatementText = function (newStatementText, propertyKey) {
            var displayText = this.getModel().getStatementString();
            var siblingConnectors = this._parent._model.children;
            var connectorName = newStatementText.match(/\((.*)\)/)[1];
            var self = this;

            connectorName = connectorName.split(",")[0].trim();

            this.getModel().setStatementString(newStatementText);
            this.renderDisplayText(newStatementText);

            self.removeArrows();
            self.processorConnectPoint.style("display", "block");
            self.processorConnectEndPoint.remove();

            _.some(siblingConnectors, function (key, i) {
                if ( (BallerinaASTFactory.isConnectorDeclaration(siblingConnectors[i]))
                         && (siblingConnectors[i]._connectorVariable == connectorName) ) {
                    self.getActionInvocationExpressionModel().setConnector(siblingConnectors[i]);
                    // Stop listening to the top edge moved event. Since this is re initialized at the render arrows
                    self.stopListening(self.getBoundingBox(), 'top-edge-moved');
                    // TODO: refactor this
                    self.renderArrows(self.getDiagramRenderingContext());
               }
            });
        };

        ActionInvocationStatementView.prototype.getActionInvocationExpressionModel = function () {
            if (BallerinaASTFactory.isActionInvocationStatement(this.getModel())) {
                var actionExpression = this.getModel().getChildren()[0];
                if(BallerinaASTFactory.isActionInvocationExpression(actionExpression)){
                    return actionExpression;
                }
            } else if (BallerinaASTFactory.isAssignmentStatement(this.getModel())) {
                var actionExpression = this.getModel().getChildren()[1].getChildren()[0];
                if (BallerinaASTFactory.isActionInvocationExpression(actionExpression)) {
                    return actionExpression;
                }
            } else if (BallerinaASTFactory.isVariableDefinitionStatement(this.getModel())) {
                var actionExpression = this.getModel().getChildren()[0];
                if (BallerinaASTFactory.isActionInvocationExpression(actionExpression)) {
                    return actionExpression;
                }
            }
        };

        ActionInvocationStatementView.prototype.updateActivatedTarget = function (target) {
            var actionInvocationExpressionModel = this.getActionInvocationExpressionModel();
            if (!_.isUndefined(target)) {
                actionInvocationExpressionModel.setConnector(target);
            } else {
                actionInvocationExpressionModel.setConnector(undefined);
            }
            this.updateStatementString();
        };

        ActionInvocationStatementView.prototype.updateStatementString = function () {
            var actionInvocationExpressionModel = this.getActionInvocationExpressionModel();
            if (BallerinaASTFactory.isAssignmentStatement(this.getModel())) {
                var rightOperandExp = this.getModel().getChildren()[1];
                if (!_.isUndefined(rightOperandExp) && BallerinaASTFactory.isRightOperandExpression(rightOperandExp)) {
                    rightOperandExp.setRightOperandExpressionString(actionInvocationExpressionModel.getExpression());
                }
            } else if (BallerinaASTFactory.isVariableDefinitionStatement(this.getModel())) {
                this.getModel().setRightExpression(actionInvocationExpressionModel.getExpression());
            }
        };

        ActionInvocationStatementView.prototype.isAtValidDropTarget = function(){
            return BallerinaASTFactory.isConnectorDeclaration(this.messageManager.getActivatedDropTarget());
        };

        return ActionInvocationStatementView;

    });