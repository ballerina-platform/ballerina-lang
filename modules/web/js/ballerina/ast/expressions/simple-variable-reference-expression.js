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
import FragmentUtils from '../../utils/fragment-utils';

/**
 * Constructor for SimpleVariableReferenceExpression
 * @param {Object} args - Arguments to create the SimpleVariableReferenceExpression
 * @constructor
 */
class SimpleVariableReferenceExpression extends Expression {
    constructor(args) {
        super(args);
        this.type = 'SimpleVariableReferenceExpression';
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: '',
            2: '',
            3: '',

        };
        this.setVariableName(_.get(args, 'variableName'));
        this._packageName = _.get(args, 'packageName');
    }

    /**
     * Setter for Variable Name
     * @param variableName
     */
    setVariableName(variableName, options) {
        this.setAttribute('_variableName', variableName, options);
    }

    /**
     * Getter for Variable Name
     * @returns variableName
     */
    getVariableName() {
        return this._variableName;
    }

    /**
     * Getter for Variable type
     * @returns var type
     */
    getVariableType() {
        return this.children[0].getTypeName();
    }

    /**
     * setter for Variable type
     */
    setVariableType(typeName) {
        return this.children[0].setTypeName(typeName);
    }

    /**
     * Get the package name
     * @returns {string} package name
     */
    getPackageName() {
        return this._packageName;
    }

    /**
     * Set the package name
     * @param {string} packageName
     * @param {object} options
     */
    setPackageName(packageName, options) {
        this.setAttribute('_packageName', packageName, options);
    }

    /**
     * initialize SimpleVariableReferenceExpression from json object
     * @param {Object} jsonNode to initialize from
     * @param {string} [jsonNode.variable_reference_name] variable name of the SimpleVariableReferenceExpression
     * @param {string} [jsonNode.package_name] package name of SimpleVariableReferenceExpression
     */
    initFromJson(jsonNode) {
        this.getChildren().length = 0;
        const self = this;
        _.each(jsonNode.children, (childNode) => {
            const child = self.getFactory().createFromJson(childNode);
            self.addChild(child, undefined, true, true);
            child.initFromJson(childNode);
        });
        this.setVariableName(jsonNode.variable_reference_name, { doSilently: true });
        this.setPackageName(jsonNode.package_name, { doSilently: true });
    }

    /**
     * Set the expression from the expression string
     * @param {string} expressionString
     * @override
     */
    setExpressionFromString(expression, callback) {
        if (!_.isNil(expression)) {
            const fragment = FragmentUtils.createExpressionFragment(expression);
            const parsedJson = FragmentUtils.parseFragment(fragment);
            if ((!_.has(parsedJson, 'error')
                   || !_.has(parsedJson, 'syntax_errors'))
                   && _.isEqual(parsedJson.type, 'simple_variable_reference_expression')) {
                this.initFromJson(parsedJson);
                if (_.isFunction(callback)) {
                    callback({ isValid: true });
                }
            } else if (_.isFunction(callback)) {
                callback({ isValid: false, response: parsedJson });
            }
        }
    }

    /**
     * Get the expression string
     * @returns {string} expression string
     * @override
     */
    getExpressionString() {
        return (!_.isNil(this.getPackageName()) ? (this.getPackageName()
                + this.getWSRegion(1) + ':' + this.getWSRegion(2)) : '')
                + this.getVariableName() + this.getWSRegion(3);
    }

}

export default SimpleVariableReferenceExpression;
