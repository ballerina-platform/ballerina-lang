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
                for (let i = 1; i <= structNodes.length + 1; i++) {
                    if (!names[`${structDefaultName}${i}`]) {
                        node.getName().setValue(`${structDefaultName}${i}`, true);
                        node.setName(node.getName(), true);
                    }
                }
            } else {
                node.getName().setValue(`${structDefaultName}1`, true);
                node.setName(node.getName(), true);
            }
        } else if (this.isEnum(node)) {
            const enumDefaultName = 'name';
            const enumNodes = root.filterTopLevelNodes(this.isEnum);
            const names = {};

            for (let i = 0; i < enumNodes.length; i++) {
                const name = enumNodes[i].getName().value;
                names[name] = name;
            }

            if (enumNodes.length > 0) {
                for (let i = 1; i <= enumNodes.length + 1; i++) {
                    if (!names[`${enumDefaultName}${i}`]) {
                        node.getName().setValue(`${enumDefaultName}${i}`, true);
                        node.setName(node.getName(), true);
                    }
                }
            } else {
                node.getName().setValue(`${enumDefaultName}1`, true);
                node.setName(node.getName(), true);
            }
        }
        return undefined;
    }

    /**
     * Check whether the variable def is an endpoint type definition
     * @param node
     * @return {*}
     */
    isEndpointTypeVariableDef(node) {
        return this.isEndpointType(node);
    }

    /**
     * Return true if the statement is a connector declaration.
     *
     * @param {any} node
     * @returns {boolean} true if the statement is a connector declaration.
     * @memberof TreeUtil
     */
    isConnectorDeclaration(node) {
        if (node && this.isVariableDef(node)) {
            const expression = _.get(node, 'variable.initialExpression');
            return _.get(node, 'variable.typeNode.connector') || (expression && this.isConnectorInitExpr(expression));
        }
        return false;
    }

    /**
     * Get the connector init for the variable definition
     * @param {object} node - variable node
     * @param {object} parent - node's parent
     * @return {*} connector init expression node
     */
    getConnectorInitForVariableDefinition(node, parent) {
        if (_.get(node, 'variable.initialExpression')) {
            return _.get(node, 'variable.initialExpression');
        } else if (this.isConnectorDeclaration(node)) {
            const variableName = _.get(node, 'variable.name.value');
            const parentAssignmentStatements = _.filter(parent.statements, (stmt) => {
                return this.isAssignment(stmt);
            });

            for (let itr = 0; itr < parentAssignmentStatements.length; itr++) {
                const stmt = parentAssignmentStatements[itr];
                if (_.get(stmt, 'expression')
                    && this.isConnectorInitExpr(_.get(stmt, 'expression'))
                    && stmt.variables[0].variableName.value === variableName) {
                    return _.get(stmt, 'expression');
                }
            }
        }

        return undefined;
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

        if (invocationExpression && this.isCheckExpr(invocationExpression)) {
            invocationExpression = invocationExpression.expression;
        }
        return (invocationExpression && this.isInvocation(invocationExpression)
        && invocationExpression.actionInvocation);
    }

    /**
     * Get variable ref of Action Invocation
     * @param {object} node - variable def node object
     * @returns {object} - whether the node is an invocation node
     */
    getVariableReference(node) {
        let invocationExpression;
        if (this.isAssignment(node) || this.isExpressionStatement(node)) {
            invocationExpression = _.get(node, 'expression');
        } else if (this.isVariableDef(node)) {
            invocationExpression = _.get(node, 'variable.initialExpression');
        }

        if (invocationExpression && this.isCheckExpr(invocationExpression)) {
            invocationExpression = invocationExpression.expression;
        }
        return invocationExpression.expression.variableName.value;
    }

    /**
     * Get variable ref
     * @param {object} node - variable def node object
     * @returns {object} - whether the node is an invocation node
     */
    getInvocationSignature(node) {
        let invocationExpression;
        if (this.isAssignment(node) || this.isExpressionStatement(node)) {
            invocationExpression = _.get(node, 'expression');
        } else if (this.isVariableDef(node)) {
            invocationExpression = _.get(node, 'variable.initialExpression');
        }

        if (invocationExpression && this.isCheckExpr(invocationExpression)) {
            invocationExpression = invocationExpression.expression;
        }
        return invocationExpression.getInvocationSignature();
    }

    /**
     * Check whether the node is a client responding statement
     * @param {object} node - variable def or assignment node object
     * @returns {boolean} - whether the node is a client responding statement
     */
    statementIsClientResponder(node) {
        if (this.isReturn(node)) {
            return true;
        }
        let resource = node;
        while (resource) {
            resource = resource.parent;
            if (resource && this.isResource(resource)) {
                // visit and find ACTION Invocation.
                const param = resource.parameters[0];
                if (param === undefined) return false;
                const c = param.name.value;
                const action = node.find((e) => { return this.isInvocation(e); });
                if (action && c === _.get(action, 'expression.variableName.value', '++')) {
                    return true;
                }
                break;
            }
        }
        return false;
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
     * @return {object} - connector init expression
     */
    getConnectorInitFromStatement(node) {
        if (this.isAssignment(node)) {
            return _.get(node, 'expression');
        } else if (this.isVariableDef(node)) {
            return this.getConnectorInitForVariableDefinition(node, node.parent);
        }
        return undefined;
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

    getAllVisibleEndpoints(parent) {
        if (!parent) {
            return [];
        }
        const visibleEndpoints = [];
        if (this.isFunction(parent) || this.isResource(parent) || this.isService(parent)) {
            return visibleEndpoints.concat(_.get(parent, 'endpointNodes'));
        }

        return visibleEndpoints.concat(this.getAllVisibleEndpoints(parent.parent));
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
            if (parsedJson.error) {
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
                statementParentNode.replaceStatements(node, newStatementNode.getFailedBody().getStatements()[0], false);
            } else {
                statementParentNode.replaceStatements(node, newStatementNode, false);
            }
        } else if (node.isExpression) {
            // Get the parent node.
            const expressionParentNode = node.parent;

            source = source.endsWith(';')
                ? source.substr(0, source.length - 1)
                : source;

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
            source = source.replace(/;$/, '');
            if (parent.filterParameters instanceof Function
                && (parent.filterParameters(param => (param.id === node.id)).length > 0)) {
                // Invoke the fragment parser util for parsing argument parameter.
                const parseJson = FragmentUtils.parseFragment(FragmentUtils.createArgumentParameterFragment(source));
                const newParameterNode = TreeBuilder.build(parseJson, parent, parent.kind);
                // clear white space data so it will be formated properly.
                newParameterNode.clearWS();
                // Replace the old parameter with the newly created parameter node.
                parent.replaceParameters(node, newParameterNode, false);
            } else if (parent.filterReturnParameters instanceof Function
                && (parent.filterReturnParameters(returnParam => (returnParam.id === node.id)).length > 0)) {
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
        // const fileData = node.getRoot().getFile();
        // FIXME
        // return getLangServerClientInstance()
        //     .then((client) => {
        //         const position = node.parent.getPosition() || node.getPosition();
        //         const options = {
        //             textDocument: fileData.content,
        //             position: {
        //                 line: position.endLine,
        //                 character: position.endColumn,
        //             },
        //             fileName: fileData.name,
        //             filePath: fileData.path,
        //             fullPath: fileData.fullPath,
        //             packageName: fileData.packageName,
        //         };

        //         return client.getCompletions(options);
        //     })
        //     .then((response) => {
        //         if (!response || response.error) {
        //             return Array(numberOfVars).fill().map((el, index) => (`${varPrefix}${index + 1}`));
        //         }

        //         const varNameRegex = new RegExp(varPrefix + '[\\d]*');
        //         const completions = response.result.left.filter((completionItem) => {
        //             // all variables have type as 9 as per the declaration in lang server
        //             return (completionItem.kind === 9) && varNameRegex.test(completionItem.label);
        //         });
        //         const tempVarSuffixes = completions.map((varName) => {
        //             return Number.parseInt(varName.label.substring(varPrefix.length), 10) || 0;
        //         });

        //         tempVarSuffixes.sort((a, b) => (a - b));
        //         const varNames = [];
        //         for (let i = 0; i < numberOfVars; i++) {
        //             varNames.push(`${varPrefix}${(tempVarSuffixes[tempVarSuffixes.length - 1] || 0) + i + 1}`);
        //         }
        //         return varNames;
        //     });
        return varPrefix + 1;
    }

    /**
     * @description Get package name from astRoot
     * @param {ASTModel} astRoot
     * @returns
     * @memberof TreeUtil
     */
    getPackageNameString(astRoot) {
        if (!astRoot) {
            return '.';
        }
        const packageDeclaration = astRoot.filterTopLevelNodes({ kind: 'PackageDeclaration' });
        if (!packageDeclaration || !packageDeclaration[0]) {
            return '.';
        }
        return packageDeclaration[0].getPackageNameString();
    }

    /**
     * @description check whether provided node is a main function.
     * @param {Node} node - node to check whether it is a main function.
     * @return {boolean} is given function is a main function.
     * */
    isMainFunction(node) {
        return (node.kind === 'Function'
            && (node.getName().value === 'main' || node.getName() === 'main')
            && node.getParameters().length === 1
            && ((node.getReturnParameters && node.getReturnParameters().length === 0) ||
                (node.getReturnParams && node.getReturnParams().length === 0))
            && ((node.getParameters()[0].typeNode && node.getParameters()[0].typeNode.kind === 'ArrayType'
                && node.getParameters()[0].typeNode.elementType.typeKind === 'string')
                || node.getParameters()[0].type === 'string[]'));
    }


    /**
     * Get client invokable parent construct of the given node.
     * These include : function, action and resource
     * @param {Node} node node
     * @return {Node} parent client invocation node.
     */
    getClientInvokableParentNode(node) {
        const parent = node.parent;
        if (this.isAction(parent) || this.isFunction(parent) || this.isResource(parent)) {
            return parent;
        }
        if (this.isCompilationUnit(node)) {
            return undefined;
        }
        return this.getClientInvokableParentNode(node.parent);
    }

    /**
     * Check if function is an initialization function.
     *
     * @param {any} node tree node.
     * @returns {bool} true if it is a init function.
     * @memberof TreeUtil
     */
    isInitFunction(node) {
        const regex = /\.\<init\>$/g;
        return regex.test(node.name.value);
    }

     /**
     * Provides available endpoints for provided models scope
     * @param {object} model current model object
     * @return {array}  available endpoints
     */
    getCurrentEndpoints(model) {
        if (this.isResource(model) || this.isFunction(model)) {
            return model.getEndpointNodes();
        } else if (model.parent) {
            return this.getCurrentEndpoints(model.parent);
        } else {
            return [];
        }
    }

    /**
     * Check if the block is part of a lifeline in diagram.
     *
     * @param {any} node tree node.
     * @returns {bool} true if block is on a lifeline.
     * @memberof TreeUtil
     */
    isLineBlock(node) {
        if (this.isFunction(node.parent) ||
            this.isResource(node.parent) ||
            this.isAction(node.parent)) {
            if (this.isInitFunction(node.parent)) {
                return false;
            }
            if (node.parent.workers.length > 0) {
                if (node.parent.body) {
                    return !(node.parent.body.id === node.id);
                }
            }
        }
        if (this.isChildOfTransformer(node)) {
            return false;
        }
        return true;
    }

    /**
     *  Check if block node has statements.
     *
     * @param {any} node tree node.
     * @returns {bool} true if block is empty
     * @memberof TreeUtil
     */
    isEmptyBlock(node) {
        if (this.isBlock(node)) {
            return node.statements.length === 0;
        } else {
            return false;
        }
    }

    /**
     * Check if the block node is an else blocl
     *
     * @param {any} node tree node.
     * @returns {bool} true if block is an else block;
     * @memberof TreeUtil
     */
    isElseBlock(node) {
        if (this.isIf(node.parent)
            && node.parent.elseStatement
            && this.isBlock(node.parent.elseStatement)) {
            return node.parent.elseStatement.id === node.id;
        }
        return false;
    }

    isFinally(node) {
        if (this.isTry(node.parent)
            && node.parent.finallyBody
            && node.parent.finallyBody.id === node.id) {
            return true;
        }
        return false;
    }

    isChildOfTransformer(node) {
        if (node.parent) {
            if (this.isTransformer(node.parent)) {
                return true;
            } else {
                return this.isChildOfTransformer(node.parent);
            }
        }
        return false;
    }

    /**
     * Generate default name for workers
     * @param {Node} parent - parent node.
     * @param {Node} node - current node.
     * @return {Object} undefined if unsuccessful.
     * */
    generateWorkerName(parent, node) {
        const workerDefaultName = 'worker';
        const workers = parent.getWorkers();
        const names = {};
        for (let i = 0; i < workers.length; i++) {
            const name = workers[i].getName().value;
            names[name] = name;
        }

        if (workers.length > 0) {
            for (let j = 1; j <= workers.length; j++) {
                if (!names[`${workerDefaultName}${j}`]) {
                    node.getName().setValue(`${workerDefaultName}${j}`, true);
                    node.setName(node.getName(), false);
                    break;
                }
            }
        } else {
            node.getName().setValue(`${workerDefaultName}1`, true);
            node.setName(node.getName(), false);
        }
        return undefined;
    }

    /**
     * Generate default name for endpoints
     * @param {Node} parent - parent node.
     * @param {Node} node - current node.
     * */
    generateEndpointName(parent, node) {
        const defaultName = 'ep';
        let defaultIndex = 0;
        const names = this.getCurrentEndpoints(parent)
                        .map((endpoint) => { return endpoint.name.getValue(); })
                        .sort();
        names.every((endpoint, i) => {
            if (names[i] !== defaultName + (i + 1)) {
                defaultIndex = i + 1;
                return false;
            } else {
                return true;
            }
        });
        node.name
        .setValue(`${defaultName + (defaultIndex === 0 ? names.length + 1 : defaultIndex)}`, true);
    }

    /**
     * Generate default name for endpoints
     * @param {Node} node - current node.
     * @param {string} defaultName - default variable name.
     * @param {number} indexIncreament - increase variable name index.
     * @return {string} undefined if unsuccessful.
     * */
    generateVariableName(node, defaultName, indexIncreament = 0) {
        let defaultIndex = 0;
        const names = [];
        node.getStatements().forEach((currentStatement) => {
            if (this.isVariableDef(currentStatement)) {
                if (currentStatement.getVariableName().getValue().startsWith(defaultName)) {
                    names.push(currentStatement.getVariableName().getValue());
                }
            } else if (this.isAssignment(currentStatement)) {
                currentStatement.getVariables().forEach((variable) => {
                    if (variable.getVariableName().getValue().startsWith(defaultName)) {
                        names.push(variable.getVariableName().getValue());
                    }
                });
            }
        });
        names.every((endpoint, i) => {
            if (names[i] !== defaultName + (i + 1)) {
                defaultIndex = i + 1;
                return false;
            } else {
                return true;
            }
        });
        return `${defaultName
            + (defaultIndex === 0 ? names.length + 1 + indexIncreament : defaultIndex + indexIncreament)}`;
    }

    getAllEndpoints(node) {
        let endpoints = [];
        if (node.kind === 'CompilationUnit') {
            endpoints = endpoints.concat(_.filter(node.topLevelNodes, (topLevelNode) => {
                return topLevelNode.kind === 'Endpoint';
            }));
        } else if (node.endpointNodes) {
            endpoints = endpoints.concat(node.endpointNodes);
        }
        if (node.parent) {
            endpoints = endpoints.concat(this.getAllEndpoints(node.parent));
        }
        return endpoints;
    }
}

export default new TreeUtil();
