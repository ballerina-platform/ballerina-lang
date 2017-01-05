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

        //TODO : Move this to a common constant.
        var variableTypes = ['message', 'connection', 'string', 'int', 'exception', 'json', 'xml', 'map', 'string[]', 'int[]'];

        /**
         * Creates the return type pane.
         * @param {Object} args - Arguments for creating the view.
         * @param {Object} args.activatorElement - The variable button which activates to show the pane.
         * @param {ServiceDefinition} args.model - The service definition model.
         * @param {Object} args.paneAppendElement - The element to which the pane should be appended to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         */
        var createReturnTypePane = function (args) {
            var activatorElement = _.get(args, "activatorElement");
            var model = _.get(args, "model");
            var paneElement = _.get(args, "paneAppendElement");
            var viewOptions = _.get(args, "viewOptions");

            var returnTypeEditorWrapper = $("<div/>", {
                class: "main-action-wrapper return-type-main-action-wrapper"
            }).appendTo(paneElement);

            // Positioning the main wrapper
            returnTypeEditorWrapper.css("left",
                viewOptions.position.left - parseInt(returnTypeEditorWrapper.css("width"), 10));
            returnTypeEditorWrapper.css("top", viewOptions.position.top);

            // Creating header content.
            var headerWrapper = $("<div/>", {
                class: "action-content-wrapper-heading return-type-wrapper-heading"
            }).appendTo(returnTypeEditorWrapper);

            // Creating return type dropdown.
            var returnTypeDropDown = $("<select/>").appendTo(headerWrapper);

            // Adding dropdown elements.
            _.forEach(variableTypes, function (type) {
                // Adding return types.
                returnTypeDropDown.append(
                    $('<option></option>').val(type).html(type)
                );
            });

            // Wrapper for the add and check icon.
            var addIconWrapper = $("<div/>", {
                class: "action-icon-wrapper return-type-action-icon"
            }).appendTo(headerWrapper);

            var addButton = $("<span class='fw-stack fw-lg'><i class='fw fw-square fw-stack-2x'></i>" +
                "<i class='fw fw-add fw-stack-1x fw-inverse return-type-action-icon-i'></i></span>")
                .appendTo(addIconWrapper);

            // Add new return type.
            $(addButton).click(function () {
                var type = returnTypeDropDown.val();

                // Add return type to model
                model.addReturnType(type);

                // Recreating the return type details view.
                _createCurrentReturnTypeView(model, returnTypeContentWrapper, returnTypeDropDown, headerWrapper);
            });

            // Creating the content editing div.
            var returnTypeContentWrapper = $("<div/>", {
                class: "action-content-wrapper-body arguments-details-wrapper"
            }).appendTo(returnTypeEditorWrapper);

            // Creating the return types details view.
            _createCurrentReturnTypeView(model, returnTypeContentWrapper, returnTypeDropDown, headerWrapper);

            // Showing and hiding the return types pane upon arguments button/activator is clicked.
            $(activatorElement).click({returnTypeEditorWrapper: returnTypeEditorWrapper}, function (event) {
                if ($(event.currentTarget).data("showing-pane") === "true") {
                    $(event.currentTarget).removeClass("operations-argument-icon");
                    event.data.returnTypeEditorWrapper.hide();
                    $(event.currentTarget).data("showing-pane", "false");
                } else {
                    $(event.currentTarget).addClass("operations-argument-icon");
                    event.data.returnTypeEditorWrapper.show();
                    $(event.currentTarget).data("showing-pane", "true");
                }
            });
        };

        /**
         * Creates the return type detail wrapper and its events.
         * @param model - The model which contains the return types.
         * @param wrapper - The wrapper element which these details should be appended to.
         * @param returnTypeDropDown - The dropdown which has the available arguments.
         * @param headerWrapper - Wrapper which container the arguments editor.
         * @private
         */
        function _createCurrentReturnTypeView(model, wrapper, returnTypeDropDown, headerWrapper) {
            // Clearing all the element in the wrapper as we are rerendering the return types view.
            wrapper.empty();

            // Creating return types info.
            _.forEach(model.getReturnTypes(), function (argument, index) {
                var returnTypeWrapper = $("<div/>", {
                    class: "arguments-detail-wrapper"
                }).appendTo(wrapper);

                // Creating a wrapper for the argument type.
                $("<div/>", {
                    text: argument.getType(),
                    class: "arguments-detail-type-wrapper"
                }).appendTo(returnTypeWrapper);

                var deleteIcon = $("<i class='fw fw-cancel arguments-detail-close-wrapper'></i>");

                deleteIcon.appendTo(returnTypeWrapper);

                // Removes the value of the argument in the model and rebind the arguments to the arguments view.
                deleteIcon.click(function () {
                    $(returnTypeWrapper).remove();
                    model.removeReturnType(argument.getType());
                    _createCurrentReturnTypeView(model, wrapper, returnTypeDropDown, headerWrapper);
                });

                // Not add a thematic break.
                if (model.getReturnTypes().length - 1 != index) {
                    $("<hr/>").appendTo(wrapper);
                }
            });
        }

        var returnTypes = {};

        returnTypes.createReturnTypePane = createReturnTypePane;

        return returnTypes;
    });