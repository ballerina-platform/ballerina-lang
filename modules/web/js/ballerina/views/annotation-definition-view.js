/**
 * Copyright (c) 2016-2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import log from 'log';
import $ from 'jquery';
import Canvas from './canvas';
import SVGCanvas from './../../ballerina/views/svg-canvas';
import AnnotationDefinition from './../ast/annotation-definition';
import ASTNode from './../ast/node';
import BallerinaASTFactory from 'ballerina/ast/ballerina-ast-factory';
import Alerts from 'alerts';
import AnnotationAttributeDefinitionView from './annotation-attribute-definition-view';

/**
 * The view to represent an annotation definition which is an AST Visitor.
 * */
class AnnotationDefinitionView extends SVGCanvas {
    constructor(args) {
        super(args);

        this._annotationName = _.get(args, 'definitionName', "");
        this._attachedDefinitions = _.get(args, 'attachedDefinitions', []);
        this._annotationProperties = _.get(args, 'annotationProperties', []);
        this._parentView = _.get(args, 'parentView');
        this._viewOptions.offsetTop = _.get(args, 'viewOptionsOffsetTop', 75);
        this._viewOptions.topBottomTotalGap = _.get(args, 'viewOptionsTopBottomTotalGap', 100);
        this._viewOptions.panelIcon = _.get(args.viewOptions, 'cssClass.service_icon');
        this._viewOptions.minHeight = _.get(args, 'minHeight', 300);

        this._totalHeight = 170;

        if (_.isNil(this._model) || !(this._model instanceof AnnotationDefinition)) {
            log.error('Annotation definition is undefined or is of different type.' + this._model);
            throw 'Annotation definition is undefined or is of different type.' + this._model;
        }

        if (_.isNil(this._container)) {
            log.error('Container for annotation definition is undefined.' + this._container);
            throw 'Container for annotation definition is undefined.' + this._container;
        }
    }

    setModel(model) {
        if (!_.isNil(model) && model instanceof AnnotationDefinition) {
            this._model = model;
        } else {
            log.error('Annotation definition is undefined or is of different type.' + model);
            throw 'Annotation definition is undefined or is of different type.' + model;
        }
    }

    setContainer(container) {
        if (!_.isNil(container)) {
            this._container = container;
        } else {
            log.error('Container for annotation definition is undefined.' + container);
            throw 'Container for annotation definition is undefined.' + container;
        }
    }

    setViewOptions(viewOptions) {
        this._viewOptions = viewOptions;
    }

    getModel() {
        return this._model;
    }

    getContainer() {
        return this._container;
    }

    getViewOptions() {
        return this._viewOptions;
    }

    render(diagramRenderingContext) {
        this.setDiagramRenderingContext(diagramRenderingContext);

        // Draws the outlying body of the struct definition.
        this.drawAccordionCanvas(this._viewOptions, this.getModel().getID(), this.getModel().getType().toLowerCase(), this.getModel().getAnnotationName());

        // Setting the styles for the canvas icon.
        this.getPanelIcon().addClass(_.get(this._viewOptions, "cssClass.annotation_icon", ""));

        var self = this;

        //Scroll to the added position and highlight the heading
        var currentContainer = $('#' + this.getModel().getID());

        $(_.get(this._viewOptions, "design_view.container", "")).scrollTop(currentContainer.parent().position().top);
        var hadingBox = $('#' + this.getModel().getID() + "_heading");
        var canvas_heading_new = _.get(this._viewOptions, "cssClass.canvas_heading_new", "");
        var new_drop_timeout = _.get(this._viewOptions, "design_view.new_drop_timeout", "");
        hadingBox.addClass(canvas_heading_new);
        setTimeout(function () {
            hadingBox.removeClass(canvas_heading_new);
        }, new_drop_timeout);

        $(this.getTitle()).text(this.getModel().getAnnotationName())
            .on("change paste keyup", function () {
                self.getModel().setAnnotationName($(this).text());
            }).on("click", function (event) {
            event.stopPropagation();
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

                var newAnnotationName = $(this).val() + String.fromCharCode(enteredKey);

                try {
                    self.getModel().setAnnotationName(newAnnotationName);
                } catch (error) {
                    Alerts.error(error);
                    e.stopPropagation();
                    return false;
                }
            }
        });
        /////////////////////////////////////////////////

        var attachmentButton = $('<div class="attachments-btn" data-toggle="tooltip" title="Attachments" data-placement="bottom"></div>')
            .appendTo(this.getBodyWrapper()).tooltip();

        // Positioning the attachment button.
        attachmentButton.css('left', '5px');
        attachmentButton.css('top', '5px');

        $('<span class="btn-icon">Attachments</span>').appendTo(attachmentButton);

