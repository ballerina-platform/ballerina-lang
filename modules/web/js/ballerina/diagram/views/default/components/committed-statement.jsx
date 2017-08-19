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
import CommittedStatementAST from './../ast/statements/committed-statement';

/**
 * React component for committed statement.
 *
 * @class CommittedStatement
 * @extends {React.Component}
 */
class CommittedStatement extends React.Component {
    /**
     * Creates an instance of Committed statement.
     * @param {Object} props - react properties.
     *
     * @memberof committedStatement
     * */
    constructor(props) {
        super(props);
        this.onAddAbortedFailedClick = this.onAddAbortedFailedClick.bind(this);
    }

    /**
     * Event handler for click add aborted and failed statement button.
     * */
    onAddAbortedFailedClick() {
        const parent = this.props.model.getParent();
        if (!parent.getAbortedStatement()) {
            parent.createAbortedStatement();
        } else if (!parent.getFailedStatement()) {
            parent.createFailedStatement();
        }
    }

    /**
     * Get add aborted failed statement button
     * @return {object} react element or null
     * */
    getAddAbortedFailedStatementButton() {
        const model = this.props.model;
        const parent = model.parent;
        const bBox = model.viewState.bBox;
        if (!parent.getAbortedStatement() || !parent.getFailedStatement()) {
            return (<g onClick={this.onAddAbortedFailedClick}>
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
                >+
                </text>
            </g>);
        }
        return null;
    }

    /**
     * Get block statement decorator for committed statement.
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
                title={'Committed'}
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
            title={'Committed'}
        >
            {children}
        </BlockStatementDecorator>);
    }

    /**
     * Renders view for a commit statement.
     *
     * @returns {object} The view.
     * @memberof CommittedStatement
     */
    render() {
        return this.getBlockStatementDecorator(this.getAddAbortedFailedStatementButton());
    }
}

CommittedStatement.propTypes = {
    model: PropTypes.instanceOf(CommittedStatementAST).isRequired,
};

export default CommittedStatement;
