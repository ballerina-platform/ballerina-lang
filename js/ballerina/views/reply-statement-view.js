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
define(['lodash', 'log', './ballerina-statement-view', './../ast/reply-statement', 'd3utils', 'd3', './point'],
    function (_, log, BallerinaStatementView, ReplyStatement, D3Utils, d3, Point) {

        /**
         * The view to represent a reply statement which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {ReplyStatement} args.model - The reply statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var ReplyStatementView = function (args) {

            BallerinaStatementView.call(this, args);

            if (_.isNil(this._model) || !(this._model instanceof ReplyStatement)) {
                log.error("Return statement definition is undefined or is of different type." + this._model);
                throw "Return statement definition is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for return statement is undefined." + this._container);
                throw "Container for return statement is undefined." + this._container;
            }

            // View options for height and width of the assignment statement box.
            this._viewOptions.height = _.get(args, "viewOptions.height", 30);
            this._viewOptions.width = _.get(args, "viewOptions.width", 120);
            this.getBoundingBox().fromTopCenter(this._topCenter, this._viewOptions.width, this._viewOptions.height);
        };

        ReplyStatementView.prototype = Object.create(BallerinaStatementView.prototype);
        ReplyStatementView.prototype.constructor = ReplyStatementView;

        ReplyStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof ReplyStatement) {
                this._model = model;
            } else {
                log.error("Return statement definition is undefined or is of different type." + model);
                throw "Return statement definition is undefined or is of different type." + model;
            }
        };

        ReplyStatementView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for return statement is undefined." + container);
                throw "Container for return statement is undefined." + container;
            }
        };

        ReplyStatementView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        ReplyStatementView.prototype.getModel = function () {
            return this._model;
        };

        ReplyStatementView.prototype.getContainer = function () {
            return this._container;
        };

        ReplyStatementView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        /**
         * Rendering the view for reply statement.
         * @returns {group} The svg group which contains the elements of the reply statement view.
         */
        ReplyStatementView.prototype.render = function () {
            var width =  this._viewOptions.width;
            var height = this._viewOptions.height;

            var startActionGroup = D3Utils.group(d3.select(this._container));
            var line_start = new Point(this.getBoundingBox().x(), this.getBoundingBox().y() + height/2);
            // FixMe: here 50 is the length of the arrow line. This value has to determine dynamically considering the client lifeline
            var distanceToClient = 50;
            var line_end = new Point(this.getBoundingBox().x() - distanceToClient, this.getBoundingBox().y() + height/2);
            var reply_rect = D3Utils.rect(this.getBoundingBox().x(), this.getBoundingBox().y(), width, height, 0, 0, startActionGroup).classed('statement-rect', true);
            var reply_text = D3Utils.textElement(this.getBoundingBox().x() + width/2, this.getBoundingBox().y() + height/2, 'Reply', startActionGroup).classed('statement-text', true);
            var reply_line = D3Utils.lineFromPoints(line_start,line_end, startActionGroup)
                .classed('message', true);
            var arrowHeadWidth = 5;
            var reply_arrow_head = D3Utils.outputTriangle(line_end.x(), line_end.y(), startActionGroup).classed("action-arrow", true);

            log.info("Rendering the Reply Statement.");

            this._createPropertyPane({
                model: this._model,
                statementGroup:startActionGroup
            });

            var self = this;
            this.getBoundingBox().on('top-edge-moved', function(dy){
                reply_rect.attr('y',  parseFloat(reply_rect.attr('y')) + dy);
                reply_text.attr('y',  parseFloat(reply_text.attr('y')) + dy);
                reply_line.attr('y1',  parseFloat(reply_line.attr('y1')) + dy);
                reply_line.attr('y2',  parseFloat(reply_line.attr('y2')) + dy);

                line_end = new Point(self.getBoundingBox().x() - distanceToClient, self.getBoundingBox().y() + height/2);

                reply_arrow_head.remove();
                reply_arrow_head = D3Utils.outputTriangle(line_end.x(), line_end.y(), startActionGroup).classed("action-arrow", true);
            });
            return startActionGroup;
        };

        ReplyStatementView.prototype._createPropertyPane = function (args) {
            var model = _.get(args, "model", {});
            var viewOptions = _.get(args, "viewOptions", {});
            var statementGroup = _.get(args, "statementGroup", null);

            viewOptions.actionButton = _.get(args, "viewOptions.actionButton", {});
            viewOptions.actionButton.class = _.get(args, "actionButton.class", "property-pane-action-button");
            viewOptions.actionButton.wrapper = _.get(args, "actionButton.wrapper", {});
            viewOptions.actionButton.wrapper.class = _.get(args, "actionButton.wrapper.class", "property-pane-action-button-wrapper");
            viewOptions.actionButton.deleteClass = _.get(args, "viewOptions.actionButton.deleteClass", "property-pane-action-button-delete");

            viewOptions.actionButton.width = _.get(args, "viewOptions.action.button.width", 22);
            viewOptions.actionButton.height = _.get(args, "viewOptions.action.button.height", 22);

            $(statementGroup.node()).click(function (statementView, event) {
                //var diagramRenderingContext = self.getDiagramRenderingContext();
                log.debug("Clicked reply statement group");

                event.stopPropagation();
                $(window).trigger('click');
                // Not allowing to click the statement group multiple times.
                if ($("." + viewOptions.actionButton.wrapper.class).length > 0) {
                    log.debug("reply statement group is already clicked");
                    return;
                }

                // Get the bounding box of the if else view.
                var statementBoundingBox = statementView.getBoundingBox();

                // Calculating width for edit and delete button.
                var propertyButtonPaneRectWidth = viewOptions.actionButton.width;

                // Creating an SVG group for the edit and delete buttons.
                var propertyButtonPaneGroup = D3Utils.group(statementGroup);

                // Adding svg definitions needed for styling edit and delete buttons.
                var svgDefinitions = propertyButtonPaneGroup.append("defs");

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

                var centerPointX = statementBoundingBox.x()+ (statementBoundingBox.w() / 2);
                var centerPointY = statementBoundingBox.y()+ statementBoundingBox.h();

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

                // Creating the delete action button.
                var deleteButtonRect = D3Utils.rect(centerPointX - (propertyButtonPaneRectWidth / 2), centerPointY + 3,
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
        return ReplyStatementView;
    });