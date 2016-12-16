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
define(['lodash', 'log', 'jquery', 'd3', 'd3utils', './../visitors/ast-visitor', './../ast/node', 'app/diagram-core/models/point', './bounding-box'],
    function (_, log, $, d3, D3Utils, ASTVisitor, BBox) {

        /**
         * A common class which consists functions of moving or resizing views.
         * @param {Object} args - Arguments for creating the view.
         * @param {ASTNode} args.model - Any ASTNode as the model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @param {ToolPalette} args.toolPalette - reference for tool palette
         * @constructor
         */
        var BallerinaView = function (args) {
            this._parent = _.get(args, "parent");
            this._model = _.get(args, "model");
            this._container = _.get(args, "container");
            this._viewOptions = _.get(args, "viewOptions", {});
            this._boundingBox = new BBox();
            this.toolPalette = _.get(args, "toolPalette");
            this.messageManager =  _.get(args, "messageManager");
            ASTVisitor.call(this, args);
        };

        BallerinaView.prototype = Object.create(ASTVisitor.prototype);
        BallerinaView.prototype.constructor = BallerinaView;

        BallerinaView.prototype.setParent = function (parent) {
            this._parent = parent;
        };
        BallerinaView.prototype.getParent = function () {
            return this._parent;
        };

        /**
         * Creates the property pane for a given html element according to a model's properties. Here a model is an AST
         * node.
         * @param {Object} args - The arguments json object for creating a property pane.
         * @param {ASTNode} args.model - The model(the AST node).
         * @param {SVGElement|HTMLElement} args.htmlElement - The html element to which the property will be added to.
         * @param {Object} args.viewOptions - Configuration related to the view.
         * @param {Point} args.viewOptions.position - The position at which the property pane to be created.
         */
        BallerinaView.prototype.createPropertyPane = function (args) {
            var model = _.get(args, "model");
            var activatorElement = _.get(args, "activatorElement");
            var paneAppendElement = _.get(args, "paneAppendElement");
            var editableProperties = _.get(args, "editableProperties", []);

            if (_.isNil(activatorElement)) {
                log.error("Unable to render property pane as the html element is undefined." + activatorElement);
                throw "Unable to render property pane as the html element is undefined." + activatorElement;
            }

            var viewOptions = _.get(args, "viewOptions", {});
            viewOptions.position = _.get(args, "viewOptions.position", {});
            viewOptions.position.x = _.get(args, "viewOptions.position.x", 50);
            viewOptions.position.y = _.get(args, "viewOptions.position.y", 0);

            viewOptions.actionButton = _.get(args, "viewOptions.actionButton", {});
            viewOptions.actionButton.class = _.get(args, "actionButton.class", "property-pane-action-button");
            viewOptions.actionButton.wrapper = _.get(args, "actionButton.wrapper", {});
            viewOptions.actionButton.wrapper.class = _.get(args, "actionButton.wrapper.class", "property-pane-action-button-wrapper");
            viewOptions.actionButton.enableEdit = _.get(args, "viewOptions.actionButton.enableEdit", true);
            viewOptions.actionButton.enableDisabling = _.get(args, "viewOptions.actionButton.enableDisabling", false);
            viewOptions.actionButton.enableDelete = _.get(args, "viewOptions.actionButton.enableDelete", true);
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

            $(activatorElement).click(function () {

                // if (_.isNil(this.getAttribute("data-showing-property-menu")) || this.getAttribute("data-showing-property-menu") == "false") {
                //     this.setAttribute("data-showing-property-menu", "true");
                // } else {
                //     return;
                // }

                // Calculating the width of the action button pane.
                var propertyButtonPaneRectWidth = viewOptions.actionButton.enableEdit ? viewOptions.actionButton.width : 0;
                propertyButtonPaneRectWidth += viewOptions.actionButton.enableDisabling ? viewOptions.actionButton.width : 0;
                propertyButtonPaneRectWidth += viewOptions.actionButton.enableDelete ? viewOptions.actionButton.width : 0;

                var propertyButtonPaneSVG = activatorElement;
                if (activatorElement instanceof HTMLElement) {
                    var svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
                    svg.setAttribute("x", $(activatorElement).offset().left);
                    svg.setAttribute("y", $(activatorElement).offset().top);
                    svg.setAttribute("width", propertyButtonPaneRectWidth);
                    svg.setAttribute("height", viewOptions.actionButton.height);
                    propertyButtonPaneSVG = $(svg).prependTo(activatorElement.parentElement);
                } else {
                    propertyButtonPaneSVG = activatorElement.parentElement;
                }

                //

                var propertyButtonPaneGroup = D3Utils.group(d3.select(propertyButtonPaneSVG));

                // Creating the action button pane.
                var propertyButtonPaneRect = D3Utils.rect(viewOptions.position.x - (propertyButtonPaneRectWidth / 2), viewOptions.position.y + 3,
                    propertyButtonPaneRectWidth, viewOptions.actionButton.height, 0, 0, propertyButtonPaneGroup)
                    .classed(viewOptions.actionButton.wrapper.class, true);

                var svgDefinitions = d3.select(propertyButtonPaneSVG).append("defs");

                var editButtonRect;
                var disableButtonRect;
                var deleteButtonRect;
                if (viewOptions.actionButton.enableEdit) {
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

                    // Creating the edit action button.
                    editButtonRect = D3Utils.rect(viewOptions.position.x - (propertyButtonPaneRectWidth / 2), viewOptions.position.y + 3,
                        viewOptions.actionButton.width, viewOptions.actionButton.height, 0, 0, propertyButtonPaneGroup)
                        .classed(viewOptions.actionButton.class, true).classed(viewOptions.actionButton.editClass, true);
                }

                if (viewOptions.actionButton.enableDisabling) {
                    var disableButtonPattern = svgDefinitions.append("pattern")
                        .attr("id", "disableIcon")
                        .attr("width", "100%")
                        .attr("height", "100%");

                    disableButtonPattern.append("image")
                    // TODO : svg image is missing
                        .attr("xlink:href", "images/disable.svg")
                        .attr("x", (viewOptions.actionButton.width / 2) - (14 / 2))
                        .attr("y", (viewOptions.actionButton.height / 2) - (14 / 2))
                        .attr("width", "14")
                        .attr("height", "14");

                    var disableButtonXPosition = viewOptions.position.x + (viewOptions.actionButton.enableEdit ? viewOptions.actionButton.width : 0);

                    // Creating the edit action button.
                    disableButtonRect = D3Utils.rect(disableButtonXPosition - (propertyButtonPaneRectWidth / 2), viewOptions.position.y + 3,
                        viewOptions.actionButton.width, viewOptions.actionButton.height, 0, 0, propertyButtonPaneGroup)
                        .classed(viewOptions.actionButton.class, true).classed(viewOptions.actionButton.disableClass, true);
                }

                if (viewOptions.actionButton.enableDelete) {
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

                    var editButtonXPosition = viewOptions.position.x + (viewOptions.actionButton.enableEdit ? viewOptions.actionButton.width : 0);
                    editButtonXPosition += viewOptions.actionButton.enableDisabling ? viewOptions.actionButton.width : 0;

                    // Creating the delete action button.
                    deleteButtonRect = D3Utils.rect(editButtonXPosition - (propertyButtonPaneRectWidth / 2), viewOptions.position.y,
                        viewOptions.actionButton.width, viewOptions.actionButton.height, 0, 0, propertyButtonPaneGroup)
                        .classed(viewOptions.actionButton.class, true).classed(viewOptions.actionButton.deleteClass, true);
                }

                // Adding on click event for edit button.
                $(editButtonRect.node()).click(function () {

                    // Show only one property pane
                    if (_.isNil(this.getAttribute("data-showing-property-pane")) || this.getAttribute("data-showing-property-pane") == "false") {
                        this.setAttribute("data-showing-property-pane", "true");
                    } else {
                        return;
                    }

                    // Hiding property button pane.
                    $(propertyButtonPaneGroup.node()).remove();

                    var propertyPaneWrapper = $("<div/>", {
                        "class": viewOptions.propertyForm.wrapper.class /*+ " nano"*/
                    }).appendTo($(".canvas-container"));

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

                    $(closeIcon).click(function (editButton) {
                        editButton.setAttribute("data-showing-property-menu", "false");
                        $(propertyPaneWrapper).remove();
                    }.bind(this, this));

                    var propertyPaneBody = $("<div/>", {
                        "class": viewOptions.propertyForm.body.class /*+ " nano-content"*/
                    }).appendTo(propertyPaneWrapper);

                    // $(".nano").nanoScroller();

                    _.forEach(editableProperties, function (editableProperty) {
                        var propertyWrapper = $("<div/>", {
                            "class": viewOptions.propertyForm.body.property.wrapper
                        }).appendTo(propertyPaneBody);
                        switch (editableProperty.propertyType) {
                            case "text":
                                createTextBox(editableProperty, propertyWrapper);
                                break;
                            case "checkbox":
                                createCheckBox(editableProperty, propertyWrapper);
                                break;
                            case "dropdown":
                                createDropdown(editableProperty, propertyWrapper);
                                break;
                            default:
                                log.error("Unknown property type found when creating property editor.");
                                throw "Unknown property type found when creating property editor.";
                        }
                    });

                    function createTextBox(property, propertyWrapper) {
                        var propertyTitle = $("<span>" + property.key + " :<span/>").appendTo(propertyWrapper);
                        var propertyValue = $("<input type='text' value='" + property.getterMethod.call(property.model) + "'>").appendTo(propertyWrapper);
                        $(propertyValue).keyup(function () {
                            property.setterMethod.call(property.model, $(this).val());
                        });
                    }

                    function createCheckBox(property, propertyWrapper) {
                        var isChecked = property.getterMethod.call() ? "checked" : "";
                        var propertyValue = $("<input type='checkbox' " + property.key + " " + isChecked + "/>").appendTo(propertyWrapper);
                        $(propertyValue).change(function () {
                            property.setterMethod.call($(this).val());
                        });
                    }

                    function createDropdown(property, propertyWrapper) {
                        var propertyTitle = $("<span>" + property.key + "<span/>").appendTo(propertyWrapper);
                        var propertyValue = $("<select />");
                        for (var val in data) {
                            $("<option />", {value: val, text: data[val]}).appendTo(propertyValue);
                        }

                        $(propertyValue).appendTo(propertyWrapper);

                    }
                });

                // Adding on click event for delete button.
                $(deleteButtonRect.node()).click(function () {
                    log.info("initializing delete");
                    //TODO: get each element as child
                    var child = model;
                    var parent = child.parent;
                    parent.removeChild(child);
                });

            });
        };

        BallerinaView.prototype.childViewRemovedCallback = function (child) {
            log.info("[Eventing] Child element view removed. ");
            //TODO: logic to remove each element from each container
        };

        BallerinaView.prototype.getBoundingBox = function () {
            return this._boundingBox;
        };

        return BallerinaView;
    });