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
import log from 'log';
import BallerinaView from './ballerina-view';
import VariablesView from './variables-view';
import TypeStructDefinition from './type-struct-definition-view';
import BallerinaASTFactory from 'ballerina/ast/ballerina-ast-factory';
import SVGCanvas from './svg-canvas';
import TypeMapper from 'typeMapper';
import InputStructView from './input-struct-view';
import OutputStructView from './output-struct-view';
import TypeMapperStatement from './type-mapper-statement-view';
import TypeMapperBlockStatement from './type-mapper-block-statement-view';
import Constants from 'constants';
import AST from './../ast/module';
import select2 from 'select2';
import alerts from 'alerts';
var TYPE_MAPPER_COMBOBOX_DEFAULT_SELECTION = Constants.TYPE_MAPPER_COMBOBOX_DEFAULT_SELECTION;
var TYPE_MAPPER_COMBOBOX_PREVIOUS_SELECTION = Constants.TYPE_MAPPER_COMBOBOX_PREVIOUS_SELECTION;
var TYPE_MAPPER_TARGET_STRUCT_SCHEMA = Constants.TYPE_MAPPER_TARGET_STRUCT_SCHEMA;
var TYPE_MAPPER_SOURCE_STRUCT_SCHEMA = Constants.TYPE_MAPPER_SOURCE_STRUCT_SCHEMA;
var TYPE_MAPPER_TARGET_STRUCT_NAME = Constants.TYPE_MAPPER_TARGET_STRUCT_NAME;
var TYPE_MAPPER_SOURCE_STRUCT_NAME = Constants.TYPE_MAPPER_SOURCE_STRUCT_NAME;
var TYPE_MAPPER_COMBOBOX_TARGET_IS_ALREADY_RENDERED_IN_SOURCE = Constants.TYPE_MAPPER_COMBOBOX_TARGET_IS_ALREADY_RENDERED_IN_SOURCE;
var TYPE_MAPPER_COMBOBOX_SOURCE_IS_ALREADY_RENDERED_IN_TARGET = Constants.TYPE_MAPPER_COMBOBOX_SOURCE_IS_ALREADY_RENDERED_IN_TARGET;

class TypeMapperDefinitionView extends SVGCanvas {
    constructor(args) {
        super(args);

        this._parentView = _.get(args, "parentView");
        //set panel icon for the type mapper
        this._viewOptions.panelIcon = _.get(args.viewOptions, "cssClass.type_mapper_icon");
        this._onConnectInstance = _.get(args, 'onConnectInstance', {});
        this._onDisconnectInstance = _.get(args, 'onDisconnectInstance', {});
        this._sourceInfo = _.get(args, 'sourceInfo', {});
        this._targetInfo = _.get(args, 'targetInfo', {});
        this._selectedSourceStruct = _.get(args, 'selectedSourceStruct', TYPE_MAPPER_COMBOBOX_DEFAULT_SELECTION);
        this._selectedTargetStruct = _.get(args, 'selectedTargetStruct', TYPE_MAPPER_COMBOBOX_DEFAULT_SELECTION);

        if (_.isNil(this._model) || !(BallerinaASTFactory.isTypeMapperDefinition(this._model))) {
            log.error("Type Mapper definition is undefined or is of different type." + this._model);
            throw "Type Mapper definition is undefined or is of different type." + this._model;
        }

        if (_.isNil(this._container)) {
            log.error("Container for Type Mapper definition is undefined." + this._container);
            throw "Container for Type Mapper definition is undefined." + this._container;
        }
        this._typeMapper = undefined;
        this._blockStatementView = undefined;

    }

    canVisitTypeMapperDefinition(typeMapperDefinition) {
        return true;
    }

    canVisitStatement(statement) {
        return true;
    }

    canVisitBlockStatementView(blockStatementView) {
        return true;
    }

