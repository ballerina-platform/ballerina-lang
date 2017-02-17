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
            var functionExp = self.getFunctionInvocationExpression(this.getModel());
            var schema = self.getFunctionSchema(functionExp, diagramRenderingContext);
            if (schema) {
                this.getTypeMapperFunctionRenderer().addFunction(schema, {
                    model: this.getModel(),
                    functionSchema: schema
                });
                //Handling left hand side
                var functionReturns = self.getModel().getChildren()[0].getChildren();
                var functionReturnIndex = 0;
                _.forEach(functionReturns, function (functionReturn) {
                    if (BallerinaASTFactory.isStructFieldAccessExpression(functionReturn)) {
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
                            connection["id"] = self.getModel().getID();
                            this.getTypeMapperRenderer().addConnection(connection);
                        }
                    } else if (BallerinaASTFactory.isVariableReferenceExpression(functionReturn)){
                        //TODO handle variable assignment
                    }
                })
            } else {
                alerts.error("No function exists in name : " + functionExp.getFunctionName());
            }
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
                schema['parameters'] = uniqueParams.slice(functionDef.getReturnParams().length,uniqueParams.length);
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
        return TypeMapperFunctionAssignmentView;
    });