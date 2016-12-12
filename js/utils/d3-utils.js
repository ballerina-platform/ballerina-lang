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

    var heights = {
        textBox: 30,
        before: 20,
        after: 36,
        label: 12,
        label_textBox: 10,
        textBox_label: 24,
        textBox_checkbox: 24,
        checkbox: 13,
        checkbox_checkbox: 35
    };

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
     * Create the properties form by appending form elements such as textbox, label, checkbox etc.
     * @param parent SVG parent element of the form
     * @param parameters Model parameters of the selected element
     * @param propertyPaneSchema Property rendering schema of the selected element
     * @param rect Property window rectangle
     * @returns {*} Created form element
     */
    var form = function (parent, parameters, propertyPaneSchema, rect) {
        var foreignObject = parent.append("foreignObject")
            .attr("x", 23)
            .attr("y", 20)
            .attr("width", "240")
            .attr("height", "100%");

        var form = foreignObject.append("xhtml:form")
            .attr("id", "property-form")
            .attr("autocomplete", "on");

        var previous = null;
        var next = null;
        var rectangleHeight = heights.before;

        for (var i = 0; i < propertyPaneSchema.length; i++) {
            var property = propertyPaneSchema[i];

            if (property.text) {
                //append a label for the textbox
                next = "label";
                rectangleHeight = getCurrentRectHeight(rectangleHeight, previous, next);
                var label = appendLabel(form, property.text);
                label.attr("style", "display: block;");
                //append a textbox
                appendTextBox(form, parameters[i].value, property.key);
                rectangleHeight += (heights.label + heights.label_textBox + heights.textBox);
                previous = "textbox";

            } else if (property.dropdown) {
                //append a label before dropdown
                next = "label";
                rectangleHeight = getCurrentRectHeight(rectangleHeight, previous, next);
                var label = appendLabel(form, property.dropdown);
                label.attr("style", "display: block;");
                //append the dropdown
                var selected = parameters[i].value;
                var dropdownValues = [];
                property.values.forEach(function (value, index) {
                    if (value === selected) {
                        dropdownValues[index] = {value: value.toLowerCase(), text: value, selected: true};
                    } else {
                        dropdownValues[index] = {value: value.toLowerCase(), text: value};
                    }
                });
                appendDropdown(form, dropdownValues, property.key, i, selected);
                rectangleHeight += (heights.label + heights.label_textBox + heights.textBox);
                previous = "dropdown";

            } else if (property.checkbox) {
                //append a checkbox
                next = "checkbox";
                rectangleHeight = getCurrentRectHeight(rectangleHeight, previous, next);
                appendCheckBox(form, property, parameters[i].value);
                rectangleHeight += heights.checkbox;
                previous = "checkbox";

            }
        }
        rectangleHeight += heights.after;
        rect.attr("height", rectangleHeight);
        return form;
    };

    var getCurrentRectHeight = function (currentHeight, previous, next) {
        if (previous === "textbox" || previous === "dropdown") {
            if (next === "label") {
                return currentHeight + heights.textBox_label;

            } else if (next === "checkbox") {
                return currentHeight + heights.textBox_checkbox;

            }

        } else if (previous === "checkbox") {
            if (next === "checkbox") {
                return currentHeight + heights.checkbox_checkbox
            }

        } else if (!previous) {
            return currentHeight;

        }
    };

    var appendLabel = function (parent, value) {
        return parent.append("label")
            .attr("class", "property-label")
            .text(value);
    };

    var appendCheckBox = function (parent, property, checked) {
        var checkbox = parent.append("xhtml:input")
            .attr("class", "property-checkbox")
            .attr("type", "checkbox")
            .attr("name", property.key);
        if (checked) {
            checkbox.attr("checked", true);
        }

        checkbox.on("change", function (e) {
            e = e || event;
            saveProperties();
            e.stopPropagation();
        }).on("click", function (e) {
            e = e || event;
            e.stopPropagation();
        });
        appendLabel(parent, property.checkbox);
        parent.append("br");
        parent.append("br");
    };



    var draw = {};
    draw.centeredRect = centeredRect;
    draw.rect = rect;
    draw.line = line;
    draw.textElement = textElement;
    draw.circle = circle;
    draw.group = group;
    draw.svg = svg;
    draw.inputTriangle = inputTriangle;
    draw.outputTriangle = outputTriangle;
    draw.dashedLine = dashedLine;
    draw.polygon = polygon;

    return draw;
});
