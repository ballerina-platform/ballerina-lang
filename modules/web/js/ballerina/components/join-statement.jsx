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
import CompoundStatementDecorator from './compound-statement-decorator';
import PropTypes from 'prop-types';
import {getComponentForNodeArray} from './utils';
import BallerinaASTFactory from '../ast/ballerina-ast-factory';
import './join-statement.css';

class JoinStatement extends React.Component {

    render() {
        let model = this.props.model,
            bBox = model.viewState.bBox;
        const children = getComponentForNodeArray(this.props.model.getChildren());
        const props = this.props;
        const parameterBbox = this.props.model.viewState.components.param;

        this.editorOptions = {
            propertyType: 'text',
            key: 'Join Condition',
            model: props.model,
            getterMethod: props.model.getJoinType,
            setterMethod: props.model.setJoinType
        };
        const parameterEditorOptions = {
            propertyType: 'text',
            key: 'Join Condition',
            model: props.model,
            getterMethod: props.model.getParameterAsString,
            setterMethod: props.model.setParameterAsString
        };


        let lifeLineY1;
        let lifeLineY2;
        if (model.children.length > 0) {
            const firstChild = model.children[0].viewState;
            lifeLineY1 = firstChild.bBox.y + firstChild.components['drop-zone'].h;
            const lastChild = model.children[model.children.length - 1].viewState;
            lifeLineY2 = lastChild.bBox.y + lastChild.components['drop-zone'].h;
        }

        let addTimeoutBtn;
        if (!model.parent.hasTimeout()) {
            addTimeoutBtn =
                (
                    <g onClick={this.addTimeout.bind(this)}>
                        <rect x={bBox.x + bBox.w - 20} y={bBox.y + bBox.h - 20} width={20} height={20}
                              className='add-timeout-button'/>
                        <text x={bBox.x + bBox.w - 15} y={bBox.y + bBox.h - 10} width={20} height={20}
                              className='add-timeout-button-label'>+
                        </text>
                    </g>
                );
        } else {
            addTimeoutBtn = null;
        }

        return (
            <CompoundStatementDecorator model={model} bBox={bBox} onDelete={this.onDelete.bind(this)}>
                <BlockStatementDecorator model={model} dropTarget={model} bBox={bBox} title={'Join'}
                                         parameterBbox={parameterBbox} utilities={addTimeoutBtn} undeletable={true}
                                         parameterEditorOptions={parameterEditorOptions}
                                         expression={{text: model.getJoinType()}} editorOptions={this.editorOptions}>
                    {model.children.length > 0 &&
                    <line x1={bBox.getCenterX()} y1={lifeLineY1} x2={bBox.getCenterX()} y2={lifeLineY2}
                          className="join-lifeline"/> }
                    {children}
                </BlockStatementDecorator>
            </CompoundStatementDecorator>);
    }

    addTimeout() {
        const parent = this.props.model.parent;
        const newTimeoutStatement = BallerinaASTFactory.createTimeoutStatement();
        parent.addChild(newTimeoutStatement);
    }

    onDelete(){
        this.props.model.parent.remove();
    }
}

JoinStatement.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    })
};


export default JoinStatement;
