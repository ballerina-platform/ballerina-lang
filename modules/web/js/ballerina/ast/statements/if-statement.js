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
import ConditionalStatement from './conditional-statement';
import FragmentUtils from '../../utils/fragment-utils';

/**
 * Class for if conditions in ballerina. Extended from Conditional-Statement
 * @augments ConditionalStatement
 */
class IfStatement extends ConditionalStatement {
    /**
     * Constructor for IfStatement
     * @param {Object} args - Argument object for creating an if statement.
     * @param {string} [args.condition="true"] - The condition for "if".
     * @param {Statement} [args.statements="[]] - Statements of the "if".
     * @constructor
     */
    constructor(args) {
        super();
        this.type = 'IfStatement';
        if (!_.isNil(_.get(args, 'condition'))) {
            this.setCondition(_.get(args, 'condition'));
        } else {
            const options = {
                basicLiteralType: 'boolean',
                basicLiteralValue: true,
            };
            // create default condition
            this.setCondition(this.getFactory().createBasicLiteralExpression(options));
        }
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: ' ',
            2: '',
            3: ' ',
            4: '\n',
            5: ' ',
        };
    }

    /**
     * Set if condition
     * @param {Expression} condition condition expression
     * @param {object} options set attribute options
     * @returns {void}
     */
    setCondition(condition, options) {
        if (!_.isNil(condition)) {
            this.setAttribute('_condition', condition, options);
        }
    }

    /**
     * Get condition as a string
     * @return {string} condition string
     */
    getConditionString() {
        return this.getCondition().getExpressionString();
    }

    /**
     * Set condition expression from string
     * @param {string} conditionString condition string to be set
     * @returns {void}
     */
    setConditionFromString(conditionString) {
        if (!_.isNil(conditionString)) {
            const fragment = FragmentUtils.createExpressionFragment(conditionString);
            const parsedJson = FragmentUtils.parseFragment(fragment);
            const condition = this.getFactory().createFromJson(parsedJson);
            condition.initFromJson(parsedJson);
            this.setCondition(condition);
            condition.setParent(this, {doSilently: true});
        }
    }

    /**
     * Get condition expression
     * @return {expression} condition expression
     */
    getCondition() {
        return this._condition;
    }

    /**
     * initialize IfStatement from json object
     * @param {Object} jsonNode to initialize from
     * @returns {void}
     */
    initFromJson(jsonNode) {
        if (!_.isNil(jsonNode.condition)) {
            const condition = this.getFactory().createFromJson(jsonNode.condition);
            condition.initFromJson(jsonNode.condition);
            this.setCondition(condition);
            condition.setParent(this, {doSilently: true});
        }
        _.each(jsonNode.children, (childNode) => {
            let child;
            // FIXME Keeping existing fragile  logic to detect connector declaration as it is for now.
            // FIXME We should refactor this
            if (childNode.type === 'variable_definition_statement' &&
                !_.isNil(childNode.children[1]) && childNode.children[1].type === 'connector_init_expr') {
                child = this.getFactory().createConnectorDeclaration();
            } else {
                child = this.getFactory().createFromJson(childNode);
            }
            this.addChild(child);
            child.initFromJson(childNode);
        });
    }
}

export default IfStatement;
