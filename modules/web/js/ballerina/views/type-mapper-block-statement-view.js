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
import BlockStatement from '../ast/statements/block-statement';
import TypeMapperRenderer from 'typeMapper';
import TypeMapperStatement from './type-mapper-statement-view';
import BallerinaASTFactory from 'ballerina/ast/ballerina-ast-factory';
import TypeMapperFunctionAssignmentView from './type-mapper-function-assignment-view';
import AST from './../ast/module';
import Constants from 'constants';
var STRUCT_DEFINITION_ATTRIBUTES_ARRAY_PROPERTY_TYPE = Constants.STRUCT_DEFINITION_ATTRIBUTES_ARRAY_PROPERTY_TYPE;
var STRUCT_DEFINITION_ATTRIBUTES_ARRAY_PROPERTY_NAME = Constants.STRUCT_DEFINITION_ATTRIBUTES_ARRAY_PROPERTY_NAME;

class TypeMapperBlockStatementView extends BallerinaView {
    constructor(args) {
        super(args);
        this._parentView = _.get(args, "parentView");
        this._onConnectInstance = _.get(args, 'onConnectInstance', {});
        this._onDisconnectInstance = _.get(args, 'onDisconnectInstance', {});
        this._sourceInfo = _.get(args, 'sourceInfo', {});
        this._targetInfo = _.get(args, 'targetInfo', {});

    }

    canVisitBlockStatement(blockStatement) {
        return true;
    }

    /**
     * Rendering the view of the Block Statement.
     * @param {Object} diagramRenderingContext - the object which is carrying data required for rendering
     */
    render(diagramRenderingContext) {
        this._diagramRenderingContext = diagramRenderingContext;
        this._model.accept(this);
        this._model.on('child-added', function (child) {
            this.visit(child)
        }, this);

    }

    /**
     * Initializing Connections.
     */
    initializeConnections() {
        var self = this;
        this._parentView.setOnConnectInstance(self.onAttributesConnect);
        this._parentView.setOnDisconnectInstance(self.onAttributesDisConnect);

    }

    /**
     * Calls the render method for a statements.
     * @param {statement} statement - The statement model.
     */
    visitStatement(statement) {
        var self = this;
        if (BallerinaASTFactory.isAssignmentStatement(statement)) {
            if (self.getFunctionInvocationStatement(statement)) {
                var typeMapperFunctionDefinitionView = new TypeMapperFunctionAssignmentView({
                    model: statement,
                    parentView: this,
                    typeMapperRenderer: this._parentView.getTypeMapperRenderer(),
                    sourceInfo: self.getSourceInfo(),
                    targetInfo: self.getTargetInfo()
                });
                typeMapperFunctionDefinitionView.render(this._diagramRenderingContext);
            } else {
                var typeMapperStatementView = new TypeMapperStatement({
                    model: statement,
                    parentView: this,
                    typeMapperRenderer: this._parentView.getTypeMapperRenderer(),
                    sourceInfo: self.getSourceInfo(),
                    targetInfo: self.getTargetInfo()
                });
                typeMapperStatementView.render(this._diagramRenderingContext);
            }
        } else if (BallerinaASTFactory.isFunctionInvocationStatement(statement)) {
            var functionInvocationExp = statement.getChildren()[0];
            var assignmentNode = BallerinaASTFactory.createAssignmentStatement();
            var leftOperand = BallerinaASTFactory.createLeftOperandExpression();
            leftOperand.setLeftOperandExpressionString('');
            leftOperand.setLeftOperandType('');
            var rightOperand = BallerinaASTFactory.createRightOperandExpression();
            rightOperand.setRightOperandExpressionString('');
            assignmentNode.addChild(leftOperand);
            assignmentNode.addChild(rightOperand);
            rightOperand.addChild(functionInvocationExp);
            self.getModel().removeChild(statement, true, false);
            self.getModel().addChild(assignmentNode, _.findLastIndex(self.getModel().getChildren()) - 1, undefined, false);
            var typeMapperFunctionDefinitionView = new TypeMapperFunctionAssignmentView({
                model: assignmentNode,
                parentView: this,
                typeMapperRenderer: this._parentView.getTypeMapperRenderer(),
                sourceInfo: self.getSourceInfo(),
                targetInfo: self.getTargetInfo()
            });
            typeMapperFunctionDefinitionView.render(this._diagramRenderingContext);
        }
    }

    getFunctionInvocationStatement(statement) {
        if (statement instanceof AST.AssignmentStatement) { //use case for getter
            var children = statement.getChildren();
            var rightOperand = children[1];
            if (rightOperand.getChildren()[0] instanceof AST.FunctionInvocationExpression) {
                return rightOperand.getChildren()[0];
            }
        }
        return undefined;
    }

