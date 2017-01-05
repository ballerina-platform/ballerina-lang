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
define(['require', 'lodash', 'jquery'],
    function (require, _, $) {

        var variableTypes = ['message', 'connection', 'string', 'int', 'exception'];

        /**
         * Creates the variable pane
         * @param {Object} args - Arguments for creating the view.
         * @param {Object} args.activatorElement - The variable button which activates to show the pane.
         * @param {ServiceDefinition} args.model - The service definition model.
         * @param {Object} args.paneAppendElement - The element to which the pane should be appended to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         */
        var createArgumentsPane = function (args) {
            var activatorElement = _.get(args, "activatorElement");
            var model = _.get(args, "model");
            var paneElement = _.get(args, "paneAppendElement");
            var viewOptions = _.get(args, "viewOptions");

            var argumentsEditorWrapper = $("<div/>", {
                class: "main-action-wrapper service-annotation-main-action-wrapper"
            }).appendTo(paneElement);

            // Positioning the main wrapper
            argumentsEditorWrapper.css("left",
                viewOptions.position.left - parseInt(argumentsEditorWrapper.css("width"), 10));
            argumentsEditorWrapper.css("top", viewOptions.position.top);

            // Creating header content.
            var headerWrapper = $("<div/>", {
                class: "action-content-wrapper-heading service-annotation-wrapper-heading"
            }).appendTo(argumentsEditorWrapper);

            // Creating annotations dropdown.
            var argumentTypeDropDown = $("<select/>").appendTo(headerWrapper);

            // Adding dropdown elements.
            _.forEach(variableTypes, function (type) {
                // Adding annotations which has no value to the dropdown.
                argumentTypeDropDown.append(
                    $('<option></option>').val(type).html(type)
                );
            });

            // Text input for editing the value of an annotation.
            var argumentIdentifierInput = $("<input/>", {
                type: "text"
            }).appendTo(headerWrapper);

            // Wrapper for the add and check icon.
            var addIconWrapper = $("<div/>", {
                class: "action-icon-wrapper service-annotation-action-icon"
            }).appendTo(headerWrapper);

            var addButton = $("<span class='fw-stack fw-lg'><i class='fw fw-square fw-stack-2x'></i>" +
                "<i class='fw fw-add fw-stack-1x fw-inverse service-annotation-action-icon-i'></i></span>")
                .appendTo(addIconWrapper);

            // Adding a value to a new annotation.
            $(addButton).click(function () {
                var argumentType = argumentTypeDropDown.val();
                var argumentValue = argumentIdentifierInput.val();

                if (!_.isEmpty(argumentValue)) { // Sets the annotation values in the model
                    model.addFunctionArgument(argumentType, argumentValue);

                    //Clear the text box and drop down value
                    argumentIdentifierInput.val("");

                    // Recreating the annotation details view.
                    _createCurrentAnnotationView(model, argumentsContentWrapper, argumentTypeDropDown, headerWrapper);
                }
            });

            // Creating the content editing div.
            var argumentsContentWrapper = $("<div/>", {
                class: "action-content-wrapper-body service-annotation-details-wrapper"
            }).appendTo(argumentsEditorWrapper);

            // Creating the annotation details view.
            _createCurrentAnnotationView(model, argumentsContentWrapper, argumentTypeDropDown, headerWrapper);

            // Showing and hiding the annotation pane upton annotation button/activator is clicked.
            $(activatorElement).click({argumentsEditorWrapper: argumentsEditorWrapper}, function (event) {
                if ($(event.currentTarget).data("showing-pane") === "true") {
                    $(event.currentTarget).removeClass("operations-annotation-icon");
                    event.data.argumentsEditorWrapper.hide();
                    $(event.currentTarget).data("showing-pane", "false");
                } else {
                    $(event.currentTarget).addClass("operations-annotation-icon");
                    event.data.argumentsEditorWrapper.show();
                    $(event.currentTarget).data("showing-pane", "true");
                }
            });
        };

        /**
         * Creates the annotation detail wrapper and its events.
         * @param model - The annotation data.
         * @param wrapper - The wrapper element which these details should be appended to.
         * @param annotationTypeDropDown - The dropdown which has the available annotations.
         * @param headerWrapper - Wrapper which container the annotation editor.
         * @private
         */
        function _createCurrentAnnotationView(model, wrapper, annotationTypeDropDown, headerWrapper) {
            // Clearing all the element in the wrapper as we are rerendering the annotation view.
            wrapper.empty();

            // Creating annotation info.
            _.forEach(model.getFunctionArguments(), function (argument, index) {
                var functionalArgumentWrapper = $("<div/>", {
                    class: "service-annotation-detail-wrapper"
                }).appendTo(wrapper);

                // Creating a wrapper for the annotation type.
                var functionArgumentTypeWrapper = $("<div/>", {
                    text: argument.type,
                    class: "service-annotation-detail-type-wrapper"
                }).appendTo(functionalArgumentWrapper);

                // Creating a wrapper for the annotation value.
                var functionalArgumentValueWrapper = $("<div/>", {
                    text: ": " + argument.identifier,
                    class: "service-annotation-detail-value-wrapper"
                }).appendTo(functionalArgumentWrapper);

                var deleteIcon = $("<i class='fw fw-cancel service-annotation-detail-close-wrapper'></i>");

                deleteIcon.appendTo(functionalArgumentWrapper);

                // Removes the value of the annotation in the model and rebind the annotations to the dropdown and
                // to the annotation view.
                deleteIcon.click(function () {
                    $(functionalArgumentWrapper).remove();
                    model.removeFunctionArgument(argument.identifier);
                    _createCurrentAnnotationView(model, wrapper, annotationTypeDropDown, headerWrapper);
                });

                // Not add a thematic break.
                if (model.getFunctionArguments().length - 1 != index) {
                    $("<hr/>").appendTo(wrapper);
                }

                // When an annotation detail is clicked.
                functionalArgumentWrapper.click({
                    clickedAnnotationValueWrapper: functionalArgumentValueWrapper,
                    clickedAnnotationTypeWrapper: functionArgumentTypeWrapper,
                    deleteIcon: deleteIcon,
                    argument: argument
                }, function (event) {
                    var clickedAnnotationValueWrapper = event.data.clickedAnnotationValueWrapper;
                    var argument = event.data.argument;
                    var deleteIcon = event.data.deleteIcon;

                    // Empty the content inside the annotation value and type wrapper.
                    clickedAnnotationValueWrapper.empty();

                    // Changing the background
                    $(event.currentTarget).css("background-color", "#f5f5f5");

                    // Creating the text area for the value of the annotation.
                    var argumentValueTextbox = $("<input/>", {
                        val: argument.identifier,
                        class: "form-control"
                    }).appendTo(clickedAnnotationValueWrapper);

                    argumentValueTextbox.click(function (event) {
                        event.stopPropagation();
                    });

                    // Gets the user input and set it as the annotation value
                    argumentValueTextbox.on("change keyup input", function (e) {
                        argument.identifier = e.target.value;
                    });

                    // Adding in-line display block to override the hovering css.
                    deleteIcon.show();

                    // Resetting of other annotations wrapper which has been used for editing.
                    functionalArgumentWrapper.siblings().each(function () {

                        // Removing the textareas of other annotations and use simple text.
                        var argumentIdentifierDiv = $(this).children().eq(1);
                        if (argumentIdentifierDiv.find("input").length > 0) {
                            // Reverting the background color of other annotation editors.
                            $(this).removeAttr("style");

                            var annotationIdentifier = ": " + argumentIdentifierDiv.find("input").val();
                            argumentIdentifierDiv.empty().text(annotationIdentifier);

                            deleteIcon.removeAttr("style");
                        }
                    });
                });
            });
        }

        var argumentsView = {};

        argumentsView.createArgumentsPane = createArgumentsPane;

        return argumentsView;
    });