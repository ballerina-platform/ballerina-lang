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
import log from 'log';
import Expression from './expression';
import FragmentUtils from './../../utils/fragment-utils';

class FieldAccessExpression extends Expression {
    constructor(args) {
        super('FieldAccessExpression');
        this._isArrayExpression = _.get(args, 'isArrayExpression', false);
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: '',
        };
    }

    /**
     * A FieldAccessExpression can have either 1 or 2 child/children. First one being a
     * {@link VariableReferenceExpression} and the 2nd being {@link FieldAccessExpression} or another expression
     * such as {@link FunctionInvocationExpression}. Hence if 2nd child exists, we call getExpression() on that child.
     * @return {string}
     * @override
     */
    getExpressionString() {
        if (this.getChildren().length === 1) {
            const exp = this.getChildren()[0];
            if (this.getFactory().isBasicLiteralExpression(exp)) {
                if (exp.getBasicLiteralType() === 'string') {
                    if (this.getIsArrayExpression()) {
                        return '[' + exp.getExpressionString() + ']';
                    }
                    return '.' + exp.getBasicLiteralValue();
                }
                return '[' + exp.getExpressionString() + ']';
            }
            return '[' + exp.getExpressionString() + ']';
        } else if (this.getChildren().length === 2) {
            const firstVar = this.getChildren()[0];
            const secondVar = this.getChildren()[1];
            if (this.getFactory().isFieldAccessExpression(this.getParent())) {
                // if this is an inner field access expression
                if (this.getIsArrayExpression()) {
                    return '[' + firstVar.getExpressionString() + ']' + secondVar.getExpressionString();
                }
                if (this.getFactory().isBasicLiteralExpression(firstVar)) {
                    if (firstVar.getBasicLiteralType() === 'string') {
                        if (this.getIsArrayExpression()) {
                            return '[' + firstVar.getExpressionString() + ']' + secondVar.getExpressionString();
                        }
                        return '.' + firstVar.getBasicLiteralValue() + secondVar.getExpressionString();
                    }
                } else {
                    return '.' + firstVar.getExpressionString() + secondVar.getExpressionString();
                }
            } else {
                return firstVar.getExpressionString() + secondVar.getExpressionString();
            }
        } else {
            log.error('Error in determining Field Access expression');
        }
    }

    setExpressionFromString(expressionString) {
        const fragment = FragmentUtils.createExpressionFragment(expressionString);
        const parsedJson = FragmentUtils.parseFragment(fragment);

        if ((!_.has(parsedJson, 'error') || !_.has(parsedJson, 'syntax_errors'))
            && _.isEqual(parsedJson.type, 'field_access_expression')) {
            this.initFromJson(parsedJson);

            // Manually firing the tree-modified event here.
            // TODO: need a proper fix to avoid breaking the undo-redo
            this.trigger('tree-modified', {
                origin: this,
                type: 'custom',
                title: 'Modify Field Access Expression',
                context: this,
            });

            if (_.isFunction(callback)) {
                callback({ isValid: true });
            }
        } else if (_.isFunction(callback)) {
            callback({ isValid: false, response: parsedJson });
        }
    }

    setIsArrayExpression(isArrayExpression, options) {
        this.setAttribute('_isArrayExpression', isArrayExpression, options);
    }

    getIsArrayExpression() {
        return this._isArrayExpression;
    }

    /**
     * initialize FieldAccessExpression from json object
     * @param {Object} jsonNode to initialize from
     */
    initFromJson(jsonNode) {
        this.getChildren().length = 0;
        this.setIsArrayExpression(jsonNode.is_array_expression, { doSilently: true });
        _.each(jsonNode.children, (childNode) => {
            const child = this.getFactory().createFromJson(childNode);
            this.addChild(child);
            child.initFromJson(childNode);
        });
    }
}

export default FieldAccessExpression;
