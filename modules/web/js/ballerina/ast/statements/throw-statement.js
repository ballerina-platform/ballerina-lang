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
 * Class for throw statement in ballerina.
 */
class ThrowStatement extends Statement {
    /**
     * Constructor for throw statement
     * @constructor
     */
    constructor() {
        super('ThrowStatement');
        this.type = 'ThrowStatement';
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: ' ',
            2: '\n'
        };
    }

    /**
     * Set expression
     * @param {expression} expression throw statement expression
     * @param {object} options set attribute options
     * @returns {void}
     */
    setExpression(expression, options) {
        if (!_.isNil(expression)) {
            this.setAttribute('_expression', expression, options);
        } else {
            log.error('Cannot set undefined to the throw statement.');
        }
    }

    /**
     * Set the throw statement string
     * @param {string} statementString statement string from which is statement set
     * @param {function} callback callback function
     * @override
     */
    setStatementFromString(statementString, callback) {
        const fragment = FragmentUtils.createStatementFragment(statementString + ';');
        const parsedJson = FragmentUtils.parseFragment(fragment);

        if ((!_.has(parsedJson, 'error') || !_.has(parsedJson, 'syntax_errors'))
            && _.isEqual(parsedJson.type, 'throw_statement')) {
            this.initFromJson(parsedJson);

            // Manually firing the tree-modified event here.
            // TODO: need a proper fix to avoid breaking the undo-redo
            this.trigger('tree-modified', {
                origin: this,
                type: 'custom',
                title: 'Modify Throw Statement',
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
     * Get the throw statement string
     * @return {string} throw statement string
     * @override
     */
    getStatementString() {
        return 'throw' + this.getWSRegion(1) + this.getChildren()[0].getExpressionString();
    }

    /**
     * initialize ThrowStatement from json object
     * @param {Object} jsonNode to initialize from
     * @returns {void}
     */
    initFromJson(jsonNode) {
        this.getChildren().length = 0;
        const self = this;
        _.each(jsonNode.children, (childNode) => {
            const child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    }
}

export default ThrowStatement;