    /**
     * Rendering the view of the Type Mapper definition.
     * @param {Object} diagramRenderingContext - the object which is carrying data required for rendering
     */
    render(diagramRenderingContext) {
        this.setDiagramRenderingContext(diagramRenderingContext);
        var self = this;

        // Draws the outlying body of the function.
        this.drawAccordionCanvas(this._viewOptions, this.getModel().getID(), this.getModel().type.toLowerCase(),
            this.getModel().getTypeMapperName());

        var resourceParameter = _.find(self.getModel().getChildren(), function (child) {
            return BallerinaASTFactory.isResourceParameter(child)
        });
        if (!_.isUndefined(resourceParameter)) {
            this.setSelectedSourceStruct(resourceParameter.getType());
        }
        var returnType = _.find(self.getModel().getChildren(), function (child) {
            return BallerinaASTFactory.isReturnType(child)
        });
        if (!_.isUndefined(returnType)) {
            this.setSelectedTargetStruct(returnType.getType());
        }

        // Setting the styles for the canvas icon.
        this.getPanelIcon().addClass(_.get(this._viewOptions, "cssClass.type_mapper_icon", ""));

        var currentContainer = $('#' + this.getModel().getID());
        this._container = currentContainer;

        this._package = diagramRenderingContext.getPackagedScopedEnvironment().getCurrentPackage();

        //Get all the structs which are defined for current package
        var predefinedStructs = this._package.getStructDefinitions();
        this.getSourceInfo()["predefinedStructs"] = predefinedStructs;
        this.getTargetInfo()["predefinedStructs"] = predefinedStructs;

        $(this.getTitle()).text(this.getModel().getTypeMapperName())
            .on("change paste keyup", function () {
                self.getModel().setTypeMapperName($(this).text());
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
                    var newTypeMapperName = $(this).val() + String.fromCharCode(enteredKey);
                    try {
                        self.getModel().setTypeMapperName(newTypeMapperName);
                    } catch (error) {
                        e.stopPropagation();
                        return false;
                    }
                }
            });

        var dataMapperContainerId = "data-mapper-container___" + this._model.id;
        var sourceId = 'sourceStructs' + this.getModel().id;
        var targetId = 'targetStructs' + this.getModel().id;
        var sourceContent = $('<div class="leftType">' +
            '<div class="source-view">' +
            '<select id="' + sourceId + '" class="type-mapper-combo">' +
            '<option value="-1">--Select--</option>' +
            '</select>' +
            '</div></div>');

        var targetContent = $('<div class="rightType">' +
            '<div class="target-view">' +
            '<select id="' + targetId + '" class="type-mapper-combo">' +
            '<option value="-1">--Select--</option>' +
            '</select>' +
            '</div></div>');

        var dataMapperContainer = $('<div id="' + dataMapperContainerId + '" class="data-mapper-container">'
                                            +'<div id ="typeMapperContextMenu" class ="typeMapperContextMenu"></div>');

        dataMapperContainer.append(sourceContent);
        dataMapperContainer.append(targetContent);
        currentContainer.find('svg').parent().append(dataMapperContainer);
        currentContainer.find('svg').remove();


