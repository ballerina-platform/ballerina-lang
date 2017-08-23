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
 * Class for XML Element Literal.
 * @class XMLElementLiteral.
 * */
class XMLElementLiteral extends Expression {
    constructor(args) {
        super('XMLElementLiteral');
        this._attributes = _.get(args, 'attributes', []);
        this.type_name = _.get(args, 'type_name', '');
    }

    getExpressionString(isTemplate) {
        let expression = '';
        expression += isTemplate ? '' : `${this.type_name} \``;
        if (this.children[0]) {
            if (ASTFactory.isBasicLiteralExpression(this.children[0])) {
                expression += `<${this.children[0].getBasicLiteralValue()}`;
            } else if (ASTFactory.isSimpleVariableReferenceExpression(this.children[0])) {
                expression += `<{{${this.children[0].getVariableName()}}}`;
            } else if (ASTFactory.isXMLQNameExpression(this.children[0])) {
                expression += `<${this.children[0]._localName}`;
            }

            this._attributes.forEach((attribute) => {
                attribute.children.forEach((child) => {
                    if (ASTFactory.isBasicLiteralExpression(child)) {
                        if (attribute.children.indexOf(child) === 0 || attribute.children.indexOf(child) === 1) {
                            expression += `="${child.getBasicLiteralValue()}`;
                        } else {
                            expression += `${child.getBasicLiteralValue()}`;
                        }
                        if (attribute.children.indexOf(child) === (attribute.children.length - 1)) {
                            expression += '"';
                        }
                    } else if (ASTFactory.isSimpleVariableReferenceExpression(child)) {
                        if (attribute.children.indexOf(child) === 0) {
                            expression += ` {{${child.getVariableName()}}}`;
                        } else if (attribute.children.indexOf(child) === 1) {
                            expression += `="{{${child.getVariableName()}}}`;
                        } else {
                            expression += `{{${child.getVariableName()}}}`;
                        }

                        if (attribute.children.indexOf(child) !== 0
                            && attribute.children.indexOf(child) === (attribute.children.length - 1)) {
                            expression += '"';
                        }
                    } else if (ASTFactory.isXMLQNameExpression(child)) {
                        expression += ` ${child.getLocalName()}`;
                    } else if (ASTFactory.isBinaryExpression(child)) {
                        expression += `="${child.getExpressionString(true)}"`;
                    }
                });
            });
            if (!this.children[1] && !this.children[2]) {
                expression += '/>';
            } else {
                expression += '>';
            }
        }

        if (ASTFactory.isXMLSequenceLiteral(this.children[1]) && this.children[1].children.length > 0) {
            expression += this.children[1].getExpressionString(true);
        } else if (ASTFactory.isXMLQNameExpression(this.children[1])) {
            if (ASTFactory.isBasicLiteralExpression(this.children[1])) {
                expression += `</${this.children[1]._localName}>`;
            } else if (ASTFactory.isSimpleVariableReferenceExpression(this.children[1])) {
                expression += `</{{${this.children[1].getVariableName()}}}>`;
            } else {
                expression += `</${this.children[1]._localName}>`;
            }
        }

        if (this.children[2]) {
            if (ASTFactory.isBasicLiteralExpression(this.children[2])) {
                expression += `</${this.children[2]._localName}>`;
            } else if (ASTFactory.isSimpleVariableReferenceExpression(this.children[2])) {
                expression += `</{{${this.children[2].getVariableName()}}}>`;
            } else {
                expression += `</${this.children[2]._localName}>`;
            }
        }

        expression += isTemplate ? '' : '`';
        return expression;
    }

    /**
     * Initialize the model from the json Node.
     * @param {Object} jsonNode - Json node fore XML Element Literal.
     * */
    initFromJson(jsonNode) {
        this.children = [];
        this.type_name = _.get(jsonNode, 'type_name', '');
        this._attributes = [];
        _.forEach(jsonNode.children, (childNode) => {
            const child = ASTFactory.createFromJson(childNode);
            this.addChild(child, undefined, true, true);
            child.initFromJson(childNode);
        });

        _.forEach(jsonNode.attributes, (childNode) => {
            const child = ASTFactory.createFromJson(childNode);
            this._attributes.push(child);
            child.initFromJson(childNode);
        });
    }
}

export default XMLElementLiteral;
