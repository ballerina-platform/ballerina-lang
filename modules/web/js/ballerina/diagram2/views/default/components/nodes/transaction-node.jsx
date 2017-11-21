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
import AddCompoundBlock from './add-compound-block';
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
     * get Add block button rendering view.
     * @return {XML} return node.
     * */
    getAddBlockButton() {
        const model = this.props.model;
        const transactionBody = model.transactionBody;
        const failedBody = model.failedBody;
        const abortedBody = model.abortedBody;
        const committedBody = model.committedBody;
        const blocksToBeAdded = [];

        if (!failedBody) {
            const failedBlock = {
                name: 'failed',
                addBlock: this.addFailedBody,
            };
            blocksToBeAdded.push(failedBlock);
        }

        if (!abortedBody) {
            const abortedBlock = {
                name: 'aborted',
                addBlock: this.addAbortedBody,
            };
            blocksToBeAdded.push(abortedBlock);
        }

        if (!committedBody) {
            const committedBlock = {
                name: 'committed',
                addBlock: this.addCommittedBody,
            };
            blocksToBeAdded.push(committedBlock);
        }

        if (blocksToBeAdded.length > 0) {
            return (
                <AddCompoundBlock
                    blocksToBeAdded={blocksToBeAdded}
                    model={transactionBody}
                />
            );
        } else {
            return null;
        }
    }

    /**
     * Add failed body to transaction statement.
     * */
    addFailedBody() {
        const parent = this.props.model;
        const newTransactionBlock = DefaultNodeFactory.createTransaction();
        const newFailedBlock = newTransactionBlock.failedBody;
        newFailedBlock.clearWS();
        parent.setFailedBody(newFailedBlock, false, 'Add Failed Body');
    }

    /**
     * Add aborted body to transaction statement.
     * */
    addAbortedBody() {
        const parent = this.props.model;
        const newTransactionBlock = DefaultNodeFactory.createTransaction();
        const newAbortedBlock = newTransactionBlock.abortedBody;
        newAbortedBlock.clearWS();
        parent.setAbortedBody(newAbortedBlock, false, 'Add Aborted Body');
    }

    /**
     * Add committed body to transaction statement.
     * */
    addCommittedBody() {
        const parent = this.props.model;
        const newTransactionBlock = DefaultNodeFactory.createTransaction();
        const newCommittedBlock = newTransactionBlock.committedBody;
        newCommittedBlock.clearWS();
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
                    title={'transaction'}
                    titleWidth={transactionBody.viewState.components.titleWidth.w}
                    model={model}
                    body={transactionBody}
                />
                {this.getAddBlockButton()}
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
                {abortedBody &&
                <CompoundStatementDecorator
                    bBox={abortedBody.viewState.bBox}
                    title={'Aborted'}
                    titleWidth={abortedBody.viewState.components.titleWidth.w}
                    model={abortedBody}
                    body={abortedBody}
                />
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
