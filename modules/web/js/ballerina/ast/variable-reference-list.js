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
import ASTNode from './node';
import FragmentUtils from '../utils/fragment-utils';

/**
 * Constructor for VariableReferenceList
 */
class VariableReferenceList extends ASTNode {
    /**
     *constructor for Variable reference list
     * @constructor
     */
    constructor() {
        super('VariableReferenceList');
    }

    /**
     * Get the expression string
     * @return {string} expression string
     */
    getExpressionString() {
        let exprString = '';
        this.getChildren().forEach((child, index) => {
            if (index !== 0) {
                exprString += ',';
                exprString += (child.whiteSpace.useDefault ? ' ' : child.getWSRegion(0));
            }
            exprString += child.getExpressionString();
        });
        return exprString;
    }

    /**
     * Set the expression from the expression string
     * @param {string} expression expression string
     * @param {function} callback function
     * @override
     */
    setExpressionFromString(expression, callback) {
        if (!_.isNil(expression)) {
            const fragment = FragmentUtils.createExpressionFragment(expression);
            const parsedJson = FragmentUtils.parseFragment(fragment);
            if ((!_.has(parsedJson, 'error')
                   || !_.has(parsedJson, 'syntax_errors'))
                   && _.isEqual(parsedJson.type, 'variable_reference_list')) {
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
     * setting parameters from json
     * @param {object} jsonNode json node
     * @returns {void}
     */
    initFromJson(jsonNode) {
        if (!_.isEmpty(jsonNode.children)) {
            jsonNode.children.forEach((childJsonNode) => {
                const child = this.getFactory().createFromJson(childJsonNode);
                child.initFromJson(childJsonNode);
                this.addChild(child, undefined, true, true);
            });
        }
    }
}

export default VariableReferenceList;
