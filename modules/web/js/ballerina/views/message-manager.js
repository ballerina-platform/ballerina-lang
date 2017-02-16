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
define(['log', 'lodash','d3','./point', 'backbone','event_channel', 'ballerina/ast/ballerina-ast-factory'],
    function (log, _, d3,Point, Backbone, EventChannel, BallerinaASTFactory) {

    var MessageManager = function(args) {
        log.debug("Initialising Message Manager");
        this.typeBeingDragged = undefined;
        this._canvas = _.get(args, 'canvas');
    };

    MessageManager.prototype = Object.create(EventChannel.prototype);
    MessageManager.prototype.constructor = MessageManager;

    MessageManager.prototype.setMessageSource = function(source){
         if (!_.isUndefined(source)) {
             this.messageSource = source;
         }
    };

    MessageManager.prototype.getMessageSource = function(){
       return this.messageSource;
    };

    MessageManager.prototype.setMessageTarget = function(destination){
        if (!_.isUndefined(destination)) {
            this.messageTarget = destination;
        }
    };
    MessageManager.prototype.getMessageTarget = function(){
        return this.messageTarget;
    };

    MessageManager.prototype.setActivatedDropTarget = function (dropTarget) {
        if (!_.isUndefined(dropTarget)) {
            this.activatedDropTarget = dropTarget;
        }
    };

    MessageManager.prototype.getActivatedDropTarget = function () {
        return this.activatedDropTarget;
    };

    MessageManager.prototype.setValidateCallBack = function (callBackMethod) {
        if (!_.isUndefined(callBackMethod)) {
            this.validateCallBack = callBackMethod;
        }
    };

    MessageManager.prototype.getValidateCallBack = function () {
        return this.validateCallBack;
    };

    MessageManager.prototype.setActivatedDropTarget = function (activatedDropTarget,validateCallBack) {
        if (!_.isUndefined(activatedDropTarget)) {
            if (!_.isEqual(activatedDropTarget, this.getActivatedDropTarget())){
                /**
                 * @eventMessageManager#drop-target-changed
                 * @type ASTNode
                 */
                // this.trigger('drop-target-changed', activatedDropTarget);
            }
            this.activatedDropTarget = activatedDropTarget;
        }
        if (!_.isUndefined(validateCallBack)) {
            this.setValidateCallBack(validateCallBack);
        }
    };

    /**
     * Set the type being dragged at a given moment.
     * @param type being dragged
     * @param validateDropTargetCallback {DragDropManager~validateDropTargetCallback} - call back to do additional validations on drop target
     *
     */
    MessageManager.prototype.setTypeBeingDragged = function (type, validateDropTargetCallback) {
        if (!_.isUndefined(type)) {
            this.typeBeingDragged = type;
        }
        if (!_.isUndefined(validateDropTargetCallback)) {
            this.validateDropTargetCallback = validateDropTargetCallback;
        }
    };

    /**
     * Gets the type which is being dragged at a given moment - if any.
     * @return type
     */
    MessageManager.prototype.getTypeBeingDragged = function () {
        return this.typeBeingDragged;
    };

    MessageManager.prototype.isOnDrag = function () {
        return !_.isNil(this.typeBeingDragged);
    };

    MessageManager.prototype.reset = function(){
        /**
         * @event MessageManager#drag-stop
         * @type {ASTNode}
         */
        this.setMessageSource(undefined);
        this.setValidateCallBack( undefined);
        this.setActivatedDropTarget(undefined);
        this.typeBeingDragged = undefined;
    };

    MessageManager.prototype.startDrawMessage = function(source, actionInvocationModel, sourcePoint, connectorPoint){
        var connectorStartPoint,
            connectorEndPoint;

        if(connectorPoint){
            connectorStartPoint = connectorPoint.x();
            connectorEndPoint = connectorPoint.y();
        }
        else{
            connectorStartPoint = sourcePoint.x();
            connectorEndPoint = sourcePoint.y();
        }

        this.setMessageSource(source);

        var self = this,
            container = d3.select(this._canvas.getSVG().get(0));

        var tempLine = container.append("line")
            .attr("x1",connectorStartPoint )
            .attr("y1",connectorEndPoint )
            .attr("x2",sourcePoint.x() )
            .attr("y2",sourcePoint.y() )
            .attr("stroke","#9d9d9d");
        var points = "" +  sourcePoint.x() + "," + (sourcePoint.y() - 5) + " " + ( sourcePoint.x() + 5) + ","
            + (sourcePoint.y()) + " " + sourcePoint.x() + "," + (sourcePoint.y() + 5);
        var arrowPoint = container.append("polyline")
            .attr("points", points);

        container.on("mousemove", function () {
            var m = d3.mouse(this);
            //setting an offset of 5 to avoid the mouse pointer overlapping with the arrow
            tempLine.attr("x2", m[0] - 5);
            tempLine.attr("y2", sourcePoint.y());
            var newPoints = "" +  (m[0] - 5) + "," + (sourcePoint.y() - 5) + " " + ( m[0]) + ","
                + (sourcePoint.y()) + " " +  (m[0]- 5) + "," + ( sourcePoint.y() + 5);
            arrowPoint.attr("points", newPoints);
        });

        container.on("mouseup", function () {
            // unbind current listeners
            container.on("mousemove", null);
            container.on("mouseup", null);
            var startPoint = new Point(tempLine.attr("x1"),tempLine.attr("y1"));
            var endPoint = new Point(tempLine.attr("x2"),tempLine.attr("y2"));

            tempLine.remove();
            arrowPoint.remove();
            self.getMessageSource().getModel().trigger("drawConnectionForAction", startPoint, container);
            self.trigger('drop-target-changed', undefined);
            self.reset();
        });
    };

    return MessageManager;
});