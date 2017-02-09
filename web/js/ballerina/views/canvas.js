
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
define(['log', 'lodash', 'jquery', 'd3', 'd3utils', './../visitors/ast-visitor', './ballerina-view', './message-manager'],
    function(log, _, $, d3, D3Utils, AstVisitor, BallerinaView, MessageManager){

    /**
        * Generic class for a canvas. i.e Services, Functions.
        * @param {Object} args={} - Argument for a canvas.
        * @constructor
        * @augments BallerinaView
        */
    var Canvas = function(args) {
        var mMArgs = {'canvas': this};
        args.messageManager = new MessageManager(mMArgs);
        BallerinaView.call(this, args);

        /**
         * The icon of the icon position at top left corner.
         * @type {HTMLElement}
         * @private
         */
        this._panelIcon = undefined;

        /**
         * The title of the canvas. This is editable.
         * @type {HTMLAnchorElement}
         * @private
         */
        this._titleLink = undefined;

        /**
         * The wrapper which contains the right top corner operations pane.
         * @type {HTMLDivElement}
         * @private
         */
        this._canvasOperationsWrapper = undefined;

        /**
         * Gets the body wrapper.
         * @type {HTMLDivElement}
         * @private
         */
        this._bodyWrapper = undefined;

        this.bindEvents();
    };

    Canvas.prototype = Object.create(BallerinaView.prototype);
    Canvas.prototype.constructor = Canvas;

    Canvas.prototype.bindEvents = function () {
        this._model.on('child-removed', this.childRemovedCallback, this);
        this._model.on('before-remove', this.onBeforeModelRemove, this);
    };

    /**
     * Since canvas by default init a drop area within content area. Hence, all the subclasses need a way to override
     * allowed type of node to drop there.
     * This method is to achieve that extensibility. Override this in subclasses to allow only a certain type of nodes
     * to drop.
     *
     * @param node {ASTNode} node which is being dragged ATM
     * @return {boolean}
     */
    Canvas.prototype.isAValidNodeForCanvasDropArea = function (node) {
        return true;
    };

    /**
     * Draws the main body of the model
     * @param {Object} options - Options for modifying the canvas
     * @param {string} id - The ID of the model.
     * @param {string} name - The type of model.
     * @param {string} title - The identifier of the model.
     */
    Canvas.prototype.drawAccordionCanvas = function (options, id, name, title) {
        // The main wrapper of the canvas.
        var outerDiv = $("<div/>", {
            id: "_" + id,
            class: _.get(options, "cssClass.outer_div", "")
        }).appendTo(this.getContainer());

        //// Creating the heading of the canvas.

        // Creating the wrapper for the heading.
        var panelHeading = $("<div/>", {
            id: id + "_heading",
            "data-toggle": "collapse",
            "data-target": "#" + id + "_body",
            class: _.get(options, "cssClass.head_div", "")
        }).appendTo(outerDiv);

        // The title element of the heading.
        var panelTitle = $("<h4/>", {
            class: _.get(options, "cssClass.panel_title", "")
        }).appendTo(panelHeading);

        // The icon of the canvas positioned in the heading.
        this._panelIcon = $("<i/>", {
            class: _.get(options, "cssClass.panel_icon", "")
        }).appendTo(panelTitle);

        this._titleLink = $("<a/>", {
            class: _.get(options, "cssClass.title_link", ""),
            "contenteditable": "true",
            "spellcheck": "false",
            text: _.isUndefined(title) ? "" : title
        }).appendTo(panelTitle);

        this._canvasOperationsWrapper = $("<div class='canvas-operations-wrapper'/>").appendTo(panelTitle);

        // Creating collapsable icon.
        var panelCollapsibleIcon = $("<i/>", {
            class: _.get(options, 'cssClass.panel_right_icon'),
            title:"Collapse Pane"
        }).appendTo(this._canvasOperationsWrapper).tooltip();

        $("<span class='pull-right canvas-operations-separator'>|</span>").appendTo(this._canvasOperationsWrapper);

        // Creating delete icon.
        var panelDeleteIcon = $("<i/>", {
            class: _.get(options, 'cssClass.panel_delete_icon'),
            title:"Delete"
        }).appendTo(this._canvasOperationsWrapper).tooltip();

        $("<span class='pull-right canvas-operations-separator'>|</span>").appendTo(this._canvasOperationsWrapper);

        panelHeading.append(panelTitle);

        panelHeading.click({panelCollapsibleIcon: panelCollapsibleIcon}, function (event) {
            $(event.data.panelCollapsibleIcon).toggleClass('fw-down fw-up');
        });

        //// Creating the body of the canvas.

        // The wrapper for the body of the canvas.
        var bodyContainer = $("<div/>", {
            id: id + "_body",
            class: _.get(options, "cssClass.canvas", "")
        }).appendTo(outerDiv);

        this._bodyWrapper = $("<div/>", {
            id: id,
            name: name,
            class: _.get(options, "cssClass.outer_box", "")
        }).appendTo(bodyContainer);

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
                    return self.isAValidNodeForCanvasDropArea(nodeBeingDragged);
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

        panelDeleteIcon.click(function (event) {
            log.debug("Clicked delete button");

            event.stopPropagation();
            self._model.remove();
        });
    };

    /**
     * Override the remove view callback
     */
    Canvas.prototype.onBeforeModelRemove = function () {
        $("#_" + this.getModel().getID()).remove();
        // resize the bounding box in order to the other objects to resize
        this.getBoundingBox().h(0).w(0);
    };

    Canvas.prototype.getOperationsPane = function () {
        return this._canvasOperationsWrapper;
    };

    Canvas.prototype.getPanelIcon = function () {
        return this._panelIcon;
    };

    Canvas.prototype.getTitle = function () {
        return this._titleLink;
    };

    Canvas.prototype.getBodyWrapper = function() {
        return this._bodyWrapper;
    };

    return Canvas;

});