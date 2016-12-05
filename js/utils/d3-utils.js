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

    var logParentUndefined = function (parent) {
        if (_.isUndefined(parent)) {
            var errMsg = 'Parent Undefined';
            log.warn(errMsg);
            throw errMsg;
        }
    };

    var circleOnPoint = function (point, r, parent) {
        logParentUndefined(parent);
        return parent.draw.circle(point.x(), point.y(), r);
    };

    var rectWithTitle = function (center, width, height, containerWidth, containerHeight, rx, ry, parent, colour, title) {
        logParentUndefined(parent);

        var composite = {};

        var x = center.x() - containerWidth / 2;
        var y = center.y();

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
            .attr("stroke-width", 1)
            .attr("rx", rx)
            .attr("fill-opacity",.4)
            .attr("ry", ry);
        var middleRect = parent.append("rect")
            .attr("id", "middleRect")
            .attr("x", x)
            .attr("y", y + height)
            .attr("width", containerWidth)
            .attr("height", containerHeight)
            .attr("fill", "#ffffff")
            .attr("stroke-width", 1)
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
            .attr("stroke-width", 1)
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
        composite.middleRect = middleRect;
        return composite;
    };

    var polygon = function (points, parent, textModel, center) {

        // get TextModel and if dynamicRectWidth is not 130 add that as width
        var modelId = textModel.cid;
        var x = center.x() - 15;
        var y = center.y();
        var height = 30;
        var width = 30;

        return parent.append('polygon')
            .attr('points', points)
            .attr('stroke-width', 1)
            .attr('fill', "#ffffff")
            .attr('stroke', "#000000")
            .attr('stroke-linejoin', 'round');
    };

    var arrowPolygon = function (points, parent) {

        return parent.append('polygon')
            .attr('points', points)
            .attr('stroke-width', 1)
            .attr('fill', "#333333")
            .attr('stroke', "#000000")
            .attr('stroke-linejoin', 'round');
    };

    var basicRect = function (x, y, width, height, rx, ry, parent) {
        logParentUndefined(parent);
        rx = rx || 0;
        ry = ry || 0;
        return parent.append("rect")
            .attr("x", x)
            .attr("y", y)
            .attr("width", width)
            .attr("height", height)
            .attr("fill-opacity", 0.01)
            .attr("stroke-width", 1)
            .attr("rx", rx)
            .attr("ry", ry);
    };
    var genericRect = function (x, y, width, height, rx, ry, parent, colour, textModel) {
        logParentUndefined(parent);
        // get TextModel and if dynamicRectWidth is not 130 add that as width
        var modelId = textModel.cid;
        var dynamicWidth = textModel.dynamicRectWidth();
        if(dynamicWidth != 130){
            width = dynamicWidth;
        }
        rx = rx || 0;
        ry = ry || 0;
        return parent.append("rect")
            .attr("x", x)
            .attr("y", y)
            .attr("width", width)
            .attr("height", height)
            .attr("fill", colour || "steelblue")
            .attr("stroke", "black")
            .attr("stroke-width", 1)
            .attr("rx", rx)
            .attr("ry", ry)
            .attr("id",modelId);
    };

    var genericCenteredRect = function (center, width, height, rx, ry, parent, colour, textModel) {
        logParentUndefined(parent);
        rx = rx || 0;
        ry = ry || 0;
        return parent.draw.genericRect(center.x() - width / 2, center.y() - height / 2, width, height, rx, ry, parent, colour, textModel);
    };

    var genericTextRect = function (center,width,height,rx,ry,textContent,x,y,parent,colour, textModel){
        logParentUndefined(parent);

        var rect =parent.draw.genericRect(center.x() - width / 2, center.y(), width, height, rx, ry, parent, colour, textModel);
        var text = rect.draw.genericTextElement(center.x(), center.y(), textContent, rect,textModel)
            .attr('text-anchor', 'middle').attr('dominant-baseline', 'middle');
        return parent;
    };

    var rect = function (x, y, width, height, rx, ry, parent, colour) {
        logParentUndefined(parent);

        rx = rx || 0;
        ry = ry || 0;
        return parent.append("rect")
            .attr("x", x)
            .attr("y", y)
            .attr("width", width)
            .attr("height", height)
            .attr("fill", colour || "#000000")
            .attr("stroke", "black")
            .attr("stroke-width", 1)
            .attr("rx", rx)
            .attr("ry", ry);
    };

    var centeredRect = function (center, width, height, rx, ry, parent, colour) {
        logParentUndefined(parent);
        rx = rx || 0;
        ry = ry || 0;
        return parent.draw.rect(center.x() - width / 2, center.y(), width, height, rx, ry, parent, colour);
    };


    var centeredBasicRect = function (center, width, height, rx, ry, parent) {
        logParentUndefined(parent);
        rx = rx || 0;
        ry = ry || 0;
        return parent.draw.basicRect(center.x() - width / 2, center.y(), width, height, rx, ry, parent);
    };

    var line = function (x1, y1, x2, y2, parent) {
        logParentUndefined(parent);
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
        logParentUndefined(parent);
        return parent.draw.line(startPoint.x(), startPoint.y(), endPoint.x(), endPoint.y());
    };

    var verticalLine = function (start, height, parent) {
        logParentUndefined(parent);
        return parent.draw.line(start.x(), start.y(), start.x(), start.y() + height, parent);
    };

    var editableText = function (x, y, text) {

    };

    var genericTextElement = function (x, y, textContent, parent,txtModel) {
        logParentUndefined(parent);
        var modelId = txtModel.cid;
        var dynamicPosition = txtModel.dynamicTextPosition();
        if(dynamicPosition != undefined){
            x = dynamicPosition;
        }

        return parent.append("text")
            .attr("x", x)
            .attr("y", y)
            .attr("id",modelId)
            .text(function () {
                return textContent;
            });
    };

    var genericCenteredText = function (center, textContent, parent,txtModel) {
        logParentUndefined(parent);
        return parent.draw.genericTextElement(center.x(), center.y(), textContent, parent,txtModel)
            .attr('text-anchor', 'middle').attr('dominant-baseline', 'middle');
    };
    var textElement = function (x, y, textContent, parent) {
        logParentUndefined(parent);
        return parent.append("text")
            .attr("x", x)
            .attr("y", y)
            .text(function () {
                return textContent;
            });
    };

    var inputTriangle = function (x, y, parent) {
        logParentUndefined(parent);
        var points = "" + x + "," + (y - 5) + " " + (x + 5) + "," + (y) + " " + x + "," + (y + 5);
        return parent.append("polyline")
            .attr("points", points);
    };

    var outputTriangle = function (x, y, parent) {
        logParentUndefined(parent);
        var points = "" + x + "," + y + " " + (x + 5) + "," + (y - 5) + " " + (x + 5) + "," + (y + 5);
        return parent.append("polyline")
            .attr("points", points);
    };

    var dashedLine = function (x1,y1, x2, y2, color, parent) {
        logParentUndefined(parent);
        return parent.append("line")
            .attr("x1", x1)
            .attr("y1", y1)
            .attr("x2", x2)
            .attr("y2", y2)
            .attr("stroke", color)
            .attr("stroke-width", 1)
            .attr("stroke-dasharray", "4, 3");
    };

    var circle = function (x, y, radius, parent, colour) {
        logParentUndefined(parent);

        var circle = parent.append("circle")
            .attr("cx", x )
            .attr("cy", y )
            .attr("r", radius)
            .attr("fill-opacity", 0)
            .attr("fill", colour);
        return circle;
    };

    var centeredText = function (center, textContent, parent) {
        logParentUndefined(parent);
        return parent.draw.textElement(center.x(), center.y(), textContent, parent)
            .attr('text-anchor', 'middle').attr('dominant-baseline', 'middle');
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
        logParentUndefined(parent);

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

    var updateParentOnLayoutChange = function () {
        if (defaultView.selectedNode.attributes.textModel != null) {
            model = defaultView.selectedNode.attributes.textModel;
            // updating any parent elements if exists:TODO: can be updated to be fired onBlur
            if (model.hasParent === true) {
                // This could be made into a objectList if there are multiple
                var parentModel = model.parentObject();
                eventManager.notifyParent(parentModel, model);
            }
        }
    };
    
    /**
     * Save properties in selected element's model by calling saveMyProperties method in respective elements
     */
    var saveProperties = function () {
        var inputs = $('#property-form')[0].getElementsByTagName("input");
        defaultView.selectedNode.get("utils").saveMyProperties(defaultView.selectedNode, inputs);
        //TODO FOR TEXT GENERIC
        if(defaultView.selectedNode.attributes.textModel != null){
            var int = Number(7) || 7.7;
            var dlength =  ((inputs.title.value.length+1) * 8);
            var txtm = defaultView.selectedNode.attributes.textModel;
            txtm.textChanged(dlength);
        }
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
        logParentUndefined(parent);
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
        logParentUndefined(parent);
        var textBox = parent.append("input")
            .attr("class", "property-textbox")
            .attr("type", "text")
            .attr("name", name)
            .attr("value", value);

        textBox.on("keyup", saveProperties)
            .on("dblclick", function () {
                this.select();
            });
        textBox.on("blur", updateParentOnLayoutChange);
        parent.append("br");
        parent.append("br");
    };

    var appendLabel = function (parent, value) {
        logParentUndefined(parent);
        return parent.append("label")
            .attr("class", "property-label")
            .text(value);
    };

    var appendDropdown = function (parent, optionsList, name, count, selectedValue) {
        logParentUndefined(parent);
        var input = parent.append("input")
            .attr("name", name)
            .attr("id", name)
            .attr("class", "property-dropdown")
            .attr("list", "dropdown-" + count)
            .attr("value", selectedValue);

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

        parent.append("br");
        parent.append("br");
        var currentValue = "";
        input.on("click", function () {
                if (this.value !== "") {
                    currentValue = this.value;
                }
                this.value = "";
            })
            .on("change", saveProperties)
            .on("blur", function () {
                if (this.value === "") {
                    this.value = currentValue;
                }
            })
    };

    var group = function (parent) {
        logParentUndefined(parent);
        return parent.append("g");
    };

    var svg = function (opts, parent) {
        logParentUndefined(parent);
        return parent.append("svg")
            .attr("height", opts.height)
            .attr("width", opts.width)
            .attr("class", opts.class);
    };

    var propertySVG = function (opts, parent) {
        logParentUndefined(parent);
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
        logParentUndefined(parent);
        rx = rx || 0;
        ry = ry || 0;
        return parent.draw.rect(center.x() - width / 2, center.y(), width, height, rx, ry, parent, colour);
    };

    var draw = {};
    draw.centeredRect = centeredRect;
    draw.rect = rect;
    draw.genericCenteredRect = genericCenteredRect;
    draw.genericRect = genericRect;
    draw.basicRect = basicRect;
    draw.centeredBasicRect = centeredBasicRect;
    draw.line = line;
    draw.lineFromPoints = lineFromPoints;
    draw.verticalLine = verticalLine;
    draw.editableText = editableText;
    draw.centeredText = centeredText;
    draw.textElement = textElement;
    draw.genericCenteredText = genericCenteredText;
    draw.genericTextElement = genericTextElement;
    draw.circle = circle;
    draw.circleOnPoint = circleOnPoint;
    draw.group = group;
    draw.svg = svg;
    draw.propertySVG = propertySVG;
    draw.rectWithTitle = rectWithTitle;
    draw.regroup = regroup;
    draw.propertyRect = propertyRect;
    draw.form = form;
    draw.inputTriangle = inputTriangle;
    draw.outputTriangle = outputTriangle;
    draw.dashedLine = dashedLine;
    draw.polygon = polygon;
    draw.arrowPolygon = arrowPolygon;

    return draw;

});
