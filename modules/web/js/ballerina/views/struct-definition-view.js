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
define(['lodash', 'log', 'd3', 'alerts', './ballerina-view', 'ballerina/ast/ballerina-ast-factory', './canvas', './../ast/node', './struct-variable-definition-view'],
    function (_, log, d3, Alerts, BallerinaView, BallerinaASTFactory,
              Canvas, ASTNode, StructVariableDefinitionView) {
        var StructDefinitionView = function (args) {
            Canvas.call(this, args);

            this._viewOptions.offsetTop = _.get(args, "viewOptionsOffsetTop", 50);
            this._viewOptions.topBottomTotalGap = _.get(args, "viewOptionsTopBottomTotalGap", 100);
            //set panel icon for the struct
            this._viewOptions.panelIcon = _.get(args.viewOptions, "cssClass.struct_icon");
            //set initial height for the struct container svg
            this._totalHeight = 30;
        };

        StructDefinitionView.prototype = Object.create(Canvas.prototype);
        StructDefinitionView.prototype.constructor = Canvas;

        StructDefinitionView.prototype.canVisitStructDefinition = function (structDefinition) {
            return true;
        };

        /**
         * Rendering the view of the Struct definition.
         * @param {Object} diagramRenderingContext - the object which is carrying data required for rendering
         */
        StructDefinitionView.prototype.render = function (diagramRenderingContext) {
            this.setDiagramRenderingContext(diagramRenderingContext);

            // Draws the outlying body of the struct definition.
            this.drawAccordionCanvas(this._viewOptions, this.getModel().getID(), this.getModel().getType().toLowerCase(), this.getModel().getStructName());

            // Setting the styles for the canvas icon.
            this.getPanelIcon().addClass(_.get(this._viewOptions, "cssClass.struct_icon", ""));

            var self = this;

            //Scroll to the added position and highlight the heading
            var currentContainer = $('#' + this.getModel().getID());
            $(_.get(this._viewOptions, "design_view.container", "")).scrollTop(currentContainer.parent().position().top);
            var hadingBox = $('#' + this.getModel().getID() + "_heading");
            var canvas_heading_new = _.get(this._viewOptions, "cssClass.canvas_heading_new", "");
            var new_drop_timeout = _.get(this._viewOptions, "design_view.new_drop_timeout", "");
            hadingBox.addClass(canvas_heading_new);
            setTimeout(function(){hadingBox.removeClass(canvas_heading_new)}, new_drop_timeout);

            $(this.getTitle()).text(this.getModel().getStructName())
                .on("change paste keyup", function () {
                    self.getModel().setStructName($(this).text());
                }).on("click", function (event) {
                    event.stopPropagation();
                }).keypress(function (e) {
                    var enteredKey = e.which || e.charCode || e.keyCode;
                    // Disabling enter key
                    if (enteredKey == 13) {
                        event.stopPropagation();
                        return false;
                    }

                    var newServiceName = $(this).val() + String.fromCharCode(enteredKey);

                    try {
                        self.getModel().setStructName(newServiceName);
                    } catch (error) {
                        Alerts.error(error);
                        event.stopPropagation();
                        return false;
                    }
                });

            var structContentWrapper = $("<div/>", {
                id: this.getModel().getID(),
                class: "struct-content-wrapper"
            }).data("model", this.getModel()).appendTo(this.getBodyWrapper());

            //// Creating operational panel

            var structOperationsWrapper = $("<div/>", {
                class: "struct-content-operations-wrapper"
            }).appendTo(structContentWrapper);

            var typeDropdownWrapper = $('<div class="type-drop-wrapper struct-view"></div>')
                .appendTo(structOperationsWrapper);

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

            // Creating the identifier text box.
            var identifierTextBox = $("<input/>", {
                type: "text",
                class: "struct-identifier-text-input",
                "placeholder": "Identifier"
            }).keypress(function (e) {
                var enteredKey = e.which || e.charCode || e.keyCode;
                // Adding new variable upon enter key.
                if (enteredKey == 13) {
                    addStructVariableButton.click();
                    event.stopPropagation();
                    return false;
                }

                var newIdentifier = $(this).val() + String.fromCharCode(enteredKey);

                // Validation the identifier against grammar.
                if (!ASTNode.isValidIdentifier(newIdentifier)) {
                    var errorString = "Invalid identifier for a variable: " + newIdentifier;
                    Alerts.error(errorString);
                    event.stopPropagation();
                    return false;
                }
            }).keydown(function(e){
                var enteredKey = e.which || e.charCode || e.keyCode;

                // If tab pressed.
                if (e.shiftKey && _.isEqual(enteredKey, 9)) {
                    typeDropdown.dropdownButton.trigger("click");
                }
            }).appendTo(structOperationsWrapper);

            // Creating cancelling add new constant button.
            var addStructVariableButton = $("<div class='add-struct-variable-button pull-left'/>")
                .appendTo(structOperationsWrapper);
            $("<span class='fw-stack fw-lg'><i class='fw fw-square fw-stack-2x'></i>" +
                "<i class='fw fw-check fw-stack-1x fw-inverse add-struct-variable-button-square'></i></span>").appendTo(addStructVariableButton);

            $(addStructVariableButton).click(function () {
                try {
                    var bType = typeDropdown.select2('data')[0].text;
                    var identifier = $(identifierTextBox).val().trim();

                    self.getModel().addVariableDefinition(bType, identifier);

                    self._renderVariableDefinitions(structVariablesWrapper);

                    $(identifierTextBox).val("");
                } catch (e) {
                    Alerts.error(e);
                }
            });

            //// End of operational panel.

            //// Creating struct content panel

            var structVariablesWrapper = $("<div/>",{
                class: "struct-content-variables-wrapper"
            }).appendTo(structContentWrapper);

            this._renderVariableDefinitions(structVariablesWrapper);

            $(structVariablesWrapper).click(function(e){
                e.preventDefault();
                return false;
            });

            //// End of struct content panel

            // On window click.
            $(window).click(function (event) {
                self._renderVariableDefinitions(structVariablesWrapper);
            });
        };

        StructDefinitionView.prototype._renderVariableDefinitions = function (wrapper) {
            $(wrapper).empty();
            var self = this;

            _.forEach(this._model.getVariableDefinitions(), function(variableDefinition) {

                var variableDefinitionView = new StructVariableDefinitionView({
                    parent: self.getModel(),
                    model: variableDefinition,
                    container: wrapper,
                    toolPalette: self.getToolPalette(),
                    messageManager: self.getMessageManager(),
                    parentView: self
                });

                self.getDiagramRenderingContext().getViewModelMap()[variableDefinition.id] = variableDefinitionView;

                variableDefinitionView.render(self.getDiagramRenderingContext());

                $(variableDefinitionView.getDeleteButton()).click(function () {
                    self._renderVariableDefinitions(wrapper);
                });

                $(variableDefinitionView.getWrapper()).click({
                    modelID: variableDefinition.getID()
                }, function (event) {
                    self._renderVariableDefinitions(wrapper);
                    var variableDefinitionView = self.getDiagramRenderingContext()
                        .getViewModelMap()[event.data.modelID];
                    variableDefinitionView.renderEditView();
                });
            });
        };

        StructDefinitionView.prototype._getTypeDropdownValues = function () {
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

        return StructDefinitionView;
    });