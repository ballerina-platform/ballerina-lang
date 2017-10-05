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
import PanelDecorator from './../decorators/panel-decorator';
import { getComponentForNodeArray } from './../../../../diagram-util';
import GlobalExpanded from './globals-expanded';
import GlobalDefinitions from './global-definitions';
import * as DesignerDefaults from './../../designer-defaults';
import PanelDecoratorButton from './../decorators/panel-decorator-button';
import ServiceTransportLine from './service-transport-line';
import ImageUtil from './../../../../image-util';
import ServerConnectorProperties from '../utils/server-connector-properties';

/**
 * React component for a connector definition.
 *
 * @class ConnectorNode
 * @extends {React.Component}
 */
class ConnectorNode extends React.Component {

    /**
     * Creates an instance of ConnectorNode.
     * @param {Object} props React properties.
     * @memberof ConnectorNode
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
     * Event handler when the swagger button of the Connector definition panel is clicked.
     * @memberof ConnectorNode
     */
    onSwaggerButtonClicked() {
        this.context.editor.showSwaggerViewForService(this.props.model);
    }

    /**
     * Checks if a specific type of node can be dropped.
     *
     * @param {ASTNode} nodeBeingDragged The node that is being dropped.
     * @returns {boolean} True if can be dropped, else false.
     * @memberof ConnectorNode
     */
    canDropToPanelBody(nodeBeingDragged) {
        /* const nodeFactory = ASTFactory;
          // IMPORTANT: override default validation logic
          // Panel's drop zone is for resource defs and connector declarations only.
        return nodeFactory.isConnectorDeclaration(nodeBeingDragged)
              || nodeFactory.isResourceDefinition(nodeBeingDragged);*/
    }

    /**
     * Handles global variable delete event.
     *
     * @param {ASTNode} deletedGlobal Variable AST.
     * @memberof ConnectorNode
     */
    handleDeleteVariable(deletedGlobal) {
        this.props.model.removeVariableDefinitionStatement(deletedGlobal.getID());
    }

    /**
     * Handles variables badge click event to expand or collapse the view.
     *
     * @memberof ConnectorNode
     */
    handleVarialblesBadgeClick() {
        this.props.model.viewState.variablesExpanded = !this.props.model.viewState.variablesExpanded;
        this.context.editor.update();
    }
    /**
     * Handles the mouse enter event on the connector definition
     */
    onMouseEnter() {
        this.setState({ style: 'showResourceGroup' });
    }

    /**
     * Handles the mouse leave event on the connector definition
     */
    onMouseLeave() {
        if (!this.props.model.viewState.showWebSocketMethods) {
            this.setState({ style: 'hideResourceGroup' });
        }
    }
    /**
     * Renders the view for a connector definition.
     *
     * @returns {ReactElement} The view.
     * @memberof ConnectorNode
     */
    render() {
        const model = this.props.model;
        const viewState = model.viewState;
        const bBox = model.viewState.bBox;
        const variables = model.getVariableDefs();

        // get the connector name
        const title = model.getName().value;

        /**
         * Here we skip rendering the variables
         */
        const blockNode = getComponentForNodeArray(model.getActions(), this.context.mode);

        const expandedVariablesBBox = {
            x: bBox.x + DesignerDefaults.panel.body.padding.left + 10,
            y: bBox.y + DesignerDefaults.panel.body.padding.top + 5,
        };

        const rightComponents = [];

        return (
            <g>
                <PanelDecorator
                    icon="tool-icons/connector"
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
                                model={this.props.model}
                                newValuePlaceholder={''}
                                onDeleteClick={this.handleDeleteVariable}
                                getValue={g => (g.getSource())}
                            /> :
                            <GlobalDefinitions
                                bBox={expandedVariablesBBox}
                                numberOfItems={variables.length}
                                title={'Variables'}
                                onExpand={this.handleVarialblesBadgeClick}
                            />
                        }
                    {blockNode}
                </PanelDecorator>
                {/*<ServerConnectorProperties*/}
                    {/*bBox={bBox}*/}
                    {/*model={this.props.model}*/}
                {/*/>*/}
            </g>);
    }
}

ConnectorNode.propTypes = {
    model: PropTypes.instanceOf(Object).isRequired,
};

ConnectorNode.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
    mode: PropTypes.string,
};

export default ConnectorNode;