        var attachmentPaneWrapper = $('<div class="attachment-pane"/>').appendTo($(this.getBodyWrapper()));
        // Positioning the variable pane from the left border of the container(service, resource, etc).
        attachmentPaneWrapper.css('left', (5 + 80) + 'px');
        // Positioning the variable pane from the top border of the container(service, resource, etc).
        attachmentPaneWrapper.css('top', (5 - 0) + 'px');
        // Setting max-width of the variable wrapper.
        attachmentPaneWrapper.css('max-width', this._viewOptions.width + 'px');
        attachmentPaneWrapper.css('margin-bottom', 10 + 'px');

        var attachmentContentWrapper = $('<div class="variables-content-wrapper"/>').appendTo(attachmentPaneWrapper);

        var collapserWrapper = $('<div class="variable-pane-collapser-wrapper"/>')
            .data('collapsed', 'true')
            .appendTo(attachmentPaneWrapper);
        $('<i class=\'fw fw-left\'></i>').appendTo(collapserWrapper);

        var variablesActionWrapper = $('<div class="variables-action-wrapper"/>').appendTo(attachmentContentWrapper);

        // Creating add variable editor button.
        var addVariableButton = $('<div class="action-icon-wrapper variable-add-icon-wrapper" ' +
            'data-toggle="tooltip" title="Add Attachment" data-placement="bottom"/>')
            .appendTo(variablesActionWrapper).tooltip();
        $('<i class="fw fw-add"></i>').appendTo(addVariableButton);

        var variableAddPane = $('<div class="action-content-wrapper-heading attachment-add-action-wrapper"/>')
            .appendTo(variablesActionWrapper);

        // Creating the variable type dropdown.
        var variableIdentifier = $('<input id="text" placeholder="Identifier"/>').appendTo(variableAddPane);

        // Add new variable upon enter key.
        $(variableIdentifier).on('change paste keydown', function (e) {
            if (_.isEqual(e.which, 13)) {
                variableAddCompleteButtonPane.click();
            }
        }).keypress(function (e) {
            var enteredKey = e.which || e.charCode || e.keyCode;

            // Disabling enter key
            if (_.isEqual(enteredKey, 13)) {
                e.stopPropagation();
                return false;
            }

            var newIdentifier = $(this).val() + String.fromCharCode(enteredKey);

            // Validation the identifier against grammar.
            if (!ASTNode.isValidIdentifier(newIdentifier)) {
                var errorString = 'Invalid identifier for a variable: ' + newIdentifier;
                log.error(errorString);
                Alerts.error(errorString);
                e.stopPropagation();
                return false;
            }
        });

        // Creating cancelling add new variable button.
        var variableAddCancelButtonPane = $('<div class="action-icon-wrapper variable-add-cancel-action-wrapper"/>')
            .appendTo(variableAddPane);
        $('<span class="fw-stack fw-lg"><i class="fw fw-square fw-stack-2x"></i>' +
            '<i class="fw-cancel fw-stack-1x fw-inverse"></i></span>').appendTo(variableAddCancelButtonPane);
        // Creating add new variable button.
        var variableAddCompleteButtonPane = $('<div class="action-icon-wrapper ' +
            'variable-add-complete-action-wrapper">').appendTo(variableAddPane);
        $('<span class="fw-stack fw-lg"><i class="fw fw-square fw-stack-2x"></i>' +
            '<i class="fw fw-check fw-stack-1x fw-inverse"></i></span>').appendTo(variableAddCompleteButtonPane);

        // Add new variable activate button.
        $(addVariableButton).click(function () {
            $(variableAddPane).show();
            $(this).hide();
            $(variableIdentifier).focus();
        });

        // Cancel adding a new variable.
        $(variableAddCancelButtonPane).click(function () {
            $(variableAddPane).hide();
            $(addVariableButton).show();
        });

        // Rendering the variables
        this._renderAttachments(attachmentContentWrapper, collapserWrapper);

        // When a new variable is created.
        $(variableAddCompleteButtonPane).click(function () {
            var identifierOfNewVariable = variableIdentifier.val().trim();

            try {
                self._model.addAnnotationAttachmentPoint(identifierOfNewVariable);
                var oldWrapperSize = $(attachmentContentWrapper).height();

                // Recreating the arguments details view.
                self._renderAttachments(attachmentContentWrapper, collapserWrapper);

                // Changing the content of the collapser.
                collapserWrapper.empty();
                collapserWrapper.data('collapsed', 'false');
                $('<i class=\'fw fw-left\'></i>').appendTo(collapserWrapper);
                attachmentContentWrapper.show();

                // Clearing values in inputs.
                variableIdentifier.val('');

                // Trigger the event to inform that a new variable has been added and the height of the variable pane
                // has been changed
                $(attachmentContentWrapper).trigger('contentWrapperShown', $(attachmentContentWrapper).height() - oldWrapperSize);
            } catch (error) {
                log.error(error);
                Alerts.error(error);
            }
        });

