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
            './point','typeMapper'], function (_, log, d3, BallerinaView, VariablesView, TypeStructDefinition, BallerinaASTFactory, Canvas, Point,TypeMapper) {
    var TypeConverterDefinitionView = function (args) {
        Canvas.call(this, args);

        this._parentView = _.get(args, "parentView");
        this._viewOptions.offsetTop = _.get(args, "viewOptionsOffsetTop", 50);
        this._viewOptions.topBottomTotalGap = _.get(args, "viewOptionsTopBottomTotalGap", 100);
        //set panel icon for the type converter
        this._viewOptions.panelIcon = _.get(args.viewOptions, "cssClass.type_converter_icon");
        //set initial height for the type converter container svg
        this._totalHeight = 30;

        if (_.isNil(this._model) || !(BallerinaASTFactory.isTypeConverterDefinition(this._model))) {
            log.error("Type Converter definition is undefined or is of different type." + this._model);
            throw "Type Converter definition is undefined or is of different type." + this._model;
        }

        if (_.isNil(this._container)) {
            log.error("Container for Type Converter definition is undefined." + this._container);
            throw "Container for Type Converter definition is undefined." + this._container;
        }
        this.init();
        this._typeMapper = new TypeMapper(this.onAttributesConnect,this.onAttributesDisConnect);
        console.log("PPPPPPPPPPPPPPPPPPPPPPPPPPPPPP");
        console.log(this._typeMapper);
    };

    TypeConverterDefinitionView.prototype = Object.create(Canvas.prototype);
    TypeConverterDefinitionView.prototype.constructor = Canvas;

    TypeConverterDefinitionView.prototype.init = function(){
        //Registering event listeners
        this.listenTo(this._model, 'child-removed', this.childViewRemovedCallback);
    };

    TypeConverterDefinitionView.prototype.canVisitTypeConverterDefinition = function (typeConverterDefinition) {
        return true;
    };

    /**
     * Rendering the view of the Type Converter definition.
     * @param {Object} diagramRenderingContext - the object which is carrying data required for rendering
     */
    TypeConverterDefinitionView.prototype.render = function (diagramRenderingContext) {
        this.diagramRenderingContext = diagramRenderingContext;
        this.drawAccordionCanvas(this._container, this._viewOptions, this._model.id, this._model.type.toLowerCase(), this._model._typeConverterName);
        var divId = this._model.id;
        var currentContainer = $('#' + divId);
        var self = this;
        this._assignedModel = this;

        var selectorContainer = $('<div class="selector"><div class="source-view"><span>Source :</span><select id="sourceStructs">' +
            '</select></div><div class="target-view"><span>Target :</span><select id="targetStructs"></select></div></div>');
        var dataMapperContainer = $('<div id="data-mapper-container"></div>');

        currentContainer.find('svg').parent().append(selectorContainer);
        currentContainer.find('svg').parent().append(dataMapperContainer);
        currentContainer.find('svg').remove();

        this.loadSchemas(currentContainer,"#sourceStructs");
        this.loadSchemas(currentContainer,"#targetStructs");

        $(currentContainer).find("#sourceStructs").change(function() {

            var employee = BallerinaASTFactory.createStructDefinition();
            employee.setStructName("Employee");
            var v1 = BallerinaASTFactory.createVariableDeclaration();
            v1.setType("string");
            v1.setIdentifier("empName");
            var v2 = BallerinaASTFactory.createVariableDeclaration();
            v2.setType("string");
            v2.setIdentifier("empSurName");

            employee.addChild(v1);
            employee.addChild(v2);

            var leftTypeStructDef = BallerinaASTFactory.createTypeStructDefinition();
            //todo set person value dynamically
            leftTypeStructDef.setTypeStructName("Person");
            leftTypeStructDef.setIdentifier("p");
            leftTypeStructDef.setSchema(employee);
            leftTypeStructDef.setCategory("SOURCE");
            //leftTypeStructDef.setDataMapperInstance(self._typeMapper);
            leftTypeStructDef.setOnConnectInstance(self.onAttributesConnect);
            leftTypeStructDef.setOnDisconnectInstance(self.onAttributesDisConnect);
            self._model.addChild(leftTypeStructDef);

        });

//        var assignmentStmt = BallerinaASTFactory.createAssignmentStatement();
//        var leftOp = BallerinaASTFactory.createLeftOperandExpression();
//        var rightOp = BallerinaASTFactory.createRightOperandExpression();
//        assignmentStmt.addChild(leftOp);
//        assignmentStmt.addChild(rightOp);

        $(currentContainer).find("#targetStructs").change(function() {

            var person = BallerinaASTFactory.createStructDefinition();
            person.setStructName("Person");
            var v1 = BallerinaASTFactory.createVariableDeclaration();
            v1.setType("string");
            v1.setIdentifier("name");
            var v2 = BallerinaASTFactory.createVariableDeclaration();
            v2.setType("string");
            v2.setIdentifier("surname");

            person.addChild(v1);
            person.addChild(v2);

            var rightTypeStructDef = BallerinaASTFactory.createTypeStructDefinition();
            rightTypeStructDef.setTypeStructName("Employee");
            rightTypeStructDef.setIdentifier("e");
            rightTypeStructDef.setSchema(person);
            rightTypeStructDef.setCategory("TARGET");
            //rightTypeStructDef.setDataMapperInstance(self._typeMapper);
            rightTypeStructDef.setOnConnectInstance(self.onAttributesConnect);
            rightTypeStructDef.setOnDisconnectInstance(self.onAttributesDisConnect);
            self._model.addChild(rightTypeStructDef);


            var newVariableDeclaration = BallerinaASTFactory.createVariableDeclaration();
            // Pushing new variable declaration//todo setType
            newVariableDeclaration.setType("Employee");
            newVariableDeclaration.setIdentifier("e");
            self._model.addChild(newVariableDeclaration);

            var newReturnStatement = BallerinaASTFactory.createReturnStatement();
            newReturnStatement.setReturnExpression("e");
            self._model.addChild(newReturnStatement);

        });

        this._container = currentContainer;
        this.getBoundingBox().fromTopLeft(new Point(0, 0), currentContainer.width(), currentContainer.height());
        this.getModel().accept(this);

        $("#title-" + this._model.id).addClass("type-converter-title-text").text(this._model.getTypeConverterName())
            .on("change paste keyup", function (e) {
                self._model.setTypeConverterName($(this).text());
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

    TypeConverterDefinitionView.prototype.getChildContainer = function () {
        return this._rootGroup;
    };

    TypeConverterDefinitionView.prototype.loadSchemas = function (parentId,selectId) {

        var types = ['-select-','employee','student','person'];

        for (var i = 0; i < types.length; i++) {
            $(parentId).find(selectId).append('<option value="'+i+'">'+types[i]+'</option>');

        };


    };

    /**
     * Calls the render method for a type struct definition.
     * @param {typeStructDefinition} typeStructDefinition - The type struct definition model.
     */
    TypeConverterDefinitionView.prototype.visitTypeStructDefinition = function (typeStructDefinition) {
        log.debug("Visiting type struct definition");
        var self = this;
        var typeStructContainer = this.getChildContainer();
        var typeStructDefinitionView = new TypeStructDefinition({model: typeStructDefinition,container: typeStructContainer,
            toolPalette: this.toolPalette, parentView: this});
        typeStructDefinitionView.render(this.diagramRenderingContext);
    };

    /**
     * Receives attributes connected
     * @param connection object
     */
    TypeConverterDefinitionView.prototype.onAttributesConnect = function (connection) {

        var assignmentStmt = BallerinaASTFactory.createAssignmentStatement();
        var leftOp = BallerinaASTFactory.createLeftOperandExpression();
        var leftOperandExpression = "e." + connection.sourceProperty;
        leftOp.setLeftOperandExpressionString(leftOperandExpression);
        var rightOp = BallerinaASTFactory.createRightOperandExpression();
        var rightOperandExpression = "p." + connection.targetProperty;
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
    TypeConverterDefinitionView.prototype.onAttributesDisConnect = function (connection) {

       // alert(888);
    };

    return TypeConverterDefinitionView;
});