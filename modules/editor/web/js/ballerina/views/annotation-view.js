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

        /**
         * Creates the variable pane
         * @param {Object} args - Arguments for creating the view.
         * @param {Object} args.activatorElement - The variable button which activates to show the pane.
         * @param {ServiceDefinition} args.model - The service definition model.
         * @param {Object} args.paneAppendElement - The element to which the pane should be appended to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         */
        var createAnnotationPane = function (args) {
            var activatorElement = _.get(args, "activatorElement");
            var model = _.get(args, "model");
            var paneElement = _.get(args, "paneAppendElement");
            var viewOptions = _.get(args, "viewOptions");

            var annotationEditorWrapper = $("<div/>", {
                class: "main-action-wrapper service-annotation-main-action-wrapper"
            }).appendTo(paneElement);

            // Positioning the main wrapper
            annotationEditorWrapper.css("left",
                viewOptions.position.left - parseInt(annotationEditorWrapper.css("width"), 10));
            annotationEditorWrapper.css("top", viewOptions.position.top);

            // Creating header content.
            var headerWrapper = $("<div/>", {
                class: "action-content-wrapper-heading service-annotation-wrapper-heading"
            }).appendTo(annotationEditorWrapper);

            // Creating annotations dropdown.
            var annotationTypeDropDown = $("<select/>").appendTo(headerWrapper);

            // Text input for editing the value of an annotation.
            var annotationValueInput = $("<input/>", {
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
            $(addButton).click(function (event) {
                var annotationType = annotationTypeDropDown.val();
                var annotationValue = annotationValueInput.val();

                try {
                    // Sets the annotation values in the model
                    model.addAnnotation(annotationType, annotationValue);

                    //Clear the text box and drop down value
                    annotationValueInput.val("");

                    // Recreating the annotation details view.
                    _createCurrentAnnotationView(model, annotationsContentWrapper, annotationTypeDropDown, headerWrapper);

                    // Re-add elements to dropdown.
                    _addAnnotationsToDropdown(model, annotationTypeDropDown, headerWrapper);
                } catch (e) {
                    event.preventDefault();
                    return false;
                }
            });

            // Add new annotation upon enter key.
            $(annotationValueInput).on("change paste keydown", function (e) {
                if (e.which == 13) {
                    addButton.click();
                }
            });

            // Add elements to dropdown.
            _addAnnotationsToDropdown(model, annotationTypeDropDown, headerWrapper);

            // Creating the content editing div.
            var annotationsContentWrapper = $("<div/>", {
                class: "action-content-wrapper-body service-annotation-details-wrapper"
            }).appendTo(annotationEditorWrapper);

            // Creating the annotation details view.
            _createCurrentAnnotationView(model, annotationsContentWrapper, annotationTypeDropDown, headerWrapper);

            // Showing and hiding the annotation pane upton annotation button/activator is clicked.
            $(activatorElement).click({
                annotationEditorWrapper: annotationEditorWrapper,
                annotationValueInput: annotationValueInput
            }, function (event) {
                if ($(event.currentTarget).data("showing-pane") === "true") {
                    $(event.currentTarget).removeClass("operations-annotation-icon");
                    event.data.annotationEditorWrapper.hide();
                    $(event.currentTarget).data("showing-pane", "false");
                } else {
                    $(event.currentTarget).addClass("operations-annotation-icon");
                    event.data.annotationEditorWrapper.show();
                    $(event.currentTarget).data("showing-pane", "true");
                    $(event.data.annotationValueInput).focus();
                }
            });

            $(annotationEditorWrapper).click(function (event) {
                event.stopPropagation();
            });

            // On window click.
            $(window).click({
                activatorElement: activatorElement,
                argumentsEditorWrapper: annotationEditorWrapper
            }, function (event) {
                if ($(event.data.activatorElement).data("showing-pane") === "true"){
                    $(event.data.activatorElement).click();
                }
            });
        };

        /**
         * Adds annotation with values to the dropdown.
         * @param model - The model which contains the list of annotations.
         * @param annotationTypeDropDown - The <select> element which has the available annotation.
         * @param headerWrapper - Wrapper which container the annotation editor.
         * @private
         */
        function _addAnnotationsToDropdown(model, annotationTypeDropDown, headerWrapper) {
            // Clearing existing options in the dropdown.
            annotationTypeDropDown.empty();

            // Adding dropdown elements.
            _.forEach(model.getAnnotations(), function (annotation) {
                // Adding annotations which has no value to the dropdown.
                if (_.isEmpty(annotation.value)) {
                    annotationTypeDropDown.append(
                        $('<option></option>').val(annotation.key).html(annotation.key)
                    );
                }
            });

            // Disable dropdown if options available.
            if (annotationTypeDropDown.find("option").length == 0) {
                $(headerWrapper).attr("disabled", true);
                $(headerWrapper).find("*").attr("disabled", true);
            } else {
                $(headerWrapper).attr("disabled", false);
                $(headerWrapper).find("*").attr("disabled", false);
            }
        }

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

            // Calculating the number of non-empty annotations.
            var nonEmptyAnnotations = 0;
            _.forEach(model.getAnnotations(), function (annotation) {
                if (!_.isEmpty(annotation.value)) {
                    nonEmptyAnnotations++;
                }
            });

            // Creating annotation info.
            _.forEach(model.getAnnotations(), function (annotation, index) {
                if (!_.isEmpty(annotation.value)) {

                    var annotationWrapper = $("<div/>", {
                        class: "service-annotation-detail-wrapper"
                    }).appendTo(wrapper);

                    // Creating a wrapper for the annotation type.
                    var annotationTypeWrapper = $("<div/>", {
                        text: annotation.key,
                        class: "service-annotation-detail-type-wrapper"
                    }).appendTo(annotationWrapper);

                    // Creating a wrapper for the annotation value.
                    var annotationValueWrapper = $("<div/>", {
                        text: ": " + annotation.value,
                        class: "service-annotation-detail-value-wrapper"
                    }).appendTo(annotationWrapper);

                    var deleteIcon = $("<i class='fw fw-cancel service-annotation-detail-close-wrapper'></i>");

                    deleteIcon.appendTo(annotationWrapper);

                    // Removes the value of the annotation in the model and rebind the annotations to the dropdown and
                    // to the annotation view.
                    deleteIcon.click(function () {
                        model.addAnnotation(annotation.key, "");
                        $(annotationWrapper).remove();
                        _addAnnotationsToDropdown(model, annotationTypeDropDown, headerWrapper);
                        _createCurrentAnnotationView(model, wrapper, annotationTypeDropDown, headerWrapper);
                    });

                    // Not add a thematic break.
                    if (nonEmptyAnnotations - 1 != index) {
                        $("<hr/>").appendTo(wrapper);
                    }

                    // When an annotation detail is clicked.
                    annotationWrapper.click({
                        clickedAnnotationValueWrapper: annotationValueWrapper,
                        deleteIcon: deleteIcon,
                        annotation: annotation
                    }, function (event) {
                        var clickedAnnotationValueWrapper = event.data.clickedAnnotationValueWrapper;
                        var annotation = event.data.annotation;
                        var deleteIcon = event.data.deleteIcon;

                        // Empty the content inside the annotation value and type wrapper.
                        clickedAnnotationValueWrapper.empty();

                        // Changing the background
                        $(event.currentTarget).css("background-color", "#f5f5f5");

                        // Creating the text area for the value of the annotation.
                        var annotationValueTextArea = $("<textarea/>", {
                            text: annotation.value,
                            class: "form-control"
                        }).appendTo(clickedAnnotationValueWrapper);

                        annotationValueTextArea.click(function (event) {
                            event.stopPropagation();
                        });

                        // Gets the user input and set it as the annotation value
                        annotationValueTextArea.on("change keyup input", function (e) {
                            model.addAnnotation(annotation.key, e.target.value);
                        });

                        // Adding in-line display block to override the hovering css.
                        deleteIcon.show();

                        // Resetting of other annotations wrapper which has been used for editing.
                        annotationWrapper.siblings().each(function () {

                            // Removing the textareas of other annotations and use simple text.
                            var annotationValueDiv = $(this).children().eq(1);
                            if (annotationValueDiv.find("textarea").length > 0) {
                                // Reverting the background color of other annotation editors.
                                $(this).removeAttr("style");

                                var annotationVal = ": " + annotationValueDiv.find("textarea").val();
                                annotationValueDiv.empty().text(annotationVal);

                                deleteIcon.removeAttr("style");
                            }
                        });
                    });
                }

            });
        }

        var annotationView = {};

        annotationView.createAnnotationPane = createAnnotationPane;

        return annotationView;
    });