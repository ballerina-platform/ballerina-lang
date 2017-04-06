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
import _ from 'lodash';
import * as d3 from 'd3';
import log from 'log';
import SimpleStatementView from './simple-statement-view';
import Point from './point';
import D3Utils from 'd3utils';
import BallerinaASTFactory from './../ast/ballerina-ast-factory';

/**
 * Action invocation statement view.
 * @class ActionInvocationStatementView
 * @extends SimpleStatementView
 */
class ActionInvocationStatementView extends SimpleStatementView {
    /**
     * @param args {*} constructor arguments
     * @constructor
     */
    constructor(args) {
        super(args);
        this._connectorView = {};

        if (_.isNil(this._container)) {
            log.error('Container for action statement is undefined.' + this._container);
            throw 'Container for action statement is undefined.' + this._container;
        }

        this.getBoundingBox().fromTopCenter(this._topCenter, this._viewOptions.width, this._viewOptions.height);
        this._processorConnector = undefined;
        this._processorConnector2 = undefined;
        this._forwardArrowHead = undefined;
        this._backArrowHead = undefined;
        this._arrowGroup = undefined;

    }

    init() {
        this.getModel().on('drawConnectionForAction', this.drawActionConnections, this);
        Object.getPrototypeOf(this.constructor.prototype).init.call(this);
    }

    setDiagramRenderingContext(context) {
        this._diagramRenderingContext = context;
    }

    getDiagramRenderingContext() {
        return this._diagramRenderingContext;
    }

    drawActionConnections() {
        var actionInvocationModel = this.getActionInvocationExpressionModel();
        if(this.isAtValidDropTarget()){
            this.updateActivatedTarget(this.messageManager.getActivatedDropTarget(), actionInvocationModel);
        }

        if(!_.isNil(actionInvocationModel.getConnector())) {
            this.renderDisplayText(this.getModel().getStatementString());
            this.renderArrows(this.getDiagramRenderingContext());
        }
    }

    setConnectorView(view) {
        this._connectorView = view;
    }

    getConnectorView() {
        return this._connectorView;
    }

