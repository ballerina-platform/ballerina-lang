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
define(['lodash', 'log','./ballerina-view', './variables-view', './type-struct-definition-view',
        'ballerina/ast/ballerina-ast-factory', './svg-canvas','typeMapper'],
    function (_, log,BallerinaView, VariablesView, TypeStructDefinition, BallerinaASTFactory, SVGCanvas,
              TypeMapper) {
        var TypeMapperDefinitionView = function (args) {
            SVGCanvas.call(this, args);

            this._parentView = _.get(args, "parentView");
            //set panel icon for the type mapper
            this._viewOptions.panelIcon = _.get(args.viewOptions, "cssClass.type_mapper_icon");
            //set initial height for the type mapper container svg
            this._totalHeight = 30;
            if (_.isNil(this._model) || !(BallerinaASTFactory.isTypeMapperDefinition(this._model))) {
                log.error("Type Mapper definition is undefined or is of different type." + this._model);
                throw "Type Mapper definition is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for Type Mapper definition is undefined." + this._container);
                throw "Container for Type Mapper definition is undefined." + this._container;
            }
            this._typeMapper = undefined;

        };

        TypeMapperDefinitionView.prototype = Object.create(SVGCanvas.prototype);
        TypeMapperDefinitionView.prototype.constructor = TypeMapperDefinitionView;

        TypeMapperDefinitionView.prototype.canVisitTypeMapperDefinition = function (typeMapperDefinition) {
            return true;
        };

        /**
         * Rendering the view of the Type Mapper definition.
         * @param {Object} diagramRenderingContext - the object which is carrying data required for rendering
         */
        TypeMapperDefinitionView.prototype.render = function (diagramRenderingContext) {
            this.setDiagramRenderingContext(diagramRenderingContext);

            // Draws the outlying body of the function.
            this.drawAccordionCanvas(this._viewOptions, this.getModel().getID(), this.getModel().type.toLowerCase(),
                this.getModel().getTypeMapperName());
            
            this._package = diagramRenderingContext.getPackagedScopedEnvironment().getCurrentPackage();

            // Setting the styles for the canvas icon.
            this.getPanelIcon().addClass(_.get(this._viewOptions, "cssClass.type_mapper_icon", ""));

            var currentContainer = $('#' + this.getModel().getID());
            this._container = currentContainer;
            // todo verify this.getBoundingBox().fromTopLeft(new Point(0, 0), currentContainer.width(), currentContainer.height());
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

            //Get all the structs which are defined for current package
            var predefinedStructs = diagramRenderingContext.getPackagedScopedEnvironment().getCurrentPackage().getStructDefinitions();

            var dataMapperContainerId = "data-mapper-container___" + this._model.id;
            var sourceId = 'sourceStructs' + this._model.id;
            var targetId = 'targetStructs' + this._model.id;
            var selectorContainer = $('<div class="selector">' +
                                            '<div class="source-view">' +
                                                '<span>Source :</span>' +
                                                    '<select id="' + sourceId + '">' +
                                                        '<option value="-1">--Select--</option>' +
                                                    '</select>' +
                                            '</div>' +
                                            '<div class="target-view">' +
                                                '<span>Target :</span>' +
                                                    '<select id="' + targetId + '">' +
                                                        '<option value="-1">--Select--</option>' +
                                                    '</select>' +
                                            '</div>' +
                                        '</div>');

            var dataMapperContainer = $('<div id="' + dataMapperContainerId + '" class="data-mapper-container"></div>');

            currentContainer.find('svg').parent().append(selectorContainer).append(dataMapperContainer);
            currentContainer.find('svg').remove();

            this.loadSchemasToComboBox(currentContainer, "#" + sourceId,"#"+targetId, predefinedStructs);

            $(currentContainer).find("#" + sourceId).change(function () {
                var sourceDropDown = $("#" + sourceId + " option:selected");
                var selectedArrayIndex = sourceDropDown.val();
                var selectedStructNameForSource = sourceDropDown.text();
                self.getModel().removeTypeStructDefinition("SOURCE");
                var schema = predefinedStructs[selectedArrayIndex];

                if (selectedStructNameForSource != self.getModel().getSelectedStructNameForTarget()) {
                    if (!self.getModel().getSelectedStructNameForSource()) {
                        self.getModel().setSelectedStructNameForSource(selectedStructNameForSource);
                    }

                    var leftTypeStructDef = BallerinaASTFactory.createTypeStructDefinition();
                    leftTypeStructDef.setTypeStructName(schema.getStructName());
                    leftTypeStructDef.setSelectedStructName(self.getModel().getSelectedStructNameForSource());
                    leftTypeStructDef.setIdentifier("y");
                    leftTypeStructDef.setSchema(schema);
                    leftTypeStructDef.setCategory("SOURCE");
                    leftTypeStructDef.setOnConnectInstance(self.onAttributesConnect);
                    leftTypeStructDef.setOnDisconnectInstance(self.onAttributesDisConnect);
                    self.getModel().addChild(leftTypeStructDef);
                    self.getModel().setSelectedStructNameForSource(selectedStructNameForSource);
                } else {
                    //todo set the selectedvalue directly ro combobox using name without iterating
                    $("#" + sourceId).val(self.getModel().getSelectedStructIndex(predefinedStructs,
                        self.getModel().getSelectedStructNameForSource()));
                }
            });

            $(currentContainer).find("#" + targetId).change(function () {
                var targetDropDown = $("#" + targetId + " option:selected");
                var selectedArrayIndex = targetDropDown.val();
                var selectedStructNameForTarget = targetDropDown.text();
                self.getModel().removeTypeStructDefinition("TARGET");
                var schema = predefinedStructs[selectedArrayIndex];

                if (self.getModel().getSelectedStructNameForSource() != selectedStructNameForTarget) {
                    if (!self.getModel().getSelectedStructNameForTarget()) {
                        self.getModel().setSelectedStructNameForTarget(selectedStructNameForTarget);
                    }

                    var rightTypeStructDef = BallerinaASTFactory.createTypeStructDefinition();
                    rightTypeStructDef.setTypeStructName(schema.getStructName());
                    rightTypeStructDef.setSelectedStructName(self.getModel().getSelectedStructNameForTarget());
                    rightTypeStructDef.setIdentifier("x");
                    rightTypeStructDef.setSchema(schema);
                    rightTypeStructDef.setCategory("TARGET");
                    rightTypeStructDef.setOnConnectInstance(self.onAttributesConnect);
                    rightTypeStructDef.setOnDisconnectInstance(self.onAttributesDisConnect);
                    self.getModel().addChild(rightTypeStructDef);
                    self.getModel().setSelectedStructNameForTarget(selectedStructNameForTarget);

                    var newVariableDeclaration = BallerinaASTFactory.createVariableDeclaration();
                    newVariableDeclaration.setType(schema.getStructName());
                    newVariableDeclaration.setIdentifier("x");
                    self.getModel().addChild(newVariableDeclaration);

                    var newReturnStatement = BallerinaASTFactory.createReturnStatement();
                    newReturnStatement.setReturnExpression("x");
                    self.getModel().addChild(newReturnStatement);
                } else {
                    $("#" + targetId).val(self.getModel().getSelectedStructIndex(predefinedStructs,
                        self.getModel().getSelectedStructNameForTarget()));
                }
            });
            this.getModel().accept(this);

            this.getModel().on('child-added', function (child) {
                self.visit(child);
                self.getModel().trigger("child-visited", child);
            });
        };

        TypeMapperDefinitionView.prototype.loadSchemasToComboBox = function (parentId, selectSourceId,selectTargetId,schemaArray) {
            for (var i = 0; i < schemaArray.length; i++) {
                $(parentId).find(selectSourceId).append('<option value="' + i + '">' + schemaArray[i].getStructName() + '</option>');
                $(parentId).find(selectTargetId).append('<option value="' + i + '">' + schemaArray[i].getStructName() + '</option>');

            }
        };

        /**
         * Calls the render method for a type struct definition.
         * @param typeStructDefinition
         */
        TypeMapperDefinitionView.prototype.visitTypeStructDefinition = function (typeStructDefinition) {
            log.debug("Visiting type struct definition");
            var self = this;
            var typeStructDefinitionView = new TypeStructDefinition({
                model: typeStructDefinition, parentView: this
            });
            typeStructDefinitionView.render(this.diagramRenderingContext, this._typeMapper);
        };

        /**
         * Receives attributes connected
         * @param connection object
         */
        TypeMapperDefinitionView.prototype.onAttributesConnect = function (connection) {
            var assignmentStmt = BallerinaASTFactory.createAssignmentStatement();
            var leftOp = BallerinaASTFactory.createLeftOperandExpression();
            var leftOperandExpression = "x." + connection.targetProperty;
            leftOp.setLeftOperandExpressionString(leftOperandExpression);
            var rightOp = BallerinaASTFactory.createRightOperandExpression();
            var rightOperandExpression = "y." + connection.sourceProperty;
            if (connection.isComplexMapping) {
                rightOperandExpression = "(" + connection.complexMapperName + ":" + connection.targetType + ")" + 
                    rightOperandExpression;
            }
            rightOp.setRightOperandExpressionString(rightOperandExpression);
            assignmentStmt.addChild(leftOp);
            assignmentStmt.addChild(rightOp);

            var index = _.findLastIndex(connection.targetReference.getParent().getChildren(), function (child) {
                return BallerinaASTFactory.isVariableDeclaration(child);
            });
            connection.targetReference.getParent().addChild(assignmentStmt, index + 1);

        };

        /**
         * Receives the attributes disconnected
         * @param connection object
         */
        TypeMapperDefinitionView.prototype.onAttributesDisConnect = function (connection) {

            connection.targetReference.getParent().removeAssignmentDefinition(connection.sourceProperty,
                connection.targetProperty);
        };

        TypeMapperDefinitionView.prototype.getModel = function () {
            return this._model;
        };

        TypeMapperDefinitionView.prototype.getContainer = function () {
            return this._container;
        };

        TypeMapperDefinitionView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        TypeMapperDefinitionView.prototype.getChildContainer = function () {
            return this._childContainer;
        };

        TypeMapperDefinitionView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };
        
        TypeMapperDefinitionView.prototype.getPackage = function () {
            return this._package;
        };
        
        TypeMapperDefinitionView.prototype.getTypes = function () {
           return this.getDiagramRenderingContext().getEnvironment().getTypes();
        };

        return TypeMapperDefinitionView;
    });