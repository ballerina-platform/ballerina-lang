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
import BallerinaASTFactory from './../ballerina-ast-factory';
import FragmentUtils from './../../utils/fragment-utils';
import EnableDefaultWSVisitor from './../../visitors/source-gen/enable-default-ws-visitor';

/**
 * class for Transaction aborted statement
 */
class TransactionAbortedStatement extends Statement {
    /**
     * Constructor for TransactionAbortedStatement
     * @constructor
     */
    constructor() {
        super();
        this.type = 'TransactionAbortedStatement';
    }

    /**
     * Get Aborted Statement associated with transaction.
     * @return {AbortedStatement} aborted statement
     * */
    getAbortedStatement() {
        return this.children.find(c => (BallerinaASTFactory.isAbortedStatement(c)));
    }

    /**
     * Get Committed Statement associated with transaction.
     * @return {CommittedStatement} committed statement
     * */
    getCommittedStatement() {
        return this.children.find(c => (BallerinaASTFactory.isCommittedStatement(c)));
    }

    /**
     * Create Aborted Statement.
     * @param {object} args aborted statement arguments
     * @return {AbortedStatement} new aborted statement
     */
    createAbortedStatement(args) {
        const abortedStatement = BallerinaASTFactory.createAbortedStatement(args);
        this.addChild(abortedStatement);
        return abortedStatement;
    }

    /**
     * Create Committed Statement.
     * @param {object} args committed statement arguments
     * @return {CommittedStatement} new committed statement
     */
    createCommittedStatement(args) {
        const committedStatement = BallerinaASTFactory.createCommittedStatement(args);
        this.addChild(committedStatement);
        return committedStatement;
    }

    /**
     * Initialize the node from the node related model json.
     * @param {object} jsonNode - json object for the node.
     * @returns {void}
     */
    initFromJson(jsonNode) {
        const self = this;
        _.each(jsonNode.children, (childNode) => {
            const child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    }

    /**
     * Set the statement from string
     * @param {string} statementString statement string from which the statement is being set
     * @param {function} callback callback function
     * @override
     */
    setStatementFromString(statementString, callback) {
        const fragment = FragmentUtils.createStatementFragment(statementString);
        const parsedJson = FragmentUtils.parseFragment(fragment);

        if ((!_.has(parsedJson, 'error') || !_.has(parsedJson, 'syntax_errors'))
            && _.isEqual(parsedJson.type, 'transaction_aborted_statement')) {
            const nodeToFireEvent = this;
            this.initFromJson(parsedJson);
            nodeToFireEvent.accept(new EnableDefaultWSVisitor());
            // Manually firing the tree-modified event here.
            // TODO: need a proper fix to avoid breaking the undo-redo
            this.trigger('tree-modified', {
                origin: nodeToFireEvent,
                type: 'custom',
                title: 'Modify Transaction Aborted Statement',
                context: nodeToFireEvent,
            });

            if (_.isFunction(callback)) {
                callback({ isValid: true });
            }
        } else if (_.isFunction(callback)) {
            callback({ isValid: false, response: parsedJson });
        }
    }
}

export default TransactionAbortedStatement;
