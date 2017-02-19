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
define(['lodash', 'jquery', 'log', 'alerts', './ballerina-view'],
    function (_, $, log, Alerts, BallerinaView) {

        /**
         * Arguments for creating a constant definition view.
         * @param args - See docs of {@link BallerinaView}.
         * @constructor
         */
        var StructVariableDefinitionView = function (args) {
            BallerinaView.call(this, args);
            this._parentView = _.get(args, "parentView");
            this._structVariableWrapper = undefined;
            this._typeWrapper = undefined;
            this._identifierWrapper = undefined;
            this._deleteButton = undefined;
        };

        StructVariableDefinitionView.prototype = Object.create(BallerinaView.prototype);
        StructVariableDefinitionView.prototype.constructor = StructVariableDefinitionView;

        /**
         * @inheritDoc
         * Implements the view for a constant definition.
         */
        StructVariableDefinitionView.prototype.render = function (diagramRenderContext) {
            this.setDiagramRenderingContext(diagramRenderContext);

            var self = this;

            var structVariableDefinitionWrapper = $("<div/>", {
                id: this.getModel().getID(),
                class: "struct-variable-definition-wrapper"
            }).data("model", this.getModel()).appendTo(this.getContainer());

            this._structVariableWrapper = structVariableDefinitionWrapper.get(0);

            var structVariableDefinitionTypeWrapper = $("<div/>", {
                text: this.getModel().getTypeName(),
                class: "struct-variable-definition-type pull-left"
            }).appendTo(structVariableDefinitionWrapper);

            this._typeWrapper = structVariableDefinitionTypeWrapper.get(0);

            var structVariableDefinitionIdentifierWrapper = $("<div/>", {
                text: this.getModel().getName(),
                class: "struct-variable-definition-identifier pull-left"
            }).appendTo(structVariableDefinitionWrapper);

            this._identifierWrapper = structVariableDefinitionIdentifierWrapper.get(0);

            // Creating delete button.
            var deleteButton = $("<i class='fw fw-cancel'></i>").css("visibility", "hidden")
                .appendTo(structVariableDefinitionWrapper);

            $(structVariableDefinitionWrapper).hover(function () {
                deleteButton.css("visibility", "visible");
            }, function () {
                deleteButton.css("visibility", "hidden");
            });

            this._deleteButton = deleteButton.get(0);

            // Removes the value of the argument in the model and rebind the arguments to the arguments view.
            $(deleteButton).click(function () {
                $(structVariableDefinitionWrapper).remove();
                self.getParent().removeVariableDefinition(self.getModel().getID());
            });
        };

        /**
         * @inheritDoc
         * Implements the view for a constant definition.
         */
        StructVariableDefinitionView.prototype.renderEditView = function () {
            var self = this;

            $(this._typeWrapper).empty();

            var typeEditWrapper = $("<div/>",{
                click: function(e) {e.stopPropagation();}
            }).appendTo(this._typeWrapper);

            //Initialize the select2 control
            var typeDropdownWrapper = $('<div class="type-drop-wrapper struct-edit"></div>')
                .appendTo(typeEditWrapper);

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
                    } else {
                        data.results = self._getTypeDropdownValues();
                    }
                    query.callback(data);
                }
            });

            $(typeDropdown).on("select2:open", function() {
                $(".select2-search__field").attr("placeholder", "Search");
            });

            $(typeDropdown).val(self.getModel().getTypeName()).change();

            $(typeDropdown).on("select2:select", function() {
                self.getModel().setTypeName(typeDropdown.select2('data')[0].text);
            });

            $(this._identifierWrapper).empty();

            var identifierEditWrapper = $("<div/>",{
                click: function(e) {e.stopPropagation();}
            }).appendTo(this._identifierWrapper);

            // Creating the identifier text box.
            var identifierTextBox = $("<input/>", {
                type: "text",
                class: "struct-variable-identifier-text-input",
                val: this.getModel().getName()
            }).keypress(function (e) {
                var enteredKey = e.which || e.charCode || e.keyCode;
                // Disabling enter key
                if (enteredKey == 13) {
                    event.stopPropagation();
                    return false;
                }

                var newIdentifier = $(this).val() + String.fromCharCode(enteredKey);

                try {
                    self.getModel().setName(newIdentifier);
                } catch (error) {
                    Alerts.error(error);
                    event.stopPropagation();
                    return false;
                }
            }).keydown(function(e){
                var enteredKey = e.which || e.charCode || e.keyCode;

                // If tab pressed.
                if (e.shiftKey && _.isEqual(enteredKey, 9)) {
                    typeDropdown.dropdownButton.trigger("click");
                }
            }).keyup(function(){
                self.getModel().setName($(this).val());
            }).appendTo($(identifierEditWrapper));

        };

        StructVariableDefinitionView.prototype.getDeleteButton = function () {
            return this._deleteButton;
        };

        StructVariableDefinitionView.prototype.getWrapper = function() {
            return this._structVariableWrapper;
        };

        StructVariableDefinitionView.prototype._getTypeDropdownValues = function () {
            var dropdownData = [];
            // Adding items to the type dropdown.
            var bTypes = this.getDiagramRenderingContext().getEnvironment().getTypes();
            _.forEach(bTypes, function (bType) {
                dropdownData.push({id: bType, text: bType});
            });

            var structTypes = this.getDiagramRenderingContext().getPackagedScopedEnvironment().getCurrentPackage().getStructDefinitions();
            _.forEach(structTypes, function (sType) {
                dropdownData.push({id: sType.getStructName(), text: sType.getStructName()});
            });

            return dropdownData;
        };

        return StructVariableDefinitionView;
    });