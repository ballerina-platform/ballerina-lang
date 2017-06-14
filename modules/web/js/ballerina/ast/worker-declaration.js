/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ASTNode from './node';
import CommonUtils from '../utils/common-utils';

class WorkerDeclaration extends ASTNode {
    constructor(args) {
        super('WorkerDeclaration');
        this._isDefaultWorker = _.get(args, 'isDefaultWorker', false);
        this._reply = _.get(args, 'replyStatement', null);
        this._workerDeclarationStatement = _.get(args, 'declarationStatement', '');
        this._invoker = undefined;
        this._replyReceiver = undefined;
        this._workerName = undefined;
        this._argumentsList = [];
    }

    setIsDefaultWorker(isDefaultWorker, options) {
        if (!_.isNil(isDefaultWorker)) {
            this.setAttribute('_isDefaultWorker', isDefaultWorker, options);
        }
    }

    getReply() {
        return this._reply;
    }

    isDefaultWorker() {
        return this._isDefaultWorker;
    }

    /**
     * Set the worker declaration statement [workerName(message m)]
     * @param {string} declarationStatement
     */
    setWorkerDeclarationStatement(declarationStatement, options) {
        this.setWorkerName(declarationStatement);
    }

    /**
     * Get the worker declaration statement
     * @return {string} _workerDeclarationStatement
     */
    getWorkerDeclarationStatement() {
        return this.getWorkerName();
    }

    getWorkerName() {
        return this._workerName;
    }

    setWorkerName(workerName, options) {
        this.setAttribute('_workerName', workerName, options);
    }

    /**
     * Get the invoker statement
     * @return {ASTNode} invoker statement
     */
    getInvoker() {
        return this._invoker;
    }

    /**
     * Set the invoker statement
     * @param {ASTNode} invoker
     * @param {object} options
     */
    setInvoker(invoker, options) {
        this.setAttribute('_invoker', invoker, options);
    }

    /**
     * Get the invoker statement
     * @return {ASTNode} reply receiver statement
     */
    getReplyReceiver() {
        return this._replyReceiver;
    }

    /**
     * Set the reply receiver statement
     * @param {ASTNode} replyReceiver
     * @param {object} options
     */
    setReplyReceiver(replyReceiver, options) {
        this.setAttribute('_replyReceiver', replyReceiver, options);
    }

    addArgument(paramType, paramName) {
        this.getArgumentsList().push({
            parameter_type: paramType,
            parameter_name: paramName,
        });
    }

    getArgumentsList() {
        return this._argumentsList;
    }

    initFromJson(jsonNode) {
        const self = this;
        const BallerinaASTFactory = this.getFactory();
        this.setWorkerName(jsonNode.worker_name);
        const args = jsonNode.argument_declaration;

        _.forEach(args, (argument) => {
            self.addArgument(argument.parameter_type, argument.parameter_name);
        });

        this.setWorkerDeclarationStatement(this.getWorkerName());

        // TODO: check whether return types are allowed
        _.each(jsonNode.children, (childNode) => {
            let child;
            let childNodeTemp;
            if (childNode.type === 'variable_definition_statement' && !_.isNil(childNode.children[1]) && childNode.children[1].type === 'connector_init_expr') {
                child = BallerinaASTFactory.createConnectorDeclaration();
                childNodeTemp = childNode;
            } else {
                child = BallerinaASTFactory.createFromJson(childNode);
                childNodeTemp = childNode;
            }
            self.addChild(child);
            child.initFromJson(childNodeTemp);
        });
    }

    /**
     * @inheritDoc
     * @override
     */
    generateUniqueIdentifiers() {
        CommonUtils.generateUniqueIdentifier({
            node: this,
            attributes: [{
                defaultValue: 'newWorker',
                setter: this.setWorkerName,
                getter: this.getWorkerName,
                parents: [{
                    // function-def/connector-action/resource
                    node: this.parent,
                    getChildrenFunc: this.parent.getWorkerDeclarations,
                    getter: this.getWorkerName,
                }],
            }],
        });
    }

    /**
     * Returns the list of arguments as a string separated by commas.
     * @return {string} - Arguments as string.
     */
    getArgumentsAsString() {
        let argsAsString = '';
        const args = this.getArgumentsList();
        _.forEach(args, (argument, index) => {
            argsAsString += `${argument.parameter_type} `;
            argsAsString += argument.parameter_name;
            if (args.length - 1 != index) {
                argsAsString += ' , ';
            }
        });
        return argsAsString;
    }
}

export default WorkerDeclaration;

