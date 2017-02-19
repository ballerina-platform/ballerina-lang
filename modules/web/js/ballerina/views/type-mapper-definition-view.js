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
define(['lodash', 'log', './ballerina-view', './variables-view', './type-struct-definition-view',
        'ballerina/ast/ballerina-ast-factory', './svg-canvas', 'typeMapper', './input-struct-view', './output-struct-view', './type-mapper-statement-view',
        './type-mapper-block-statement-view', 'constants', './../ast/module', 'select2'],
    function (_, log, BallerinaView, VariablesView, TypeStructDefinition, BallerinaASTFactory, SVGCanvas, TypeMapper,
              InputStructView, OutputStructView, TypeMapperStatement, TypeMapperBlockStatement, Constants, AST, select2) {
        var TypeMapperDefinitionView = function (args) {
            SVGCanvas.call(this, args);

            this._parentView = _.get(args, "parentView");
            //set panel icon for the type mapper
            this._viewOptions.panelIcon = _.get(args.viewOptions, "cssClass.type_mapper_icon");
            this._onConnectInstance = _.get(args, 'onConnectInstance', {});
            this._onDisconnectInstance = _.get(args, 'onDisconnectInstance', {});
            this._sourceInfo = _.get(args, 'sourceInfo', {});
            this._targetInfo = _.get(args, 'targetInfo', {});

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

        };

        TypeMapperDefinitionView.prototype = Object.create(SVGCanvas.prototype);
        TypeMapperDefinitionView.prototype.constructor = TypeMapperDefinitionView;

        TypeMapperDefinitionView.prototype.canVisitTypeMapperDefinition = function (typeMapperDefinition) {
            return true;
        };

        TypeMapperDefinitionView.prototype.canVisitStatement = function (statement) {
            return true;
        };

        TypeMapperDefinitionView.prototype.canVisitBlockStatementView = function (blockStatementView) {
            return true;
        };

        /**
         * Rendering the view of the Type Mapper definition.
         * @param {Object} diagramRenderingContext - the object which is carrying data required for rendering
         */
        TypeMapperDefinitionView.prototype.render = function (diagramRenderingContext) {
            this.setDiagramRenderingContext(diagramRenderingContext);
            var selectedSourceStruct = TYPE_MAPPER_COMBOBOX_DEFAULT_SELECTION;
            var selectedTargetStruct = TYPE_MAPPER_COMBOBOX_DEFAULT_SELECTION;

            // Draws the outlying body of the function.
            this.drawAccordionCanvas(this._viewOptions, this.getModel().getID(), this.getModel().type.toLowerCase(),
                this.getModel().getTypeMapperName());

            // Setting the styles for the canvas icon.
            this.getPanelIcon().addClass(_.get(this._viewOptions, "cssClass.type_mapper_icon", ""));

            var currentContainer = $('#' + this.getModel().getID());
            this._container = currentContainer;

            this._package = diagramRenderingContext.getPackagedScopedEnvironment().getCurrentPackage();

            //Get all the structs which are defined for current package
            var predefinedStructs = this._package.getStructDefinitions();
            this.getSourceInfo()["predefinedStructs"] = predefinedStructs;
            this.getTargetInfo()["predefinedStructs"] = predefinedStructs;

            var self = this;

            $(this.getTitle()).text(this.getModel().getTypeMapperName())
                .on("change paste keyup", function () {
                    self.getModel().setTypeMapperName($(this).text());
                }).on("click", function (event) {
                event.stopPropagation();
            }).keypress(function (e) {
                var enteredKey = e.which || e.charCode || e.keyCode;
                // Disabling enter key
                if (enteredKey == 13) {
                    event.stopPropagation();
                    return false;
                }
                var newTypeMapperName = $(this).val() + String.fromCharCode(enteredKey);
                try {
                    self.getModel().setTypeMapperName(newTypeMapperName);
                } catch (error) {
                    event.stopPropagation();
                    return false;
                }
            });

            var dataMapperContainerId = "data-mapper-container___" + this._model.id;
            var sourceId = 'sourceStructs' + this.getModel().id;
            var targetId = 'targetStructs' + this.getModel().id;
            var selectorContainer = $('<div class="selector">' +
                '<div class="source-view">' +
                '<span>Source :</span>' +
                '<select id="' + sourceId + '" class="type-mapper-combo">' +
                '<option value="-1">--Select--</option>' +
                '</select>' +
                '</div>' +
                '<div class="target-view">' +
                '<span>Target :</span>' +
                '<select id="' + targetId + '" class="type-mapper-combo">' +
                '<option value="-1">--Select--</option>' +
                '</select>' +
                '</div>' +
                '</div>');

            var dataMapperContainer = $('<div id="' + dataMapperContainerId + '" class="data-mapper-container"></div>');

            currentContainer.find('svg').parent().append(selectorContainer).append(dataMapperContainer);
            currentContainer.find('svg').remove();

            this.loadSchemasToComboBox(currentContainer, "#" + sourceId, "#" + targetId, predefinedStructs);

            $(".type-mapper-combo").select2();
            $("#" + targetId).on("select2:open", function () {
                var predefinedStructs = self._package.getStructDefinitions();
                if (predefinedStructs.length > 0) {
                    $("#" + targetId).empty().append('<option value="-1">--Select--</option>');
                    self.getTargetInfo()["predefinedStructs"] = predefinedStructs;
                    self.loadSchemaToComboBox(currentContainer, "#" + targetId, predefinedStructs);
                }
            });

            $("#" + sourceId).on("select2:open", function () {
                var predefinedStructs = self._package.getStructDefinitions();
                if (predefinedStructs.length > 0) {
                    $("#" + sourceId).empty().append('<option value="-1">--Select--</option>');
                    self.getSourceInfo()["predefinedStructs"] = predefinedStructs;
                    self.loadSchemaToComboBox(currentContainer, "#" + sourceId, predefinedStructs);
                }
            });

            $(currentContainer).find("#" + sourceId).change(function () {
                var sourceDropDown = $("#" + sourceId + " option:selected");
                var selectedNewStructNameForSource = sourceDropDown.text();
                self.getSourceInfo()[TYPE_MAPPER_COMBOBOX_PREVIOUS_SELECTION] = selectedSourceStruct;

                if (selectedNewStructNameForSource != selectedTargetStruct) {
                    selectedSourceStruct = selectedNewStructNameForSource;
                    if (selectedNewStructNameForSource != "--Select--") {
                        self.getModel().removeResourceParameter();
                        self.getModel().addResourceParameterChild(selectedNewStructNameForSource, "y");
                    }
                } else {
                    self.setSourceSchemaNameToComboBox('#sourceStructs' + self.getModel().id, selectedSourceStruct);
                }
            });

            $(currentContainer).find("#" + targetId).change(function () {
                var targetDropDown = $("#" + targetId + " option:selected");
                var selectedStructNameForTarget = targetDropDown.text();
                self.getTargetInfo()[TYPE_MAPPER_COMBOBOX_PREVIOUS_SELECTION] = selectedTargetStruct;

                if (selectedStructNameForTarget != selectedSourceStruct) {
                    selectedTargetStruct = selectedStructNameForTarget;
                    if (selectedStructNameForTarget != "--Select--") {
                        self.getModel().removeReturnType();
                        self.getModel().addReturnTypeChild(selectedStructNameForTarget, "x");
                        self.getModel().fillReturnStatement("x");
                        self.getModel().fillVariableDefStatement(selectedStructNameForTarget, "x");
                    }
                } else {
                    self.setTargetSchemaNameToComboBox('#targetStructs' + self.getModel().id, selectedTargetStruct);
                }
            });
            this.getModel().accept(this);

            this.getModel().on('child-added', function (child) {
                self.visit(child);
            });

            var dropActiveClass = _.get(this._viewOptions, 'cssClass.design_view_drop');

            this._container.mouseover(function (event) {
                if (self.toolPalette.dragDropManager.isOnDrag()) {
                    if (_.isEqual(self.toolPalette.dragDropManager.getActivatedDropTarget(), self)) {
                        return;
                    }
                    // register this as a drop target and validate possible types of nodes to drop - second arg is a call back to validate
                    // tool view will use this to provide feedback on impossible drop zones
                    self.toolPalette.dragDropManager.setActivatedDropTarget(self.getModel().getBlockStatement(), function (nodeBeingDragged) {
                            if (self.getTypeMapperRenderer()) {
                                if (BallerinaASTFactory.isAssignmentStatement(nodeBeingDragged)) {
                                    var children = nodeBeingDragged.getChildren();
                                    var functionInvocationExp = children[1].getChildren()[0];
                                    var functionSchema = self.getFunctionSchema(functionInvocationExp, self.getDiagramRenderingContext());
                                    return functionSchema.returnType.length > 0 && functionSchema.parameters.length > 0
                                } else if (BallerinaASTFactory.isFunctionInvocationStatement(nodeBeingDragged)) {
                                    var functionInvocationExp = nodeBeingDragged.getChildren()[0];
                                    var functionSchema = self.getFunctionSchema(functionInvocationExp, self.getDiagramRenderingContext());
                                    return functionSchema.returnType.length > 0 && functionSchema.parameters.length > 0
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
                                    variableRefExp.setVariableReferenceName('');
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
                                    variableRefExp.setVariableReferenceName('');
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
        };


        /**
         * return attributes list as a json object
         * @returns {Object} attributes array
         */
        TypeMapperDefinitionView.prototype.getFunctionSchema = function (functionInvocationExp, diagramRenderingContext) {
            var schema;
            var packages = diagramRenderingContext.getPackagedScopedEnvironment().getPackages();
            var funcName = functionInvocationExp.getFunctionName();
            if (funcName.split(':').length > 1) {
                funcName = funcName.split(':')[1];
            }
            var functionPackage = _.find(packages, function (aPackage) {
                return aPackage.getFunctionDefinitionByName(funcName);
            });
            var functionDef = functionPackage.getFunctionDefinitionByName(funcName);
            if (functionDef) {
                schema = {};
                schema['name'] = funcName;
                schema['returnType'] = functionDef.getReturnParams();
                schema['parameters'] = functionDef.getParameters();
            }
            return schema;
        };

        TypeMapperDefinitionView.prototype.loadSchemasToComboBox = function (parentId, sourceComboboxId,
                                                                             targetComboboxId, schemaArray) {
            for (var i = 0; i < schemaArray.length; i++) {
                $(parentId).find(sourceComboboxId).append('<option value="' + schemaArray[i].getStructName() + '">'
                    + schemaArray[i].getStructName() + '</option>');
                $(parentId).find(targetComboboxId).append('<option value="' + schemaArray[i].getStructName() + '">'
                    + schemaArray[i].getStructName() + '</option>');
            }
        };

        TypeMapperDefinitionView.prototype.loadSchemaToComboBox = function (parentId, comboBoxId, schemaArray) {
            for (var i = 0; i < schemaArray.length; i++) {
                $(parentId).find(comboBoxId).append('<option value="' + schemaArray[i].getStructName() + '">'
                    + schemaArray[i].getStructName() + '</option>');
            }
        };

        TypeMapperDefinitionView.prototype.setSourceSchemaNameToComboBox = function (sourceComboboxId, sourceName) {
            $(sourceComboboxId).val(sourceName).trigger('change.select2');
        };

        TypeMapperDefinitionView.prototype.setTargetSchemaNameToComboBox = function (targetComboboxId, targetName) {
            $(targetComboboxId).val(targetName).trigger('change.select2');
        };

        /**
         * Calls the render method for a resource parameter which represents source input
         * @param resourceParameter
         */
        TypeMapperDefinitionView.prototype.visitResourceParameter = function (resourceParameter) {
            log.debug("Visiting resource parameter");
            var self = this;
            var sourceStructName = resourceParameter.getType();

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

            self.getSourceInfo()["sourceStructName"] = sourceStructName;
            var predefinedStructs = self.getSourceInfo().predefinedStructs;

            _.each(predefinedStructs, function (struct) {
                if (struct.getStructName() == sourceStructName) {
                    self.getSourceInfo()["sourceStruct"] = struct;
                    return false;
                }
            });

            self.setSourceSchemaNameToComboBox('#sourceStructs' + self.getModel().id, sourceStructName);
            var inputStructView = new InputStructView({
                model: resourceParameter, parentView: this, onConnectInstance: self.getOnConnectInstance(),
                onDisconnectInstance: self.getOnDisconnectInstance(), sourceInfo: self.getSourceInfo()
            });

            inputStructView.render(this.diagramRenderingContext, self.getTypeMapperRenderer());
        };

        /**
         * Calls the render method for a return type which represents target input
         * @param returnType
         */
        TypeMapperDefinitionView.prototype.visitReturnType = function (returnType) {
            log.debug("Visiting return type");
            var self = this;
            var targetStructName = returnType.getType();

            if (!self.getBlockStatementView()) {
                self.setBlockStatementView(new TypeMapperBlockStatement({
                    model: null, parentView: this, sourceInfo: self.getSourceInfo(), targetInfo: self.getTargetInfo()
                }));
                self.getBlockStatementView().initializeConnections();
            }

            self.getTargetInfo()["targetStructName"] = targetStructName;
            var predefinedStructs = self.getTargetInfo().predefinedStructs;

            _.each(predefinedStructs, function (struct) {
                if (struct.getStructName() == targetStructName) {
                    self.getTargetInfo()["targetStruct"] = struct;
                    return false;
                }
            });

            self.setTargetSchemaNameToComboBox('#targetStructs' + self.getModel().id, targetStructName);

            var outputStructView = new OutputStructView({
                model: returnType, parentView: this, onConnectInstance: self.getOnConnectInstance(),
                onDisconnectInstance: self.getOnDisconnectInstance(), targetInfo: self.getTargetInfo()
            });
            outputStructView.render(this.diagramRenderingContext, self.getTypeMapperRenderer());
        };

        /**
         * Calls the view of the Block statement
         * @param {blockStatement} blockStatement - The block statement model.
         */
        TypeMapperDefinitionView.prototype.visitBlockStatement = function (blockStatement) {
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

        };

        /**
         * returns the call back function to be called when a connection is drawn
         * @returns {object}
         */
        TypeMapperDefinitionView.prototype.getOnConnectInstance = function () {
            return this._onConnectInstance;
        };

        /**
         * set the call back function for connecting a source and a target
         * @param onConnectInstance
         */
        TypeMapperDefinitionView.prototype.setOnConnectInstance = function (onConnectInstance, options) {
            var self = this;
            if (!_.isNil(onConnectInstance)) {
                self._onConnectInstance = onConnectInstance;
            } else {
                log.error('Invalid onConnectInstance [' + onConnectInstance + '] Provided');
                throw 'Invalid onConnectInstance [' + onConnectInstance + '] Provided';
            }
        };

        /**
         * returns the call back function to be called when a connection is removed
         * @returns {object}
         */
        TypeMapperDefinitionView.prototype.getOnDisconnectInstance = function () {
            return this._onDisconnectInstance;
        };

        /**
         * set the call back function for disconnecting a source and a target
         * @param onDisconnectInstance
         */
        TypeMapperDefinitionView.prototype.setOnDisconnectInstance = function (onDisconnectInstance, options) {
            var self = this;
            if (!_.isNil(onDisconnectInstance)) {
                self._onDisconnectInstance = onDisconnectInstance;
            } else {
                log.error('Invalid onDisconnectInstance [' + onDisconnectInstance + '] Provided');
                throw 'Invalid onDisconnectInstance [' + onDisconnectInstance + '] Provided';
            }
        };

        /**
         * returns the type mapper renderer
         * @returns {object}
         */
        TypeMapperDefinitionView.prototype.getTypeMapperRenderer = function () {
            return this._typeMapper;
        };

        /**
         * returns the block statement view
         * @returns {object}
         */
        TypeMapperDefinitionView.prototype.getBlockStatementView = function () {
            return this._blockStatementView;
        };

        /**
         * sets the block statement view
         * @returns {object}
         */
        TypeMapperDefinitionView.prototype.setBlockStatementView = function (blockStatementView) {
            this._blockStatementView = blockStatementView;
        };

        TypeMapperDefinitionView.prototype.getModel = function () {
            return this._model;
        };

        TypeMapperDefinitionView.prototype.getContainer = function () {
            return this._container;
        };

        TypeMapperDefinitionView.prototype.getPackage = function () {
            return this._package;
        };

        TypeMapperDefinitionView.prototype.getTypes = function () {
            return this.getDiagramRenderingContext().getEnvironment().getTypes();
        };

        TypeMapperDefinitionView.prototype.getParentView = function () {
            return this._parentView;
        };

        TypeMapperDefinitionView.prototype.getSourceInfo = function () {
            return this._sourceInfo;
        };

        TypeMapperDefinitionView.prototype.getTargetInfo = function () {
            return this._targetInfo;
        };

        return TypeMapperDefinitionView;
    });