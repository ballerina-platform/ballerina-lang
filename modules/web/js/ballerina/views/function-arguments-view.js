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
import Alerts from 'alerts';
import BallerinaASTFactory from 'ballerina/ast/ballerina-ast-factory';

/**
 * Creates the arguments pane.
 * @param {Object} args - Arguments for creating the view.
 * @param {Object} args.activatorElement - The variable button which activates to show the pane.
 * @param {ServiceDefinition} args.model - The service definition model.
 * @param {Object} args.paneAppendElement - The element to which the pane should be appended to.
 * @param {Object} [args.viewOptions={}] - Configuration values for the view.
 * @param {boolean} [args.disableEditing] - Disable editing the pane.
 */
var createArgumentsPane = function (args, diagramRenderingContext) {
    var activatorElement = _.get(args, "activatorElement");
    var model = _.get(args, "model");
    var paneElement = _.get(args, "paneAppendElement");
    var viewOptions = _.get(args, "viewOptions");
    this._viewOfModel = _.get(args, "view");
    var disableEditing = _.get(args, "disableEditing");
    var self = this;

    var argumentsEditorWrapper = $("<div/>", {
        class: "main-action-wrapper arguments-main-action-wrapper"
    }).appendTo(paneElement);

    this._argumentsEditorWrapper = argumentsEditorWrapper;

    // Positioning the main wrapper
    argumentsEditorWrapper.css("left",
        viewOptions.position.left - parseInt(argumentsEditorWrapper.css("width"), 10));
    argumentsEditorWrapper.css("top", viewOptions.position.top);

    // Creating header content.
    var headerWrapper = $("<div/>", {
        class: "action-content-wrapper-heading arguments-wrapper-heading"
    }).appendTo(argumentsEditorWrapper);

    // Hiding the editing wrapper if editing disabled.
    if (disableEditing) {
        headerWrapper.hide();
    }

    // Creating arguments dropdown.
    var typeDropdownWrapper = $('<div class="type-drop-wrapper"/>').appendTo(headerWrapper);

    this._argumentTypeDropDown = $("<select/>").appendTo(typeDropdownWrapper);

    $(this._argumentTypeDropDown).select2({
        data: _getTypeDropdownValues(diagramRenderingContext),
        tags: true,
        selectOnClose: true
    });

    $(document).ready(function() {
        $(typeDropdownWrapper).empty();
        self._argumentTypeDropDown = $("<select/>").appendTo(typeDropdownWrapper);
        $(self._argumentTypeDropDown).select2({
            tags: true,
            selectOnClose: true,
            data : _getTypeDropdownValues(diagramRenderingContext),
            query: function (query) {
                var data = {results: []};
                if (!_.isNil(query.term)) {
                    _.forEach(_getTypeDropdownValues(diagramRenderingContext), function (item) {
                        if (item.text.toUpperCase().indexOf(query.term.toUpperCase()) >= 0) {
                            data.results.push(item);
                        }
                    });
                    // Adding user typed string when there is no any matching item in the list
                    if(data.results.length == 0){
                        data.results.push({id: query.term, text: query.term});
                    }
                } else {
                    data.results = _getTypeDropdownValues(diagramRenderingContext);
                }
                query.callback(data);
            }
        });

        $(self._argumentTypeDropDown).on("select2:open", function() {
            $(".select2-search__field").attr("placeholder", "Search");
        });
    });

    // Text input for editing the identifier of an arguments.
    var argumentIdentifierInput = $("<input/>", {
        type: "text",
        placeholder: "Identifier"
    }).appendTo(headerWrapper);

    // Wrapper for the add and check icon.
    var addIconWrapper = $("<div/>", {
        class: "action-icon-wrapper arguments-action-icon"
    }).appendTo(headerWrapper);

    var addButton = $("<span class='fw-stack fw-lg'><i class='fw fw-square fw-stack-2x'></i>" +
        "<i class='fw fw-add fw-stack-1x fw-inverse arguments-action-icon-i'></i></span>")
        .appendTo(addIconWrapper);

    // Adding a value to a new arguments.
    $(addButton).click(() => {
        try {
            var argumentType = this._argumentTypeDropDown.val();
            var argumentValue = argumentIdentifierInput.val();

            if (!_.isEmpty(argumentValue)) {
                // Sets the argument in the model
                model.addArgument(argumentType, argumentValue);

                //Clear the text box and drop down value
                argumentIdentifierInput.val("");

                // Recreating the arguments details view.
                _createCurrentArgumentsView(model, argumentsContentWrapper, this._argumentTypeDropDown, headerWrapper,
                    disableEditing);
            }
        } catch (error) {
            Alerts.error(error);
        }
    });

    // Add new argument upon enter key.
    $(argumentIdentifierInput).on("change paste keydown", function (e) {
        if (e.which == 13) {
            addButton.click();
        }
    });

    // Creating the content editing div.
    var argumentsContentWrapper = $("<div/>", {
        class: "action-content-wrapper-body arguments-details-wrapper"
    }).appendTo(argumentsEditorWrapper);

    // Creating the arguments details view.
    _createCurrentArgumentsView(model, argumentsContentWrapper, this._argumentTypeDropDown, headerWrapper,
        disableEditing);

    // Showing and hiding the arguments pane upon arguments button/activator is clicked.
    $(activatorElement).click({
        argumentsEditorWrapper: argumentsEditorWrapper,
        argumentIdentifierInput: argumentIdentifierInput
    }, function (event) {
        if ($(event.currentTarget).data("showing-pane") === "true") {
            $(event.currentTarget).removeClass("operations-argument-icon");
            event.data.argumentsEditorWrapper.hide();
            $(event.currentTarget).data("showing-pane", "false");
        } else {
            $(event.currentTarget).addClass("operations-argument-icon");
            event.data.argumentsEditorWrapper.show();
            $(event.currentTarget).data("showing-pane", "true");
            $(event.data.argumentIdentifierInput).focus();
        }
    });

    $(argumentsEditorWrapper).click(function (event) {
        event.stopPropagation();
    });

    // On window click.
    $(window).click({
        activatorElement: activatorElement,
        argumentsEditorWrapper: argumentsEditorWrapper
    }, function (event) {
        if ($(event.data.activatorElement).data("showing-pane") === "true"){
            $(event.data.activatorElement).click();
        }
    });

    return this;
};

