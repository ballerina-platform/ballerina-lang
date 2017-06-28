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

import React from 'react';
import PropTypes from 'prop-types';
import PanelDecorator from './panel-decorator';
import { getComponentForNodeArray } from './utils';
import ConnectorDefinitionAST from './../ast/connector-definition';
import GlobalExpanded from './globals-expanded';
import GlobalDefinitions from './global-definitions';
import * as DesignerDefaults from './../configs/designer-defaults';
import BallerinaASTFactory from './../ast/ballerina-ast-factory';

/**
 * React component for a connector definition.
 *
 * @class ConnectorDefinition
 * @extends {React.Component}
 */
class ConnectorDefinition extends React.Component {

    /**
     * Creates an instance of ConnectorDefinition.
     * @param {Object} props React properties.
     *
     * @memberof ConnectorDefinition
     */
    constructor(props) {
        super(props);
        this.variableDefRegex = /\s*(int|string|boolean)\s+([a-zA-Z0-9_]+)\s*=\s*(.*)/g; // This is not 100% accurate
        this.handleAddVariable = this.handleAddVariable.bind(this);
        this.handleDeleteVariable = this.handleDeleteVariable.bind(this);
        this.handleVarialblesBadgeClick = this.handleVarialblesBadgeClick.bind(this);
    }

    /**
     * Checkes if node can be dropped to panel body.
     *
     * @param {ASTNode} nodeBeingDragged The ast node being dropped.
     * @returns {boolean} true if {@link ConnectorDeclaration} or {@link ConnectorAction}, else false.
     * @memberof ConnectorDefinition
     */
    canDropToPanelBody(nodeBeingDragged) {
        const nodeFactory = this.props.model.getFactory();
        // IMPORTANT: override default validation logic
        // Panel's drop zone is for resource defs and connector declarations only.
        return nodeFactory.isConnectorDeclaration(nodeBeingDragged)
            || nodeFactory.isConnectorAction(nodeBeingDragged);
    }

    /**
     * Adds variable to the model.
     *
     * @param {string} value Variable name.
     * @memberof ConnectorDefinition
     */
    handleAddVariable(value) {
        const variableDefRegex = /\s*(int|string|boolean)\s+([a-zA-Z0-9_]+)\s*=\s*(.*)/g; // This is not 100% accurate
        const match = variableDefRegex.exec(value);

        if (match && match[1] && match[2] && match[3]) {
            this.props.model.addVariableDefinitionStatement(match[1], match[2], match[3]);
        }
    }

    /**
     * Deletes variable.
     *
     * @param {VariableDefinition} deletedGlobal Variable to be removed.
     * @memberof ConnectorDefinition
     */
    handleDeleteVariable(deletedGlobal) {
        this.props.model.removeVariableDefinitionStatement(deletedGlobal.getID());
    }

    /**
     * Event for variables badge click.
     * @memberof ConnectorDefinition
     */
    handleVarialblesBadgeClick() {
        this.props.model.viewState.variablesExpanded = !this.props.model.viewState.variablesExpanded;
        this.context.editor.trigger('update-diagram');
    }

    /**
     * Renders view for a connector definition.
     *
     * @returns {ReactElement} The view.
     * @memberof ConnectorDefinition
     */
    render() {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const viewState = model.getViewState();
        const components = viewState.components;
        this.variableDefRegex = /const\s+(int|string|boolean)\s+([a-zA-Z0-9_]+)\s*=\s*(.*)/g; // Not 100% accurate
        const variables = model.filterChildren(child => BallerinaASTFactory.isVariableDefinitionStatement(child));

        // get the connector name
        const title = model.getConnectorName();

        const childrenWithNoVariables = model.filterChildren(
                                                    child => !BallerinaASTFactory.isVariableDefinitionStatement(child));

        /**
         * Here we skip rendering the variables
         */
        const children = getComponentForNodeArray(childrenWithNoVariables);

        const expandedVariablesBBox = {
            x: bBox.x + DesignerDefaults.panel.body.padding.left,
            y: components.body.y + DesignerDefaults.panel.body.padding.top,
        };

        const titleComponentData = [{
            isNode: true,
            model: this.props.model.getArgumentParameterDefinitionHolder(),
        }];

        return (<PanelDecorator
            icon="tool-icons/connector"
            title={title}
            bBox={bBox}
            model={model}
            dropTarget={this.props.model}
            dropSourceValidateCB={node => this.canDropToPanelBody(node)}
            titleComponentData={titleComponentData}
        >
            {
                    this.props.model.viewState.variablesExpanded ?
                        <GlobalExpanded
                            bBox={expandedVariablesBBox}
                            globals={variables}
                            onCollapse={this.handleVarialblesBadgeClick}
                            title="Variables"
                            model={this.props.model}
                            onAddNewValue={this.props.model.addVariableDefinitionFromString.bind(this.props.model)}
                            newValuePlaceholder={'nnn'}
                            onDeleteClick={this.handleDeleteVariable}
                            addText={'+ Add Variable'}
                            getValue={g => (g.getStatementString())}
                        /> :
                        <GlobalDefinitions
                            bBox={expandedVariablesBBox}
                            numberOfItems={variables.length}
                            title={'Variables'}
                            onExpand={this.handleVarialblesBadgeClick}
                        />
                }
            {children}
        </PanelDecorator>);
    }
}

ConnectorDefinition.propTypes = {
    model: PropTypes.instanceOf(ConnectorDefinitionAST).isRequired,
};

ConnectorDefinition.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired
};

export default ConnectorDefinition;
