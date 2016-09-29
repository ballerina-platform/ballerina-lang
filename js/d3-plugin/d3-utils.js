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

var D3Utils = (function (d3_utils) {

    var d3Ref = undefined;

    var circle = function (cx, cy, r, parent) {
        parent = parent || d3Ref;
        return parent.append("circle")
            .attr("cx", cx)
            .attr("cy", cy)
            .attr("r", r);
    };

    var circleOnPoint = function (point, r, parent) {
        parent = parent || d3Ref;
        return parent.draw.circle(point.x(), point.y(), r);
    };

    var rectWithTitle = function (center, width, height, containerWidth, containerHeight, rx, ry, parent, colour, title) {
        parent = parent || d3Ref;

        x = center.x() - containerWidth / 2;
        y = center.y() - height / 2;

        rx = rx || 0;
        ry = ry || 0;
        parent.append("rect")
            .attr("id", "titleRect")
            .attr("x", x)
            .attr("y", y)
            .attr("width", width)
            .attr("height", height)
            .attr("fill", colour)
            .attr("stroke", "black")
            .attr("stroke-width", 2)
            .attr("fill-opacity", 0.2)
            .attr("rx", rx)
            .attr("ry", ry);
        var containerRect = parent.append("rect")
            .attr("id", "containerRect")
            .attr("x", x)
            .attr("y", y)
            .attr("width", containerWidth)
            .attr("height", containerHeight)
            .attr("fill", "grey")
            .attr("stroke", "black")
            .attr("stroke-width", 2)
            .attr("rx", rx)
            .attr("fill-opacity", 0.2)
            .attr("ry", ry);
        parent.append("text")
            .attr("x", x + 20)
            .attr("y", y + 19)
            .attr("fill", "black")
            .text(title);
        return containerRect;
    };

    var basicRect = function (x, y, width, height, rx, ry, parent) {
        parent = parent || d3Ref;
        rx = rx || 0;
        ry = ry || 0;
        return parent.append("rect")
            .attr("x", x)
            .attr("y", y)
            .attr("width", width)
            .attr("height", height)
            .attr("fill-opacity", 0.01)
            .attr("stroke-width", 2)
            //.style("filter", "url(#drop-shadow)")
            .attr("rx", rx)
            .attr("ry", ry);
    };

    var rect = function (x, y, width, height, rx, ry, parent, colour) {
        parent = parent || d3Ref;

        rx = rx || 0;
        ry = ry || 0;
        return parent.append("rect")
            .attr("x", x)
            .attr("y", y)
            .attr("width", width)
            .attr("height", height)
            .attr("fill", colour || "steelblue")
            .attr("stroke-width", 2)
            .attr("rx", rx)
            .attr("ry", ry);
    };

    var centeredRect = function (center, width, height, rx, ry, parent, colour) {
        parent = parent || d3Ref;
        rx = rx || 0;
        ry = ry || 0;
        return parent.draw.rect(center.x() - width / 2, center.y() - height / 2, width, height, rx, ry, parent, colour);
    };


    var centeredBasicRect = function (center, width, height, rx, ry, parent) {
        parent = parent || d3Ref;
        rx = rx || 0;
        ry = ry || 0;
        return parent.draw.basicRect(center.x() - width / 2, center.y() - height / 2, width, height, rx, ry, parent);
    };

    var line = function (x1, y1, x2, y2, parent) {
        parent = parent || d3Ref;
        return parent.append("line")
            .attr("x1", x1)
            .attr("y1", y1)
            .attr("x2", x2)
            .attr("y2", y2);
    };

    /**
     * Draws a line between two points.
     * @param {Point} startPoint starting point for the line.
     * @param {Point} endPoint ending point for the line.
     * @param [parent] parent element.
     */
    var lineFromPoints = function (startPoint, endPoint, parent) {
        parent = parent || d3Ref;
        return parent.draw.line(startPoint.x(), startPoint.y(), endPoint.x(), endPoint.y());
    };

    var verticalLine = function (start, height, parent) {
        parent = parent || d3Ref;
        return parent.draw.line(start.x(), start.y(), start.x(), start.y() + height, parent);
    };

    var editableText = function (x, y, text) {

    };

    var textElement = function (x, y, textContent, parent) {
        parent = parent || d3Ref;
        return parent.append("text")
            .attr("x", x)
            .attr("y", y)
            .text(function () {
                return textContent;
            });
    };

    var centeredText = function (center, textContent, parent) {
        parent = parent || d3Ref;
        return parent.draw.textElement(center.x(), center.y(), textContent, parent)
            .attr('text-anchor', 'middle').attr('dominant-baseline', 'middle');
    };


    var group = function (parent) {
        parent = parent || d3Ref;
        return parent.append("g");
    };

    var svg = function (opts, parent) {
        parent = parent || d3Ref;
        return parent.append("svg")
            .attr("height", opts.height)
            .attr("width", opts.width)
            .attr("class", opts.class);
    };

    // FIXME: refactor to use native window methods
    var regroup = function (elements) {
        var g = d3Ref.append("g");
        var $g = jQuery(g.node());
        elements.forEach(function (element) {
            var $e = jQuery(element.node());
            $g.append($e.detach())
        });
        return g;
    };

    var decorate = function (d3ref) {
        if (typeof d3ref === 'undefined') {
            throw "undefined d3 ref.";
        }
        var draw = {};
        draw.centeredRect = centeredRect;
        draw.rect = rect;
        draw.basicRect = basicRect;
        draw.centeredBasicRect = centeredBasicRect;
        draw.line = line;
        draw.lineFromPoints = lineFromPoints;
        draw.verticalLine = verticalLine;
        draw.editableText = editableText;
        draw.centeredText = centeredText;
        draw.textElement = textElement;
        draw.circle = circle;
        draw.circleOnPoint = circleOnPoint;
        draw.group = group;
        draw.svg = svg;
        draw.rectWithTitle = rectWithTitle;

        var d3Proto = Object.getPrototypeOf(d3ref);
        d3Proto.draw = draw;

        return d3Ref = d3ref;
    };

    var d3 = function () {
        return d3Ref;
    };

    d3_utils.decorate = decorate;

    return d3_utils;

}(D3Utils || {}));