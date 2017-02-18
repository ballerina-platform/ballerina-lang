/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
define(['require', 'lodash', 'jquery', 'ballerina/ast/ballerina-ast-factory'],
    function (require, _, $, BallerinaASTFactory) {

        /**
         * Creates the arguments pane.
         * @param {Object} args - Arguments for creating the view.
         * @param {Object} args.activatorElement - The variable button which activates to show the pane.
         * @param {ServiceDefinition} args.model - The service definition model.
         * @param {Object} args.paneAppendElement - The element to which the pane should be appended to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @param {boolean} [args.disableEditing] - Disable editing the pane.
         */
        var createArgumentsPane = function (args, diagramRenderingContext) {
            var activatorElement = _.get(args, "activatorElement");
            var model = _.get(args, "model");
            var paneElement = _.get(args, "paneAppendElement");
            var viewOptions = _.get(args, "viewOptions");
            var disableEditing = _.get(args, "disableEditing");

            var argumentsEditorWrapper = $("<div/>", {
                class: "main-action-wrapper arguments-main-action-wrapper"
            }).appendTo(paneElement);

            // Positioning the main wrapper
            argumentsEditorWrapper.css("left",
                viewOptions.position.left - parseInt(argumentsEditorWrapper.css("width"), 10));
            argumentsEditorWrapper.css("top", viewOptions.position.top);

            // Creating header content.
            var headerWrapper = $("<div/>", {
                class: "action-content-wrapper-heading arguments-wrapper-heading"
            }).appendTo(argumentsEditorWrapper);

            // Hiding the editing wrapper if editing disabled.
            if (disableEditing) {
                headerWrapper.hide();
            }

            // Creating arguments dropdown.
            var argumentTypeDropDown = $("<select/>").appendTo(headerWrapper);
            var typeDropdownWrapper = $('<div class="type-drop-wrapper"></div>');
            argumentTypeDropDown.appendTo(typeDropdownWrapper);
            typeDropdownWrapper.appendTo(headerWrapper);

            var variableTypes = diagramRenderingContext.getEnvironment().getTypes();
            // Adding dropdown elements.
            _.forEach(variableTypes, function (type) {
                // Adding arguments which has no value to the dropdown.
                argumentTypeDropDown.append(
                    $('<option></option>').val(type).html(type)
                );
            });

            argumentTypeDropDown.select2({
                tags: true,
                selectOnClose: true

            });

            // Text input for editing the identifier of an arguments.
            var argumentIdentifierInput = $("<input/>", {
                type: "text"
            }).appendTo(headerWrapper);

            // Wrapper for the add and check icon.
            var addIconWrapper = $("<div/>", {
                class: "action-icon-wrapper arguments-action-icon"
            }).appendTo(headerWrapper);

            var addButton = $("<span class='fw-stack fw-lg'><i class='fw fw-square fw-stack-2x'></i>" +
                "<i class='fw fw-add fw-stack-1x fw-inverse arguments-action-icon-i'></i></span>")
                .appendTo(addIconWrapper);

            // Adding a value to a new arguments.
            $(addButton).click(function () {
                var argumentType = argumentTypeDropDown.val();
                var argumentValue = argumentIdentifierInput.val();

                if (!_.isEmpty(argumentValue)) {
                    // Sets the argument in the model
                    model.addArgument(argumentType, argumentValue);

                    //Clear the text box and drop down value
                    argumentIdentifierInput.val("");

                    // Recreating the arguments details view.
                    _createCurrentArgumentsView(model, argumentsContentWrapper, argumentTypeDropDown, headerWrapper,
                        disableEditing);
                }
            });

            // Add new argument upon enter key.
            $(argumentIdentifierInput).on("change paste keydown", function (e) {
                if (e.which == 13) {
                    addButton.click();
                }
            });

            // Creating the content editing div.
            var argumentsContentWrapper = $("<div/>", {
                class: "action-content-wrapper-body arguments-details-wrapper"
            }).appendTo(argumentsEditorWrapper);

            // Creating the arguments details view.
            _createCurrentArgumentsView(model, argumentsContentWrapper, argumentTypeDropDown, headerWrapper,
                disableEditing);

            // Showing and hiding the arguments pane upon arguments button/activator is clicked.
            $(activatorElement).click({
                argumentsEditorWrapper: argumentsEditorWrapper,
                argumentIdentifierInput: argumentIdentifierInput
            }, function (event) {
                if ($(event.currentTarget).data("showing-pane") === "true") {
                    $(event.currentTarget).removeClass("operations-argument-icon");
                    event.data.argumentsEditorWrapper.hide();
                    $(event.currentTarget).data("showing-pane", "false");
                } else {
                    $(event.currentTarget).addClass("operations-argument-icon");
                    event.data.argumentsEditorWrapper.show();
                    $(event.currentTarget).data("showing-pane", "true");
                    $(event.data.argumentIdentifierInput).focus();
                }
            });

            $(argumentsEditorWrapper).click(function (event) {
                event.stopPropagation();
            });

            // On window click.
            $(window).click({
                activatorElement: activatorElement,
                argumentsEditorWrapper: argumentsEditorWrapper
            }, function (event) {
                if ($(event.data.activatorElement).data("showing-pane") === "true"){
                    $(event.data.activatorElement).click();
                }
            });
        };

        /**
         * Creates the arguments detail wrapper and its events.
         * @param model - The arguments data.
         * @param wrapper - The wrapper element which these details should be appended to.
         * @param argumentsTypeDropDown - The dropdown which has the available arguments.
         * @param headerWrapper - Wrapper which container the arguments editor.
         * @param {boolean} disableEditing - Disable editing.
         * @private
         */
        function _createCurrentArgumentsView(model, wrapper, argumentsTypeDropDown, headerWrapper, disableEditing) {
            // Clearing all the element in the wrapper as we are rerendering the arguments view.
            wrapper.empty();

            // Creating arguments info.
            _.forEach(model.getArguments(), function (argument, index) {
                var argumentWrapper = $("<div/>", {
                    class: "arguments-detail-wrapper"
                }).appendTo(wrapper);

                // Creating a wrapper for the argument type.
                $("<div/>", {
                    text: argument.type,
                    class: "arguments-detail-type-wrapper"
                }).appendTo(argumentWrapper);

                // Creating a wrapper for the argument value.
                var argumentIdentifierWrapper = $("<div/>", {
                    text: ": " + argument.identifier,
                    class: "arguments-detail-identifier-wrapper"
                }).appendTo(argumentWrapper);

                var deleteIcon = $("<i class='fw fw-cancel arguments-detail-close-wrapper'></i>");

                deleteIcon.appendTo(argumentWrapper);

                // Hiding the delete icon if editing is disabled.
                if (disableEditing) {
                    deleteIcon.hide();
                }

                // Removes the value of the argument in the model and rebind the arguments to the arguments view.
                deleteIcon.click(function () {
                    $(argumentWrapper).remove();
                    model.removeArgument(argument.identifier);
                    _createCurrentArgumentsView(model, wrapper, argumentsTypeDropDown, headerWrapper, disableEditing);
                });

                // Not add a thematic break.
                if (model.getArguments().length - 1 != index) {
                    $("<hr/>").appendTo(wrapper);
                }

                if (!disableEditing) {
                    // When an arguments detail is clicked.
                    argumentWrapper.click({
                        clickedArgumentValueWrapper: argumentIdentifierWrapper,
                        deleteIcon: deleteIcon,
                        argument: argument
                    }, function (event) {
                        var clickedArgumentValueWrapper = event.data.clickedArgumentValueWrapper;
                        var argument = event.data.argument;
                        var deleteIcon = event.data.deleteIcon;

                        // Empty the content inside the arguments value and type wrapper.
                        clickedArgumentValueWrapper.empty();

                        // Changing the background
                        $(event.currentTarget).css("background-color", "#f5f5f5");

                        // Creating the text area for the identifier of the argument.
                        var argumentValueTextbox = $("<input/>", {
                            val: argument.identifier
                        }).appendTo(clickedArgumentValueWrapper);

                        argumentValueTextbox.click(function (event) {
                            event.stopPropagation();
                        });

                        // Gets the user input and set it as the argument identifier.
                        argumentValueTextbox.on("change keyup input", function (e) {
                            argument.setIdentifier(e.target.value);
                        });

                        // Adding in-line display block to override the hovering css.
                        deleteIcon.show();

                        // Resetting of other arguments wrapper which has been used for editing.
                        argumentWrapper.siblings().each(function () {

                            // Removing the text box of other arguments and use simple text.
                            var argumentIdentifierDiv = $(this).children().eq(1);
                            if (argumentIdentifierDiv.find("input").length > 0) {
                                // Reverting the background color of other argument editors.
                                $(this).removeAttr("style");

                                var argumentIdentifier = ": " + argumentIdentifierDiv.find("input").val();
                                argumentIdentifierDiv.empty().text(argumentIdentifier);

                                deleteIcon.removeAttr("style");
                            }
                        });
                    });
                }
            });
        }

        var argumentsView = {};

        argumentsView.createArgumentsPane = createArgumentsPane;

        return argumentsView;
    });