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

        toolTemplate: _.template("<div id=\"<%=id%>\" class=\"tool-block tool-container <%=classNames%>\"  " +
            "data-placement=\"bottom\" data-toggle=\"tooltip\" title='<%=title%>'> <img src=\"<%=icon%>\" " +
            "class=\"tool-image\"  /><p class=\"tool-title\"><%=title%></p></div>"),
        toolTemplateVertical: _.template("<div id=\"<%=id%>-tool\" class=\"tool-block tool-container-vertical " +
            "<%=classNames%>\"> <div class=\"tool-container-vertical-icon\" data-placement=\"bottom\" " +
            "data-toggle=\"tooltip\" title='<%=title%>'><img src=\"<%=icon%>\" class=\"tool-image\"  />" +
            "</div><div class=\"tool-container-vertical-title\" data-placement=\"bottom\" data-toggle=\"tooltip\" " +
            "title='<%=title%>'><%=title%></div><p class=\"tool-title\"><%=title%></p></div>"),

        initialize: function (options) {
            _.extend(this, _.pick(options, ["toolPalette"]));
            this._options = options;
            _.set(this, 'options.dragIcon.box.size', '60px');
        },

        createHandleDragStopEvent: function(){
            var toolView = this;
            return function (event, ui) {
                if(toolView.toolPalette.dragDropManager.isAtValidDropTarget()){
                    var indexForNewNode = toolView.toolPalette.dragDropManager.getDroppedNodeIndex();
                    if(indexForNewNode >= 0){
                        toolView.toolPalette.dragDropManager.getActivatedDropTarget()
                            .addChild(toolView.toolPalette.dragDropManager.getNodeBeingDragged(), indexForNewNode);
                    } else {
                        toolView.toolPalette.dragDropManager.getActivatedDropTarget()
                            .addChild(toolView.toolPalette.dragDropManager.getNodeBeingDragged());
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
                // Get the meta information/ arguments to create the particular tool
                var meta = toolView.model.get('meta') || {};
                toolView.toolPalette.dragDropManager.setNodeBeingDragged(toolView.model.nodeFactoryMethod(meta, true));
            }
        },

        render: function (parent, orderVertical) {
            var dragCursorOffset = _.isUndefined(this.model.dragCursorOffset) ?  { left: 30, top: -10 } : this.model.dragCursorOffset;
            this._dragCursorOffset = dragCursorOffset;
            var self = this;

            if(!_.isNil(this.model.attributes.seperator) && this.model.attributes.seperator ){
                parent.append("<div class='clear-fix '/><div class='tool-separator' />");
                return;
            }
            // setting id for the div
            this.$el.attr('id', this.model.id);
            if (orderVertical) {
                this.$el.html(this.toolTemplateVertical(this.model.attributes));
            } else {
                this.$el.html(this.toolTemplate(this.model.attributes));
            }

            if(!_.isNil(this.model.parent)){
                parent.find('#'+this.model.parent).after(this.$el);
            } else {
                parent.append(this.$el);
            }

            this.$el.find('.tool-block').tooltip();

            this.$el.draggable({
                helper: _.isUndefined(this.createCloneCallback) ?  'clone' : this.createCloneCallback(self),
                cursor: 'move',
                cursorAt: dragCursorOffset,
                containment: _.get(self._options, 'containment_element'),
                zIndex: 10001,
                stop: this.createHandleDragStopEvent(),
                start : this.createHandleDragStartEvent(),
                drag:this.createHandleOnDragEvent()
            });

            // registering id-modified event
            this.model.on('id-modified', this.updateToolId, this);
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
        },

        /**
         * updates tool id and change view attributes of the tool item
         * @param {string} id - id of the tool
         */
        updateToolId: function (id) {
            this.$el.find('.tool-container-vertical-title').text(id);
            this.$el.attr('id', id);
        }
    });

    return toolView;
});
