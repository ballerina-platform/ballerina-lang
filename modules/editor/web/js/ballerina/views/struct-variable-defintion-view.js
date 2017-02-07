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
define(['lodash', 'jquery', 'log', 'alerts', './ballerina-view', './../ast/variable-declaration', '../utils/dropdown'],
    function (_, $, log, Alerts, BallerinaView, VariableDeclaration, Dropdown) {

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
                text: this.getModel().getType(),
                class: "struct-variable-definition-type pull-left"
            }).appendTo(structVariableDefinitionWrapper);

            this._typeWrapper = structVariableDefinitionTypeWrapper.get(0);

            var structVariableDefinitionIdentifierWrapper = $("<div/>", {
                text: this.getModel().getIdentifier(),
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
                self.getParent().removeVariableDeclaration(self.getModel().getID());
            });
        };

        /**
         * @inheritDoc
         * Implements the view for a constant definition.
         */
        StructVariableDefinitionView.prototype.renderEditView = function () {
            var self = this;

            $(this._typeWrapper).empty();
            var typeDropdown = new Dropdown({
                class: {mainWrapper: "struct-variable-type-dropdown-wrapper"},
                onSelectCallBackFunction: function(key, value) {
                    self.getModel().setType(key)
                },
                onDropdownOpen: function() {
                    self._parentView.getBodyWrapper().css("height", $(self._parentView.getBodyWrapper()).height());
                    self._parentView.getBodyWrapper().css("overflow-x", "visible");
                    $(self._parentView.getBodyWrapper()).closest(".canvas-container").css("overflow", "visible");

                    this.removeAllItems();

                    // Adding items to the type dropdown.
                    var bTypes = self.getDiagramRenderingContext().getEnvironment().getTypes();
                    _.forEach(bTypes, function (bType) {
                        typeDropdown.addItem({key: bType, value: bType});
                    });

                    var structTypes = self.getDiagramRenderingContext().getPackagedScopedEnvironment().getCurrentPackage().getStructDefinitions();
                    _.forEach(structTypes, function (sType) {
                        typeDropdown.addItem({key: sType.getStructName(), value: sType.getStructName()});
                    });
                },
                onDropdownClosed: function() {
                    self._parentView.getBodyWrapper().css("height", "");
                    self._parentView.getBodyWrapper().css("overflow-x", "");
                    $(self._parentView.getBodyWrapper()).closest(".canvas-container").css("overflow", "");
                }
            });
            typeDropdown.getElement().appendTo($(this._typeWrapper));

            typeDropdown.setSelectedValue(this.getModel().getType());

            $(this._identifierWrapper).empty();

            // Creating the identifier text box.
            var identifierTextBox = $("<input/>", {
                type: "text",
                class: "struct-variable-identifier-text-input",
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
            }).keydown(function(e){
                var enteredKey = e.which || e.charCode || e.keyCode;

                // If tab pressed.
                if (e.shiftKey && _.isEqual(enteredKey, 9)) {
                    typeDropdown.dropdownButton.trigger("click");
                }
            }).keyup(function(){
                self.getModel().setIdentifier($(this).val());
            }).appendTo($(this._identifierWrapper));

        };

        StructVariableDefinitionView.prototype.getDeleteButton = function () {
            return this._deleteButton;
        };

        StructVariableDefinitionView.prototype.getWrapper = function() {
            return this._structVariableWrapper;
        };

        return StructVariableDefinitionView;
    });