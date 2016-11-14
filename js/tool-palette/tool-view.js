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

define(['require', 'jquery', 'd3', 'backbone', 'lodash', 'd3utils'], function (require, $, d3, Backbone, _, D3Utils) {

    var toolView = Backbone.View.extend({

        toolTemplate: _.template("<div id=\"<%=id%>\" class=\"tool-container\"> <img src=\"<%=icon%>\" class=\"tool-image\"  /><p class=\"tool-title\"><%=title%></p></div>"),
        handleDragStopEvent: function (event, ui) {

        },
        handleOnDragEvent : function(event,ui){
            var helperElm = ui.helper;
            var span = helperElm[0].childNodes;
            var validator = document.getElementById("validator");

            //Visual feedback on invalid drops on endpoints
            //if(eventManager.invalid){
            //    validator.innerText="X";
            //    validator.className = "tool-validator";
            //    validator.style.display="block";
            //}
            //else{
            //    validator.style.display="none";
            //}
        },

        handleDragStartEvent : function(event,ui){
            var elm = $(ui.draggable).clone();
            //eventManager.currentType(event.currentTarget.lastChild.id);
            //eventManager.draggedElement(elm);

        },

        initialize: function () {
        },

        render: function (parent) {
            var id = this.model.attributes.id;
            var icon = this.model.attributes.icon;
            var createCloneCallback = this.model.get("createCloneCallback");
            var dragCursorOffset = this.model.get("dragCursorOffset");
            var self = this;
            this.$el.html(this.toolTemplate(this.model.attributes));
            parent.append(this.$el);

            this.$el.draggable({
                helper: _.isUndefined(createCloneCallback) ?  'clone' : createCloneCallback(self),
                cursor: 'move',
                cursorAt: _.isUndefined(dragCursorOffset) ?  { left: -2, top: -2 } : dragCursorOffset,
                zIndex: 10001,
                stop: this.handleDragStopEvent,
                start : this.handleDragStartEvent,
                drag:this.handleOnDragEvent
            });

            return this;
        },

        createContainerForDraggable: function(){
            var body = d3.select("body");
            var div = body.append("div").attr("id", "draggingToolClone");
            //For validation feedback
            div.append('span').attr("id","validator");
            div =  D3Utils.decorate(div);
            return div;
        }

    });

    return toolView;
});
