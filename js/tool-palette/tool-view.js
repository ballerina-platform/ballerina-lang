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

        toolTemplate: _.template("<div id=\"<%=id%>\" class=\"tool-container\"  data-placement=\"bottom\" data-toggle=\"tooltip\" title='<%=title%>'> <img src=\"<%=icon%>\" class=\"tool-image\"  /><p class=\"tool-title\"><%=title%></p></div>"),

        initialize: function (options) {
            _.extend(this, _.pick(options, ["toolPalette"]));
        },

        createHandleDragStopEvent: function (event, ui) {
            this.toolPalette.dragDropManager.reset();
        },

        createHndleOnDragEvent : function(event,ui){
            var helperElm = ui.helper;
            var span = helperElm[0].childNodes;
            var validator = document.getElementById("validator");

            //Visual feedback on invalid drop targets
            if(!this.toolPalette.dragDropManager.isAtValidDropTarget()){
                validator.innerText="X";
                validator.className = "tool-validator";
                validator.style.display="block";
            }
            else{
                validator.style.display="none";
            }
        },

        createHandleDragStartEvent : function(){
            var toolView = this;
            return function(event,ui){
                toolView.toolPalette.dragDropManager.setTypeBeingDragged(this.model);
            }
        },

        render: function (parent) {
            //var createCloneCallback = this.model.get("createCloneCallback");
            var dragCursorOffset = this.getDragCursorOffset();
            var self = this;
            this.$el.html(this.toolTemplate(this.model.attributes));
            this.$el.tooltip();
            parent.append(this.$el);

            this.$el.draggable({
                helper: _.isUndefined(this.createCloneCallback) ?  'clone' : this.createCloneCallback(self),
                cursor: 'move',
                cursorAt: _.isUndefined(dragCursorOffset) ?  { left: -2, top: -2 } : dragCursorOffset,
                zIndex: 10001,
                stop: this.createHandleDragStartEvent(),
                start : this.createHandleDragStartEvent(),
                drag:this.createHandleDragStartEvent()
            });

            return this;
        },

        createContainerForDraggable: function(){
            var body = d3.select("body");
            var div = body.append("div").attr("id", "draggingToolClone");
            //For validation feedback
            div.append('span').attr("id","validator");
            return div;
        },

        partialRender: function (parent, attributes) {
            var dragCursorOffset = this.getDragCursorOffset();
            var self = this;
            this.$el.html(this.toolTemplate(attributes));
            this.$el.tooltip();
            parent.append(this.$el);

            this.$el.draggable({
                helper: _.isUndefined(this.createCloneCallback) ? 'clone' : this.createCloneCallback(self),
                cursor: 'move',
                cursorAt: _.isUndefined(dragCursorOffset) ? {left: -2, top: -2} : dragCursorOffset,
                zIndex: 10001,
                stop: this.createHandleDragStartEvent(),
                start: this.createHandleDragStartEvent(),
                drag: this.createHandleDragStartEvent()
            });
            return this;
        },

        createCloneCallback: function (view) {
            function cloneCallBack() {
                var div = view.createContainerForDraggable();
                d3.xml("images/tool-icons/lifeline.svg").mimeType("image/svg+xml").get(function (error, xml) {
                    if (error) throw error;
                    var svg = xml.getElementsByTagName("svg")[0];
                    d3.select(svg).attr("width", "100px").attr("height", "100px");
                    d3.select(svg).attr("width", "100px");
                    div.node().appendChild(svg);
                });
                return div.node();
            }

            return cloneCallBack;
        },

        getDragCursorOffset: function () {
            return {left: 50, top: 50};
        }

    });

    return toolView;
});
