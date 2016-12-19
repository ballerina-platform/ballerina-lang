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
define(['log', 'lodash','d3','./point', 'backbone','event_channel'], function (log, _, d3,Point, Backbone, EventChannel) {

    var MessageManager = function() {
    log.info("Initialising Message Manager");
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
            this.setActivatedDropTarget(activatedDropTarget);
        }
        if (!_.isUndefined(validateCallBack)) {
            this.setValidateCallBack(validateCallBack);
        }
    };

    MessageManager.prototype.isOnDrag = function(){
        return !_.isNil(this.getMessageSource());
    };

    MessageManager.prototype.isAtValidDropTarget = function(){
        var allowedBySource = true,
            allowedByTarget = true,
            allowedBySourceValidateCallBack = true,
            allowedByTargetValidateCallBack = true;

        if(!_.isUndefined(this.getActivatedDropTarget())){

            allowedBySource = this.getMessageSource.canBeActionOf(this.getActivatedDropTarget());
            allowedByTarget = this.getActivatedDropTarget().canBeConnectorOf(this.getMessageSource());

            //var validateDropTargetCallback = this.getValidateCallBack();
            //if(!_.isUndefined(validateDropTargetCallback)){
            //    if(_.isFunction(validateDropTargetCallback)){
            //        allowedByTargetValidateCallBack = validateDropTargetCallback(this.getActivatedDropTarget());
            //    }
            //}
            //
            //var validateDropSourceCallback = this.get('validateDropSourceCallback');
            //if(!_.isUndefined(validateDropSourceCallback)){
            //    if(_.isFunction(validateDropSourceCallback)){
            //        allowedBySourceValidateCallBack = validateDropSourceCallback(this.getTypeBeingDragged());
            //    }
            //}

            return allowedBySource && allowedByTarget
                && allowedBySourceValidateCallBack
                && allowedByTargetValidateCallBack;
        }
        return false;
    };

    MessageManager.prototype.reset = function(){
        /**
         * @event MessageManager#drag-stop
         * @type {ASTNode}
         */
        //this.trigger('drag-stop', this.get('typeBeingDragged'));
        // this.trigger('drop-target-changed', undefined);
        this.setMessageSource(undefined);
        this.setValidateCallBack( undefined);
        this.setActivatedDropTarget(undefined);

    },

    MessageManager.prototype.startDrawMessage = function(source,sourcePoint,parent){
        this.setMessageSource(source);
        var self = this;


        var tempLine =  parent.append("line")
            .attr("x1", sourcePoint.x() )
            .attr("y1",sourcePoint.y())
            .attr("x2",sourcePoint.x() )
            .attr("y2", sourcePoint.y() )
            .attr("stroke","#9d9d9d");
        var points = "" +  sourcePoint.x() + "," + (sourcePoint.y() - 5) + " " + ( sourcePoint.x() + 5) + "," + (sourcePoint.y()) + " " + sourcePoint.x() + "," + (sourcePoint.y() + 5);
        var arrowPoint = parent.append("polyline")
            .attr("points", points);

        parent.on("mousemove", function () {
            log.info("in mousemove of start-draw-message");
            var m = d3.mouse(this);
            tempLine.attr("x2", m[0]);
            tempLine.attr("y2", m[1]);
            var newPoints = "" +  m[0] + "," + (m[1] - 5) + " " + ( m[0] + 5) + "," + ( m[1]) + " " +  m[0] + "," + ( m[1] + 5);
            arrowPoint.attr("points",newPoints);
        });

        parent.on("mouseup", function () {
            // unbind current listeners
           parent.on("mousemove", null);
            parent.on("mouseup", null);

            var startPoint = new Point(tempLine.attr("x1"),tempLine.attr("y1"));
            var endPoint = new Point(tempLine.attr("x2"),tempLine.attr("y2"));

            if(self.isAtValidDropTarget()){

                var connectorReference = self.getActivatedDropTarget();
                self.getMessageSource().setConnector(connectorReference);
                self.getMessageSource().trigger("drawConnectionForAction",startPoint,parent);
            }
            tempLine.remove();
            arrowPoint.remove();
            self.getMessageSource().trigger("drawConnectionForAction",startPoint,parent);
            self.reset();

            // line.remove();
            // arrowPoint.remove();
        });


    };
   //MessageManager.prototype.constructor = MessageManager;

    return MessageManager;

});