    /**
     * returns the values of children
     * @returns {object}
     */
    getExpressionProperties(fieldExpression, structSchema, propertyArray) {

        var self = this;
        var tempType = "";
        var tempAttr = {};

        var variableRefExpression = fieldExpression.findChild(BallerinaASTFactory.isVariableReferenceExpression);

        tempAttr[STRUCT_DEFINITION_ATTRIBUTES_ARRAY_PROPERTY_NAME] = variableRefExpression.getVariableName();

        _.each(structSchema.getAttributesArray().properties, function (property) {
            if (property.name == variableRefExpression.getVariableName()) {
                tempType = property.type;
                return false;
            }
        });

        tempAttr[STRUCT_DEFINITION_ATTRIBUTES_ARRAY_PROPERTY_TYPE] = tempType;

        propertyArray.push(tempAttr);

        var innerFieldExpression = _.find(fieldExpression.getChildren(), function (child) {
            return BallerinaASTFactory.isStructFieldAccessExpression(child);
        });

        if (!_.isUndefined(innerFieldExpression)) {
            var availableDefinedStructs = self.getSourceInfo().predefinedStructs;
            var innerStruct = _.find(availableDefinedStructs, function (struct) {
                return struct.getStructName() == tempType
            });
            self.getExpressionProperties(innerFieldExpression, innerStruct, propertyArray);
        }
        return propertyArray;
    }

    /**
     * Receives attributes connected
     * @param connection object
     */
    onAttributesConnect(connection) {
        var sourceModel = connection.sourceReference;
        var sourceFuncSchema;
        var targetModel = connection.targetReference;
        var targetFuncSchema;
        if (connection.sourceReference.model) {
            sourceModel = connection.sourceReference.model;
            sourceFuncSchema = connection.sourceReference.functionSchema;
        }
        if (connection.targetReference.model) {
            targetModel = connection.targetReference.model;
            targetFuncSchema = connection.targetReference.functionSchema;
        }
        if (BallerinaASTFactory.isResourceParameter(sourceModel) && BallerinaASTFactory.isReturnType(targetModel)) {
            var isComplexMapping = connection.isComplexMapping;
            var targetCastValue = "";
            if (isComplexMapping) {
                targetCastValue = connection.targetType;
            }
            var assignmentStatementNode = sourceModel.getParent().returnConstructedAssignmentStatement("y", "x", connection.sourceProperty, connection.targetProperty, isComplexMapping, targetCastValue);
            var blockStatement = targetModel.getParent().getBlockStatement();
            var lastIndex = _.findLastIndex(blockStatement.getChildren());
            blockStatement.addChild(assignmentStatementNode, lastIndex);
        } else if (BallerinaASTFactory.isResourceParameter(sourceModel) &&
            BallerinaASTFactory.isAssignmentStatement(targetModel)) {
            var resourceParam = sourceModel;
            var functionInvocationExp = targetModel.getChildren()[1].getChildren()[0];
            var functionInvocationExpParams = functionInvocationExp.getParams();
            var structFieldAccess = BallerinaASTFactory.createStructFieldAccessExpression({isLHSExpr: false});
            var variableRefExp = BallerinaASTFactory.createVariableReferenceExpression({variableName: resourceParam.identifier});
            structFieldAccess.addChild(variableRefExp);
            var index = _.findIndex(targetFuncSchema.parameters, function (param) {
                return param.name === connection.targetProperty[0];
            });
            var parentStructFieldExp = structFieldAccess;
            var root = structFieldAccess;
            _.forEach(connection.sourceProperty, function (sourceProperty) {
                structFieldAccess = BallerinaASTFactory.createStructFieldAccessExpression({isLHSExpr: false});
                variableRefExp = BallerinaASTFactory.createVariableReferenceExpression({variableName: sourceProperty});
                structFieldAccess.addChild(variableRefExp);
                parentStructFieldExp.addChild(structFieldAccess);
                parentStructFieldExp = structFieldAccess;
            });
            if (functionInvocationExp.getChildren().length > index) {
                functionInvocationExp.getChildren()[index].remove();
                functionInvocationExp.addChild(root, index);
            } else {
                functionInvocationExp.addChild(root);
            }

        } else if (BallerinaASTFactory.isAssignmentStatement(sourceModel)
            && BallerinaASTFactory.isReturnType(targetModel)) {
            var leftOperand = sourceModel.getChildren()[0];
            var structFieldAccessExp = targetModel.getParent().getStructFieldAccessExpression('x', connection.targetProperty);
            var index = _.findIndex(sourceFuncSchema.returnType, function (param) {
                return param.name === connection.sourceProperty[0];
            });
            leftOperand.removeChild(leftOperand.getChildren()[index], true);
            leftOperand.addChild(structFieldAccessExp, index);
        } else if (BallerinaASTFactory.isAssignmentStatement(sourceModel)
            && BallerinaASTFactory.isAssignmentStatement(targetModel)) {
            var functionInvocation = sourceModel.getChildren()[1].getChildren()[0];
            sourceModel.remove();
//                sourceModel.setParent(undefined);
            var targetFunctionInvocation = targetModel.getChildren()[1].getChildren()[0];
            var index = _.findIndex(targetFuncSchema.parameters, function (param) {
                return param.name === connection.targetProperty[0];
            });
            if (targetFunctionInvocation.getChildren().length > index) {
                targetFunctionInvocation.getChildren()[index].remove();
                targetFunctionInvocation.addChild(functionInvocation, index);
            } else {
                targetFunctionInvocation.addChild(functionInvocation);
            }
        }
    }