/**
 * Creates the arguments detail wrapper and its events.
 * @param model - The arguments data.
 * @param wrapper - The wrapper element which these details should be appended to.
 * @param argumentsTypeDropDown - The dropdown which has the available arguments.
 * @param headerWrapper - Wrapper which container the arguments editor.
 * @param {boolean} disableEditing - Disable editing.
 * @private
 */
function _createCurrentArgumentsView(model, wrapper, argumentsTypeDropDown, headerWrapper, disableEditing) {
    // Clearing all the element in the wrapper as we are rerendering the arguments view.
    wrapper.empty();

    // Creating arguments info.
    _.forEach(model.getArguments(), function (argument, index) {
        var argumentWrapper = $("<div/>", {
            class: "arguments-detail-wrapper"
        }).appendTo(wrapper);

        // Creating a wrapper for the argument type.
        $("<div/>", {
            text: argument.type,
            class: "arguments-detail-type-wrapper"
        }).appendTo(argumentWrapper);

        // Creating a wrapper for the argument value.
        var argumentIdentifierWrapper = $("<div/>", {
            text: ": " + argument.identifier,
            class: "arguments-detail-identifier-wrapper"
        }).appendTo(argumentWrapper);

        var deleteIcon = $("<i class='fw fw-cancel arguments-detail-close-wrapper'></i>");

        deleteIcon.appendTo(argumentWrapper);

        // Hiding the delete icon if editing is disabled.
        if (disableEditing) {
            deleteIcon.hide();
        }

        // Removes the value of the argument in the model and rebind the arguments to the arguments view.
        deleteIcon.click(function () {
            $(argumentWrapper).remove();
            model.removeArgument(argument.identifier);
            _createCurrentArgumentsView(model, wrapper, argumentsTypeDropDown, headerWrapper, disableEditing);
        });

        // Not add a thematic break.
        if (model.getArguments().length - 1 != index) {
            $("<hr/>").appendTo(wrapper);
        }

        if (!disableEditing) {
            // When an arguments detail is clicked.
            argumentWrapper.click({
                clickedArgumentValueWrapper: argumentIdentifierWrapper,
                deleteIcon: deleteIcon,
                argument: argument
            }, function (event) {
                var clickedArgumentValueWrapper = event.data.clickedArgumentValueWrapper;
                var argument = event.data.argument;
                var deleteIcon = event.data.deleteIcon;

                // Empty the content inside the arguments value and type wrapper.
                clickedArgumentValueWrapper.empty();

                // Changing the background
                $(event.currentTarget).css("background-color", "#f5f5f5");

                // Creating the text area for the identifier of the argument.
                var argumentValueTextbox = $("<input/>", {
                    val: argument.identifier
                }).appendTo(clickedArgumentValueWrapper);

                argumentValueTextbox.click(function (event) {
                    event.stopPropagation();
                });

                // Gets the user input and set it as the argument identifier.
                argumentValueTextbox.on("change keyup input", function (e) {
                    argument.setIdentifier(e.target.value);
                });

                // Adding in-line display block to override the hovering css.
                deleteIcon.show();

                // Resetting of other arguments wrapper which has been used for editing.
                argumentWrapper.siblings().each(function () {

                    // Removing the text box of other arguments and use simple text.
                    var argumentIdentifierDiv = $(this).children().eq(1);
                    if (argumentIdentifierDiv.find("input").length > 0) {
                        // Reverting the background color of other argument editors.
                        $(this).removeAttr("style");

                        var argumentIdentifier = ": " + argumentIdentifierDiv.find("input").val();
                        argumentIdentifierDiv.empty().text(argumentIdentifier);

                        deleteIcon.removeAttr("style");
                    }
                });
            });
        }
    });
}

