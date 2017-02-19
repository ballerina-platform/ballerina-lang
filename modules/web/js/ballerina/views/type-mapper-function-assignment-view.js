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

define(['lodash', 'jquery', './ballerina-view', 'log', 'typeMapper', './../ast/assignment-statement', 'alerts', 'ballerina/ast/ballerina-ast-factory'],
    function (_, $, BallerinaView, log, TypeMapper, AssignmentStatement, alerts, BallerinaASTFactory) {

        //todo add correct doc comments
        /**
         * The view to represent a worker declaration which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {WorkerDeclaration} args.model - The worker declaration model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var TypeMapperFunctionAssignmentView = function (args) {
            BallerinaView.call(this, args);
            this._parentView = _.get(args, "parentView");
            this._typeMapperRenderer = _.get(args, 'typeMapperRenderer');
            this._model = _.get(args, 'model');
            this._sourceInfo = _.get(args, 'sourceInfo', {});
            this._targetInfo = _.get(args, 'targetInfo', {});
            if (_.isNil(this.getModel()) || !(this._model instanceof AssignmentStatement)) {
                log.error("Type Mapper Function Assignment is undefined or is of different type." + this.getModel());
                throw "Type Mapper Function Assignment is undefined or is of different type." + this.getModel();
            }

        };

        TypeMapperFunctionAssignmentView.prototype = Object.create(BallerinaView.prototype);
        TypeMapperFunctionAssignmentView.prototype.constructor = TypeMapperFunctionAssignmentView;

        TypeMapperFunctionAssignmentView.prototype.setModel = function (model) {
            this._model = model;
        };

        TypeMapperFunctionAssignmentView.prototype.setContainer = function (container) {
            this._container = container;
        };

        TypeMapperFunctionAssignmentView.prototype.getModel = function () {
            return this._model;
        };

        TypeMapperFunctionAssignmentView.prototype.getContainer = function () {
            return this._container;
        };

        TypeMapperFunctionAssignmentView.prototype.getTypeMapperFunctionRenderer = function () {
            return this._typeMapperRenderer;
        };

        TypeMapperFunctionAssignmentView.prototype.getSourceInfo = function () {
            return this._sourceInfo;
        };

        TypeMapperFunctionAssignmentView.prototype.getTargetInfo = function () {
            return this._targetInfo;
        };

        TypeMapperFunctionAssignmentView.prototype.getParentView = function () {
            return this._parentView;
        };

        //todo add correct doc comments
        /**
         * Rendering the view of the worker declaration.
         * @returns {Object} - The svg group which the worker declaration view resides in.
         */
        TypeMapperFunctionAssignmentView.prototype.render = function (diagramRenderingContext) {
            var self = this;
            var functionExp = self.getFunctionInvocationExpression(self.getModel());
            self.addFunction(functionExp, diagramRenderingContext, self, self.getModel());
            var schema = self.getFunctionSchema(functionExp, diagramRenderingContext);
            //Handling left hand side
            var leftOperandChildren = self.getModel().getChildren()[0].getChildren();
            var functionReturnIndex = 0;
            _.forEach(leftOperandChildren, function (functionReturn) {
                if (BallerinaASTFactory.isStructFieldAccessExpression(functionReturn)) {
                    self.handleStructFieldAccssExp(functionReturn, self, schema, functionExp, functionReturnIndex);
                } else if (BallerinaASTFactory.isVariableReferenceExpression(functionReturn)) {
                    //TODO handle variable assignment
                }
                functionReturnIndex++;
            });
            //Right handside iteration
            self.handleFunctionInvocation(functionExp, self, diagramRenderingContext);

        };

        TypeMapperFunctionAssignmentView.prototype.addFunction = function (functionExp, diagramRenderingContext, self, assignmentModel) {
            var schema = self.getFunctionSchema(functionExp, diagramRenderingContext);
            if (schema) {
                //todo remove adding diagramRenderingContext to below object .. get it from renderer
                self.getTypeMapperFunctionRenderer().addFunction(schema, {
                    model: assignmentModel,
                    currentView: this,
                    renderingContext: diagramRenderingContext,
                    functionSchema: schema,
                    functionInvocationExpression: functionExp
                }, self.onFunctionDelete);
                _.forEach(functionExp.getChildren(), function (child) {
                    if (BallerinaASTFactory.isFunctionInvocationExpression(child)) {
                        var assmtModel = BallerinaASTFactory.createAssignmentStatement();
                        var leftOperand = BallerinaASTFactory.createLeftOperandExpression();
                        leftOperand.setLeftOperandExpressionString('');
                        leftOperand.setLeftOperandType('');
                        assmtModel.addChild(leftOperand);
                        var rightOperand = BallerinaASTFactory.createRightOperandExpression();
                        rightOperand.setRightOperandExpressionString('');
                        rightOperand.addChild(child);
                        assmtModel.addChild(rightOperand);
                        // need to do this, as this was added as child to assignment node.
                        child.setParent(functionExp, {doSilently:true});
                        self.addFunction(child, diagramRenderingContext, self, assmtModel);
                    }
                });
            } else {
                alerts.error("No function exists in name : " + functionExp.getFunctionName());
            }
        };

        TypeMapperFunctionAssignmentView.prototype.handleStructFieldAccssExp = function (functionReturn, self, schema, functionExp, functionReturnIndex) {
            if (functionReturn.getChildren().length > 0) {
                var targetStructSchema = self.getTargetInfo().targetStruct;
                var targetPropertyNames = [];
                var targetPropertyTypes = [];
                var fieldExpression = _.find(functionReturn.getChildren(), function (child) {
                    return BallerinaASTFactory.isStructFieldAccessExpression(child);
                });
                var complexTargetProperties = self.getParentView().getExpressionProperties(fieldExpression, targetStructSchema, []);
                _.each(complexTargetProperties, function (property) {
                    targetPropertyNames.push(property.name);
                    targetPropertyTypes.push(property.type);
                });
                var sourcePropertyNames = [];
                sourcePropertyNames.push(schema.returnType[functionReturnIndex].name);
                var sourcePropertyTypes = [];
                sourcePropertyTypes.push(schema.returnType[functionReturnIndex].type);
                var connection = {};
                connection["targetStruct"] = self.getTargetInfo().targetStructName;
                connection["targetProperty"] = targetPropertyNames;
                connection["targetType"] = targetPropertyTypes;
                connection["sourceStruct"] = schema.name;
                connection["sourceProperty"] = sourcePropertyNames;
                connection["sourceType"] = sourcePropertyTypes;
                connection["id"] = functionExp.getID();
                connection["sourceId"] = functionExp.getID();
                connection["sourceFunction"] = true;
                connection["targetFunction"] = false;
                self.getTypeMapperFunctionRenderer().addConnection(connection);
            }
        };

        TypeMapperFunctionAssignmentView.prototype.handleFunctionInvocation = function (functionExp, self, diagramRenderingContext) {
            var index = 0;
            _.forEach(functionExp.getChildren(), function (functionParam) {
                if (BallerinaASTFactory.isFunctionInvocationExpression(functionParam)) {
                    //function -> function
                    var sourceFunction = functionParam;
                    var targetFunction = functionExp;
                    var sourceSchema = self.getFunctionSchema(sourceFunction, diagramRenderingContext);
                    var targetSchema = self.getFunctionSchema(targetFunction, diagramRenderingContext);
                    var sourcePropertyTypes = [],
                        sourcePropertyNames = [],
                        targetPropertyTypes = [],
                        targetPropertyNames = [];
                    sourcePropertyNames.push(sourceSchema.returnType[0].name);
                    sourcePropertyTypes.push(sourceSchema.returnType[0].type);
                    targetPropertyNames.push(targetSchema.parameters[index].name);
                    targetPropertyTypes.push(targetSchema.parameters[index].type);
                    var connection = {};
                    connection["targetStruct"] = targetSchema.name;
                    connection["targetProperty"] = targetPropertyNames;
                    connection["targetType"] = targetPropertyTypes;
                    connection["sourceStruct"] = sourceSchema.name;
                    connection["sourceProperty"] = sourcePropertyNames;
                    connection["sourceType"] = sourcePropertyTypes;
                    connection["id"] = sourceFunction.getID();
                    connection["sourceId"] = sourceFunction.getID();
                    connection["targetId"] = targetFunction.getID();
                    connection["sourceFunction"] = true;
                    connection["targetFunction"] = true;
                    console.log(connection);
                    self.getTypeMapperFunctionRenderer().addConnection(connection);
                    self.handleFunctionInvocation(functionParam, self, diagramRenderingContext);
                } else if (BallerinaASTFactory.isStructFieldAccessExpression(functionParam)) {
                    //struct -> function
                    var sourceStructSchema = self.getSourceInfo().sourceStruct;
                    var targetSchema = self.getFunctionSchema(functionExp, diagramRenderingContext);
                    var targetPropertyNames = [];
                    var targetPropertyTypes = [];
                    var sourcePropertyNames = [];
                    var sourcePropertyTypes = [];

                    var fieldExpression = _.find(functionParam.getChildren(), function (child) {
                        return BallerinaASTFactory.isStructFieldAccessExpression(child);
                    });
                    var complexTargetProperties = self.getParentView().getExpressionProperties(fieldExpression, sourceStructSchema, []);
                    _.each(complexTargetProperties, function (property) {
                        sourcePropertyNames.push(property.name);
                        sourcePropertyTypes.push(property.type);
                    });
                    targetPropertyNames.push(targetSchema.parameters[index].name);
                    targetPropertyTypes.push(targetSchema.parameters[index].type);
                    var connection = {};
                    connection["targetStruct"] = targetSchema.name;
                    connection["targetProperty"] = targetPropertyNames;
                    connection["targetType"] = targetPropertyTypes;
                    connection["sourceStruct"] = self.getSourceInfo().sourceStructName;
                    connection["sourceProperty"] = sourcePropertyNames;
                    connection["sourceType"] = sourcePropertyTypes;
                    connection["id"] = functionExp.getID();
                    connection["targetId"] = functionExp.getID();
                    connection["sourceFunction"] = false;
                    connection["targetFunction"] = true;
                    self.getTypeMapperFunctionRenderer().addConnection(connection);
                }
                index = index + 1;
            })
        };

        TypeMapperFunctionAssignmentView.prototype.getFunctionInvocationExpression = function (assignmentStatement) {
            var children = assignmentStatement.getChildren();
            return children[1].getChildren()[0];
        };


        /**
         * return attributes list as a json object
         * @returns {Object} attributes array
         */
        TypeMapperFunctionAssignmentView.prototype.getFunctionSchema = function (functionInvocationExp, diagramRenderingContext) {
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
            var mergedParams = [];
            mergedParams = mergedParams.concat(functionDef.getReturnParams());
            mergedParams = mergedParams.concat(functionDef.getParameters());
            var uniqueParams = this.getUniqueParams(mergedParams);
            if (functionDef) {
                schema = {};
                schema['name'] = funcName;
                schema['returnType'] = uniqueParams.slice(0, functionDef.getReturnParams().length);
                schema['parameters'] = uniqueParams.slice(functionDef.getReturnParams().length, uniqueParams.length);
            }
            return schema;
        };


        TypeMapperFunctionAssignmentView.prototype.getUniqueParams = function (params) {
            var uniqueParams = [];
            var uniqueParamIds = [];
            _.forEach(params, function (param) {
                var matchedParam = _.find(uniqueParams, function (uniqueParam) {
                    return uniqueParam.name == param.name && uniqueParam.type == param.type;
                });
                if (!matchedParam) {
                    uniqueParams.push(param);
                    uniqueParamIds.push({name: param.name, id: 0});
                } else {
                    var uniqueParamId = _.find(uniqueParamIds, function (paramId) {
                        return paramId.name == param.name;
                    });
                    var newId = uniqueParamId.id + 1;
                    param.name = param.name + newId;
                    uniqueParams.push(param);
                    uniqueParamId.id = newId;
                }
            });
            return uniqueParams;
        };

        /**
         * Receives the callBack on function delete
         * @param connection object
         */
        TypeMapperFunctionAssignmentView.prototype.onFunctionDelete = function (connection) {
            var functionReferenceObj = connection.reference.model;
            var functionInvocationExpression = connection.reference.functionInvocationExpression;
            var parentOfFunctionInvocationExpression = functionInvocationExpression.getParent();
            var blockStatement = functionReferenceObj.getParent();
            var renderingConext = connection.reference.renderingContext;
            var currentView = connection.reference.currentView;

            var innerFunctionInvocationExpression = _.find(functionInvocationExpression.getChildren(), function (child) {
                if (BallerinaASTFactory.isFunctionInvocationExpression(child)) {
                    return child;
                }
            });
            if(!BallerinaASTFactory.isFunctionInvocationExpression(parentOfFunctionInvocationExpression)){
                blockStatement.removeChildById(parentOfFunctionInvocationExpression.getParent().getID());

            }else{
                functionInvocationExpression.remove();
                parentOfFunctionInvocationExpression.addChild(BallerinaASTFactory.createVariableReferenceExpression());
            }
            if(!_.isUndefined(innerFunctionInvocationExpression)){

                var childSchema = currentView.getFunctionSchema(innerFunctionInvocationExpression,renderingConext);
                var assignmentStaetment = BallerinaASTFactory.createAssignmentStatement();
                var leftOperandExpression = BallerinaASTFactory.createLeftOperandExpression();
                var rightOperandExpression = BallerinaASTFactory.createRightOperandExpression();
                assignmentStaetment.addChild(leftOperandExpression);
                assignmentStaetment.addChild(rightOperandExpression);

                _.forEach(childSchema.returnType, function (aReturnType) {
                    var structFieldAccessExp = BallerinaASTFactory.createStructFieldAccessExpression();
                    leftOperandExpression.addChild(structFieldAccessExp);
                });
                leftOperandExpression.setLeftOperandExpressionString('');
                leftOperandExpression.setLeftOperandType('');
                rightOperandExpression.addChild(innerFunctionInvocationExpression);
                rightOperandExpression.setRightOperandExpressionString('');
                rightOperandExpression.getChildren()[0].setParams('');

                var lastIndex = _.findLastIndex(blockStatement.getChildren());
                blockStatement.addChild(assignmentStaetment,lastIndex,undefined,false);
            }
        };
        return TypeMapperFunctionAssignmentView;
    });