        // Hiding/showing the attachments depending on the default "collapsed" value of collapserWrapper.
        if (_.isEqual(collapserWrapper.data('collapsed'), 'false')) {
            $(collapserWrapper).empty();
            $('<i class=\'fw fw-left\'></i>').appendTo(collapserWrapper);
            attachmentContentWrapper.find('.variable-wrapper').show();
            var dh = $(attachmentContentWrapper).height() !== this._minHeight ?
                $(attachmentContentWrapper).height() - this._minHeight : 0;
            $(attachmentPaneWrapper).trigger('contentWrapperShown', dh);
        } else {
            $(collapserWrapper).empty();
            $('<i class=\'fw fw-right\'></i>').appendTo(collapserWrapper);
            attachmentContentWrapper.find('.variable-wrapper').hide();
            var height = $(attachmentContentWrapper).height();
            $(attachmentPaneWrapper).trigger('contentWrapperHidden', height);
        }

        // The click event for hiding and showing attachments.
        collapserWrapper.click(function () {
            $(this).empty();
            if ($(this).data('collapsed') === 'false') {
                $(this).data('collapsed', 'true');
                $('<i class=\'fw fw-right\'></i>').appendTo(this);
                attachmentContentWrapper.find('.variable-wrapper').hide();
                $(attachmentContentWrapper).trigger('contentWrapperHidden');
            } else {
                $(this).data('collapsed', 'false');
                $('<i class=\'fw fw-left\'></i>').appendTo(this);
                attachmentContentWrapper.find('.variable-wrapper').show();
                var dh = $(attachmentContentWrapper).height() !== self._minHeight ?
                    $(attachmentContentWrapper).height() - self._minHeight : 0;
                $(attachmentContentWrapper).trigger('contentWrapperShown', dh);
            }
        });

        // By default the variable pane is shown on pane load.
        $(attachmentButton).css('opacity', 1);

        // When the variable button is clicked we show and hide the variable pane.
        $(attachmentButton).click(function () {
            if ($(attachmentPaneWrapper).is(':visible')) {
                // Variable pane is already shown.
                $(this).css({opacity: ''});
                attachmentPaneWrapper.hide();

            } else {
                // Variable pane is hidden.
                $(this).css('opacity', 1);
                attachmentPaneWrapper.show();
            }
        });

        // Stop propagating event to elements behind. This is needed for closing the wrapper when clicked outside.
        attachmentPaneWrapper.click(function (event) {
            event.stopPropagation();
        });

