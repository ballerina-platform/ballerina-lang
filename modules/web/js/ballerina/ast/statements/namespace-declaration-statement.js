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
import statement from './statement';
import CommonUtils from '../../utils/common-utils';
import FragmentUtils from '../../utils/fragment-utils';
import EnableDefaultWSVisitor from './../../visitors/source-gen/enable-default-ws-visitor';

/**
 * class for namespace declaration statement.
 * @class NamespaceDeclarationStatement
 * */
class NamespaceDeclarationStatement extends statement {
    /**
     * constructor for NamespaceDeclarationStatement
     * */
    constructor() {
        super('NamespaceDeclarationStatement');
        this.whiteSpace.defaultDescriptor.regions = {
            0: ' ',
            1: ' ',
            2: ' ',
            3: '',
            4: '\n',
        };
    }

    /**
     * Get the statement string
     * @return {string} statement as a string.
     * */
    getStatementString() {
        return this.getChildren()[0].getStatementString();
    }

    /**
     * set the Identifier
     * @param {string} identifier - identifier.
     * @param {object} options - options to be passed in to setAttribute
     *
     * */
    setIdentifier(identifier, options) {
        this.getChildren()[0].setIdentifier(identifier, options);
    }

    /**
     * get the identifier.
     * @return {string} identifier.
     * */
    getIdentifier() {
        return this.getChildren()[0].getIdentifier();
    }

    /**
     * set the statement from a string
     * @param {string} stmtString - partial statement as a string.
     * @param {function} callback - callback to call when done.
     * */
    setStatementFromString(stmtString, callback) {
        const fragment = FragmentUtils.createStatementFragment(stmtString + ';');
        const parsedJson = FragmentUtils.parseFragment(fragment);

        if ((!_.has(parsedJson, 'error') && !_.has(parsedJson, 'syntax_errors'))) {
            let nodeToFireEvent = this;
            if (_.isEqual(parsedJson.type, 'namespace_declaration_statement')) {
                this.initFromJson(parsedJson);
            } else if (_.has(parsedJson, 'type')) {
                const newNode = this.getFactory().createFromJson(parsedJson);
                if (this.getFactory().isStatement(newNode)) {
                    const parent = this.getParent();
                    const index = this.getIndexOfChild(this);
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
     * Generate unique identifiers.
     * */
    generateUniqueIdentifiers() {
        if (this.getFactory().isResourceDefinition(this.parent) || this.getFactory().isConnectorAction(this.parent)
            || this.getFactory().isFunctionDefinition(this.parent)) {
            CommonUtils.generateUniqueIdentifier({
                node: this,
                attributes: [{
                    checkEvenIfDefined: true,
                    defaultValue: 'ns',
                    setter: this.setIdentifier,
                    getter: this.getIdentifier,
                    parents: [{
                        // resource/connector action / function  definition
                        node: this.parent,
                        getChildrenFunc: this.parent.getNamespaceDeclarationStatements,
                        getter: this.getIdentifier,
                    }],
                }],
            });
        }
    }

    /**
     * initialize from a json.
     * @param {object} jsonNode - json node for namespaceDeclarationStatement.
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

export default NamespaceDeclarationStatement;
