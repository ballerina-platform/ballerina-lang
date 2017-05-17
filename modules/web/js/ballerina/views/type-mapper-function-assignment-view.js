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
import $ from 'jquery';
import BallerinaView from './ballerina-view';
import log from 'log';
import TypeMapper from 'typeMapper';
import AssignmentStatement from '../ast/statements/assignment-statement';
import alerts from 'alerts';
import BallerinaASTFactory from 'ballerina/ast/ballerina-ast-factory';

//todo add correct doc comments
/**
 * The view to represent a worker declaration which is an AST visitor.
 * @param {Object} args - Arguments for creating the view.
 * @param {WorkerDeclaration} args.model - The worker declaration model.
 * @param {Object} args.container - The HTML container to which the view should be added to.
 * @param {Object} [args.viewOptions={}] - Configuration values for the view.
 * @constructor
 */
class TypeMapperFunctionAssignmentView extends BallerinaView {
    constructor(args) {
        super(args);
        this._parentView = _.get(args, "parentView");
        this._typeMapperRenderer = _.get(args, 'typeMapperRenderer');
        this._model = _.get(args, 'model');
        this._sourceInfo = _.get(args, 'sourceInfo', {});
        this._targetInfo = _.get(args, 'targetInfo', {});
        if (_.isNil(this.getModel()) || !(this._model instanceof AssignmentStatement)) {
            log.error("Type Mapper Function Assignment is undefined or is of different type." + this.getModel());
            throw "Type Mapper Function Assignment is undefined or is of different type." + this.getModel();
        }

    }

    setModel(model) {
        this._model = model;
    }

    setContainer(container) {
        this._container = container;
    }

    getModel() {
        return this._model;
    }

    getContainer() {
        return this._container;
    }

    getTypeMapperFunctionRenderer() {
        return this._typeMapperRenderer;
    }

    getSourceInfo() {
        return this._sourceInfo;
    }

    getTargetInfo() {
        return this._targetInfo;
    }

    getParentView() {
        return this._parentView;
    }

    //todo add correct doc comments
    /**
     * Rendering the view of the worker declaration.
     * @returns {Object} - The svg group which the worker declaration view resides in.
     */
    render(diagramRenderingContext) {
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

    }

    addFunction(functionExp, diagramRenderingContext, self, assignmentModel) {
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
    }

    handleStructFieldAccssExp(functionReturn, self, schema, functionExp, functionReturnIndex) {
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
    }

    handleFunctionInvocation(functionExp, self, diagramRenderingContext) {
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
    }

    getFunctionInvocationExpression(assignmentStatement) {
        var children = assignmentStatement.getChildren();
        return children[1].getChildren()[0];
    }

    /**
     * return attributes list as a json object
     * @returns {Object} attributes array
     */
    getFunctionSchema(functionInvocationExp, diagramRenderingContext) {
        var schema;
        var funcName = functionInvocationExp.getFunctionName();
        var packageName = functionInvocationExp.getPackageName();
        var fullPackageName = functionInvocationExp.getFullPackageName();
        var functionPackage ;
        if (!_.isEmpty(fullPackageName)) {
            functionPackage = diagramRenderingContext.getPackagedScopedEnvironment().getPackageByName(fullPackageName);
        } else {
            functionPackage = diagramRenderingContext.getPackagedScopedEnvironment().getCurrentPackage();
        }
        var functionDef;
        if (functionPackage) {
            functionDef = functionPackage.getFunctionDefinitionByName(funcName);
        } 
        var mergedParams = [];
        mergedParams = mergedParams.concat(functionDef.getReturnParams());
        mergedParams = mergedParams.concat(functionDef.getParameters());
        var uniqueParams = this.getUniqueParams(mergedParams);
        if (functionDef) {
            schema = {};
            schema['name'] = funcName;
            schema['packageName'] = _.isNil(packageName) ? '' : packageName;
            schema['returnType'] = uniqueParams.slice(0, functionDef.getReturnParams().length);
            schema['parameters'] = uniqueParams.slice(functionDef.getReturnParams().length, uniqueParams.length);
        }
        return schema;
    }

    getUniqueParams(params) {
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
    }

    /**
     * Receives the callBack on function delete
     * @param connection object
     */
    onFunctionDelete(connection) {
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
    }
}

export default TypeMapperFunctionAssignmentView;
    