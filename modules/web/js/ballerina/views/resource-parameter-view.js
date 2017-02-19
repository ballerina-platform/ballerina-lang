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
define(['lodash', 'jquery', 'log', 'alerts', './ballerina-view', './../ast/argument', './../ast/resource-parameter'],
    function (_, $, log, Alerts, BallerinaView, Argument, ResourceParameter) {

        /**
         * Creates the resource parameters pane.
         * @param {Object} args - Arguments for creating the view.
         * @param {Object} args.activatorElement - The variable button which activates to show the pane.
         * @param {ServiceDefinition} args.model - The service definition model.
         * @param {Object} args.paneAppendElement - The element to which the pane should be appended to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @augments BallerinaView
         */
        var ResourceParameterView = function (args) {
            BallerinaView.call(this, args);
            this._parameterWrapper = undefined;
            this._deleteButton = undefined;
        };

        ResourceParameterView.prototype = Object.create(BallerinaView.prototype);
        ResourceParameterView.prototype.constructor = ResourceParameterView;

        /**
         * @inheritDoc
         * Implements the view for a resource parameter.
         */
        ResourceParameterView.prototype.render = function (diagramRenderContext) {
            this.setDiagramRenderingContext(diagramRenderContext);

            var self = this;

            // Creating a wrapper for the parameter.
            var parameterWrapper = $("<div/>", {
                id: this.getModel().getID(),
                class: "resource-parameters-detail-wrapper",
                text: this.getModel().getParameterAsString()
            }).data("model", this.getModel()).appendTo(this.getContainer());

            this._parameterWrapper = parameterWrapper.get(0);

            var deleteIcon = $("<i class='fw fw-cancel resource-parameters-detail-close-wrapper'></i>");

            this._deleteButton = deleteIcon.get(0);

            $(deleteIcon).appendTo(parameterWrapper);

            // Removes the value of the argument in the model and rebind the arguments to the arguments view.
            $(deleteIcon).click(function () {
                $(parameterWrapper).remove();
                self.getParent().removeParameter(self.getModel().getID());
            });
        };

        /**
         * Render the editing view of a resource parameter.
         */
        ResourceParameterView.prototype.renderEditView = function (diagramRenderingContext) {
            var self = this;

            $(this._parameterWrapper).empty();

            //// Start of annotation section

            var annotationWrapper = $("<div/>", {
                class: "action-content-wrapper-heading resource-parameters-heading-annotations-wrapper",
                click: function(e) {e.stopPropagation();}
            }).appendTo(this._parameterWrapper);

            // Enable/Disable annotation.
            var allowAnnotationCheckBox = $("<input/>", {
                type: "checkbox"
            }).appendTo(annotationWrapper);

            // The "annotate" text.
            $("<span class='resource-parameters-edit-annotations-wrapper-text'>Annotate</span>")
                .appendTo(annotationWrapper);

            // Creating the annotation type dropdown.
            var annotationTypeDropdown = $("<select/>").appendTo(annotationWrapper);

            // Adding supported annotations.
            _.forEach(ResourceParameter.getSupportedAnnotations(), function (annotation) {
                annotationTypeDropdown.append(
                    $('<option></option>').val(annotation).html(annotation)
                );
            });

            // Setting selected value
            $(annotationTypeDropdown).val(this.getModel().getAnnotationType());

            // Annotation value text box.
            var annotationValue = $("<input/>", {
                type: "text",
                val: this.getModel().getAnnotationText()
            }).keypress(function (e) {
                // Updating annotation text of the model on typing.
                var enteredKey = e.which || e.charCode || e.keyCode;
                var newIdentifier = $(this).val() + String.fromCharCode(enteredKey);
                if ($(allowAnnotationCheckBox).is(":checked")) {
                    self.getModel().setAnnotationText(newIdentifier);
                }
            }).keyup(function(){
                self.getModel().setAnnotationText($(this).val());
            }).appendTo(annotationWrapper);

            // Setting a default value for @PathParam and updating model when changed.
            $(annotationTypeDropdown).change(function () {
                if ($(this).val() === "@http:PathParam" && $(annotationValue).val() === "") {
                    $(annotationValue).val("/");
                } else if ($(annotationValue).val() === "/") {
                    $(annotationValue).val("");
                }

                if ($(allowAnnotationCheckBox).is(":checked")) {
                    self.getModel().setAnnotationType($(this).val());
                }
            });

            // Show/Hide annotation type dropdown and annotation text box when the checkbox is clicked.
            $(allowAnnotationCheckBox).change(function () {
                if ($(this).is(":checked")) {
                    $(annotationTypeDropdown).show();
                    $(annotationValue).show();

                    self.getModel().setAnnotationType($(annotationTypeDropdown).val());
                    self.getModel().setAnnotationText($(annotationValue).val());
                } else {
                    $(annotationTypeDropdown).hide();
                    $(annotationValue).hide();

                    self.getModel().setAnnotationType(undefined);
                    self.getModel().setAnnotationText(undefined);
                }
            });

            // Updating UI if annotations are already there for this param.
            if (_.isUndefined(this.getModel().getAnnotationType())) {
                $(annotationTypeDropdown).hide();
                $(annotationValue).hide();
            } else {
                $(allowAnnotationCheckBox).prop("checked", true);

            }

            //// End of annotation section

            //// Start of parameter section

            var parameterWrapper = $("<div/>", {
                class: "action-content-wrapper-heading resource-parameters-heading-parameter-wrapper",
                click: function(e) {e.stopPropagation();}
            }).appendTo(this._parameterWrapper);

            // Creating parameter type dropdown.
            var parameterTypeDropDown = $("<select/>").appendTo(parameterWrapper);

            this._supportedParameterTypes = this.getDiagramRenderingContext().getEnvironment().getTypes();
            // Adding dropdown elements.
            _.forEach(this._supportedParameterTypes, function (type) {
                // Adding supported parameter types to the type dropdown.
                parameterTypeDropDown.append(
                    $('<option></option>').val(type).html(type)
                );
            });

            // Setting selected bType of the parameter.
            $(parameterTypeDropDown).val(this.getModel().getType());

            // Updating parameter type upon change,
            $(parameterTypeDropDown).change(function () {
                self.getModel().setType($(this).val());
            });

            // Text input for the new identifier.
            var parameterIdentifierInput = $("<input/>", {
                type: "text",
                val: this.getModel().getIdentifier()
            }).keypress(function (e) {
                var enteredKey = e.which || e.charCode || e.keyCode;
                // Disabling enter key
                if (enteredKey == 13) {
                    event.stopPropagation();
                    return false;
                }

                var newIdentifier = $(this).val() + String.fromCharCode(enteredKey);

                try {
                    self.getModel().setIdentifier(newIdentifier);
                } catch (error) {
                    Alerts.error(error);
                    event.stopPropagation();
                    return false;
                }
            }).keyup(function(){
                self.getModel().setIdentifier($(this).val());
            }).appendTo(parameterWrapper);

            //// End of parameter section

            $(this._deleteButton).appendTo(this._parameterWrapper);
        };

        ResourceParameterView.prototype.getParameterWrapper = function () {
            return this._parameterWrapper;
        };

        ResourceParameterView.prototype.getDeleteButton = function () {
            return this._deleteButton;
        };

        return ResourceParameterView;
    });