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
import _ from 'lodash';
import PropTypes from 'prop-types';
import StatementDropZone from '../../../../../drag-drop/DropZone';
import LifeLineDecorator from './../decorators/lifeline.jsx';
import PanelDecorator from './../decorators/panel-decorator';
import ResourceTransportLink from './resource-transport-link';
import { getComponentForNodeArray } from './../../../../diagram-util';
import { lifeLine } from './../../designer-defaults';
import ImageUtil from './../../../../image-util';
import './service-definition.css';
import AddResourceDefinition from './add-resource-definition';
import TreeUtil from '../../../../../model/tree-util';
import ConnectorDeclarationDecorator from '../decorators/connector-declaration-decorator';

class ResourceNode extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            style: 'hideResourceGroup',
        };
        this.onMouseOver = this.onMouseOver.bind(this);
        this.onMouseOut = this.onMouseOut.bind(this);
    }

    canDropToPanelBody(dragSource) {
        return TreeUtil.isConnectorDeclaration(dragSource)
            || TreeUtil.isWorker(dragSource);
    }

    /**
     * Handles the mouse enter event on the service definition
     */
    onMouseOver() {
        this.setState({ style: 'showResourceGroup' });
    }

    /**
     * Handles the mouse leave event on the service definition
     */
    onMouseOut() {
        if (_.isEmpty(this.props.model.viewState.overlayContainer)) {
            this.setState({ style: 'hideResourceGroup' });
        }
    }

    render() {
        const bBox = this.props.model.viewState.bBox;
        const name = this.props.model.getName().value;
        const parentNode = this.props.model.parent;
        const body = this.props.model.body;
        const bodyBBox = this.props.model.body.viewState.bBox;
        // const connectorOffset = this.props.model.viewState.components.statementContainer.expansionW;
        // lets calculate function worker lifeline bounding box.
        const resource_worker_bBox = {};
        resource_worker_bBox.x = bodyBBox.x + (bodyBBox.w - lifeLine.width) / 2;
        resource_worker_bBox.y = bodyBBox.y - lifeLine.head.height;
        resource_worker_bBox.w = lifeLine.width;
        resource_worker_bBox.h = bodyBBox.h + lifeLine.head.height * 2;

        const classes = {
            lineClass: 'default-worker-life-line',
            polygonClass: 'default-worker-life-line-polygon',
        };
        const argumentParameters = this.props.model.getParameters();

        const connectors = this.props.model.body.statements
            .filter((element) => { return TreeUtil.isConnectorDeclaration(element); }).map((statement) => {
                return (
                    <ConnectorDeclarationDecorator
                        model={statement}
                        title={statement.variable.name.value}
                        bBox={statement.viewState.bBox}
                    />);
            });        

        const blockNode = getComponentForNodeArray(this.props.model.getBody(), this.context.mode);
        const workers = getComponentForNodeArray(this.props.model.workers, this.context.mode);
        // const nodeFactory = ASTFactory;
        // Check for connector declaration children
        // const connectorChildren = (this.props.model.filterChildren(nodeFactory.isConnectorDeclaration));

        let annotationBodyHeight = 0;
        if (!_.isNil(this.props.model.viewState.components.annotation)) {
            annotationBodyHeight = this.props.model.viewState.components.annotation.h;
        }

        const tLinkBox = Object.assign({}, bBox);
        tLinkBox.y += annotationBodyHeight;
        const thisNodeIndex = parentNode.getIndexOfResources(this.props.model);
        const resourceSiblings = parentNode.getResources();
        let showAddResourceBtn = true;
        if (parentNode.getProtocolPackageIdentifier().value === 'ws' && resourceSiblings.length >= 6) {
            showAddResourceBtn = false;
        }
        return (
            <g>
                <ResourceTransportLink bBox={tLinkBox} />
                <PanelDecorator
                    icon="tool-icons/resource"
                    title={name}
                    bBox={bBox}
                    model={this.props.model}
                    dropTarget={this.props.model}
                    canDrop={this.canDropToPanelBody}
                    argumentParams={argumentParameters}
                    packageIdentifier={parentNode.getProtocolPackageIdentifier().value}
                >
                    <g>
                        <StatementDropZone
                            x={bodyBBox.x}
                            y={bodyBBox.y}
                            width={bodyBBox.w}
                            height={bodyBBox.h}
                            baseComponent="rect"
                            dropTarget={body}
                            enableDragBg
                        />
                        <LifeLineDecorator
                            title="default"
                            bBox={resource_worker_bBox}
                            classes={classes}
                            icon={ImageUtil.getSVGIconString('tool-icons/worker-white')}
                            iconColor='#025482'
                        />
                        {blockNode}
                        {workers}
                        {connectors}
                    </g>
                </PanelDecorator>
                {(thisNodeIndex !== parentNode.getResources().length - 1 && showAddResourceBtn &&
                !this.props.model.viewState.collapsed) &&
                <g
                    className={this.state.style}
                    onMouseOver={this.onMouseOver}
                    onMouseOut={this.onMouseOut}
                >
                    <rect
                        x={bBox.x - 50}
                        y={bBox.y + bBox.h}
                        width={bBox.w + 20}
                        height='50'
                        fillOpacity="0"
                        strokeOpacity="0"
                    />
                    <AddResourceDefinition
                        model={this.props.model}
                        style={this.state.style}
                    />
                </g>
                }
            </g>);
    }
}

ResourceNode.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
    mode: PropTypes.string,
};

export default ResourceNode;