        $(document).ready(function() {

            self.loadSchemasToComboBox(currentContainer, "#" + sourceId, "#" + targetId, predefinedStructs);

            $(".type-mapper-combo").select2();
            $("#" + targetId).on("select2:open", function () {
                var predefinedStructs = self._package.getStructDefinitions();
                if (predefinedStructs.length > 0) {
                    $("#" + targetId).empty().append('<option value="-1">--Select--</option>');
                    self.getTargetInfo()["predefinedStructs"] = predefinedStructs;
                    self.loadSchemaToComboBox(currentContainer, "#" + targetId, predefinedStructs);
                    $("#" + targetId).val(-1).change();
                }
            });

            $("#" + sourceId).on("select2:open", function () {
                var predefinedStructs = self._package.getStructDefinitions();
                if (predefinedStructs.length > 0) {
                    $("#" + sourceId).empty().append('<option value="-1">--Select--</option>');
                    self.getSourceInfo()["predefinedStructs"] = predefinedStructs;
                    self.loadSchemaToComboBox(currentContainer, "#" + sourceId, predefinedStructs);
                    $("#" + sourceId).val(-1).change();
                }
            });

            $(currentContainer).find("#" + sourceId).change(function () {
                var sourceDropDown = $("#" + sourceId + " option:selected");
                var selectedNewStructNameForSource = sourceDropDown.text();
                if (selectedNewStructNameForSource != TYPE_MAPPER_COMBOBOX_DEFAULT_SELECTION && selectedNewStructNameForSource
                    != self.getSelectedSourceStruct()) {
                    if(selectedNewStructNameForSource == self.getSelectedTargetStruct()){

                        self.getSourceInfo()[TYPE_MAPPER_COMBOBOX_SOURCE_IS_ALREADY_RENDERED_IN_TARGET] = true;
                        self.getModel().removeReturnType();
                        self.setSelectedTargetStruct(TYPE_MAPPER_COMBOBOX_DEFAULT_SELECTION);
                        self.setTargetSchemaNameToComboBox('#targetStructs' + self.getModel().id, self.getSelectedTargetStruct());

                    }else{
                        self.getSourceInfo()[TYPE_MAPPER_COMBOBOX_SOURCE_IS_ALREADY_RENDERED_IN_TARGET] = false;
                    }
                    self.getSourceInfo()[TYPE_MAPPER_COMBOBOX_PREVIOUS_SELECTION] = self.getSelectedSourceStruct();
                    self.getModel().removeResourceParameter();
                    self.getModel().addResourceParameterChild(selectedNewStructNameForSource, "y");
                }else if(selectedNewStructNameForSource == TYPE_MAPPER_COMBOBOX_DEFAULT_SELECTION){
                    self.setSourceSchemaNameToComboBox('#sourceStructs' + self.getModel().id, self.getSelectedSourceStruct());
                }
            });

            $(currentContainer).find("#" + targetId).change(function () {
                var targetDropDown = $("#" + targetId + " option:selected");
                var selectedStructNameForTarget = targetDropDown.text();
                if (selectedStructNameForTarget != TYPE_MAPPER_COMBOBOX_DEFAULT_SELECTION && selectedStructNameForTarget
                    != self.getSelectedTargetStruct()) {
                    if(selectedStructNameForTarget == self.getSelectedSourceStruct()){

                        self.getTargetInfo()[TYPE_MAPPER_COMBOBOX_TARGET_IS_ALREADY_RENDERED_IN_SOURCE] = true;
                        self.getModel().removeResourceParameter();
                        self.setSelectedSourceStruct(TYPE_MAPPER_COMBOBOX_DEFAULT_SELECTION);
                        self.setSourceSchemaNameToComboBox('#sourceStructs' + self.getModel().id, self.getSelectedSourceStruct());

                    }else{
                        self.getTargetInfo()[TYPE_MAPPER_COMBOBOX_TARGET_IS_ALREADY_RENDERED_IN_SOURCE] = false;
                    }
                    self.getTargetInfo()[TYPE_MAPPER_COMBOBOX_PREVIOUS_SELECTION] = self.getSelectedTargetStruct();
                    self.getModel().removeReturnType();
                    self.getModel().addReturnTypeChild(selectedStructNameForTarget, "x");
                    self.getModel().fillReturnStatement("x");
                    self.getModel().fillVariableDefStatement(selectedStructNameForTarget, "x");
                }else if(selectedStructNameForTarget == TYPE_MAPPER_COMBOBOX_DEFAULT_SELECTION){
                    self.setTargetSchemaNameToComboBox('#targetStructs' + self.getModel().id, self.getSelectedTargetStruct());
                }
            });

            self.getModel().accept(self);
                self.getModel().on('child-added', function (child) {
                self.visit(child);
            });

            var dropActiveClass = _.get(self._viewOptions, 'cssClass.design_view_drop');


                self._container.mouseover(function (event) {
                if (self.toolPalette.dragDropManager.isOnDrag()) {
                    if (_.isEqual(self.toolPalette.dragDropManager.getActivatedDropTarget(), self)) {
                        return;
                    }
                    // register self as a drop target and validate possible types of nodes to drop - second arg is a call back to validate
                    // tool view will use self to provide feedback on impossible drop zones
                    self.toolPalette.dragDropManager.setActivatedDropTarget(self.getModel().getBlockStatement(), function (nodeBeingDragged) {
                            if (self.getTypeMapperRenderer()) {
                                if (BallerinaASTFactory.isAssignmentStatement(nodeBeingDragged)) {
                                    var children = nodeBeingDragged.getChildren();
                                    var functionInvocationExp = children[1].getChildren()[0];
                                    var functionSchema = self.getFunctionSchema(functionInvocationExp, self.getDiagramRenderingContext());
                                    if (!(functionSchema.returnType.length > 0 && functionSchema.parameters.length > 0)) {
                                        alerts.error('The function needs to have atleast one input and output parameter, to be dragged and dropped in to the type mapper!');
                                        return false;
                                    } else {
                                        return true;
                                    }
                                } else if (BallerinaASTFactory.isFunctionInvocationStatement(nodeBeingDragged)) {
                                    var functionInvocationExp = nodeBeingDragged.getChildren()[0];
                                    var functionSchema = self.getFunctionSchema(functionInvocationExp, self.getDiagramRenderingContext());
                                    if (!(functionSchema.returnType.length > 0 && functionSchema.parameters.length > 0)) {
                                        alerts.error('The function needs to have atleast one input and output parameter to be dragged and dropped in to the type mapper!');
                                        return false;
                                    } else {
                                        return true;
                                    }
                                } else {
                                    return false;
                                }
                            } else {
                                return false;
                            }
                        },
                        function (nodeBeingDragged) {
                            if (BallerinaASTFactory.isAssignmentStatement(nodeBeingDragged)) {
                                var children = nodeBeingDragged.getChildren();
                                var functionInvocationExp = children[1].getChildren()[0];
                                var functionSchema = self.getFunctionSchema(functionInvocationExp, self.getDiagramRenderingContext());
                                var leftOperand = nodeBeingDragged.getChildren()[0];
                                _.forEach(functionSchema.returnType, function (aReturnType) {
                                    var structFieldAccessExp = BallerinaASTFactory.createStructFieldAccessExpression();
                                    leftOperand.addChild(structFieldAccessExp);
                                });
                                leftOperand.setLeftOperandExpressionString('');
                                leftOperand.setLeftOperandType('');
                                var rightOperand = nodeBeingDragged.getChildren()[1];
                                _.forEach(functionSchema.parameters, function (params) {
                                    var variableRefExp = BallerinaASTFactory.createVariableReferenceExpression();
                                    variableRefExp.setVariableName('');
                                    functionInvocationExp.addChild(variableRefExp);
                                });
                                rightOperand.setRightOperandExpressionString('');
                                rightOperand.getChildren()[0].setParams('');
                                return _.findLastIndex(self.getModel().getBlockStatement().getChildren());
                            } else if (BallerinaASTFactory.isFunctionInvocationStatement(nodeBeingDragged)) {
                                var functionInvocationExp = nodeBeingDragged.getChildren()[0];
                                var functionSchema = self.getFunctionSchema(functionInvocationExp, self.getDiagramRenderingContext());
                                _.forEach(functionSchema.parameters, function (params) {
                                    var variableRefExp = BallerinaASTFactory.createVariableReferenceExpression();
                                    variableRefExp.setVariableName('');
                                    functionInvocationExp.addChild(variableRefExp);
                                });
                            }
                        });


                    // indicate drop area
                    self._container.addClass(dropActiveClass);

                    // reset ui feed back on drop target change
                    self.toolPalette.dragDropManager.once("drop-target-changed", function () {
                        self._container.removeClass(dropActiveClass);
                    });
                }
                event.stopPropagation();
            }).mouseout(function (event) {
                // reset ui feed back on hover out
                if (self.toolPalette.dragDropManager.isOnDrag()) {
                    if (_.isEqual(self.toolPalette.dragDropManager.getActivatedDropTarget(), self.getModel().getBlockStatement())) {
                        self.toolPalette.dragDropManager.clearActivatedDropTarget();
                    }
                }
                event.stopPropagation();
            });

                self._model.on('after-remove', function (child) {
                    self.stopListening();
            },self);

                self._model.on('after-remove', function (child) {
                    self.stopListening();
            },self);

        });
    }

    /**
     * return attributes list as a json object
     * @returns {Object} attributes array
     */
    getFunctionSchema(functionInvocationExp, diagramRenderingContext) {
        var schema;
        var packages = diagramRenderingContext.getPackagedScopedEnvironment().getPackages();
        var funcName = functionInvocationExp.getFunctionName();
        if (funcName.split(':').length > 1) {
            funcName = funcName.split(':')[1];
        }
        var functionPackage = _.find(packages, function (aPackage) {
            return aPackage.getFunctionDefinitionByName(funcName);
        });
        //This fix is done bcz the packages array returned from package scope environment doesn't have 
        // the current package populated correctly. The functions definitions are missing there.
        var functionDef;
        if (functionPackage) {
            functionDef = functionPackage.getFunctionDefinitionByName(funcName);
        } else {
            functionDef = diagramRenderingContext.getPackagedScopedEnvironment().getCurrentPackage().getFunctionDefinitionByName(funcName);
        }
        if (functionDef) {
            schema = {};
            schema['name'] = funcName;
            schema['returnType'] = functionDef.getReturnParams();
            schema['parameters'] = functionDef.getParameters();
        }
        return schema;
    }

    loadSchemasToComboBox(parentId, sourceComboboxId, targetComboboxId, schemaArray) {
        for (var i = 0; i < schemaArray.length; i++) {
            $(parentId).find(sourceComboboxId).append('<option value="' + schemaArray[i].getStructName() + '">'
                + schemaArray[i].getStructName() + '</option>');
            $(parentId).find(targetComboboxId).append('<option value="' + schemaArray[i].getStructName() + '">'
                + schemaArray[i].getStructName() + '</option>');
        }
    }

    loadSchemaToComboBox(parentId, comboBoxId, schemaArray) {
        for (var i = 0; i < schemaArray.length; i++) {
            $(parentId).find(comboBoxId).append('<option value="' + schemaArray[i].getStructName() + '">'
                + schemaArray[i].getStructName() + '</option>');
        }
    }

    setSourceSchemaNameToComboBox(sourceComboboxId, sourceName) {
        $(sourceComboboxId).val(sourceName).trigger('change.select2');
    }

    setTargetSchemaNameToComboBox(targetComboboxId, targetName) {
        $(targetComboboxId).val(targetName).trigger('change.select2');
    }

    /**
     * Calls the render method for a resource parameter which represents source input
     * @param resourceParameter
     */
    visitResourceParameter(resourceParameter) {
        log.debug("Visiting resource parameter");
        var self = this;
        var sourceStructName = resourceParameter.getType();
        self.setSelectedSourceStruct(sourceStructName);

        var parent = resourceParameter.getParent();
        var blockStatement = _.find(parent.getChildren(), function (child) {
            return BallerinaASTFactory.isBlockStatement(child);
        });

        var variableDefStatement = _.find(blockStatement.getChildren(), function (child) {
            return BallerinaASTFactory.isVariableDefinitionStatement(child);
        });

        if (_.isUndefined(variableDefStatement)) {
            variableDefStatement = _.find(parent.getChildren(), function (child) {
                return BallerinaASTFactory.isVariableDefinitionStatement(child);
            });

            var refTypeInitiExpression = _.find(variableDefStatement.getChildren(), function (child) {
                return BallerinaASTFactory.isReferenceTypeInitiExpression(child);
            });
            variableDefStatement.removeChild(refTypeInitiExpression);
            var rightOperandExpression = BallerinaASTFactory.createRightOperandExpression();
            rightOperandExpression.addChild(refTypeInitiExpression);
            variableDefStatement.addChild(rightOperandExpression);

            parent.removeChild(variableDefStatement);
            blockStatement.addChild(variableDefStatement, 0)
        }

        if (!self.getBlockStatementView()) {
            self.setBlockStatementView(new TypeMapperBlockStatement({
                model: null, parentView: this, sourceInfo: self.getSourceInfo(), targetInfo: self.getTargetInfo()
            }));
            self.getBlockStatementView().initializeConnections();
        }

        self.getSourceInfo()[TYPE_MAPPER_SOURCE_STRUCT_NAME] = sourceStructName;
        var predefinedStructs = self.getSourceInfo().predefinedStructs;

        _.each(predefinedStructs, function (struct) {
            if (_.isEqual(struct.getStructName(), sourceStructName)) {
                self.getSourceInfo()[TYPE_MAPPER_SOURCE_STRUCT_SCHEMA] = struct;
                return false;
            }
        });

        registerListeners(self, self.getSourceInfo().sourceStruct);

        _.each(self.getSourceInfo().sourceStruct.getChildren(), function (variableDef) {
            _.each(predefinedStructs, function (predefinedStruct) {
                if (_.isEqual(variableDef.getBType(), predefinedStruct.getStructName())) {
                    traverseChildren(self, predefinedStructs, predefinedStruct);
                }
            });

        });

        self.setSourceSchemaNameToComboBox('#sourceStructs' + self.getModel().id, sourceStructName);
        var inputStructView = new InputStructView({
            model: resourceParameter, parentView: this, onConnectInstance: self.getOnConnectInstance(),
            onDisconnectInstance: self.getOnDisconnectInstance(), sourceInfo: self.getSourceInfo()
        });

        inputStructView.render(this.diagramRenderingContext, self.getTypeMapperRenderer());
    }

    /**
     * Calls the render method for a return type which represents target input
     * @param returnType
     */
    visitReturnType(returnType) {
        log.debug("Visiting return type");
        var self = this;
        var targetStructName = returnType.getType();
        self.setSelectedTargetStruct(targetStructName);

        if (!self.getBlockStatementView()) {
            self.setBlockStatementView(new TypeMapperBlockStatement({
                model: null, parentView: this, sourceInfo: self.getSourceInfo(), targetInfo: self.getTargetInfo()
            }));
            self.getBlockStatementView().initializeConnections();
        }

        self.getTargetInfo()[TYPE_MAPPER_TARGET_STRUCT_NAME] = targetStructName;
        var predefinedStructs = self.getTargetInfo().predefinedStructs;

        _.each(predefinedStructs, function (struct) {
            if (_.isEqual(struct.getStructName(), targetStructName)) {
                self.getTargetInfo()[TYPE_MAPPER_TARGET_STRUCT_SCHEMA] = struct;
                return false;
            }
        });
        registerListeners(self, self.getTargetInfo().targetStruct);

        _.each(self.getTargetInfo().targetStruct.getChildren(), function (variableDef) {
            _.each(predefinedStructs, function (predefinedStruct) {
                if (_.isEqual(variableDef.getBType(), predefinedStruct.getStructName())) {
                    traverseChildren(self, predefinedStructs, predefinedStruct);
                }
            });

        });

        self.setTargetSchemaNameToComboBox('#targetStructs' + self.getModel().id, targetStructName);

        var outputStructView = new OutputStructView({
            model: returnType, parentView: this, onConnectInstance: self.getOnConnectInstance(),
            onDisconnectInstance: self.getOnDisconnectInstance(), targetInfo: self.getTargetInfo()
        });
        outputStructView.render(this.diagramRenderingContext, self.getTypeMapperRenderer());
    }

    /**
     * Calls the view of the Block statement
     * @param {blockStatement} blockStatement - The block statement model.
     */
    visitBlockStatement(blockStatement) {
        var self = this;
        if (!self.getBlockStatementView()) {
            self.setBlockStatementView(new TypeMapperBlockStatement({
                model: blockStatement,
                parentView: this,
                sourceInfo: self.getSourceInfo(),
                targetInfo: self.getTargetInfo()
            }));
            self.getBlockStatementView().initializeConnections();
            self.getBlockStatementView().render(this.diagramRenderingContext);
        } else {
            self.getBlockStatementView().setModel(blockStatement);
            self.getBlockStatementView().setSourceInfo(self.getSourceInfo());
            self.getBlockStatementView().setTargetInfo(self.getTargetInfo());
            self.getBlockStatementView().render(this.diagramRenderingContext);
        }

    }

    /**
     * returns the call back function to be called when a connection is drawn
     * @returns {object}
     */
    getOnConnectInstance() {
        return this._onConnectInstance;
    }

    /**
     * set the call back function for connecting a source and a target
     * @param onConnectInstance
     */
    setOnConnectInstance(onConnectInstance, options) {
        var self = this;
        if (!_.isNil(onConnectInstance)) {
            self._onConnectInstance = onConnectInstance;
        } else {
            log.error('Invalid onConnectInstance [' + onConnectInstance + '] Provided');
            throw 'Invalid onConnectInstance [' + onConnectInstance + '] Provided';
        }
    }

    /**
     * returns the call back function to be called when a connection is removed
     * @returns {object}
     */
    getOnDisconnectInstance() {
        return this._onDisconnectInstance;
    }

    /**
     * set the call back function for disconnecting a source and a target
     * @param onDisconnectInstance
     */
    setOnDisconnectInstance(onDisconnectInstance, options) {
        var self = this;
        if (!_.isNil(onDisconnectInstance)) {
            self._onDisconnectInstance = onDisconnectInstance;
        } else {
            log.error('Invalid onDisconnectInstance [' + onDisconnectInstance + '] Provided');
            throw 'Invalid onDisconnectInstance [' + onDisconnectInstance + '] Provided';
        }
    }

    /**
     * returns the type mapper renderer
     * @returns {object}
     */
    getTypeMapperRenderer() {
        return this._typeMapper;
    }

    /**
     * returns the block statement view
     * @returns {object}
     */
    getBlockStatementView() {
        return this._blockStatementView;
    }

    /**
     * sets the block statement view
     * @returns {object}
     */
    setBlockStatementView(blockStatementView) {
        this._blockStatementView = blockStatementView;
    }

    /**
     * set the selectedSourceStruct
     * @param selectedSourceStruct
     */
    setSelectedSourceStruct(selectedSourceStruct) {
        var self = this;
        if (!_.isNil(selectedSourceStruct)) {
            self._selectedSourceStruct = selectedSourceStruct;
        } else {
            log.error('Invalid selectedSourceStruct [' + selectedSourceStruct + '] Provided');
            throw 'Invalid selectedSourceStruct [' + selectedSourceStruct + '] Provided';
        }
    }

    /**
     * set the selectedTargetStruct
     * @param selectedTargetStruct
     */
    setSelectedTargetStruct(selectedTargetStruct) {
        var self = this;
        if (!_.isNil(selectedTargetStruct)) {
            self._selectedTargetStruct = selectedTargetStruct;
        } else {
            log.error('Invalid selectedTargetStruct [' + selectedTargetStruct + '] Provided');
            throw 'Invalid selectedTargetStruct [' + selectedTargetStruct + '] Provided';
        }
    }

    getModel() {
        return this._model;
    }

    getContainer() {
        return this._container;
    }

    getPackage() {
        return this._package;
    }

    getTypes() {
        return this.getDiagramRenderingContext().getEnvironment().getTypes();
    }

    getParentView() {
        return this._parentView;
    }

    getSourceInfo() {
        return this._sourceInfo;
    }

    getTargetInfo() {
        return this._targetInfo;
    }

    getSelectedSourceStruct() {
        var self = this;
        if (self._selectedSourceStruct == undefined) {
            self._selectedSourceStruct = TYPE_MAPPER_COMBOBOX_DEFAULT_SELECTION;
        }
        return this._selectedSourceStruct;
    }

    getSelectedTargetStruct() {
        var self = this;
        if (self._selectedTargetStruct == undefined) {
            self._selectedTargetStruct = TYPE_MAPPER_COMBOBOX_DEFAULT_SELECTION;
        }
        return this._selectedTargetStruct;
    }
}

