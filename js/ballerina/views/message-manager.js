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
define(['log', 'lodash','d3','./point', 'backbone'], function (log, _, d3,Point, Backbone) {

    var MessageManager = function() {
    log.info("Initialising Message Manager");
    };
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

    MessageManager.prototype.startDrawMessage = function(source,sourcePoint,parent){
        this.setMessageSource(source);

        this.trigger("message-draw-start",source);

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
            log.info("in mousemove");
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
            var endpoint = new Point(tempLine.attr("x2"),tempLine.attr("y2"));

            // line.remove();
            // arrowPoint.remove();
        });


    };
   MessageManager.prototype.constructor = MessageManager;

    return MessageManager;

});

