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
import Expression from './../expressions/expression';

/**
 * Class to represent a condition to ballerina.
 */
class ConditionalStatement extends Statement {
    /**
     * CConstructor for condition statement
     * @param {string|expression} condition The condition of a conditional statement.
     * @param {[ASTNode]} statements The statements of a conditional statement.
     * @constructor
     */
    constructor(condition, statements) {
        super();
        this._condition = condition || 'true';
        this._statments = statements || [];
        this.type = 'ConditionalStatement';
    }

    /**
     * Set the condition.
     * @param {Expression} condition full package name
     * @param {Object} options set attribute options
     * @returns {void}
     */
    setCondition(condition, options) {
        if (!_.isNil(condition) && condition instanceof Expression) {
            this.setAttribute('_condition', condition, options);
        } else {
            log.error('Invalid value for condition received: ' + condition);
        }
    }

    /**
     * Get condition
     * @return {expression} condition expression
     */
    getCondition() {
        return this._condition;
    }

    /**
     * Set the statements.
     * @param {[Statement]} statements Statements array
     * @param {Object} options set attribute options
     * @returns {void}
     */
    setStatements(statements, options) {
        // There should be atleast one statement.
        if (!_.isNil(statements)) {
            this.setAttribute('_statments', statements, options);
        } else {
            log.error('Cannot set undefined array of statements.');
        }
    }

    /**
     * Get the Statements
     * @return {[ASTNode]} statement array
     */
    getStatements() {
        return this._statments;
    }
}

export default ConditionalStatement;

