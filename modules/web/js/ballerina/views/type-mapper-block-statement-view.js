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
define(['lodash', 'log', './ballerina-view', './../ast/block-statement', 'typeMapper', './type-mapper-statement-view',
        'ballerina/ast/ballerina-ast-factory', './type-mapper-function-assignment-view', './../ast/module'],
    function (_, log, BallerinaView, BlockStatement, TypeMapperRenderer, TypeMapperStatement, BallerinaASTFactory,
              TypeMapperFunctionAssignmentView, AST) {

        var TypeMapperBlockStatementView = function (args) {
            BallerinaView.call(this, args);
            this._parentView = _.get(args, "parentView");
            this._onConnectInstance = _.get(args, 'onConnectInstance', {});
            this._onDisconnectInstance = _.get(args, 'onDisconnectInstance', {});
            this._sourceInfo = _.get(args, 'sourceInfo', {});
            this._targetInfo = _.get(args, 'targetInfo', {});

        };

        TypeMapperBlockStatementView.prototype = Object.create(BallerinaView.prototype);
        TypeMapperBlockStatementView.prototype.constructor = TypeMapperBlockStatementView;

        TypeMapperBlockStatementView.prototype.canVisitBlockStatement = function (blockStatement) {
            return true;
        };

        /**
         * Rendering the view of the Block Statement.
         * @param {Object} diagramRenderingContext - the object which is carrying data required for rendering
         */
        TypeMapperBlockStatementView.prototype.render = function (diagramRenderingContext) {
            this._diagramRenderingContext = diagramRenderingContext;

            this._model.accept(this);
            this._model.on('child-added', function (child) {
                this.visit(child)
            }, this)

        };

        /**
         * Initializing Connections.
         */
        TypeMapperBlockStatementView.prototype.initializeConnections = function () {
            var self = this;
            this._parentView.setOnConnectInstance(self.onAttributesConnect);
            this._parentView.setOnDisconnectInstance(self.onAttributesDisConnect);

        };

        /**
         * Calls the render method for a statements.
         * @param {statement} statement - The statement model.
         */
        TypeMapperBlockStatementView.prototype.visitStatement = function (statement) {
            var self = this;
            if (BallerinaASTFactory.isAssignmentStatement(statement)) {
                if (self.getFunctionInvocationStatement(statement)) {
                    var typeMapperFunctionDefinitionView = new TypeMapperFunctionAssignmentView({
                        model: statement,
                        parentView: this,
                        typeMapperRenderer: this._parentView.getTypeMapperRenderer()
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
            }
        };

        TypeMapperBlockStatementView.prototype.getFunctionInvocationStatement = function (statement) {
            if (statement instanceof AST.AssignmentStatement) { //use case for getter
                var children = statement.getChildren();
                var rightOperand = children[1];
                if (rightOperand.getChildren()[0] instanceof AST.FunctionInvocationExpression) {
                    return rightOperand.getChildren()[0];
                }
            }
            return undefined;
        };


        /**
         * Receives attributes connected
         * @param connection object
         */
        TypeMapperBlockStatementView.prototype.onAttributesConnect = function (connection) {
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
                var paramStr = resourceParam.identifier;
                var structFieldAccess = BallerinaASTFactory.createStructFieldAccessExpression({isLHSExpr: false});
                var variableRefExp = BallerinaASTFactory.createVariableReferenceExpression({variableReferenceName: resourceParam.identifier});
                functionInvocationExp.addChild(variableRefExp);
                functionInvocationExp.addChild(structFieldAccess);
                var parentStructFieldExp = structFieldAccess;
                _.forEach(connection.sourceProperty, function (sourceProperty) {
                    structFieldAccess = BallerinaASTFactory.createStructFieldAccessExpression({isLHSExpr: false});
                    variableRefExp = BallerinaASTFactory.createVariableReferenceExpression({variableReferenceName: sourceProperty});
                    paramStr += "." + sourceProperty;
                    parentStructFieldExp.addChild(variableRefExp);
                    parentStructFieldExp.addChild(structFieldAccess);
                    parentStructFieldExp = structFieldAccess;
                });
                if (functionInvocationExpParams && functionInvocationExpParams[0] !== '') {
                    functionInvocationExpParams += "," + paramStr;
                } else {
                    functionInvocationExpParams = paramStr;
                }
                functionInvocationExp.setParams(functionInvocationExpParams);
            } else if (BallerinaASTFactory.isAssignmentStatement(sourceModel)
                && BallerinaASTFactory.isReturnType(targetModel)) {
                var leftOperand = sourceModel.getChildren()[0];
                var index = _.findIndex(sourceFuncSchema.returnType, function (param) {
                    return param.name === connection.sourceProperty[0];
                });
                var sourceVariableRef = leftOperand.getChildren()[index];
                var assignmentStatementNode = targetModel.getParent()
                    .getAssignmentStatementForFunctionReturnVariable(sourceVariableRef, "x", connection.targetProperty,
                        connection.isComplexMapping, connection.complexMapperName);
                var blockStatement = targetModel.getParent().getBlockStatement();
                var lastIndex = _.findLastIndex(blockStatement.getChildren());
                blockStatement.addChild(assignmentStatementNode, lastIndex);
            }
        };

        /**
         * Receives the attributes disconnected
         * @param connection object
         */
        TypeMapperBlockStatementView.prototype.onAttributesDisConnect = function (connection) {

            var blockStatement = connection.targetReference.getParent().getBlockStatement();
            var assignmentStatementId = connection.id;
            blockStatement.removeChildById(assignmentStatementId);
        };

        /**
         * returns the source info
         * @returns {object}
         */
        TypeMapperBlockStatementView.prototype.getSourceInfo = function () {
            return this._sourceInfo;
        };

        /**
         * sets the source info
         * @returns {object}
         */
        TypeMapperBlockStatementView.prototype.setSourceInfo = function (sourceInfo) {
            this._sourceInfo = sourceInfo;
        };

        /**
         * returns the source info
         * @returns {object}
         */
        TypeMapperBlockStatementView.prototype.getTargetInfo = function () {
            return this._targetInfo;
        };

        /**
         * sets the target info
         * @returns {object}
         */
        TypeMapperBlockStatementView.prototype.setTargetInfo = function (targetInfo) {
            this._targetInfo = targetInfo;
        };

        return TypeMapperBlockStatementView;
    });