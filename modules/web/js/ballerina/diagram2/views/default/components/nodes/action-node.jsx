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
import LifeLineDecorator from './../decorators/lifeline.jsx';
import PanelDecorator from './../decorators/panel-decorator';
import { getComponentForNodeArray } from './../../../../diagram-util';
import { lifeLine } from './../../designer-defaults';
import ImageUtil from './../../../../image-util';
import './service-definition.css';
import TreeUtil from '../../../../../model/tree-util';
import ConnectorDeclarationDecorator from '../decorators/connector-declaration-decorator';
import StatementDropZone from '../../../../../drag-drop/DropZone';

class ActionNode extends React.Component {

    canDropToPanelBody(dragSource) {
        return TreeUtil.isConnectorDeclaration(dragSource)
            || TreeUtil.isWorker(dragSource);
    }

    render() {
        const bBox = this.props.model.viewState.bBox;
        const name = this.props.model.getName().value;
        const statementContainerBBox = this.props.model.body.viewState.bBox;
        const bodyBBox = this.props.model.body.viewState.bBox;
        const body = this.props.model.getBody();
        const action_worker_bBox = {};
        action_worker_bBox.x = statementContainerBBox.x + (statementContainerBBox.w - lifeLine.width) / 2;
        action_worker_bBox.y = statementContainerBBox.y - lifeLine.head.height;
        action_worker_bBox.w = lifeLine.width;
        action_worker_bBox.h = statementContainerBBox.h + lifeLine.head.height * 2;

        const classes = {
            lineClass: 'default-worker-life-line',
            polygonClass: 'default-worker-life-line-polygon',
        };
        const argumentParameters = this.props.model.getParameters();

        const blockNode = getComponentForNodeArray(this.props.model.getBody(), this.context.mode);
        const connectors = this.props.model.body.statements
            .filter((element) => { return TreeUtil.isConnectorDeclaration(element); }).map((statement) => {
                return (
                    <ConnectorDeclarationDecorator
                        model={statement}
                        title={statement.variable.name.value}
                        bBox={statement.viewState.bBox}
                    />);
            });
        const workers = getComponentForNodeArray(this.props.model.workers, this.context.mode);
        let annotationBodyHeight = 0;
        if (!_.isNil(this.props.model.viewState.components.annotation)) {
            annotationBodyHeight = this.props.model.viewState.components.annotation.h;
        }

        const tLinkBox = Object.assign({}, bBox);
        tLinkBox.y += annotationBodyHeight;
        return (
            <g>
                <PanelDecorator
                    icon="tool-icons/action"
                    title={name}
                    bBox={bBox}
                    model={this.props.model}
                    dropTarget={this.props.model}
                    canDrop={this.canDropToPanelBody}
                    argumentParams={argumentParameters}
                >
                    <g>
                        { this.props.model.getWorkers().length === 0 &&
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
                                    bBox={action_worker_bBox}
                                    classes={classes}
                                    icon={ImageUtil.getSVGIconString('tool-icons/worker-white')}
                                    iconColor='#025482'
                                />
                                {blockNode}
                            </g>
                        }{
                            this.props.model.workers.map((item) => {
                                return (<StatementDropZone
                                    x={item.getBody().viewState.bBox.x}
                                    y={item.getBody().viewState.bBox.y}
                                    width={item.getBody().viewState.bBox.w}
                                    height={item.getBody().viewState.bBox.h}
                                    baseComponent="rect"
                                    dropTarget={item.getBody()}
                                    enableDragBg
                                />);
                            })
                        }
                        {workers}
                        {connectors}
                    </g>
                </PanelDecorator>
            </g>);
    }
}

ActionNode.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
    mode: PropTypes.string,
};

export default ActionNode;
