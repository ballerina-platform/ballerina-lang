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
 */
class ElseIfStatement extends ConditionalStatement {
    /**
     * Constructor for if-else statement
     * @param {object} args constructor arguments
     */
    constructor(args) {
        super();
        if (!_.isNil(_.get(args, 'condition'))) {
            this._condition = _.get(args, 'condition');
        }
        this.type = 'ElseIfStatement';
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: ' ',
            2: ' ',
            3: '',
            4: ' ',
            5: '\n',
            6: ' ',
        };
    }

    /**
     * Set condition from string
     * @param {string} conditionString condition string
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
     * Set condition expression
     * @param {Expression} condition condition string
     * @param {object} options set attribute options
     * @returns {void}
     */
    setCondition(condition, options) {
        if (!_.isNil(condition)) {
            this.setAttribute('_condition', condition, options);
        }
    }

    /**
     * Get condition string
     * @return {string|*} condition string
     */
    getConditionString() {
        return this.getCondition().getExpressionString();
    }

    /**
     * Get condition expression
     * @return {Expression} condition expression
     */
    getCondition() {
        return this._condition;
    }

    /**
     * initialize Else If Statement from json object
     * @param {Object} jsonNode to initialize from
     * @returns {void}
     */
    initFromJson(jsonNode) {
        if (!_.isNil(jsonNode.condition)) {
            const condition = this.getFactory().createFromJson(jsonNode.condition);
            condition.initFromJson(jsonNode.condition);
            this.setCondition(condition, {doSilently: true});
            condition.setParent(this, {doSilently: true});
        }
        _.each(jsonNode.children, (childNode) => {
            let child;
            // FIXME: Keeping existing fragile logic to detect connector
            // declaration as it is for now. We should refactor this
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

export default ElseIfStatement;
