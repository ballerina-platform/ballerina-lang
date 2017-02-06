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
define(['lodash', 'd3','log', './ballerina-statement-view', './../ast/action-invocation-expression', './point', 'd3utils', './../ast/ballerina-ast-factory'],
    function (_, d3, log, BallerinaStatementView, ActionInvocationExpression, Point, D3Utils, BallerinaASTFactory) {

        var ActionInvocationStatementView = function (args) {
            BallerinaStatementView.call(this, args);
            this._connectorView = {};

            if (_.isNil(this._container)) {
                log.error("Container for action statement is undefined." + this._container);
                throw "Container for action statement is undefined." + this._container;
            }

            // View options for height and width of the assignment statement box.
            this._viewOptions.height = _.get(args, "viewOptions.height", 30);
            this._viewOptions.width = _.get(args, "viewOptions.width", 120);
            this.getBoundingBox().fromTopCenter(this._topCenter, this._viewOptions.width, this._viewOptions.height);
            this._processorConnector = undefined;
            this._processorConnector2 = undefined;
            this._forwardArrowHead = undefined;
            this._backArrowHead = undefined;
            this._arrowGroup = undefined;
        };

        ActionInvocationStatementView.prototype = Object.create(BallerinaStatementView.prototype);
        ActionInvocationStatementView.prototype.constructor = ActionInvocationStatementView;


        ActionInvocationStatementView.prototype.init = function(){
            this.getModel().on("drawConnectionForAction",this.drawActionConnections,this);
            Object.getPrototypeOf(this.constructor.prototype).init.call(this);
        };
        ActionInvocationStatementView.prototype.setDiagramRenderingContext = function(context){
            this._diagramRenderingContext = context;
        };
        ActionInvocationStatementView.prototype.getDiagramRenderingContext = function(){
            return this._diagramRenderingContext;
        };

        // TODO : Please revisit this method. Needs a refactor
        ActionInvocationStatementView.prototype.drawActionConnections = function(startPoint){
            // Action invocation model is the child of the right operand
            var actionInvocationModel = this._model.getChildren()[1].getChildren()[0];
            log.debug("Drawing connections for http connector actions");
            // TODO : Please alter this logic
            if(!_.isNil(this.getModel().getConnector())) {
                var connector = this.getDiagramRenderingContext().getViewModelMap()[this.messageManager.getActivatedDropTarget().id];
                actionInvocationModel.setConnector(this.messageManager.getActivatedDropTarget());
                this.DrawArrow(this.getDiagramRenderingContext());
            }
        };

        ActionInvocationStatementView.prototype.setModel = function (model) {
            var actionInvocationModel = this._model.getChildren()[1].getChildren()[0];
            if (!_.isNil(model) && model instanceof ActionInvocationExpression) {
                actionInvocationModel = model;
            } else {
                log.error("Action statement definition is undefined or is of different type." + model);
                throw "Action statement definition is undefined or is of different type." + model;
            }
        };

        ActionInvocationStatementView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for action statement is undefined." + container);
                throw "Container for action statement is undefined." + container;
            }
        };

        ActionInvocationStatementView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        ActionInvocationStatementView.prototype.getModel = function () {
            return this._model.getChildren()[1].getChildren()[0];
        };

        ActionInvocationStatementView.prototype.getContainer = function () {
            return this._container;
        };

        ActionInvocationStatementView.prototype.getViewOptions = function () {
            return this._viewOptions;
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
            // TODO : Please revisit this method. Needs a refactor
            this.setDiagramRenderingContext(renderingContext);
            // Action invocation statement is generally an assignment statement. Where the action invocation model is the child of the right operand
            var actionInvocationModel = this._model.getChildren()[1].getChildren()[0];
            var leftOperandModel = this._model.getChildren()[0];
            var connectorModel =  actionInvocationModel.getConnector();
            var self = this;

            if(!_.isUndefined(connectorModel)) {
                this.connector = this.getDiagramRenderingContext().getViewOfModel(connectorModel);
            }
            else {
                var siblingConnectors = _.filter(this._parent._model.children, { 'type': 'ConnectorDeclaration' });

                _.some(siblingConnectors, function (key, i) {
                    var connectorReference = siblingConnectors[i];

                    actionInvocationModel._connector = connectorReference;
                    self.messageManager.setMessageSource(actionInvocationModel);
                    self.messageManager.updateActivatedTarget(connectorReference);

                    return true;
                });
            }

            var assignmentStatementGroup = D3Utils.group(d3.select(this._container));
            var parentGroup = this._container;
            //added attribute 'id' starting with '_' to be compatible with HTML4
            assignmentStatementGroup.attr("id", "_" + this._model.id);
            var width = this.getBoundingBox().w();
            var height = this.getBoundingBox().h();
            var x = this.getBoundingBox().getLeft();
            var y = this.getBoundingBox().getTop();

            var assignmentRect = D3Utils.rect(x, y, width, height, 0, 0, assignmentStatementGroup).classed('statement-rect', true);
            // TODO : Please revisit these calculations.
            var processorConnectorPoint = D3Utils.circle((x + width), ((y + height / 2)), 10, assignmentStatementGroup);
            processorConnectorPoint.attr("fill-opacity", 0.01);

            this.processorConnectPoint = processorConnectorPoint;
            var assignmentText = actionInvocationModel.getActionPackageName() + ":" + actionInvocationModel.getActionName();
            // TODO : Please revisit these calculations.
            var expressionText = D3Utils.textElement(x + width / 2, y + height / 2, assignmentText, assignmentStatementGroup)
                .classed('statement-text', true);
            actionInvocationModel.accept(this);

            // Creating property pane
            var editableProperty = {
                    propertyType: "text",
                    key: "Action Invocation",
                    model: this._model,
                    getterMethod: this._model.getStatementString,
                    setterMethod: this._model.setStatementString
            };

            this._createPropertyPane({
                model: this._model,
                statementGroup: assignmentStatementGroup,
                editableProperties: editableProperty
            });

            this.parentContainer = d3.select(parentGroup);
            //Drawing the message.
            this.DrawArrow(this.getDiagramRenderingContext());

            self = this;

            this.processorConnectPoint.on("mousedown", function () {
                d3.event.preventDefault();
                d3.event.stopPropagation();
                var x =  parseFloat(self.processorConnectPoint.attr('cx'));
                var y =  parseFloat(self.processorConnectPoint.attr('cy'));
                var sourcePoint = self.toGlobalCoordinates(new Point(x, y));

                self.messageManager.startDrawMessage(actionInvocationModel, sourcePoint);
                self.messageManager.setTypeBeingDragged(true);
            });

            this.processorConnectPoint.on("mouseover", function () {
                processorConnectorPoint
                    .style("fill", "#444")
                    .style("fill-opacity", 0.5)
                    .style("cursor", 'url(images/BlackHandwriting.cur), pointer');
            });

            this.processorConnectPoint.on("mouseout", function () {
                processorConnectorPoint.style("fill-opacity", 0.01);
            });

            this.getBoundingBox().on('top-edge-moved', function(dy){
                assignmentRect.attr('y',  parseFloat(assignmentRect.attr('y')) + dy);
                expressionText.attr('y',  parseFloat(expressionText.attr('y')) + dy);
                processorConnectorPoint.attr('cy',  parseFloat(processorConnectorPoint.attr('cy')) + dy);
            });
        };

        ActionInvocationStatementView.prototype.DrawArrow = function (context) {
            this.setDiagramRenderingContext(context);
            var actionInvocationModel = this._model.getChildren()[1].getChildren()[0];
            var connectorModel = actionInvocationModel.getConnector();
            var actionInvocationModelMessageManager = Object.create(this.messageManager);

            if(!_.isUndefined(connectorModel)) {
                this.connector = this.getDiagramRenderingContext().getViewOfModel(connectorModel);
            }

            if(!_.isUndefined(this.connector)) {
                var parent = this.parentContainer;
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
                this._processorConnector2 = D3Utils.line(Math.round(startPoint.x()), Math.round(startPoint.y()) + 8, Math.round(connectorCenterPointX),
                    Math.round(startPoint.y()) + 8, this._arrowGroup).classed("action-dash-line", true);
                this._backArrowHead = D3Utils.outputTriangle(Math.round(startPoint.x()), Math.round(startPoint.y()) + 8, this._arrowGroup).classed("action-arrow", true);

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

                this.arrowHeadEndPoint = arrowHeadEnd;

                var self = this;

                connectorModel.addConnectorActionReference(this);

                this.arrowHeadEndPoint.on("mousedown", function () {
                    d3.event.preventDefault();
                    d3.event.stopPropagation();

                    var x =  parseFloat(self.arrowHeadEndPoint.attr('cx'));
                    var y =  parseFloat(self.arrowHeadEndPoint.attr('cy'));
                    var x1 =  parseFloat(self._processorConnector.attr('x1'));
                    var y1 =  parseFloat(self._processorConnector.attr('y1'));

                    var sourcePoint = self.toGlobalCoordinates(new Point(x, y));
                    var connectorPoint = self.toGlobalCoordinates(new Point(x1, y1));

                    connectorModel.removeConnectorActionReference(self.getModel().id);

                    self.messageManager.startDrawMessage(actionInvocationModel, sourcePoint, connectorPoint);
                    self.messageManager.setTypeBeingDragged(true);

                    self._forwardArrowHead.remove();
                    self._processorConnector.remove();
                    self._processorConnector2.remove();
                    self._backArrowHead.remove();
                    self.arrowHeadEndPoint.remove();
                });

                this.arrowHeadEndPoint.on("mouseover", function () {
                    arrowHeadEnd
                        .style("fill-opacity", 0.5)
                        .style("cursor", 'url(images/BlackHandwriting.cur), pointer');
                });

                this.arrowHeadEndPoint.on("mouseout", function () {
                    arrowHeadEnd.style("fill-opacity", 0.01);
                });
            }
        };

        /**
         * Remove the forward and the backward arrow heads
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
            d3.select("#_" +this._model.id).remove();
            this.getDiagramRenderingContext().getViewOfModel(this._model.getParent()).getStatementContainer()
                .removeInnerDropZone(this._model);
            this.removeArrows();
            // resize the bounding box in order to the other objects to resize
            var moveOffset = -this.getBoundingBox().h() - 30;
            this.getBoundingBox().move(0, moveOffset);
        };

        return ActionInvocationStatementView;

    });