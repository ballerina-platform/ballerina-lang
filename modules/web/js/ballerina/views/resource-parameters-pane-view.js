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
define(['lodash', 'log', 'jquery', 'alerts', './resource-parameter-view', './../ast/node',
        './../ast/resource-parameter', 'select2'],
    function (_, log, $, Alerts, ResourceParameterView, ASTNode, ResourceParameter, select2) {

        /**
         * Creates the resource parameters pane. This is not a ballerina view. This is simply a pane which is created
         * for the sole purpose of creating and showing resource parameters.
         * @param {Object} args - Arguments for creating the view.
         * @param {Object} args.activatorElement - The variable button which activates to show the pane.
         * @param {ASTNode} args.model - The service definition model.
         * @param {Object} args.paneAppendElement - The element to which the pane should be appended to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @param {ResourceDefinitionView} args.view - The resource definition view.
         */
        var ResourceParametersPaneView = function (args) {
            this._activatorElement = _.get(args, "activatorElement");
            this._model = _.get(args, "model");
            this._paneElement = _.get(args, "paneAppendElement");
            this._viewOptions = _.get(args, "viewOptions");
            this._viewOfModel = _.get(args, "view");
            this._resourceParametersEditorWrapper = undefined;
        };

        ResourceParametersPaneView.prototype.constructor = ResourceParametersPaneView;

        ResourceParametersPaneView.prototype.createParametersPane = function () {
            var self = this;

            this._resourceParametersEditorWrapper = $("<div/>", {
                class: "main-action-wrapper resource-parameters-main-action-wrapper"
            }).appendTo(this._paneElement);

            // Positioning the main wrapper
            this._resourceParametersEditorWrapper.css("left",
                this._viewOptions.position.x() - parseInt(this._resourceParametersEditorWrapper.css("width"), 10));
            this._resourceParametersEditorWrapper.css("top", this._viewOptions.position.y());

            // Creating header content.
            var headerWrapper = $("<div/>", {
                class: "action-content-wrapper-heading resource-parameters-wrapper-heading"
            }).appendTo(this._resourceParametersEditorWrapper);

            var annotationWrapper = $("<div/>", {
                class: "action-content-wrapper-heading resource-parameters-heading-annotations-wrapper"
            }).appendTo(headerWrapper);

            // Enable/Disable annotation.
            var allowAnnotationCheckBox = $("<input/>", {
                type: "checkbox"
            }).prop("checked", true).appendTo(annotationWrapper);

            // The "annotate" text.
            $("<span class='resource-parameters-heading-annotations-wrapper-text'>Annotate</span>")
                .appendTo(annotationWrapper);

            // Creating the annotation type dropdown.
            var annotationTypeDropdown = $("<select/>").appendTo(annotationWrapper);

            // Adding supported annotations.
            this._addSupportedAnnotations(annotationTypeDropdown);

            // Annotation value text box.
            var annotationValue = $("<input/>", {
                type: "text",
                text: annotationTypeDropdown.val() === "@http:PathParam" ? "/" : "",
                "placeholder": "orderID"
            }).appendTo(annotationWrapper);

            // Setting a default value for @PathParam.
            $(annotationTypeDropdown).change(function () {
                if ($(this).val() === "@http:PathParam" && $(annotationValue).val() === "") {
                    $(annotationValue).val("/");
                } else if ($(annotationValue).val() === "/") {
                    $(annotationValue).val("");
                }
            });

            var parameterWrapper = $("<div/>", {
                class: "action-content-wrapper-heading resource-parameters-heading-parameter-wrapper"
            }).appendTo(headerWrapper);

            // Creating parameter type dropdown.
            var typeDropdownWrapper = $('<div class="type-drop-wrapper resource"/>').appendTo(parameterWrapper);

            var parameterTypeDropDown = $("<select/>").appendTo(typeDropdownWrapper);

            $(parameterTypeDropDown).select2({
                data: this._getTypeDropdownValues(),
                tags: true,
                selectOnClose: true
            });

            $(document).ready(function() {
                $(typeDropdownWrapper).empty();
                parameterTypeDropDown = $("<select/>").appendTo(typeDropdownWrapper);
                $(parameterTypeDropDown).select2({
                    tags: true,
                    selectOnClose: true,
                    data : self._getTypeDropdownValues(),
                    query: function (query) {
                        var data = {results: []};
                        if (!_.isNil(query.term)) {
                            _.forEach(self._getTypeDropdownValues(), function (item) {
                                if (item.text.toUpperCase().indexOf(query.term.toUpperCase()) >= 0) {
                                    data.results.push(item);
                                }
                            });
                            // Adding user typed string when there is no any matching item in the list
                            if(data.results.length == 0){
                                data.results.push({id: query.term, text: query.term});
                            }
                        } else {
                            data.results = self._getTypeDropdownValues();
                        }
                        query.callback(data);
                    }
                });

                $(parameterTypeDropDown).on("select2:open", function() {
                    $(".select2-search__field").attr("placeholder", "Search");
                });
            });

            // Text input for the new identifier.
            var parameterIdentifierInput = $("<input/>", {
                type: "text",
                "placeholder": "m"
            }).keypress(function (e) {
                /* Ignore Delete and Backspace keypress in firefox and capture other keypress events.
                 (Chrome and IE ignore keypress event of these keys in browser level)*/
                if (!_.isEqual(e.key, "Delete") && !_.isEqual(e.key, "Backspace")) {
                    var enteredKey = e.which || e.charCode || e.keyCode;
                    // Disabling enter key
                    if (_.isEqual(enteredKey, 13)) {
                        addButton.click();
                        e.stopPropagation();
                        return false;
                    }

                    var newIdentifier = $(this).val() + String.fromCharCode(enteredKey);

                    // Validation the identifier against grammar.
                    if (!ASTNode.isValidIdentifier(newIdentifier)) {
                        var errorString = "Invalid identifier for a parameter: " + newIdentifier;
                        log.error(errorString);
                        Alerts.error(errorString);
                        e.stopPropagation();
                        return false;
                    }
                }
            }).appendTo(parameterWrapper);

            // Show/Hide annotation type dropdown and annotation text box when the checkbox is clicked.
            $(allowAnnotationCheckBox).change(function () {
                if ($(this).is(":checked")) {
                    $(annotationTypeDropdown).show();
                    $(annotationValue).show();
                    $(parameterIdentifierInput).removeClass("resource-parameters-wrapper-heading-identifier-textbox");
                } else {
                    $(annotationTypeDropdown).hide();
                    $(annotationValue).hide();
                    $(parameterIdentifierInput).addClass("resource-parameters-wrapper-heading-identifier-textbox");
                }
            });

            // Wrapper for the add and check icon.
            var addIconWrapper = $("<div/>", {
                class: "action-icon-wrapper resource-parameters-action-icon"
            }).appendTo(headerWrapper);

            // Creating add new parameter button.
            var addButton = $("<span class='fw-stack fw-lg'><i class='fw fw-square fw-stack-2x'></i>" +
                "<i class='fw fw-add fw-stack-1x fw-inverse resource-parameters-action-icon-i'></i></span>")
                .appendTo(addIconWrapper);

            // Creating the content editing div.
            var parametersContentWrapper = $("<div/>", {
                class: "action-content-wrapper-body resource-parameters-details-wrapper"
            }).appendTo(this._resourceParametersEditorWrapper);

            // Adding a new parameter.
            $(addButton).click(function () {
                var annotationType = $(allowAnnotationCheckBox).is(":checked") ? annotationTypeDropdown.val() : undefined;
                var annotationText = $(allowAnnotationCheckBox).is(":checked") ? annotationValue.val() : undefined;
                var parameterType = $(parameterTypeDropDown).val();
                var parameterIdentifier = $(parameterIdentifierInput).val();

                try {
                    self._model.addParameter(annotationType, annotationText, parameterType, parameterIdentifier);

                    // Clearing values in inputs.
                    annotationValue.val("");
                    parameterIdentifierInput.val("");

                    // Recreating the arguments details view.
                    self._createCurrentParametersView(parametersContentWrapper);
                } catch (error) {
                    log.error(error);
                    Alerts.error(error);
                }
            });

            // Creating the parameters details view.
            this._createCurrentParametersView(parametersContentWrapper);

            // Showing and hiding the arguments pane upon arguments button/activator is clicked.
            $(this._activatorElement).click({
                argumentIdentifierInput: parameterIdentifierInput
            }, function (event) {
                if ($(event.currentTarget).data("showing-pane") === "true") {
                    $(event.currentTarget).removeClass("operations-argument-icon");
                    self._resourceParametersEditorWrapper.hide();
                    $(event.currentTarget).data("showing-pane", "false");
                } else {
                    $(event.currentTarget).addClass("operations-argument-icon");
                    self._resourceParametersEditorWrapper.show();
                    $(event.currentTarget).data("showing-pane", "true");
                    $(event.data.argumentIdentifierInput).focus();
                }
            });

            // Stopping propagation when clicked on the editor.
            $(this._resourceParametersEditorWrapper).click(function (event) {
                event.stopPropagation();
            });

            // On window click.
            $(window).click({
                activatorElement: this._activatorElement
            }, function (event) {
                self._createCurrentParametersView(parametersContentWrapper);
                if ($(event.data.activatorElement).data("showing-pane") === "true") {
                    $(event.data.activatorElement).click();
                }
            });
        };

        /**
         * Creates the parameter detail wrapper and its events.
         * @param wrapper - The wrapper element which these details should be appended to.
         * @private
         */
        ResourceParametersPaneView.prototype._createCurrentParametersView = function (wrapper) {
            var self = this;

            // Clearing all the element in the wrapper as we are re-rendering the parameters view.
            wrapper.empty();

            // Creating parameters info.
            _.forEach(self._model.getParameters(), function (parameter, index) {

                var resourceParameterView = new ResourceParameterView({
                    parent: self._model,
                    model: parameter,
                    container: wrapper,
                    toolPalette: self._viewOfModel.getToolPalette(),
                    messageManager: self._viewOfModel.getMessageManager(),
                    parentView: self._viewOfModel
                });

                self._viewOfModel.getDiagramRenderingContext().getViewModelMap()[parameter.id] = resourceParameterView;

                resourceParameterView.render(self._viewOfModel.getDiagramRenderingContext());

                // Not add a thematic break.
                if (self._model.getParameters().length - 1 != index) {
                    $("<hr/>").appendTo(wrapper);
                }

                $(resourceParameterView.getDeleteButton()).click(function () {
                    self._createCurrentParametersView(wrapper);
                });

                $(resourceParameterView.getParameterWrapper()).click({
                    modelID: parameter.getID()
                }, function (event) {
                    self._createCurrentParametersView(wrapper);
                    var paramViewToEdit = self._viewOfModel.getDiagramRenderingContext()
                        .getViewModelMap()[event.data.modelID];
                    paramViewToEdit.renderEditView();
                });
            });
        };

        /**
         * The following function will bind the supported annotations to the annotation dropdown.
         *
         * The params of a resource can either have one of @Form or @Body annotation. There cant be multiple or both of
         * them.
         * Invalid examples : (@Form byte i, @Body type b), (@Body type i, @Body type b), (@Form type i, @Form type b).
         * Valid examples : (@Form byte i), (@Body type b)
         * @param {HTMLInputElement} annotationTypeDropdown - The text box html element which has the annotation value.
         * @private
         */
        ResourceParametersPaneView.prototype._addSupportedAnnotations = function (annotationTypeDropdown) {
            // Copying the array
            var availableAnnotationTypes = ResourceParameter.getSupportedAnnotations().slice(0);

            // Check if there are parameters with @Form or @Body annotations.
            var hasFormParam = _.findIndex(this._model.getParameters(), function (resourceParam) {
                return resourceParam.getAnnotationType() === "@FormParam" ||
                    resourceParam.getAnnotationType() === "@Body";
            });

            // If there is already parameters with @Form or @Body annotation, remove them from suggestions.
            if (hasFormParam) {
                _.remove(availableAnnotationTypes, function (availableAnnotationType) {
                    return availableAnnotationType === "@FormParam" || availableAnnotationType === "@Body";
                });
            }

            // Adding the available and supported annotations.
            _.forEach(availableAnnotationTypes, function (availableAnnotationType) {
                annotationTypeDropdown.append(
                    $('<option></option>').val(availableAnnotationType).html(availableAnnotationType)
                );
            });
        };

        /**
         * Returns an object array with support types.
         * @return {Object[]} Object array as supported data types.
         */
        ResourceParametersPaneView.prototype._getTypeDropdownValues = function() {
            var dropdownData = [];
            // Adding items to the type dropdown.
            var bTypes = this._viewOfModel.getDiagramRenderingContext().getEnvironment().getTypes();
            _.forEach(bTypes, function (bType) {
                dropdownData.push({id: bType, text: bType});
            });

            return dropdownData;
        };

        /**
         * moves the position of parameter editor view
         * @param {Object} args - object which contains delta values for x and y
         * @param {Object} args.dx - delta value for x value
         * @param {Object} args.dy - delta value for y value
         */
        ResourceParametersPaneView.prototype.move = function (args) {
            var dx = _.get(args, "dx", 0);
            var dy = _.get(args, "dy", 0);

            // Left margin of the resource view
            var leftMargin = this._viewOfModel.getBoundingBox().x();
            var resourceParameterEditorLeft = parseInt(this._resourceParametersEditorWrapper.css("left"), 10) + dx;
            // This is to ensure that resource parameter editor doesn't go beyond the left margin of the resource.(to avoid clipping)
            if (leftMargin < resourceParameterEditorLeft) {
                this._resourceParametersEditorWrapper.css("left", resourceParameterEditorLeft);
            } else {
                this._resourceParametersEditorWrapper.css("left", leftMargin);
            }
            this._resourceParametersEditorWrapper.css("top", (parseInt(this._resourceParametersEditorWrapper.css("top"), 10) + dy));
        };

        return ResourceParametersPaneView;
    });
