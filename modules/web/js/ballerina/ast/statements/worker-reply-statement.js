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
import Statement from './statement';
import BallerinaASTFactory from './../../ast/ballerina-ast-factory';

/**
 * Class to represent worker reply statement in ballerina.
 * @constructor
 */
class WorkerReplyStatement extends Statement {
    constructor(args) {
        super('WorkerReplyStatement');
        this._source = _.get(args, 'source');
        this._destination = _.get(args, 'destination');
        this._expressionList = _.get(args, 'expressionList', []);
        this._replyStatement = _.get(args, 'replyStatement', 'm1,m2 <- newWorker1');
        this._workerName = _.get(args, 'workerName', 'newWorker1');
    }

    setSource(source) {
        this._source = source;
    }

    getSource() {
        return this._source;
    }

    setDestination(destination) {
        this._destination = destination;
    }

    getDestination() {
        return this._destination;
    }

    /**
     * Set destination worker Name
     * @param {string} workerName
     */
    setWorkerName(workerName) {
        this._workerName = workerName;
    }

    /**
     * Get destination worker name
     * @returns {string} _workerName - destination worker name
     */
    getWorkerName() {
        return this._workerName;
    }

    setReplyStatement(replyStatement) {
        this._workerName = (replyStatement.split('<-')[1]).trim();
        this.setAttribute('_replyStatement', replyStatement);
    }

    getReplyStatement() {
        return this._replyStatement;
    }

    addToExpressionList(expression) {
        this._expressionList.push(expression);
    }

    getExpressionList() {
        return this._expressionList;
    }

    canBeAChildOf(node) {
        return this.getFactory().isResourceDefinition(node)
            || this.getFactory().isFunctionDefinition(node)
            || this.getFactory().isWorkerDeclaration(node)
            || this.getFactory().isConnectorAction(node)
            || (this.getFactory().isStatement(node) && !node._isChildOfWorker);
    }

    /**
     * initialize from json
     * @param jsonNode
     */
    initFromJson(jsonNode) {
        var workerName = jsonNode.worker_name;
        const self = this;
        const expressionList = jsonNode.expression_list;
        let expressionString = '';

        for (let itr = 0; itr < expressionList.length; itr ++) {
            const expressionNode = BallerinaASTFactory.createFromJson(expressionList[itr].expression[0]);
            expressionNode.initFromJson(expressionList[itr].expression[0]);
            self.addToExpressionList(expressionNode);
            expressionString += expressionNode.getExpression();
            if (itr !== expressionList.length - 1) {
                expressionString += ',';
            }
        }

        var replyStatement = expressionString + ' <- ' + workerName;
        this.setReplyStatement(replyStatement);
        var workerInstance = _.find(this.getParent().getChildren(), function (child) {
            return self.getFactory().isWorkerDeclaration(child) && !child.isDefaultWorker() && child.getWorkerName() === workerName;
        });
        this.setDestination(workerInstance);
        this.setWorkerName(workerName);
    }
}

export default WorkerReplyStatement;

