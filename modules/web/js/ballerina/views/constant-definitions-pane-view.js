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
define(['require', 'lodash', 'jquery', 'log', 'd3utils', 'd3', 'alerts', './point',
        'ballerina/ast/ballerina-ast-factory', './constant-definition-view', './../ast/node', 'select2'],
    function (require, _, $, log, D3Utils, d3, Alerts, Point,
              BallerinaASTFactory, ConstantDefinitionView, ASTNode, select2) {

        /**
         * Creates a new instance for a constant definition pane view.
         * @param args - Arguments for creating the constant definitions pane.
         * @param {BallerinaASTRoot} args.model - The Ballerina AST root model.
         * @param {HTMLElement} args.paneAppendElement - The element to which the pane to be appended.
         * @param {BallerinaFileEditor} args.view - The ballerina filed editor view.
         * @constructor
         */
        var ConstantDefinitionPaneView = function(args) {
            this._model = _.get(args, "model");
            this._paneAppendElement = _.get(args, "paneAppendElement");
            this._viewOfModel = _.get(args, "view");

            this._constantDefinitionsButton = undefined;
            this._constantsDefinitionsMainWrapper = undefined;
        };

        ConstantDefinitionPaneView.prototype.constructor = ConstantDefinitionPaneView;

        /**
         * Creates the pane view of the constant definitions.
         */
        ConstantDefinitionPaneView.prototype.createConstantDefinitionPane = function() {
            var self = this;

            // Creating constants button.
            this._constantDefinitionsButton = $("<div class='constants-btn' data-toggle='tooltip' title='Constants' " +
                "data-placement='bottom'></div>")
                .appendTo(this._paneAppendElement);

            $("<span class='btn-icon'>" +
                " Const. </span>")
                .appendTo(this._constantDefinitionsButton).tooltip();

            this._constantsDefinitionsMainWrapper = $("<div class='constants-pane'/>").appendTo(this._paneAppendElement);

            var constantsWrapper = $("<div class='constants-wrapper'/>").appendTo(this._constantsDefinitionsMainWrapper);

            var collapserWrapper = $("<div class='constant-pane-collapser-wrapper' data-placement='bottom' " +
                " title='Open Constant Pane' data-toggle='tooltip'/>")
                .data("collapsed", "true")
                .appendTo(constantsWrapper);
            $("<i class='fw fw-right'></i>").appendTo(collapserWrapper);

            var constantsActionWrapper = $("<div class='constants-action-wrapper'/>").appendTo(constantsWrapper);

            // Creating add constant editor button.
            var addConstantButton = $("<div class='action-icon-wrapper constant-add-icon-wrapper' title='Add Constant'" +
                "data-toggle='tooltip' data-placement='bottom'/>")
                .appendTo(constantsActionWrapper);
            $("<i class='fw fw-add'></i>").appendTo(addConstantButton);

            var constantsAddPane = $("<div class='action-content-wrapper-heading constant-add-action-wrapper'/>")
                .appendTo(constantsActionWrapper);

            var constantBTypeSelect = $("<select/>");
            var typeDropdownWrapper = $('<div class="type-drop-wrapper"></div>');
            constantBTypeSelect.appendTo(typeDropdownWrapper);
            typeDropdownWrapper.appendTo(constantsAddPane);
            var constantIdentifierText = $("<input id='text' placeholder='Identifier'/>").appendTo(constantsAddPane);
            var constantValueText = $("<input id='text' placeholder='Value'/>").appendTo(constantsAddPane);

            var constantTypes = this._viewOfModel.getDiagramRenderingContext().getEnvironment().getTypes();
            _.forEach(constantTypes, function(constantType){
                $("<option value=" + constantType + ">" + constantType + "</option>")
                    .appendTo($(constantBTypeSelect));
            });
            constantBTypeSelect.select2({
                tags: true,
                selectOnClose: true
            });
            // Add new constant upon enter key.
            $(constantIdentifierText).keypress(function (e) {
                var enteredKey = e.which || e.charCode || e.keyCode;
                // Disabling enter key
                if (enteredKey == 13) {
                    constantAddCompleteButtonPane.click();
                    event.stopPropagation();
                    return false;
                }

                var newIdentifier = $(this).val() + String.fromCharCode(enteredKey);

                // Validation the identifier against grammar.
                if (!ASTNode.isValidIdentifier(newIdentifier)) {
                    var errorString = "Invalid identifier for a parameter: " + newIdentifier;
                    log.error(errorString);
                    Alerts.error(errorString);
                    event.stopPropagation();
                    return false;
                }
            });

            // Add new constant when pressed enter on value field.
            $(constantValueText).keypress(function(e){
                var enteredKey = e.which || e.charCode || e.keyCode;
                // Disabling enter key
                if (enteredKey == 13) {
                    constantAddCompleteButtonPane.click();
                    event.stopPropagation();
                    return false;
                }
            });

            // Creating cancelling add new constant button.
            var constantAddCancelButtonPane = $("<div class='action-icon-wrapper constant-add-cancel-action-wrapper' " +
                "data-placement='bottom' title='Cancel' data-toggle='tooltip'/>")
                .appendTo(constantsAddPane);
            $("<span class='fw-stack fw-lg'><i class='fw fw-square fw-stack-2x'></i>" +
                "<i class='fw fw-cancel fw-stack-1x fw-inverse'></i></span>").appendTo(constantAddCancelButtonPane);
            // Creating add new constant button.
            var constantAddCompleteButtonPane = $("<div class='action-icon-wrapper " +
                "constant-add-complete-action-wrapper' title='Add' data-placement='bottom' data-toggle='tooltip'/>")
                .appendTo(constantsAddPane);
            $("<span class='fw-stack fw-lg'><i class='fw fw-square fw-stack-2x'></i>" +
                "<i class='fw fw-check fw-stack-1x fw-inverse'></i></span>").appendTo(constantAddCompleteButtonPane);

            // Add new constant activate button.
            $(addConstantButton).click(function () {
                $(constantsAddPane).show();
                $(this).hide();
                $(constantIdentifierText).focus();
                self._constantDefinitionsButton.css("opacity", "1");
            });

            // Cancel adding a new constant.
            $(constantAddCancelButtonPane).click(function () {
                $(constantsAddPane).hide();
                $(addConstantButton).show();
                self._constantDefinitionsButton.css("opacity", "");
            });

            var constantsDefinitionsContentWrapper = $("<div class='constants-content-wrapper'/>")
                .appendTo(constantsWrapper);

            // Rendering the variables
            this._renderConstantDefinitions(constantsDefinitionsContentWrapper, collapserWrapper);

            // When a new variable is created.
            $(constantAddCompleteButtonPane).click(function () {
                var typeOfNewConstant = constantBTypeSelect.val();
                var identifierOfNewConstant = constantIdentifierText.val();
                var valueOfNewConstant = constantValueText.val();

                try {
                    self._model.addConstantDefinition(typeOfNewConstant, identifierOfNewConstant, valueOfNewConstant);

                    // Clearing values in inputs.
                    constantIdentifierText.val("");
                    constantValueText.val("");

                    // Recreating the arguments details view.
                    self._renderConstantDefinitions(constantsDefinitionsContentWrapper, collapserWrapper);

                    // Changing the content of the collapser.
                    collapserWrapper.empty();
                    collapserWrapper.data("collapsed", "false");
                    $("<i class='fw fw-left'></i>").appendTo(collapserWrapper);
                    constantsWrapper.show();
                    self._constantsDefinitionsMainWrapper.css("width", "92%");
                } catch (error) {
                    Alerts.error(error);
                }
            });

            // The click event for hiding and showing constants.
            collapserWrapper.click(function () {
                $(this).empty();
                if ($(this).data("collapsed") === "false") {
                    $(this).data("collapsed", "true").attr('data-original-title', "Open Constant Pane").tooltip('hide');
                    $("<i class='fw fw-right'></i>").appendTo(this);
                    constantsWrapper.find('.constants-content-wrapper').hide();
                    constantsActionWrapper.hide();
                    self._constantsDefinitionsMainWrapper.css("width", "0%");
                } else {
                    $(this).data("collapsed", "false").attr('data-original-title', "Close Constant Pane").tooltip('hide');
                    $("<i class='fw fw-left'></i>").appendTo(this);
                    constantsActionWrapper.show();
                    constantsWrapper.find('.constants-content-wrapper').show();
                    self._constantsDefinitionsMainWrapper.css("width", "92%");
                }
            });

            // Stop propagating event to elements behind. This is needed for closing the wrapper when clicked outside.
            this._constantsDefinitionsMainWrapper.click(function (event) {
                event.stopPropagation();
            });
        };

        /**
         * Renders the view for the created constant definitions.
         * @param {HTMLElement} wrapper - The container of the constant definition views.
         * @param {HTMLElement} collapserWrapper - The collapsible arrow.
         * @private
         */
        ConstantDefinitionPaneView.prototype._renderConstantDefinitions = function(wrapper, collapserWrapper) {
            $(wrapper).empty();
            var self = this;

            _.forEach(this._model.getConstantDefinitions(), function(constantDefinition) {

                var constantDefinitionView = new ConstantDefinitionView({
                    parent: self._model,
                    model: constantDefinition,
                    container: wrapper,
                    toolPalette: self._viewOfModel.getToolPalette(),
                    messageManager: self._viewOfModel.getMessageManager(),
                    parentView: self._viewOfModel
                });

                self._viewOfModel.getDiagramRenderingContext().getViewModelMap()[constantDefinition.id] = constantDefinitionView;

                constantDefinitionView.render(self._viewOfModel.getDiagramRenderingContext());

                $(constantDefinitionView.getDeleteButton()).click(function () {
                    self._renderConstantDefinitions(wrapper, collapserWrapper);
                });
            });

        };

        return ConstantDefinitionPaneView;
    });