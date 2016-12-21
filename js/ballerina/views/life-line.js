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

define(['lodash', 'jquery', 'd3', 'log', 'd3utils', './point', './ballerina-view', 'property_pane_utils'], function (_, $, d3, log, D3Utils, Point, BallerinaView,  PropertyPaneUtils) {

    /**
     * View for a generic lifeline
     * @param args {object} - config
     * @param args.container {SVGGElement} - SVG group element to draw the life line
     * @param args.centerPoint {Point} - center point to draw the life line.
     * @param args.cssClass {object} - css classes for the lifeline
     * @param args.cssClass.group {string} - css class for root group
     * @param args.cssClass.title {string} - css class for the title
     * @param args.title {string} - title
     * @param args.rect {Object} - top and bottom rectangle properties
     * @param args.rect.width {number} - rect width
     * @param args.rect.height {number} - rect height
     * @param args.rect.round {number} - rx and ry
     * @param args.content {Object} - properties for content area
     * @param args.content.width {number} - width size
     * @param args.content.offsetX {number} - offset in X from top and bottom center points
     * @param args.content.offsetY {number} - offset from Y top and bottom center points
     *
     * @class LifeLineView
     * @augments EventChannel
     * @constructor
     */
    var LifeLineView = function (args) {
        BallerinaView.call(this, args);
        this._containerD3 = d3.select(this._container);
        this._viewOptions = args;
        this._topCenter = this._viewOptions.centerPoint.clone();

        _.set(this._viewOptions, 'title',  _.get(this._viewOptions, 'title', 'life-line'));
        _.set(this._viewOptions, 'rect.width', _.get(this._viewOptions, 'rect.width', 120));
        _.set(this._viewOptions, 'rect.height', _.get(this._viewOptions, 'rect.height', 30));
        _.set(this._viewOptions, 'rect.round', _.get(this._viewOptions, 'rect.round', 0));
        _.set(this._viewOptions, 'line.height', _.get(this._viewOptions, 'line.height', 240));
        _.set(this._viewOptions, 'content.width', _.get(this._viewOptions, 'content.width', 140));
        _.set(this._viewOptions, 'content.offset', _.get(this._viewOptions, 'content.offset', {top:50, bottom: 50}));
        _.set(this._viewOptions, 'cssClass.title', _.get(this._viewOptions, 'cssClass.title', 'life-line-title'));
        _.set(this._viewOptions, 'cssClass.group', _.get(this._viewOptions, 'cssClass.group', 'life-line'));

        this._bottomCenter = this._topCenter.clone().move(0, _.get(this._viewOptions, 'line.height' ));

        this._rootGroup = D3Utils.group(this._containerD3)
            .classed(_.get(this._viewOptions, 'cssClass.group'), true);

        this.getBoundingBox()
            .x(this._topCenter.x() -  _.get(this._viewOptions, 'rect.width')/2)
            .y(this._topCenter.y() +  _.get(this._viewOptions, 'rect.height')/2)
            .w(_.get(this._viewOptions, 'rect.width'))
            .h(_.get(this._viewOptions, 'line.height') + _.get(this._viewOptions, 'rect.width'));
    };

    LifeLineView.prototype = Object.create(BallerinaView.prototype);
    LifeLineView.prototype.constructor = LifeLineView;

    LifeLineView.prototype.position = function (x, y) {
        this._rootGroup.attr("transform", "translate(" + x + "," + y + ")");
    };

    LifeLineView.prototype.getMidPoint = function () {
        return this._topCenter.x();
    };

    LifeLineView.prototype.getMiddleLineCenter = function () {
        return new Point(this.getMidPoint(), (this._topCenter.y()+ this._bottomCenter.y())/2);
    };

    LifeLineView.prototype.getMiddleLineHeight = function () {
        return this._bottomCenter.y() - this._topCenter.y();
    };

    LifeLineView.prototype.width = function () {
        return this._topPolygon.attr('width');
    };

    LifeLineView.prototype._updateBoundingBox = function () {
        
    }

    LifeLineView.prototype.render = function () {
        this.renderMiddleLine();
        this.renderTopPolygon();
        this.renderBottomPolygon();
        this.renderTitle();
        this.renderMiddleRectangle();
        this.renderContentArea();
        return this;
    };

    LifeLineView.prototype.move = function (dx, dy) {
        this._bottomCenter.move(dx, dy);
        this._topCenter.move(dx, dy);
    };

    LifeLineView.prototype.increaseHeight = function (dy) {
        this._bottomCenter.move(0, dy);
    };

    LifeLineView.prototype.setHeight = function (height) {
        var newY = height - this._topCenter.y();
        this._bottomCenter.y(newY);
    };

    LifeLineView.prototype.getTopCenter = function () {
        return this._topCenter;
    };

    LifeLineView.prototype.getBottomCenter = function () {
        return this._bottomCenter;
    };

    LifeLineView.prototype.renderTopPolygon = function () {
        var self = this;
        this._topPolygon = D3Utils.centeredRect(this._topCenter,
            this._viewOptions.rect.width, this._viewOptions.rect.height, 0, 0, this._rootGroup);

        this._topCenter.on('moved', function (offset) {
            var x = parseFloat(self._topPolygon.attr('x'));
            var y = parseFloat(self._topPolygon.attr('y'));
            self._topPolygonText
                .attr('x', x + offset.dx)
                .attr('y', y + offset.dy)
        });
    };

    LifeLineView.prototype.renderBottomPolygon = function () {
        var self = this;
        this._bottomPolygon = D3Utils.centeredRect(this._bottomCenter,
            this._viewOptions.rect.width, this._viewOptions.rect.height, 0, 0, this._rootGroup);

        this._bottomCenter.on('moved', function (offset) {
            var x = parseFloat(self._bottomPolygon.attr('x'));
            var y = parseFloat(self._bottomPolygon.attr('y'));
            self._bottomPolygon
                .attr('x', x + offset.dx)
                .attr('y', y + offset.dy)
        });
    };

    LifeLineView.prototype.renderTitle = function(){
        var self = this;
        this._topPolygonText = D3Utils.centeredText(this._topCenter,
            this._viewOptions.title, this._rootGroup)
            .classed(this._viewOptions.cssClass.title, true).classed("genericT", true);

        this._topCenter.on('moved', function (offset) {
            var x = parseFloat(self._topPolygonText.attr('x'));
            var y = parseFloat(self._topPolygonText.attr('y'));
            self._topPolygonText
                .attr('x', x + offset.dx)
                .attr('y', y + offset.dy)
        });

        this._bottomPolygonText = D3Utils.centeredText(this._bottomCenter,
            this._viewOptions.title, this._rootGroup)
            .classed(this._viewOptions.cssClass.title, true).classed("genericT", true);

        this._bottomCenter.on('moved', function (offset) {
            var x = parseFloat(self._bottomPolygonText.attr('x'));
            var y = parseFloat(self._bottomPolygonText.attr('y'));
            self._bottomPolygonText
                .attr('x', x + offset.dx)
                .attr('y', y + offset.dy)
        });

    };

    LifeLineView.prototype.renderMiddleLine = function () {
        var self = this;
        this._middleLine = D3Utils.lineFromPoints(this._topCenter, this._bottomCenter, this._rootGroup);

        this._topCenter.on('moved', function (offset) {
            var x1 = parseFloat(self._middleLine.attr('x1'));
            var y1 = parseFloat(self._middleLine.attr('y1'));
            self._middleLine
                .attr('x1', x1 + offset.dx)
                .attr('y1', y1 + offset.dy)
        });

        this._bottomCenter.on('moved', function (offset) {
            var x2 = parseFloat(self._middleLine.attr('x2'));
            var y2 = parseFloat(self._middleLine.attr('y2'));
            self._middleLine
                .attr('x2', x2 + offset.dx)
                .attr('y2', y2 + offset.dy)
        });

    };

    LifeLineView.prototype.renderMiddleRectangle = function () {};

    LifeLineView.prototype.renderContentArea = function () {
        this._contentArea = D3Utils.group(this._rootGroup);
    };

    LifeLineView.prototype.getContentArea = function () {
        return this._contentArea;
    };

    LifeLineView.prototype.getContentOffset = function () {
        return _.get(this._viewOptions, 'content.offset');
    };

    LifeLineView.prototype.createPropertyPane = function (args) {
        var model = _.get(args, "model", {});
        var viewOptions = _.get(args, "viewOptions", {});
        var lifeLineGroup = _.get(args, "lifeLineGroup", null);
        var editableProperties = _.get(args, "editableProperties", []);

        viewOptions.actionButton = _.get(args, "viewOptions.actionButton", {});
        viewOptions.actionButton.class = _.get(args, "actionButton.class", "property-pane-action-button");
        viewOptions.actionButton.wrapper = _.get(args, "actionButton.wrapper", {});
        viewOptions.actionButton.wrapper.class = _.get(args, "actionButton.wrapper.class", "property-pane-action-button-wrapper");
        viewOptions.actionButton.editClass = _.get(args, "viewOptions.actionButton.editClass", "property-pane-action-button-edit");
        viewOptions.actionButton.disableClass = _.get(args, "viewOptions.actionButton.disableClass", "property-pane-action-button-disable");
        viewOptions.actionButton.deleteClass = _.get(args, "viewOptions.actionButton.deleteClass", "property-pane-action-button-delete");

        viewOptions.actionButton.width = _.get(args, "viewOptions.action.button.width", 22);
        viewOptions.actionButton.height = _.get(args, "viewOptions.action.button.height", 22);

        viewOptions.propertyForm = _.get(args, "propertyForm", {});
        viewOptions.propertyForm.wrapper = _.get(args, "propertyForm.wrapper", {});
        viewOptions.propertyForm.wrapper.class = _.get(args, "propertyForm.wrapper", "property-pane-form-wrapper");
        viewOptions.propertyForm.heading = _.get(args, "propertyForm.heading", {});
        viewOptions.propertyForm.heading.class = _.get(args, "propertyForm.heading.class", "property-pane-form-heading");
        viewOptions.propertyForm.heading.iconClass = _.get(args, "propertyForm.heading.class", "property-pane-form-heading-icon");
        viewOptions.propertyForm.heading.textClass = _.get(args, "propertyForm.heading.class", "property-pane-form-heading-text");
        viewOptions.propertyForm.heading.iconCloseClass = _.get(args, "propertyForm.heading.class", "property-pane-form-heading-close-icon");
        viewOptions.propertyForm.body = _.get(args, "propertyForm.body", {});
        viewOptions.propertyForm.body.class = _.get(args, "propertyForm.body.class", "property-pane-form-body");
        viewOptions.propertyForm.body.property = _.get(args, "propertyForm.body.property", {});
        viewOptions.propertyForm.body.property.wrapper = _.get(args, "propertyForm.body.property.wrapper", "property-pane-form-body-property-wrapper");

        viewOptions.propertyForm.body.addStatement = _.get(args, "propertyForm.body.addStatement", {});
        viewOptions.propertyForm.body.addStatement.text = _.get(args, "propertyForm.body.addStatement.text", "Add");
        viewOptions.propertyForm.body.addStatement.class = _.get(args, "propertyForm.body.addStatement.class", "property-pane-form-body-add-button");

        var self = this;
        // Adding click event for 'life-line' group.
        $(lifeLineGroup.node()).click(function (lifeLineView, event) {

            log.debug("Clicked life-line group");

            event.stopPropagation();

            // Not allowing to click the statement group multiple times.
            if ($("." + viewOptions.actionButton.wrapper.class).length > 0) {
                log.debug("life-line group is already clicked");
                return;
            }

            // Calculating width for edit and delete button.
            var propertyButtonPaneRectWidth = viewOptions.actionButton.width * 2;

            // Creating an SVG group for the edit and delete buttons.
            var propertyButtonPaneGroup = D3Utils.group(lifeLineGroup);

            // Adding svg definitions needed for styling edit and delete buttons.
            var svgDefinitions = propertyButtonPaneGroup.append("defs");
            var editButtonPattern = svgDefinitions.append("pattern")
                .attr("id", "editIcon")
                .attr("width", "100%")
                .attr("height", "100%");

            editButtonPattern.append("image")
                .attr("xlink:href", "images/edit.svg")
                .attr("x", (viewOptions.actionButton.width / 2) - (14 / 2))
                .attr("y", (viewOptions.actionButton.height / 2) - (14 / 2))
                .attr("width", "14")
                .attr("height", "14");

            var deleteButtonPattern = svgDefinitions.append("pattern")
                .attr("id", "deleteIcon")
                .attr("width", "100%")
                .attr("height", "100%");

            deleteButtonPattern.append("image")
                .attr("xlink:href", "images/delete.svg")
                .attr("x", (viewOptions.actionButton.width / 2) - (14 / 2))
                .attr("y", (viewOptions.actionButton.height / 2) - (14 / 2))
                .attr("width", "14")
                .attr("height", "14");

            // Bottom center point.
            var centerPointX = lifeLineView._bottomCenter._x;
            var centerPointY = lifeLineView._bottomCenter._y + 15;

            var smallArrowPoints =
                // Bottom point of the polygon.
                " " + centerPointX + "," + centerPointY +
                // Left point of the polygon
                " " + (centerPointX - 3) + "," + (centerPointY + 3) +
                // Right point of the polygon.
                " " + (centerPointX + 3) + "," + (centerPointY + 3);

            var smallArrow = D3Utils.polygon(smallArrowPoints, lifeLineGroup);

            // Creating the action button pane border.
            var propertyButtonPaneRect = D3Utils.rect(centerPointX - (propertyButtonPaneRectWidth / 2), centerPointY + 3,
                propertyButtonPaneRectWidth, viewOptions.actionButton.height, 0, 0, propertyButtonPaneGroup)
                .classed(viewOptions.actionButton.wrapper.class, true);

            // Not allowing to click background elements.
            $(propertyButtonPaneRect.node()).click(function(event){
                event.stopPropagation();
            });

            // Creating the edit action button.
            var editButtonRect = D3Utils.rect(centerPointX - (propertyButtonPaneRectWidth / 2), centerPointY + 3,
                viewOptions.actionButton.width, viewOptions.actionButton.height, 0, 0, propertyButtonPaneGroup)
                .classed(viewOptions.actionButton.class, true).classed(viewOptions.actionButton.editClass, true);

            // Creating the delete action button.
            var deleteButtonRect = D3Utils.rect(centerPointX + viewOptions.actionButton.width  - (propertyButtonPaneRectWidth / 2), centerPointY + 3,
                viewOptions.actionButton.width, viewOptions.actionButton.height, 0, 0, propertyButtonPaneGroup)
                .classed(viewOptions.actionButton.class, true).classed(viewOptions.actionButton.deleteClass, true);

            // When the outside of the propertyButtonPaneRect is clicked.
            $(window).click(function (event) {
                log.debug("window click");
                $(propertyButtonPaneGroup.node()).remove();
                $(smallArrow.node()).remove();

                // Remove this handler.
                $(this).unbind("click");
            });

            // Adding on click event for edit button.
            $(editButtonRect.node()).click(function (event) {

                log.debug("Clicked edit button");

                var parentSVG = propertyButtonPaneGroup.node().ownerSVGElement;

                event.stopPropagation();

                // Hiding property button pane.
                $(propertyButtonPaneGroup.node()).remove();

                // 175 is the width set in css
                var propertyPaneWrapper = $("<div/>", {
                    class: viewOptions.propertyForm.wrapper.class /*+ " nano"*/,
                    css : {
                        "margin": (parseInt($(parentSVG.parentElement).css("padding"), 10) + 3) + "px"
                    },
                    click : function(event){
                        event.stopPropagation();
                    }
                }).offset({ top: centerPointY, left: centerPointX - (175 / 2)}).appendTo(parentSVG.parentElement);

                // When the outside of the propertyPaneWrapper is clicked.
                $(window).click(function (event) {
                    log.debug("window click");
                    closeAllPopUps();
                });

                var propertyPaneHeading = $("<div/>", {
                    "class": viewOptions.propertyForm.heading.class
                }).appendTo(propertyPaneWrapper);

                var editIcon = $("<i/>", {
                    "class": "fw fw-edit " + viewOptions.propertyForm.heading.iconClass
                }).appendTo(propertyPaneHeading);

                var editTitle = $("<span class='" + viewOptions.propertyForm.heading.textClass +"'>Edit</span>")
                    .appendTo(propertyPaneHeading);

                var closeIcon = $("<i/>", {
                    "class": "fw fw-cancel " + viewOptions.propertyForm.heading.iconCloseClass
                }).appendTo(propertyPaneHeading);

                // When the "X" button is clicked.
                $(closeIcon).click(function () {
                    closeAllPopUps();
                });

                // Div which contains the form for the properties.
                var propertyPaneBody = $("<div/>", {
                    "class": viewOptions.propertyForm.body.class /*+ " nano-content"*/
                }).appendTo(propertyPaneWrapper);

                // Creating the property form.
                PropertyPaneUtils.createPropertyForm(propertyPaneBody,
                    viewOptions.propertyForm.body.property.wrapper, editableProperties);

                // FIXME: Removing the add button temporarily
                // Adding "Add" button.
                // var buttonPane = $("<div/>").appendTo(propertyPaneBody);
                // var addButton = $("<button/>", {
                //     class : viewOptions.propertyForm.body.addStatement.class,
                //     text : viewOptions.propertyForm.body.addStatement.text
                //
                // }).appendTo(buttonPane);

                // $(addButton).click(function (event) {
                //     //TODO: Add for connector declaration
                // });

                // Close the popups of property pane body.
                function closeAllPopUps() {
                    $(propertyPaneWrapper).remove();

                    // Remove the small arrow.
                    $(smallArrow.node()).remove();

                    $(this).unbind('click');
                }

            });

            $(deleteButtonRect.node()).click(function(event){
                log.info("Clicked delete button");

                event.stopPropagation();

                // Hiding property button pane.
                $(propertyButtonPaneGroup.node()).remove();
                $(smallArrow.node()).remove();

                var child = model;
                var parent = child.parent;
                parent.removeChild(child);
            });

        }.bind(lifeLineGroup.node(), this));
    };


    return LifeLineView;
});