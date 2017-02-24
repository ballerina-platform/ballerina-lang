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
define(['lodash', 'log', 'jquery', 'alerts', './return-type-view', './../ast/node','select2'],
    function (_, log, $, Alerts, ReturnTypeView, ASTNode, select2) {

        /**
         * Creates the return types pane. This is not a ballerina view. This is simply a pane which is created
         * for the sole purpose of creating and showing return types.
         * @param {Object} args - Arguments for creating the view.
         * @param {Object} args.activatorElement - The variable button which activates to show the pane.
         * @param {ASTNode} args.model - The AST node which has return types.
         * @param {Object} args.paneAppendElement - The element to which the pane should be appended to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @param {ASTNode} args.view - The view of the model.
         */
        var ReturnTypePaneView = function (args) {
            this._supportedReturnTypes = ['message', 'connection', 'string', 'int', 'exception', 'json', 'xml',
                'map', 'string[]', 'int[]'];

            this._activatorElement = _.get(args, "activatorElement");
            this._model = _.get(args, "model");
            this._paneElement = _.get(args, "paneAppendElement");
            this._viewOptions = _.get(args, "viewOptions");
            this._viewOfModel = _.get(args, "view");
            this._returnTypeEditorWrapper = undefined;
        };

        ReturnTypePaneView.prototype.constructor = ReturnTypePaneView;

        ReturnTypePaneView.prototype.createReturnTypePane = function () {
            var self = this;

            this._returnTypeEditorWrapper = $("<div/>", {
                class: "main-action-wrapper return-types-main-action-wrapper"
            }).appendTo(this._paneElement);

            // Positioning the main wrapper
            this._returnTypeEditorWrapper.css("left",
                this._viewOptions.position.x() - parseInt(this._returnTypeEditorWrapper.css("width"), 10));
            this._returnTypeEditorWrapper.css("top", this._viewOptions.position.y());

            // Creating header content.
            var headerWrapper = $("<div/>", {
                class: "action-content-wrapper-heading return-types-wrapper-heading"
            }).appendTo(this._returnTypeEditorWrapper);

            var returnTypeWrapper = $("<div/>", {
                class: "action-content-wrapper-heading return-types-heading-named-return-wrapper"
            }).appendTo(headerWrapper);

            // Checkbox to enable/disable named return types.
            var allowNamedReturnCheckBox = $("<input/>", {
                type: "checkbox",
                checked : this._model.hasNamedReturnTypes()
            }).appendTo(returnTypeWrapper);

            // The "Named Return Types" text.
            $("<span class='return-types-heading-named-return-wrapper-text'>Named Return Types</span>")
                .appendTo(returnTypeWrapper);

            // Creating the return type dropdown.
            var returnTypeDropdown = $("<select/>");
            var typeDropdownWrapper = $('<div class="type-drop-wrapper"></div>');
            returnTypeDropdown.appendTo(typeDropdownWrapper);
            typeDropdownWrapper.appendTo(returnTypeWrapper);

            // Adding dropdown elements.
            _.forEach(this._supportedReturnTypes, function (type) {
                // Adding supported return types to the type dropdown.
                returnTypeDropdown.append(
                    $('<option></option>').val(type).html(type)
                );
            });
            returnTypeDropdown.select2({
                tags: true,
                selectOnClose: true
            });
            // Return type name value text box.
            var returnTypeNameInput = $("<input/>", {
                type: "text",
                "placeholder": "m"
            }).keypress(function (e) {
                var enteredKey = e.which || e.charCode || e.keyCode;
                // Disabling enter key
                if (enteredKey == 13) {
                    addButton.click();
                    event.stopPropagation();
                    return false;
                }

                var newIdentifier = $(this).val() + String.fromCharCode(enteredKey);

                // Validation the identifier against grammar.
                if (!ASTNode.isValidIdentifier(newIdentifier)) {
                    var errorString = "Invalid identifier for a return type: " + newIdentifier;
                    log.error(errorString);
                    Alerts.error(errorString);
                    event.stopPropagation();
                    return false;
                }
            }).toggle(self._model.hasNamedReturnTypes()).appendTo(returnTypeWrapper);

            if (self._model.hasNamedReturnTypes() && self._model.hasReturnTypes()) {
                $(self._returnTypeEditorWrapper).css("width", "+=125").css("left", "-=125");
            }

            // Show/Hide return type name text box when the checkbox is clicked.
            $(allowNamedReturnCheckBox).change(function () {
                if ($(this).is(":checked")) {
                    if (!self._model.hasNamedReturnTypes() && self._model.hasReturnTypes()) {
                        Alerts.error("Return types without identifiers already exists. Remove them to " +
                            "add return types with identifiers");
                        allowNamedReturnCheckBox.prop('checked', false);
                    } else {
                        $(returnTypeNameInput).show();
                        $(self._returnTypeEditorWrapper).css("width", "+=125").css("left", "-=125");
                    }
                } else {
                    if (self._model.hasNamedReturnTypes() && self._model.hasReturnTypes()) {
                        Alerts.error("Return types with identifiers already exists. Remove them to " +
                            "add return types without identifiers");
                        allowNamedReturnCheckBox.prop('checked', true);
                    } else {
                        $(returnTypeNameInput).hide();
                        $(self._returnTypeEditorWrapper).css("width", "-=125").css("left", "+=125");
                    }
                }
            });

            // Wrapper for the add and check icon.
            var addIconWrapper = $("<div/>", {
                class: "action-icon-wrapper return-types-action-icon"
            }).appendTo(headerWrapper);

            // Creating add new return type button.
            var addButton = $("<span class='fw-stack fw-lg'><i class='fw fw-square fw-stack-2x'></i>" +
                "<i class='fw fw-add fw-stack-1x fw-inverse return-types-action-icon-i'></i></span>")
                .appendTo(addIconWrapper);

            // Creating the content editing div.
            var returnTypesContentWrapper = $("<div/>", {
                class: "action-content-wrapper-body return-types-details-wrapper"
            }).appendTo(this._returnTypeEditorWrapper);

            // Adding a new return type.
            $(addButton).click(function () {
                var returnType = $(returnTypeDropdown).val();
                var returnTypeName = $(allowNamedReturnCheckBox).is(":checked") ? returnTypeNameInput.val() : undefined;

                try {
                    self._model.addReturnType(returnType, returnTypeName);

                    // Clearing values in inputs.
                    returnTypeNameInput.val("");

                    // Recreating the arguments details view.
                    self._createCurrentReturnTypeView(returnTypesContentWrapper);
                } catch (error) {
                    Alerts.error(error);
                }
            });

            // Creating the return types details view.
            this._createCurrentReturnTypeView(returnTypesContentWrapper);

            // Showing and hiding the arguments pane upon arguments button/activator is clicked.
            $(this._activatorElement).click({
                returnTypeNameInput: returnTypeNameInput
            }, function (event) {
                if ($(event.currentTarget).data("showing-pane") === "true") {
                    $(event.currentTarget).removeClass("operations-argument-icon");
                    self._returnTypeEditorWrapper.hide();
                    $(event.currentTarget).data("showing-pane", "false");
                } else {
                    $(event.currentTarget).addClass("operations-argument-icon");
                    self._returnTypeEditorWrapper.show();
                    $(event.currentTarget).data("showing-pane", "true");
                    $(event.data.returnTypeNameInput).focus();
                }
            });

            // Stopping propagation when clicked on the editor.
            $(this._returnTypeEditorWrapper).click(function (event) {
                event.stopPropagation();
            });

            // On window click.
            $(window).click({
                activatorElement: this._activatorElement
            }, function (event) {
                self._createCurrentReturnTypeView(returnTypesContentWrapper);
                if ($(event.data.activatorElement).data("showing-pane") === "true") {
                    $(event.data.activatorElement).click();
                }
            });
        };

        /**
         * Creates the return types detail wrapper and its events.
         * @param wrapper - The wrapper element which these details should be appended to.
         * @private
         */
        ReturnTypePaneView.prototype._createCurrentReturnTypeView = function (wrapper) {
            var self = this;

            // Clearing all the element in the wrapper as we are re-rendering the return types view.
            wrapper.empty();

            // Creating return types info.
            _.forEach(self._model.getReturnTypes(), function (returnType, index) {

                var returnTypeView = new ReturnTypeView({
                    parent: self._model,
                    model: returnType,
                    container: wrapper,
                    toolPalette: self._viewOfModel.getToolPalette(),
                    messageManager: self._viewOfModel.getMessageManager(),
                    parentView: self._viewOfModel
                });

                self._viewOfModel.getDiagramRenderingContext().getViewModelMap()[returnType.id] = returnTypeView;

                returnTypeView.render(self._viewOfModel.getDiagramRenderingContext());

                // Not add a thematic break.
                if (self._model.getReturnTypes().length - 1 != index) {
                    $("<hr/>").appendTo(wrapper);
                }

                $(returnTypeView.getDeleteButton()).click(function () {
                    self._createCurrentReturnTypeView(wrapper);
                });

                $(returnTypeView.getReturnTypeWrapper()).click({
                    modelID: returnType.getID()
                }, function (event) {
                    self._createCurrentReturnTypeView(wrapper);
                    var paramViewToEdit = self._viewOfModel.getDiagramRenderingContext()
                        .getViewModelMap()[event.data.modelID];
                    paramViewToEdit.renderEditView();
                });
            });
        };

        return ReturnTypePaneView;
    });