/**
 * Gets the supported ballerina datatypes.
 * @param  {DiagramRenderContext} diagramRenderingContext The diagram rendering context which the data types are
 *  picked up form.
 * @return {Object[]} An array of supported data types.
 */
function _getTypeDropdownValues(diagramRenderingContext) {
    var dropdownData = [];
    // Adding items to the type dropdown.
    var bTypes = diagramRenderingContext.getEnvironment().getTypes();
    _.forEach(bTypes, function (bType) {
        dropdownData.push({id: bType, text: bType});
    });

    return dropdownData;
}

/**
 * moves the position of argument editor view
 * @param {Object} args - object which contains delta values for x and y
 * @param {Number} args.dx - delta value for x value
 * @param {Number} args.dy - delta value for y value
 */
function move(args) {
    var dx = _.get(args, "dx", 0);
    var dy = _.get(args, "dy", 0);

    // Left margin of the connector action view
    var leftMargin = this._viewOfModel.getBoundingBox().x();
    var argumentsEditorLeft = parseInt(this._argumentsEditorWrapper.css("left"), 10) + dx;
    // This is to ensure that argument editor doesn't go beyond the left margin of the connector action.(to avoid clipping)
    if (leftMargin < argumentsEditorLeft) {
        this._argumentsEditorWrapper.css("left", argumentsEditorLeft);
    } else {
        this._argumentsEditorWrapper.css("left", leftMargin);
    }
    this._argumentsEditorWrapper.css("top", (parseInt(this._argumentsEditorWrapper.css("top"), 10) + dy));
}

/**
 * Reload the types in the argument type drop down
 */
function reloadArgumentTypeDropDown() {
    var self = this;
    $(this._argumentTypeDropDown).select2('destroy').empty();
    var defaultTypes = this._viewOfModel.getDiagramRenderingContext().getEnvironment().getTypes();
    _.forEach(defaultTypes, function (bType) {
        $(self._argumentTypeDropDown).select2({data: [{id: bType, text: bType}]});
    });
    var typesInPackage = this._viewOfModel.getDiagramRenderingContext().getPackagedScopedEnvironment().getCurrentPackage().getTypesInPackage();
    _.forEach(typesInPackage, function (bType) {
        $(self._argumentTypeDropDown).select2({data: [{id: bType, text: bType}]});
    });
}

var argumentsView = {};

argumentsView.createArgumentsPane = createArgumentsPane;
argumentsView.reloadArgumentTypeDropDown = reloadArgumentTypeDropDown;
argumentsView.move = move;

export default argumentsView;
