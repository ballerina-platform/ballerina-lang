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
define(['lodash', './node'], function (_, ASTNode) {

    var TypeMapperDefinition = function (args) {
        this._typeMapperName = _.get(args, 'typeMapperName', 'newTypeMapper');
        ASTNode.call(this, 'TypeMapperDefinition');
    };

    TypeMapperDefinition.prototype = Object.create(ASTNode.prototype);
    TypeMapperDefinition.prototype.constructor = TypeMapperDefinition;

    /**
     * Set the type mapper name
     * @param typeMapperName
     */
    TypeMapperDefinition.prototype.setTypeMapperName = function (typeMapperName, options) {
        if (!_.isNil(typeMapperName)) {
            this.setAttribute('_typeMapperName', typeMapperName, options);
        } else {
            log.error('Invalid Type Mapper name [' + typeMapperName + '] Provided');
            throw 'Invalid Type Mapper name [' + typeMapperName + '] Provided';
        }
    };

    /**
     * returns the type mapper name
     * @returns {string} type mapper name
     */
    TypeMapperDefinition.prototype.getTypeMapperName = function () {
        return this._typeMapperName;
    };

    /**
     * return variable declarations
     * @returns {Array} variable declarations array
     */
    TypeMapperDefinition.prototype.getVariableDeclarations = function () {
        var variableDeclarations = [];
        var self = this;
        var ballerinaASTFactory = this.getFactory();

        _.forEach(this.getChildren(), function (child) {
            if (ballerinaASTFactory.isVariableDeclaration(child)) {
                variableDeclarations.push(child);
            }
        });
        return variableDeclarations;
    };

    /**
     * Gets the return SourceAndTaergetObjects
     * @return {object} - Return type.
     */
    TypeMapperDefinition.prototype.getSourceAndTargetObjects = function (targetExpression) {
        var sourceTargetContainer = {};
        var ballerinaASTFactory = this.getFactory();

        _.forEach(this.getChildren(), function (child) {
            if (ballerinaASTFactory.isTypeStructDefinition(child)) {
                if(child.getTypeStructName() == targetExpression){
                    sourceTargetContainer["target"] = child;
                }else{
                    sourceTargetContainer["source"] = child;
                }
            }
        });
        return sourceTargetContainer;
    };



    /**
     * Add variable declaration
     * @param newVariableDeclaration
     */
    TypeMapperDefinition.prototype.addVariableDeclaration = function (newVariableDeclaration) {
        // Get the index of the last variable declaration.
        var self = this;
        var ballerinaASTFactory = this.getFactory();

        var index = _.findLastIndex(this.getChildren(), function (child) {
            return ballerinaASTFactory.isVariableDeclaration(child);
        });

        this.addChild(newVariableDeclaration, index + 1);
    };

    /**
     * Remove variable declaration
     * @param variableDeclarationIdentifier
     */
    TypeMapperDefinition.prototype.removeVariableDeclaration = function (variableDeclarationIdentifier) {
        var self = this;
        var ballerinaASTFactory = this.getFactory();
        // Removing the variable from the children.
        var variableDeclarationChild = _.find(this.getChildren(), function (child) {
            return ballerinaASTFactory.isVariableDeclaration(child)
                && child.getIdentifier() === variableDeclarationIdentifier;
        });
        this.removeChild(variableDeclarationChild);
    };

    /**
     * Gets the return type
     * @return {string} - Return type.
     */
    TypeMapperDefinition.prototype.getReturnType = function () {
        var returnType = "";
        var ballerinaASTFactory = this.getFactory();

        _.forEach(this.getChildren(), function (child) {
            if (ballerinaASTFactory.isReturnType(child)) {
                _.forEach(child.getChildren(), function (resourceChild) {
                    if(ballerinaASTFactory.isStructType(resourceChild)){
                        returnType = resourceChild.getTypeName();
                    }
                });
            }
        });
        return returnType;
    };

    /**
     * returns the input parameter and its identifier
     * @returns {String} argument
     */
    TypeMapperDefinition.prototype.getInputParamAndIdentifier = function () {
        var inputParam = "";
        var identifier = "";
        var ballerinaASTFactory = this.getFactory();

        _.forEach(this.getChildren(), function (child) {
            if (ballerinaASTFactory.isResourceParameter(child)) {
                _.forEach(child.getChildren(), function (resourceChild) {
                    if(ballerinaASTFactory.isStructType(resourceChild)){
                        inputParam = resourceChild.getTypeName();
                    } else if(ballerinaASTFactory.isSymbolName(resourceChild)){
                        identifier = resourceChild.getName();
                    }
                });
            }
        });
        return inputParam + " " + identifier;
    };

    /**
     * removes the already selected child before adding a new child
     * @param type
     */
    TypeMapperDefinition.prototype.removeTypeStructDefinition = function (type) {
        var self = this;
        var ballerinaASTFactory = this.getFactory();

        var selectedTypeDef = _.find(this.getChildren(), function (child) {
            return ballerinaASTFactory.isTypeStructDefinition(child)
                && child.getCategory() === type;
        });

        if (!_.isUndefined(selectedTypeDef)) {
            this.removeChild(selectedTypeDef);
        }
    };

    /**
     * removes the already selected child before adding a new child
     * @param sourceProperty
     * @param targetProperty
     */
    TypeMapperDefinition.prototype.removeAssignmentDefinition = function (sourceProperty, targetProperty) {
        //TODO: Get rid of hardcoded x and y
        var self = this;
        var ballerinaASTFactory = this.getFactory();
        var assignmentStatement = _.find(this.getChildren(), function (child) {
            return ballerinaASTFactory.isAssignmentStatement(child) &&
                (('x.' + targetProperty + ' = ' + 'y.' + sourceProperty) === child.getStatementString());
        });
        if (!_.isUndefined(assignmentStatement)) {
            this.removeChild(assignmentStatement);
        }
    };

    /**
     * returns the index of the selected struct
     * @param structArray
     * @param selectedStructName
     */
    TypeMapperDefinition.prototype.getSelectedStructIndex = function (structArray, selectedStructName) {
        return _.findIndex(structArray, function (child) {
            return child._structName === selectedStructName;
        });
    };

    /**
     * Adds new type struct definition.
     * @param {string} typeStructName
     * @param {string} identifier
     * @param {object} onConnectFunctionReference
     * @param {object} onDisConnectFunctionReference
     */
    TypeMapperDefinition.prototype.addTypeStructDefinitionChild = function (typeStructName, identifier,onConnectFunctionReference,onDisConnectFunctionReference) {

        // Creating new type struct definition.
        var newTypeStructDef = this.getFactory().createTypeStructDefinition();
        newTypeStructDef.setTypeStructName(typeStructName);
        newTypeStructDef.setIdentifier(identifier);
        newTypeStructDef.setOnConnectInstance(onConnectFunctionReference);
        newTypeStructDef.setOnDisconnectInstance(onDisConnectFunctionReference);
        this.addChild(newTypeStructDef);
    };

    /**
     * Adds new resource parameter.
     * @param {string} typeStructName
     * @param {string} identifier
     */
    TypeMapperDefinition.prototype.addResourceParameterChild = function (typeStructName, identifier) {

        // Creating a new ResourceParameter.
        var newResourceParameter = this.getFactory().createResourceParameter();
        newResourceParameter.setIdentifier(identifier);
        var newSimpleTypeName = this.getFactory().createSimpleTypeName();
        newSimpleTypeName.setName(typeStructName);
        newResourceParameter.addChild(newSimpleTypeName);

        var lastIndex = _.findLastIndex(this.getChildren());
        this.addChild(newResourceParameter, lastIndex - 1);
    };

    /**
     * Adds new return type.
     * @param {string} typeStructName
     * @param {string} identifier
     */
    TypeMapperDefinition.prototype.addReturnTypeChild = function (typeStructName, identifier) {

        // Creating a new ResourceParameter.
        var newReturnType = this.getFactory().createReturnType();
        var newSimpleTypeName = this.getFactory().createSimpleTypeName();
        newSimpleTypeName.setName(typeStructName);
        newReturnType.addChild(newSimpleTypeName);

        var lastIndex = _.findLastIndex(this.getChildren());
        this.addChild(newReturnType, lastIndex - 1);
    };

    /**
     * Adds new variable definition statement.
     * @param {string} typeStructName
     * @param {string} identifier
     */
    TypeMapperDefinition.prototype.addVariableDefinitionStatement = function (typeStructName, identifier) {

        // Creating a new ResourceParameter.
        var newReturnType = this.getFactory().createReturnType();
        //todo check setting annotations
        var newStructType =this.getFactory().createStructType();
        newStructType.setTypeName(typeStructName);
        var newSymbolName =this.getFactory().createSymbolName();
        newSymbolName.setName(identifier);
        newReturnType.addChild(newStructType);
        newReturnType.addChild(newSymbolName);
        this.addChild(newReturnType);
    };

    /**
     * Adds new statement.
     * @param {string} identifier
     */
    TypeMapperDefinition.prototype.addStatement = function (identifier) {

        // Creating a new Statement.
        var newStatement = this.getFactory().createStatement();
        //todo check setting annotations
        var newReturnStatement =this.getFactory().createReturnStatement();
        newReturnStatement.setReturnExpression(identifier);
        newStatement.addChild(newReturnStatement);
        this.addChild(newStatement);
    };

    /**
     * Constructs new assignment statement.
     * @param {string} sourceIdentifier
     * @param {string} targetIdentifier
     * @param {string} sourceValue
     * @param {string} targetValue
     * @returns {AssignmentStatement}
     */
    TypeMapperDefinition.prototype.returnConstructedAssignmentStatement = function (sourceIdentifier,targetIdentifier,sourceValue,targetValue) {

        // Creating a new Assignment Statement.
        var newAssignmentStatement = this.getFactory().createAssignmentStatement();
        var leftOperandExpression = this.getFactory().createLeftOperandExpression();
        var rightOperandExpression = this.getFactory().createRightOperandExpression();
        var newExpression = this.getFactory().createExpression();

        var sourceStructFieldAccessExpression = this.getFactory().createStructFieldAccessExpression();
        var sourceVariableReferenceExpressionForIdentifier = this.getFactory().createVariableReferenceExpression();
        sourceVariableReferenceExpressionForIdentifier.setVariableReferenceName(sourceIdentifier);
        var sourceFieldExpression = this.getFactory().createFieldExpression();
        var sourceVariableReferenceExpressionForValue = this.getFactory().createVariableReferenceExpression();
        sourceVariableReferenceExpressionForValue.setVariableReferenceName(sourceValue);
        sourceFieldExpression.addChild(sourceVariableReferenceExpressionForValue);
        sourceStructFieldAccessExpression.addChild(sourceVariableReferenceExpressionForIdentifier);
        sourceStructFieldAccessExpression.addChild(sourceFieldExpression);

        newExpression.addChild(sourceStructFieldAccessExpression);

        var targetStructFieldAccessExpression = this.getFactory().createStructFieldAccessExpression();
        var targetVariableReferenceExpressionForIdentifier = this.getFactory().createVariableReferenceExpression();
        targetVariableReferenceExpressionForIdentifier.setVariableReferenceName(targetIdentifier);
        var targetFieldExpression = this.getFactory().createFieldExpression();
        var targetVariableReferenceExpressionForTarget = this.getFactory().createVariableReferenceExpression();
        targetVariableReferenceExpressionForTarget.setVariableReferenceName(targetValue);
        targetFieldExpression.addChild(targetVariableReferenceExpressionForTarget);
        targetStructFieldAccessExpression.addChild(targetVariableReferenceExpressionForIdentifier);
        targetStructFieldAccessExpression.addChild(targetFieldExpression);

        leftOperandExpression.addChild(newExpression);
        newAssignmentStatement.addChild(leftOperandExpression);
        rightOperandExpression.addChild(targetStructFieldAccessExpression);
        newAssignmentStatement.addChild(rightOperandExpression);

        return newAssignmentStatement;
    };

    /**
     * Adds assignmentStatement to statement
     * @param assignmentStatement
     */
    TypeMapperDefinition.prototype.addAssignmentStatement = function (assignmentStatement) {

        // returns child Statement.
        var ballerinaASTFactory = this.getFactory();
        var statement = _.find(this.getChildren(), function (child) {
            return ballerinaASTFactory.isStatement(child);
        });

        if(!_.isUndefined(statement)){
            statement.addChild(assignmentStatement);
        }
    };

    /**
     * Gets the reference of block statement child
     * @return {string} - String blockStatement.
     */
    TypeMapperDefinition.prototype.getBlockStatement = function() {
        var blockStatement = undefined;
        var ballerinaASTFactory = this.getFactory();

        _.forEach(this.getChildren(), function (child) {
            if (ballerinaASTFactory.isBlockStatement(child)) {
                blockStatement = child;
                return false;
            }
        });
        return blockStatement;
    };

    return TypeMapperDefinition;
});