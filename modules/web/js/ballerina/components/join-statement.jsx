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
import BlockStatementDecorator from './block-statement-decorator';
import CompoundStatementDecorator from './compound-statement-decorator';
import { getComponentForNodeArray } from './utils';
import BallerinaASTFactory from '../ast/ballerina-ast-factory';
import './join-statement.css';
import JoinStatementAST from './../ast/statements/join-statement';

/**
 * React UI component to represent the the join section of the
 * fork-join language construct.
 */
class JoinStatement extends React.Component {

    /**
     * Construct the join ui, set the bound methods to improve performance.
     */
    constructor() {
        super();
        this.thisAddTimeout = this.addTimeout.bind(this);
        this.thisOnDelete = this.onDelete.bind(this);
    }

    /**
     * Join statement has a special delete behavior than the other ,
     * in it parent is deleted instead of the self.
     * @returns {void} nothing
     */
    onDelete() {
        this.props.model.parent.remove();
    }

    /**
     * Add new timeout construct to the parent fork.
     * @returns {void}
     */
    addTimeout() {
        const parent = this.props.model.parent;
        const newTimeoutStatement = BallerinaASTFactory.createTimeoutStatement();
        parent.addChild(newTimeoutStatement);
    }

    /**
     * Override the rendering logic.
     * @returns {XML} rendered component.
     */
    render() {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const children = getComponentForNodeArray(this.props.model.getChildren());
        const props = this.props;
        const parameterBbox = this.props.model.viewState.components.param;

        this.editorOptions = {
            propertyType: 'text',
            key: 'Join Condition',
            model: props.model,
            getterMethod: props.model.getJoinConditionString,
            setterMethod: props.model.setJoinConditionFromString,
        };
        const parameterEditorOptions = {
            propertyType: 'text',
            key: 'Join Parameter',
            value: parameterBbox.text,
            model: props.model,
            getterMethod: props.model.getParameterAsString,
            setterMethod: props.model.setParameterAsString,
        };


        let lifeLineY1;
        let lifeLineY2;
        if (model.children.length > 0) {
            const firstChild = model.children[0].viewState;
            lifeLineY1 = firstChild.bBox.y + firstChild.bBox.h;
            const lastChild = model.children[model.children.length - 1].viewState;
            lifeLineY2 = lastChild.bBox.y + lastChild.components['drop-zone'].h;
        }

        let addTimeoutBtn;
        if (!model.parent.hasTimeout()) {
            addTimeoutBtn = (
                <g onClick={this.thisAddTimeout}>
                    <rect
                        x={bBox.getRight() - 10}
                        y={bBox.getBottom() - 25}
                        width={20}
                        height={20}
                        rx={10}
                        ry={10}
                        className="add-timeout-button"
                    />
                    <text
                        x={bBox.getRight() - 4}
                        y={bBox.getBottom() - 15}
                        width={20}
                        height={20}
                        className="add-timeout-button-label"
                    >+</text>
                </g>);
        } else {
            addTimeoutBtn = null;
        }

        if (model.children.length > 0) {
            children.unshift(
                <line
                    x1={bBox.getCenterX()}
                    y1={lifeLineY1}
                    x2={bBox.getCenterX()}
                    y2={lifeLineY2}
                    className="join-lifeline"
                    key="join-life-line"
                />);
        }
        return (
            <CompoundStatementDecorator
                model={model}
                bBox={bBox}
                onDelete={this.thisOnDelete}
            >
                <BlockStatementDecorator
                    model={model}
                    dropTarget={model}
                    bBox={bBox}
                    title={'Join'}
                    parameterBbox={parameterBbox}
                    utilities={addTimeoutBtn}
                    undeletable
                    parameterEditorOptions={parameterEditorOptions}
                    expression={{ text: model.getJoinConditionString() }}
                    editorOptions={this.editorOptions}
                >
                    {children}
                </BlockStatementDecorator>
            </CompoundStatementDecorator>);
    }

}

JoinStatement.propTypes = {
    model: PropTypes.instanceOf(JoinStatementAST).isRequired,
};


export default JoinStatement;
