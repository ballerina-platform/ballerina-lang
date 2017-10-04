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
import NodeFactory from '../../../../../model/node-factory';
import FragmentUtils from '../../../../../utils/fragment-utils';
import TreeBuilder from '../../../../../model/tree-builder';

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
     * Create assignment statement from given args
     * @static
     * @param {any} args arguments
     * @param {Expression} args.expression expression for the assignment
     * @memberof TransformFactory
     */
    static createAssignmentStatement(args) {
        const assignment = NodeFactory.createAssignment({});
        assignment.setExpression(args.expression);
        return assignment;
    }

    /**
     * Create default expression based on argument type
     * @static
     * @param {any} args args
     * @memberof TransformFactory
     */
    static createDefaultExpression(args) {
        // TODO : create default expression based on argument type
    }

}

export default TransformFactory;
