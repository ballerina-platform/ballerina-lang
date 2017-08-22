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
import ASTFactory from './../ast-factory';

/**
 * class for XML sequence literal.
 * @class XMLSequenceLiteral
 * */
class XMLSequenceLiteral extends Expression {
    constructor(args) {
        super('XMLSequenceLiteral');
        this.type_name = _.get(args, 'type_name', '');
    }

    getExpressionString() {
        let expression = `${this.type_name} \``;
        this.children.forEach((child) => {
            if (ASTFactory.isBasicLiteralExpression(child)) {
                expression += `${child.getBasicLiteralValue()}`;
            } else if (ASTFactory.isSimpleVariableReferenceExpression(child)) {
                expression += `{{${child.getVariableName()}}}`;
            }
        });
        expression += '`';
        return expression;
    }


    /**
     * Initialize from the json.
     * @param {object} jsonNode - json node for the XML sequence literal.
     * */
    initFromJson(jsonNode) {
        this.children = [];
        this.type_name = _.get(jsonNode, 'type_name', '');
        _.forEach(jsonNode.children, (childNode) => {
            const child = ASTFactory.createFromJson(childNode);
            this.addChild(child, undefined, true, true);
            child.initFromJson(childNode);
        });
    }
}

export default XMLSequenceLiteral;