    /**
     * Receives the attributes disconnected
     * @param connection object
     */
    onAttributesDisConnect(connection) {
        var sourceModel = connection.sourceReference;
        var sourceFuncSchema;
        var targetModel = connection.targetReference;
        var targetFuncSchema;
        if (connection.sourceReference.model) {
            sourceModel = connection.sourceReference.model;
            sourceFuncSchema = connection.sourceReference.functionSchema;
        }
        if (connection.targetReference.model) {
            targetModel = connection.targetReference.model;
            targetFuncSchema = connection.targetReference.functionSchema;
        }
        if (BallerinaASTFactory.isReturnType(targetModel)
            && BallerinaASTFactory.isResourceParameter(sourceModel)) {
            var blockStatement = connection.targetReference.getParent().getBlockStatement();
            var assignmentStatementId = connection.id;
            blockStatement.removeChildById(assignmentStatementId);
        } else if (BallerinaASTFactory.isAssignmentStatement(targetModel)
            && BallerinaASTFactory.isResourceParameter(sourceModel)) {
            //Struct -> function
            var resourceParam = sourceModel;
            var functionInvocationExp = targetModel.getChildren()[1].getChildren()[0];
            var index = _.findIndex(targetFuncSchema.parameters, function (param) {
                return param.name === connection.targetProperty[0];
            });
            var structInput = functionInvocationExp.getChildren()[index];
            structInput.remove();
            var variableRefExp = BallerinaASTFactory.createVariableReferenceExpression();
            variableRefExp.setVariableName('');
            functionInvocationExp.addChild(variableRefExp, index);
        } else if (BallerinaASTFactory.isAssignmentStatement(sourceModel)
            && BallerinaASTFactory.isReturnType(targetModel)) {
            //function -> struct
            //TODO: fix for multiple returns
            var leftOperand = sourceModel.getChildren()[0];
            leftOperand.getChildren()[0].remove();
            var structFieldAccessExp = BallerinaASTFactory.createStructFieldAccessExpression();
            leftOperand.addChild(structFieldAccessExp, 0);
        } else if (BallerinaASTFactory.isAssignmentStatement(sourceModel)
            && BallerinaASTFactory.isAssignmentStatement(targetModel)) {
            var sourceFunctionInvocation = sourceModel.getChildren()[1].getChildren()[0];
            sourceFunctionInvocation.remove();
            var targetFunctionInvocation = targetModel.getChildren()[1].getChildren()[0];
            var index = _.findIndex(targetFuncSchema.parameters, function (param) {
                return param.name === connection.targetProperty[0];
            });
            var variableRefExp = BallerinaASTFactory.createVariableReferenceExpression();
            variableRefExp.setVariableName('');
            if (targetFunctionInvocation.getChildren().length > index) {
                targetFunctionInvocation.getChildren()[index].remove();
                targetFunctionInvocation.addChild(variableRefExp, index);
            } else {
                targetFunctionInvocation.addChild(variableRefExp);
            }
            var assmtModel = BallerinaASTFactory.createAssignmentStatement();
            var leftOperand = BallerinaASTFactory.createLeftOperandExpression();
            leftOperand.setLeftOperandExpressionString('');
            leftOperand.setLeftOperandType('');
            assmtModel.addChild(leftOperand);
            var rightOperand = BallerinaASTFactory.createRightOperandExpression();
            rightOperand.setRightOperandExpressionString('');
            rightOperand.addChild(sourceFunctionInvocation);
            assmtModel.addChild(rightOperand);
            _.forEach(sourceFuncSchema.returnType, function (aReturnType) {
                var structFieldAccessExp = BallerinaASTFactory.createStructFieldAccessExpression();
                leftOperand.addChild(structFieldAccessExp);
            });
            var node = targetFunctionInvocation.getParent();
            var blockStatement;
            while (node && !blockStatement) {
                if (BallerinaASTFactory.isBlockStatement(node)) {
                    blockStatement = node;
                }
                node = node.getParent();
            }
            blockStatement.addChild(assmtModel, _.findLastIndex(blockStatement.getChildren()), undefined, false);
        }
    }

    /**
     * returns the source info
     * @returns {object}
     */
    getSourceInfo() {
        return this._sourceInfo;
    }

    /**
     * sets the source info
     * @returns {object}
     */
    setSourceInfo(sourceInfo) {
        this._sourceInfo = sourceInfo;
    }

    /**
     * returns the source info
     * @returns {object}
     */
    getTargetInfo() {
        return this._targetInfo;
    }

    /**
     * sets the target info
     * @returns {object}
     */
    setTargetInfo(targetInfo) {
        this._targetInfo = targetInfo;
    }
}

export default TypeMapperBlockStatementView;
    