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
import AbstractTreeUtil from './abstract-tree-util';

class TreeUtil extends AbstractTreeUtil {

    getFullPackageName(node) {
        const root = node.getRoot();
        if (!root) {
            return '';
        }
        const packageAlias = node.getPackageAlias();
        if (!packageAlias) {
            return '';
        }
        const importNode = root.filterTopLevelNodes(this.isImport).find((im) => {
            return im.getAlias().value === packageAlias.value;
        });
        let fullPackageName = '';
        if (importNode) {
            fullPackageName = importNode.getPackageName().map((pkgName) => {
                return pkgName.value;
            }).join('.');
        }
        return fullPackageName;
    }

    /**
     * Generate default name for root level statements.
     * @param {Node} root - root node.
     * @param {Node} node - current node.
     * @return {Object} undefined if unsuccessful.
     * */
    generateDefaultName(root, node) {
        if (!root) {
            return undefined;
        }

        if (this.isFunction(node) && node.getName().value !== 'main') {
            const functionDefaultName = 'function';
            const functionNodes = root.filterTopLevelNodes(this.isFunction);
            const names = {};
            for (let i = 0; i < functionNodes.length; i++) {
                const name = functionNodes[i].getName().value;
                names[name] = name;
            }

            if (functionNodes.length > 0) {
                for (let i = 1; i <= functionNodes.length + 1; i++) {
                    if (!names[`${functionDefaultName}${i}`]) {
                        node.getName().setValue(`${functionDefaultName}${i}`, true);
                        node.setName(node.getName(), true);
                        break;
                    }
                }
            } else {
                node.getName().setValue(`${functionDefaultName}1`, true);
                node.setName(node.getName(), true);
            }
        }
        return undefined;
    }


    /**
     * Return true if the statement is a connector declaration.
     *
     * @param {any} node
     * @returns {boolean} true if the statement is a connector declaration.
     * @memberof TreeUtil
     */
    isConnectorDeclaration(node) {
        const expression = _.get(node, 'variable.initialExpression');
        return (expression && this.isConnectorInitExpr(expression));
    }

    /**
     * Check whether the node is an action invocation expression
     * @param {object} node - variable def node object
     * @returns {boolean} - whether the node is an invocation node
     */
    variableDefIsInvocation(node) {
        const initialExpression = _.get(node, 'variable.initialExpression');
        return (initialExpression && this.isInvocation(initialExpression));
    }

    /**
     * Get the variable definition by traversing the parent nodes
     * @param {object} parent - parent block node
     * @param {string} name - variable name
     * @return {object} variable definition node
     */
    getVariableDefByName(parent, name) {
        if (!parent) {
            return undefined;
        }
        const variableDef = _.find(parent.statements, (statement) => {
            if (this.isVariableDef(statement)) {
                return statement.variable.name.value === name;
            } else {
                return false;
            }
        });

        if (!variableDef) {
            return this.getVariableDefByName(parent.parent, name);
        }

        return variableDef;
    }

    getAllVisibleConnectorDeclarations(parent) {
        if (!parent) {
            return [];
        }
        let filteredItems = [];
        const statements = parent.statements;
        if (statements) {
            filteredItems = _.filter(statements, (stmt) => {
                return this.isConnectorDeclaration(stmt);
            });
        }
        return filteredItems.concat(this.getAllVisibleConnectorDeclarations(parent.parent));
    }

    updateActionInvocation() {
        console.log("TESTTTTT");
    }
}

export default new TreeUtil();
