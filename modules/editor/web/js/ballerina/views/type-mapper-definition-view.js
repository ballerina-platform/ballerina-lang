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
define(['lodash', 'log', 'd3', './ballerina-view', './variables-view', './type-struct-definition-view', 'ballerina/ast/ballerina-ast-factory', './canvas',
    './point', 'typeMapper'], function (_, log, d3, BallerinaView, VariablesView, TypeStructDefinition, BallerinaASTFactory, Canvas, Point, TypeMapper) {
    var TypeMapperDefinitionView = function (args) {
        Canvas.call(this, args);

        this._parentView = _.get(args, "parentView");
        this._viewOptions.offsetTop = _.get(args, "viewOptionsOffsetTop", 50);
        this._viewOptions.topBottomTotalGap = _.get(args, "viewOptionsTopBottomTotalGap", 100);
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
        this._typeMapper = new TypeMapper(this.onAttributesConnect, this.onAttributesDisConnect, this);

    };

    TypeMapperDefinitionView.prototype = Object.create(Canvas.prototype);
    TypeMapperDefinitionView.prototype.constructor = Canvas;

    TypeMapperDefinitionView.prototype.canVisitTypeMapperDefinition = function (typeMapperDefinition) {
        return true;
    };

    /**
     * Rendering the view of the Type Mapper definition.
     * @param {Object} diagramRenderingContext - the object which is carrying data required for rendering
     */
    TypeMapperDefinitionView.prototype.render = function (diagramRenderingContext) {
        this.diagramRenderingContext = diagramRenderingContext;
        this.drawAccordionCanvas(this._container, this._viewOptions, this._model.id, this._model.type.toLowerCase(), this._model._typeMapperName);
        var divId = this._model.id;
        var currentContainer = $('#' + divId);
        var self = this;
        this._assignedModel = this;

        console.log("AAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        //todo get current package name dynamically
        this._package = diagramRenderingContext.getPackagedScopedEnvironment().getCurrentPackage();
        var predefinedStructs = this._package.getStructDefinitions();
        console.log(predefinedStructs);

        var dataMapperContainerId = "data-mapper-container-" + this._model.id;
        var sourceId = 'sourceStructs' + this._model.id;
        var targetId = 'targetStructs' + this._model.id;
        var selectorContainer = $('<div class="selector"><div class="source-view"><span>Source :</span><select id="' + sourceId + '">' +
            '<option value="-1">--Select--</option></select></div><div class="target-view"><span>Target :</span><select id="' + targetId + '"><option value="-1">--Select--</option></select></div></div>');
        var dataMapperContainer = $('<div id="' + dataMapperContainerId + '" class="data-mapper-container"></div>');

        currentContainer.find('svg').parent().append(selectorContainer);
        currentContainer.find('svg').parent().append(dataMapperContainer);
        currentContainer.find('svg').remove();

        this.loadSchemasToComboBox(currentContainer, "#" + sourceId, predefinedStructs);
        this.loadSchemasToComboBox(currentContainer, "#" + targetId, predefinedStructs);

        $(currentContainer).find("#" + sourceId).change(function () {
            var selectedArrayIndex = $("#" + sourceId + " option:selected").val();
            var selectedStructNameForSource = $("#" + sourceId + " option:selected").text();
            // self._model.removeTypeStructDefinition("SOURCE");
            var schema = predefinedStructs[selectedArrayIndex];

            if (selectedStructNameForSource != self._model.getSelectedStructNameForTarget()) {
                if (!self._model.getSelectedStructNameForSource()) {
                    self._model.setSelectedStructNameForSource(selectedStructNameForSource);
                }

                var leftTypeStructDef = BallerinaASTFactory.createTypeStructDefinition();
                leftTypeStructDef.setTypeStructName(schema.getStructName());
                leftTypeStructDef.setSelectedStructName(self._model.getSelectedStructNameForSource());
                leftTypeStructDef.setIdentifier("y");
                leftTypeStructDef.setSchema(schema);
                leftTypeStructDef.setCategory("SOURCE");
                //leftTypeStructDef.setDataMapperInstance(self._typeMapper);
                leftTypeStructDef.setOnConnectInstance(self.onAttributesConnect);
                leftTypeStructDef.setOnDisconnectInstance(self.onAttributesDisConnect);
                self._model.addChild(leftTypeStructDef);
                self._model.setSelectedStructNameForSource(selectedStructNameForSource);
            } else {
                var t = self._model.getSelectedStructIndex(predefinedStructs, self._model.getSelectedStructNameForSource());
                $("#" + sourceId).val(self._model.getSelectedStructIndex(predefinedStructs,
                    self._model.getSelectedStructNameForSource()));
            }
        });

        $(currentContainer).find("#" + targetId).change(function () {

            var selectedArrayIndex = $("#" + targetId + " option:selected").val();
            var selectedStructNameForTarget = $("#" + targetId + " option:selected").text();
            // self._model.removeTypeStructDefinition("TARGET");
            var schema = predefinedStructs[selectedArrayIndex];

            if (self._model.getSelectedStructNameForSource() != selectedStructNameForTarget) {
                if (!self._model.getSelectedStructNameForTarget()) {
                    self._model.setSelectedStructNameForTarget(selectedStructNameForTarget);
                }

                var rightTypeStructDef = BallerinaASTFactory.createTypeStructDefinition();
                rightTypeStructDef.setTypeStructName(schema.getStructName());
                rightTypeStructDef.setSelectedStructName(self._model.getSelectedStructNameForTarget());
                rightTypeStructDef.setIdentifier("x");
                rightTypeStructDef.setSchema(schema);
                rightTypeStructDef.setCategory("TARGET");
                //rightTypeStructDef.setDataMapperInstance(self._typeMapper);
                rightTypeStructDef.setOnConnectInstance(self.onAttributesConnect);
                rightTypeStructDef.setOnDisconnectInstance(self.onAttributesDisConnect);
                self._model.addChild(rightTypeStructDef);
                self._model.setSelectedStructNameForTarget(selectedStructNameForTarget);

                var newVariableDeclaration = BallerinaASTFactory.createVariableDeclaration();
                newVariableDeclaration.setType(schema.getStructName());
                newVariableDeclaration.setIdentifier("x");
                self._model.addChild(newVariableDeclaration);

                var newReturnStatement = BallerinaASTFactory.createReturnStatement();
                newReturnStatement.setReturnExpression("x");
                self._model.addChild(newReturnStatement);
            } else {
                var t = self._model.getSelectedStructIndex(predefinedStructs, self._model.getSelectedStructNameForTarget());
                $("#" + targetId).val(self._model.getSelectedStructIndex(predefinedStructs,
                    self._model.getSelectedStructNameForTarget()));
            }
        });

        this._container = currentContainer;
        this.getBoundingBox().fromTopLeft(new Point(0, 0), currentContainer.width(), currentContainer.height());
        this.getModel().accept(this);

        $("#title-" + this._model.id).addClass("type-mapper-title-text").text(this._model.getTypeMapperName())
            .on("change paste keyup", function (e) {
                self._model.setTypeMapperName($(this).text());
            }).on("click", function (event) {
            event.stopPropagation();
        }).on("keydown", function (e) {
            // Check whether the Enter key has been pressed. If so return false. Won't type the character
            if (e.keyCode === 13) {
                return false;
            }
        });

        this._model.on('child-added', function (child) {
            self.visit(child);
            self._model.trigger("child-visited", child);
        });

        this.setServiceContainerWidth(this._container.width());
    };

    TypeMapperDefinitionView.prototype.getChildContainer = function () {
        return this._rootGroup;
    };

    TypeMapperDefinitionView.prototype.loadSchemasToComboBox = function (parentId, selectId, schemaArray) {
        for (var i = 0; i < schemaArray.length; i++) {
            $(parentId).find(selectId).append('<option value="' + i + '">' + schemaArray[i].getStructName() + '</option>');

        }
    };

    /**
     * Calls the render method for a type struct definition.
     * @param {typeStructDefinition} typeStructDefinition - The type struct definition model.
     */
    TypeMapperDefinitionView.prototype.visitTypeStructDefinition = function (typeStructDefinition) {
        log.debug("Visiting type struct definition");
        var self = this;
        var typeStructContainer = this.getChildContainer();
        var typeStructDefinitionView = new TypeStructDefinition({
            model: typeStructDefinition, container: typeStructContainer,
            toolPalette: this.toolPalette, parentView: this
        });
        typeStructDefinitionView.render(this.diagramRenderingContext);
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
            rightOperandExpression = "(" + connection.complexMapperName + ":" + connection.targetType + ")" + rightOperandExpression;
        }
        rightOp.setRightOperandExpressionString(rightOperandExpression);
        assignmentStmt.addChild(leftOp);
        assignmentStmt.addChild(rightOp);

        var index = _.findLastIndex(connection.targetReference.getParent().getChildren(), function (child) {
            return connection.targetReference.getParent().BallerinaASTFactory.isVariableDeclaration(child);
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

    return TypeMapperDefinitionView;
});