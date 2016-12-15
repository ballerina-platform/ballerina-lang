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
define(['log', 'lodash', 'jquery', 'd3', 'd3utils', './../visitors/ast-visitor', './ballerina-view','./message-manager'], function(log, _, $, d3, D3Utils, AstVisitor, BallerinaView,MessageManager){

    var Canvas = function(args) {
        args.messageManager = new MessageManager();
        BallerinaView.call(this, args);
    };

    Canvas.prototype = Object.create(BallerinaView.prototype);
    Canvas.prototype.constructor = Canvas;

    Canvas.prototype.getSVG = function () {
        return this._svg;
    };

    Canvas.prototype.getMainWrapper = function () {
        return this._mainSVGGroup;
    };

    Canvas.prototype.drawAccordionCanvas = function (parent, options, id, name, title) {
        var serviceContainer = $('<div><svg class="service-container"></svg></div>');
        serviceContainer.attr('id', id);
        serviceContainer.attr('name', name);
        serviceContainer.addClass(_.get(options, 'cssClass.outer_box'));
        var canvas = serviceContainer;
        this._serviceContainer = serviceContainer;

        //draw a collapse accordion
        var outerDiv = $('<div></div>');

        outerDiv.addClass(_.get(options, 'cssClass.outer_div'));
        var panelHeading = $('<div></div>');
        panelHeading.attr('id', canvas[0].id + 3).attr('role', 'tab');
        var panelTitle = $('<h4></h4>');
        panelTitle.addClass(_.get(options, 'cssClass.panel_title'));
        var panelIcon = $('<i></i>');
        panelIcon.addClass(_.get(options, 'cssClass.panel_icon'));
        if (canvas[0].getAttribute('name') == "service") {
            panelIcon.addClass(_.get(options, 'cssClass.service_icon'));
        } else if (canvas[0].getAttribute('name') == "connector") {
            panelIcon.addClass(_.get(options, 'cssClass.connector_icon'));
        } else if (canvas[0].getAttribute('name') == "function") {
            panelIcon.addClass(_.get(options, 'cssClass.function_icon'));
        }
        panelTitle.append(panelIcon);
        var titleLink = $('<a>' + canvas[0].getAttribute('name') + '</a>');
        if(title !== undefined) {
            titleLink.append("&nbsp;" + title);
        }
        titleLink.addClass(_.get(options, 'cssClass.title_link'));
        //TODO: update href,aria-controls
        titleLink.attr('role', 'button').attr('data-toggle', 'collapse').attr('data-parent', "#accordion").attr('href', '#' + canvas[0].id).attr('aria-expanded', 'false').attr('aria-controls', canvas[0].id);
        panelTitle.append(titleLink);

        var panelRightIcon = $('<i></i>');
        panelRightIcon.addClass(_.get(options, 'cssClass.panel_right_icon'));
        panelRightIcon.attr('role', 'button').attr('data-toggle', 'collapse').attr('data-parent', "#accordion").attr('href', '#' + canvas[0].id).attr('aria-expanded', 'false').attr('aria-controls', canvas[0].id);
        panelTitle.append(panelRightIcon);

        panelHeading.append(panelTitle);

        titleLink.click(function () {
            $(this).parent().find('i.right-icon-clickable').toggleClass('fw-down fw-up');
        });

        panelRightIcon.click(function () {
            $(this).toggleClass('fw-down fw-up');
        });

        var bodyDiv = $('<div></div>');
        bodyDiv.addClass(_.get(options, 'cssClass.body_div'));
        bodyDiv.attr('id', canvas[0].id).attr('aria-labelledby', canvas[0].id + 3).attr('role', 'tabpanel');
        bodyDiv.addClass(_.get(options, 'cssClass.canvas'));
        bodyDiv.append(canvas);

        outerDiv.append(panelHeading);
        outerDiv.append(bodyDiv);

        // append to parent
        parent.append(outerDiv);

        var self = this,
            dropActiveClass = _.get(options, 'cssClass.design_view_drop');
        outerDiv.mouseover(function(event){

            //if someone is dragging a tool from tool-palette
            if(self.toolPalette.dragDropManager.isOnDrag()){

                if(_.isEqual(self.toolPalette.dragDropManager.getActivatedDropTarget(), self)){
                    return;
                }

                // register this as a drop target and validate possible types of nodes to drop - second arg is a call back to validate
                // tool view will use this to provide feedback on impossible drop zones
                self.toolPalette.dragDropManager.setActivatedDropTarget(self._model, function(nodeBeingDragged){
                    return self._model.canBeParentOf(nodeBeingDragged) && nodeBeingDragged.canBeAChildOf(self._model);
                });

                // indicate drop area
                outerDiv.addClass(dropActiveClass);

                // reset ui feed back on drop target change
                self.toolPalette.dragDropManager.once("drop-target-changed", function(){
                    outerDiv.removeClass(dropActiveClass);
                });
            }
            event.stopPropagation();
        }).mouseout(function(event){
            // reset ui feed back on hover out
            if(self.toolPalette.dragDropManager.isOnDrag()){
                if(_.isEqual(self.toolPalette.dragDropManager.getActivatedDropTarget(), self._model)){
                    self.toolPalette.dragDropManager.clearActivatedDropTarget();
                }
            }
            event.stopPropagation();
        });
    };

    /**
     * Set service container height
     * @param newHeight
     */
    Canvas.prototype.setServiceContainerHeight = function (newHeight) {
        var canvas = _.first($(this._serviceContainer).children());
        canvas.setAttribute('height', newHeight);
    };

    return Canvas;

});