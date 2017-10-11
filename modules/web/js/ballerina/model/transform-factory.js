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

import FragmentUtils from '../utils/fragment-utils';
import TreeBuilder from './tree-builder';
import Environment from '../env/environment';
import { VarPrefix } from '../utils/transform-utils';

class TransformFactory {
    /**
     * Create for expression for fields
     * @param  {string} name expression name
     * @return {object} expression object
     */
    static createVariableRefExpression(name) {
        const fragment = FragmentUtils.createExpressionFragment(name);
        const parsedJson = FragmentUtils.parseFragment(fragment);
        const refExpr = TreeBuilder.build(parsedJson.variable.initialExpression);
        return refExpr;
    }

    /**
     * Create for statement for fields
     * @param  {string} name  variable name
     * @param  {string} type  variable type
     * @param  {string} value default value
     * @return {object} statement object
     */
    static createVariableDef(name, type, value) {
        const fragment = FragmentUtils.createStatementFragment(type + ' ' + name + ' = ' + value + '"";');
        const parsedJson = FragmentUtils.parseFragment(fragment);
        const refExpr = TreeBuilder.build(parsedJson);
        return refExpr;
    }

    /**
     * Create default expression based on argument type
     * @static
     * @param {any} type type
     * @memberof TransformFactory
     * @return {object} expression object
     */
    static createDefaultExpression(type) {
        const defaultValue = Environment.getDefaultValue(type);
        let fragment = FragmentUtils.createExpressionFragment('null');
        if (defaultValue !== undefined) {
            fragment = FragmentUtils.createExpressionFragment(defaultValue);
        }
        const parsedJson = FragmentUtils.parseFragment(fragment);
        const exp = TreeBuilder.build(parsedJson.variable.initialExpression);
        return exp;
    }

    /**
     * Create for statement from statement
     * @param  {string} statement  statement string
     * @return {object} statement object
     */
    static createVariableDefFromStatement(statement) {
        const fragment = FragmentUtils.createStatementFragment(statement);
        const parsedJson = FragmentUtils.parseFragment(fragment);
        const refExpr = TreeBuilder.build(parsedJson);
        return refExpr;
    }

    /**
     * Create type cast expression
     * @static
     * @param {any} expression expression
     * @param {any} targetType target type
     * @returns {Expression} type cast expression
     * @memberof TransformFactory
     */
    static createTypeCastExpr(expression, targetType) {
        const fragment = FragmentUtils.createExpressionFragment(`(${targetType})${expression.getSource()}`);
        const parsedJson = FragmentUtils.parseFragment(fragment);
        const castExpr = TreeBuilder.build(parsedJson.variable.initialExpression);
        return castExpr;
    }

    /**
     * Create type conversion expression
     * @static
     * @param {any} expression expression
     * @param {any} targetType target type
     * @returns {Expression} type conversion expression
     * @memberof TransformFactory
     */
    static createTypeConversionExpr(expression, targetType) {
        const fragment = FragmentUtils.createExpressionFragment(`<${targetType}>${expression.getSource()}`);
        const parsedJson = FragmentUtils.parseFragment(fragment);
        const castExpr = TreeBuilder.build(parsedJson.variable.initialExpression);
        return castExpr;
    }

    /**
     * Create assignment statement with function invocation.
     * @static
     * @param {any} args arguments
     * @returns assignment statement
     * @memberof TransformFactory
     */
    static createFunctionInvocationAssignmentStatement(args) {
        const { functionDef, packageName, fullPackageName } = args;

        let functionInvokeString = '';
        if (packageName && packageName !== 'Current Package') {
            functionInvokeString = `${packageName}:`;
        }
        const functionParams = functionDef.getParameters().map((param) => {
            return Environment.getDefaultValue(param.type);
        });
        const paramString = functionParams.join(', ');

        functionInvokeString = `${functionInvokeString}${functionDef.getName()}(${paramString})`;

        const varRefNames = args.functionDef.getReturnParams().map((param, index) => {
            return VarPrefix.OUTPUT + index + 1;
        });

        if (varRefNames.length > 0) {
            const varRefListString = `var ${varRefNames.join(', ')}`;
            functionInvokeString = `${varRefListString} = ${functionInvokeString}`;
        }
        const fragment = FragmentUtils.createStatementFragment(functionInvokeString);
        const parsedJson = FragmentUtils.parseFragment(fragment);
        const assignmentNode = TreeBuilder.build(parsedJson);
        assignmentNode.getExpression().setFullPackageName(fullPackageName);
        return assignmentNode;
    }
}

export default TransformFactory;
