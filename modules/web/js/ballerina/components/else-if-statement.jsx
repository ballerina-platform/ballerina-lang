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
import BlockStatementDecorator from './block-statement-decorator';
import PropTypes from 'prop-types';
import {getComponentForNodeArray} from './utils';
import BallerinaASTFactory from '../ast/ballerina-ast-factory';

class ElseIfStatement extends React.Component {
    constructor() {
        super();
        this.state = {
            showAddButton: false
        };
        this.onAddElseIfClick = this.onAddElseIfClick.bind(this);
        this.onMouseEnter = this.onMouseEnter.bind(this);
        this.onMouseOut = this.onMouseOut.bind(this);
    }

    onMouseEnter() {
        this.setState({showAddButton: true});
    }

    onMouseOut() {
        this.setState({showAddButton: false});
    }

    onAddElseIfClick() {
        const newElseIfStatement = BallerinaASTFactory.createElseIfStatement();
        const thisNodeIndex = this.props.model.parent.getIndexOfChild(this.props.model);
        this.props.model.parent.addElseIfStatement(newElseIfStatement, thisNodeIndex + 1);
    }

    render() {
        const {model} = this.props;
        const {bBox} = model.viewState;
        const {expression} = model.viewState.components;
        const parent = model.parent;

        const editorOptions = {
            propertyType: 'text',
            key: 'If condition',
            model: this.props.model,
            getterMethod: this.props.model.getConditionString,
            setterMethod: this.props.model.setConditionFromString
        };

        const children = getComponentForNodeArray(this.props.model.getChildren());

        const addElseIfBtn = (
            <g onClick={this.onAddElseIfClick}>
                <rect x={bBox.x+bBox.w-10} y={bBox.y+bBox.h-25} width={20} height={20} rx={10} ry={10} className='add-else-if-button'/>
                <text x={bBox.x+bBox.w-4} y={bBox.y+bBox.h-15} width={20} height={20} className='add-else-if-button-label'>+</text>
            </g>
        );

        return (
            <BlockStatementDecorator dropTarget={model} model={model} bBox={bBox} title={'Else If'} expression={expression}
                utilities={addElseIfBtn} editorOptions={editorOptions}>
                {children}
            </BlockStatementDecorator>
        );
    }
}

ElseIfStatement.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    })
};


export default ElseIfStatement;
