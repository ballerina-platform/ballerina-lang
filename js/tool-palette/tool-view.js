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

var Tools = (function (tools) {
    var views = tools.Views || {};

    var toolView = Backbone.View.extend({

        toolTemplate: _.template(" <div id=\"<%=id%>\" class=\"tool-container\"> <img src=\"<%=icon%>\" class=\"tool-image\"  /></div>"),
        handleDragStopEvent: function (event, ui) {
            console.log("handleDragStopEvent");
        },

        initialize: function () {
            console.log("ToolView initialized");
        },

        render: function () {
            var id = this.model.attributes.id;
            var icon = this.model.attributes.icon;
            this.$el.html(this.toolTemplate(this.model.attributes));
            this.$el.draggable({
                helper: 'clone',
                cursor: 'move',
                stop: this.handleDragStopEvent
            });

            /*
             var viewObj = this;
             var svg = d3.select(this.$el[0]).append("svg").attr("width", 80).attr("height", 60);
             var g = svg.append("g");
             var drag = d3.drag()
             .on("start",function(){
             console.log("Drag Start initialized " + this);
             })
             .on("drag", function(d, i) {
             var d = {};
             console.log("Dragging initialized " + this);
             var x = d3.event.x;
             var y = d3.event.y;
             d3.select(this.parentNode).attr("transform", "translate(" + x + "," + y + ")");
             })
             .on("end",function(){
             relCoords = d3.mouse($('svg').get(0));
             if(toolId == "tool1"){
             var lifeline = createLifeLine("Lifeline", createPoint(relCoords[0]-240, 50));
             diagram.addElement(lifeline, lifeLineOptions);
             }
             });


             g.append("image").attr("x",0)
             .attr("y",0)
             .attr("width", 80)
             .attr("height", 60)
             .attr("xlink:href", toolImage)
             .call(drag);
             */

            return this;
        }
    });

    views.ToolView = toolView;
    tools.Views = views;
    return tools;

}(Tools || {}));
