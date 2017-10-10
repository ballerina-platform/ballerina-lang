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
import CompoundStatementDecorator from './compound-statement-decorator';
import { getComponentForNodeArray } from './../../../../diagram-util';
import TryNodeModel from './../../../../../model/tree/try-node';
import DropZone from './../../../../../drag-drop/DropZone';
import './try-node.css';

class TryNode extends React.Component {

    constructor(props) {
        super(props);
        this.editorOptions = {
            propertyType: 'text',
            key: 'Try condition',
            model: props.model,
            getterMethod: props.model.getConditionString,
            setterMethod: props.model.setConditionFromString,
        };
        this.onAddElseClick = this.onAddElseClick.bind(this);
    }

    onAddElseClick() {
    }

    render() {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const expression = model.viewState.components.expression;
        const catchViews = getComponentForNodeArray(model.catchBlocks);
        const dropZone = model.viewState.components['drop-zone'];
        return (
            <g>
                <DropZone
                    x={dropZone.x}
                    y={dropZone.y}
                    width={dropZone.w}
                    height={dropZone.h}
                    baseComponent="rect"
                    dropTarget={model.parent}
                    dropBefore={model}
                    renderUponDragStart
                />
                <CompoundStatementDecorator
                    dropTarget={model}
                    bBox={bBox}
                    title={'Try'}
                    expression={expression}
                    editorOptions={this.editorOptions}
                    model={model}
                    body={model.body}
                />
                {catchViews}
                <CompoundStatementDecorator
                    bBox={bBox}
                    title={'Finally'}
                    expression={expression}
                    model={model.finallyBody}
                    body={model.finallyBody}
                />
            </g>
        );
    }
}

TryNode.propTypes = {
    model: PropTypes.instanceOf(TryNodeModel).isRequired,
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    }),
};

TryNode.contextTypes = {
    mode: PropTypes.string,
};

export default TryNode;
