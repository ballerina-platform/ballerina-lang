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
import log from 'log';
import Statement from './statement';
import FragmentUtils from './../../utils/fragment-utils';

/**
 * Class for try-catch statement in ballerina.
 */
class TryCatchStatement extends Statement {
    /**
     * Constructor for TryCatchStatement
     * @param {object} args - arguments for the tryCatchStatement
     * @override
     */
    constructor(args) {
        super();
        this.type = 'TryCatchStatement';
        this._tryStatement = _.get(args, 'tryStatement');
        this._catchStatements = _.get(args, 'catchStatements', []);
        this._finallyStatement = _.get(args, 'finallyStatement');
    }

    /**
     * setter for catch block exception
     * @param {string} exception - exception string
     * @param {object} options - set attribute options
     * @returns {void}
     */
    setExceptionType(exception, options) {
        if (!_.isNil(exception)) {
            this.setAttribute('_exceptionType', exception, options);
        } else {
            log.error('Cannot set undefined to the exception.');
        }
    }

    /**
     * getter for catch block exception type
     * @return {string} _exceptionType
     */
    getExceptionType() {
        return this._exceptionType;
    }

    /**
     * initialize TryCatchStatement from json object
     * @param {Object} jsonNode to initialize from
     * @returns {void}
     */
    initFromJson(jsonNode) {
        this._catchStatements.length = 0;
        this.getChildren().length = 0;
        const self = this;
        const tryBlockNode = jsonNode.try_block;
        const catchBlocks = jsonNode.catch_blocks;
        const finallyBlockNode = jsonNode.finally_block;

        this._tryStatement = self.getFactory().createFromJson(tryBlockNode);
        this._tryStatement.initFromJson(tryBlockNode);
        this.addChild(this._tryStatement);

        _.each(catchBlocks, (catchBlock) => {
            const catchNode = self.getFactory().createFromJson(catchBlock);
            this.getCatchStatements().push(catchNode);
            this.addChild(catchNode);
            catchNode.initFromJson(catchBlock);
        });

        if (!_.isNil(finallyBlockNode)) {
            this._finallyStatement = self.getFactory().createFromJson(finallyBlockNode);
            this._finallyStatement.initFromJson(finallyBlockNode);
            this.addChild(this._finallyStatement);
        }
    }

    /**
     * Set the statement from statement string
     * @param {string} statementString - statement string
     * @param {function} callback - callback function
     * @returns {void}
     */
    setStatementFromString(statementString, callback) {
        const fragment = FragmentUtils.createStatementFragment(statementString);
        const parsedJson = FragmentUtils.parseFragment(fragment);

        if ((!_.has(parsedJson, 'error') || !_.has(parsedJson, 'syntax_errors'))
            && _.isEqual(parsedJson.type, 'try_catch_statement')) {
            this.initFromJson(parsedJson);

            // Manually firing the tree-modified event here.
            // TODO: need a proper fix to avoid breaking the undo-redo
            this.trigger('tree-modified', {
                origin: this,
                type: 'custom',
                title: 'Modify TryCatch Statement',
                context: this,
            });

            if (_.isFunction(callback)) {
                callback({ isValid: true });
            }
        } else if (_.isFunction(callback)) {
            callback({ isValid: false, response: parsedJson });
        }
    }

    /**
     * Get the try statement
     * @returns {TryStatement} try statement
     */
    getTryStatement() {
        return this._tryStatement;
    }

    /**
     * Get the catch statement
     * @returns {CatchStatement} catch statement
     */
    getCatchStatements() {
        return this._catchStatements;
    }
}

export default TryCatchStatement;

