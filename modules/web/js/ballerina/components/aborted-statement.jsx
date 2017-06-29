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
import AbortedStatementAST from './../ast/statements/aborted-statement';
import BlockStatementDecorator from './block-statement-decorator';
import { getComponentForNodeArray } from './utils';

/**
 * React component for Aborted Statement.
 *
 * @class AbortedStatement
 * @extends {React.Component}
 */
class AbortedStatement extends React.Component {
    /**
     * Creates an instance of AbortedStatement.
     * @param {Object} props React properties.
     *
     * @memberof AbortedStatement
     */
    constructor(props) {
        super(props);
        this.onAddCommittedClick = this.onAddCommittedClick.bind(this);
    }

    /**
     * Event handler for click add committed button.
     * */
    onAddCommittedClick() {
        const parent = this.props.model.parent;
        parent.createCommittedStatement();
    }

    /**
     * Get add committed statement button
     * @return {object} react element or null
     * */
    getAddCommittedStatementButton() {
        const model = this.props.model;
        const parent = model.parent;
        const bBox = model.viewState.bBox;
        if (!parent.getCommittedStatement()) {
            return (<g onClick={this.onAddCommittedClick}>
                <rect
                    x={bBox.x + bBox.w - 10}
                    y={bBox.y + bBox.h - 25}
                    width={20}
                    height={20}
                    rx={10}
                    ry={10}
                    className="add-else-button"
                />
                <text
                    x={bBox.x + bBox.w - 4}
                    y={bBox.y + bBox.h - 15}
                    width={20}
                    height={20}
                    className="add-else-button-label"
                >+</text>
            </g>);
        }
        return null;
    }

    /**
     * Get block statement decorator for aborted statement.
     * @param {object} utilities utilities for BlockStatementDecorator.
     * @return {object} View of a BlockStatementDecorator.
     * */
    getBlockStatementDecorator(utilities) {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const titleWidth = model.viewState.titleWidth;
        const children = getComponentForNodeArray(model.getChildren());
        if (utilities) {
            return (<BlockStatementDecorator
                model={model}
                dropTarget={model}
                bBox={bBox}
                titleWidth={titleWidth}
                title={'Aborted'}
                utilities={utilities}
            >
                {children}
            </BlockStatementDecorator>);
        }
        return (<BlockStatementDecorator
            model={model}
            dropTarget={model}
            bBox={bBox}
            titleWidth={titleWidth}
            title={'Aborted'}
        >
            {children}
        </BlockStatementDecorator>);
    }

    /**
     * Renders the view for a aborted statement.
     *
     * @returns {object} The view.
     *
     * @memberof AbortedStatement
     */
    render() {
        return this.getBlockStatementDecorator(this.getAddCommittedStatementButton());
    }
}

AbortedStatement.propTypes = {
    model: PropTypes.instanceOf(AbortedStatementAST).isRequired,
};

export default AbortedStatement;
