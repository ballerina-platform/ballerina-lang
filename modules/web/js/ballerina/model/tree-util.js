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
import TreeBuilder from './tree-builder';
import FragmentUtils from '../utils/fragment-utils';
import { getLangServerClientInstance } from 'langserver/lang-server-client-controller';

/**
 * Util class for tree related functionality.
 * @class TreeUtil
 * @extends AbstractTreeUtil
 * */
class TreeUtil extends AbstractTreeUtil {
    /**
     * Get the full package name for the given node.
     * @param {Node} node - current node.
     * @return {String} full package name.
     * */
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
     * @param {CompilationUnitNode} root - root node.
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

            // Get the existing function names.
            const names = {};
            for (let i = 0; i < functionNodes.length; i++) {
                const name = functionNodes[i].getName().value;
                names[name] = name;
            }

            if (functionNodes.length > 0) {
                // Set the function name.
                for (let i = 1; i <= functionNodes.length + 1; i++) {
                    if (!names[`${functionDefaultName}${i}`]) {
                        node.getName().setValue(`${functionDefaultName}${i}`, true);
                        node.setName(node.getName(), true);
                        break;
                    }
                }
            } else {
                // If this is the first function set name to be function1.
                node.getName().setValue(`${functionDefaultName}1`, true);
                node.setName(node.getName(), true);
            }
        } else if (this.isService(node)) {
            const serviceDefaultName = 'service';
            const serviceNodes = root.filterTopLevelNodes(this.isService);
            const names = {};
            for (let i = 0; i < serviceNodes.length; i++) {
                const name = serviceNodes[i].getName().value;
                names[name] = name;
            }

            if (serviceNodes.length > 0) {
                for (let i = 1; i <= serviceNodes.length + 1; i++) {
                    if (!names[`${serviceDefaultName}${i}`]) {
                        node.getName().setValue(`${serviceDefaultName}${i}`, true);
                        node.setName(node.getName(), true);
                        break;
                    }
                }
            } else {
                node.getName().setValue(`${serviceDefaultName}1`, true);
                node.setName(node.getName(), true);
            }
        } else if (this.isConnector(node)) {
            const connectorDefaultName = 'ClientConnector';
            const connectorNodes = root.filterTopLevelNodes(this.isConnector);
            const names = {};
            for (let i = 0; i < connectorNodes.length; i++) {
                const name = connectorNodes[i].getName().value;
                names[name] = name;
            }

            if (connectorNodes.length > 0) {
                for (let i = 1; i <= connectorNodes.length + 1; i++) {
                    if (!names[`${connectorDefaultName}${i}`]) {
                        node.getName().setValue(`${connectorDefaultName}${i}`, true);
                        node.setName(node.getName(), true);
                        break;
                    }
                }
            } else {
                node.getName().setValue(`${connectorDefaultName}1`, true);
                node.setName(node.getName(), true);
            }
        } else if (this.isStruct(node)) {
            const structDefaultName = 'Struct';
            const structNodes = root.filterTopLevelNodes(this.isStruct);
            const names = {};

            for (let i = 0; i < structNodes.length; i++) {
                const name = structNodes[i].getName().value;
                names[name] = name;
            }

            if (structNodes.length > 0) {
                for (let i = 1; i <= structNodes.length; i++) {
                    if (!names[`${structDefaultName}${i}`]) {
                        node.getName().setValue(`${structDefaultName}${i}`, true);
                        node.setName(node.getName(), true);
                    }
                }
            } else {
                node.getName().setValue(`${structDefaultName}1`, true);
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
    statementIsInvocation(node) {
        let invocationExpression;
        if (this.isAssignment(node) || this.isExpressionStatement(node)) {
            invocationExpression = _.get(node, 'expression');
        } else if (this.isVariableDef(node)) {
            invocationExpression = _.get(node, 'variable.initialExpression');
        }
        return (invocationExpression && this.isInvocation(invocationExpression)
        && invocationExpression.invocationType === 'ACTION');
    }

    /**
     * Sync the new node's invocation type
     * @param {object} originalNode
     * @param newNode
     */
    syncInvocationType(originalNode, newNode) {
        let invocationExpression;
        if (this.isAssignment(originalNode) || this.isExpressionStatement(originalNode)) {
            invocationExpression = _.get(originalNode, 'expression');
            newNode.expression.invocationType = invocationExpression.invocationType;
        } else if (this.isVariableDef(originalNode)) {
            invocationExpression = _.get(originalNode, 'variable.initialExpression');
            newNode.variable.initialExpression.invocationType = invocationExpression.invocationType;
        }
    }

    /**
     * Get the connector init expression from the statement
     * @param {object} node - statement node
     * @return {boolean} - true | false
     */
    getConnectorInitFromStatement(node) {
        if (this.isAssignment(node)) {
            return _.get(node, 'expression');
        } else if (this.isVariableDef(node)) {
            return _.get(node, 'variable.initialExpression');
        }
        return false;
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

    /**
     * Change the invocation endpoint. This is the callback function for endpoint dropdown click
     * @param {object} node - invocation node
     * @param {string} newEp - new endpoint string
     */
    changeInvocationEndpoint(node, newEp) {
        // TODO: will be replaced accordingly with the new set source
        if (this.isVariableDef(node)) {
            _.set(node, 'variable.initialExpression.expression.variableName.value', newEp);
        } else if (this.isAssignment(node)) {
            _.set(node, 'expression.expression.variableName.value', newEp);
        }
    }

    /**
     * Get the receiver statement sending data to the given worker
     * @param {object} worker - worker node
     * @return {*} index of the statement
     */
    getReceiverForSender(worker, sender) {
        const statements = worker.body.statements;
        const receiverIndex = _.findIndex(statements, (stmt) => {
            return this.isWorkerReceive(stmt) && stmt.workerName.value === sender.name.value;
        });

        if (receiverIndex >= 0) {
            return statements[receiverIndex];
        } else {
            return undefined;
        }
    }

    /**
     * Get the sender statement receiving data from the given worker
     * @param {object} worker - worker node
     * @return {*} index of the statement
     */
    getSenderForReceiver(worker) {
        const statements = worker.body.statements;
        const receiverIndex = _.findIndex(statements, (stmt) => {
            return this.isWorkerSend(stmt);
        });

        if (receiverIndex >= 0) {
            return statements[receiverIndex];
        } else {
            return undefined;
        }
    }

    /**
     * Get the worker by name
     * @param {string} workerName - worker name
     * @param {array} workerList - worker list
     * @return {object} worker node
     */
    getWorkerByName(workerName, workerList) {
        const index = _.findIndex(workerList, (worker) => {
            return worker.name.value === workerName;
        });

        return index >= 0 ? workerList[index] : undefined;
    }

    /**
     * Set the source of the current node.
     * @param {Node} node - Node for setting source.
     * @param {string|array} source - set source for the node.
     * */
    setSource(node, source, ballerinaFileEditor) {
        /* TODO: handle expression, argument parameter and return parameter. */
        // check node kind.
        if (node.isStatement) {
            // get the parent of the node.
            const statementParentNode = node.parent;
            let fragment;
            // If node kind is retry
            if (node.kind === 'Retry') {
                fragment = FragmentUtils.createTransactionFailedFragment(source);
            } else {
                fragment = FragmentUtils.createStatementFragment(source);
            }

            // invoke the fragment util for the coresponding kind.
            const parsedJson = FragmentUtils.parseFragment(fragment);
            // show an error and skip the setSource method when user provides an unparsable content.
            if (parsedJson.error){
                ballerinaFileEditor.context.alert.showError('Invalid content provided !');
                return;
            }
            const newStatementNode = TreeBuilder.build(parsedJson, statementParentNode, statementParentNode.kind);
            // clear white space data so it will be formated properly.
            newStatementNode.clearWS();

            if (this.statementIsInvocation(node)) {
                this.syncInvocationType(node, newStatementNode);
            }

            // replace the old node with new node.
            if (this.isService(statementParentNode)) {
                statementParentNode.replaceVariables(node, newStatementNode, false);
            } else if (this.isConnector(statementParentNode)) {
                statementParentNode.replaceVariableDefs(node, newStatementNode, false);
            } else if (this.isTransaction(newStatementNode)) {
                statementParentNode.parent.setCondition(newStatementNode.getCondition());
                statementParentNode.replaceStatements(node, newStatementNode.getFailedBody().getStatements[0], false);
            } else {
                statementParentNode.replaceStatements(node, newStatementNode, false);
            }
        } else if (node.isExpression) {
            // Get the parent node.
            const expressionParentNode = node.parent;

            // invoke the fragment util and get the new node.
            const parseJson = FragmentUtils.parseFragment(FragmentUtils.createExpressionFragment(source));
            const newExpressionNode = TreeBuilder.build(parseJson, expressionParentNode, expressionParentNode.kind);
            // clear white space data so it will be formated properly.
            newExpressionNode.clearWS();
            // Get the initial expression from returning node.
            if (newExpressionNode && newExpressionNode.variable.initialExpression) {
                newExpressionNode.variable.initialExpression.parent = expressionParentNode;
                // Set the condition using new node.
                expressionParentNode.setCondition(newExpressionNode.variable.initialExpression);
            }
        } else {
            const parent = node.parent;
            if (parent.filterParameters instanceof Function
                && parent.filterParameters(param => (param.id === node.id))) {
                // Invoke the fragment parser util for parsing argument parameter.
                const parseJson = FragmentUtils.parseFragment(FragmentUtils.createArgumentParameterFragment(source));
                const newParameterNode = TreeBuilder.build(parseJson, parent, parent.kind);
                // clear white space data so it will be formated properly.
                newParameterNode.clearWS();
                // Replace the old parameter with the newly created parameter node.
                parent.replaceParameters(node, newParameterNode, false);
            } else if (parent.filterReturnParameters instanceof Function
                && parent.filterReturnParameters(returnParam => (returnParam.id === node.id))) {
                // Invoke the fragment parser util for parsing return parameter.
                const parseJson = FragmentUtils.parseFragment(FragmentUtils.createReturnParameterFragment(source));
                const newReturnParameterNode = TreeBuilder.build(parseJson, parent, parent.kind);
                // clear white space data so it will be formated properly.
                newReturnParameterNode.clearWS();
                // Replace the old parameter with the newly created parameter node.
                parent.replaceReturnParameters(node, newReturnParameterNode, false);
            }
        }
    }

    getNewTempVarName(node, varPrefix, numberOfVars = 1) {
        const fileData = node.getRoot().getFile();
        return getLangServerClientInstance()
            .then((client) => {
                const position = node.parent.getPosition() || node.getPosition();
                const options = {
                    textDocument: fileData.content,
                    position: {
                        line: position.endLine,
                        character: position.endColumn,
                    },
                    fileName: fileData.name,
                    filePath: fileData.path,
                    packageName: fileData.packageName,
                };

                return client.getCompletions(options);
            })
            .then((response) => {
                if (!response) {
                    return Array(numberOfVars).fill().map((el, index) => (`${varPrefix}${index + 1}`));
                }

                const varNameRegex = new RegExp(varPrefix + '[\\d]*');
                const completions = response.result.filter((completionItem) => {
                    // all variables have type as 9 as per the declaration in lang server
                    return (completionItem.kind === 9) && varNameRegex.test(completionItem.label);
                });
                const tempVarSuffixes = completions.map((varName) => {
                    return Number.parseInt(varName.label.substring(varPrefix.length), 10) || 0;
                });

                tempVarSuffixes.sort((a, b) => (a - b));
                const varNames = [];
                for (let i = 0; i < numberOfVars; i++) {
                    varNames.push(`${varPrefix}${(tempVarSuffixes[tempVarSuffixes.length - 1] || 0) + i + 1}`);
                }
                return varNames;
            });
    }
}

export default new TreeUtil();
