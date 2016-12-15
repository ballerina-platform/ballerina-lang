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
            this._viewOptions.child = _.get(options, "child.value", false);
            this._middleLine = undefined;

            // Make the lifeline uneditable by default
            if (_.get(options, "editable", false)) {
                // TODO : Implement for welcome page.
            }
        }
    };

    LifeLine.prototype.constructor = LifeLine;

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

            if (this._viewOptions.polygon.shape == "rect") {
                this._bottomPolygon.attr("y", parseFloat(this._bottomPolygon.attr("y")) + lineHeightDifference);
            }else if(this._viewOptions.polygon.shape == "diamond"){
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
            }

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
        // a group element is passed for default worker
        if(this._viewOptions.child ){
           this._lifelineGroup = D3Utils.group((this._canvas)).classed("client", true);
        }
        else{
            this._lifelineGroup = D3Utils.group(d3.select(this._canvas)).classed("client", true);
        }

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
            this._bottomPolygon.attr('stroke', "#9d9d9d");

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
            this._topPolygon.attr('stroke', "#9d9d9d");

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

            this._bottomPolygon = D3Utils.centeredRect(new Point(this._viewOptions.centerPoint.x, this._viewOptions.centerPoint.y + this._viewOptions.line.height + 12), this._viewOptions.polygon.width, this._viewOptions.polygon.height , 0, 0, this._lifelineGroup);
            this._bottomPolygon.attr('fill', "#FFFFFF");
            this._bottomPolygon.attr('stroke-width', "1");
            this._bottomPolygon.attr('stroke', "#9d9d9d");

            if(this._viewOptions.text.value == "Resource Worker") {
                this._topPolygon.style('stroke-width', "2");
                this._bottomPolygon.style('stroke-width', "2");
                this._topPolygon.attr('stroke', "#333333");
                this._bottomPolygon.attr('stroke', "#333333");
            }

            // // Add text to bottom polygon.
             this._bottomPolygonText = D3Utils.textElement((this._viewOptions.centerPoint.x + 1), (this._viewOptions.centerPoint.y + this._viewOptions.line.height + 10) ,
                 this._viewOptions.text.value, this._lifelineGroup)
                 .classed(this._viewOptions.text.class, true).classed("genericT", true);
           
            // // Centering the text to the middle of the bottom polygon.
            // Centering the text to the middle of the top polygon.
            this._bottomPolygonText.attr('text-anchor', "middle");
            this._bottomPolygonText.attr('alignment-baseline', "central");
        }

        // Adding property editor buttons.
        if (_.get(this._viewOptions, "editable", false)) {
           // this.addEditableAndDeletable();
        }
         return this._lifelineGroup;
        // TODO : Implement draggable.
    };

    LifeLine.prototype.getMiddleLine = function () {
        return this._middleLine;
    };

    LifeLine.prototype.getViewOptions = function () {
        return this._viewOptions;
    };

    return LifeLine;
});