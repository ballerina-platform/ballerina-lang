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
 * Constructor for FunctionInvocationExpression
 * @param {Object} args - Arguments to create the FunctionInvocationExpression
 * @constructor
 * @augments Expression
 */
class FunctionInvocationExpression extends Expression {
    constructor(args) {
        super('FunctionInvocationExpression');
        this._packageName = _.get(args, 'packageName', '');
        this._functionName = _.get(args, 'functionName', 'callFunction');
        this._fullPackageName = _.get(args, 'fullPackageName', '');
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: '',
            2: '',
            3: '',
        };
        this.whiteSpace.defaultDescriptor.children = {
            nameRef: {
                0: '',
                1: '',
                2: '',
                3: '',
            },
        };
    }

    setFunctionName(functionName, options) {
        this.setAttribute('_functionName', functionName, options);
    }

    getFunctionName() {
        return this._functionName;
    }

    setFullPackageName(packageName, options) {
        this.setAttribute('_fullPackageName', packageName, options);
    }

    getFullPackageName() {
        return this._fullPackageName;
    }

    setPackageName(packageName, options) {
        this.setAttribute('_packageName', packageName, options);
    }

    getPackageName() {
        return this._packageName;
    }

    /**
     * Get the expression string
     * @returns {string} expression string
     * @override
     */
    getExpressionString() {
        let text = '';
        if (!_.isNil(this._packageName) && !_.isEmpty(this._packageName) && !_.isEqual(this._packageName, 'Current Package')) {
            text += this._packageName + this.getChildWSRegion('nameRef', 1) + ':';
        }
        text += this.getChildWSRegion('nameRef', 2) + this._functionName + this.getWSRegion(1);
        text += '(' + this.getWSRegion(2);

        this.children.forEach((child, index) => {
            if (index !== 0) {
                text += ',';
                text += child.getExpressionString({ includePrecedingWS: true });
            } else {
                text += child.getExpressionString();
            }
        });
        text += ')' + this.getWSRegion(3);
        return text;
    }

    setExpressionFromString(expressionString, callback) {
        const fragment = FragmentUtils.createExpressionFragment(expressionString);
        const parsedJson = FragmentUtils.parseFragment(fragment);

        if ((!_.has(parsedJson, 'error') || !_.has(parsedJson, 'syntax_errors'))
            && _.isEqual(parsedJson.type, 'function_invocation_expression')) {
            this.initFromJson(parsedJson);

            // Manually firing the tree-modified event here.
            // TODO: need a proper fix to avoid breaking the undo-redo
            this.trigger('tree-modified', {
                origin: this,
                type: 'custom',
                title: 'Modify Function Invocation Expression',
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
     * Creating the function invocation statement which invoked by the parsed code.
     * @param {Object} jsonNode - A node explaining the structure of a function invocation.
     * @param {string} jsonNode.type - The type of this current node. The value would be
     * "function_invocation_expression";
     * @param {string} jsonNode.package_name - The package name of the function being invoked. Example : "system".
     * @param {string} jsonNode.function_name - The body of the function information. Example : "println".
     * @param {Object[]} jsonNode.children - The arguments of the function invocation.
     */
    initFromJson(jsonNode) {
        this.children = [];
        const self = this;
        this.setPackageName(jsonNode.package_name, { doSilently: true });
        if (_.isEqual(jsonNode.package_path, '.')) {
            this.setFullPackageName('Current Package', { doSilently: true });
        } else {
            this.setFullPackageName(jsonNode.package_path, { doSilently: true });
        }
        this.setFunctionName(jsonNode.function_name, { doSilently: true });
        _.each(jsonNode.children, (childNode) => {
            const child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    }

}

export default FunctionInvocationExpression;
