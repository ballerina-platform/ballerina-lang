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
import BallertinaView from './ballerina-view';

class AnnotationAttributeDefinitionView extends BallertinaView {
    constructor(args) {
        super(args);
        this._parentView = _.get(args, "parentView");
        this._attributeVariableWrapper = undefined;
        this._typeWrapper = undefined;
        this._identifierWrapper = undefined;
        this._deleteButton = undefined;
    }

    render(diagramRenderContext) {
        this.setDiagramRenderingContext(diagramRenderContext);

        var self = this;

        var annotationAttributeDefinitionWrapper = $("<div/>", {
            id: this.getModel().getID(),
            class: "struct-variable-definition-wrapper"
        }).data("model", this.getModel()).appendTo(this.getContainer());

        this._annotationAttributeDefinitionTypeWrapper = annotationAttributeDefinitionWrapper.get(0);

        var annotationAttributeDefinitionTypeWrapper = $("<div/>", {
            text: this.getModel().getAttributeType(),
            class: "struct-variable-definition-type pull-left"
        }).appendTo(annotationAttributeDefinitionWrapper);

        this._typeWrapper = annotationAttributeDefinitionTypeWrapper.get(0);

        var annotationAttributeDefinitionIdentifierWrapper = $("<div/>", {
            text: this.getModel().getAttributeName(),
            class: "struct-variable-definition-identifier pull-left"
        }).appendTo(annotationAttributeDefinitionWrapper);

        this._attributeNameWrapper = annotationAttributeDefinitionIdentifierWrapper.get(0);

        var deleteButton = $("<i class='fw fw-cancel'></i>").css("visibility", "hidden")
            .appendTo(annotationAttributeDefinitionWrapper);

        $(annotationAttributeDefinitionWrapper).hover(function () {
            deleteButton.css("visibility", "visible");
        }, function () {
            deleteButton.css("visibility", "hidden");
        });

        this._deleteButton = deleteButton.get(0);

        $(deleteButton).click(function(){
            $(annotationAttributeDefinitionWrapper).remove();
            self.getParent().removeAnnotationAttributeDefinition(self.getModel().getID());
        });
    }

    renderEditView() {
        var self = this;

        $(this._typeWrapper).empty();

        var typeEditWrapper = $("<div/>",{
            click: function(e) {e.stopPropagation();}
        }).appendTo(this._typeWrapper);

        //Initialize the select2 control
        var typeDropdownWrapper = $('<div class="type-drop-wrapper struct-edit"></div>')
            .appendTo(typeEditWrapper);

        $(document).ready(function(){
            var typeDropdown = $("<select/>").appendTo(typeDropdownWrapper);

            $(typeDropdown).select2({
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

            $(typeDropdown).on("select2:open", function() {
                $(".select2-search__field").attr("placeholder", "Search");
            });

            $(typeDropdown).val(self.getModel().getAttributeType()).change();

            $(typeDropdown).on("select2:select", function() {
                self.getModel().setAttributeType(typeDropdown.select2('data')[0].text);
            });
        });

        $(this._identifierWrapper).empty();

        var identifierEditWrapper = $("<div/>",{
            click: function(e) {e.stopPropagation();}
        }).appendTo(this._identifierWrapper);

        // Creating the identifier text box.
        var identifierTextBox = $("<input/>", {
            type: "text",
            class: "struct-variable-identifier-text-input",
            val: this.getModel().getAttributeName()
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
                    self.getModel().setAttributeName(newIdentifier);
                } catch (error) {
                    Alerts.error(error);
                    e.stopPropagation();
                    return false;
                }
            }
        }).keydown(function(e){
            var enteredKey = e.which || e.charCode || e.keyCode;

            // If tab pressed.
            if (e.shiftKey && _.isEqual(enteredKey, 9)) {
                typeDropdown.dropdownButton.trigger("click");
            }
        }).keyup(function(){
            self.getModel().setAttributeName($(this).val());
        }).appendTo($(identifierEditWrapper));

    }


    getDeleteButton() {
        return this._deleteButton;
    }

    getWrapper() {
        return this._attributeVariableWrapper;
    }

    _getTypeDropdownValues() {
        var dropdownData = [];
        // Adding items to the type dropdown.
        var bTypes = this.getDiagramRenderingContext().getEnvironment().getTypes();
        _.forEach(bTypes, function (bType) {
            dropdownData.push({id: bType, text: bType});
        });

        var structTypes = this.getDiagramRenderingContext().getPackagedScopedEnvironment().getCurrentPackage().getAnnotationDefinitions();
        _.forEach(structTypes, function (sType) {
            dropdownData.push({id: sType.getAnnotationName(), text: sType.getAnnotationName()});
        });

        return dropdownData;
    }
}

export default AnnotationAttributeDefinitionView;