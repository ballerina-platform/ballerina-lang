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
            _.set(this, 'options.dragIcon.box.size', '60px');
        },

        createHandleDragStopEvent: function (event, ui) {
            this.toolPalette.dragDropManager.reset();
            this._$divBeingDragged = undefined;
        },

        createHandleOnDragEvent : function(event,ui){
            //visual feedback on invalid drop targets
            if(!this.toolPalette.dragDropManager.isAtValidDropTarget()){

            }
            else{

            }
        },

        createHandleDragStartEvent : function(){
            var toolView = this;
            return function(event,ui){
                toolView.toolPalette.dragDropManager.setTypeBeingDragged(toolView.model.nodeFactoryMethod());
            }
        },

        render: function (parent) {
            var dragCursorOffset = this.model.dragCursorOffset;
            var self = this;
            this.$el.html(this.toolTemplate(this.model.attributes));
            this.$el.tooltip();
            parent.append(this.$el);

            this.$el.draggable({
                helper: _.isUndefined(this.createCloneCallback) ?  'clone' : this.createCloneCallback(self),
                cursor: 'move',
                cursorAt: _.isUndefined(dragCursorOffset) ?  { left: 30, top: -10 } : dragCursorOffset,
                zIndex: 10001,
                stop: this.createHandleDragStopEvent(),
                start : this.createHandleDragStartEvent(),
                drag:this.createHandleOnDragEvent()
            });

            return this;
        },

        createContainerForDraggable: function(){
            var body = d3.select("body");
            var div = body.append("div").attr("id", "draggingToolClone");
            this._$divBeingDragged = div;
            //For validation feedback
            div.append('span').attr("id","validator");
            return div;
        },

        createCloneCallback: function (view) {
            var iconSVG,
                iconSize = _.get(this, 'options.dragIcon.box.size');
            d3.xml(this.model.icon).mimeType("image/svg+xml").get(function (error, xml) {
                if (error) throw error;
                iconSVG = xml.getElementsByTagName("svg")[0];
                d3.select(iconSVG).attr("width", iconSize).attr("height", iconSize);
            });
            function cloneCallBack() {
                var div = view.createContainerForDraggable();
                div.node().appendChild(iconSVG);
                return div.node();
            }

            return cloneCallBack;
        }

    });

    return toolView;
});
