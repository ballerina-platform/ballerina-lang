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

define(['require', 'lodash', 'jquery', 'd3', 'log'], function (require, _, $, d3, log) {
    /**
     * Adding the generic util functions for rendering basic shapes
     */

    /**
     * Check whether the parent is undefined or not.
     * @param parent
     * @throws error message
     */
    var logParentUndefined = function (parent) {
        if (_.isUndefined(parent)) {
            var errMsg = 'Parent Undefined';
            log.warn(errMsg);
            throw errMsg;
        }
    };

    /**
     * Draws a basic svg rectangle
     * @param x
     * @param y
     * @param width
     * @param height
     * @param rx
     * @param ry
     * @param parent
     * @returns basic svg rectangle
     */
    var rect = function (x, y, width, height, rx, ry, parent) {
        logParentUndefined(parent);
        rx = rx || 0;
        ry = ry || 0;
        return parent.append("rect")
            .attr("x", x)
            .attr("y", y)
            .attr("width", width)
            .attr("height", height)
            .attr("rx", rx)
            .attr("ry", ry);
    };

    /**
     * Draws a basic svg circle
     * @param x
     * @param y
     * @param radius
     * @param parent
     * @returns basic svg circle
     */
    var circle = function (x, y, radius, parent) {
        logParentUndefined(parent);
        var circle = parent.append("circle")
            .attr("cx", x )
            .attr("cy", y )
            .attr("r", radius);
        return circle;
    };

    /**
     * Draws a basic centered svg rectangle
     * @param center
     * @param width
     * @param height
     * @param rx
     * @param ry
     * @param parent
     * @returns centered rectangle
     */
    var centeredRect = function (center, width, height, rx, ry, parent) {
        logParentUndefined(parent);
        rx = rx || 0;
        ry = ry || 0;
        return this.rect(center.x() - width / 2, center.y() - height / 2, width, height, rx, ry, parent);
    };

    /**
     * Draw a generic svg line
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param parent
     * @returns Generic svg line
     */
    var line = function (x1, y1, x2, y2, parent) {
        logParentUndefined(parent);
        return parent.append("line")
            .attr("x1", x1)
            .attr("y1", y1)
            .attr("x2", x2)
            .attr("y2", y2);
    };

    /**
     * Draw a generic svg line
     * @param start {Point}
     * @param end {Point}
     * @param parent
     * @returns Generic svg line
     */
    var lineFromPoints = function (start, end, parent) {
        logParentUndefined(parent);
        return parent.append("line")
            .attr("x1", start.x())
            .attr("y1", start.y())
            .attr("x2", end.x())
            .attr("y2", end.y());
    };

    /**
     * Draw a generic svg text Element
     * @param x
     * @param y
     * @param textContent
     * @param parent
     * @returns svg text element
     */
    var textElement = function (x, y, textContent, parent) {
        logParentUndefined(parent);
        return parent.append("text")
            .attr("x", x)
            .attr("y", y)
            .text(function () {
                return textContent;
            });
    };

    /**
    * Draw a generic svg input Element
    * @param x
    * @param y
    * @param textContent
    * @param parent
    * @returns svg text element
    */
    var inputElement = function (x, y, textContent, parent) {
        logParentUndefined(parent);
        return parent.append("input")
            .attr("x", x)
            .attr("y", y)
            .text(function () {
                return textContent;
            });
    };

    /**
     * Draw a centered svg text Element on a given point
     *
     * @param center {Point}
     * @param text {String}
     * @param parent {SVGElement}
     * @returns svg text element
     */
    var centeredText = function (center, text, parent) {
        logParentUndefined(parent);
        return parent.append("text")
            .attr("x", center.x())
            .attr("y", center.y())
            .attr('text-anchor', "middle")
            .attr('alignment-baseline', "central")
            .text(function () {
                return text;
            });
    };

    /**
     * Append a svg group to the given parent
     * @param parent
     * @returns svg group
     */
    var group = function (parent) {
        logParentUndefined(parent);
        return parent.append("g");
    };

    /**
     * Append a generic svg to the provided parent element
     * @param opts
     * @param parent
     * @returns svg
     */
    var svg = function (opts, parent) {
        logParentUndefined(parent);
        return parent.append("svg")
            .attr("height", opts.height)
            .attr("width", opts.width)
            .attr("class", opts.class);
    };

    /**
     * Draw a right pointed arrow head
     * @param x
     * @param y
     * @param parent
     * @returns polyline generating an arrow head
     */
    var inputTriangle = function (x, y, parent) {
        logParentUndefined(parent);
        var points = "" + x + "," + (y - 5) + " " + (x + 5) + "," + (y) + " " + x + "," + (y + 5);
        return parent.append("polyline")
            .attr("points", points);
    };

    /**
     * Draw a left pointed arrow head
     * @param x
     * @param y
     * @param parent
     * @returns polyline generating an arrow head
     */
    var outputTriangle = function (x, y, parent) {
        logParentUndefined(parent);
        var points = "" + x + "," + y + " " + (x + 5) + "," + (y - 5) + " " + (x + 5) + "," + (y + 5);
        return parent.append("polyline")
            .attr("points", points);
    };

    /**
     * Draw a dashed line
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param color
     * @param parent
     * @returns generic line which is dashed
     */
    var dashedLine = function (x1,y1, x2, y2, color, parent) {
        logParentUndefined(parent);
        return parent.append("line")
            .attr("x1", x1)
            .attr("y1", y1)
            .attr("x2", x2)
            .attr("y2", y2)
            .attr("stroke-dasharray", "4, 3");
    };

    /**
     * Draw a generic polygon
     * @param points
     * @param parent
     * @returns polygon generated by the given points
     */
    var polygon = function (points, parent) {
        logParentUndefined(parent);
        return parent.append('polygon')
            .attr('points', points);
    };

    /**
     * Draw a generic polyline
     * @param points
     * @param parent
     * @returns polyline generated by the given points
     */
    var polyline = function (points, parent) {
        logParentUndefined(parent);
        return parent.append('polyline')
            .attr('points', points);
    };

    var draw = {};
    draw.centeredRect = centeredRect;
    draw.rect = rect;
    draw.line = line;
    draw.lineFromPoints = lineFromPoints;
    draw.textElement = textElement;
    draw.centeredText = centeredText;
    draw.circle = circle;
    draw.group = group;
    draw.svg = svg;
    draw.inputTriangle = inputTriangle;
    draw.outputTriangle = outputTriangle;
    draw.dashedLine = dashedLine;
    draw.polygon = polygon;
    draw.polyline = polyline;

    return draw;
});