/**
 * register listeners to be fired upon changes in structs
 * @param {object} view node where structs are included
 * @param {object} struct where listener is registered
 */
var registerListeners = function (view, struct) {
    view.listenTo(struct, 'tree-modified', function (child) {
        var connectionArray;
        if (child.type === 'child-removed') {
            // connectionArray = self.getTypeMapperRenderer().getTargetConnectionsByProperty(
            //     struct.getStructName(), [child.data.child.getName()],[child.data.child.getTypeName()]);
            //TODO : retrieve the exact connection and delete only that connection
            connectionArray = view.getTypeMapperRenderer().getTargetConnectionsByStruct(struct.getStructName());
            _.each(connectionArray, function (connection) {
                view.getBlockStatementView().onAttributesDisConnect(connection);
            });
        } else if (child.type === 'custom') {
            // connectionArray = self.getTypeMapperRenderer().getTargetConnectionsByProperty(
            //     struct.getStructName(), [child.data.oldValue],[child.context.getTypeName()]);
            //TODO : retrieve the exact connection and delete only that connection
            connectionArray = view.getTypeMapperRenderer().getTargetConnectionsByStruct(struct.getStructName());
            _.each(connectionArray, function (connection) {
                view.getBlockStatementView().onAttributesDisConnect(connection);
            });
        }
        var typeMapperModel = view._model;
        var containerId = view._container.selector;
        typeMapperModel.remove();
        $('#_' + containerId.substring(1, containerId.length)).remove();
        typeMapperModel.getParent().removeChild(typeMapperModel);
        typeMapperModel.getParent().addChild(typeMapperModel);
    });
};

/**
 * traverse structs to register listeners in a nested struct scenario
 * @param view
 * @param predefinedStructs
 * @param structOfVariableDef
 */
var traverseChildren = function (view, predefinedStructs, structOfVariableDef) {
    registerListeners(view, structOfVariableDef);
    _.each(structOfVariableDef.getChildren(), function (innerVariableDef) {
        _.each(predefinedStructs, function (predefinedStruct) {
            if (innerVariableDef.getBType() === predefinedStruct.getStructName()) {
                if (innerVariableDef.getChildren().length == 0) {
                    registerListeners(view, predefinedStruct);
                } else {
                    traverseChildren(view, predefinedStructs, predefinedStruct);
                }
            }
        });
    });
};

export default TypeMapperDefinitionView;
    