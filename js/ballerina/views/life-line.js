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

define(['lodash', 'jquery', 'd3', 'log', 'd3utils', 'app/diagram-core/models/point'], function (_, $, d3, log, D3Utils, Point) {

    /**
     *
     * @param canvas The SVG canvas to be drawn on.
     * @param options
     * @constructor
     */
    var LifeLine = function (canvas, options) {
        if (!_.isNil(canvas)) {
            this._canvas = canvas;

            // Set default options for the life-lines
            this._viewOptions = options || {};
            this._viewOptions.class = _.get(options, "class", "lifeline");
            this._viewOptions.editable = _.get(options, "editable", true);

            // Lifeline starting point.
            this._viewOptions.centerPoint = _.get(options, "centerPoint", {});
            this._viewOptions.centerPoint.x = _.get(options, "centerPoint.x", 90);
            this._viewOptions.centerPoint.y = _.get(options, "centerPoint.y", 40);

            // Lifeline polygon options.
            this._viewOptions.polygon = _.get(options, "polygon", {});
            this._viewOptions.polygon.shape = _.get(options, "polygon.shape", "diamond");
            this._viewOptions.polygon.width = _.get(options, "polygon.width", 65);
            this._viewOptions.polygon.height = _.get(options, "polygon.height", 65);
            this._viewOptions.polygon.roundX = _.get(options, "polygon.roundX", 0);
            this._viewOptions.polygon.roundY = _.get(options, "polygon.roundY", 0);
            this._viewOptions.polygon.class = _.get(options, "polygon.class", "client lifeline-polygon");

            // Lifeline droppable-rect options.
            this._viewOptions.droppableRect = _.get(options, "droppableRect", {});
            this._viewOptions.droppableRect.width = _.get(options, "droppableRect.width", 100);
            this._viewOptions.droppableRect.height = _.get(options, "droppableRect.height", 300);
            this._viewOptions.droppableRect.roundX = _.get(options, "droppableRect.roundX", 1);
            this._viewOptions.droppableRect.roundY = _.get(options, "droppableRect.roundY", 1);
            this._viewOptions.droppableRect.class = _.get(options, "droppableRect.class", "client lifeline-droppableRect");

            // Lifeline line options.
            this._viewOptions.line = _.get(options, "line", {});
            this._viewOptions.line.height = _.get(options, "line.height", 275);
            this._viewOptions.line.class = _.get(options, "line.class", "client lifeline-line");

            // Lifeline text options.
            this._viewOptions.text = _.get(options, "text", {});
            this._viewOptions.text.value = _.get(options, "text.value", "Client");
            this._viewOptions.text.class = _.get(options, "text.class", "client lifeline-title");
            this._viewOptions.action = _.get(options, "action", {});
            this._viewOptions.action.value = _.get(options, "action.value", "Action");

            // Make the lifeline uneditable by default
            if (_.get(options, "editable", false)) {
                // TODO : Implement for welcome page.
            }
        }
    };

    LifeLine.prototype.constructor = LifeLine;

    LifeLine.prototype.getLifeLineOptions = function () {
      return this._viewOptions;
    };

    /**
     * Redraws the lifeline.
     * @param options The lifeline options. Use #getLifeLineOptions() to get the existing options of a lifeline.
     */
    LifeLine.prototype.reRender = function(options) {
        this._viewOptions = options;
        // Deleting existing lifeline.
        this._lifelineGroup.remove();
        // Redrawing the lifeline.
        this.render();
    };

    /**
     * Update the height of the line.
     * @param newHeight The new height value.
     * @param fromBottom true if to update the height from the bottom, else update the height from the top.
     */
    LifeLine.prototype.setLineHeight = function (newHeight, fromBottom) {
        if (newHeight < 1) {
            log.info("New height for a lifeline cannot be less than 1.");
            return;
        }

        this._viewOptions.line.height = newHeight;

        if (_.isNil(fromBottom)) {
            fromBottom = true;
        }

        if (fromBottom) {
            // Get difference. Positive value implies that the size has increased.
            var lineHeightDifference = newHeight - (parseFloat(this._middleLine.attr("y2")) - parseFloat(this._middleLine.attr("y1")));

            // Updating the height of the line.
            this._middleLine.attr("y2", parseFloat(this._middleLine.attr("y1")) + newHeight);

            // Positioning bottom polygon.
            var newPoints = "";
            var pointsOfBottomPolygon = this._bottomPolygon.attr("points").split(" ");
            _.forEach(pointsOfBottomPolygon, function(point, index){
                if (index != 0) {
                    var coordinate = point.split(",");
                    newPoints += coordinate[0] + "," + (parseFloat(coordinate[1]) + parseFloat(lineHeightDifference));
                    if (index != pointsOfBottomPolygon.length - 1) {
                        newPoints += " ";
                    }
                }
            });

            this._bottomPolygon.attr("points", newPoints);

            // Position bottom polygon text.
            this._bottomPolygonText.attr("y", parseFloat(this._bottomPolygonText.attr("y")) + lineHeightDifference);

            // Updating droppable middle rect
            this._droppableMiddleRect.attr("height", newHeight);
        } else {
            // TODO : To implement
        }
    };

    LifeLine.prototype.position = function (x, y) {
        this._lifelineGroup.attr("transform", "translate(" + x + "," + y + ")");
    };

    LifeLine.prototype.render = function () {
        // Creating group for lifeline.
        this._lifelineGroup = D3Utils.group(d3.select(this._canvas)).classed("client", true);

        if (this._viewOptions.polygon.shape == "diamond") { // Drawing top polygon.
            var polygonYOffset = this._viewOptions.polygon.height / 2;
            var polygonXOffset = this._viewOptions.polygon.width / 2;
            var topPolygonPoints =
                // Bottom point of the polygon.
                " " + this._viewOptions.centerPoint.x + "," + (this._viewOptions.centerPoint.y + polygonYOffset) +
                // Right point of the polygon
                " " + (this._viewOptions.centerPoint.x + polygonXOffset) + "," + this._viewOptions.centerPoint.y +
                // Top point of the polygon.
                " " + this._viewOptions.centerPoint.x + "," + (this._viewOptions.centerPoint.y - polygonYOffset) +
                // Left point of the polygon.
                " " + (this._viewOptions.centerPoint.x - polygonXOffset) + "," + this._viewOptions.centerPoint.y;

            this._topPolygon = D3Utils.polygon(topPolygonPoints, this._lifelineGroup);
            this._topPolygon.attr("stroke-linejoin", "round");

            // Add text to top polygon.
            this._topPolygonText = D3Utils.textElement(this._viewOptions.centerPoint.x, this._viewOptions.centerPoint.y,
                this._viewOptions.text.value, this._lifelineGroup)
                .classed(this._viewOptions.text.class, true).classed("genericT", true);

            // Centering the text to the middle of the top polygon.
            this._topPolygonText.attr('dominant-baseline', "middle");

            // Drawing middle line.
            //// Bottom point of the top polygon will be the starting y point of the middle line.
            var startingYPointOfTheMiddleLine = this._viewOptions.centerPoint.y + polygonYOffset;
            var endingYPointOfTheMiddleLine = this._viewOptions.centerPoint.y + polygonYOffset + this._viewOptions.line.height;
            this._middleLine = D3Utils.line(this._viewOptions.centerPoint.x, startingYPointOfTheMiddleLine,
                this._viewOptions.centerPoint.x, endingYPointOfTheMiddleLine, this._lifelineGroup);

            // Drawing bottom polygon.
            var centerYPointOfBottomPolygon = endingYPointOfTheMiddleLine + polygonYOffset;

            // Drawing droppable middle rect
            var startingXPointOfDroppableRect = this._viewOptions.centerPoint.x - (polygonXOffset / 2);
            var startingYPointOfDroppableRect = this._viewOptions.centerPoint.y + polygonYOffset;
            this._droppableMiddleRect = D3Utils.rect(startingXPointOfDroppableRect, startingYPointOfDroppableRect,
                (this._viewOptions.polygon.width / 2), this._viewOptions.line.height, 0, 0, this._lifelineGroup);
            this._droppableMiddleRect.attr('style', 'cursor: pointer');
            this._droppableMiddleRect.attr('fill', "#FFFFFF");
            this._droppableMiddleRect.attr("fill-opacity", 0.01);
            this._droppableMiddleRect.on('mouseover', function () {
                d3.select(this).attr('fill', "green");
                d3.select(this).attr('fill-opacity', 0.1);
            }).on('mouseout', function () {
                d3.select(this).attr('fill', "#FFFFFF");
                d3.select(this).attr("fill-opacity", 0.01);
            }).on('mouseup', function (data) {

            });

            var bottomPolygonPoints =
                // Bottom point of the polygon.
                " " + this._viewOptions.centerPoint.x + "," + (centerYPointOfBottomPolygon + polygonYOffset) +
                // Left point of the polygon
                " " + (this._viewOptions.centerPoint.x + polygonXOffset) + "," + centerYPointOfBottomPolygon +
                // Top point of the polygon.
                " " + this._viewOptions.centerPoint.x + "," + (centerYPointOfBottomPolygon - polygonYOffset) +
                // Right point of the polygon.
                " " + (this._viewOptions.centerPoint.x - polygonXOffset) + "," + centerYPointOfBottomPolygon;

            this._bottomPolygon = D3Utils.polygon(bottomPolygonPoints, this._lifelineGroup);
            this._bottomPolygon.attr('fill', "#FFFFFF");
            this._bottomPolygon.attr('stroke-width', "1");
            this._bottomPolygon.attr('stroke', "#000000");

            // Add text to bottom polygon.
            this._bottomPolygonText = D3Utils.textElement(this._viewOptions.centerPoint.x, centerYPointOfBottomPolygon,
                this._viewOptions.text.value, this._lifelineGroup)
                .classed(this._viewOptions.text.class, true).classed("genericT", true);

            // Centering the text to the middle of the bottom polygon.
            this._bottomPolygonText.attr('dominant-baseline', "middle");
        } else if (this._viewOptions.polygon.shape == "rect") {
            this._topPolygon = D3Utils.centeredRect(new Point(this._viewOptions.centerPoint.x, this._viewOptions.centerPoint.y), this._viewOptions.polygon.width, this._viewOptions.polygon.height, 0, 0, this._lifelineGroup);
            this._topPolygon.attr('fill', "#FFFFFF");
            this._topPolygon.attr('stroke-width', "1");
            this._topPolygon.attr('stroke', "#000000");

            // Add text to top polygon.
            this._topPolygonText = D3Utils.textElement(this._viewOptions.centerPoint.x, this._viewOptions.centerPoint.y,
                this._viewOptions.text.value , this._lifelineGroup)
                .classed(this._viewOptions.text.class, true).classed("genericT", true);

            // Centering the text to the middle of the top polygon.
            this._topPolygonText.attr('text-anchor', "middle");
            this._topPolygonText.attr('alignment-baseline', "central");

            // Drawing middle line.
            //// Bottom point of the top polygon will be the starting y point of the middle line.
            var startingYPointOfTheMiddleLine = this._viewOptions.centerPoint.y + (this._viewOptions.polygon.height / 2);
            var endingYPointOfTheMiddleLine = this._viewOptions.centerPoint.y + this._viewOptions.polygon.height + this._viewOptions.line.height;
            this._middleLine = D3Utils.line(this._viewOptions.centerPoint.x, startingYPointOfTheMiddleLine,
                this._viewOptions.centerPoint.x, endingYPointOfTheMiddleLine, this._lifelineGroup);

            // Drawing droppable middle rect
            var startingXPointOfDroppableRect = this._viewOptions.centerPoint.x - (this._viewOptions.polygon.width / 4);
            var startingYPointOfDroppableRect = this._viewOptions.centerPoint.y + (this._viewOptions.polygon.height / 2);
            this._droppableMiddleRect = D3Utils.rect(startingXPointOfDroppableRect, startingYPointOfDroppableRect,
                (this._viewOptions.polygon.width / 2), (this._viewOptions.polygon.height / 2) + this._viewOptions.line.height, 0, 0, this._lifelineGroup);
            this._droppableMiddleRect.attr('style', 'cursor: pointer');
            this._droppableMiddleRect.attr('fill', "#FFFFFF");
            this._droppableMiddleRect.attr("fill-opacity", 0.01);
            // this._droppableMiddleRect.attr('fill', "#FFFFFF");
            // this._droppableMiddleRect.attr('stroke-width', "1");
            // this._droppableMiddleRect.attr('stroke', "#000000");
            this._droppableMiddleRect.on('mouseover', function () {
                d3.select(this).attr('fill', "green");
                d3.select(this).attr('fill-opacity', 0.1);
            }).on('mouseout', function () {
                d3.select(this).attr('fill', "#FFFFFF");
                d3.select(this).attr("fill-opacity", 0.01);
            }).on('mouseup', function (data) {

            });

            this._bottomPolygon = D3Utils.centeredRect(new Point(this._viewOptions.centerPoint.x, this._viewOptions.centerPoint.y + this._viewOptions.line.height), this._viewOptions.polygon.width, this._viewOptions.polygon.height, 0, 0, this._lifelineGroup);
            this._bottomPolygon.attr('fill', "#FFFFFF");
            this._bottomPolygon.attr('stroke-width', "1");
            this._bottomPolygon.attr('stroke', "#000000");
            // this._bottomPolygon.attr('fill', "#FFFFFF");
            // this._bottomPolygon.attr('stroke-width', "1");
            // this._bottomPolygon.attr('stroke', "#000000");
            //
            // // Add text to bottom polygon.
            // this._bottomPolygonText = D3Utils.textElement(this._viewOptions.centerPoint.x, centerYPointOfBottomPolygon,
            //     this._viewOptions.text.value, this._lifelineGroup)
            //     .classed(this._viewOptions.text.class, true).classed("genericT", true);
            //
            // // Centering the text to the middle of the bottom polygon.
            // this._bottomPolygonText.attr('dominant-baseline', "middle");
        }

        // Adding property editor buttons.
        if (_.get(this._viewOptions, "editable", false)) {
            this.addEditableAndDeletable();
        }

        // TODO : Implement draggable.
    };

    // TODO : Implement rendering processors
    LifeLine.prototype.renderProcessors = function () {
        // Minimum length for a Lifeline
        var minimumLength = 250;
        // Distance from lifeline's center point to first processor.
        var initDistance = 60;
        // Space between two processors
        var distanceBetweenProcessors = 30;
        var centerPoint = this.modelAttr('centerPoint');
        var xValue = centerPoint.x();
        var yValue = centerPoint.y();
        yValue += initDistance;

        var initialHeight = parseInt(this.d3el.line.attr("y2")) - parseInt(this.d3el.line.attr("y1"));
        var totalIncrementedHeight = 0;
        var previousHeight = 0;

        for (var id in this.modelAttr("children").models) {

            if (this.modelAttr("children").models[id] instanceof Processor) {
                var processor = this.modelAttr("children").models[id];
                var processorViewOptions = {
                    model: processor,
                    center: new DiagramCore.Models.Point({x: xValue, y: yValue}),
                    canvas: this.canvas,
                    serviceView: this.serviceView
                };
                var processorView = new ProcessorView(processorViewOptions);
                processorView.render();
                processor.setY(yValue);
                yValue += processor.getHeight() + distanceBetweenProcessors;
                previousHeight = processor.getHeight();
            } else {
                var messagePoint = this.modelAttr("children").models[id];
                if (messagePoint.direction() == "outbound") {
                    if (!_.isUndefined(messagePoint.forceY) && _.isEqual(messagePoint.forceY, true)) {
                        yValue = messagePoint.y();
                    }
                    messagePoint.y(yValue);
                    messagePoint.x(xValue);
                } else {
                    if (!_.isUndefined(messagePoint.forceY) && _.isEqual(messagePoint.forceY, true)) {
                        yValue = messagePoint.y();
                    }
                    var sourceY = messagePoint.message().source().y();
                    if (yValue < sourceY) {
                        messagePoint.y(sourceY);
                    } else {
                        messagePoint.y(yValue);
                        messagePoint.message().source().y(yValue);
                    }
                    messagePoint.x(xValue);
                }
                yValue += 60;
                totalIncrementedHeight = totalIncrementedHeight + 40;
            }
        }

        var totalHeight = parseInt(yValue) - parseInt(this.d3el.line.attr("y1"));
        if (totalHeight < minimumLength) {
            totalHeight = minimumLength;
        }
        if (!_.isUndefined(this.serviceView.model.highestLifeline) && this.serviceView.model.highestLifeline !== null) {
            if (this.serviceView.model.highestLifeline.getHeight() > totalHeight) {
                totalHeight = this.serviceView.model.highestLifeline.getHeight();
            }
            var maxHeight = this.serviceView.model.highestLifeline.getHeight();
        }
        this.model.setHeight(totalHeight);
        this.adjustHeight(this.d3el, totalHeight - initialHeight);

        if (this.serviceView.model.highestLifeline == undefined || maxHeight < this.model.getHeight()) {
            this.serviceView.model.highestLifeline = this.model;
            this.serviceView.render();
            return false;
        }
        return this.d3el;
    };

    // TODO : Implement rendering messages
    LifeLine.prototype.renderMessages = function () {
        for (var id in this.modelAttr("children").models) {
            var messagePoint = this.modelAttr("children").models[id];
            if ((messagePoint instanceof MessagePoint)) {
                var linkView = new MessageLinkView({
                    model: messagePoint.message(),
                    options: {class: "message"},
                    serviceView: this.serviceView
                });
                linkView.render("#" + this.serviceView.model.wrapperId, "messages");
            }
        }
    };

    // TODO : Fix icons and field creations
    LifeLine.prototype.addEditableAndDeletable = function () {
        var optionMenuStartX = this._viewOptions.centerPoint.x + 2 + (this._viewOptions.polygon.width + 30) / 2;
        var optionMenuStartY = this._viewOptions.centerPoint.y - this._viewOptions.polygon.height / 2;
        var optionsMenuGroup = D3Utils.group(this._lifelineGroup).classed("option-menu option-menu-hide", true);

        var optionsMenuWrapper = D3Utils.rect(optionMenuStartX + 8,
            optionMenuStartY,
            30,
            58,
            0,
            0,
            optionsMenuGroup, "#f8f8f3").attr("style", "stroke: #ede9dc; stroke-width: 1; opacity:0.5; cursor: pointer").on("mouseover", function () {
            d3.select(this).attr("style", "stroke: #ede9dc; stroke-width: 1; opacity: .7; cursor: pointer");
        }).on("mouseout", function () {
            d3.select(this).attr("style", "stroke: #ede9dc; stroke-width: 1; opacity: 0.5; cursor: pointer");
        });

        var deleteOption = D3Utils.rect(optionMenuStartX + 11,
            optionMenuStartY + 3,
            24,
            24,
            0,
            0,
            optionsMenuGroup, "url(#delIcon)").attr("style", "opacity:0.5; cursor: pointer").on("mouseover", function () {
            d3.select(this).attr("style", "stroke: #ede9dc; stroke-width: 1; opacity: 1; cursor: pointer");
            optionsMenuWrapper.attr("style", "stroke: #ede9dc; stroke-width: 1; opacity: .7");
        }).on("mouseout", function () {
            d3.select(this).attr("style", "stroke: #ede9dc; stroke-width: 1; opacity: 0.5; cursor: pointer");
            optionsMenuWrapper.attr("style", "stroke: #ede9dc; stroke-width: 1; opacity: 0.5; cursor: pointer");
        });

        var editOption = D3Utils.rect(optionMenuStartX + 11,
            optionMenuStartY + 32,
            24,
            24,
            0,
            0,
            optionsMenuGroup, "url(#editIcon)").attr("style", "opacity:0.5; cursor: pointer").on("mouseover", function () {
            d3.select(this).attr("style", "stroke: #ede9dc; stroke-width: 1; opacity: 1; cursor: pointer");
            optionsMenuWrapper.attr("style", "stroke: #ede9dc; stroke-width: 1; opacity: .7; cursor: pointer");
        }).on("mouseout", function () {
            d3.select(this).attr("style", "stroke: #ede9dc; stroke-width: 1; opacity: 0.5; cursor: pointer");
            optionsMenuWrapper.attr("style", "stroke: #ede9dc; stroke-width: 1; opacity: 0.5; cursor: pointer");
        });

        var viewObj = this;

        this._lifelineGroup.on("click", (function () {
            // viewObj.serviceView.model.selectedNode = viewObj.model;
            if (optionsMenuGroup.classed("option-menu-hide")) {
                optionsMenuGroup.classed("option-menu-hide", false);
                optionsMenuGroup.classed("option-menu-show", true);

                // if (viewObj.serviceView.model.selectedOptionsGroup) {
                //     viewObj.serviceView.model.selectedOptionsGroup.classed("option-menu-hide", true);
                //     viewObj.serviceView.model.selectedOptionsGroup.classed("option-menu-show", false);
                // }
                // if (viewObj.serviceView.model.propertyWindow) {
                //     viewObj.serviceView.model.propertyWindow = false;
                //     viewObj.serviceView.enableDragZoomOptions();
                //     $('#property-pane-svg').empty();
                // }
                // viewObj.serviceView.model.selectedOptionsGroup = optionsMenuGroup;

            } else {
                optionsMenuGroup.classed("option-menu-hide", true);
                optionsMenuGroup.classed("option-menu-show", false);
                // viewObj.serviceView.model.propertyWindow = false;
                // viewObj.serviceView.enableDragZoomOptions();
                // viewObj.serviceView.model.selectedOptionsGroup = null;
                // viewObj.serviceView.render();
            }
        }));

        editOption.on("click", function () {

            // if (viewObj.serviceView.model.propertyWindow) {
            //     viewObj.serviceView.model.propertyWindow = false;
            //     viewObj.serviceView.enableDragZoomOptions();
            //     viewObj.serviceView.render();
            //
            // } else {
            //     viewObj.serviceView.model.selectedMainElementText = {
            //         top: viewObj.d3el.svgTitle,
            //         bottom: viewObj.d3el.svgTitleBottom
            //     };
            //
            //     var options = {
            //         x: parseFloat(this.getAttribute("x")) + 6,
            //         y: parseFloat(this.getAttribute("y")) + 21
            //     };
            //
            //     viewObj.serviceView.selectedNode = viewObj.model;
            //
            //     viewObj.serviceView.drawPropertiesPane(d3Ref, options,
            //         viewObj.model.get("utils").getMyParameters(viewObj.model),
            //         viewObj.model.get('utils').getMyPropertyPaneSchema(
            //             viewObj.model));
            // }
        });

        deleteOption.on("click", function () {
            //Get the parent of the model and delete it from the parent
            // if (~viewObj.model.get("title").indexOf("Resource")) {
            //     var resourceElements = viewObj.serviceView.model.get("diagramResourceElements").models;
            //     for (var itr = 0; itr < resourceElements.length; itr++) {
            //         if (resourceElements[itr].cid === viewObj.model.cid) {
            //             resourceElements.splice(itr, 1);
            //             var currentResources = viewObj.serviceView.model.resourceLifeLineCounter();
            //             viewObj.serviceView.model.resourceLifeLineCounter(currentResources - 1);
            //             viewObj.serviceView.model.get("diagramResourceElements").length -= 1;
            //             viewObj.serviceView.render();
            //             break;
            //         }
            //     }
            // } else {
            //     var endpointElements = viewObj.serviceView.model.get("diagramEndpointElements").models;
            //     for (var itr = 0; itr < endpointElements.length; itr++) {
            //         if (endpointElements[itr].cid === viewObj.model.cid) {
            //             endpointElements.splice(itr, 1);
            //             var currentEndpoints = viewObj.serviceView.model.endpointLifeLineCounter();
            //             viewObj.serviceView.model.endpointLifeLineCounter(currentEndpoints - 1);
            //             viewObj.serviceView.model.get("diagramEndpointElements").length -= 1;
            //             viewObj.serviceView.render();
            //             break;
            //         }
            //     }
            // }
        });
    };

    return LifeLine;
});