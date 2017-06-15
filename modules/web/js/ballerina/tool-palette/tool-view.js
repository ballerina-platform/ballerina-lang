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

import log from 'log';
import $ from 'jquery';
import * as d3 from 'd3';
import Backbone from 'backbone';
import _ from 'lodash';
import D3Utils from 'd3utils';

const toolView = Backbone.View.extend({

    toolTemplate: _.template('<div id="<%=id%>" class="tool-block tool-container <%=classNames%>"  ' +
            "data-placement=\"bottom\" data-container=\"body\" data-template=\"<div class='tooltip tool-palette' role='tooltip'><div class='tooltip-arrow'></div><div class='tooltip-inner'></div></div>\" data-toggle=\"tooltip\" title='<%=title%>'> <i class=\"<%=cssClass%>\"></i> " +
            '<span class="tool-title-wrap" ><p class="tool-title"><%=name%></p></span></div>'),
    toolTemplateVertical: _.template('<div id="<%=id%>-tool" class="tool-block tool-container-vertical ' +
            '<%=classNames%>"> <div class="tool-container-vertical-icon" data-placement="bottom" ' +
            "data-toggle=\"tooltip\" title='<%=title%>'> <img src=\"<%=icon.getAttribute(\"src\")%>\" class=\"tool-image\"  />" +
            '</div><div class="tool-container-vertical-title" data-placement="bottom" data-toggle="tooltip" ' +
            "title='<%=title%>'><%=title%></div><p class=\"tool-title\"><%=title%></p></div>"),

    initialize(options) {
        _.extend(this, _.pick(options, ['toolPalette']));
        this._options = options;
        _.set(this, 'options.dragIcon.box.size', '60px');
    },

    createHandleDragStopEvent() {
        const toolView = this;
        return function (event, ui) {
            if (toolView.toolPalette.dragDropManager.isAtValidDropTarget()) {
                const indexForNewNode = toolView.toolPalette.dragDropManager.getDroppedNodeIndex();
                const nodeBeingDragged = toolView.toolPalette.dragDropManager.getNodeBeingDragged();
                if (indexForNewNode >= 0) {
                    toolView.toolPalette.dragDropManager.getActivatedDropTarget()
                            .addChild(nodeBeingDragged, indexForNewNode, false, false, true);
                } else {
                    toolView.toolPalette.dragDropManager.getActivatedDropTarget()
                            .addChild(nodeBeingDragged, undefined, false, false, true);
                }
            }
            toolView.toolPalette.dragDropManager.reset();
            toolView._$disabledIcon = undefined;
            toolView._$draggedToolIcon = undefined;
        };
    },

    createHandleOnDragEvent() {
        const toolView = this;
        return function (event, ui) {
            if (!toolView.toolPalette.dragDropManager.isAtValidDropTarget()) {
                toolView._$disabledIcon.show();
                toolView._$draggedToolIcon.css('opacity', 0.1);
            } else {
                toolView._$disabledIcon.hide();
                toolView._$draggedToolIcon.css('opacity', 1);
            }
        };
    },

    createHandleDragStartEvent() {
        const toolView = this;
        return function (event, ui) {
            // Get the meta information/ arguments to create the particular tool
            const meta = toolView.model.get('meta') || {};
            toolView.toolPalette.dragDropManager.setNodeBeingDragged(toolView.model.nodeFactoryMethod(meta, true));
        };
    },

    render(parent, orderVertical) {
        const dragCursorOffset = _.isUndefined(this.model.dragCursorOffset) ? { left: 30, top: -10 } : this.model.dragCursorOffset;
        this._dragCursorOffset = dragCursorOffset;
        const self = this;

        if (!_.isNil(this.model.attributes.seperator) && this.model.attributes.seperator) {
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

        if (!_.isNil(this.model.parent)) {
            parent.find(`#${this.model.parent}`).after(this.$el);
        } else {
            parent.append(this.$el);
        }

        this.$el.find('.tool-block').tooltip();

        this.$el.draggable({
            helper: 'clone',
            cursor: 'move',
            cursorAt: dragCursorOffset,
            containment: _.get(self._options, 'containment_element'),
            zIndex: 10001,
            stop: this.createHandleDragStopEvent(),
            start: this.createHandleDragStartEvent(),
            drag: this.createHandleOnDragEvent(),
        });

        // registering id-modified event
        this.model.on('id-modified', this.updateToolId, this);
        return this;
    },

    createContainerForDraggable() {
        const body = d3.select('body');
        const div = body.append('div').attr('id', 'draggingToolClone')
                        .classed(_.get(this._options, 'cssClass.dragContainer'), true);

        // For validation feedback
        const disabledIconDiv = div.append('div').classed(_.get(this._options, 'cssClass.disabledIconContainer'), true);
        disabledIconDiv.append('i').classed(_.get(this._options, 'cssClass.disabledIcon'), true);
        this._$disabledIcon = $(disabledIconDiv.node());
        this._$disabledIcon.css('top', this._dragCursorOffset.top + 20);
        this._$disabledIcon.css('left', this._dragCursorOffset.left - 10);
        return div;
    },

    createCloneCallback(view) {
        const icon = this.model.icon;
        var self = this;
        const iconSize = _.get(this, 'options.dragIcon.box.size');
        var self = this;
        d3.select(icon).attr('width', iconSize).attr('height', iconSize);
        function cloneCallBack() {
            const div = view.createContainerForDraggable();
            div.node().appendChild(icon);
            self._$draggedToolIcon = $(icon);
            return div.node();
        }

        return cloneCallBack;
    },

    /**
     * updates tool id and change view attributes of the tool item
     * @param {string} id - id of the tool
     */
    updateToolId(id) {
        this.$el.find('.tool-container-vertical-title').text(id);
        this.$el.attr('id', id);
    },
});

export default toolView;
