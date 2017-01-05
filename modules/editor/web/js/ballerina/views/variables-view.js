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
define(['require', 'lodash', 'jquery', 'log', 'd3utils', 'd3', './point', 'ballerina/ast/ballerina-ast-factory',
        './../ast/variable-declaration', './../ast/connector-declaration'],
    function (require, _, $, log, D3Utils, d3, Point, BallerinaASTFactory,
              VariableDeclaration, ConnectorDeclaration) {

        // TODO move variable types into constant class
        var variableTypes = ['message', 'connection', 'string', 'int', 'exception', 'json', 'xml', 'string[]', 'int[]'];

        /**
         * Creating the variable variable button.
         * @param serviceContentSvg -  The <svg> element which contains the content of the service.
         * @param x - Starting X position.
         * @param y - Starting Y position.
         * @return An svg <circle> of the variable button.
         * @private
         */
        var createVariableButton = function (serviceContentSvg, x, y) {
            // Creating variable button.
            var variableButton = $("<div class='variable-btn'></div>")
                .appendTo(serviceContentSvg.ownerSVGElement.parentElement);

            // Positioning the variable button.
            variableButton.css("left", parseInt(x) + "px");
            variableButton.css("top", parseInt(y) + "px");

            $("<i class='fw fw-variable fw-2x'></i>").appendTo(variableButton);

            return variableButton;
        };

        /**
         * Creates the variable pane
         * @param {Object} args - Arguments for creating the view.
         * @param {Object} args.activatorElement - The variable button which activates to show the pane.
         * @param {ServiceDefinition} args.model - The service definition model.
         * @param {Object} args.paneAppendElement - The element to which the pane should be appended to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         */
        var createVariablePane = function (args) {
            var activatorElement = _.get(args, "activatorElement");
            var model = _.get(args, "model");
            var paneElement = _.get(args, "paneAppendElement");
            var viewOptions = _.get(args, "viewOptions");

            if (_.isNil(activatorElement)) {
                log.error("Unable to render property pane as the html element is undefined." + activatorElement);
                throw "Unable to render property pane as the html element is undefined." + activatorElement;
            }

            var variablePaneWrapper = $("<div class='service-variable-pane'/>").appendTo($(paneElement));
            // Positioning the variable pane from the left border of the container(service, resource, etc).
            variablePaneWrapper.css("left", viewOptions.position.x + "px");
            // Positioning the variable pane from the top border of the container(service, resource, etc).
            variablePaneWrapper.css("top", viewOptions.position.y + "px");
            // Setting max-width of the variable wrapper.
            variablePaneWrapper.css("max-width", viewOptions.width + "px");

            var variablesContentWrapper = $("<div class='variables-content-wrapper'/>").appendTo(variablePaneWrapper);

            var collapserWrapper = $("<div class='variable-pane-collapser-wrapper'/>")
                .data("collapsed", "false")
                .appendTo(variablePaneWrapper);
            $("<i class='fw fw-left'></i>").appendTo(collapserWrapper);

            var variablesActionWrapper = $("<div class='variables-action-wrapper'/>").appendTo(variablesContentWrapper);

            // Creating add variable editor button.
            var addVariableButton = $("<div class='action-icon-wrapper variable-add-icon-wrapper'/>")
                .appendTo(variablesActionWrapper);
            $("<span class='fw-stack fw-lg'><i class='fw fw-square fw-stack-2x'></i>" +
                "<i class='fw fw-add fw-stack-1x fw-inverse'></i></span>").appendTo(addVariableButton);

            var variableAddPane = $("<div class='action-content-wrapper-heading variable-add-action-wrapper'/>")
                .appendTo(variablesActionWrapper);

            var variableSelect = $("<select/>").appendTo(variableAddPane);
            var variableText = $("<input id='text' placeholder='Variable Name'/>").appendTo(variableAddPane);
            for (var typeCount = 0; typeCount < variableTypes.length; typeCount++) {
                $("<option value=" + variableTypes[typeCount] + ">" + variableTypes[typeCount] + "</option>")
                    .appendTo($(variableSelect));
            }

            // Creating cancelling add new variable button.
            var variableAddCancelButtonPane = $("<div class='action-icon-wrapper variable-add-cancel-action-wrapper'/>")
                .appendTo(variableAddPane);
            $("<span class='fw-stack fw-lg'><i class='fw fw-square fw-stack-2x'></i>" +
                "<i class='fw fw-cancel fw-stack-1x fw-inverse'></i></span>").appendTo(variableAddCancelButtonPane);
            // Creating add new variable button.
            var variableAddCompleteButtonPane = $("<div class='action-icon-wrapper " +
                "variable-add-complete-action-wrapper'/>").appendTo(variableAddPane);
            $("<span class='fw-stack fw-lg'><i class='fw fw-square fw-stack-2x'></i>" +
                "<i class='fw fw-check fw-stack-1x fw-inverse'></i></span>").appendTo(variableAddCompleteButtonPane);

            // Add new variable activate button.
            $(addVariableButton).click(function () {
                $(variableAddPane).show();
                $(this).hide();
            });

            // Cancel adding a new variable.
            $(variableAddCancelButtonPane).click(function () {
                $(variableAddPane).hide();
                $(addVariableButton).show();
            });

            // Rendering the variables
            _renderVariables(variablesContentWrapper, model);

            // When a new variable is created.
            $(variableAddCompleteButtonPane).click(function () {
                var declaredVariables = model.getVariableDeclarations();
                var typeOfNewVariable = variableSelect.val();
                var identifierOfNewVariable = variableText.val();

                // Validate whether there already a variable with the same identifier.
                var alreadyDeclaredVariable = _.find(declaredVariables, function (declaredVariable) {
                    return declaredVariable.getIdentifier() == identifierOfNewVariable;
                });

                if (_.isNil(alreadyDeclaredVariable)) {
                    var newVariableDeclaration = BallerinaASTFactory.createVariableDeclaration();

                    // Pushing new variable declaration
                    newVariableDeclaration.setType(typeOfNewVariable);
                    newVariableDeclaration.setIdentifier(identifierOfNewVariable);

                    declaredVariables.push(newVariableDeclaration);

                    // Get the index of the last variable declaration.
                    var index = _.findLastIndex(model.getChildren(), function (child) {
                        return child instanceof VariableDeclaration;
                    });

                    // index = -1 when there are not any variable declarations, hence get the index for connector
                    // declarations.
                    if (index == -1) {
                        index = _.findLastIndex(model.getChildren(), function (child) {
                            return child instanceof ConnectorDeclaration;
                        });
                    }

                    model.addChild(newVariableDeclaration, index + 1);

                    // Rendering the variables after adding a new variable.
                    _renderVariables(variablesContentWrapper, model, collapserWrapper, variablesContentWrapper);

                    // Changing the content of the collapser.
                    collapserWrapper.empty();
                    collapserWrapper.data("collapsed", "false");
                    $("<i class='fw fw-left'></i>").appendTo(collapserWrapper);
                } else {
                    // TODO : Show alert of error.
                }
            });

            // The click event for hiding and showing variables.
            collapserWrapper.click(function () {
                $(this).empty();
                if ($(this).data("collapsed") === "false") {
                    $(this).data("collapsed", "true");
                    $("<i class='fw fw-right'></i>").appendTo(this);
                    variablesContentWrapper.find(".variable-wrapper").hide();
                } else {
                    $(this).data("collapsed", "false");
                    $("<i class='fw fw-left'></i>").appendTo(this);
                    variablesContentWrapper.find(".variable-wrapper").show();
                }
            });

            // By default the variable pane is shown on pane load.
            $(activatorElement).css("opacity", 1);

            // When the variable button is clicked we show and hide the variable pane.
            $(activatorElement).click(function () {
                if ($(variablePaneWrapper).is(":visible")) {
                    // Variable pane is already shown.
                    $(this).css({opacity: ''});
                    variablePaneWrapper.hide();

                } else {
                    // Variable pane is hidden.
                    $(this).css("opacity", 1);
                    variablePaneWrapper.show();
                }
            });

            // Stop propagating event to elements behind. This is needed for closing the wrapper when clicked outside.
            variablePaneWrapper.click(function (event) {
                event.stopPropagation();
            });

            return variablePaneWrapper;
        };

        /**
         * Rendering the variables belonging to a model.
         * @param variablePaneWrapper - The wrapper to which the variables should be appended to.
         * @param model - The model, need for getting the declared variables.
         * @private
         */
        function _renderVariables(variablePaneWrapper, model) {
            if (model.getVariableDeclarations().length > 0) {
                // Clear existing variables on UI.
                $(variablePaneWrapper).find(".variable-wrapper").remove();

                _.forEach(model.getVariableDeclarations(), function (variableDeclaration) {
                    var variableType = variableDeclaration.getType();
                    // TODO : Revisit whether color should be changed or not.
                    var variableWrapperClass = "variable-wrapper variable-wrapper-message";
                    var variableTypeWrapperClass = "variable-type variable-type-message";
                    var variableIdentifierClass = "variable-identifier variable-identifier-message";

                    var variableWrapper = $("<div/>", {
                        class: variableWrapperClass
                    }).appendTo(variablePaneWrapper);

                    var variableTypeWrapper = $("<div/>", {
                        text: variableType,
                        class: variableTypeWrapperClass
                    }).appendTo(variableWrapper);

                    var variableIdentifierWrapper = $("<input/>", {
                        type: "text",
                        class: variableIdentifierClass,
                        data: {
                            val: variableDeclaration.getIdentifier()
                        }
                    }).val(variableDeclaration.getIdentifier()).appendTo(variableWrapper);

                    // When variable identifier is changed, update the model.
                    $(variableIdentifierWrapper).on("change keyup input", {
                        model: model
                    }, function (event) {
                        var oldVariableIdentifier = $(event.currentTarget).data("val");
                        var newVariableIdentifier = $(event.currentTarget).val();

                        $(event.currentTarget).data("val", newVariableIdentifier);
                        var declaredVariables = event.data.model.getVariableDeclarations();

                        // Updating the model. Updating this model also updates in model.getChildren().
                        var variableToUpdate = _.find(declaredVariables, function (declaredVariable) {
                            return declaredVariable.getIdentifier() === oldVariableIdentifier;
                        });

                        variableToUpdate.setIdentifier(newVariableIdentifier);

                    });

                    // Creating delete button.
                    var variableDeleteIcon = $("<i class='fw fw-cancel'></i>").appendTo(variableWrapper);

                    // When delete button is clicked, update the UI and the model.
                    $(variableDeleteIcon).click({
                        variableWrapper: variableWrapper,
                        variableIdentifier: variableIdentifierWrapper.val(),
                        model: model
                    }, function (event) {
                        var declaredVariables = event.data.model.getVariableDeclarations();

                        // Deleting variable from UI.
                        event.data.variableWrapper.remove();

                        // Deleting variable from the model.
                        _.remove(declaredVariables, function (declaredVariable) {
                            return declaredVariable.getIdentifier() === event.data.variableIdentifier;
                        });

                        // Deleting the variable from the children.
                        _.remove(model.getChildren(), function (child) {
                            return BallerinaASTFactory.isVariableDeclaration(child) &&
                                child.getIdentifier() === event.data.variableIdentifier;
                        });

                        // Re-rendering variable declarations.
                        _renderVariables(variablePaneWrapper, model);
                    });
                });
            }
        }

        var variableView = {};

        variableView.createVariableButton = createVariableButton;
        variableView.createVariablePane = createVariablePane;

        return variableView;
    });