
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
define(['log', 'lodash', 'jquery', 'd3', 'd3utils', './../visitors/ast-visitor', './ballerina-view', './message-manager', 'mcustom_scroller'],
    function(log, _, $, d3, D3Utils, AstVisitor, BallerinaView, MessageManager, mcustomScroller){

    var Canvas = function(args) {
        var mMArgs = {'canvas': this};
        args.messageManager = new MessageManager(mMArgs);
        BallerinaView.call(this, args);

        /**
         * The <svg> element which is has all svg elements should be drawn on.
         * @type {SVGSVGElement}
         * @private
         */
        this._svg = undefined;

        /**
         * A wrapper SVG <g> element which resides inside {@link _svg}.
         * @type {SVGGElement}
         * @private
         */
        this._rootGroup = undefined;

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

        this._minHeight = 400;

        this.bindEvents();
    };

    Canvas.prototype = Object.create(BallerinaView.prototype);
    Canvas.prototype.constructor = Canvas;

    Canvas.prototype.bindEvents = function () {
        this.on('remove-view', this.removeViewCallback, this);
        this._model.on('child-removed', this.childRemovedCallback, this);
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
            class: _.get(options, 'cssClass.panel_right_icon')
        }).appendTo(this._canvasOperationsWrapper);

        $("<span class='pull-right canvas-operations-separator'>|</span>").appendTo(this._canvasOperationsWrapper);

        // Creating delete icon.
        var panelDeleteIcon = $("<i/>", {
            class: _.get(options, 'cssClass.panel_delete_icon')
        }).appendTo(this._canvasOperationsWrapper);

        $("<span class='pull-right canvas-operations-separator'>|</span>").appendTo(this._canvasOperationsWrapper);

        panelHeading.append(panelTitle);

        panelHeading.click({panelCollapsibleIcon: panelCollapsibleIcon}, function (event) {
            $(event.data.panelCollapsibleIcon).toggleClass('fw-down fw-up');
        });

        //// Creating the body of the canvas.

        // The wrapper for the body of the canvas.
        var bodyWrapper = $("<div/>", {
            id: id + "_body",
            class: _.get(options, "cssClass.canvas", "")
        }).appendTo(outerDiv);

        // Wrapper for the SVG
        var svgContainer = $("<div/>", {
            id: id,
            name: name,
            class: _.get(options, "cssClass.outer_box", "")
        }).appendTo(bodyWrapper);

        this._svg = $("<svg class='" + _.get(options, "cssClass.svg_container", "") + "'></svg>")
            .appendTo(svgContainer);

        this._rootGroup = D3Utils.group(d3.select(this._svg.get(0)));

        // Setting initial height of the SVG.
        this.setSVGHeight(this._minHeight);

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

            var child = self._model;
            var parent = child.parent;
            self.trigger("remove-view", parent, child);
        });

        $(svgContainer).mCustomScrollbar({
            theme: "dark",
            axis: "x",
            scrollInertia: 0,
            autoHideScrollbar: true,
            mouseWheel: {
                enable: false
            }
        });
    };

    /**
     * Set canvas container height
     * @param newHeight
     */
    Canvas.prototype.setSVGHeight = function (newHeight) {
        var dn = newHeight < this._minHeight ? this._minHeight : newHeight;
        this._svg.attr('height', dn);
        this.getBoundingBox().h(dn);

        // If service container's height is lesser than the height of the svg
        // Increase the height of the service container and the inner div
        if ($(this._container).closest("svg").attr('height')) {
            if ($(this._container).closest(".panel-body").height() < $(this._container).closest("svg").attr('height')) {
                $(this._container).closest(".panel-body").height($(this._container).closest("svg").attr("height"));
                $(this._container).closest(".panel-body").find("#" + $(this._container).closest(".panel-body")
                        .attr("id")).height($(this._container).closest("svg").attr('height'));
            }
        } else {
            if ($(this._container).height() < $(this._container).find('svg').attr('height')) {
                $(this._container).height($(this._container).find('svg').attr('height'));
                $(this._container).find("#" + $(this._container).attr('id')).height($(this._container).find('svg')
                    .attr('height'));
            }
        }
    };

    /**
     * Set canvas container width
     * @param {number} newWidth
     */
    Canvas.prototype.setSVGWidth = function (newWidth) {
        this._svg.attr('width', newWidth);
        this.getBoundingBox().w(newWidth);
        $(this._container).closest(".panel-body").find(".outer-box").mCustomScrollbar("update");
    };

    /**
     * Override the remove view callback
     * @param {ASTNode} parent - parent node
     * @param {ASTNode} child - child node
     */
    Canvas.prototype.removeViewCallback = function (parent, child) {
        $("#_" + this.getModel().getID()).remove();
        this.unplugView(
            {
                w: 0,
                h: 0
            }, parent, child);
    };

    Canvas.prototype.getSVG = function () {
        return this._svg;
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

    Canvas.prototype.getRootGroup = function () {
        return this._rootGroup;
    };

    return Canvas;

});