        /////////////////////////////////////////////////

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
            data : this._getTypeDropdownValues()
        });

        $(document).ready(function() {
            $(typeDropdownWrapper).empty();
            typeDropdown = $("<select/>").appendTo(typeDropdownWrapper);
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
        });

        // Creating the identifier text box.
        var identifierTextBox = $("<input/>", {
            type: "text",
            class: "struct-identifier-text-input",
            "placeholder": "Identifier"
        }).keypress(function (e) {
            /* Ignore Delete and Backspace keypress in firefox and capture other keypress events.
             (Chrome and IE ignore keypress event of these keys in browser level)*/
            if (!_.isEqual(e.key, "Delete") && !_.isEqual(e.key, "Backspace")) {
                var enteredKey = e.which || e.charCode || e.keyCode;
                // Adding new variable upon enter key.
                if (_.isEqual(enteredKey, 13)) {
                    addStructVariableButton.click();
                    e.stopPropagation();
                    return false;
                }

                var newIdentifier = $(this).val() + String.fromCharCode(enteredKey);

                // Validation the identifier against grammar.
                if (!ASTNode.isValidIdentifier(newIdentifier)) {
                    var errorString = "Invalid identifier for a variable: " + newIdentifier;
                    Alerts.error(errorString);
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
        }).appendTo(structOperationsWrapper);

        // Creating the default value text box.
        var defaultValueTextBox = $("<input/>", {
            type: "text",
            class: "struct-default-value-text-input",
            placeholder: "Default Value"
        }).keypress(function (e) {
            /* Ignore Delete and Backspace keypress in firefox and capture other keypress events.
             (Chrome and IE ignore keypress event of these keys in browser level)*/
            if (!_.isEqual(e.key, "Delete") && !_.isEqual(e.key, "Backspace")) {
                var enteredKey = e.which || e.charCode || e.keyCode;
                // Adding new variable upon enter key.
                if (_.isEqual(enteredKey, 13)) {
                    addStructVariableButton.click();
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
                var defaultValue = $(defaultValueTextBox).val().trim();

                self.getModel().addAnnotationAttributeDefinition(bType, identifier, defaultValue);

                self._renderAttributeDefinitions(structVariablesWrapper);

                $(identifierTextBox).val("");
                $(defaultValueTextBox).val("");
            } catch (e) {
                Alerts.error(e);
            }
        });

        //// End of operational panel.

        //// Creating struct content panel

        var structVariablesWrapper = $("<div/>",{
            class: "struct-content-variables-wrapper"
        }).appendTo(structContentWrapper);

        this._renderAttributeDefinitions(structVariablesWrapper);

        $(structVariablesWrapper).click(function(e){
            e.preventDefault();
            return false;
        });

        //// End of struct content panel

        // On window click.
        $(window).click(function (event) {
            self._renderAttributeDefinitions(structVariablesWrapper);
        });

        currentContainer.find('svg').remove();
    }

    _getTypeDropdownValues() {
        var dropdownData = [];
        // Adding items to the type dropdown.
        var bTypes = this.getDiagramRenderingContext().getEnvironment().getTypes();
        _.forEach(bTypes, function (bType) {
            dropdownData.push({id: bType, text: bType});
        });

        // var annotationsTypes = this.getDiagramRenderingContext().getPackagedScopedEnvironment().getCurrentPackage().getAnnotationDefinitions();
        // _.forEach(annotationsTypes, function (sType) {
        //     dropdownData.push({id: sType.getAnnotationName(), text: sType.getAnnotationName()});
        // });

        return dropdownData;
    }

    _renderAttributeDefinitions(wrapper){
        $(wrapper).empty();
        var self = this;
        _.forEach(this._model.getAnnotationAttributeDefinitions(), function(attributeDefinition) {
            var annotationAttributeDefinitionView = new AnnotationAttributeDefinitionView({
                parent: self.getModel(),
                model: attributeDefinition,
                container: wrapper,
                toolPalette: self.getToolPalette(),
                messageManager: self.getMessageManager(),
                parentView: self
            });

            self.getDiagramRenderingContext().getViewModelMap()[attributeDefinition.id] = annotationAttributeDefinitionView;

            annotationAttributeDefinitionView.render(self.getDiagramRenderingContext());

            $(annotationAttributeDefinitionView.getDeleteButton()).click(function(){
                self._renderAttributeDefinitions(wrapper);
            });

            $(annotationAttributeDefinitionView.getWrapper()).click({
                modelID: attributeDefinition.getID()
            }, function(event){
                self._renderAttributeDefinitions(wrapper);
                var annotationAttributeDefinitionView = self.getDiagramRenderingContext()
                    .getViewModelMap()[event.data.modelID];
                annotationAttributeDefinitionView.renderEditView();
            });
        });
    }

    _renderAttachments(attachmentPaneWrapper, collapserWrapper) {
        // Clear existing variables on UI.
        $(attachmentPaneWrapper).find('.variable-wrapper').remove();

        var self = this;

        _.forEach(this._model.getAttachmentPoints(), function (attachmentPoint) {
            var variableDefinitionWrapper = $('<div/>', {
                id: self.getModel().getID(),
                class: 'variable-wrapper variable-wrapper-message'
            }).data('model', self.getModel()).appendTo(attachmentPaneWrapper);

            self._variableDefinitionWrapper = variableDefinitionWrapper;

            var variableDefintionStatementWrapper = $('<span/>', {
                text: attachmentPoint,
                'contenteditable': true,
                class: 'variable-identifier variable-identifier-message',
                'prevValue': attachmentPoint
            }).keyup(function() {
                try {
                    self.getModel().removeAnnotationAttachmentPoints($(variableDefintionStatementWrapper).attr('prevValue'));
                    self.getModel().addAnnotationAttachmentPoint($(this).text().trim()+'');
                    $(variableDefintionStatementWrapper).attr('prevValue',$(this).text().trim()+'');
                } catch (error) {
                    Alerts.error(error);
                }
            }).appendTo(variableDefinitionWrapper);

            // Creating delete button.
            var deleteButton = $('<i class=\'fw fw-cancel\'></i>').appendTo(variableDefinitionWrapper);

            self._deleteButton = deleteButton.get(0);

            $(self._deleteButton).click(() => {
                var oldWrapperSize = $('.variables-content-wrapper').height();
                self.removeAnnotationAttachmentPoints($(variableDefintionStatementWrapper).text().trim()+'');
                self._renderAttachments(attachmentPaneWrapper, collapserWrapper);
                $('.variables-content-wrapper').trigger('contentWrapperShown', $('.variables-content-wrapper').height() - oldWrapperSize);
            });
        });
    }
}
AnnotationDefinitionView.prototype.constructor = Canvas;
export default AnnotationDefinitionView;