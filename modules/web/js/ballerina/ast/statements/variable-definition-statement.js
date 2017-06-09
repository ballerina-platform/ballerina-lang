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
import Statement from './statement';
import CommonUtils from '../../utils/common-utils';
import VariableDeclaration from './../variable-declaration';
import FragmentUtils from '../../utils/fragment-utils';

/**
 * Class to represent an Variable Definition statement.
 * @param {Object} [args] - Arguments for creating a variable definition statement.
 * @param {string} [args.leftExpression] - The left hand expression.
 * @param {string|undefined} [args.rightExpression] - The right hand expression.
 * @constructor
 * @augments Statement
 */
class VariableDefinitionStatement extends Statement {
    constructor(args) {
        super('VariableDefinitionStatement');
        this.whiteSpace.defaultDescriptor.regions =  {
            0: ' ',
            1: ' ',
            2: ' ',
            3: '',
            4: '\n'
        };
    }

    /**
     * Get the variable definition statement string
     * @return {string} - Variable definition expression string
     */
    getStatementString() {
        var variableDefinitionStatementString;
        variableDefinitionStatementString = this.getBType();
        if (((this.getChildren()[0]).getChildren()[0])._isArray) {
            for (var itr = 0; itr < ((this.getChildren()[0]).getChildren()[0])._dimensions; itr ++) {
                variableDefinitionStatementString += '[]';
            }
        }
        variableDefinitionStatementString += this.getWSRegion(0) + this.getIdentifier();
        if (!_.isNil(this.children[1])) {
            variableDefinitionStatementString +=
              this.getWSRegion(1) + '=' + this.getWSRegion(2) + this.children[1].getExpressionString();
        } else {
            variableDefinitionStatementString += this.getWSRegion(3);
        }
        return variableDefinitionStatementString;
    }

    /**
     * Gets the ballerina type of the variable definition statement.
     * @return {string} - The ballerina type.
     */
    getBType() {
        return !_.isNil(this.children[0]) ? this.children[0].getVariableType() : undefined;
    }

    /**
     * Gets the identifier of the variable definition statement.
     * @return {string} - The identifier.
     */
    getIdentifier() {
        return !_.isNil(this.children[0]) ? this.children[0].getVariableName() : undefined;
    }

    /**
     * Gets the identifier of the variable definition statement.
     * @return {string} - The identifier.
     */
    getValue() {
        return !_.isNil(this.children[1]) ? this.children[1].getExpressionString()
                  : undefined;
    }

    setIdentifier(identifier) {
        this.children[0].setVariableName(identifier);
    }

    setBType(bType) {
        this.children[0].setVariableType(bType);
    }

    setValue(value) {
        let fragment = FragmentUtils.createExpressionFragment(value);
        let parsedJson = FragmentUtils.parseFragment(fragment);
        if ((!_.has(parsedJson, 'error')
               || !_.has(parsedJson, 'syntax_errors'))) {
            let child = this.getFactory().createFromJson(parsedJson);
            this.initFromJson(child);
            this.children[1] = child;
            this.trigger('tree-modified', {
                origin: this,
                type: 'custom',
                title: 'Variable definition modified',
                context: this,
            });
        }
    }

    /**
     * Set the variable definition statement string
     * @param {string} variableDefinitionStatementString - variable definition statement string
     */
    setStatementFromString(stmtString) {
        if (!_.isNil(stmtString)) {
            let fragment = FragmentUtils.createStatementFragment(stmtString + ';');
            let parsedJson = FragmentUtils.parseFragment(fragment);
            if ((!_.has(parsedJson, 'error')
                   || !_.has(parsedJson, 'syntax_errors'))
                   && _.isEqual(parsedJson.type, 'variable_definition_statement')) {
                this.initFromJson(parsedJson);
            }

            // Manually firing the tree-modified event here.
            // TODO: need a proper fix to avoid breaking the undo-redo
            this.trigger('tree-modified', {
                origin: this,
                type: 'custom',
                title: 'Variable definition modified',
                context: this,
            });
        }
    }

    /**
     * @inheritDoc
     * @override
     */
    generateUniqueIdentifiers() {
        if (this.getFactory().isResourceDefinition(this.parent) || this.getFactory().isConnectorAction(this.parent)) {
            CommonUtils.generateUniqueIdentifier({
                node: this,
                attributes: [{
                    checkEvenIfDefined: true,
                    defaultValue: "i",
                    setter: this.setIdentifier,
                    getter: this.getIdentifier,
                    parents: [{
                        // resource/connector action definition
                        node: this.parent,
                        getChildrenFunc: this.parent.getVariableDefinitionStatements,
                        getter: this.getIdentifier
                    }, {
                        // service/connector definition
                        node: this.parent.parent,
                        getChildrenFunc: this.parent.parent.getVariableDefinitionStatements,
                        getter: this.getIdentifier
                    }, {
                        // ballerina-ast-root definition
                        node: this.parent.parent.parent,
                        getChildrenFunc: this.parent.parent.parent.getConstantDefinitions,
                        getter: VariableDeclaration.prototype.getIdentifier
                    }]
                }]
            });
        }
    }

    initFromJson(jsonNode) {
        this.children = [];
        _.each(jsonNode.children, (childNode) => {
            var child = this.getFactory().createFromJson(childNode);
            this.addChild(child, undefined, true, true);
            child.initFromJson(childNode);
        });
    }
}

export default VariableDefinitionStatement;