    /**
     * Rendering the view for get-Action statement.
     * @returns {group} The svg group which contains the elements of the action statement view.
     */
    render(renderingContext) {
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
        if (_.isUndefined(connectorModel)) {
            var connectorsInImmediateScope = this.getParent().getModel().getConnectorsInImmediateScope();
            var connectorReference = connectorsInImmediateScope[0];
            actionInvocationExpressionModel.setConnector(connectorReference);
            self.messageManager.setMessageSource(model);
            self.updateActivatedTarget(connectorReference);
        }

        // Setting display text.
        this.renderDisplayText(model.getStatementString());
        this.renderProcessorConnectPoint(renderingContext);
        // Register the events for processorConnectorPoint on BBox events
        this.processorConnectPointOnBBoxEvents();
        this.processorConnectPointOnMouseEvents();
        this.renderArrows(renderingContext);

        // Creating property pane
        var editableProperty = {
            propertyType: 'text',
            key: 'Action Invocation',
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
    }

    renderArrows(renderingContext) {
        this.setDiagramRenderingContext(renderingContext);
        var actionInvocationExpressionModel = this.getActionInvocationExpressionModel();
        var connectorModel = actionInvocationExpressionModel.getConnector();
        var connectorView;

        if(!_.isNil(connectorModel)) {
            connectorView = this.getDiagramRenderingContext().getViewOfModel(connectorModel);
        }

        if(!_.isNil(connectorView)) {
            var self = this;

            // Unregister the BBox rightEdgeMove event listener
            this.stopListening(this.getBoundingBox(), 'right-edge-moved', self.onRightEdgeMoveCallback);

            var parent = this.getStatementGroup();
            this._arrowGroup = D3Utils.group(parent).attr('transform', 'translate(0,0)');
            var width = this.getBoundingBox().w();
            var height = this.getBoundingBox().h();
            var x = this.getBoundingBox().getLeft();
            var y = this.getBoundingBox().getTop();
            var sourcePointX = x + width;
            var sourcePointY = y + height / 2;

            var startPoint = new Point(sourcePointX, sourcePointY);
            var connectorCenterPointX = connectorView.getMiddleLineCenter().x();

            // Move the processorConnectPoint to the connector x
            this._processorConnectPoint.attr('cx', connectorCenterPointX);

            this._processorConnector = D3Utils.line(Math.round(startPoint.x()), Math.round(startPoint.y()), Math.round(connectorCenterPointX),
                Math.round(startPoint.y()), this._arrowGroup).classed('action-line', true);
            this._forwardArrowHead = D3Utils.inputTriangle(Math.round(connectorCenterPointX) - 5, Math.round(startPoint.y()), this._arrowGroup).classed('action-arrow', true);
            this._forwardArrowHead.attr('transform', 'translate(0,0)');
            this._processorConnector2 = D3Utils.line(Math.round(startPoint.x()), Math.round(startPoint.y()) + 8, Math.round(connectorCenterPointX),
                Math.round(startPoint.y()) + 8, this._arrowGroup).classed('action-dash-line', true);
            this._backArrowHead = D3Utils.outputTriangle(Math.round(startPoint.x()), Math.round(startPoint.y()) + 8, this._arrowGroup).classed('action-arrow', true);
            this._backArrowHead.attr('transform', 'translate(0,0)');

            this.listenTo(this.getBoundingBox(), 'top-edge-moved', self.topEdgeMoveArrowPositionCallback, this);

            this.listenTo(this.getBoundingBox(), 'right-edge-moved', self.rightEdgeMoveArrowPositionCallback, this);

            this.listenTo(connectorView.getBoundingBox(),'moved', self.connectorMoveArrowPositionCallback, this);

            if(!_.isUndefined(this.getParent()._contentGroup)) {
                var thisNodeGroup = parent.node();
                var thisNodeGroupParent = this.getParent()._contentGroup.node();

                thisNodeGroupParent.appendChild(thisNodeGroup);
            }

            connectorModel.addConnectorActionReference(this);
        } else {
            this._processorConnectPoint.attr('cx', this.getBoundingBox().getRight());
        }
    }

    renderProcessorConnectPoint() {
        var boundingBox = this.getBoundingBox();
        var x = boundingBox.getRight();
        var y = boundingBox.getTop();
        var height = boundingBox.h();
        var processorConnectPoint = D3Utils.circle(x, ((y + height / 2)), 10, d3.select(this.getStatementGroup().node().ownerSVGElement));
        processorConnectPoint.attr('fill-opacity', 0.01);
        this._processorConnectPoint = processorConnectPoint;
    }

    /**
     * Remove related arrow group
     */
    removeArrows() {
        var actionInvocationExpressionModel = this.getActionInvocationExpressionModel();
        var connectorModel = actionInvocationExpressionModel.getConnector();
        var connectorView;

        if (!_.isNil(this._arrowGroup) && !_.isNil(this._arrowGroup.node())) {
            d3.select(this._arrowGroup).node().remove();
        }

        // When the statement text being updated, we need to stop listening to the current respective connectorView's
        // bounding box move event
        if(!_.isNil(connectorModel)) {
            connectorView = this.getDiagramRenderingContext().getViewOfModel(connectorModel);
            this.stopListening(connectorView, 'moved');
        }

        this.stopListening(this.getBoundingBox(), 'top-edge-moved', this.topEdgeMoveArrowPositionCallback, this);
        this.stopListening(this.getBoundingBox(), 'right-edge-moved', this.rightEdgeMoveArrowPositionCallback, this);
    }

    /**
     * Covert a point in user space Coordinates to client viewport Coordinates.
     * @param {Point} point a point in current user coordinate system
     */
    toGlobalCoordinates(point) {
        var pt = this._processorConnectPoint.node().ownerSVGElement.createSVGPoint();
        pt.x = point.x();
        pt.y = point.y();
        pt = pt.matrixTransform(this._processorConnectPoint.node().getCTM());
        return new Point(pt.x, pt.y);
    }

    /**
     * Remove statement view callback
     */
    onBeforeModelRemove() {
        this.stopListening(this.getBoundingBox());
        d3.select('#_' +this._model.id).remove();
        this.removeArrows();
        // resize the bounding box in order to the other objects to resize
        var gap = this.getParent().getStatementContainer().getInnerDropZoneHeight();
        this.getBoundingBox().move(0, -this.getBoundingBox().h() - gap).w(0);
    }

    updateStatementText(newStatementText) {
        var connectorName = newStatementText.match(/\((.*)\)/)[1];
        var self = this;
        var actionInvocationExpressionModel = this.getActionInvocationExpressionModel();
        var currentConnectorModel = actionInvocationExpressionModel.getConnector();
        var currentConnectorView;

        // When the statement text being updated, we need to stop listening to the current respective connectorView's
        // bounding box move event
        if(!_.isNil(currentConnectorModel)) {
            currentConnectorView = this.getDiagramRenderingContext().getViewOfModel(currentConnectorModel);
            this.stopListening(currentConnectorView.getBoundingBox(), 'moved');
        }

        connectorName = connectorName.split(',')[0].trim();
        var connector = this.getParent().getModel().getConnectorByName(connectorName);

        this.getModel().setStatementString(newStatementText);
        this.renderDisplayText(newStatementText);

        self.removeArrows();
        self._processorConnectPoint.style('display', 'block');

        if (!_.isNil(connector)) {
            self.getActionInvocationExpressionModel().setConnector(connector);
            self.renderArrows(self.getDiagramRenderingContext());
        } else {
            this._processorConnectPoint.attr('cx', this.getBoundingBox().getRight());
        }
    }

    getActionInvocationExpressionModel() {
        var actionExpression;
        if (BallerinaASTFactory.isActionInvocationStatement(this.getModel())) {
            actionExpression = this.getModel().getChildren()[0];
            if(BallerinaASTFactory.isActionInvocationExpression(actionExpression)){
                return actionExpression;
            }
        } else if (BallerinaASTFactory.isAssignmentStatement(this.getModel())) {
            actionExpression = this.getModel().getChildren()[1].getChildren()[0];
            if (BallerinaASTFactory.isActionInvocationExpression(actionExpression)) {
                return actionExpression;
            }
        } else if (BallerinaASTFactory.isVariableDefinitionStatement(this.getModel())) {
            actionExpression = this.getModel().getChildren()[1];
            if (BallerinaASTFactory.isActionInvocationExpression(actionExpression)) {
                return actionExpression;
            }
        }
    }

    updateActivatedTarget(target) {
        var actionInvocationExpressionModel = this.getActionInvocationExpressionModel();
        actionInvocationExpressionModel.setConnector(target);
        this.updateStatementString();
    }

    updateStatementString() {
        var actionInvocationExpressionModel = this.getActionInvocationExpressionModel();
        if (BallerinaASTFactory.isAssignmentStatement(this.getModel())) {
            var rightOperandExp = this.getModel().getChildren()[1];
            if (!_.isUndefined(rightOperandExp) && BallerinaASTFactory.isRightOperandExpression(rightOperandExp)) {
                rightOperandExp.setRightOperandExpressionString(actionInvocationExpressionModel.getExpression());
            }
        } else if (BallerinaASTFactory.isVariableDefinitionStatement(this.getModel())) {
            this.getModel().setRightExpression(actionInvocationExpressionModel.getExpression());
        }
    }

    isAtValidDropTarget() {
        return BallerinaASTFactory.isConnectorDeclaration(this.messageManager.getActivatedDropTarget());
    }

    /**
     * Register Event listeners for the processorConnectorPoint on BBox events
     */
    processorConnectPointOnBBoxEvents() {
        var self = this;
        this.listenTo(this.getBoundingBox(), 'top-edge-moved', self.onTopEdgeMoveCallback, self);
        this.listenTo(this.getBoundingBox(), 'right-edge-moved', self.onRightEdgeMoveCallback, self);
    }

    onTopEdgeMoveCallback(dy) {
        this._processorConnectPoint.attr('cy',  parseFloat(this._processorConnectPoint.attr('cy')) + dy);
        this.getSvgRect().attr('y', parseFloat(this.getSvgRect().attr('y')) + dy);
        this.getSvgText().attr('y', parseFloat(this.getSvgText().attr('y')) + dy);
    }

    onRightEdgeMoveCallback(dx) {
        this._processorConnectPoint.attr('cx',  parseFloat(this._processorConnectPoint.attr('cx')) + dx);
    }

    processorConnectPointOnMouseEvents() {
        var self = this;
        var model = this.getModel();
        this._processorConnectPoint.on('mousedown', function () {
            d3.event.preventDefault();
            d3.event.stopPropagation();
            var x =  self.getBoundingBox().getRight();
            var y =  self.getBoundingBox().getTop() + self.getBoundingBox().h() / 2;
            var sourcePoint = self.toGlobalCoordinates(new Point(x, y));

            var actionInvocationExpressionModel = self.getActionInvocationExpressionModel();
            var currentConnectorModel = actionInvocationExpressionModel.getConnector();
            var currentConnectorView;

            // When the statement text being updated, we need to stop listening to the current respective connectorView's
            // bounding box move event
            if(!_.isNil(currentConnectorModel)) {
                currentConnectorView = self.getDiagramRenderingContext().getViewOfModel(currentConnectorModel);
                self.stopListening(currentConnectorView.getBoundingBox(), 'moved');
            }

            // If there are already drawn arrow, this will remove
            self.removeArrows();

            self.messageManager.startDrawMessage(self, model, sourcePoint);
            self.messageManager.setTypeBeingDragged(true);
        });

        this._processorConnectPoint.on('mouseover', function () {
            self._processorConnectPoint
                .style('fill', '#444')
                .style('fill-opacity', 0.5)
                .style('cursor', 'url(images/BlackHandwriting.cur), pointer');
        });

        this._processorConnectPoint.on('mouseout', function () {
            self._processorConnectPoint.style('fill-opacity', 0.01);
        });
    }

    topEdgeMoveArrowPositionCallback(offset) {
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
    }

    rightEdgeMoveArrowPositionCallback(offset) {
        var currentX1ProcessorConnector = this._processorConnector.attr('x1');
        var currentX1ProcessorConnector2 = this._processorConnector2.attr('x1');
        var backwardArrowTransformX = this._backArrowHead.node().transform.baseVal.consolidate().matrix.e;
        var backwardArrowTransformY = this._backArrowHead.node().transform.baseVal.consolidate().matrix.f;

        this._processorConnector.attr('x1', parseFloat(currentX1ProcessorConnector) + offset);
        this._processorConnector2.attr('x1', parseFloat(currentX1ProcessorConnector2) + offset);
        this._backArrowHead.node().transform.baseVal.getItem(0).setTranslate(backwardArrowTransformX + offset, backwardArrowTransformY + 0);
    }

    connectorMoveArrowPositionCallback(offset) {
        var currentX2ProcessorConnector = this._processorConnector.attr('x2');
        var currentX2ProcessorConnector2 = this._processorConnector2.attr('x2');
        var forwardArrowTransformX = this._forwardArrowHead.node().transform.baseVal.consolidate().matrix.e;
        var forwardArrowTransformY = this._forwardArrowHead.node().transform.baseVal.consolidate().matrix.f;

        this._processorConnector.attr('x2', parseFloat(currentX2ProcessorConnector) + offset.dx);
        this._processorConnector2.attr('x2', parseFloat(currentX2ProcessorConnector2) + offset.dx);
        this._forwardArrowHead.node().transform.baseVal.getItem(0).setTranslate(forwardArrowTransformX + offset.dx, forwardArrowTransformY + offset.dy);
        this._processorConnectPoint.attr('cx', parseFloat(this._processorConnectPoint.attr('cx')) + offset.dx);
        this._processorConnectPoint.attr('cy', parseFloat(this._processorConnectPoint.attr('cy')) + offset.dy);
    }
}

export default ActionInvocationStatementView;
