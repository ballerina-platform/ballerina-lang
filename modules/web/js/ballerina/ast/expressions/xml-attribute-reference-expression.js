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

/**
 * class for xml attribute reference expression.
 *
 * @class XMLAttributeReferenceExpression
 * */
class XMLAttributeReferenceExpression extends Expression {

    /**
     * constructor for {@link XMLAttributeReferenceExpression}.
     * */
    constructor() {
        super('XMLAttributeReferenceExpression');
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: ' ',
            2: ' ',
            3: ' ',
            4: '\n',
        };
    }

    /**
     * get the expression as a string.
     * @return {string} expression string.
     * */
    getExpressionString() {
        let expressionString = `${this.getChildren()[0].getVariableName()}@`;
        if (this.getChildren()[1].getExpressionString() !== this.getChildren()[0].getVariableName()) {
            expressionString += `[${this.getChildren()[1].getExpressionString()}]`;
        }
        return expressionString;
    }

    /**
     * initialize the XMLAttributeReferenceExpression from json.
     * @param {object} jsonNode - json node.
     * */
    initFromJson(jsonNode) {
        this.children = [];
        _.each(jsonNode.children, (childNode) => {
            const child = this.getFactory().createFromJson(childNode);
            this.addChild(child, undefined, true, true);
            child.initFromJson(childNode);
        });
    }
}

export default XMLAttributeReferenceExpression;
