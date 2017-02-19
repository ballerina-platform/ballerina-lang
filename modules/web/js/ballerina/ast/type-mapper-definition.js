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
define(['lodash', './node', '../utils/common-utils'], function (_, ASTNode, CommonUtils) {

    var TypeMapperDefinition = function (args) {
        this._typeMapperName = _.get(args, 'typeMapperName');
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
        var returnType = 'ReturnType';
        var ballerinaASTFactory = this.getFactory();

        _.forEach(this.getChildren(), function (child) {
            if (ballerinaASTFactory.isReturnType(child)) {
                returnType = child.getType();
            }
        });
        return returnType;
    };

    /**
     * returns the input parameter and its identifier
     * @returns {String} argument
     */
    TypeMapperDefinition.prototype.getInputParamAndIdentifier = function () {
        var inputParamAndIdentifier = 'InputType inputIdentifier';
        var ballerinaASTFactory = this.getFactory();

        _.forEach(this.getChildren(), function (child) {
            if (ballerinaASTFactory.isResourceParameter(child)) {
                inputParamAndIdentifier = child.getArgumentAsString();
            }
        });
        return inputParamAndIdentifier;
    };

    /**
     * removes the already selected child before adding a new child
     */
    TypeMapperDefinition.prototype.removeResourceParameter = function () {
        var self = this;
        var ballerinaASTFactory = this.getFactory();

        var previousInputType = _.find(this.getChildren(), function (child) {
            return ballerinaASTFactory.isResourceParameter(child)
        });

        var blockStatement = self.getBlockStatement();
        _.find(blockStatement.getChildren(), function (child) {
            if (ballerinaASTFactory.isAssignmentStatement(child)) {
                child.remove();
            }
        });

        if (!_.isUndefined(previousInputType)) {
            this.removeChild(previousInputType);
        }
    };

    /**
     * removes the already selected child before adding a new child
     */
    TypeMapperDefinition.prototype.removeReturnType = function () {
        var self = this;
        var ballerinaASTFactory = this.getFactory();

        var previousOutputType = _.find(this.getChildren(), function (child) {
            return ballerinaASTFactory.isReturnType(child)
        });

        var blockStatement = self.getBlockStatement();
        _.find(blockStatement.getChildren(), function (child) {
            if (ballerinaASTFactory.isAssignmentStatement(child)) {
                child.remove();
            }
        });

        if (!_.isUndefined(previousOutputType)) {
            this.removeChild(previousOutputType);
        }
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
        newResourceParameter.setType(typeStructName);

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
        newReturnType.setType(typeStructName);

        var lastIndex = _.findLastIndex(this.getChildren());
        this.addChild(newReturnType, lastIndex - 1);
    };

    /**
     * fill return statement.
     * @param {string} identifier
     */
    TypeMapperDefinition.prototype.fillReturnStatement = function (identifier) {

        var self = this;
        var ballerinaASTFactory = this.getFactory();
        var blockStatement = _.find(self.getChildren(), function (child) {
            return ballerinaASTFactory.isBlockStatement(child);
        });

        var returnStatement = _.find(blockStatement.getChildren(), function (child) {
            return ballerinaASTFactory.isReturnStatement(child);
        });

        var variableReferenceExpression = _.find(returnStatement.getChildren(), function (child) {
            return ballerinaASTFactory.isVariableReferenceExpression(child);
        });

        variableReferenceExpression.setVariableReferenceName(identifier);
    };

    /**
     * fill return statement.
     * @param {string} identifier
     */
    TypeMapperDefinition.prototype.fillVariableDefStatement = function (structName, identifier) {

        var self = this;
        var ballerinaASTFactory = this.getFactory();

        var blockStatement = _.find(self.getChildren(), function (child) {
            return ballerinaASTFactory.isBlockStatement(child);
        });

        var variableDefStatement = _.find(blockStatement.getChildren(), function (child) {
            return ballerinaASTFactory.isVariableDefinitionStatement(child);
        });

        variableDefStatement.setLeftExpression(structName + " " + identifier);
    };

    /**
     * Adds new variable definition statement.
     * @param {string} typeStructName
     * @param {string} identifier
     */
    TypeMapperDefinition.prototype.addVariableDefinitionStatement = function (typeStructName, identifier) {

        // Creating a new ResourceParameter.
        var newReturnType = this.getFactory().createReturnType();
        var newStructType = this.getFactory().createStructType();
        newStructType.setTypeName(typeStructName);
        var newSymbolName = this.getFactory().createSymbolName();
        newSymbolName.setName(identifier);
        newReturnType.addChild(newStructType);
        newReturnType.addChild(newSymbolName);
        this.addChild(newReturnType);
    };

    /**
     * Constructs new assignment statement.
     * @param sourceIdentifier
     * @param targetIdentifier
     * @param sourceValue
     * @param targetValue
     * @param isComplexMapping
     * @param targetCastValue
     * @returns {AssignmentStatement}
     */
    TypeMapperDefinition.prototype.returnConstructedAssignmentStatement = function (sourceIdentifier, targetIdentifier, sourceValue, targetValue,
                                                                                    isComplexMapping, targetCastValue) {

        // Creating a new Assignment Statement.
        var self = this;
        var newAssignmentStatement = this.getFactory().createAssignmentStatement();
        var leftOperandExpression = this.getFactory().createLeftOperandExpression();
        var rightOperandExpression = this.getFactory().createRightOperandExpression();
        var typeCastExpression = undefined;

        var sourceStructFieldAccessExpression = this.getFactory().createStructFieldAccessExpression();
        var sourceVariableReferenceExpressionForIdentifier = this.getFactory().createVariableReferenceExpression();
        sourceVariableReferenceExpressionForIdentifier.setVariableReferenceName(sourceIdentifier);
        var sourceFieldExpression = this.getFactory().createStructFieldAccessExpression();
        var tempRefOfFieldExpression;

        _.forEach(sourceValue, function (sourceVal) {
            var tempFieldExpression;
            var tempVariableReferenceExpression = self.getFactory().createVariableReferenceExpression();
            tempVariableReferenceExpression.setVariableReferenceName(sourceVal);
            if (_.head(sourceValue) == sourceVal) {
                sourceFieldExpression.addChild(tempVariableReferenceExpression);
                tempRefOfFieldExpression = sourceFieldExpression
            } else {
                tempFieldExpression = self.getFactory().createStructFieldAccessExpression();
                tempFieldExpression.addChild(tempVariableReferenceExpression);
                tempRefOfFieldExpression.addChild(tempFieldExpression);
                tempRefOfFieldExpression = tempFieldExpression;
            }
        });
        sourceStructFieldAccessExpression.addChild(sourceVariableReferenceExpressionForIdentifier);
        sourceStructFieldAccessExpression.addChild(sourceFieldExpression);


        var targetStructFieldAccessExpression = this.getFactory().createStructFieldAccessExpression();
        var targetVariableReferenceExpressionForIdentifier = this.getFactory().createVariableReferenceExpression();
        targetVariableReferenceExpressionForIdentifier.setVariableReferenceName(targetIdentifier);
        var targetFieldExpression = this.getFactory().createStructFieldAccessExpression();
        var tempRefOfFieldExpression;

        _.forEach(targetValue, function (targetVal) {
            var tempFieldExpression;
            var tempVariableReferenceExpression = self.getFactory().createVariableReferenceExpression();
            tempVariableReferenceExpression.setVariableReferenceName(targetVal);
            if (_.head(targetValue) == targetVal) {
                targetFieldExpression.addChild(tempVariableReferenceExpression);
                tempRefOfFieldExpression = targetFieldExpression
            } else {
                tempFieldExpression = self.getFactory().createStructFieldAccessExpression();
                tempFieldExpression.addChild(tempVariableReferenceExpression);
                tempRefOfFieldExpression.addChild(tempFieldExpression);
                tempRefOfFieldExpression = tempFieldExpression;
            }
        });

        targetStructFieldAccessExpression.addChild(targetVariableReferenceExpressionForIdentifier);
        targetStructFieldAccessExpression.addChild(targetFieldExpression);

        leftOperandExpression.addChild(targetStructFieldAccessExpression);
        newAssignmentStatement.addChild(leftOperandExpression);

        if (isComplexMapping) {
            typeCastExpression = this.getFactory().createTypeCastExpression();
            typeCastExpression.setName(targetCastValue);
            rightOperandExpression.addChild(typeCastExpression);
            typeCastExpression.addChild(sourceStructFieldAccessExpression);
        } else {
            rightOperandExpression.addChild(sourceStructFieldAccessExpression);
        }
        newAssignmentStatement.addChild(rightOperandExpression);

        return newAssignmentStatement;
    };


    /**
     * 
     * @param identifier
     * @param properties
     * @returns {*}
     */
    TypeMapperDefinition.prototype.getStructFieldAccessExpression = function (identifier, properties) {
        var self = this;
        var structFieldAccessExpression = this.getFactory().createStructFieldAccessExpression();
        var variableReferenceExpression = this.getFactory().createVariableReferenceExpression();
        variableReferenceExpression.setVariableReferenceName(identifier);
        var targetFieldExpression = this.getFactory().createStructFieldAccessExpression();
        var tempRefOfFieldExpression;

        _.forEach(properties, function (property) {
            var tempFieldExpression;
            var tempVariableReferenceExpression = self.getFactory().createVariableReferenceExpression();
            tempVariableReferenceExpression.setVariableReferenceName(property);
            if (_.head(properties) == property) {
                targetFieldExpression.addChild(tempVariableReferenceExpression);
                tempRefOfFieldExpression = targetFieldExpression
            } else {
                tempFieldExpression = self.getFactory().createStructFieldAccessExpression();
                tempFieldExpression.addChild(tempVariableReferenceExpression);
                tempRefOfFieldExpression.addChild(tempFieldExpression);
                tempRefOfFieldExpression = tempFieldExpression;
            }
        });
        structFieldAccessExpression.addChild(variableReferenceExpression);
        structFieldAccessExpression.addChild(targetFieldExpression);
        return structFieldAccessExpression;
    };

    /**
     * Gets the reference of block statement child
     * @return {string} - String blockStatement.
     */
    TypeMapperDefinition.prototype.getBlockStatement = function () {
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

    /**
     * @inheritDoc
     * @override
     */
    TypeMapperDefinition.prototype.generateUniqueIdentifiers = function () {
        CommonUtils.generateUniqueIdentifier({
            node: this,
            attributes: [{
                defaultValue: "newTypeMapper",
                setter: this.setTypeMapperName,
                getter: this.getTypeMapperName,
                parents: [{
                    node: this.parent,
                    getChildrenFunc: this.parent.getTypeMapperDefinitions,
                    getter: this.getTypeMapperName
                }]
            }]
        });
    };

    /**
     * Initialize TypeMapperDefinition from json object
     * @param {Object} jsonNode - JSON object for initialization.
     * @param {string} jsonNode.type_mapper_name - Name of the type mapper definition.
     */
    TypeMapperDefinition.prototype.initFromJson = function (jsonNode) {
        var ballerinaASTFactory = this.getFactory();
        var self = this;
        this.setTypeMapperName(jsonNode.type_mapper_name, {doSilently: true});
        var returnNode = undefined;
        _.each(jsonNode.children, function (childNode) {
            if(childNode.type === 'return_type'){
                returnNode = childNode;
            }else{
                var child = ballerinaASTFactory.createFromJson(childNode);
                self.addChild(child);
                child.initFromJson(childNode);
            }
        });
        var blockStatementNode = ballerinaASTFactory.createBlockStatement();
        var childrenArray = self.getChildren();
        _.forEach(childrenArray, function (child) {
            if (!(ballerinaASTFactory.isArgument(child) || ballerinaASTFactory.isVariableDefinitionStatement(child))) {
                blockStatementNode.addChild(child);
            }
        });


        _.forEach(blockStatementNode.getChildren(), function (childNode) {
            _.remove(self.getChildren(), function(child){
                return _.isEqual(childNode.getID(), child.getID());
            });
        });

        var returnASTNode = ballerinaASTFactory.createReturnType();
        returnASTNode.setType(returnNode.parameter_type);
        
        self.addChild(returnASTNode);

        // _.forEach(self.getChildren(), function (outerChild) {
        //     _.forEach(blockStatementNode.getChildren(), function (child) {
        //         if(outerChild.getID() == child.getID()){
        //             self.removeChild(outerChild);
        //         }
        //     });
        // });
        self.addChild(blockStatementNode);
    };


    return TypeMapperDefinition;
});