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
import { getComponentForNodeArray } from './utils';
import BlockStatementDecorator from './block-statement-decorator';
import SimpleBBox from './../ast/simple-bounding-box';
import * as DesignerDefaults from './../configs/designer-defaults';
import ASTNode from '../ast/node';
import DragDropManager from '../tool-palette/drag-drop-manager';
import './while-statement.css';
import CompoundStatementDecorator from './compound-statement-decorator';

class WhileStatement extends React.Component {

    constructor(props) {
        super(props);
        this.state = { innerDropZoneActivated: false, innerDropZoneDropNotAllowed: false };
    }

    render() {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const expression = model.viewState.components.expression;
        const blockStatementBBox = new SimpleBBox();
        blockStatementBBox.x = bBox.x;
        blockStatementBBox.y = bBox.y + model.viewState.components['drop-zone'].h;
        blockStatementBBox.h = bBox.h - model.viewState.components['drop-zone'].h;
        blockStatementBBox.w = bBox.w;
        const innerDropZoneActivated = this.state.innerDropZoneActivated;
        const innerDropZoneDropNotAllowed = this.state.innerDropZoneDropNotAllowed;
        const dropZoneClassName = ((!innerDropZoneActivated) ? 'inner-drop-zone' : 'inner-drop-zone active')
            + ((innerDropZoneDropNotAllowed) ? ' block' : '');
        const children = getComponentForNodeArray(this.props.model.getChildren());

        this.editorOptions = {
            propertyType: 'text',
            key: 'Condition',
            model,
            getterMethod: model.getConditionString,
            setterMethod: model.setConditionFromString,
        };
        return (<CompoundStatementDecorator model={model} bBox={bBox}>
            <BlockStatementDecorator
                dropTarget={model} bBox={blockStatementBBox} title={'While'}
                expression={expression} editorOptions={this.editorOptions}
            >
                {children}
            </BlockStatementDecorator>
        </CompoundStatementDecorator>);
    }
}

WhileStatement.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    }),
    model: PropTypes.instanceOf(ASTNode).isRequired,
};

WhileStatement.contextTypes = {
	 dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired,
};

export default WhileStatement;
