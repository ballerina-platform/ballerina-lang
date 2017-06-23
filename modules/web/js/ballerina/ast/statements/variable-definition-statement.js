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
import Statement from './statement';
import CommonUtils from '../../utils/common-utils';
import VariableDeclaration from './../variable-declaration';
import FragmentUtils from '../../utils/fragment-utils';
import EnableDefaultWSVisitor from './../../visitors/source-gen/enable-default-ws-visitor';

/**
 * Class to represent an Variable Definition statement.
 */
class VariableDefinitionStatement extends Statement {
    /**
     * Constructor for VariableDefinitionStatement
     * @constructor
     */
    constructor() {
        super('VariableDefinitionStatement');
        this.whiteSpace.defaultDescriptor.regions = {
            0: ' ',
            1: ' ',
            2: ' ',
            3: '',
            4: '\n',
        };
    }

    /**
     * Get the variable definition statement string
     * @return {string} - Variable definition expression string
     */
    getStatementString() {
        let variableDefinitionStatementString = !_.isNil(((this.getChildren()[0]).getChildren()[0]).getPkgName()) ?
            (((this.getChildren()[0]).getChildren()[0]).getPkgName() + ':') : '';
        variableDefinitionStatementString += this.getBType();
        if (((this.getChildren()[0]).getChildren()[0]).isArray()) {
            for (let itr = 0; itr < ((this.getChildren()[0]).getChildren()[0]).getDimensions; itr++) {
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

    /**
     * Set the identifier of the variable
     * @param {string} identifier - variable identifier
     * @returns {void}
     */
    setIdentifier(identifier, opts) {
        this.children[0].setVariableName(identifier, opts);
    }

    /**
     * Set the BuiltIn Type of the variable
     * @param {string} bType - BType of the variable
     * @returns {void}
     */
    setBType(bType, opts) {
        this.children[0].setVariableType(bType, opts);
    }

    /**
     * Set the variable value
     * @param {string} value - value of the variable
     * @returns {void}
     */
    setValue(value) {
        const fragment = FragmentUtils.createExpressionFragment(value);
        const parsedJson = FragmentUtils.parseFragment(fragment);
        if ((!_.has(parsedJson, 'error')
               || !_.has(parsedJson, 'syntax_errors'))) {
            const child = this.getFactory().createFromJson(parsedJson);
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
     * @param {string} stmtString - variable definition statement string
     * @param {function} callback - callback function
     * @override
     */
    setStatementFromString(stmtString, callback) {
        const fragment = FragmentUtils.createStatementFragment(stmtString + ';');
        const parsedJson = FragmentUtils.parseFragment(fragment);

        if ((!_.has(parsedJson, 'error') && !_.has(parsedJson, 'syntax_errors'))) {
            let nodeToFireEvent = this;
            if (_.isEqual(parsedJson.type, 'variable_definition_statement')) {
                this.initFromJson(parsedJson);
            } else if (_.has(parsedJson, 'type')) {
                // user may want to change the statement type
                const newNode = this.getFactory().createFromJson(parsedJson);
                if (this.getFactory().isStatement(newNode)) {
                    // somebody changed the type of statement to an assignment
                    // to capture retun value of function Invocation
                    const parent = this.getParent();
                    const index = parent.getIndexOfChild(this);
                    parent.removeChild(this, true);
                    parent.addChild(newNode, index, true, true);
                    newNode.initFromJson(parsedJson);
                    nodeToFireEvent = newNode;
                }
            } else {
                log.error('Error while parsing statement. Error response' + JSON.stringify(parsedJson));
            }

            if (_.isFunction(callback)) {
                callback({ isValid: true });
            }
            nodeToFireEvent.accept(new EnableDefaultWSVisitor());
            // Manually firing the tree-modified event here.
            // TODO: need a proper fix to avoid breaking the undo-redo
            nodeToFireEvent.trigger('tree-modified', {
                origin: nodeToFireEvent,
                type: 'custom',
                title: 'Modify Variable Definition',
                context: nodeToFireEvent,
            });
        } else {
            log.error('Error while parsing statement. Error response' + JSON.stringify(parsedJson));
            if (_.isFunction(callback)) {
                callback({ isValid: false, response: parsedJson });
            }
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
                    defaultValue: 'i',
                    setter: this.setIdentifier,
                    getter: this.getIdentifier,
                    parents: [{
                        // resource/connector action definition
                        node: this.parent,
                        getChildrenFunc: this.parent.getVariableDefinitionStatements,
                        getter: this.getIdentifier,
                    }, {
                        // service/connector definition
                        node: this.parent.parent,
                        getChildrenFunc: this.parent.parent.getVariableDefinitionStatements,
                        getter: this.getIdentifier,
                    }, {
                        // ballerina-ast-root definition
                        node: this.parent.parent.parent,
                        getChildrenFunc: this.parent.parent.parent.getConstantDefinitions,
                        getter: VariableDeclaration.prototype.getIdentifier,
                    }],
                }],
            });
        }
    }

    /**
     * initialize VariableDefinitionStatement from json object
     * @param {Object} jsonNode to initialize from
     * @returns {void}
     */
    initFromJson(jsonNode) {
        this.children = [];
        _.each(jsonNode.children, (childNode) => {
            const child = this.getFactory().createFromJson(childNode);
            this.addChild(child, undefined, true, true);
            child.initFromJson(childNode);
        });
    }
}

export default VariableDefinitionStatement;
