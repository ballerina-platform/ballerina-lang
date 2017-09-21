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
import { getComponentForNodeArray } from './../../../diagram-util';
import GlobalExpanded from './globals-expanded';
import GlobalDefinitions from './global-definitions';
import * as DesignerDefaults from './../../../../configs/designer-defaults';
import ServiceDefinitionAST from './../../../../ast/service-definition';
import PanelDecoratorButton from './panel-decorator-button';
import ServiceTransportLine from './service-transport-line';
import ImageUtil from './image-util';
import ASTFactory from '../../../../ast/ast-factory';

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
        this.state = {
            style: 'hideResourceGroup',
        };
        this.handleDeleteVariable = this.handleDeleteVariable.bind(this);
        this.handleVarialblesBadgeClick = this.handleVarialblesBadgeClick.bind(this);
        this.onMouseEnter = this.onMouseEnter.bind(this);
        this.onMouseLeave = this.onMouseLeave.bind(this);
    }

    /**
     * Event handler when the swagger button of the service definition panel is clicked.
     * @memberof ServiceDefinition
     */
    onSwaggerButtonClicked() {
        this.context.editor.showSwaggerViewForService(this.props.model);
    }

    /**
     * Checks if a specific type of node can be dropped.
     *
     * @param {ASTNode} nodeBeingDragged The node that is being dropped.
     * @returns {boolean} True if can be dropped, else false.
     * @memberof ServiceDefinition
     */
    canDropToPanelBody(nodeBeingDragged) {
        const nodeFactory = ASTFactory;
          // IMPORTANT: override default validation logic
          // Panel's drop zone is for resource defs and connector declarations only.
        return nodeFactory.isConnectorDeclaration(nodeBeingDragged)
              || nodeFactory.isResourceDefinition(nodeBeingDragged);
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
        this.context.editor.update();
    }
    /**
     * Handles the mouse enter event on the service definition
     */
    onMouseEnter() {
        this.setState({ style: 'showResourceGroup' });
    }

    /**
     * Handles the mouse leave event on the service definition
     */
    onMouseLeave() {
        if (!this.props.model.getViewState().showWebSocketMethods) {
            this.setState({ style: 'hideResourceGroup' });
        }
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
        const variables = model.filterChildren(child => ASTFactory.isVariableDefinitionStatement(child));

        // get the service name
        const title = model.getServiceName();

        const childrenWithNoVariables = model.filterChildren(
                                                child => !ASTFactory.isVariableDefinitionStatement(child));

        /**
         * Here we skip rendering the variables
         */
        const children = getComponentForNodeArray(childrenWithNoVariables, this.context.mode);

        const expandedVariablesBBox = {
            x: bBox.x + DesignerDefaults.panel.body.padding.left,
            y: components.body.y + DesignerDefaults.panel.body.padding.top,
        };

        const rightComponents = [];

        // TODO: Check whether the service is a http/https and then only allow. JMS services does not need swagger defs.
        // eslint-disable-next-line no-constant-condition
        if (this.props.model.getProtocolPkgName() === 'http') {
            // Pushing swagger edit button.
            rightComponents.push({
                component: PanelDecoratorButton,
                props: {
                    key: `${model.getID()}-swagger-button`,
                    icon: ImageUtil.getSVGIconString('swagger'),
                    tooltip: 'Swagger Source',
                    onClick: () => this.onSwaggerButtonClicked(),
                },
            });
        }

        // todo: this is a hack need to be fixed
        const resources = this.props.model.filterChildren(child => ASTFactory.isResourceDefinition(child));
        this.props.model.getViewState().components.transportLine.y2 = 0;
        if (resources[resources.length - 1]) {
            this.props.model.getViewState().components.transportLine.y2
                = resources[resources.length - 1].getViewState().components.body.y - 15;
        }

        return (
            <g
                className={`protocol-${this.props.model.getProtocolPkgName()}`}
                onMouseLeave={this.onMouseLeave}
                onMouseEnter={this.onMouseEnter}
            >
                <PanelDecorator
                    icon="tool-icons/service"
                    title={title}
                    bBox={bBox}
                    model={model}
                    dropTarget={this.props.model}
                    dropSourceValidateCB={node => this.canDropToPanelBody(node)}
                    rightComponents={rightComponents}
                    protocol={this.props.model.getProtocolPkgName()}
                    showPropertyForm={this.props.model.getViewState().showPropertyForm}
                >
                    <ServiceTransportLine
                        model={this.props.model}
                        bBox={this.props.model.getViewState().components.transportLine}
                        style={this.state.style}
                        resources={resources}
                    />
                    {
                            viewState.variablesExpanded ?
                                <GlobalExpanded
                                    bBox={expandedVariablesBBox}
                                    globals={variables}
                                    onCollapse={this.handleVarialblesBadgeClick}
                                    title="Variables"
                                    addText={'+ Add Variable'}
                                    model={this.props.model}
                                    onAddNewValue={this.props.model.addVariableDefinitionFromString.bind(this.props.model)}
                                    newValuePlaceholder={''}
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
                </PanelDecorator>
            </g>);
    }
}

ServiceDefinition.propTypes = {
    model: PropTypes.instanceOf(ServiceDefinitionAST).isRequired,
};

ServiceDefinition.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
    mode: PropTypes.string,
};

export default ServiceDefinition;
