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
import PanelDecorator from '../decorators/panel-decorator';
import ImageUtil from '../../../../image-util';
import StatementDropZone from '../../../../../drag-drop/DropZone';
import LifeLine from '../decorators/lifeline';
import FunctionNodeModel from '../../../../../model/tree/function-node';
import { getComponentForNodeArray } from './../../../../diagram-util';
import TreeUtil from '../../../../../model/tree-util';
import ConnectorDeclarationDecorator from '../decorators/connector-declaration-decorator';

class FunctionNode extends React.Component {

    constructor(props) {
        super(props);
        this.canDropToPanelBody = this.canDropToPanelBody.bind(this);
    }

    canDropToPanelBody(dragSource) {
        return TreeUtil.isConnectorDeclaration(dragSource)
            || TreeUtil.isWorker(dragSource);
    }

    render() {
        const bBox = this.props.model.viewState.bBox;
        const name = this.props.model.getName().value;

        // change icon for main function
        let icons = 'tool-icons/function';
        if (name === 'main') {
            icons = 'tool-icons/main-function';
        }
        const body = this.props.model.getBody();
        const bodyBBox = body.viewState.bBox;
        const blockNode = getComponentForNodeArray(body, this.context.mode);
        const workers = getComponentForNodeArray(this.props.model.workers, this.context.mode);

        const classes = {
            lineClass: 'default-worker-life-line',
            polygonClass: 'default-worker-life-line-polygon',
        };

        const argumentParameters = this.props.model.getParameters();
        const returnParameters = this.props.model.getReturnParameters();

        const connectors = this.props.model.body.statements
            .filter((element) => { return TreeUtil.isConnectorDeclaration(element); }).map((statement) => {
                return (
                    <ConnectorDeclarationDecorator
                        model={statement}
                        title={statement.variable.name.value}
                        bBox={statement.viewState.bBox}
                    />);
            });

        return (
            <PanelDecorator
                bBox={bBox}
                title={name}
                model={this.props.model}
                icon={icons}
                dropTarget={this.props.model}
                canDrop={this.canDropToPanelBody}
                argumentParams={argumentParameters}
                returnParams={returnParameters}
            >
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
                    <LifeLine
                        title="default"
                        bBox={this.props.model.viewState.components.defaultWorkerLine}
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
            </PanelDecorator>);
       // TODOX }
    }
}

FunctionNode.propTypes = {
    model: PropTypes.instanceOf(FunctionNodeModel).isRequired,
};

FunctionNode.contextTypes = {
    mode: PropTypes.string,
};

export default FunctionNode;
