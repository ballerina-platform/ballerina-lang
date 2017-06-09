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

class TransactionAbortedStatement extends Statement {
    constructor(args) {
        super();
        this.type = "TransactionAbortedStatement";
    }

    /**
     * Get Aborted Statement associated with transaction.
     * @return {AbortedStatement}
     * */
    getAbortedStatement() {
        return this.children.find(c => (BallerinaASTFactory.isAbortedStatement(c)));
    }

    /**
     * Get Committed Statement associated with transaction.
     * @return {CommittedStatement}
     * */
    getCommittedStatement() {
        return this.children.find(c => (BallerinaASTFactory.isCommittedStatement(c)));
    }

    /**
     * Create Aborted Statement.
     * @param {object} args
     * @return {AbortedStatement}
     * */
    createAbortedStatement(args) {
        let abortedStatement = BallerinaASTFactory.createAbortedStatement(args);
        this.addChild(abortedStatement);
        return abortedStatement;
    }

    /**
     * Create Committed Statement.
     * @param {object} args
     * @return {CommittedStatement}
     * */
    createCommittedStatement(args) {
        let committedStatement = BallerinaASTFactory.createCommittedStatement(args);
        this.addChild(committedStatement);
        return committedStatement;
    }

    /**
     * Initialize the node from the node related model json.
     * @param {object} jsonNode - json object for the node.
     * */
    initFromJson(jsonNode) {
        let self = this;
        _.each(jsonNode.children, function (childNode) {
            let child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    }

    /**
     * Set the statement from string
     * @param {string} statementString
     * @param {function} callback
     * @override
     */
    setStatementFromString(statementString, callback) {
        const fragment = FragmentUtils.createStatementFragment(statementString);
        const parsedJson = FragmentUtils.parseFragment(fragment);

        if ((!_.has(parsedJson, 'error') || !_.has(parsedJson, 'syntax_errors'))
            && _.isEqual(parsedJson.type, 'transaction_aborted_statement')) {

            this.initFromJson(parsedJson);

            // Manually firing the tree-modified event here.
            // TODO: need a proper fix to avoid breaking the undo-redo
            this.trigger('tree-modified', {
                origin: this,
                type: 'custom',
                title: 'TransactionAborted Statement Custom Tree modified',
                context: this,
            });

            if (_.isFunction(callback)) {
                callback({isValid: true});
            }
        } else {
            if (_.isFunction(callback)) {
                callback({isValid: false, response: parsedJson});
            }
        }
    }
}

export default TransactionAbortedStatement;