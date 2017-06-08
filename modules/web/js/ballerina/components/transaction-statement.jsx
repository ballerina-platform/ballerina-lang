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

class TransactionStatement extends React.Component {
    constructor(props) {
        super(props);
        this.onAddAbortedCommittedClick = this.onAddAbortedCommittedClick.bind(this);
    }

    /**
     * Get add aborted and committed statement button.
     * @return {object}
     * */
    getAddAbortedCommittedStatementButton() {
        let model = this.props.model;
        let parent = model.parent;
        let bBox = model.viewState.bBox;
        if (!parent.getCommittedStatement() || !parent.getAbortedStatement()) {
            return (
                <g onClick={this.onAddAbortedCommittedClick}>
                    <rect x={bBox.x + bBox.w - 20} y={bBox.y + bBox.h - 20} width={20} height={20}
                          className="add-else-button"/>
                    <text x={bBox.x + bBox.w - 15} y={bBox.y + bBox.h - 10} width={20} height={20}
                          className="add-else-button-label">+
                    </text>
                </g>
            );
        }
        return null;
    }

    /**
     * Event Handler for click add aborted and committed button.
     * */
    onAddAbortedCommittedClick() {
        let parent = this.props.model.parent;
        if (!parent.getAbortedStatement()) {
            parent.createAbortedStatement();
        } else if (!parent.getCommittedStatement()) {
            parent.createCommittedStatement();
        }
    }

    /**
     * Get block statement decorator for transaction statement.
     * @param {object} utilities.
     * @return {object}
     * */
    getBlockStatementDecorator(utilities) {
        let model = this.props.model;
        let bBox = model.viewState.bBox;
        let titleWidth = model.viewState.titleWidth;
        let children = getComponentForNodeArray(this.props.model.getChildren());

        // If utilities available add utilities to the block statement.
        if (utilities) {
            return (<BlockStatementDecorator dropTarget={model} bBox={bBox} titleWidth={titleWidth}
                                             title={"Transaction"} model={model.parent}
                                             utilities={utilities}>
                {children}
            </BlockStatementDecorator>);
        } else {
            return (<BlockStatementDecorator dropTarget={model} bBox={bBox} titleWidth={titleWidth}
                                             title={"Transaction"} model={model.parent}>
                {children}
            </BlockStatementDecorator>);
        }
    }

    render() {
        return this.getBlockStatementDecorator(this.getAddAbortedCommittedStatementButton());
    }
}

TransactionStatement.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired
    })
};

export default TransactionStatement;
