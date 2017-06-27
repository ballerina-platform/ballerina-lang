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
import Expression from './expression';
import FragmentUtils from './../../utils/fragment-utils';

/**
 * Constructor for ReferenceTypeInitExpression
 * @param {Object} args - Arguments to create the ReferenceTypeInitExpression
 * @constructor
 */
class ReferenceTypeInitExpression extends Expression {
    constructor(args) {
        super('ReferenceTypeInitExpression');
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: '',
            2: '',
        };
    }

    /**
     * initialize ReferenceTypeInitExpression from json object
     * @param {Object} jsonNode to initialize from
     */
    initFromJson(jsonNode) {
        this.getChildren().length = 0;
        const self = this;
        _.each(jsonNode.children, (childNode) => {
            const child = self.getFactory().createFromJson(childNode);
            self.addChild(child, undefined, true, true);
            child.initFromJson(childNode);
        });
    }

    /**
     * Get the expression String
     * @returns {string}
     * @override
     */
    getExpressionString() {
        let generatedExpression = '';
        this.children.forEach((child) => {
            generatedExpression += child.getExpressionString() + ',';
        });
        generatedExpression = '{' + this.getWSRegion(1) + (generatedExpression.substring(0, generatedExpression.length - 1))
            + '}' + this.getWSRegion(2);

        return generatedExpression;
    }

    /**
     * Set the expression from the string
     * @param {string} expressionString
     * @param {function} callback
     * @override
     */
    setExpressionFromString(expressionString, callback) {
        const fragment = FragmentUtils.createExpressionFragment(expressionString);
        const parsedJson = FragmentUtils.parseFragment(fragment);

        if ((!_.has(parsedJson, 'error') || !_.has(parsedJson, 'syntax_errors'))
            && _.isEqual(parsedJson.type, 'reference_type_init_expression')) {
            this.initFromJson(parsedJson);

            // Manually firing the tree-modified event here.
            // TODO: need a proper fix to avoid breaking the undo-redo
            this.trigger('tree-modified', {
                origin: this,
                type: 'custom',
                title: 'Modify Reference Type Init Expression',
                context: this,
            });

            if (_.isFunction(callback)) {
                callback({ isValid: true });
            }
        } else if (_.isFunction(callback)) {
            callback({ isValid: false, response: parsedJson });
        }
    }
}

export default ReferenceTypeInitExpression;
