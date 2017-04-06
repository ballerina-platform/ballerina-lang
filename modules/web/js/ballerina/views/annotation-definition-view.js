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
import BallerinaASTFactory from 'ballerina/ast/ballerina-ast-factory';
import Alerts from 'alerts';

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
        this.drawAccordionCanvas(this._viewOptions, this.getModel().getID(), this.getModel().type.toLowerCase(),
            this.getModel().getAnnotationName());

        this.getPanelIcon().addClass(_.get(this._viewOptions, "cssClass.annotation_icon", ""));
        var self = this;

        var currentContainer = $('#' + this.getModel().getID());
        this._container = currentContainer;
        this._package = diagramRenderingContext.getPackagedScopedEnvironment().getCurrentPackage();

        $(_.get(this._viewOptions, "design_view.container", "")).scrollTop(currentContainer.parent().position().top);
        var headingBox = $('#' + this.getModel().getID() + "_heading");
        var canvas_heading_new = _.get(this._viewOptions, "cssClass.canvas_heading_new", "");
        var new_drop_timeout = _.get(this._viewOptions, "design_view.new_drop_timeout", "");
        headingBox.addClass(canvas_heading_new);
        setTimeout(function () {
            headingBox.removeClass(canvas_heading_new);
        }, new_drop_timeout);

        $(this.getTitle()).text(this.getModel().getAnnotationName()).on('change paste keyup', function () {
            self.getModel().setAnnotationName($(this).text());
        })
            .on('click', function (event) {
                event.stopPropagation();
            })
            .on('blur', function (event) {
                if ($(this).text().length > 50) {
                    var textToDisplay = $(this).text().substring(0, 47) + '...';
                    $(this).text(textToDisplay);
                }
            })
            .on('focus', function (event) {
                $(this).text(self.getModel().getAnnotationName());
            })
            .keypress(function (e) {
                /* Ignore Delete and Backspace keypress in firefox and capture other keypress events.
                 (Chrome and IE ignore keypress event of these keys in browser level)*/
                if (!_.isEqual(e.key, 'Delete') && !_.isEqual(e.key, 'Backspace')) {
                    var enteredKey = e.which || e.charCode || e.keyCode;
                    // Disabling enter key
                    if (_.isEqual(enteredKey, 13)) {
                        e.stopPropagation();
                        return false;
                    }

                    var newAnnotationName = $(this).text() + String.fromCharCode(enteredKey);

                    try {
                        self.getModel().setAnnotationName(newAnnotationName);
                    } catch (error) {
                        // Alerts.error(error);
                        e.stopPropagation();
                        return false;
                    }
                }
            });

        var annotationContentWrapper = $("<div/>", {
            id: this.getModel().getID(),
            class: "annotation-definition-content-wrapper"
        }).data("model", this.getModel()).appendTo(this.getBodyWrapper());

        var annotationOperationsWrapper = $("<div/>", {
            class: "annotation-definition-operation-wrapper"
        }).appendTo(annotationContentWrapper);

        var typeDropDownWrapper = $('<div class="type-drop-wrapper annotation-definition"></div>')
            .appendTo(annotationOperationsWrapper);

        var typeDropDown = $("<select/>").appendTo(typeDropDownWrapper);

        $(typeDropDown).select2({
            data: this._getTypeDropdownValues()
        });

        $(document).ready(function () {
            $(typeDropDownWrapper).empty();
            typeDropDown = $("<select/>").appendTo(typeDropDownWrapper);
            $(typeDropDown).select2({
                tags: true,
                selectOnClose: true,
                data: self._getTypeDropdownValues(),
                query: function (query) {
                    var data = {result: []};
                    if (!_.isNil(query.term)) {
                        _.forEach(self._getTypeDropdownValues(), function (item) {
                            if (item.text.toUpperCase().indexOf(query.term.toUpperCase()) >= 0) {
                                data.results.push(item);
                            }
                        });
                        if (data.results.length == 0) {
                            data.results.push({id: query.term, text: query.term});
                        }
                    } else {
                        data.results = self._getTypeDropdownValues();
                    }
                    query.callback(data);
                }
            });
            $(typeDropDown).on("select2:open", function () {
                $(".select2-search__field").attr("placeholder", "search");
            });
        });

        var attributeIdentifierTextBox = $("<input/>", {
            type: "text",
            class: "annotation-attribute-identifier-text-input",
            "placeholder": "Identifier"
        }).keypress(function (e) {
            if (!_.isEqual(e.key, "Delete") && !_.isEqual(e.key, "Backspace")) {
                var enteredKey = e.which || e.charCode || e.keyCode;
                // Adding new attribute upon enter key.
                if (_.isEqual(enteredKey, 13)) {
                    addAnnotationAttributeButton.click();
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
        }).keydown(function (e) {
            var enteredKey = e.which || e.charCode || e.keyCode;

            // If tab pressed.
            if (e.shiftKey && _.isEqual(enteredKey, 9)) {
                typeDropDown.dropdownButton.trigger("click");
            }
        }).appendTo(annotationOperationsWrapper);

        var valueTextBox = $("<input/>", {
            type: "text",
            class: "annotation-default-value-text-input",
            "placeholder": "Default Value"
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
        }).keydown(function (e) {
            var enteredKey = e.which || e.charCode || e.keyCode;

            // If tab pressed.
            if (e.shiftKey && _.isEqual(enteredKey, 9)) {
                typeDropdown.dropdownButton.trigger("click");
            }
        }).appendTo(annotationOperationsWrapper);

        var addAnnotationAttributeButton = $("<div class='add-annotation-variable-button pull-left'/>")
            .appendTo(annotationOperationsWrapper);

        $("<span class='fw-stack fw-lg'><i class='fw fw-square fw-stack-2x'></i>" +
            "<i class='fw fw-check fw-stack-1x fw-inverse add-annotation-variable-button-square'></i></span>").appendTo(addAnnotationAttributeButton);

        $(addAnnotationAttributeButton).click(function () {
            try {
                var bType = typeDropdown.select2('data')[0].text;
                var identifier = $(attributeIdentifierTextBox).val().trim();
                var defaultValue = $(valueTextBox).val().trim();

                self.getModel().addVariableDefinition(bType, identifier, defaultValue);

                self._renderVariableDefinitions(annotationAttributeVariablesWrapper);

                $(attributeIdentifierTextBox).val("");
                $(valueTextBox).val("");
            } catch (e) {
                Alerts.error(e);
            }
        });

        var annotationAttributeVariablesWrapper = $("<div/>", {
            class: "annotation-attribute-content-variables-wrapper"
        }).appendTo(annotationContentWrapper);


        var annotationDefinitionContainerId = "annotation-definition-container___" + this._model.id;
        var annotationDefinitionContainer = $('<div id="' + annotationDefinitionContainerId +
            '" class="annotation-definition-container"></div>');

        var annotationDefinitionInnerContainer = $('<div class="annotation-definition-inner-container"></div>');
        var definitionSignatureRow = $('<div class="row"></div>');

        var annotationColumn2 = $('<div class="col-lg-5"></div>');
        var annotationInputGroup2 = $('<div class="input-group"></div>');
        var annotationInputGroupAddon2 = $('<span class="input-group-addon"></span>');

        annotationInputGroup2.append(annotationInputGroupAddon2);
        annotationColumn2.append(annotationInputGroup2);
        definitionSignatureRow.append(annotationColumn2);

        var annotationColumn3 = $('<div class="col-lg-2"></div>');
        var annotationInputGroup3 = $('<div class="input-group"></div>');

        annotationColumn3.append(annotationInputGroup3);
        definitionSignatureRow.append(annotationColumn3);

        annotationDefinitionInnerContainer.append(definitionSignatureRow);

        var attachmentLabel = $("<span>Attachments</span>");
        annotationInputGroupAddon2.append(attachmentLabel);
        var attachmentInput = $("<input type='text' class='attachments form-control'/>");
        annotationInputGroup2.append(attachmentInput);

        attachmentInput.val(_.join(this.getModel().getAttachmentPoints(), ','));

        attachmentInput.on('change', function (e) {
            if (!_.isNil(attachmentInput.val())) {
                self.getModel().setAttachmentPoints(_.split(attachmentInput.val()), {doSilently: false});
            }
        });

        var addAttributeButton = $("<div id='add-attribute-btn' class='add-annotation-attribute-button-wrapper'" +
            "data-toggle='tooltip' data-placement='bottom' data-original-title='Add Attribute'><i class='fw fw-add'></i></div>");
        annotationInputGroup3.append(addAttributeButton);

        var attributeRow = $('<div class="row attribute-row"></div>');
        var listColumn = $('<div class="col-lg-5"></div>');
        var listOfAttributes = $('<ul class="attribute-list list-group"></ul>');

        annotationDefinitionInnerContainer.append("<span class='input-label'>Attributes:</span>");

        listColumn.append(listOfAttributes);
        attributeRow.append(listColumn);
        annotationDefinitionInnerContainer.append(attributeRow);

        addAttributeButton.on('click', function (e) {
            var leftHandSideInputs = listOfAttributes.find(".left-hand-side");
            var isEmptyExists = false;
            if (leftHandSideInputs && leftHandSideInputs.length > 0) {
                _.forEach(leftHandSideInputs, function (leftHandSideInput) {
                    if (_.isEqual($(leftHandSideInput).val(), "")) {
                        isEmptyExists = true;
                    }
                });

                if (isEmptyExists) {
                    return;
                }
            }

            var listItem = $('<li class="list-group-item"></li>');
            var listItemRow = $('<div class="row"></div>');
            var leftHandSideColumn = $('<div class="col-lg-5"></div>');
            var leftHandSideInputGroup = $('<div class="input-group"></div>');

            leftHandSideColumn.append(leftHandSideInputGroup);
            listItemRow.append(leftHandSideColumn);

            listItemRow.append("<div class='col-lg-1'>=</div>");

            var rightHandSideColumn = $('<div class="col-lg-5"></div>');
            var rightHandSideInputGroup = $('<div class="input-group"></div>');

            rightHandSideColumn.append(rightHandSideInputGroup);
            listItemRow.append(rightHandSideColumn);

            var closeButton = $("<div class='col-lg-1 close-btn'></div>");
            var closeIcon = $("<i class='fw fw-cancel '></i>");

            closeButton.append(closeIcon);
            listItemRow.append(closeButton);

            closeButton.on('click', function (e) {
                $(this).closest("li").remove();
            });

            var leftHandSideInput = $('<input type="text" class="left-hand-side form-control" placeholder="eg:-string value"/>');
            var rightHandSideInput = $('<input type="text" class="right-hand-side form-control"/>');

            leftHandSideInputGroup.append(leftHandSideInput);
            rightHandSideInputGroup.append(rightHandSideInput);

            listItem.append(listItemRow);

            listOfAttributes.append(listItem);
        });

        annotationDefinitionContainer.append(annotationDefinitionInnerContainer);
        currentContainer.find('svg').parent().append(annotationDefinitionContainer);
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
}

export default AnnotationDefinitionView;