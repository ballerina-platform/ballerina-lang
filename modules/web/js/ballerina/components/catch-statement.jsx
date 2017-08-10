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
import { getComponentForNodeArray } from './utils';
import BlockStatementDecorator from './block-statement-decorator';
import CatchStatementAST from './../ast/statements/catch-statement';
import BallerinaASTFactory from './../ast/ballerina-ast-factory';
import './try-catch-statement.css';

/**
 * React component for a catch statement.
 *
 * @class CatchStatement
 * @extends {React.Component}
 */
class CatchStatement extends React.Component {

    /**
     * Creates an instance of CatchStatement.
     * @param {Object} props React properties.
     * @memberof CatchStatement
     */
    constructor(props) {
        super(props);
        this.editorOptions = {
            propertyType: 'text',
            key: 'Catch parameter',
            model: props.model,
            getterMethod: props.model.getParameterDefString,
            setterMethod: props.model.setParameterDefString,
        };
        this.onAddFinallyClick = this.onAddFinallyClick.bind(this);
    }

    /**
     * Add a new finally clause
     */
    onAddFinallyClick() {
        const parent = this.props.model.parent;
        const newStatement = BallerinaASTFactory.createFinallyStatement();
        parent.addChild(newStatement);
    }

    /**
     * Renders the view for a catch statement.
     *
     * @returns {ReactElement} The view.
     * @memberof CatchStatement
     */
    render() {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const expression = model.viewState.components.expression;
        const children = getComponentForNodeArray(this.props.model.getChildren());
        const addFinallyBtn = (
            <g onClick={this.onAddFinallyClick}>
                <rect
                    x={bBox.x + bBox.w - 10}
                    y={bBox.y + bBox.h - 25}
                    width={20}
                    height={20}
                    rx={10}
                    ry={10}
                    className="add-finally-button"
                />
                <text
                    x={bBox.x + bBox.w - 4}
                    y={bBox.y + bBox.h - 15}
                    width={20}
                    height={20}
                    className="add-finally-button-label"
                >
                    +
                </text>
            </g>
        );
        const catchStatements = _.filter(model.getParent().getChildren(), (child) => {
            return BallerinaASTFactory.isCatchStatement(child);
        });
        const nodeIndex = _.findIndex(catchStatements, (child) => {
            return child.getID() === model.getID();
        });

        const addFinally = (catchStatements.length - 1 === nodeIndex)
            && _.isNil(model.getParent().getFinallyStatement());

        return (<BlockStatementDecorator
            dropTarget={model}
            model={model}
            bBox={bBox}
            title={'Catch'}
            expression={expression}
            editorOptions={this.editorOptions}
            utilities={addFinally ? addFinallyBtn : undefined}
        >
            {children}
        </BlockStatementDecorator>);
    }
}

CatchStatement.propTypes = {
    model: PropTypes.instanceOf(CatchStatementAST).isRequired,
};


export default CatchStatement;
