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
import _ from 'lodash';
import $ from 'jquery';
import log from 'log';
import Alerts from 'alerts';
import BallerinaView from './ballerina-view';
import Argument from './../ast/argument';
import ResourceParameter from './../ast/resource-parameter';

/**
 * Creates the resource parameters pane.
 * @param {Object} args - Arguments for creating the view.
 * @param {Object} args.activatorElement - The variable button which activates to show the pane.
 * @param {ServiceDefinition} args.model - The service definition model.
 * @param {Object} args.paneAppendElement - The element to which the pane should be appended to.
 * @param {Object} [args.viewOptions={}] - Configuration values for the view.
 * @augments BallerinaView
 */
class ResourceParameterView extends BallerinaView {
    constructor(args) {
        super(args);
        this._parameterWrapper = undefined;
        this._deleteButton = undefined;
    }

    /**
     * @inheritDoc
     * Implements the view for a resource parameter.
     */
    render(diagramRenderContext) {
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
    }

    /**
     * Render the editing view of a resource parameter.
     */
    renderEditView(diagramRenderingContext) {
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

        // Setting selected bType of the parameter.
        $(parameterTypeDropDown).val(this.getModel().getType()).change();

        // Updating parameter type upon change,
        $(parameterTypeDropDown).change(function () {
            self.getModel().setBType($(this).val());
        });

        // Text input for the new identifier.
        var parameterIdentifierInput = $("<input/>", {
            type: "text",
            val: this.getModel().getIdentifier()
        }).keypress(function (e) {
            /* Ignore Delete and Backspace keypress in firefox and capture other keypress events.
             (Chrome and IE ignore keypress event of these keys in browser level)*/
            if (!_.isEqual(e.key, "Delete") && !_.isEqual(e.key, "Backspace")) {
                var enteredKey = e.which || e.charCode || e.keyCode;
                // Disabling enter key
                if (_.isEqual(enteredKey, 13)) {
                    e.stopPropagation();
                    return false;
                }

                var newIdentifier = $(this).val() + String.fromCharCode(enteredKey);

                try {
                    self.getModel().setIdentifier(newIdentifier);
                } catch (error) {
                    Alerts.error(error);
                    e.stopPropagation();
                    return false;
                }
            }
        }).keyup(function(){
            self.getModel().setIdentifier($(this).val());
        }).appendTo(parameterWrapper);

        //// End of parameter section

        $(this._deleteButton).appendTo(this._parameterWrapper);
    }

    /**
     * Returns an object array with support types.
     * @return {Object[]} Object array as supported data types.
     */
    _getTypeDropdownValues() {
        var dropdownData = [];
        // Adding items to the type dropdown.
        var bTypes = this.getDiagramRenderingContext().getEnvironment().getTypes();
        _.forEach(bTypes, function (bType) {
            dropdownData.push({id: bType, text: bType});
        });

        return dropdownData;
    }

    getParameterWrapper() {
        return this._parameterWrapper;
    }

    getDeleteButton() {
        return this._deleteButton;
    }
}

export default ResourceParameterView;
    
