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

define(['log', 'jquery', 'd3', 'backbone', 'lodash', 'd3utils'], function (log, $, d3, Backbone, _, D3Utils) {

    var toolView = Backbone.View.extend({

        toolTemplate: _.template("<div id=\"<%=id%>\" class=\"tool-container\"  data-placement=\"bottom\" data-toggle=\"tooltip\" title='<%=title%>'> <img src=\"<%=icon%>\" class=\"tool-image\"  /><p class=\"tool-title\"><%=title%></p></div>"),

        initialize: function (options) {
            _.extend(this, _.pick(options, ["toolPalette"]));
            this._options = options;
            _.set(this, 'options.dragIcon.box.size', '60px');
        },

        createHandleDragStopEvent: function(){
            var toolView = this;
            return function (event, ui) {
                if(toolView.toolPalette.dragDropManager.isAtValidDropTarget()){
                    if(toolView.toolPalette.dragDropManager.getDroppedNodeIndex() >= 0){
                        toolView.toolPalette.dragDropManager.getActivatedDropTarget()
                            .addChild(toolView.toolPalette.dragDropManager.getTypeBeingDragged(),
                                toolView.toolPalette.dragDropManager.getDroppedNodeIndex());
                    } else {
                        toolView.toolPalette.dragDropManager.getActivatedDropTarget()
                            .addChild(toolView.toolPalette.dragDropManager.getTypeBeingDragged());
                    }
                }
                toolView.toolPalette.dragDropManager.reset();
                toolView._$disabledIcon = undefined;
                toolView._$draggedToolIcon = undefined;
            };
        },

        createHandleOnDragEvent : function(){
            var toolView = this;
            return function (event, ui) {
                if(!toolView.toolPalette.dragDropManager.isAtValidDropTarget()){
                    toolView._$disabledIcon.show();
                    toolView._$draggedToolIcon.css('opacity', 0.1);
                } else {
                    toolView._$disabledIcon.hide();
                    toolView._$draggedToolIcon.css('opacity', 1);
                }
            };
        },

        createHandleDragStartEvent : function(){
            var toolView = this;
            return function(event,ui){
                toolView.toolPalette.dragDropManager.setTypeBeingDragged(toolView.model.nodeFactoryMethod({}, true));
            }
        },

        render: function (parent) {
            var dragCursorOffset = _.isUndefined(this.model.dragCursorOffset) ?  { left: 30, top: -10 } : this.model.dragCursorOffset;
            this._dragCursorOffset = dragCursorOffset;
            var self = this;
            this.$el.html(this.toolTemplate(this.model.attributes));
            this.$el.tooltip();
            parent.append(this.$el);

            this.$el.draggable({
                helper: _.isUndefined(this.createCloneCallback) ?  'clone' : this.createCloneCallback(self),
                cursor: 'move',
                cursorAt: dragCursorOffset,
                zIndex: 10001,
                stop: this.createHandleDragStopEvent(),
                start : this.createHandleDragStartEvent(),
                drag:this.createHandleOnDragEvent()
            });

            return this;
        },

        createContainerForDraggable: function(){
            var body = d3.select("body");
            var div = body.append("div").attr("id", "draggingToolClone")
                        .classed(_.get(this._options, 'cssClass.dragContainer'), true);

            //For validation feedback
            var disabledIconDiv = div.append('div').classed(_.get(this._options, 'cssClass.disabledIconContainer'), true);
            disabledIconDiv.append('i').classed(_.get(this._options, 'cssClass.disabledIcon'), true);
            this._$disabledIcon = $(disabledIconDiv.node());
            this._$disabledIcon.css('top', this._dragCursorOffset.top + 20);
            this._$disabledIcon.css('left', this._dragCursorOffset.left - 10);
            return div;
        },

        createCloneCallback: function (view) {
            var iconSVG,
                self = this,
                iconSize = _.get(this, 'options.dragIcon.box.size');
            d3.xml(this.model.icon).mimeType("image/svg+xml").get(function (error, xml) {
                if (error) throw error;
                iconSVG = xml.getElementsByTagName("svg")[0];
                d3.select(iconSVG).attr("width", iconSize).attr("height", iconSize);
            });
            function cloneCallBack() {
                var div = view.createContainerForDraggable();
                div.node().appendChild(iconSVG);
                self._$draggedToolIcon = $(iconSVG);
                return div.node();
            }

            return cloneCallBack;
        }

    });

    return toolView;
});
