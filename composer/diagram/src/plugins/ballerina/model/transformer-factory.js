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

import log from 'log';
import FragmentUtils from '../utils/fragment-utils';
import TreeBuilder from './tree-builder';
import TreeUtil from './tree-util';
import Environment from '../env/environment';

/**
 * Transform factory class.
 * @class TransformerFactory
 */
class TransformerFactory {
    /**
     * Create for expression for fields
     * @param {string} name expression name
     * @param {string} type expression type
     * @return {object} expression object
     */
    static createVariableRefExpression(name, type) {
        const fragment = FragmentUtils.createExpressionFragment(name);
        const parsedJson = FragmentUtils.parseFragment(fragment);
        const refExpr = TreeBuilder.build(parsedJson.variable.initialExpression);
        if (type) {
            refExpr.symbolType = [type];
        } else {
            log.warn('Type unknown');
        }
        refExpr.clearWS();
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
        const fragment = FragmentUtils.createStatementFragment(`${type} ${name} = ${value || ''};`);
        const parsedJson = FragmentUtils.parseFragment(fragment);
        const varDef = TreeBuilder.build(parsedJson);
        varDef.clearWS();
        return varDef;
    }

    /**
     * create a variable for provided name and type
     * @param  {string} name variable name
     * @param  {string} type variable type
     * @return {object}     created variable node
     */
    static createVariable(name, type) {
        const fragment = FragmentUtils.createStatementFragment(`${type} ${name};`);
        const parsedJson = FragmentUtils.parseFragment(fragment);
        const variable = TreeBuilder.build(parsedJson).getVariable();
        variable.clearWS();
        return variable;
    }

    /**
     * create a iterable operation for provided name and type
     * @param {string} name collection name
     * @param {string} type iterable operation type
     * @param {string} sourceType source collection type
     * @param {boolean} isLamda is given iterable operation has a lambda function
     * @return {object} created iterable operation node
     */
    static createIterableOperation(name, type, sourceType, isLamda) {
        let lambdaFunction = '';
        if (isLamda) {
            lambdaFunction = `function(${sourceType} item)(boolean){ return true; }`;
        }
        const fragment = FragmentUtils.createExpressionFragment(`${name}.${type}(${lambdaFunction})`);
        const parsedJson = FragmentUtils.parseFragment(fragment);
        const exp = TreeBuilder.build(parsedJson).getVariable().getInitialExpression();
        TransformerFactory.updateItreableOperation(exp, sourceType);
        exp.clearWS();
        return exp;
    }

    /**
     * Update iterable flag recursive for chained iterable operations
     * @param {object} node expression node
     * @param {*} sourceType source collection type
     */
    static updateItreableOperation(node, sourceType) {
        node.iterableOperation = true;
        node.symbolType[0] = sourceType;
        if (TreeUtil.isInvocation(node.getExpression())) {
            TransformerFactory.updateItreableOperation(node.getExpression(), sourceType);
        }
    }

    /**
     * Create default expression based on argument type
     * @static
     * @param {any} type type
     * @memberof TransformerFactory
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
        exp.clearWS();
        return exp;
    }

    /**
     * Create default expression based on operator type
     * @static
     * @param {Expression} operator operator
     * @param {[string]} operandTypes operator types of the operator
     * @param {int} operandIndex index of the operand that needs the default expression
     * @memberof TransformerFactory
     * @return {object} expression object
     */
    static createDefaultOperandExpression(operator, operandTypes, operandIndex) {
        const operatorLattice = Environment.getOperatorLattice();
        const operatorKind = operator.getOperatorKind();

        let operandType = 'null';

        let compatibility;
        if (TreeUtil.isTernaryExpr(operator)) {
            compatibility = operatorLattice.getCompatibleTernaryTypes(operatorKind, operandIndex);
        } else if (TreeUtil.isBinaryExpr(operator)) {
            compatibility = operatorLattice.getCompatibleBinaryTypes(operatorKind, operandTypes, operandIndex);
        } else if (TreeUtil.isUnaryExpr(operator)) {
            compatibility = operatorLattice.getCompatibleUnaryTypes(operatorKind, operandTypes, operandIndex);
        }

        if (compatibility && compatibility.length > 0) {
            operandType = compatibility[0];
        }

        return TransformerFactory.createDefaultExpression(operandType);
    }

    /**
     * Create for statement from statement
     * @param  {string} statement  statement string
     * @return {object} statement object
     */
    static createVariableDefFromStatement(statement) {
        const fragment = FragmentUtils.createStatementFragment(`${statement};`);
        const parsedJson = FragmentUtils.parseFragment(fragment);
        const refExpr = TreeBuilder.build(parsedJson);
        refExpr.clearWS();
        return refExpr;
    }

    /**
     * Create type cast expression
     * @static
     * @param {any} expression expression
     * @param {any} targetType target type
     * @returns {Expression} type cast expression
     * @memberof TransformerFactory
     */
    static createTypeCastExpr(expression, targetType) {
        const fragment = FragmentUtils.createExpressionFragment(`(${targetType})${expression.getSource()}`);
        const parsedJson = FragmentUtils.parseFragment(fragment);
        const castExpr = TreeBuilder.build(parsedJson.variable.initialExpression);
        castExpr.clearWS();
        return castExpr;
    }

    /**
     * Create type conversion expression
     * @static
     * @param {any} expression expression
     * @param {any} targetType target type
     * @returns {Expression} type conversion expression
     * @memberof TransformerFactory
     */
    static createTypeConversionExpr(expression, targetType) {
        const fragment = FragmentUtils.createExpressionFragment(`<${targetType}>${expression.getSource()}`);
        const parsedJson = FragmentUtils.parseFragment(fragment);
        const conExpr = TreeBuilder.build(parsedJson.variable.initialExpression);
        conExpr.clearWS();
        return conExpr;
    }

    static createOperatorAssignmentStatement(args = {}) {
        const statement = `var __output = ${args.defaultExpression};`;
        const fragment = FragmentUtils.createStatementFragment(statement);
        const parsedJson = FragmentUtils.parseFragment(fragment);
        const tree = TreeBuilder.build(parsedJson);
        tree.clearWS();
        return tree;
    }

    /**
     * Create Transformer conversion expression for given transformer and expression
     * @param {string} targetType statement target type
     * @param {object} expression statement expression
     * @param {strong} transformerName transformer name
     * @param {[object]} parameters transformer parameters
     */
    static createTransformerConversion(targetType, expression, transformerName, parameters) {
        let params = '';
        parameters.forEach((param, i) => {
            params += (i === 0 ? '' : ',') + param.getName().getValue();
        });
        transformerName = transformerName ? `, ${transformerName}(${params})` : '';
        const fragment = FragmentUtils.createExpressionFragment(`<${targetType}${transformerName}>${expression}`);
        const parsedJson = FragmentUtils.parseFragment(fragment);
        const conExpr = TreeBuilder.build(parsedJson.variable.initialExpression);
        conExpr.clearWS();
        return conExpr;
    }
}

export default TransformerFactory;
