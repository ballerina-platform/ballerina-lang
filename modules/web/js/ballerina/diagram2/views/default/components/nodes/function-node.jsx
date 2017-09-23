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
import PanelDecorator from '../decorators/panel-decorator';
import ImageUtil from '../../../../image-util';
import { lifeLine } from './../../designer-defaults';
/* TODOX
import LifeLine from './lifeline.jsx';
import StatementContainer from './statement-container';
import { getComponentForNodeArray } from './../../../diagram-util';

import ASTFactory from '../../../../ast/ast-factory.js';
*/
class FunctionNode extends React.Component {

    constructor(props) {
        super(props);
    }

    canDropToPanelBody(nodeBeingDragged) {
        /*TODOX
        const nodeFactory = ASTFactory;
        // IMPORTANT: override default validation logic
        // Panel's drop zone is for worker and connector declarations only.
        // Statements should only be allowed on top of function worker's dropzone.
        return nodeFactory.isConnectorDeclaration(nodeBeingDragged)
            || nodeFactory.isWorkerDeclaration(nodeBeingDragged);
            */
    }

    render() {
        const bBox = this.props.model.viewState.bBox;
        const name = this.props.model.getName().value;

        // change icon for main function
        let icons = 'tool-icons/function';
        if (name === 'main') {
            icons = 'tool-icons/main-function';
        }
                
        /*
        const statementContainerBBox = this.props.model.getViewState().components.statementContainer;
        const statementContainerBBoxClone = Object.assign({}, this.props.model.getViewState()
            .components.statementContainer);
        const connectorOffset = this.props.model.getViewState().components.statementContainer.expansionW;
        statementContainerBBoxClone.w += connectorOffset;
        const workerScopeContainerBBox = this.props.model.getViewState().components.workerScopeContainer;
        
        // lets calculate function worker lifeline bounding box.
        const function_worker_bBox = {};
        function_worker_bBox.x = statementContainerBBox.x + (statementContainerBBox.w - lifeLine.width) / 2;
        function_worker_bBox.y = statementContainerBBox.y - lifeLine.head.height;
        function_worker_bBox.w = lifeLine.width;
        function_worker_bBox.h = statementContainerBBox.h + lifeLine.head.height * 2;

        const classes = {
            lineClass: 'default-worker-life-line',
            polygonClass: 'default-worker-life-line-polygon',
        };

        // filter children nodes and create components
        const children = getComponentForNodeArray(this.props.model.getChildren(), this.context.mode);
        const nodeFactory = ASTFactory;
        // Check for connector declaration children
        const connectorChildren = (this.props.model.filterChildren(nodeFactory.isConnectorDeclaration));

        const titleComponentData = [{
            isNode: true,
            model: this.props.model.getArgumentParameterDefinitionHolder(),
        }, {
            isNode: true,
            model: this.props.model.getReturnParameterDefinitionHolder(),
        }];
        const isLambda = this.props.model.isLambda();
        const lifeline = (<LifeLine
            title="default"
            bBox={function_worker_bBox}
            classes={classes}
            icon={ImageUtil.getSVGIconString('tool-icons/worker-white')}
            iconColor='#025482'
        />);
        const statemnts = (<StatementContainer
            dropTarget={this.props.model
            }
            title="StatementContainer"
            bBox={statementContainerBBox}
        >
            { children }
        </StatementContainer>);

        if (isLambda) {
            return (<g>
                <rect x={bBox.x} y={bBox.y} height={30} width={bBox.w} className="return-parameter-group" />
                {lifeline}
                {statemnts}
            </g>);
        } else {*/
            return (
                <PanelDecorator
                    bBox={bBox}
                    title={name}
                    model={this.props.model}
                    icon={icons}
                    /*
                    dropTarget={this.props.model}
                    dropSourceValidateCB={node => this.canDropToPanelBody(node)}
                    titleComponentData={titleComponentData}*/
                >{ /*TODOX
                    <LifeLine
                        title="default"
                        bBox={function_worker_bBox}
                        classes={classes}
                        icon={ImageUtil.getSVGIconString('tool-icons/worker-white')}
                        iconColor='#025482'
                    />
                    { connectorChildren.length > 0 &&
                    <g>
                        <rect
                            x={workerScopeContainerBBox.x}
                            y={workerScopeContainerBBox.y}
                            width={workerScopeContainerBBox.w + workerScopeContainerBBox.expansionW}
                            height={workerScopeContainerBBox.h}
                            style={{ fill: 'none',
                                stroke: '#67696d',
                                strokeWidth: 2,
                                strokeLinecap: 'round',
                                strokeLinejoin: 'miter',
                                strokeMiterlimit: 4,
                                strokeOpacity: 1,
                                strokeDasharray: 5 }}
                        /> </g>
                    }
                    <StatementContainer
                        dropTarget={this.props.model}
                        title="StatementContainer"
                        bBox={statementContainerBBoxClone}
                    >
                        {children}
                    </StatementContainer>*/}
                </PanelDecorator>);
                
       // TODOX }
    }
}

FunctionNode.contextTypes = {
    mode: PropTypes.string,
};

export default FunctionNode;
