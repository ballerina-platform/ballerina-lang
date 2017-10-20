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
import DropZone from './../../../../../drag-drop/DropZone';
import DefaultNodeFactory from '../../../../../model/default-node-factory';
import './if-node.css';

/**
 * Class for transaction statement view
 * @extends React.Component
 * @class TransactionNode
 * */
class TransactionNode extends React.Component {

    /**
     * Constructor for Transaction node.
     * @param {Object} props - properties for creating transaction node.
     * */
    constructor(props) {
        super(props);

        this.state = {
            innerDropZoneExist: false,
            active: 'hidden',
        };

        // Bind this context to the following functions.
        this.addCommittedBody = this.addCommittedBody.bind(this);
        this.addAbortedBody = this.addAbortedBody.bind(this);
        this.addFailedBody = this.addFailedBody.bind(this);
    }

    /**
     * Add failed body to transaction statement.
     * */
    addFailedBody() {
        const parent = this.props.model;
        const newTransactionBlock = DefaultNodeFactory.createTransaction();
        const newFailedBlock = newTransactionBlock.failedBody;
        parent.setFailedBody(newFailedBlock, false, 'Add Failed Body');
    }

    /**
     * Add aborted body to transaction statement.
     * */
    addAbortedBody() {
        const parent = this.props.model;
        const newTransactionBlock = DefaultNodeFactory.createTransaction();
        const newAbortedBlock = newTransactionBlock.abortedBody;
        parent.setAbortedBody(newAbortedBlock, false, 'Add Aborted Body');
    }

    /**
     * Add committed body to transaction statement.
     * */
    addCommittedBody() {
        const parent = this.props.model;
        const newTransactionBlock = DefaultNodeFactory.createTransaction();
        const newCommittedBlock = newTransactionBlock.committedBody;
        parent.setCommittedBody(newCommittedBlock, false, 'Add Committed Body');
    }

    /**
     * Render the transaction node.
     * @return {XML} react component for transaction.
     * */
    render() {
        const model = this.props.model;
        const transactionBody = model.transactionBody;
        const failedBody = model.failedBody;
        const abortedBody = model.abortedBody;
        const committedBody = model.committedBody;
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
                    enableDragBg
                    enableCenterOverlayLine
                />

                <CompoundStatementDecorator
                    dropTarget={model}
                    bBox={transactionBody.viewState.bBox}
                    title={'Transaction'}
                    titleWidth={transactionBody.viewState.components.titleWidth.w}
                    model={model}
                    body={transactionBody}
                />
                {!failedBody &&
                <g onClick={this.addFailedBody}>
                    <title>Add Failed</title>
                    <rect
                        x={transactionBody.viewState.components['statement-box'].x
                        + transactionBody.viewState.components['statement-box'].w
                        + transactionBody.viewState.bBox.expansionW - 10}
                        y={transactionBody.viewState.components['statement-box'].y
                        + transactionBody.viewState.components['statement-box'].h - 25}
                        width={20}
                        height={20}
                        rx={10}
                        ry={10}
                        className="add-catch-button"
                    />
                    <text
                        x={transactionBody.viewState.components['statement-box'].x
                        + transactionBody.viewState.components['statement-box'].w
                        + transactionBody.viewState.bBox.expansionW - 4}
                        y={transactionBody.viewState.components['statement-box'].y
                        + transactionBody.viewState.components['statement-box'].h - 15}
                        width={20}
                        height={20}
                        className="add-catch-button-label"
                    >
                        +
                    </text>
                </g>
                }
                {failedBody &&
                <CompoundStatementDecorator
                    dropTarget={failedBody}
                    bBox={failedBody.viewState.bBox}
                    title={'Failed'}
                    titleWidth={failedBody.viewState.components.titleWidth.w}
                    model={failedBody}
                    body={failedBody}
                />
                }
                {failedBody && !abortedBody &&
                <g onClick={this.addAbortedBody}>
                    <title>Add Aborted</title>
                    <rect
                        x={failedBody.viewState.components['statement-box'].x
                        + failedBody.viewState.components['statement-box'].w
                        + failedBody.viewState.bBox.expansionW - 10}
                        y={failedBody.viewState.components['statement-box'].y
                        + failedBody.viewState.components['statement-box'].h - 25}
                        width={20}
                        height={20}
                        rx={10}
                        ry={10}
                        className="add-catch-button"
                    />
                    <text
                        x={failedBody.viewState.components['statement-box'].x
                        + failedBody.viewState.components['statement-box'].w
                        + failedBody.viewState.bBox.expansionW - 4}
                        y={failedBody.viewState.components['statement-box'].y
                        + failedBody.viewState.components['statement-box'].h - 15}
                        width={20}
                        height={20}
                        className="add-catch-button-label"
                    >
                        +
                    </text>
                </g>
                }
                {abortedBody &&
                <CompoundStatementDecorator
                    bBox={abortedBody.viewState.bBox}
                    title={'Aborted'}
                    titleWidth={abortedBody.viewState.components.titleWidth.w}
                    model={abortedBody}
                    body={abortedBody}
                />
                }
                {abortedBody && !committedBody &&
                <g onClick={this.addCommittedBody}>
                    <title>Add Committed</title>
                    <rect
                        x={abortedBody.viewState.components['statement-box'].x
                        + abortedBody.viewState.components['statement-box'].w
                        + abortedBody.viewState.bBox.expansionW - 10}
                        y={abortedBody.viewState.components['statement-box'].y
                        + abortedBody.viewState.components['statement-box'].h - 25}
                        width={20}
                        height={20}
                        rx={10}
                        ry={10}
                        className="add-catch-button"
                    />
                    <text
                        x={abortedBody.viewState.components['statement-box'].x
                        + abortedBody.viewState.components['statement-box'].w
                        + abortedBody.viewState.bBox.expansionW - 4}
                        y={abortedBody.viewState.components['statement-box'].y
                        + abortedBody.viewState.components['statement-box'].h - 15}
                        width={20}
                        height={20}
                        className="add-catch-button-label"
                    >
                        +
                    </text>
                </g>
                }
                {committedBody &&
                <CompoundStatementDecorator
                    bBox={committedBody.viewState.bBox}
                    title={'Committed'}
                    titleWidth={committedBody.viewState.components.titleWidth.w}
                    model={committedBody}
                    body={committedBody}
                />
                }
            </g>
        );
    }
}

TransactionNode.propTypes = {
    model: PropTypes.shape().isRequired,
};

TransactionNode.contextTypes = {
    mode: PropTypes.string,
};

export default TransactionNode;
