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

define(['lodash', 'jquery', 'd3', 'log', 'd3utils', './point', './ballerina-view',
        'property_pane_utils', 'expression_editor_utils'],
    function (_, $, d3, log, D3Utils, Point, BallerinaView,  PropertyPaneUtils, expressionEditor) {

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
        _.set(this._viewOptions, 'cssClass.topPolygon', _.get(this._viewOptions, 'cssClass.topPolygon', 'connector-life-line-top-polygon'));
        _.set(this._viewOptions, 'cssClass.bottomPolygon', _.get(this._viewOptions, 'cssClass.bottomPolygon', 'connector-life-line-bottom-polygon'));
        _.set(this._viewOptions, 'onDeleteMoveOffset', _.get(this._viewOptions, 'onDeleteMoveOffset', -180));

        this._bottomCenter = this._topCenter.clone().move(0, _.get(this._viewOptions, 'line.height' ));

        this._rootGroup = D3Utils.group(this._containerD3)
            .classed(_.get(this._viewOptions, 'cssClass.group'), true);
        // For the lifelines such as client life line we do not have a model id, although it is valid
        if (!_.isNil(this._model)) {
            this._rootGroup.attr('id', "_" + this._model.id);
        }

        this.getBoundingBox()
            .x(this._topCenter.x() -  _.get(this._viewOptions, 'rect.width')/2)
            .y(this._topCenter.y() +  _.get(this._viewOptions, 'rect.height')/2)
            .w(_.get(this._viewOptions, 'rect.width'))
            .h(_.get(this._viewOptions, 'line.height') + _.get(this._viewOptions, 'rect.height'));

        this._editableProperties = [];
    };

    LifeLineView.prototype = Object.create(BallerinaView.prototype);
    LifeLineView.prototype.constructor = LifeLineView;

    /**
     * Override remove view callback
     * @param {ASTNode} parent - parent node
     * @param {ASTNode} child - child node
     */
    LifeLineView.prototype.onBeforeModelRemove = function (parent, child) {
        d3.select("#_" +this._model.id).remove();
        this.getBoundingBox().move(_.get(this._viewOptions, 'onDeleteMoveOffset'), 0).h(0);
    };

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

    };

    LifeLineView.prototype.render = function () {
        var self = this;
        this.renderMiddleLine();
        this.renderTopPolygon();
        this.renderBottomPolygon();
        this.renderTitle();
        this.renderMiddleRectangle();
        this.renderContentArea();
        this.listenTo(this._model, 'update-property-text', this.updateTitleText);
        // When the center of the bounding box moves, we need to move the life line as well.
        this.getBoundingBox().on('center-x-moved', function (dx) {
            self.move(dx, 0);
        });
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
            this._viewOptions.rect.width, this._viewOptions.rect.height, 0, 0, this._rootGroup).classed(_.get(this._viewOptions, 'cssClass.topPolygon'), true);

        this._topCenter.on('moved', function (offset) {
            var x = parseFloat(self._topPolygon.attr('x'));
            var y = parseFloat(self._topPolygon.attr('y'));
            self._topPolygon
                .attr('x', x + offset.dx)
                .attr('y', y + offset.dy)
        });
    };

    LifeLineView.prototype.renderBottomPolygon = function () {
        var self = this;
        this._bottomPolygon = D3Utils.centeredRect(this._bottomCenter,
            this._viewOptions.rect.width, this._viewOptions.rect.height, 0, 0, this._rootGroup).classed(_.get(this._viewOptions, 'cssClass.bottomPolygon'), true);

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
        var titleText = ((this._viewOptions.title.length) > 14 ? (this._viewOptions.title.substring(0,11) + '...') : this._viewOptions.title);
        this._topPolygonText = D3Utils.centeredText(this._topCenter,
            titleText, this._rootGroup)
            .classed(this._viewOptions.cssClass.title, true).classed("genericT", true);

        this._topCenter.on('moved', function (offset) {
            var x = parseFloat(self._topPolygonText.attr('x'));
            var y = parseFloat(self._topPolygonText.attr('y'));
            self._topPolygonText
                .attr('x', x + offset.dx)
                .attr('y', y + offset.dy)
        });

        this._bottomPolygonText = D3Utils.centeredText(this._bottomCenter,
            titleText, this._rootGroup)
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
        this._editableProperties = _.get(args, "editableProperties", []);

        viewOptions.actionButton = _.get(args, "viewOptions.actionButton", {});
        viewOptions.actionButton.class = _.get(args, "actionButton.class", "property-pane-action-button");
        viewOptions.actionButton.wrapper = _.get(args, "actionButton.wrapper", {});
        viewOptions.actionButton.wrapper.class = _.get(args, "actionButton.wrapper.class", "property-pane-action-button-wrapper");
        viewOptions.actionButton.disableClass = _.get(args, "viewOptions.actionButton.disableClass", "property-pane-action-button-disable");
        viewOptions.actionButton.deleteClass = _.get(args, "viewOptions.actionButton.deleteClass", "property-pane-action-button-delete");

        viewOptions.actionButton.width = _.get(args, "viewOptions.action.button.width", 22);
        viewOptions.actionButton.height = _.get(args, "viewOptions.action.button.height", 22);

        viewOptions.propertyForm = _.get(args, "propertyForm", {});
        viewOptions.propertyForm.wrapper = _.get(args, "propertyForm.wrapper", {});
        viewOptions.propertyForm.wrapper.class = _.get(args, "propertyForm.wrapper", "expression-editor-form-wrapper");
        viewOptions.propertyForm.heading = _.get(args, "propertyForm.heading", {});
        viewOptions.propertyForm.body = _.get(args, "propertyForm.body", {});
        viewOptions.propertyForm.body.class = _.get(args, "propertyForm.body.class", "expression-editor-form-body");
        viewOptions.propertyForm.body.property = _.get(args, "propertyForm.body.property", {});
        viewOptions.propertyForm.body.property.wrapper = _.get(args, "propertyForm.body.property.wrapper",
            "expression-editor-form-body-property-wrapper");

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

            // Get the bounding box of life line.
            var lifeLineBoundingBox = lifeLineView.getBoundingBox();

            // Calculating width for edit and delete button.
            var propertyButtonPaneRectWidth = viewOptions.actionButton.width * 2;

            // Creating an SVG group for the edit and delete buttons.
            var propertyButtonPaneGroup = D3Utils.group(lifeLineGroup);

            // Delete button pane group
            var deleteButtonPaneGroup = D3Utils.group(lifeLineGroup);

            // Adding svg definitions needed for styling edit and delete buttons.
            var svgDefinitions = deleteButtonPaneGroup.append("defs");
            var deleteButtonPattern = svgDefinitions.append("pattern")
                .attr("id", "deleteIcon")
                .attr("width", "100%")
                .attr("height", "100%");

            deleteButtonPattern.append("image")
                .attr("xlink:href", "images/delete.svg")
                .attr("x", (viewOptions.actionButton.width) - (14 / 2)) // Increasing the x so the delete button would be in middle
                .attr("y", (viewOptions.actionButton.height / 2) - (14 / 2))
                .attr("width", "14")
                .attr("height", "14");

            // Bottom center point.
            var centerPointX = lifeLineView._topCenter._x;
            var centerPointY = lifeLineView._topCenter._y + 15;

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
                propertyButtonPaneRectWidth, viewOptions.actionButton.height, 0, 0, deleteButtonPaneGroup)
                .classed(viewOptions.actionButton.wrapper.class, true);

            // Not allowing to click background elements.
            $(propertyButtonPaneRect.node()).click(function(event){
                event.stopPropagation();
            });

            // Creating the delete action button.
            var deleteButtonRect = D3Utils.rect(centerPointX - (propertyButtonPaneRectWidth / 2), centerPointY + 3,
                propertyButtonPaneRectWidth, viewOptions.actionButton.height, 0, 0, deleteButtonPaneGroup)
                .classed(viewOptions.actionButton.class, true).classed(viewOptions.actionButton.deleteClass, true);

            // When the outside of the propertyButtonPaneRect is clicked.
            $(window).click(function (event) {
                log.debug("window click");
                $(propertyButtonPaneGroup.node()).remove();
                $(deleteButtonPaneGroup.node()).remove();
                $(smallArrow.node()).remove();

                // Remove this handler.
                $(this).unbind("click");
            });

            var parentSVG = propertyButtonPaneGroup.node().ownerSVGElement;

            event.stopPropagation();
            
            // Hiding property button pane.
            $(propertyButtonPaneGroup.node()).remove();
            var propertyPaneWrapper = $("<div/>", {
                class: viewOptions.propertyForm.wrapper.class,
                css: {
                    "width": (lifeLineBoundingBox.w() + 1), // Making the text box bit bigger
                    "height": _.get(self._viewOptions, 'rect.height') + 2 // Make the expression editor bit bigger
                },
                click: function (event) {
                    event.stopPropagation();
                }
            }).appendTo(parentSVG.parentElement);

            // When the outside of the propertyPaneWrapper is clicked.
            $(window).click(function (event) {
                log.debug("window click");
                closeAllPopUps();
            });

            // Div which contains the form for the properties.
            var propertyPaneBody = $("<div/>", {
                "class": viewOptions.propertyForm.body.class
            }).appendTo(propertyPaneWrapper);

            // Creating the property form.
            expressionEditor.createEditor(propertyPaneBody,
                viewOptions.propertyForm.body.property.wrapper, self._editableProperties, closeAllPopUps);

            //Calculating the position of the text box
            var windowWidth = $('.svg-container').width();
            var textBoxWidth = $('input', propertyPaneWrapper).width();
            var textBoxHeight = $('input', propertyPaneWrapper).height();
            var textBoxX = self._topCenter.x() - textBoxWidth/2;

            //Check if the text box going outside of the window and set it's position correctly
            if(windowWidth < self._topCenter.x() + textBoxWidth/2){
                textBoxX = self._topCenter.x() - textBoxWidth + self._boundingBox._w/2 ;
            }

            //Set the position of the text box wrapper
            propertyPaneWrapper.css({
                top: self._topCenter.y() - (textBoxHeight / 2) - 1 + "px", // Get the pane to match connector's y.
                left: textBoxX + "px" // Get the pane to match connector's x
            });

            //$('#edit-overlay').show();

            // Close the popups of property pane body.
            function closeAllPopUps() {
                $(propertyPaneWrapper).remove();
                $(deleteButtonPaneGroup.node()).remove();

                // Remove the small arrow.
                $(smallArrow.node()).remove();
                //$('#edit-overlay').hide();

                $(this).unbind('click');
            }

            $(deleteButtonRect.node()).click(function(event){
                event.stopPropagation();
                model.remove();

                _.each(model._connectorActionsReference, function(actionInvocationView){
                    //Remove connected arrows
                    d3.select(actionInvocationView._arrowGroup.node()).remove();
                    //Enable arrow redraw point on action invocation
                    d3.select(actionInvocationView.processorConnectPoint.node()).style("display", "block");
                    //Remove message target from action invocation
                    actionInvocationView.messageManager.setMessageSource(actionInvocationView.getActionInvocationExpressionModel());
                    actionInvocationView.updateActivatedTarget();
                });

                // Hiding property button pane.
                $(propertyButtonPaneGroup.node()).remove();
                $(deleteButtonPaneGroup.node()).remove();
                $(propertyPaneWrapper).remove();
                $(smallArrow.node()).remove();
            });

        }.bind(lifeLineGroup.node(), this));
    };

    LifeLineView.prototype.updateTitleText = function (updatedText) {
        if (!_.isUndefined(updatedText) && updatedText !== '') {
            this._editableProperties.setterMethod.call(this._editableProperties.model, updatedText);
            var updatedText = this._editableProperties.getDisplayTitle.call(this._editableProperties.model);
            if(!_.isNil(updatedText)) {
                // truncate if larger than 14 chars
                updatedText = updatedText.length > 14 ? updatedText.substring(0,11) + '...' : updatedText;
            }

            this._topPolygonText.node().textContent = updatedText;
            this._bottomPolygonText.node().textContent = updatedText;
        }
    };


    return LifeLineView;
});
