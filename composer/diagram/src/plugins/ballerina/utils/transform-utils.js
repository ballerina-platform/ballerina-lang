/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
import TreeUtil from '../model/tree-util';

export const ExpressionType = {
    ERROR: 'error',
    PLACEHOLDER: 'placeholder',
    TEMPVAR: 'tempvar',
    DIRECT: 'direct',
    CONST: 'const',
};

export const VarPrefix = {
    VAR: 'var',
    NEWVAR: 'newVar',
};

/**
 * Transform utilities class
 */
export default class TransformUtils {
    /**
     * Get a new name for temporary variable
     * @param {string} varPrefix prefix for the variable
     * @param {TransformNode} transformNode transform node for which the new temp variable is created
     * @returns {Expression} temporary var expression
     * @memberof TransformNodeMapper
     */
    static getNewTempVarName(transformNode, varPrefix = VarPrefix.VAR) {
        const varNameRegex = new RegExp(varPrefix + '[\\d]*');
        const tempVarSuffixes = [];

        transformNode.body.getStatements().forEach((stmt) => {
            let variables = [];
            if (TreeUtil.isAssignment(stmt)) {
                variables = stmt.getVariables();
            } else if (TreeUtil.isVariableDef(stmt)) {
                variables.push(stmt.getVariable());
            }

            variables.forEach((varExp) => {
                const expStr = varExp.getSource();
                if (varNameRegex.test(expStr)) {
                    const index = Number.parseInt(expStr.substring(varPrefix.length + 1), 10) || 0;
                    tempVarSuffixes.push(index);
                }
            });
        });

        // numeric sort by difference
        tempVarSuffixes.sort((a, b) => a - b);
        const index = (tempVarSuffixes[tempVarSuffixes.length - 1] || 0) + 1;
        return varPrefix + index;
    }
}
