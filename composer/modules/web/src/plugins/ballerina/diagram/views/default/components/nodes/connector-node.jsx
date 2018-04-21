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
import _ from 'lodash';
import PanelDecorator from './../decorators/panel-decorator';
import { getComponentForNodeArray } from './../../../../diagram-util';
import TreeUtil from '../../../../../model/tree-util';
import EndpointDecorator from '../decorators/endpoint-decorator';
import FragmentUtils from './../../../../../utils/fragment-utils';
import TreeBuilder from './../../../../../model/tree-builder';
import AddActionNode from './add-action-node';

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
            addAction: false,
        };
        this.handleDeleteVariable = this.handleDeleteVariable.bind(this);
        this.handleAddVariable = this.handleAddVariable.bind(this);
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
     * Handles the mouse enter event on the connector node
     */
    onMouseEnter() {
        this.setState({ addAction: true });
    }

    /**
     * Handles the mouse leave event on the connector node
     */
    onMouseLeave() {
        if (_.isEmpty(this.props.model.viewState.overlayContainer)) {
            this.setState({ addAction: false });
        }
    }

    /**
     * Checks if a specific type of node can be dropped.
     *
     * @param {ASTNode} nodeBeingDragged The node that is being dropped.
     * @returns {boolean} True if can be dropped, else false.
     * @memberof ConnectorNode
     */
    canDropToPanelBody(dragSource) {
        return TreeUtil.isEndpointTypeVariableDef(dragSource) || TreeUtil.isAction(dragSource);
    }

    /**
     * Handles global variable delete event.
     *
     * @param {ASTNode} deletedGlobal Variable AST.
     * @memberof ServiceNode
     */
    handleDeleteVariable(deletedGlobal) {
        this.props.model.removeVariableDefs(deletedGlobal);
    }

    /**
     * Handles global variable add event.
     *
     * @param {ASTNode} value Variable AST.
     * @memberof ServiceNode
     */
    handleAddVariable(value) {
        if (!value) {
            return;
        }
        const fragment = FragmentUtils.createStatementFragment(`${value}`);
        const parsedJson = FragmentUtils.parseFragment(fragment);
        if (!parsedJson.error) {
            const index = this.props.model.getVariableDefs().length - 1;
            this.props.model.addVariableDefs(TreeBuilder.build(parsedJson), index + 1);
        } else {
            this.context.alert.showError('Invalid content provided !');
        }
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
     * Renders the view for a connector definition.
     *
     * @returns {ReactElement} The view.
     * @memberof ConnectorNode
     */
    render() {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const variables = model.getVariableDefs();
        const argumentParameters = model.getParameters();

        // get the connector name
        const title = model.getName().value;
        const blockNode = getComponentForNodeArray(model.getActions(), this.context.mode);
        const connectors = variables
            .filter((element) => { return TreeUtil.isEndpointTypeVariableDef(element); }).map((statement) => {
                return (
                    <EndpointDecorator
                        model={statement}
                        title={statement.variable.name.value}
                        bBox={statement.viewState.bBox}
                    />);
            });

        const rightComponents = [];

        return (
            <g
                onMouseLeave={this.onMouseLeave}
                onMouseEnter={this.onMouseEnter}
            >
                <PanelDecorator
                    icon='connector'
                    title={title}
                    bBox={bBox}
                    model={model}
                    dropTarget={this.props.model}
                    canDrop={this.canDropToPanelBody}
                    rightComponents={rightComponents}
                    argumentParams={argumentParameters}
                >
                    {blockNode}
                    {connectors}
                </PanelDecorator>
            </g>);
    }
}

ConnectorNode.propTypes = {
    model: PropTypes.instanceOf(Object).isRequired,
};

ConnectorNode.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
    mode: PropTypes.string,
    alert: PropTypes.instanceOf(Object).isRequired,
};

export default ConnectorNode;
