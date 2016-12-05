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

define(['lodash', 'jquery', 'd3', 'log', 'd3utils'], function (_, $, d3, log, D3Utils) {

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
            var lifeLineOptions = options || {};
            lifeLineOptions.class = _.get(options, "class", "lifeline");
            lifeLineOptions.editable = _.get(options, "editable", true);

            // Lifeline starting point.
            lifeLineOptions.centerPoint = _.get(options, "centerPoint", {});
            lifeLineOptions.centerPoint.x = _.get(options, "centerPoint.x", 90);
            lifeLineOptions.centerPoint.y = _.get(options, "centerPoint.y", 40);

            // Lifeline polygon options.
            lifeLineOptions.polygon = _.get(options, "polygon", {});
            lifeLineOptions.polygon.shape = _.get(options, "polygon.shape", "rect");
            lifeLineOptions.polygon.width = _.get(options, "polygon.width", 65);
            lifeLineOptions.polygon.height = _.get(options, "polygon.height", 65);
            lifeLineOptions.polygon.roundX = _.get(options, "polygon.roundX", 0);
            lifeLineOptions.polygon.roundY = _.get(options, "polygon.roundY", 0);
            lifeLineOptions.polygon.class = _.get(options, "polygon.class", "lifeline-polygon");

            // Lifeline droppable-rect options.
            lifeLineOptions.droppableRect = _.get(options, "droppableRect", {});
            lifeLineOptions.droppableRect.width = _.get(options, "droppableRect.width", 100);
            lifeLineOptions.droppableRect.height = _.get(options, "droppableRect.height", 300);
            lifeLineOptions.droppableRect.roundX = _.get(options, "droppableRect.roundX", 1);
            lifeLineOptions.droppableRect.roundY = _.get(options, "droppableRect.roundY", 1);
            lifeLineOptions.droppableRect.class = _.get(options, "droppableRect.class", "lifeline-droppableRect");

            // Lifeline line options.
            lifeLineOptions.line = _.get(options, "line", {});
            lifeLineOptions.line.height = _.get(options, "line.height", 150);
            lifeLineOptions.line.class = _.get(options, "line.class", "lifeline-line");

            // Lifeline text options.
            lifeLineOptions.text = _.get(options, "text", {});
            lifeLineOptions.text.value = _.get(options, "text.value", "Client");
            lifeLineOptions.text.class = _.get(options, "text.class", "lifeline-title");

            this._lifeLineOptions = lifeLineOptions;

            // Make the lifeline uneditable by default
            if (_.get(options, "editable", false)) {
                // TODO : Implement for welcome page.
            }
        }
    };

    LifeLine.prototype.constructor = LifeLine;

    LifeLine.prototype.getLifeLineOptions = function () {
      return this._lifeLineOptions;
    };

    /**
     * Redraws the lifeline.
     * @param options The lifeline options. Use #getLifeLineOptions() to get the existing options of a lifeline.
     */
    LifeLine.prototype.reRender = function(options) {
        this._lifeLineOptions = options;
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

        this._lifeLineOptions.line.height = newHeight;

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
        this._lifelineGroup = D3Utils.group(d3.select(this._canvas));

        // Top polygon group.
        this._topPolygonGroup = D3Utils.group(this._lifelineGroup);

        // Drawing top polygon.
        var polygonYOffset = this._lifeLineOptions.polygon.height / 2;
        var polygonXOffset = this._lifeLineOptions.polygon.width / 2;
        var topPolygonPoints =
            // Bottom point of the polygon.
            " " + this._lifeLineOptions.centerPoint.x + "," + (this._lifeLineOptions.centerPoint.y + polygonYOffset) +
            // Right point of the polygon
            " " + (this._lifeLineOptions.centerPoint.x + polygonXOffset) + "," + this._lifeLineOptions.centerPoint.y +
            // Top point of the polygon.
            " " + this._lifeLineOptions.centerPoint.x + "," + (this._lifeLineOptions.centerPoint.y - polygonYOffset) +
            // Left point of the polygon.
            " " + (this._lifeLineOptions.centerPoint.x - polygonXOffset) + "," + this._lifeLineOptions.centerPoint.y;

        this._topPolygon = D3Utils.polygon(topPolygonPoints, this._topPolygonGroup);
        this._topPolygon.attr('fill', "#FFFFFF");
        this._topPolygon.attr('stroke-width', "1");
        this._topPolygon.attr('stroke', "#000000");

        // Add text to top polygon.
        this._topPolygonText = D3Utils.textElement(this._lifeLineOptions.centerPoint.x, this._lifeLineOptions.centerPoint.y,
            this._lifeLineOptions.text.value, this._topPolygonGroup)
            .classed(this._lifeLineOptions.text.class, true).classed("genericT",true);

        // Centering the text to the middle of the top polygon.
        this._topPolygonText.attr('dominant-baseline', "middle");

        // Drawing middle line.
        //// Bottom point of the top polygon will be the starting y point of the middle line.
        var startingYPointOfTheMiddleLine = this._lifeLineOptions.centerPoint.y + polygonYOffset;
        var endingYPointOfTheMiddleLine = this._lifeLineOptions.centerPoint.y + polygonYOffset + this._lifeLineOptions.line.height;
        this._middleLine = D3Utils.line(this._lifeLineOptions.centerPoint.x, startingYPointOfTheMiddleLine,
            this._lifeLineOptions.centerPoint.x, endingYPointOfTheMiddleLine, this._lifelineGroup);
        this._middleLine.attr('stroke-width', "1");
        this._middleLine.attr('stroke', "#000000");

        // Drawing bottom polygon.
        var centerYPointOfBottomPolygon = endingYPointOfTheMiddleLine + polygonYOffset;

        // Drawing droppable middle rect
        var startingXPointOfDroppableRect = this._lifeLineOptions.centerPoint.x - (polygonXOffset / 2);
        var startingYPointOfDroppableRect = this._lifeLineOptions.centerPoint.y + polygonYOffset;
        this._droppableMiddleRect = D3Utils.rect(startingXPointOfDroppableRect, startingYPointOfDroppableRect,
            (this._lifeLineOptions.polygon.width / 2), this._lifeLineOptions.line.height, 0, 0, this._lifelineGroup);
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
            " " + this._lifeLineOptions.centerPoint.x + "," + (centerYPointOfBottomPolygon + polygonYOffset) +
            // Left point of the polygon
            " " + (this._lifeLineOptions.centerPoint.x + polygonXOffset) + "," + centerYPointOfBottomPolygon +
            // Top point of the polygon.
            " " + this._lifeLineOptions.centerPoint.x + "," + (centerYPointOfBottomPolygon - polygonYOffset) +
            // Right point of the polygon.
            " " + (this._lifeLineOptions.centerPoint.x - polygonXOffset) + "," + centerYPointOfBottomPolygon;

        this._bottomPolygon = D3Utils.polygon(bottomPolygonPoints, this._lifelineGroup);
        this._bottomPolygon.attr('fill', "#FFFFFF");
        this._bottomPolygon.attr('stroke-width', "1");
        this._bottomPolygon.attr('stroke', "#000000");

        // Add text to bottom polygon.
        this._bottomPolygonText = D3Utils.textElement(this._lifeLineOptions.centerPoint.x, centerYPointOfBottomPolygon,
            this._lifeLineOptions.text.value, this._lifelineGroup)
            .classed(this._lifeLineOptions.text.class, true).classed("genericT",true);

        // Centering the text to the middle of the bottom polygon.
        this._bottomPolygonText.attr('dominant-baseline', "middle");

        // Adding property editor buttons.
        if (_.get(this._lifeLineOptions, "editable", false)) {
            this.addEditableAndDeletable();
        }

        // Drawing bottom polygon.
        // var bottomPolygon = D3Utils.rect(this._lifeLineOptions.centerPoint.x, this._lifeLineOptions.centerPoint.y + (this._lifeLineOptions.centerPoint.y / 2),
        //     this._lifeLineOptions.polygon.width, this._lifeLineOptions.polygon.height,
        //     this._lifeLineOptions.polygon.roundX, this._lifeLineOptions.polygon.roundY, d3.select(this._canvas))
        //     .classed(this._lifeLineOptions.polygon.class, true).classed("genericR",true);
        //
        // bottomPolygon.attr('fill', "#FFFFFF");
        // bottomPolygon.attr('stroke-width', "1");
        // bottomPolygon.attr('stroke', "#000000");
        // bottomPolygon.attr('transform', "rotate(45 100 100)");

        // var centerPoint = this._lifeLineOptions.centerPoint;
        // var title = this._lifeLineOptions.text;
        // var lifeLine = this.drawLifeLine(centerPoint, title, this.options);
        // var viewObj = this;
        // var drag = d3.drag()
        //     .on("start", function () {
        //         viewObj.dragStart(d3.event);
        //     })
        //     .on("drag", function () {
        //         viewObj.dragMove(d3.event);
        //     })
        //     .on("end", function () {
        //         viewObj.dragStop();
        //     });
        //
        // lifeLine.call(drag);
        // this.d3el = lifeLine;
        // this.el = lifeLine.node();
    };

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

    LifeLine.prototype.drawLifeLine = function (center, title, prefs, colour) {
        var d3Ref = this._canvas;
        var viewObj = this;
        var group = D3Utils.group(this._canvas);
        var lifeLineTopRectGroup = group.append("g");
        var topShape;
        var bottomShape;
        var droppableRect;
        var line;
        var polygonYOffset = 30;
        var polygonXOffset = 35;
        this.group = group;
        this.center = center;
        this.title = title;

        // var textModel = this.model.attributes.textModel;
        // if (textModel.dynamicRectWidth() === undefined) {
        //     textModel.dynamicRectWidth(130);
        // }

        lifeLineTopRectGroup.attr('style', 'cursor: pointer');

        if (this._lifeLineOptions.polygon == 'rect') {
            topShape = D3Utils.genericCenteredRect(center, this._lifeLineOptions.polygon.width + 30, this._lifeLineOptions.polygon.height,
                0, 0, lifeLineTopRectGroup, '#FFFFFF', undefined)
                .classed(this._lifeLineOptions.polygon.class, true).classed("genericR", true);
        } else if (viewObj.model.definition.shape == 'polygon') {
            var points = "" + center.x() + "," + (center.y() + polygonYOffset) +
                " " + (center.x() + polygonXOffset) + "," + center.y() +
                " " + center.x() + "," + (center.y() - polygonYOffset) +
                " " + (center.x() - polygonXOffset) + "," + center.y();
            topShape = D3Utils.polygon(points, lifeLineTopRectGroup, textModel, center);
            topShape.classed(viewObj.model.definition.class, true);
        }

        if (viewObj.model.definition.shape == 'rect') {
            droppableRect = d3Ref.draw.centeredBasicRect(createPoint(center.get('x'),
                    center.get('y') + prefs.rect.height / 2),
                prefs.droppableRect.width, prefs.droppableRect.height, 0, 0, group, textModel)
                .classed(prefs.droppableRect.class, true);
            line = d3Ref.draw.verticalLine(createPoint(center.get('x'),
                center.get('y') + prefs.rect.height / 2), prefs.line.height - prefs.rect.height, group, textModel)
                .classed(prefs.line.class, true);
        } else if (viewObj.model.definition.shape == 'polygon') {
            var lenNew = prefs.line.height - 2 * polygonYOffset;
            droppableRect = d3Ref.draw.centeredBasicRect(createPoint(center.get('x'),
                    center.get('y') + polygonYOffset),
                prefs.droppableRect.width, lenNew, 0, 0, group, textModel)
                .classed(prefs.droppableRect.class, true);
            line = d3Ref.draw.verticalLine(createPoint(center.get('x'),
                center.get('y') + polygonYOffset), lenNew, group, textModel)
                .classed(prefs.line.class, true);
        }

        droppableRect.attr('style', 'cursor: pointer');
        droppableRect.on('mouseover', function () {
            viewObj.serviceView.model.selectedNode = viewObj.model;
            d3.select(this).style("fill", "green").style("fill-opacity", 0.1);
            if (!viewObj.serviceView.isPreviewMode()) {
                viewObj.dragDropManager.setActivatedDropTarget(viewObj.model);
            }
        }).on('mouseout', function () {
            viewObj.serviceView.model.destinationLifeLine = viewObj.serviceView.model.selectedNode;
            viewObj.serviceView.model.selectedNode = null;
            d3.select(this).style("fill-opacity", 0.01);
            if (!viewObj.serviceView.isPreviewMode()) {
                viewObj.dragDropManager.clearActivatedDropTarget();
            }
        }).on('mouseup', function (data) {

        });
        var text = d3Ref.draw.genericCenteredText(center, title, group, textModel)
            .classed(prefs.text.class, true).classed("genericT", true);
        var lifeLineBottomRectGroup = group.append("g");
        if (viewObj.model.definition.shape == 'rect') {
            bottomShape = d3Ref.draw.genericCenteredRect(createPoint(center.get('x'),
                    center.get('y') + prefs.line.height), prefs.rect.width + 30,
                prefs.rect.height, 0, 0, lifeLineBottomRectGroup, '', textModel)
                .classed(prefs.rect.class, true).classed("genericR", true);
        } else if (viewObj.model.definition.shape == 'polygon') {
            var points = "" + center.x() + "," + (center.get('y') + prefs.line.height + 30) +
                " " + (center.x() + 35) + "," + (center.get('y') + prefs.line.height) +
                " " + center.x() + "," + (center.get('y') + prefs.line.height - 30) +
                " " + (center.x() - 35) + "," + (center.get('y') + prefs.line.height);
            bottomShape = d3Ref.draw.polygon(points, lifeLineTopRectGroup, textModel, center);
        }
        var textBottom = d3Ref.draw.genericCenteredText(createPoint(center.get('x'), center.get('y') + prefs.line.height), title, group, textModel)
            .classed(prefs.text.class, true).classed("genericT", true);

        if (this.model.type == "EndPoint") {
            topShape.classed("outer-dashed", true);
            bottomShape.classed("outer-dashed", true);
        }

        group.topShape = topShape;
        group.bottomShape = bottomShape;
        group.line = line;
        group.droppableRect = droppableRect;
        group.textBottom = textBottom;
        group.svgTitle = text;
        group.svgTitleBottom = textBottom;
        group.translate = function (dx, dy) {
            this.attr("transform", function () {
                return "translate(" + [dx, dy] + ")"
            })
        };

        if (!_.isUndefined(this.model.definition.editable) && !_.isUndefined(this.model.definition.deletable)
            && this.model.definition.editable && this.model.definition.deletable) {
            this.addEditableAndDeletable(d3Ref, center, prefs, group, droppableRect, lifeLineTopRectGroup);
        }

        return group;
    };

    LifeLine.prototype.addEditableAndDeletable = function () {
        var optionMenuStartX = this._lifeLineOptions.centerPoint.x + 2 + (this._lifeLineOptions.polygon.width + 30) / 2;
        var optionMenuStartY = this._lifeLineOptions.centerPoint.y - this._lifeLineOptions.polygon.height / 2;
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

        this._topPolygonGroup.on("click", (function () {
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