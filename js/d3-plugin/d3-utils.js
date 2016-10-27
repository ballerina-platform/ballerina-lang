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

        var composite = {};

        x = center.x() - containerWidth / 2;
        y = center.y() - height / 2;

        rx = rx || 0;
        ry = ry || 0;
        var containerRect = parent.append("rect")
            .attr("id", "containerRect")
            .attr("x", x)
            .attr("y", y)
            .attr("width", containerWidth)
            .attr("height", containerHeight)
            .attr("fill", "#ffffff")
            .attr("stroke", "black")
            .attr("stroke-width", 2)
            .attr("rx", rx)
            .attr("fill-opacity",.4)
            .attr("ry", ry);
        var titleRect = parent.append("rect")
            .attr("id", "titleRect")
            .attr("x", x)
            .attr("y", y)
            .attr("width", width)
            .attr("height", height)
            .attr("fill", "#ffffff")
            .attr("stroke", "black")
            .attr("stroke-width", 2)
            .attr("fill-opacity", 1)
            .attr("rx", rx)
            .attr("ry", ry);
        var text = parent.append("text")
            .attr("x", x + 20)
            .attr("y", y + 19)
            .attr("fill", "black")
            .text(title);
        composite.containerRect = containerRect;
        composite.titleRect = titleRect;
        composite.text = text;
        return composite;
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
            .attr("stroke", "black")
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

    /**
     * Create the properties form by appending form elements such as textbox, label, checkbox etc.
     * @param parent SVG parent element of the form
     * @param parameters Model parameters of the selected element
     * @param propertyPaneSchema Property rendering schema of the selected element
     * @param rect Property window rectangle
     * @param rectY Rectangle top y coordinate
     * @returns {*} Created form element
     */
    var form = function (parent, parameters, propertyPaneSchema, rect, rectY) {
        parent = parent || d3Ref;

        var foreignObject = parent.append("foreignObject")
            .attr("x", 23)
            .attr("y", 20)
            .attr("width", "200")
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
                //append a textbox
                next = "label";
                rectangleHeight = getCurrentRectHeight(rectangleHeight, previous, next);
                appendLabel(form, property.text);
                appendTextBox(form, parameters[i].value, property.key);
                rectangleHeight += (heights.label + heights.label_textBox + heights.textBox);
                previous = "textbox";

            } else if (property.dropdown) {
                //append a dropdown
                next = "label";
                rectangleHeight = getCurrentRectHeight(rectangleHeight, previous, next);
                appendLabel(form, property.dropdown);
                var selected = parameters[i].value;
                var dropdownValues = [];
                property.values.forEach(function (value, index) {
                    if (value === selected) {
                        dropdownValues[index] = {value: value.toLowerCase(), text: value, selected: true};
                    } else {
                        dropdownValues[index] = {value: value.toLowerCase(), text: value};
                    }
                });
                appendDropdown(form, dropdownValues, property.key, i);
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

    /**
     * Save properties in selected element's model by calling saveMyProperties method in respective elements
     */
    var saveProperties = function () {
        var inputs = $('#property-form')[0].getElementsByTagName("input");
        defaultView.selectedNode.get("saveMyProperties").saveMyProperties(defaultView.selectedNode, inputs);

        //render title in selected lifeline
        if (inputs.title) {
            resetMainElementTitle(inputs.title.value);
        }
    };

    var resetMainElementTitle = function (title) {
        diagram.selectedMainElementText.top.text(title);
        diagram.selectedMainElementText.bottom.text(title);
    };

    var appendCheckBox = function (parent, property, checked) {
        var checkbox = parent.append("xhtml:input")
            .attr("class", "property-checkbox")
            .attr("type", "checkbox")
            .attr("name", property.key);
        if (checked) {
            checkbox.attr("checked", true);
        }

        checkbox.on("change", saveProperties);
        appendLabel(parent, property.checkbox);
        parent.append("br");
        parent.append("br");
    };

    var appendTextBox = function (parent, value, name) {
        var textBox = parent.append("input")
            .attr("class", "property-textbox")
            .attr("type", "text")
            .attr("name", name)
            .attr("value", value);

        textBox.on("keyup", saveProperties)
            .on("dblclick", function () {
                this.select();
            });

        parent.append("br");
        parent.append("br");
    };

    var appendLabel = function (parent, value) {
        parent.append("label")
            .attr("class", "property-label")
            .text(value);
    };

    var appendDropdown = function (parent, optionsList, name, count) {
        var input = parent.append("input")
            .attr("name", name)
            .attr("id", name)
            .attr("class", "property-dropdown")
            .attr("list", "dropdown-" + count);

        var datalist = input.append("datalist")
            .attr("id", "dropdown-" + count);

        for (var i = 0; i < optionsList.length; i++) {
            var option = optionsList[i];
            datalist.append("option")
                .attr("label", option.value)
                .attr("class", "property-option")
                .attr("value", option.value);
            if (option.selected) {
                input._groups[0][0].value = option.value;
            }
        }

        input.on("change", saveProperties);
        parent.append("br");
        parent.append("br");

        input.on("click", function () {
            this.focus();
            this.value = "";
        });
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

    var propertySVG = function (opts, parent) {
        parent = parent || d3Ref;
        return parent.append("svg")
            .attr("id", opts.id)
            .attr("height", opts.height)
            .attr("width", opts.width)
            .attr("class", opts.class)
            .attr("x", opts.x)
            .attr("y", opts.y)
            .attr("fill", opts.color)
            .attr("z-index", 1500);
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

    var propertyRect = function (center, width, height, rx, ry, parent, colour) {
        parent = parent || d3Ref;
        rx = rx || 0;
        ry = ry || 0;
        return parent.draw.rect(center.x() - width / 2, center.y() - height / 2, width, height, rx, ry, parent, colour);
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
        draw.propertySVG = propertySVG;
        draw.rectWithTitle = rectWithTitle;
        draw.regroup = regroup;
        draw.propertyRect = propertyRect;
        draw.form = form;

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
