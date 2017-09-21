/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ASTNode from './node';
import CommonUtils from '../utils/common-utils';
import FragmentUtils from '../utils/fragment-utils';
import EnableDefaultWSVisitor from './../visitors/source-gen/enable-default-ws-visitor';
import ASTFactory from './ast-factory';

/**
 * Defines a connector declaration AST node.
 */
class ConnectorDeclaration extends ASTNode {
    /**
     * Constructor for connector declaration
     * @param {Object} options - arguments for a connector declaration.
     * @constructor
     */
    constructor(options) {
        super('ConnectorDeclaration');
        // this.children = _.get(options, 'childrenFactory', () => []).call();
        this._declarationStatement = _.get(options, 'declarationStatement');
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: ' ',
            2: ' ',
            3: '',
            4: '\n',
        };
        this._icon = _.get(options, 'icon');
    }

    /**
     * Set connector details from the given expression.
     *
     * @param {string} stmtString Expression entered by user.
     * @param {function} callback callback function
     * */
    setStatementFromString(stmtString, callback) {
        const fragment = FragmentUtils.createStatementFragment(`${stmtString};`);
        const parsedJson = FragmentUtils.parseFragment(fragment);

        if ((!_.has(parsedJson, 'error') && !_.has(parsedJson, 'syntax_errors'))) {
            const nodeToFireEvent = this;
            this.initFromJson(parsedJson);

            if (_.isFunction(callback)) {
                callback({ isValid: true });
            }
            nodeToFireEvent.accept(new EnableDefaultWSVisitor());
            // Manually firing the tree-modified event here.
            // TODO: need a proper fix to avoid breaking the undo-redo
            nodeToFireEvent.trigger('tree-modified', {
                origin: nodeToFireEvent,
                type: 'custom',
                title: 'Modify Connector Declaration',
                context: nodeToFireEvent,
            });
        } else {
            log.error(`Error while parsing statement. Error response${JSON.stringify(parsedJson)}`);
            if (_.isFunction(callback)) {
                callback({ isValid: false, response: parsedJson });
            }
        }
    }

    /**
     * return underline var def statement
     */
    getDeclarationStatement() {
        return this._declarationStatement;
    }

    /**
     * Get the connector name variable
     * @return {string} - connector name variable
     */
    getConnectorVariable() {
        return this._declarationStatement.getChildren()[0].getVariableName();
    }

    setFullPackageName(fullPackageName) {
        if (this._declarationStatement) {
            const connectorInit = this._declarationStatement.getChildren()[1];
            if (connectorInit && ASTFactory.isConnectorInitExpression(connectorInit)) {
                connectorInit.getConnectorName().setFullPackageName(fullPackageName);
            }
        }
    }

    getFullPackageName() {
        if (this._declarationStatement) {
            const connectorInit = this._declarationStatement.getChildren()[1];
            if (connectorInit && ASTFactory.isConnectorInitExpression(connectorInit)) {
                return connectorInit.getConnectorName().getFullPackageName();
            }
        }
        return undefined;
    }

    /**
     * Set the connector name variable
     * @param {string} variableName - variable name
     * @return {void}
     */
    setConnectorVariable(variableName) {
        this._declarationStatement.getChildren()[0].setVariableName(variableName);
    }

    /**
     * initialize ConnectorDeclaration from json object
     * @param {Object} jsonNode to initialize from
     */
    initFromJson(jsonNode) {
        const self = this;
        if (!_.isNil(jsonNode.whitespace_descriptor)) {
            this.whiteSpace.currentDescriptor = jsonNode.whitespace_descriptor;
            this.whiteSpace.useDefault = false;
        }

        this._declarationStatement = ASTFactory.createVariableDefinitionStatement();
        this._declarationStatement.initFromJson(jsonNode);
    }

    /**
     * Generate Expression to Show on the edit textbox.
     * @return {string} expression
     *
     */
    getStatementString() {
        return this._declarationStatement.getStatementString();
    }

    /**
     * @inheritDoc
     * @override
     */
    generateUniqueIdentifiers() {
        const self = this;
        const uniqueIDGenObject = {
            node: this,
            attributes: [{
                checkEvenIfDefined: true,
                defaultValue: 'endpoint',
                setter: this.setConnectorVariable,
                getter: this.getConnectorVariable,
                parents: [{
                    // resource/service definition.
                    node: this.parent,
                    getChildrenFunc: this.parent.getConnectionDeclarations,
                    getter: this.getConnectorVariable,
                }],
            }],
        };

        if (ASTFactory.isResourceDefinition(this.parent)) {
            uniqueIDGenObject.attributes[0].parents.push({
                // service definition
                node: this.parent.parent,
                getChildrenFunc: this.parent.getConnectionDeclarations,
                getter: this.getConnectorVariable,
            });
        } else if (ASTFactory.isServiceDefinition(this.parent)) {
            const resourceDefinitions = this.parent.getResourceDefinitions();
            _.forEach(resourceDefinitions, (resourceDefinition) => {
                uniqueIDGenObject.attributes[0].parents.push({
                    // resource definition
                    node: resourceDefinition,
                    getChildrenFunc: self.parent.getConnectionDeclarations,
                    getter: self.getConnectorVariable,
                });
            });
        } else if (ASTFactory.isConnectorAction(this.parent)) {
            uniqueIDGenObject.attributes[0].parents.push({
                // connector definition
                node: this.parent.parent,
                getChildrenFunc: this.parent.getConnectionDeclarations,
                getter: this.getConnectorVariable,
            });
        } else if (ASTFactory.isConnectorDefinition(this.parent)) {
            const connectorActions = this.parent.getConnectorActionDefinitions();
            _.forEach(connectorActions, (connectionAction) => {
                uniqueIDGenObject.attributes[0].parents.push({
                    // connector action definition
                    node: connectionAction,
                    getChildrenFunc: self.parent.getConnectionDeclarations,
                    getter: self.getConnectorVariable,
                });
            });
        }

        CommonUtils.generateUniqueIdentifier(uniqueIDGenObject);
    }

    /**
     * Return the identifire of the connector
     *
     * @returns {String} identifire.
     * @memberof ConnectorDeclaration
     */
    getIdentifire() {
        const variable = _.find(this._declarationStatement.children,
            ASTFactory.isSimpleVariableReferenceExpression);
        return variable.getVariableName();
    }

    /**
     * Get the connector icon name
     * @returns {string} - connector icon name
     */
    getIconName() {
        return this._icon;
    }
}

export default ConnectorDeclaration;
