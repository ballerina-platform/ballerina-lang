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
import GlobalExpanded from './globals-expanded';
import GlobalDefinitions from './global-definitions';
import * as DesignerDefaults from './../configs/designer-defaults';
import BallerinaASTFactory from './../ast/ballerina-ast-factory';
import ServiceDefinitionAST from './../ast/service-definition';
import PanelDecoratorButton from './panel-decorator-button';
import ImageUtil from './image-util';

/**
 * React component for a service definition.
 *
 * @class ServiceDefinition
 * @extends {React.Component}
 */
class ServiceDefinition extends React.Component {

    /**
     * Creates an instance of ServiceDefinition.
     * @param {Object} props React properties.
     * @memberof ServiceDefinition
     */
    constructor(props) {
        super(props);
        this.variableDefRegex = /\s*(int|string|boolean)\s+([a-zA-Z0-9_]+)\s*=\s*(.*)/g; // This is not 100% accurate
        this.handleAddVariable = this.handleAddVariable.bind(this);
        this.handleDeleteVariable = this.handleDeleteVariable.bind(this);
        this.handleVarialblesBadgeClick = this.handleVarialblesBadgeClick.bind(this);
    }

    /**
     * Event handler when the swagger button of the service definition panel is clicked.
     * @memberof ServiceDefinition
     */
    onSwaggerButtonClicked() {
        this.context.renderingContext.ballerinaFileEditor.showSwaggerView();
    }

    /**
     * Checks if a specific type of node can be dropped.
     *
     * @param {ASTNode} nodeBeingDragged The node that is being dropped.
     * @returns {boolean} True if can be dropped, else false.
     * @memberof ServiceDefinition
     */
    canDropToPanelBody(nodeBeingDragged) {
        const nodeFactory = this.props.model.getFactory();
          // IMPORTANT: override default validation logic
          // Panel's drop zone is for resource defs and connector declarations only.
        return nodeFactory.isConnectorDeclaration(nodeBeingDragged)
              || nodeFactory.isResourceDefinition(nodeBeingDragged);
    }

    /**
     * Adds new variable definition statement to the model.
     *
     * @param {string} value The value of the variable.
     * @memberof ServiceDefinition
     */
    handleAddVariable(value) {
        const variableDefRegex = /\s*(int|string|boolean)\s+([a-zA-Z0-9_]+)\s*=\s*(.*)/g; // This is not 100% accurate
        const match = variableDefRegex.exec(value);
        if (match && match[1] && match[2] && match[3]) {
            this.props.model.addVariableDefinitionStatement(match[1], match[2], match[3]);
        }
    }

    /**
     * Handles global variable delete event.
     *
     * @param {ASTNode} deletedGlobal Variable AST.
     * @memberof ServiceDefinition
     */
    handleDeleteVariable(deletedGlobal) {
        this.props.model.removeVariableDefinitionStatement(deletedGlobal.getID());
    }

    /**
     * Handles variables badge click event to expand or collapse the view.
     *
     * @memberof ServiceDefinition
     */
    handleVarialblesBadgeClick() {
        this.props.model.viewState.variablesExpanded = !this.props.model.viewState.variablesExpanded;
        this.context.editor.trigger('update-diagram');
    }

    /**
     * Renders the view for a service definition.
     *
     * @returns {ReactElement} The view.
     * @memberof ServiceDefinition
     */
    render() {
        const model = this.props.model;
        const viewState = model.getViewState();
        const components = viewState.components;
        const bBox = model.viewState.bBox;
        const variables = model.filterChildren(child => BallerinaASTFactory.isVariableDefinitionStatement(child));

        // get the service name
        const title = model.getServiceName();

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

        const rightComponents = [];

        // TODO: Check whether the service is a http/https and then only allow. JMS services does not need swagger defs.
        // eslint-disable-next-line no-constant-condition
        if (true) {
            // Pushing swagger edit button.
            rightComponents.push({
                component: PanelDecoratorButton,
                props: {
                    key: `${model.getID()}-swagger-button`,
                    icon: ImageUtil.getSVGIconString('swagger'),
                    onClick: () => this.onSwaggerButtonClicked(),
                },
            });
        }

        return (<PanelDecorator
            icon="tool-icons/service"
            title={title}
            bBox={bBox}
            model={model}
            dropTarget={this.props.model}
            dropSourceValidateCB={node => this.canDropToPanelBody(node)}
            rightComponents={rightComponents}
        >
            {
                    viewState.variablesExpanded ?
                        <GlobalExpanded
                            bBox={expandedVariablesBBox}
                            globals={variables}
                            onCollapse={this.handleVarialblesBadgeClick}
                            title="Variables"
                            addText={'+ Add Variable'}
                            onAddNewValue={this.handleAddVariable}
                            onDeleteClick={this.handleDeleteVariable}
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

ServiceDefinition.propTypes = {
    model: PropTypes.instanceOf(ServiceDefinitionAST).isRequired,
};

ServiceDefinition.contextTypes = {
    renderingContext: PropTypes.instanceOf(Object).isRequired,
    editor: PropTypes.instanceOf(Object).isRequired,
};

export default ServiceDefinition;
