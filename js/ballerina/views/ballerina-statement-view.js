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
define(['require', 'lodash', 'log', './../visitors/statement-visitor', 'd3', 'd3utils', 'property_pane_utils', './point', './bounding-box'],
    function (require, _, log, StatementVisitor, d3, D3Utils, PropertyPaneUtils, Point, BBox) {

    /**
     * A common class which consists functions of moving or resizing views.
     * @constructor
     */
    var BallerinaStatementView = function (args) {
        StatementVisitor.call(this, args);
        this._parent = _.get(args, "parent");
        this._model = _.get(args, "model");
        this._container = _.get(args, "container");
        this._viewOptions = _.get(args, "viewOptions");
        this.toolPalette = _.get(args, "toolPalette");
        this.messageManager = _.get(args, "messageManager");
        this._statementGroup = undefined;
        this._childrenViewsList = [];
        if (!_.has(args, 'topCenter')) {
            log.warn('topCenter has not defined. Default top center will be created');
        }
        this._topCenter = _.has(args, "topCenter") ? _.get(args, 'topCenter').clone() : new Point(0,0);
        this._boundingBox = new  BBox();
        var self = this;
        this._topCenter.on("moved", function(offset){
            self._bottomCenter(offset.dx, offset.dy);
        });
        StatementVisitor.call(this);
        this.init();
    };

    BallerinaStatementView.prototype = Object.create(StatementVisitor.prototype);
    BallerinaStatementView.prototype.constructor = BallerinaStatementView;

    BallerinaStatementView.prototype.init = function(){
        //Registering event listeners
        this.listenTo(this._model, 'childVisitedEvent', this.childVisitedCallback);
        this.listenTo(this._parent, 'changeStatementMetricsEvent', this.changeMetricsCallback);
    };

    BallerinaStatementView.prototype.childVisitedCallback = function (child) {
        // this.trigger("childViewAddedEvent", child);
    };

    BallerinaStatementView.prototype.childViewAddedCallback = function (child) {
    };

    BallerinaStatementView.prototype.setParent = function (parent) {
        this._parent = parent;
    };
    BallerinaStatementView.prototype.getParent = function () {
        return this._parent;
    };

    BallerinaStatementView.prototype.getStatementGroup = function () {
        return this._statementGroup;
    };
    BallerinaStatementView.prototype.setStatementGroup = function (getStatementGroup) {
        this._statementGroup = getStatementGroup;
    };
    BallerinaStatementView.prototype.getChildrenViewsList = function () {
        return this._childrenViewsList;
    };
    BallerinaStatementView.prototype.changeWidth = function (dw) {
    };
    BallerinaStatementView.prototype.changeHeight = function (dh) {
    };
    BallerinaStatementView.prototype.changeMetricsCallback = function (baseMetrics) {
    };
    BallerinaStatementView.prototype.getDiagramRenderingContext = function () {
        return this._diagramRenderingContext;
    };

    BallerinaStatementView.prototype.visitStatement = function (statement) {
        var StatementViewFactory = require('./statement-view-factory');
        var statementViewFactory = new StatementViewFactory();
        var newStatementGap = 30;
        var topCenter = new Point(this.getTopCenter().x(), this.getTopCenter().y() + newStatementGap);
        var args = {model: statement, container: this._statementGroup.node(), viewOptions: undefined, parent:this, topCenter: topCenter};
        var statementView = statementViewFactory.getStatementView(args);
        this._diagramRenderingContext.getViewModelMap()[statement.id] = statementView;
        this._childrenViewsList.push(statementView);
        statementView.render(this._diagramRenderingContext);
        this.resizeOnChildRendered(statementView.getBoundingBox());
    };

    BallerinaStatementView.prototype.visitExpression = function (statement) {
        var ExpressionViewFactory = require('./expression-view-factory');
        var expressionViewFactory = new ExpressionViewFactory();
        var args = {model: statement, container: this._statementGroup.node(), viewOptions: undefined, parent:this};
        var expressionView = expressionViewFactory.getExpressionView(args);
        this._diagramRenderingContext.getViewModelMap()[statement.id] = expressionView;
        this._childrenViewsList.push(expressionView);
        expressionView.setXPosition(this.getXPosition());
        expressionView.setYPosition(this.getYPosition() + 30);
        expressionView.render(this._diagramRenderingContext);
    };

        BallerinaStatementView.prototype._createPropertyPane = function (args) {
            var model = _.get(args, "model", {});
            var viewOptions = _.get(args, "viewOptions", {});
            var statementGroup = _.get(args, "statementGroup", null);
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
            // Adding click event for 'statement' group.
            $(statementGroup.node()).click(function (statementView, event) {

                var diagramRenderingContext = self.getDiagramRenderingContext();
                log.debug("Clicked statement group");

                event.stopPropagation();

                // Not allowing to click the statement group multiple times.
                if ($("." + viewOptions.actionButton.wrapper.class).length > 0) {
                    log.debug("statement group is already clicked");
                    return;
                }

                // Get the bounding box of the if else view.
                var statementBoundingBox = statementView.getBoundingBox();

                // Calculating width for edit and delete button.
                var propertyButtonPaneRectWidth = viewOptions.actionButton.width * 2;

                // Creating an SVG group for the edit and delete buttons.
                var propertyButtonPaneGroup = D3Utils.group(statementGroup);

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
                var centerPointX = statementBoundingBox.x + (statementBoundingBox.width / 2);
                var centerPointY = statementBoundingBox.y + statementBoundingBox.height;

                var smallArrowPoints =
                    // Bottom point of the polygon.
                    " " + centerPointX + "," + centerPointY +
                    // Left point of the polygon
                    " " + (centerPointX - 3) + "," + (centerPointY + 3) +
                    // Right point of the polygon.
                    " " + (centerPointX + 3) + "," + (centerPointY + 3);

                var smallArrow = D3Utils.polygon(smallArrowPoints, statementGroup);

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

                    // Adding "Add" button.
                    var buttonPane = $("<div/>").appendTo(propertyPaneBody);
                    var addButton = $("<button/>", {
                        class : viewOptions.propertyForm.body.addStatement.class,
                        text : viewOptions.propertyForm.body.addStatement.text

                    }).appendTo(buttonPane);

                    $(addButton).click(function (event) {
                        statementView.getModel().trigger("add-new-statement");
                    });

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

            }.bind(statementGroup.node(), this));
        };


        BallerinaStatementView.prototype.getTopCenter = function () {
        return this._topCenter;
    };

    BallerinaStatementView.prototype.getViewOptions = function () {
        return this._viewOptions;
    };

    BallerinaStatementView.prototype.getBoundingBox = function () {
        return this._boundingBox;
    };

    BallerinaStatementView.prototype.repositionStatement = function (options) {
        this.getBoundingBox().y(this.getBoundingBox().y() + options.dy);
        this.getStatementGroup().attr('transform', ('translate(0,' + options.dy + ')'));
    };

    BallerinaStatementView.prototype.resizeOnChildRendered = function (childBBox) {
        // TODO: Get these from the constants
        var widthIncrease = 20;
        var newHeight = childBBox.getBottom() + widthIncrease/2 - this.getBoundingBox().getTop();
        this.getBoundingBox().x(childBBox.x() - widthIncrease/2).w(childBBox.w() + widthIncrease).h(newHeight);
        this.getStatementGroup().outerRect.attr('height', newHeight);
        this.getStatementGroup().outerRect.attr('width', this.getBoundingBox().w());
        this.getStatementGroup().outerRect.attr('x', this.getBoundingBox().x());
        this.getStatementGroup().titleRect.attr('x', this.getBoundingBox().x());
        this.getStatementGroup().titleText.attr('x', this.getBoundingBox().x() + 20);
    };

    return BallerinaStatementView;
});
