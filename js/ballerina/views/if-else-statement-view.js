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
define(['require', 'lodash', 'log', 'property_pane_utils', './ballerina-statement-view', './../ast/if-else-statement', 'd3utils', 'd3', 'jquery', './../ast/if-statement', './../ast/if-else-statement'],
    function (require, _, log, PropertyPaneUtils, BallerinaStatementView, IfElseStatement, D3Utils, d3, $, IfStatement, IfElseStatement) {

        /**
         * The view to represent a If Else statement which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {IfElseStatement} args.model - The If Else statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} args.parent - Parent View (Resource, Worker, etc)
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var IfElseStatementView = function (args) {

            BallerinaStatementView.call(this, args);

            this._ifBlockView = undefined;
            this._elseIfViews = [];
            this._elseBlockView = undefined;
            this._totalHeight = 0;

            if (_.isNil(this._model) || !(this._model instanceof IfElseStatement)) {
                log.error("If Else statement definition is undefined or is of different type." + this._model);
                throw "If Else statement definition is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for If Else statement is undefined." + this._container);
                throw "Container for If Else statement is undefined." + this._container;
            }

            this.init();

        };

        IfElseStatementView.prototype = Object.create(BallerinaStatementView.prototype);
        IfElseStatementView.prototype.constructor = IfElseStatementView;

        IfElseStatementView.prototype.canVisitStatement = function(){
            return true;
        };

        IfElseStatementView.prototype.init = function () {
            //Registering event listeners
            this.listenTo(this._parentView, 'childViewAddedEvent', this.childViewAddedCallback);
        };

        /**
         * Visit If Statement
         * @param {IfStatement} statement
         */
        IfElseStatementView.prototype.visitIfStatement = function(statement){
            var StatementViewFactory = require('./statement-view-factory');
            var statementViewFactory = new StatementViewFactory();
            var args = {model: statement, container: this.getStatementGroup(), viewOptions: undefined, parent: this};
            var statementView = statementViewFactory.getStatementView(args);
            this._ifBlockView = statementView;
            this._diagramRenderingContext.getViewModelMap()[statement.id] = statementView;
            statementView.render(this._diagramRenderingContext);

            //adjust if-else statement's height
            this._totalHeight = this._totalHeight + statementView.getBoundingBox().height;
            this.setIfElseStatementHeight(this._totalHeight);
            this.trigger("childViewAddedEvent", statement);
        };

        /**
         * Visit If Else Statement
         * @param {IfElseStatement} statement
         */
        IfElseStatementView.prototype.visitElseIfStatement = function(statement){
            var StatementViewFactory = require('./statement-view-factory');
            var statementViewFactory = new StatementViewFactory();
            var args = {model: statement, container: this.getStatementGroup(), viewOptions: undefined, parent: this};
            var statementView = statementViewFactory.getStatementView(args);
            this._diagramRenderingContext.getViewModelMap()[statement.id] = statementView;
            statementView.render(this._diagramRenderingContext);

            //adjust if-else statement's height
            this._totalHeight = this._totalHeight + statementView.getBoundingBox().height;
            this.setIfElseStatementHeight(this._totalHeight);
            this.trigger("childViewAddedEvent", statement);
        };

        /**
         * Visit Else Statement
         * @param {ElseStatement} statement
         */
        IfElseStatementView.prototype.visitElseStatement = function(statement){
            var StatementViewFactory = require('./statement-view-factory');
            var statementViewFactory = new StatementViewFactory();
            var args = {model: statement, container: this.getStatementGroup(), viewOptions: undefined, parent: this};
            var statementView = statementViewFactory.getStatementView(args);
            this._elseBlockView = statementView;
            this._diagramRenderingContext.getViewModelMap()[statement.id] = statementView;
            statementView.render(this._diagramRenderingContext);

            //adjust if-else statement's height
            this._totalHeight = this._totalHeight + statementView.getBoundingBox().height;
            this.setIfElseStatementHeight(this._totalHeight);
            this.trigger("childViewAddedEvent", statement);
        };

        /**
         * Render the svg group to draw the if and the else statements
         */
        IfElseStatementView.prototype.render = function (diagramRenderingContext) {
            this._diagramRenderingContext = diagramRenderingContext;
            var ifElseGroup = D3Utils.group(d3.select(this._container));
            this.setStatementGroup(ifElseGroup);
            this.setBoundingBox(this.getWidth(), this.getHeight(), this.getXPosition(), this.getYPosition());
            this._model.accept(this);

            var editableProperties = [];
            _.forEach(this._model.getChildren(), function(child, index){
                var editableProperty = {};
                if (child instanceof IfStatement) {
                    editableProperty = {
                        propertyType: "text",
                        key: "If condition",
                        model: child,
                        getterMethod: child.getCondition,
                        setterMethod: child.setCondition
                    };

                    editableProperties.push(editableProperty);
                } else if(child instanceof IfElseStatement) {
                    editableProperty = {
                        propertyType: "text",
                        key: "Else If condition",
                        model: child,
                        getterMethod: child.getCondition,
                        setterMethod: child.setCondition
                    };

                    editableProperties.push(editableProperty);
                }
            });
            // Creating property pane
            this._createPropertyPane({
                ifElseGroup:ifElseGroup,
                editableProperties: editableProperties
            });
        };

        /**
         * Set the IfElseStatement model
         * @param {IfElseStatement} model
         */
        IfElseStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof IfElseStatement) {
                this._model = model;
            } else {
                log.error("If Else statement definition is undefined or is of different type." + model);
                throw "If Else statement definition is undefined or is of different type." + model;
            }
        };

        /**
         * Set the container to draw the if else group
         * @param container
         */
        IfElseStatementView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for If Else statement is undefined." + container);
                throw "Container for If Else statement is undefined." + container;
            }
        };

        IfElseStatementView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };


        /**
         * setting the height for the bounding box
         * @param height
         */
        IfElseStatementView.prototype.setIfElseStatementHeight = function (height){
            this.setBoundingBox(this.getBoundingBox().width, height, this.getBoundingBox().x, this.getBoundingBox().y);
        };

        /**
         * @returns {_model}
         */
        IfElseStatementView.prototype.getModel = function () {
            return this._model;
        };

        IfElseStatementView.prototype.getContainer = function () {
            return this._container;
        };

        IfElseStatementView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        IfElseStatementView.prototype.setIfBlockView = function (ifBlockView) {
            this._ifBlockView = ifBlockView;
        };

        IfElseStatementView.prototype.setElseBlockView = function (elseBlockView) {
            this._elseBlockView = elseBlockView;
        };

        IfElseStatementView.prototype.getIfBlockView = function () {
            return this._ifBlockView;
        };

        IfElseStatementView.prototype.getElseBlockView = function () {
            return this._elseBlockView;
        };

        IfElseStatementView.prototype.getElseIfViewList = function () {
            return this._elseIfViews;
        };

        IfElseStatementView.prototype.getLastElseIf = function () {
            return this._elseIfViews[this._elseIfViews.length - 1];
        };

        IfElseStatementView.prototype.changeChildrenMetrics = function (baseMetrics) {
            this.trigger("changeStatementMetricsEvent", baseMetrics);
        };

        IfElseStatementView.prototype._createPropertyPane = function (args) {
            var viewOptions = _.get(args, "viewOptions", {});
            var ifElseGroup = _.get(args, "ifElseGroup", null);
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

            viewOptions.propertyForm.body.addIfElse = _.get(args, "propertyForm.body.addIfElse", {});
            viewOptions.propertyForm.body.addIfElse.text = _.get(args, "propertyForm.body.addIfElse.text", "Add");
            viewOptions.propertyForm.body.addIfElse.class = _.get(args, "propertyForm.body.addIfElse.class", "property-pane-form-body-add-button");

            // Adding a css class for 'if else' group.
            ifElseGroup.classed("if-else-svg-group", true);

            // Adding click event for 'if else' group.
            $(ifElseGroup.node()).click(function (ifElseView, event) {

                log.debug("Clicked if else group");

                event.stopPropagation();

                // Not allowing to click the if else group mulutiple times.
                if ($("." + viewOptions.actionButton.wrapper.class).length > 0) {
                    log.debug("if else group is already clicked");
                    return;
                }

                // Adding style to the elements of the if else group.
                $(ifElseView.getIfBlockView().getStatementGroup().outerRect.node()).css("stroke", "#edb284");
                $(ifElseView.getIfBlockView().getStatementGroup().titleRect.node()).css("stroke", "#edb284");
                _.forEach(ifElseView.getElseIfViewList(), function (elseIfView) {
                    $(elseIfView.getStatementGroup().outerRect.node()).css("stroke", "#edb284");
                    $(elseIfView.getStatementGroup().titleRect.node()).css("stroke", "#edb284");
                });
                $(ifElseView.getElseBlockView().getStatementGroup().outerRect.node()).css("stroke", "#edb284");
                $(ifElseView.getElseBlockView().getStatementGroup().titleRect.node()).css("stroke", "#edb284");

                // Get the bounding box of the if else view.
                var ifElseBoundingBox = ifElseView.getBoundingBox();

                // Calculating width for edit and delete button.
                var propertyButtonPaneRectWidth = viewOptions.actionButton.width * 2;

                // Creating an SVG group for the edit and delete buttons.
                var propertyButtonPaneGroup = D3Utils.group(ifElseGroup);

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
                var centerPointX = ifElseBoundingBox.x + (ifElseBoundingBox.width / 2);
                var centerPointY = ifElseBoundingBox.y + ifElseBoundingBox.height;

                var smallArrowPoints =
                    // Bottom point of the polygon.
                    " " + centerPointX + "," + centerPointY +
                    // Left point of the polygon
                    " " + (centerPointX - 3) + "," + (centerPointY + 3) +
                    // Right point of the polygon.
                    " " + (centerPointX + 3) + "," + (centerPointY + 3);

                var smallArrow = D3Utils.polygon(smallArrowPoints, ifElseGroup);

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

                    // Resetting border color on if else block.
                    $(ifElseView.getIfBlockView().getStatementGroup().outerRect.node()).css("stroke", "#000000");
                    $(ifElseView.getIfBlockView().getStatementGroup().titleRect.node()).css("stroke", "#000000");
                    _.forEach(ifElseView.getElseIfViewList(), function (elseIfView) {
                        $(elseIfView.getStatementGroup().outerRect.node()).css("stroke", "#000000");
                        $(elseIfView.getStatementGroup().titleRect.node()).css("stroke", "#000000");
                    });
                    $(ifElseView.getElseBlockView().getStatementGroup().outerRect.node()).css("stroke", "#000000");
                    $(ifElseView.getElseBlockView().getStatementGroup().titleRect.node()).css("stroke", "#000000");

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
                        class : viewOptions.propertyForm.body.addIfElse.class,
                        text : viewOptions.propertyForm.body.addIfElse.text

                    }).appendTo(buttonPane);

                    $(addButton).click(function(){
                        // TODO : Write add implementation.
                    });

                    // Close the popups of property pane body.
                    function closeAllPopUps() {
                        $(propertyPaneWrapper).remove();

                        // Resetting border color on if else block.
                        $(ifElseView.getIfBlockView().getStatementGroup().outerRect.node()).css("stroke", "#000000");
                        $(ifElseView.getIfBlockView().getStatementGroup().titleRect.node()).css("stroke", "#000000");
                        _.forEach(ifElseView.getElseIfViewList(), function (elseIfView) {
                            $(elseIfView.getStatementGroup().outerRect.node()).css("stroke", "#000000");
                            $(elseIfView.getStatementGroup().titleRect.node()).css("stroke", "#000000");
                        });
                        $(ifElseView.getElseBlockView().getStatementGroup().outerRect.node()).css("stroke", "#000000");
                        $(ifElseView.getElseBlockView().getStatementGroup().titleRect.node()).css("stroke", "#000000");

                        // Remove the small arrow.
                        $(smallArrow.node()).remove();

                        $(this).unbind('click');
                    }

                });

                $(deleteButtonRect.node()).click(function(){
                    // TODO : Implement
                });

            }.bind(ifElseGroup.node(), this));
        };

        return IfElseStatementView;
    });